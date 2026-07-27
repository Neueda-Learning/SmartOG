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



