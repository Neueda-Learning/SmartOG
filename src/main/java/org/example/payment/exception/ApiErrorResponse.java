package org.example.payment.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
}

