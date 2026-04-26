CREATE TABLE IF NOT EXISTS lab_tests (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  patient_id CHAR(36) NOT NULL,
  doctor_id CHAR(36) NOT NULL,
  test_name VARCHAR(255) NOT NULL,
  test_code VARCHAR(50),
  status ENUM('requested','sample_collected','in_progress','completed') DEFAULT 'requested',
  result TEXT,
  price DECIMAL(10,2),
  requested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  CONSTRAINT fk_lab_tests_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_lab_tests_doctor FOREIGN KEY (doctor_id) REFERENCES users(id)
);

