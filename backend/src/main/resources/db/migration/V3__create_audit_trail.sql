-- =============================================================================
-- V3: Centralized audit trail table for NABH/medico-legal compliance
-- =============================================================================

CREATE TABLE IF NOT EXISTS audit_trail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(50) NOT NULL,
    username VARCHAR(50),
    details TEXT,
    ip_address VARCHAR(45),
    correlation_id VARCHAR(100),
    created_at DATETIME(6) NOT NULL,
    KEY idx_audit_entity (entity_type, entity_id),
    KEY idx_audit_user (username),
    KEY idx_audit_timestamp (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
