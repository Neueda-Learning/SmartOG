package org.example.payment.dto;

import org.example.payment.model.PaymentStatus;

import java.time.LocalDateTime;

/** API response payload for one payment status transition record. */
public record PaymentHistoryResponse(
        Long id,
        String paymentId,
        PaymentStatus fromStatus,
        PaymentStatus toStatus,
        String errorCode,
        String errorMessage,
        String triggeredBy,
        LocalDateTime changedAt
) {
}
