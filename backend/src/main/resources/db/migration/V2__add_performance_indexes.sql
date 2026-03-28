-- =============================================================================
-- V2: Performance indexes identified during architecture review
-- =============================================================================

-- Composite index for billing account + date range queries
CREATE INDEX IF NOT EXISTS idx_payment_account_date
    ON billing_payments (billing_account_id, created_at);

-- Patient full_name for search queries
CREATE INDEX IF NOT EXISTS idx_patient_name
    ON patients (full_name);

-- Billing account status filtering
CREATE INDEX IF NOT EXISTS idx_billing_acct_status
    ON patient_billing_accounts (bill_status);
