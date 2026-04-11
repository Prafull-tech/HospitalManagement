CREATE TABLE IF NOT EXISTS enquiries (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    enquiry_no VARCHAR(30) NOT NULL UNIQUE,
    patient_id BIGINT NULL,
    department_id BIGINT NULL,
    category VARCHAR(40) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    resolution VARCHAR(1000) NULL,
    assigned_to_user VARCHAR(255) NULL,
    enquirer_name VARCHAR(255) NULL,
    phone VARCHAR(30) NULL,
    email VARCHAR(255) NULL,
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_enquiry_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_enquiry_department FOREIGN KEY (department_id) REFERENCES medical_departments(id)
);

CREATE INDEX idx_enquiry_status ON enquiries(status);
CREATE INDEX idx_enquiry_department ON enquiries(department_id);
CREATE INDEX idx_enquiry_patient ON enquiries(patient_id);
CREATE INDEX idx_enquiry_assignee ON enquiries(assigned_to_user);

CREATE TABLE IF NOT EXISTS enquiry_audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    enquiry_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    performed_by VARCHAR(255) NULL,
    event_at TIMESTAMP NOT NULL,
    note VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_enquiry_audit_enquiry FOREIGN KEY (enquiry_id) REFERENCES enquiries(id)
);

CREATE INDEX idx_enquiry_audit_enquiry ON enquiry_audit_logs(enquiry_id);
CREATE INDEX idx_enquiry_audit_event ON enquiry_audit_logs(event_type);
