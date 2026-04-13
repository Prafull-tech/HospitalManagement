-- Fix billing_cycle column: change from MySQL ENUM to VARCHAR to support all BillingCycle values
ALTER TABLE hospital_subscriptions MODIFY COLUMN billing_cycle VARCHAR(20) DEFAULT 'MONTHLY';
