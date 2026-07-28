package org.example.payment.excpetion;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
}

