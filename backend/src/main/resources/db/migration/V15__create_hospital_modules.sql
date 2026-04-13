CREATE TABLE hospital_modules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    hospital_id BIGINT NOT NULL,
    module_code VARCHAR(50) NOT NULL,
    enabled BIT NOT NULL DEFAULT 1,
    CONSTRAINT pk_hospital_modules PRIMARY KEY (id),
    CONSTRAINT fk_hospital_modules_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT uk_hospital_module_code UNIQUE (hospital_id, module_code)
);

CREATE INDEX idx_hospital_module_hospital ON hospital_modules (hospital_id);
CREATE INDEX idx_hospital_module_code ON hospital_modules (module_code);