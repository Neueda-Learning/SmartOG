package org.example.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FailPaymentRequest(
        @NotBlank(message = "errorCode is required")
        @Size(max = 64, message = "errorCode max length is 64")
        String errorCode,

        @NotBlank(message = "errorMessage is required")
        @Size(max = 255, message = "errorMessage max length is 255")
        String errorMessage
) {
}

