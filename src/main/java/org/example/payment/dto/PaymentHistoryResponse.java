package org.example.payment.dto;

import org.example.payment.model.PaymentStatus;

import java.time.LocalDateTime;

/**
 * Response item for one payment status change in history.
 *
 * @param id history row id
 * @param paymentId payment identifier
 * @param fromStatus previous payment status
 * @param toStatus new payment status
 * @param errorCode error code recorded for this change
 * @param errorMessage error message recorded for this change
 * @param triggeredBy actor or system that triggered this change
 * @param changedAt time when the status changed
 */
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

