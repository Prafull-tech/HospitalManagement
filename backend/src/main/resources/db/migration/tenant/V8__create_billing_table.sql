CREATE TABLE IF NOT EXISTS bills (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  patient_id CHAR(36) NOT NULL,
  bill_number VARCHAR(50) NOT NULL UNIQUE,
  bill_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  total_amount DECIMAL(10,2) NOT NULL,
  paid_amount DECIMAL(10,2) DEFAULT 0,
  discount DECIMAL(10,2) DEFAULT 0,
  status ENUM('pending','partial','paid','cancelled') DEFAULT 'pending',
  payment_mode ENUM('cash','card','upi','insurance','online'),
  notes TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_bills_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
);

