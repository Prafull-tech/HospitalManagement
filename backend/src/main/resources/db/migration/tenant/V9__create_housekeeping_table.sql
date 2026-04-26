CREATE TABLE IF NOT EXISTS housekeeping_tasks (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  task_type VARCHAR(100) NOT NULL,
  location VARCHAR(255) NOT NULL,
  assigned_to CHAR(36),
  priority ENUM('low','medium','high','urgent') DEFAULT 'medium',
  status ENUM('pending','in_progress','completed') DEFAULT 'pending',
  scheduled_at DATETIME,
  completed_at DATETIME,
  notes TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_housekeeping_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id)
);

