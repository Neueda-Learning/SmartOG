package org.example.payment.repository;

import org.example.payment.model.Account;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

/**
 * Read/write access to the "accounts" table, which stores the current
 * balance for each settlement account used by the payment lifecycle
 * (source account balance is checked at "validate" and deducted at "send").
 */
@Repository
public class AccountRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AccountRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT account_number, balance, currency FROM accounts WHERE account_number = :accountNumber";
        MapSqlParameterSource params = new MapSqlParameterSource("accountNumber", accountNumber);
        List<Account> results = jdbcTemplate.query(sql, params, this::mapRow);
        return results.stream().findFirst();
    }

    public void deductBalance(String accountNumber, BigDecimal amount) {
        String sql = "UPDATE accounts SET balance = balance - :amount WHERE account_number = :accountNumber";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("amount", amount)
                .addValue("accountNumber", accountNumber);
        jdbcTemplate.update(sql, params);
    }

    private Account mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
        Account account = new Account();
        account.setAccountNumber(rs.getString("account_number"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setCurrency(rs.getString("currency"));
        return account;
    }
}

