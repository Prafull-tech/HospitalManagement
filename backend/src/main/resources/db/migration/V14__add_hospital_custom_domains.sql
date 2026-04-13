ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS custom_domain VARCHAR(255);
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS domain_verification_token VARCHAR(120);
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS domain_verification_status VARCHAR(30) DEFAULT 'NOT_CONFIGURED';
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS domain_verified_at TIMESTAMP NULL;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS certificate_status VARCHAR(30) DEFAULT 'NOT_REQUESTED';
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS certificate_requested_at TIMESTAMP NULL;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS certificate_issued_at TIMESTAMP NULL;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS certificate_expires_at TIMESTAMP NULL;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS last_domain_verification_error VARCHAR(1000);
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS last_certificate_error VARCHAR(1000);

CREATE UNIQUE INDEX IF NOT EXISTS idx_hospital_custom_domain ON hospitals (custom_domain);

UPDATE hospitals
SET domain_verification_status = CASE
    WHEN custom_domain IS NULL OR TRIM(custom_domain) = '' THEN 'NOT_CONFIGURED'
    WHEN domain_verification_status IS NULL OR TRIM(domain_verification_status) = '' THEN 'PENDING'
    ELSE domain_verification_status
END,
certificate_status = CASE
    WHEN custom_domain IS NULL OR TRIM(custom_domain) = '' THEN 'NOT_REQUESTED'
    WHEN certificate_status IS NULL OR TRIM(certificate_status) = '' THEN 'NOT_REQUESTED'
    ELSE certificate_status
END;