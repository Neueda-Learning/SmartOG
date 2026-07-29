package org.example.payment.dto;

import org.example.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** API response payload describing a payment. */
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
