package org.example.payment.service;

import org.example.payment.dto.CreatePaymentRequest;
import org.example.payment.dto.FailPaymentRequest;
import org.example.payment.exception.BusinessException;
import org.example.payment.exception.ErrorCode;
import org.example.payment.model.Account;
import org.example.payment.model.Currency;
import org.example.payment.model.Payment;
import org.example.payment.model.PaymentStatus;
import org.example.payment.model.PaymentStatusHistory;
import org.example.payment.repository.AccountRepository;
import org.example.payment.repository.PaymentRepository;
import org.example.payment.repository.PaymentStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Core service for payment processing.
 *
 * <p>Handles payment creation, status transitions, history tracking, and queries.
 * All status changes are validated by {@link PaymentStateMachine} to enforce
 * the allowed transition rules, and every transition is recorded in
 * {@link org.example.payment.model.PaymentStatusHistory} for a full audit trail.
 * <p>Idempotency: {@link #createPayment} uses {@code idempotencyKey} to ensure
 * the same request never creates duplicate payments.
 */
@Service
public class PaymentService {

    /** Whitelist of accepted currency codes. */
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP");

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository historyRepository;
    private final PaymentStateMachine stateMachine;
    // New: dependencies for balance checking (validate), deduction (send),
    // and currency-to-USD conversion via openexchangerates.org.
    private final AccountRepository accountRepository;
    private final ExchangeRateClient exchangeRateClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentStatusHistoryRepository historyRepository,
            PaymentStateMachine stateMachine,
            AccountRepository accountRepository,
            ExchangeRateClient exchangeRateClient
    ) {
        this.paymentRepository = paymentRepository;
        this.historyRepository = historyRepository;
        this.stateMachine = stateMachine;
        this.accountRepository = accountRepository;
        this.exchangeRateClient = exchangeRateClient;
    }

    /**
     * Creates a new payment (idempotent).
     *
     * <p>If a payment with the same {@code idempotencyKey} already exists,
     * returns the existing record ({@code created=false}) without inserting a duplicate.
     *
     * @param request the create-payment request containing accounts, amount, currency, and idempotency key
     * @return a {@link CreatePaymentResult} with the payment object and a flag indicating whether it was newly created
     * @throws org.example.payment.exception.BusinessException if source and destination accounts are the same,
     *                                                          or if the currency is not supported
     */
    @Transactional
    public CreatePaymentResult createPayment(CreatePaymentRequest request) {
        // Pre-validate: account rules and supported currency
        validateCreateRequest(request);

        // Idempotency check: return existing payment if the key is already known
        return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(existing -> new CreatePaymentResult(existing, false))
                .orElseGet(() -> {
                    // Build a new payment with initial status CREATED
                    Payment payment = new Payment();
                    payment.setId(UUID.randomUUID().toString());
                    payment.setIdempotencyKey(request.idempotencyKey());
                    payment.setSourceAccount(request.sourceAccount());
                    payment.setDestinationAccount(request.destinationAccount());
                    payment.setReference(request.reference());
                    payment.setAmount(request.amount());
                    payment.setCurrency(request.currency());
                    payment.setStatus(PaymentStatus.CREATED);
                    payment.setCreatedAt(LocalDateTime.now());
                    payment.setUpdatedAt(payment.getCreatedAt());

                    // Persist the new payment
                    paymentRepository.insert(payment);
                    // Record initial history entry: null -> CREATED
                    addHistory(payment.getId(), null, PaymentStatus.CREATED, null, null, "API");

                    return new CreatePaymentResult(payment, true);
                });
    }

    /**
     * Retrieves a single payment by its ID.
     *
     * @param paymentId the unique payment identifier
     * @return the matching {@link Payment}
     * @throws org.example.payment.exception.BusinessException with {@code PAYMENT_NOT_FOUND} if no payment exists
     */
    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "Payment not found: " + paymentId));
    }

    /**
     * Lists payments, optionally filtered by status.
     *
     * @param status the status to filter by; pass {@code null} to return all payments
     * @return list of matching payments
     */
    public List<Payment> listPayments(PaymentStatus status, String accountNumber) {
        String account = accountNumber == null ? "" : accountNumber.trim();
        if (!account.isEmpty()) {
            if (status == null) {
                return paymentRepository.findByAccountNumber(account);
            }
            return paymentRepository.findByAccountNumberAndStatus(account, status);
        }
        if (status == null) {
            // No filter applied – return every payment in the system
            return paymentRepository.findAll();
        }
        return paymentRepository.findByStatus(status);
    }

    /**
     * Returns the full status-change history for a given payment.
     *
     * <p>Verifies that the payment exists before querying history.
     *
     * @param paymentId the unique payment identifier
     * @return ordered list of {@link PaymentStatusHistory} entries
     */
    public List<PaymentStatusHistory> getHistory(String paymentId) {
        // Ensure the payment exists; throws PAYMENT_NOT_FOUND otherwise
        getPayment(paymentId);
        return historyRepository.findByPaymentId(paymentId);
    }

    /**
     * Transitions the payment to {@code VALIDATED}.
     *
     * <p>New: before allowing the transition, looks up the source account,
     * converts the payment amount into USD via {@link ExchangeRateClient},
     * and rejects the request with {@code INSUFFICIENT_FUNDS} if the
     * account balance (also expressed in USD) is not enough.
     *
     * @param paymentId the unique payment identifier
     * @return the updated {@link Payment}
     * @throws BusinessException if the source account is missing or has insufficient funds
     */
    @Transactional
    public Payment validate(String paymentId) {
        Payment payment = getPayment(paymentId);

        Account account = accountRepository.findByAccountNumber(payment.getSourceAccount())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_ACCOUNT,
                        "Source account not found: " + payment.getSourceAccount()
                ));

        BigDecimal paymentRateToUsd = exchangeRateClient.getRateToUsd(payment.getCurrency());
        BigDecimal amountInUsd = payment.getAmount().multiply(paymentRateToUsd);

        BigDecimal accountBalanceInUsd = "USD".equalsIgnoreCase(account.getCurrency())
                ? account.getBalance()
                : account.getBalance().multiply(exchangeRateClient.getRateToUsd(account.getCurrency()));

        if (accountBalanceInUsd.compareTo(amountInUsd) < 0) {
            throw new BusinessException(
                    ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient funds: balance(USD)=" + accountBalanceInUsd + " required(USD)=" + amountInUsd
            );
        }

        return transition(paymentId, PaymentStatus.VALIDATED, null, null, "API_VALIDATE");
    }

    /**
     * Transitions the payment to {@code SENT}.
     *
     * <p>New: deducts the payment amount from the source account balance,
     * logs the resulting balance, generates a random settlement receipt
     * reference, and simulates notifying the downstream system with it.
     *
     * @param paymentId the unique payment identifier
     * @return the updated {@link Payment}, including the generated {@code settlementReference}
     */
    @Transactional
    public Payment send(String paymentId) {
        Payment payment = getPayment(paymentId);

        // Deduct in USD because the account table uses USD by default.
        BigDecimal amountInUsd = payment.getAmount()
                .multiply(exchangeRateClient.getRateToUsd(payment.getCurrency()))
                .setScale(2, RoundingMode.HALF_UP);

        accountRepository.deductBalance(payment.getSourceAccount(), amountInUsd);
        accountRepository.findByAccountNumber(payment.getSourceAccount()).ifPresent(updated ->
                System.out.println("Balance after deduction for " + updated.getAccountNumber()
                        + ": " + updated.getBalance() + " " + updated.getCurrency()
                        + " (deducted " + amountInUsd + " USD)")
        );

        String settlementReference = String.valueOf(java.util.concurrent.ThreadLocalRandom.current().nextInt(2));
        System.out.println("Notifying downstream system for payment " + payment.getId()
                + " with settlementReference=" + settlementReference + " (0=no receipt, 1=receipt)");

        Payment updated = transition(paymentId, PaymentStatus.SENT, null, null, "API_SEND");
        updated.setSettlementReference(settlementReference);
        paymentRepository.update(updated);
        return updated;
    }

    /**
     * Transitions the payment to {@code COMPLETED}.
     *
     * <p>New: requires that a settlement receipt (generated by {@link #send})
     * is already present; otherwise the completion is rejected.
     *
     * @param paymentId the unique payment identifier
     * @return the updated {@link Payment}
     * @throws BusinessException if no settlement receipt was recorded during "send"
     */
    @Transactional
    public Payment complete(String paymentId) {
        Payment payment = getPayment(paymentId);

        if (!"1".equals(payment.getSettlementReference())) {
            throw new BusinessException(
                    ErrorCode.PROCESSING_ERROR,
                    "Cannot complete payment because receipt flag is not 1"
            );
        }

        return transition(paymentId, PaymentStatus.COMPLETED, null, null, "API_COMPLETE");
    }

    /**
     * Transitions the payment to {@code FAILED} and stores the error details.
     *
     * @param paymentId the unique payment identifier
     * @param request   the failure reason containing {@code errorCode} and {@code errorMessage}
     * @return the updated {@link Payment}
     */
    @Transactional
    public Payment fail(String paymentId, FailPaymentRequest request) {
        return transition(paymentId, PaymentStatus.FAILED, request.errorCode(), request.errorMessage(), "API_FAIL");
    }

    /**
     * Validates the create-payment request before any persistence.
     *
     * <ul>
     *   <li>Source and destination accounts must differ.</li>
     *   <li>Currency must be one of USD, EUR, or GBP.</li>
     * </ul>
     *
     *
     * @param request the request to validate
     * @throws BusinessException if any rule is violated
     */
    private void validateCreateRequest(CreatePaymentRequest request) {
        if (request.sourceAccount().equals(request.destinationAccount())) {
            throw new BusinessException(ErrorCode.INVALID_ACCOUNT, "sourceAccount and destinationAccount must be different");
        }
        if (!SUPPORTED_CURRENCIES.contains(request.currency())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENCY, "Unsupported currency: " + request.currency());
        }
        // New: additional type-safe check against the Currency enum,
        // matching the currency selected on the frontend.
        if (!Currency.isSupported(request.currency())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENCY, "Currency not recognized: " + request.currency());
        }
    }

    /**
     * Generic helper that drives a payment through a status transition.
     *
     * <ol>
     *   <li>Loads the payment and asserts the transition is allowed by the state machine.</li>
     *   <li>Updates the payment record in the database.</li>
     *   <li>Appends a history entry for the transition.</li>
     * </ol>
     *
     * @param paymentId    the unique payment identifier
     * @param targetStatus the desired next status
     * @param errorCode    error code to store (only relevant for FAILED transitions; otherwise {@code null})
     * @param errorMessage error description (only relevant for FAILED transitions; otherwise {@code null})
     * @param triggeredBy  label identifying what triggered this transition (e.g. "API_VALIDATE")
     * @return the updated {@link Payment}
     */
    private Payment transition(
            String paymentId,
            PaymentStatus targetStatus,
            String errorCode,
            String errorMessage,
            String triggeredBy
    ) {
        Payment payment = getPayment(paymentId);
        // Guard: verify the state machine allows this transition
        stateMachine.assertCanTransition(payment.getStatus(), targetStatus);

        PaymentStatus fromStatus = payment.getStatus();
        payment.setStatus(targetStatus);
        payment.setErrorCode(errorCode);
        payment.setErrorMessage(errorMessage);
        payment.setUpdatedAt(LocalDateTime.now());
        // Persist the status change
        paymentRepository.update(payment);

        // Write audit history so every transition is traceable
        addHistory(payment.getId(), fromStatus, targetStatus, errorCode, errorMessage, triggeredBy);
        return payment;
    }

    /**
     * Inserts a single status-change record into the history table.
     *
     * @param paymentId    the payment this history entry belongs to
     * @param from         the previous status ({@code null} for the initial CREATED entry)
     * @param to           the new status
     * @param errorCode    error code, if applicable (may be {@code null})
     * @param errorMessage error description, if applicable (may be {@code null})
     * @param triggeredBy  source of the change (e.g. "API", "API_FAIL")
     */
    private void addHistory(
            String paymentId,
            PaymentStatus from,
            PaymentStatus to,
            String errorCode,
            String errorMessage,
            String triggeredBy
    ) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPaymentId(paymentId);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setErrorCode(errorCode);
        history.setErrorMessage(errorMessage);
        history.setTriggeredBy(triggeredBy);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.insert(history);
    }

    /**
     * Result wrapper for {@link #createPayment}.
     *
     * @param payment the payment object (new or existing)
     * @param created {@code true} if this request created a brand-new payment;
     *                {@code false} if an existing record was returned due to idempotency
     */
    public record CreatePaymentResult(Payment payment, boolean created) {
    }
}
