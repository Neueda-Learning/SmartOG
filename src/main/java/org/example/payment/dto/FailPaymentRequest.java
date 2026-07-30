package org.example.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for marking a payment as failed.
 *
 * @param errorCode business or system error code
 * @param errorMessage human-readable error message
 */
public record FailPaymentRequest(
        @NotBlank(message = "errorCode is required")
        @Size(max = 64, message = "errorCode max length is 64")
        String errorCode,

        @NotBlank(message = "errorMessage is required")
        @Size(max = 255, message = "errorMessage max length is 255")
        String errorMessage
) {
}

