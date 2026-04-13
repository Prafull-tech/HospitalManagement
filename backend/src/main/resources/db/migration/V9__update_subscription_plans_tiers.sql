-- Add quarterly_price column to subscription_plans
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS quarterly_price DECIMAL(10,2) AFTER monthly_price;

-- Clear old seed plans and insert proper 4-tier structure
DELETE FROM hospital_subscriptions;
DELETE FROM subscription_plans;

INSERT INTO subscription_plans (plan_code, plan_name, description, monthly_price, quarterly_price, yearly_price, max_users, max_beds, enabled_modules, trial_days, is_active)
VALUES
    ('BASIC', 'Basic',
     'Best for small clinics or single-doctor practices. Includes patient records, appointments, basic billing, and email support.',
     999.00, 2849.00, 9990.00,
     5, 10,
     'RECEPTION,OPD,BILLING',
     14, TRUE),

    ('STANDARD', 'Standard',
     'Best for mid-size hospitals. All Basic features plus lab, pharmacy, SMS/email notifications, analytics dashboard, and priority support.',
     2999.00, 8549.00, 29990.00,
     25, 50,
     'RECEPTION,OPD,IPD,BILLING,LAB,PHARMACY,NURSING',
     14, TRUE),

    ('PROFESSIONAL', 'Professional',
     'Best for large multi-department hospitals. All Standard features plus ward/bed management, insurance/claims, custom branding, API access, and dedicated support.',
     7999.00, 22799.00, 79990.00,
     100, 200,
     'RECEPTION,OPD,IPD,BILLING,LAB,PHARMACY,NURSING,RADIOLOGY,BLOOD_BANK,HOUSEKEEPING,LAUNDRY,DIETARY',
     30, TRUE),

    ('ENTERPRISE', 'Enterprise',
     'Best for hospital chains or large institutions. Unlimited users, multi-branch, custom integrations, SLA guarantee, on-premise option, and dedicated account manager.',
     14999.00, NULL, 149990.00,
     NULL, NULL,
     'ALL',
     30, TRUE);
