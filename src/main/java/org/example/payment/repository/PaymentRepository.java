package org.example.payment.repository;

import org.example.payment.model.Payment;
import org.example.payment.model.PaymentStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PaymentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Payment payment) {
        String sql = """
                INSERT INTO payments (
                    id, idempotency_key, source_account, destination_account, reference, amount,
                    currency, status, error_code, error_message, created_at, updated_at, settlement_reference
                ) VALUES (
                    :id, :idempotencyKey, :sourceAccount, :destinationAccount, :reference, :amount,
                    :currency, :status, :errorCode, :errorMessage, :createdAt, :updatedAt, :settlementReference
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", payment.getId())
                .addValue("idempotencyKey", payment.getIdempotencyKey())
                .addValue("sourceAccount", payment.getSourceAccount())
                .addValue("destinationAccount", payment.getDestinationAccount())
                .addValue("reference", payment.getReference())
                .addValue("amount", payment.getAmount())
                .addValue("currency", payment.getCurrency())
                .addValue("status", payment.getStatus().name())
                .addValue("errorCode", payment.getErrorCode())
                .addValue("errorMessage", payment.getErrorMessage())
                .addValue("createdAt", payment.getCreatedAt())
                .addValue("updatedAt", payment.getUpdatedAt())
                .addValue("settlementReference", payment.getSettlementReference());

        jdbcTemplate.update(sql, params);
    }

    public void update(Payment payment) {
        String sql = """
                UPDATE payments
                SET status = :status,
                    error_code = :errorCode,
                    error_message = :errorMessage,
                    updated_at = :updatedAt,
                    settlement_reference = :settlementReference
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", payment.getId())
                .addValue("status", payment.getStatus().name())
                .addValue("errorCode", payment.getErrorCode())
                .addValue("errorMessage", payment.getErrorMessage())
                .addValue("updatedAt", payment.getUpdatedAt())
                .addValue("settlementReference", payment.getSettlementReference());

        jdbcTemplate.update(sql, params);
    }

    public Optional<Payment> findById(String id) {
        String sql = "SELECT * FROM payments WHERE id = :id";
        List<Payment> result = jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), new PaymentRowMapper());
        return result.stream().findFirst();
    }

    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        String sql = "SELECT * FROM payments WHERE idempotency_key = :idempotencyKey";
        List<Payment> result = jdbcTemplate.query(sql, new MapSqlParameterSource("idempotencyKey", idempotencyKey), new PaymentRowMapper());
        return result.stream().findFirst();
    }

    public List<Payment> findAll() {
        return jdbcTemplate.query("SELECT * FROM payments ORDER BY created_at DESC", new PaymentRowMapper());
    }

    public List<Payment> findByStatus(PaymentStatus status) {
        String sql = "SELECT * FROM payments WHERE status = :status ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("status", status.name()), new PaymentRowMapper());
    }

    public List<Payment> findByAccountNumber(String accountNumber) {
        String sql = """
                SELECT * FROM payments
                WHERE source_account = :accountNumber OR destination_account = :accountNumber
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("accountNumber", accountNumber), new PaymentRowMapper());
    }

    public List<Payment> findByAccountNumberAndStatus(String accountNumber, PaymentStatus status) {
        String sql = """
                SELECT * FROM payments
                WHERE (source_account = :accountNumber OR destination_account = :accountNumber)
                  AND status = :status
                ORDER BY created_at DESC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", accountNumber)
                .addValue("status", status.name());
        return jdbcTemplate.query(sql, params, new PaymentRowMapper());
    }

    private static class PaymentRowMapper implements RowMapper<Payment> {
        @Override
        public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Payment payment = new Payment();
            payment.setId(rs.getString("id"));
            payment.setIdempotencyKey(rs.getString("idempotency_key"));
            payment.setSourceAccount(rs.getString("source_account"));
            payment.setDestinationAccount(rs.getString("destination_account"));
            payment.setReference(rs.getString("reference"));
            payment.setAmount(rs.getBigDecimal("amount"));
            payment.setCurrency(rs.getString("currency"));
            payment.setStatus(PaymentStatus.valueOf(rs.getString("status")));
            payment.setErrorCode(rs.getString("error_code"));
            payment.setErrorMessage(rs.getString("error_message"));
            payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            payment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            payment.setSettlementReference(rs.getString("settlement_reference"));
            return payment;
        }
    }