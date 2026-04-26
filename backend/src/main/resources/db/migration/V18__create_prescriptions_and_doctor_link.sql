CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_number VARCHAR(50) NOT NULL,
    opd_visit_id BIGINT,
    ipd_admission_id BIGINT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    prescription_date DATE NOT NULL,
    notes TEXT,
    follow_up_date DATE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    UNIQUE KEY idx_prescription_number (prescription_number),
    KEY idx_prescription_patient (patient_id),
    KEY idx_prescription_doctor (doctor_id),
    KEY idx_prescription_hospital (hospital_id),
    KEY idx_prescription_opd_visit (opd_visit_id),
    KEY idx_prescription_date (prescription_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS prescription_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(50),
    duration VARCHAR(50),
    route VARCHAR(50),
    instructions TEXT,
    quantity INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    KEY idx_prescription_item_prescription (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE doctors
    ADD COLUMN IF NOT EXISTS app_user_id BIGINT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_doctor_app_user ON doctors (app_user_id);

ALTER TABLE prescriptions
    ADD CONSTRAINT fk_prescription_opd_visit
    FOREIGN KEY (opd_visit_id) REFERENCES opd_visits(id);

ALTER TABLE prescriptions
    ADD CONSTRAINT fk_prescription_ipd_admission
    FOREIGN KEY (ipd_admission_id) REFERENCES ipd_admissions(id);

ALTER TABLE prescriptions
    ADD CONSTRAINT fk_prescription_patient
    FOREIGN KEY (patient_id) REFERENCES patients(id);

ALTER TABLE prescriptions
    ADD CONSTRAINT fk_prescription_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors(id);

ALTER TABLE prescriptions
    ADD CONSTRAINT fk_prescription_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id);

ALTER TABLE prescription_items
    ADD CONSTRAINT fk_prescription_item_prescription
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id)
    ON DELETE CASCADE;

ALTER TABLE doctors
    ADD CONSTRAINT fk_doctor_app_user
    FOREIGN KEY (app_user_id) REFERENCES hms_users(id);