package org.example.payment.repository;

import org.example.payment.model.PaymentStatus;
import org.example.payment.model.PaymentStatusHistory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PaymentStatusHistoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PaymentStatusHistoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(PaymentStatusHistory history) {
        String sql = """
                INSERT INTO payment_status_history (
                    payment_id, from_status, to_status, error_code,
                    error_message, triggered_by, changed_at
                ) VALUES (
                    :paymentId, :fromStatus, :toStatus, :errorCode,
                    :errorMessage, :triggeredBy, :changedAt
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentId", history.getPaymentId())
                .addValue("fromStatus", history.getFromStatus() == null ? null : history.getFromStatus().name())
                .addValue("toStatus", history.getToStatus().name())
                .addValue("errorCode", history.getErrorCode())
                .addValue("errorMessage", history.getErrorMessage())
                .addValue("triggeredBy", history.getTriggeredBy())
                .addValue("changedAt", history.getChangedAt());

        jdbcTemplate.update(sql, params);
    }

    public List<PaymentStatusHistory> findByPaymentId(String paymentId) {
        String sql = "SELECT * FROM payment_status_history WHERE payment_id = :paymentId ORDER BY changed_at ASC";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("paymentId", paymentId), new PaymentStatusHistoryRowMapper());
    }

    private static class PaymentStatusHistoryRowMapper implements RowMapper<PaymentStatusHistory> {
        @Override
        public PaymentStatusHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            PaymentStatusHistory history = new PaymentStatusHistory();
            history.setId(rs.getLong("id"));
            history.setPaymentId(rs.getString("payment_id"));
            String fromStatus = rs.getString("from_status");
            history.setFromStatus(fromStatus == null ? null : PaymentStatus.valueOf(fromStatus));
            history.setToStatus(PaymentStatus.valueOf(rs.getString("to_status")));
            history.setErrorCode(rs.getString("error_code"));
            history.setErrorMessage(rs.getString("error_message"));
            history.setTriggeredBy(rs.getString("triggered_by"));
            history.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
            return history;
        }
    }
}

