package org.example.payment.dto;

import org.example.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload with payment details.
 *
 * @param id payment identifier
 * @param idempotencyKey client idempotency key
 * @param sourceAccount source account number
 * @param destinationAccount destination account number
 * @param amount transfer amount
 * @param currency payment currency
 * @param reference optional payment reference
 * @param status current payment status
 * @param errorCode error code when payment fails
 * @param errorMessage error message when payment fails
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record PaymentResponse(
        String id,
        String idempotencyKey,
        String sourceAccount,
        String destinationAccount,
        BigDecimal amount,
        String currency,
        String reference,
        PaymentStatus status,
        String errorCode,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

