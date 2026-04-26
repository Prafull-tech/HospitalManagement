CREATE TABLE IF NOT EXISTS staff_records (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  user_id CHAR(36) NOT NULL UNIQUE,
  employee_code VARCHAR(50) NOT NULL UNIQUE,
  join_date DATE,
  department VARCHAR(100),
  salary DECIMAL(10,2),
  leave_balance INT DEFAULT 20,
  status ENUM('active','on_leave','resigned','terminated') DEFAULT 'active',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_staff_records_user FOREIGN KEY (user_id) REFERENCES users(id)
);

