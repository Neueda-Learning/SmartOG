package org.example.payment.exception;

import java.time.LocalDateTime;

/**
 * Standard error response returned by API endpoints.
 *
 * @param errorCode application error code
 * @param message error details
 * @param timestamp time when the error happened
 */
public record ApiErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
}

