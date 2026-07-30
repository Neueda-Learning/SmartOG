package org.example.payment.exception;

/**
 * Runtime exception used for business rule errors.
 */
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    /**
     * Creates a business exception with a code and message.
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the application error code.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

