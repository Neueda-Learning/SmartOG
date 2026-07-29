-- Demo-only seed dataset for presentation and manual testing.
-- Covers multiple lifecycle outcomes: CREATED, VALIDATED, SENT, COMPLETED, and FAILED.

INSERT INTO payments (
    id, idempotency_key, source_account, destination_account, reference,
    amount, currency, status, error_code, error_message, created_at, updated_at
) VALUES
    ('pay-demo-001', 'demo-key-001', '10000001', '20000001', 'Payroll July batch A', 12000.00, 'USD', 'COMPLETED', NULL, NULL, '2026-07-26 09:00:00', '2026-07-26 09:04:00'),
    ('pay-demo-002', 'demo-key-002', '10000002', '20000002', 'Vendor invoice INV-8841', 875.55, 'USD', 'FAILED', 'INVALID_ACCOUNT', 'Destination account format invalid', '2026-07-26 09:05:00', '2026-07-26 09:06:00'),
    ('pay-demo-003', 'demo-key-003', '10000003', '20000003', 'Refund order RF-1103', 45.99, 'EUR', 'VALIDATED', NULL, NULL, '2026-07-26 09:10:00', '2026-07-26 09:11:00'),
    ('pay-demo-004', 'demo-key-004', '10000004', '20000004', 'Treasury transfer', 250000.00, 'GBP', 'SENT', NULL, NULL, '2026-07-26 09:12:00', '2026-07-26 09:14:00'),
    ('pay-demo-005', 'demo-key-005', '10000005', '20000005', 'Scholarship payout', 500.00, 'USD', 'CREATED', NULL, NULL, '2026-07-26 09:15:00', '2026-07-26 09:15:00'),
    ('pay-demo-006', 'demo-key-006', '10000006', '20000006', 'Travel reimbursement', 199.90, 'USD', 'FAILED', 'NETWORK_ERROR', 'Timeout when contacting destination gateway', '2026-07-26 09:16:00', '2026-07-26 09:18:00'),
    ('pay-demo-007', 'demo-key-007', '10000007', '20000007', 'Contractor payment', 3200.00, 'USD', 'COMPLETED', NULL, NULL, '2026-07-26 09:20:00', '2026-07-26 09:23:00'),
    ('pay-demo-008', 'demo-key-008', '10000008', '20000008', 'Card settlement', 999999.99, 'USD', 'FAILED', 'INSUFFICIENT_FUNDS', 'Source account balance is lower than requested amount', '2026-07-26 09:24:00', '2026-07-26 09:25:00'),
    ('pay-demo-009', 'demo-key-009', '10000009', '20000009', 'Intercompany allocation', 10000.00, 'JPY', 'FAILED', 'INVALID_CURRENCY', 'Currency JPY is not enabled in this environment', '2026-07-26 09:26:00', '2026-07-26 09:27:00'),
    ('pay-demo-010', 'demo-key-010', '10000010', '20000010', 'Subscription payout', 77.77, 'USD', 'FAILED', 'PROCESSING_ERROR', 'Unexpected downstream processing exception', '2026-07-26 09:28:00', '2026-07-26 09:31:00');

INSERT INTO payment_status_history (
    payment_id, from_status, to_status, error_code, error_message, triggered_by, changed_at
) VALUES
    -- pay-demo-001: happy path to COMPLETED
    ('pay-demo-001', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:00:00'),
    ('pay-demo-001', 'CREATED', 'VALIDATED', NULL, NULL, 'demo-seed', '2026-07-26 09:01:00'),
    ('pay-demo-001', 'VALIDATED', 'SENT', NULL, NULL, 'demo-seed', '2026-07-26 09:02:00'),
    ('pay-demo-001', 'SENT', 'COMPLETED', NULL, NULL, 'demo-seed', '2026-07-26 09:04:00'),

    -- pay-demo-002: failed during validation
    ('pay-demo-002', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:05:00'),
    ('pay-demo-002', 'CREATED', 'FAILED', 'INVALID_ACCOUNT', 'Destination account format invalid', 'demo-seed', '2026-07-26 09:06:00'),

    -- pay-demo-003: currently validated and waiting for send
    ('pay-demo-003', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:10:00'),
    ('pay-demo-003', 'CREATED', 'VALIDATED', NULL, NULL, 'demo-seed', '2026-07-26 09:11:00'),

    -- pay-demo-004: sent but not completed yet
    ('pay-demo-004', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:12:00'),
    ('pay-demo-004', 'CREATED', 'VALIDATED', NULL, NULL, 'demo-seed', '2026-07-26 09:13:00'),
    ('pay-demo-004', 'VALIDATED', 'SENT', NULL, NULL, 'demo-seed', '2026-07-26 09:14:00'),

    -- pay-demo-005: just created
    ('pay-demo-005', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:15:00'),

    -- pay-demo-006: failed after send due to transient network issue
    ('pay-demo-006', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:16:00'),
    ('pay-demo-006', 'CREATED', 'VALIDATED', NULL, NULL, 'demo-seed', '2026-07-26 09:17:00'),
    ('pay-demo-006', 'VALIDATED', 'FAILED', 'NETWORK_ERROR', 'Timeout when contacting destination gateway', 'demo-seed', '2026-07-26 09:18:00'),

    -- pay-demo-007: second happy path example
    ('pay-demo-007', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:20:00'),
    ('pay-demo-007', 'CREATED', 'VALIDATED', NULL, NULL, 'demo-seed', '2026-07-26 09:21:00'),
    ('pay-demo-007', 'VALIDATED', 'SENT', NULL, NULL, 'demo-seed', '2026-07-26 09:22:00'),
    ('pay-demo-007', 'SENT', 'COMPLETED', NULL, NULL, 'demo-seed', '2026-07-26 09:23:00'),

    -- pay-demo-008: failed at creation-level validation (funds check)
    ('pay-demo-008', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:24:00'),
    ('pay-demo-008', 'CREATED', 'FAILED', 'INSUFFICIENT_FUNDS', 'Source account balance is lower than requested amount', 'demo-seed', '2026-07-26 09:25:00'),

    -- pay-demo-009: failed because currency is unsupported in this environment
    ('pay-demo-009', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:26:00'),
    ('pay-demo-009', 'CREATED', 'FAILED', 'INVALID_CURRENCY', 'Currency JPY is not enabled in this environment', 'demo-seed', '2026-07-26 09:27:00'),

    -- pay-demo-010: processing failure after send
    ('pay-demo-010', NULL, 'CREATED', NULL, NULL, 'demo-seed', '2026-07-26 09:28:00'),
    ('pay-demo-010', 'CREATED', 'VALIDATED', NULL, NULL, 'demo-seed', '2026-07-26 09:29:00'),
    ('pay-demo-010', 'VALIDATED', 'SENT', NULL, NULL, 'demo-seed', '2026-07-26 09:30:00'),
    ('pay-demo-010', 'SENT', 'FAILED', 'PROCESSING_ERROR', 'Unexpected downstream processing exception', 'demo-seed', '2026-07-26 09:31:00');

