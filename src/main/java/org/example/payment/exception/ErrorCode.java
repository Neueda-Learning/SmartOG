package org.example.payment.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT(HttpStatus.BAD_REQUEST),
    INVALID_CURRENCY(HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

