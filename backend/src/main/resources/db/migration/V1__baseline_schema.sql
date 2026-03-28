-- =============================================================================
-- V1: Baseline schema — generated from Hibernate auto-DDL state.
-- This migration establishes the baseline for Flyway tracking.
-- All tables listed here should already exist in existing databases;
-- Flyway baseline-on-migrate handles this gracefully.
-- =============================================================================

-- Auth module
CREATE TABLE IF NOT EXISTS hms_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY idx_user_username (username),
    KEY idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Refresh tokens (new in this release)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY idx_refresh_token (token),
    KEY idx_refresh_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Reception module
CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uhid VARCHAR(50) NOT NULL,
    registration_number VARCHAR(50) NOT NULL,
    registration_date DATETIME(6) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    id_proof_type VARCHAR(50),
    id_proof_number VARCHAR(100),
    date_of_birth DATE,
    age INT NOT NULL,
    age_years INT,
    age_months INT,
    age_days INT,
    gender VARCHAR(20) NOT NULL,
    weight_kg DOUBLE,
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pin_code VARCHAR(10),
    country VARCHAR(100),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relation VARCHAR(50),
    blood_group VARCHAR(10),
    marital_status VARCHAR(20),
    occupation VARCHAR(100),
    nationality VARCHAR(50),
    religion VARCHAR(50),
    referred_by VARCHAR(255),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY idx_patient_uhid (uhid),
    UNIQUE KEY idx_patient_reg_no (registration_number),
    KEY idx_patient_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Billing module
CREATE TABLE IF NOT EXISTS patient_billing_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    ipd_admission_id BIGINT,
    opd_visit_id BIGINT,
    bill_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    pending_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    insurance_type VARCHAR(50),
    tpa_approval_status VARCHAR(50),
    corporate BOOLEAN DEFAULT FALSE,
    corporate_approved BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6),
    KEY idx_billing_account_status (bill_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS billing_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    billing_account_id BIGINT NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    service_name VARCHAR(255),
    reference_id BIGINT,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    gst_percent DECIMAL(5,2),
    cgst DECIMAL(15,2),
    sgst DECIMAL(15,2),
    igst DECIMAL(15,2),
    department VARCHAR(100),
    created_by VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'POSTED',
    created_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS billing_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ipd_admission_id BIGINT,
    billing_account_id BIGINT,
    amount DECIMAL(15,2) NOT NULL,
    mode VARCHAR(30) NOT NULL,
    reference_no VARCHAR(100),
    created_by VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    correlation_id VARCHAR(100),
    KEY idx_payment_ipd (ipd_admission_id),
    KEY idx_payment_created (created_at),
    KEY idx_payment_account_date (billing_account_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS billing_refunds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    billing_account_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    reason VARCHAR(500),
    mode VARCHAR(30),
    reference_no VARCHAR(100),
    created_by VARCHAR(255),
    created_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
