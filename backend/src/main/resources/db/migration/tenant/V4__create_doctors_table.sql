CREATE TABLE IF NOT EXISTS doctors (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  user_id CHAR(36) NOT NULL UNIQUE,
  specialization VARCHAR(100),
  qualification VARCHAR(255),
  experience_yrs INT,
  consultation_fee DECIMAL(10,2),
  available_days VARCHAR(100),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users(id)
);

