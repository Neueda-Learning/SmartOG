CREATE TABLE IF NOT EXISTS payments (
                                        id VARCHAR(64) PRIMARY KEY,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    source_account VARCHAR(20) NOT NULL,
    destination_account VARCHAR(20) NOT NULL,
    reference VARCHAR(140),
    amount DECIMAL(14, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_code VARCHAR(64),
    error_message VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_payments_status (status)
    );

CREATE TABLE IF NOT EXISTS payment_status_history (
                                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                      payment_id VARCHAR(64) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    error_code VARCHAR(64),
    error_message VARCHAR(255),
    triggered_by VARCHAR(64) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_history_payment FOREIGN KEY (payment_id) REFERENCES payments(id),
    INDEX idx_history_payment_id (payment_id)
    );

-- New: settlement receipt reference, generated during the "send" step and
-- checked during the "complete" step.
SET @add_settlement_ref_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'payments'
              AND column_name = 'settlement_reference'
        ),
        'SELECT 1',
        'ALTER TABLE payments ADD COLUMN settlement_reference VARCHAR(64)'
    )
);
PREPARE add_settlement_ref_stmt FROM @add_settlement_ref_sql;
EXECUTE add_settlement_ref_stmt;
DEALLOCATE PREPARE add_settlement_ref_stmt;

-- New: account balances used by the "validate" (balance check) and
-- "send" (balance deduction) steps.
CREATE TABLE IF NOT EXISTS accounts (
                                        account_number VARCHAR(20) PRIMARY KEY,
    balance DECIMAL(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD'
    );

INSERT IGNORE INTO accounts (account_number, balance, currency) VALUES ('12345678', 100000.00, 'USD');
INSERT IGNORE INTO accounts (account_number, balance, currency) VALUES ('87654321', 100000.00, 'USD');



