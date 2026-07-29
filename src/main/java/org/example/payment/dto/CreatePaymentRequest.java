package org.example.payment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** API request payload for creating a payment. */
public record CreatePaymentRequest(
        @NotBlank(message = "idempotencyKey is required")
        @Size(max = 64, message = "idempotencyKey max length is 64")
        String idempotencyKey,

        @NotBlank(message = "sourceAccount is required")
        @Pattern(regexp = "^[0-9]{8,20}$", message = "sourceAccount must be 8-20 digits")
        String sourceAccount,

        @NotBlank(message = "destinationAccount is required")
        @Pattern(regexp = "^[0-9]{8,20}$", message = "destinationAccount must be 8-20 digits")
        String destinationAccount,

        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        @DecimalMax(value = "1000000.00", message = "amount must not exceed 1,000,000")
        @Digits(integer = 12, fraction = 2, message = "amount must have up to 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO-4217 format (e.g. USD)")
        String currency,

        @Size(max = 140, message = "reference max length is 140")
        String reference
) {
}
