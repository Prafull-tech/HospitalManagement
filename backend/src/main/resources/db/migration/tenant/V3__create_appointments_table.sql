CREATE TABLE IF NOT EXISTS appointments (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  patient_id CHAR(36) NOT NULL,
  doctor_id CHAR(36) NOT NULL,
  appointment_date DATE NOT NULL,
  time_slot TIME NOT NULL,
  status ENUM('scheduled','completed','cancelled','no_show') DEFAULT 'scheduled',
  type ENUM('opd','ipd','follow_up','emergency') DEFAULT 'opd',
  notes TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES users(id)
);

