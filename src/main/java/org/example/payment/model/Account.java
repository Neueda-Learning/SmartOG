package org.example.payment.model;

import java.math.BigDecimal;

/**
 * Represents a settlement account with a balance kept in its own currency.
 * Used by PaymentService during the "validate" step (balance check) and
 * the "send" step (balance deduction).
 */
public class Account {
    private String accountNumber;
    private BigDecimal balance;
    private String currency;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

