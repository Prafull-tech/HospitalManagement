-- Subscription plans table
CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code VARCHAR(50) NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    monthly_price DECIMAL(10,2) NOT NULL,
    yearly_price DECIMAL(10,2),
    max_users INT,
    max_beds INT,
    enabled_modules VARCHAR(1000),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    trial_days INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_plan_code UNIQUE (plan_code)
);

CREATE INDEX idx_plan_code ON subscription_plans (plan_code);
CREATE INDEX idx_plan_active ON subscription_plans (is_active);

-- Hospital subscriptions table
CREATE TABLE IF NOT EXISTS hospital_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'TRIAL',
    start_date DATE NOT NULL,
    end_date DATE,
    trial_end_date DATE,
    billing_cycle VARCHAR(20) DEFAULT 'MONTHLY',
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hsub_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_hsub_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);

CREATE INDEX idx_hsub_hospital ON hospital_subscriptions (hospital_id);
CREATE INDEX idx_hsub_plan ON hospital_subscriptions (plan_id);
CREATE INDEX idx_hsub_status ON hospital_subscriptions (status);

-- Add platform metadata columns to hospitals
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS contact_email VARCHAR(100);
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(20);
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS onboarding_status VARCHAR(30) DEFAULT 'PENDING';

-- Seed default subscription plans
INSERT INTO subscription_plans (plan_code, plan_name, description, monthly_price, yearly_price, max_users, max_beds, trial_days, is_active)
VALUES
    ('STARTER', 'Starter', 'Basic plan for small clinics', 999.00, 9990.00, 10, 20, 14, TRUE),
    ('PROFESSIONAL', 'Professional', 'Full-featured plan for mid-size hospitals', 4999.00, 49990.00, 50, 100, 14, TRUE),
    ('ENTERPRISE', 'Enterprise', 'Unlimited plan for large hospital groups', 14999.00, 149990.00, NULL, NULL, 30, TRUE);
