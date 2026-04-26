CREATE TABLE IF NOT EXISTS radiology_tests (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  patient_id CHAR(36) NOT NULL,
  doctor_id CHAR(36) NOT NULL,
  modality ENUM('xray','mri','ct_scan','ultrasound','echo') NOT NULL,
  status ENUM('requested','scheduled','completed','reported') DEFAULT 'requested',
  report_url TEXT,
  price DECIMAL(10,2),
  requested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  CONSTRAINT fk_radiology_tests_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_radiology_tests_doctor FOREIGN KEY (doctor_id) REFERENCES users(id)
);

