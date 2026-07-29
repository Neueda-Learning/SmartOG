package org.example.payment.model;

/**
 * Supported settlement currencies. Used to validate the currency
 * selected on the frontend against a canonical, type-safe list,
 * as an additional check on top of the existing string-set validation
 * already present in PaymentService.
 */
public enum Currency {
    USD,
    EUR,
    GBP;

    public static boolean isSupported(String code) {
        if (code == null) {
            return false;
        }
        for (Currency c : values()) {
            if (c.name().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
