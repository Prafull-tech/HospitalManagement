CREATE TABLE IF NOT EXISTS company_profile (
    id BIGINT PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    logo_text VARCHAR(255),
    logo_url VARCHAR(1000),
    support_email VARCHAR(255),
    support_phone VARCHAR(30),
    address_text VARCHAR(1000),
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO company_profile (
    id,
    company_name,
    brand_name,
    logo_text,
    support_email,
    support_phone,
    address_text,
    created_at,
    updated_at
)
SELECT
    1,
    'HMS Hospital Management System',
    'HMS',
    'HMS',
    'support@hms-hospital.com',
    '+91 22 1234 5678',
    'HMS Office, Health Tech Park, Mumbai, Maharashtra, India',
    NOW(6),
    NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM company_profile WHERE id = 1);