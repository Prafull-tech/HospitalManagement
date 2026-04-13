CREATE TABLE IF NOT EXISTS hospitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_code VARCHAR(50) NOT NULL,
    hospital_name VARCHAR(255) NOT NULL,
    location VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    UNIQUE KEY idx_hospital_code (hospital_code),
    KEY idx_hospital_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE hms_users
    ADD COLUMN IF NOT EXISTS hospital_id BIGINT NULL;

ALTER TABLE hms_users
    ADD INDEX IF NOT EXISTS idx_user_hospital_role (hospital_id, role);

ALTER TABLE hms_users
    ADD CONSTRAINT fk_hms_users_hospital
        FOREIGN KEY (hospital_id) REFERENCES hospitals(id);