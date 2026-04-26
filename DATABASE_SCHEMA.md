# Database Schema Documentation

Hospital Management System - MySQL Database Schema

---

## Table of Contents

1. [Core Tables](#core-tables)
2. [Authentication & Authorization](#authentication--authorization)
3. [Patient Management](#patient-management)
4. [Doctor Management](#doctor-management)
5. [Appointment Module](#appointment-module)
6. [OPD Module](#opd-module)
7. [IPD Module](#ipd-module)
8. [Ward & Bed Management](#ward--bed-management)
9. [Billing Module](#billing-module)
10. [Pharmacy Module](#pharmacy-module)
11. [Laboratory Module](#laboratory-module)
12. [Nursing Module](#nursing-module)
13. [Prescription Module](#prescription-module)
14. [System & Audit](#system--audit)

---

## Core Tables

### hospitals

Stores hospital/tenant information for multi-tenant SaaS architecture.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| hospital_code | VARCHAR(50) | UNIQUE, NOT NULL | Unique hospital code |
| hospital_name | VARCHAR(200) | NOT NULL | Hospital name |
| subdomain | VARCHAR(100) | UNIQUE | Subdomain for tenant resolution |
| custom_domain | VARCHAR(200) | NULL | Custom domain (optional) |
| logo_url | VARCHAR(500) | NULL | Hospital logo URL |
| is_active | BOOLEAN | DEFAULT TRUE | Active status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_subdomain`, `idx_custom_domain`, `idx_is_active`

---

### subscription_plans

Defines subscription plans for hospitals.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| plan_code | VARCHAR(50) | UNIQUE, NOT NULL | Plan code (BASIC, STANDARD, PREMIUM) |
| plan_name | VARCHAR(100) | NOT NULL | Plan display name |
| monthly_price | DECIMAL(10,2) | NOT NULL | Monthly subscription price |
| max_users | INT | NOT NULL | Maximum number of users |
| max_beds | INT | NOT NULL | Maximum number of beds |
| enabled_modules | VARCHAR(500) | NOT NULL | Comma-separated enabled module codes |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

---

### hospital_subscriptions

Tracks hospital subscriptions to plans.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| plan_id | BIGINT | FOREIGN KEY → subscription_plans(id) | Plan reference |
| start_date | DATE | NOT NULL | Subscription start date |
| end_date | DATE | NOT NULL | Subscription end date |
| status | VARCHAR(20) | NOT NULL | ACTIVE, EXPIRED, SUSPENDED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_hospital_id`, `idx_status`, `idx_end_date`

---

## Authentication & Authorization

### hms_users

Stores user accounts for authentication.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| role | VARCHAR(50) | NOT NULL | User role (enum) |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| full_name | VARCHAR(200) | NOT NULL | Full name |
| email | VARCHAR(100) | NULL | Email address |
| phone | VARCHAR(20) | NULL | Phone number |
| active | BOOLEAN | DEFAULT TRUE | Account active status |
| token_version | INT | DEFAULT 0 | Token invalidation counter |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_username`, `idx_hospital_id`, `idx_role`, `idx_active`

**User Roles (25+):**
- ADMIN, SUPER_ADMIN
- DOCTOR, NURSE, RECEPTIONIST
- PHARMACIST, LAB_TECHNICIAN
- ACCOUNTANT, BILLING_STAFF
- WARD_MANAGER, HOUSEKEEPING_SUPERVISOR
- MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD
- And more...

---

### refresh_tokens

Stores JWT refresh tokens.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| token | VARCHAR(36) | UNIQUE, NOT NULL | UUID refresh token |
| username | VARCHAR(50) | NOT NULL | Associated username |
| expires_at | TIMESTAMP | NOT NULL | Token expiration time |
| revoked | BOOLEAN | DEFAULT FALSE | Revocation status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_token`, `idx_username`, `idx_expires_at`

---

### system_modules

Defines system modules for access control.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR(50) | UNIQUE, NOT NULL | Module code (OPD, IPD, PHARMACY) |
| name | VARCHAR(100) | NOT NULL | Module display name |
| module_category | VARCHAR(50) | NOT NULL | CLINICAL, NON_CLINICAL, ADMINISTRATION |
| route_path | VARCHAR(100) | NOT NULL | Frontend route path |
| enabled | BOOLEAN | DEFAULT TRUE | Module enabled status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

---

### system_roles

Defines system roles for RBAC.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR(50) | UNIQUE, NOT NULL | Role code |
| name | VARCHAR(100) | NOT NULL | Role display name |
| system_role | VARCHAR(50) | NOT NULL | System role identifier |
| active | BOOLEAN | DEFAULT TRUE | Role active status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

---

### role_module_permissions

Defines role-based permissions for modules.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| role_id | BIGINT | FOREIGN KEY → system_roles(id) | Role reference |
| module_id | BIGINT | FOREIGN KEY → system_modules(id) | Module reference |
| can_view | BOOLEAN | DEFAULT FALSE | View permission |
| can_create | BOOLEAN | DEFAULT FALSE | Create permission |
| can_edit | BOOLEAN | DEFAULT FALSE | Edit permission |
| can_delete | BOOLEAN | DEFAULT FALSE | Delete permission |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_role_id`, `idx_module_id`, `UNIQUE(role_id, module_id)`

---

### feature_toggles

Enables per-hospital feature control.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| feature_key | VARCHAR(100) | NOT NULL | Feature identifier |
| enabled | BOOLEAN | DEFAULT TRUE | Feature enabled status |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference (NULL = global) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_feature_key`, `idx_hospital_id`, `UNIQUE(feature_key, hospital_id)`

---

### audit_events

Stores system-wide audit events.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| event_type | VARCHAR(50) | NOT NULL | Event type (CREATE, UPDATE, DELETE) |
| entity_type | VARCHAR(50) | NOT NULL | Entity type (PATIENT, APPOINTMENT) |
| entity_id | BIGINT | NOT NULL | Affected entity ID |
| user_id | BIGINT | FOREIGN KEY → hms_users(id) | User who performed action |
| details | JSON | NULL | Event details in JSON format |
| event_timestamp | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Event timestamp |
| ip_address | VARCHAR(45) | NULL | Client IP address |

**Indexes:** `idx_entity_type_id`, `idx_user_id`, `idx_event_timestamp`, `idx_event_type`

---

## Patient Management

### patients

Stores patient master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| uhid | VARCHAR(50) | UNIQUE, NOT NULL | Unique Health ID (auto-generated) |
| registration_number | VARCHAR(50) | UNIQUE, NOT NULL | Registration number |
| full_name | VARCHAR(200) | NOT NULL | Patient name |
| age | INT | NULL | Age at registration |
| date_of_birth | DATE | NULL | Date of birth |
| gender | VARCHAR(20) | NOT NULL | MALE, FEMALE, OTHER |
| blood_group | VARCHAR(10) | NULL | Blood group |
| phone | VARCHAR(20) | NOT NULL | Primary phone number |
| alternate_phone | VARCHAR(20) | NULL | Alternate phone |
| email | VARCHAR(100) | NULL | Email address |
| address | TEXT | NOT NULL | Full address |
| city | VARCHAR(100) | NULL | City |
| state | VARCHAR(100) | NULL | State |
| pincode | VARCHAR(10) | NULL | PIN code |
| emergency_contact_name | VARCHAR(200) | NULL | Emergency contact name |
| emergency_contact_phone | VARCHAR(20) | NULL | Emergency contact phone |
| corporate_id | BIGINT | FOREIGN KEY → corporate_accounts(id) | Corporate account reference |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_uhid`, `idx_registration_number`, `idx_phone`, `idx_patient_name`, `idx_hospital_id`, `idx_corporate_id`

---

## Doctor Management

### doctors

Stores doctor master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR(50) | UNIQUE, NOT NULL | Doctor code |
| full_name | VARCHAR(200) | NOT NULL | Doctor name |
| department_id | BIGINT | FOREIGN KEY → medical_departments(id) | Department reference |
| specialization | VARCHAR(100) | NULL | Specialization |
| doctor_type | VARCHAR(50) | NOT NULL | CONSULTANT, RMO, RESIDENT, DUTY_DOCTOR |
| status | VARCHAR(20) | NOT NULL | ACTIVE, INACTIVE, ON_LEAVE |
| email | VARCHAR(100) | NULL | Email address |
| phone | VARCHAR(20) | NULL | Phone number |
| qualification | VARCHAR(200) | NULL | Qualifications |
| experience_years | INT | NULL | Years of experience |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_code`, `idx_department_id`, `idx_hospital_id`, `idx_status`, `idx_doctor_name`

---

### medical_departments

Stores medical department master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR(50) | UNIQUE, NOT NULL | Department code |
| name | VARCHAR(100) | NOT NULL | Department name |
| description | TEXT | NULL | Department description |
| hod_doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Head of Department |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_code`, `idx_hospital_id`

---

### doctor_schedules

Stores doctor availability schedules.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Doctor reference |
| day_of_week | VARCHAR(20) | NOT NULL | MONDAY, TUESDAY, etc. |
| start_time | TIME | NOT NULL | Shift start time |
| end_time | TIME | NOT NULL | Shift end time |
| slot_duration_minutes | INT | DEFAULT 15 | Appointment slot duration |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_doctor_id`, `idx_day_of_week`, `idx_hospital_id`

---

## Appointment Module

### appointments

Stores patient appointments.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| appointment_no | VARCHAR(50) | UNIQUE, NOT NULL | Auto-generated appointment number |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Doctor reference |
| department_id | BIGINT | FOREIGN KEY → medical_departments(id) | Department reference |
| appointment_date | DATE | NOT NULL | Appointment date |
| slot_time | TIME | NOT NULL | Appointment slot time |
| token_no | INT | NOT NULL | Token number for the day |
| status | VARCHAR(30) | NOT NULL | BOOKED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW |
| source | VARCHAR(30) | NOT NULL | FRONT_DESK, WALK_IN, ONLINE |
| visit_type | VARCHAR(20) | NOT NULL | NEW, FOLLOWUP |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_appointment_no`, `idx_patient_id`, `idx_doctor_id`, `idx_appointment_date`, `idx_status`, `idx_hospital_id`

---

### appointment_audit_log

Audits appointment lifecycle events.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| appointment_id | BIGINT | FOREIGN KEY → appointments(id) | Appointment reference |
| event_type | VARCHAR(50) | NOT NULL | BOOKED, CONFIRMED, CANCELLED, RESCHEDULED |
| user_id | BIGINT | FOREIGN KEY → hms_users(id) | User who triggered event |
| event_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Event timestamp |
| details | JSON | NULL | Event details |

**Indexes:** `idx_appointment_id`, `idx_event_type`, `idx_event_at`

---

## OPD Module

### opd_visits

Stores OPD visit records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| visit_number | VARCHAR(50) | UNIQUE, NOT NULL | Auto-generated visit number |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Doctor reference |
| visit_date | DATE | NOT NULL | Visit date |
| visit_type | VARCHAR(20) | NOT NULL | OPD, EMERGENCY |
| visit_status | VARCHAR(30) | NOT NULL | REGISTERED, IN_CONSULTATION, COMPLETED, REFERRED |
| token_number | INT | NOT NULL | Token number |
| chief_complaint | TEXT | NULL | Chief complaint |
| provisional_diagnosis | TEXT | NULL | Provisional diagnosis |
| consultation_outcome | VARCHAR(50) | NULL | OPD_TREATMENT_ONLY, LAB_TEST_ADVISED, IPD_ADMISSION_ADVISED |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_visit_number`, `idx_patient_id`, `idx_doctor_id`, `idx_visit_date`, `idx_visit_status`, `idx_hospital_id`

---

### opd_tokens

Stores OPD token management.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| visit_id | BIGINT | FOREIGN KEY → opd_visits(id) | Visit reference |
| doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Doctor reference |
| token_date | DATE | NOT NULL | Token date |
| token_number | INT | NOT NULL | Token number |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_visit_id`, `idx_doctor_id`, `idx_token_date`

---

### opd_clinical_notes

Stores OPD clinical documentation.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| visit_id | BIGINT | FOREIGN KEY → opd_visits(id) | Visit reference |
| chief_complaint | TEXT | NOT NULL | Chief complaint |
| provisional_diagnosis | TEXT | NULL | Provisional diagnosis |
| doctor_remarks | TEXT | NULL | Doctor remarks |
| advice | TEXT | NULL | Treatment advice |
| created_by | BIGINT | FOREIGN KEY → hms_users(id) | Doctor user ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_visit_id`, `idx_created_by`

---

## IPD Module

### ipd_admissions

Stores IPD admission records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| admission_number | VARCHAR(50) | UNIQUE, NOT NULL | Auto-generated admission number |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| primary_doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Primary doctor reference |
| admission_type | VARCHAR(30) | NOT NULL | ELECTIVE, EMERGENCY, ELECTIVE_CESAREAN |
| admission_status | VARCHAR(30) | NOT NULL | ADMITTED, ACTIVE, TRANSFERRED, DISCHARGE_INITIATED, DISCHARGED, CANCELLED |
| admission_priority | VARCHAR(10) | NOT NULL | P1, P2, P3, P4 |
| admission_datetime | TIMESTAMP | NOT NULL | Admission date/time |
| diagnosis | TEXT | NOT NULL | Admission diagnosis |
| discharge_summary | TEXT | NULL | Discharge summary |
| discharge_advice | TEXT | NULL | Discharge advice |
| discharge_datetime | TIMESTAMP | NULL | Discharge date/time |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_admission_number`, `idx_patient_id`, `idx_admission_status`, `idx_admission_date`, `idx_hospital_id`

---

### bed_allocations

Stores bed allocation history.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| bed_id | BIGINT | FOREIGN KEY → beds(id) | Bed reference |
| admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | Admission reference |
| allocated_at | TIMESTAMP | NOT NULL | Allocation timestamp |
| released_at | TIMESTAMP | NULL | Release timestamp |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_bed_id`, `idx_admission_id`, `idx_allocated_at`

---

### admission_charges

Stores admission-related charges.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | Admission reference |
| charge_type | VARCHAR(50) | NOT NULL | ADMISSION_FEE, SECURITY_DEPOSIT, etc. |
| amount | DECIMAL(10,2) | NOT NULL | Charge amount |
| description | VARCHAR(200) | NULL | Charge description |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_ipd_admission_id`, `idx_charge_type`

---

## Ward & Bed Management

### wards

Stores ward master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR(50) | UNIQUE, NOT NULL | Ward code |
| name | VARCHAR(100) | NOT NULL | Ward name |
| ward_type | VARCHAR(50) | NOT NULL | GENERAL_WARD, PRIVATE_WARD, ICU, ICCU, NICU, PICU, SEMI_PRIVATE |
| floor | VARCHAR(50) | NOT NULL | Floor location |
| capacity | INT | NOT NULL | Total bed capacity |
| daily_charge_per_bed | DECIMAL(10,2) | NOT NULL | Per bed daily charge |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_code`, `idx_ward_type`, `idx_hospital_id`

---

### rooms

Stores room master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| ward_id | BIGINT | FOREIGN KEY → wards(id) | Ward reference |
| room_number | VARCHAR(50) | NOT NULL | Room number |
| capacity | INT | NOT NULL | Room capacity |
| room_type | VARCHAR(50) | NOT NULL | SINGLE, DOUBLE, SHARED |
| status | VARCHAR(20) | NOT NULL | AVAILABLE, OCCUPIED, MAINTENANCE |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_ward_id`, `idx_room_number`, `idx_status`, `idx_hospital_id`

---

### beds

Stores bed master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| ward_id | BIGINT | FOREIGN KEY → wards(id) | Ward reference |
| room_id | BIGINT | FOREIGN KEY → rooms(id) | Room reference |
| bed_number | VARCHAR(50) | NOT NULL | Bed number |
| bed_status | VARCHAR(20) | NOT NULL | AVAILABLE, RESERVED, OCCUPIED, CLEANING, MAINTENANCE |
| is_isolation | BOOLEAN | DEFAULT FALSE | Isolation bed flag |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_ward_id`, `idx_room_id`, `idx_bed_number`, `idx_bed_status`, `idx_hospital_id`

---

## Billing Module

### patient_billing_accounts

Stores patient billing account summary.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| uhid | VARCHAR(50) | NOT NULL | Patient UHID |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | IPD admission reference |
| opd_visit_id | BIGINT | FOREIGN KEY → opd_visits(id) | OPD visit reference |
| total_amount | DECIMAL(12,2) | DEFAULT 0 | Total billed amount |
| paid_amount | DECIMAL(12,2) | DEFAULT 0 | Total paid amount |
| pending_amount | DECIMAL(12,2) | DEFAULT 0 | Outstanding amount |
| status | VARCHAR(20) | NOT NULL | ACTIVE, FINALIZED, DISCHARGED, CLOSED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_patient_id`, `idx_uhid`, `idx_ipd_admission_id`, `idx_opd_visit_id`, `idx_status`

---

### billing_items

Stores individual billing line items.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| billing_account_id | BIGINT | FOREIGN KEY → patient_billing_accounts(id) | Billing account reference |
| service_type | VARCHAR(50) | NOT NULL | BED, PHARMACY, LAB, OT, PROCEDURE, RADIOLOGY, CONSULTATION, NURSING, BLOOD_BANK |
| service_name | VARCHAR(200) | NOT NULL | Service/item name |
| quantity | DECIMAL(10,2) | NOT NULL | Quantity |
| unit_price | DECIMAL(10,2) | NOT NULL | Unit price |
| total_price | DECIMAL(10,2) | NOT NULL | Total price (quantity × unit_price) |
| status | VARCHAR(20) | NOT NULL | POSTED, CANCELLED, REFUNDED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| created_by | BIGINT | FOREIGN KEY → hms_users(id) | User who posted the item |

**Indexes:** `idx_billing_account_id`, `idx_service_type`, `idx_status`, `idx_created_at`

---

### billing_payments

Stores payment transactions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| billing_account_id | BIGINT | FOREIGN KEY → patient_billing_accounts(id) | Billing account reference |
| amount | DECIMAL(12,2) | NOT NULL | Payment amount |
| mode | VARCHAR(20) | NOT NULL | CASH, CARD, UPI, CHEQUE, ONLINE, INSURANCE, CORPORATE |
| reference_no | VARCHAR(100) | NULL | Transaction reference number |
| payment_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Payment timestamp |
| received_by | BIGINT | FOREIGN KEY → hms_users(id) | User who received payment |
| remarks | TEXT | NULL | Payment remarks |

**Indexes:** `idx_billing_account_id`, `idx_mode`, `idx_payment_date`

---

### corporate_accounts

Stores corporate billing accounts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| corporate_code | VARCHAR(50) | UNIQUE, NOT NULL | Corporate account code |
| company_name | VARCHAR(200) | NOT NULL | Company name |
| credit_limit | DECIMAL(12,2) | NOT NULL | Credit limit |
| billing_cycle | VARCHAR(20) | NOT NULL | WEEKLY, MONTHLY, QUARTERLY |
| contact_person | VARCHAR(200) | NULL | Contact person name |
| contact_phone | VARCHAR(20) | NULL | Contact phone |
| contact_email | VARCHAR(100) | NULL | Contact email |
| address | TEXT | NULL | Company address |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_corporate_code`, `idx_hospital_id`

---

### emi_plans

Stores EMI payment plans.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| billing_account_id | BIGINT | FOREIGN KEY → patient_billing_accounts(id) | Billing account reference |
| total_amount | DECIMAL(12,2) | NOT NULL | Total EMI amount |
| down_payment | DECIMAL(12,2) | NOT NULL | Down payment amount |
| tenure_months | INT | NOT NULL | EMI tenure in months |
| emi_amount | DECIMAL(12,2) | NOT NULL | Monthly EMI amount |
| status | VARCHAR(20) | NOT NULL | ACTIVE, COMPLETED, DEFAULTED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_billing_account_id`, `idx_status`

---

### refunds

Stores refund transactions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| billing_account_id | BIGINT | FOREIGN KEY → patient_billing_accounts(id) | Billing account reference |
| amount | DECIMAL(12,2) | NOT NULL | Refund amount |
| reason | TEXT | NOT NULL | Refund reason |
| status | VARCHAR(20) | NOT NULL | PENDING, APPROVED, PROCESSED, REJECTED |
| approved_by | BIGINT | FOREIGN KEY → hms_users(id) | Approving user |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_billing_account_id`, `idx_status`

---

## Pharmacy Module

### medicine_master

Stores medicine master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| medicine_code | VARCHAR(50) | UNIQUE, NOT NULL | Medicine code |
| medicine_name | VARCHAR(200) | NOT NULL | Medicine name |
| category | VARCHAR(50) | NOT NULL | TABLET, CAPSULE, SYRUP, INJECTION, TOPICAL, etc. |
| strength | VARCHAR(50) | NOT NULL | Strength (e.g., 500mg) |
| form | VARCHAR(50) | NOT NULL | TABLET, CAPSULE, SYRUP, SUSPENSION, INJECTION, CREAM, OINTMENT |
| barcode | VARCHAR(100) | UNIQUE, NULL | Barcode |
| min_stock | INT | DEFAULT 0 | Minimum stock level |
| unit_price | DECIMAL(10,2) | NOT NULL | Selling price per unit |
| storage_type | VARCHAR(30) | DEFAULT 'ROOM_TEMP' | ROOM_TEMP, REFRIGERATED, FROZEN |
| active | BOOLEAN | DEFAULT TRUE | Active status |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_medicine_code`, `idx_barcode`, `idx_medicine_name`, `idx_category`, `idx_hospital_id`

---

### pharmacy_racks

Stores pharmacy rack locations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| rack_code | VARCHAR(50) | UNIQUE, NOT NULL | Rack code |
| rack_name | VARCHAR(100) | NOT NULL | Rack name |
| category | VARCHAR(50) | NULL | Category stored in rack |
| location | VARCHAR(100) | NULL | Location description |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_rack_code`, `idx_hospital_id`

---

### pharmacy_shelves

Stores pharmacy shelf locations within racks.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| rack_id | BIGINT | FOREIGN KEY → pharmacy_racks(id) | Rack reference |
| shelf_code | VARCHAR(50) | UNIQUE, NOT NULL | Shelf code |
| shelf_name | VARCHAR(100) | NOT NULL | Shelf name |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_rack_id`, `idx_shelf_code`

---

### medication_orders

Stores medication orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| order_number | VARCHAR(50) | UNIQUE, NOT NULL | Auto-generated order number |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | IPD admission reference |
| opd_visit_id | BIGINT | FOREIGN KEY → opd_visits(id) | OPD visit reference |
| ordered_by_doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Ordering doctor reference |
| priority | VARCHAR(20) | NOT NULL | NORMAL, URGENT, STAT |
| status | VARCHAR(20) | NOT NULL | PENDING, ISSUED, CANCELLED |
| order_datetime | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Order timestamp |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_order_number`, `idx_patient_id`, `idx_ipd_admission_id`, `idx_opd_visit_id`, `idx_status`, `idx_priority`, `idx_hospital_id`

---

### stock_transactions

Stores medicine stock transactions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| medicine_id | BIGINT | FOREIGN KEY → medicine_master(id) | Medicine reference |
| transaction_type | VARCHAR(20) | NOT NULL | PURCHASE, SALE_OUT, EXPIRY, DAMAGE, RETURN |
| quantity | INT | NOT NULL | Transaction quantity |
| batch_number | VARCHAR(50) | NOT NULL | Batch number |
| expiry_date | DATE | NOT NULL | Expiry date |
| unit_price | DECIMAL(10,2) | NOT NULL | Unit price |
| total_amount | DECIMAL(12,2) | NOT NULL | Total amount |
| reference_id | BIGINT | NULL | Reference (order_id, invoice_id) |
| transaction_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Transaction timestamp |
| created_by | BIGINT | FOREIGN KEY → hms_users(id) | User who performed transaction |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |

**Indexes:** `idx_medicine_id`, `idx_batch_number`, `idx_transaction_type`, `idx_expiry_date`, `idx_transaction_date`, `idx_hospital_id`

---

### pharmacy_invoices

Stores pharmacy sale invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| invoice_number | VARCHAR(50) | UNIQUE, NOT NULL | Invoice number |
| sale_id | BIGINT | NOT NULL | Sale reference |
| pdf_url | VARCHAR(500) | NULL | Generated PDF URL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_invoice_number`, `idx_sale_id`

---

## Laboratory Module

### test_masters

Stores test master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| test_code | VARCHAR(50) | UNIQUE, NOT NULL | Test code |
| test_name | VARCHAR(200) | NOT NULL | Test name |
| sample_type | VARCHAR(50) | NOT NULL | BLOOD, URINE, STOOL, CSF, SPUTUM, etc. |
| turnaround_time_hours | INT | NOT NULL | Expected turnaround time |
| price | DECIMAL(10,2) | NOT NULL | Test price |
| category_id | BIGINT | FOREIGN KEY → test_categories(id) | Test category reference |
| description | TEXT | NULL | Test description |
| active | BOOLEAN | DEFAULT TRUE | Active status |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_test_code`, `idx_test_name`, `idx_category_id`, `idx_hospital_id`

---

### test_categories

Stores test category hierarchy.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR(50) | UNIQUE, NOT NULL | Category code |
| name | VARCHAR(100) | NOT NULL | Category name |
| parent_category_id | BIGINT | FOREIGN KEY → test_categories(id) | Parent category (self-reference) |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_code`, `idx_parent_category_id`, `idx_hospital_id`

---

### lab_orders

Stores lab test orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| order_number | VARCHAR(50) | UNIQUE, NOT NULL | Auto-generated order number |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | IPD admission reference |
| opd_visit_id | BIGINT | FOREIGN KEY → opd_visits(id) | OPD visit reference |
| ordered_by_doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Ordering doctor reference |
| priority | VARCHAR(20) | NOT NULL | ROUTINE, URGENT, EMERGENCY |
| status | VARCHAR(30) | NOT NULL | ORDERED, SAMPLE_PENDING, SAMPLE_COLLECTED, IN_PROGRESS, COMPLETED, VERIFIED, RELEASED |
| order_datetime | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Order timestamp |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_order_number`, `idx_patient_id`, `idx_ipd_admission_id`, `idx_opd_visit_id`, `idx_status`, `idx_priority`, `idx_hospital_id`

---

### lab_order_items

Stores individual test items within lab orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| order_id | BIGINT | FOREIGN KEY → lab_orders(id) | Order reference |
| test_id | BIGINT | FOREIGN KEY → test_masters(id) | Test reference |
| sample_type | VARCHAR(50) | NOT NULL | Sample type |
| status | VARCHAR(20) | NOT NULL | PENDING, IN_PROGRESS, COMPLETED, VERIFIED, REJECTED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_order_id`, `idx_test_id`, `idx_status`

---

### lab_results

Stores lab test results.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| order_item_id | BIGINT | FOREIGN KEY → lab_order_items(id) | Order item reference |
| test_value | VARCHAR(200) | NOT NULL | Test result value |
| unit | VARCHAR(50) | NULL | Unit of measurement |
| reference_range | VARCHAR(100) | NULL | Reference range |
| is_abnormal | BOOLEAN | DEFAULT FALSE | Abnormal value flag |
| entered_by | BIGINT | FOREIGN KEY → hms_users(id) | User who entered result |
| entered_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Entry timestamp |

**Indexes:** `idx_order_item_id`, `idx_is_abnormal`

---

### lab_reports

Stores generated lab reports.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| test_order_id | BIGINT | FOREIGN KEY → lab_orders(id) | Test order reference |
| report_url | VARCHAR(500) | NULL | Generated PDF URL |
| generated_at | TIMESTAMP | NULL | Report generation timestamp |
| verified_at | TIMESTAMP | NULL | Verification timestamp |
| verified_by | BIGINT | FOREIGN KEY → hms_users(id) | Verifying user |
| released_at | TIMESTAMP | NULL | Release timestamp |
| status | VARCHAR(20) | NOT NULL | DRAFT, VERIFIED, RELEASED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_test_order_id`, `idx_status`, `idx_generated_at`

---

### lab_audit_logs

Audits lab order lifecycle events.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| test_order_id | BIGINT | FOREIGN KEY → lab_orders(id) | Order reference |
| event_type | VARCHAR(50) | NOT NULL | ORDER_PLACED, SAMPLE_COLLECTED, RESULT_ENTERED, etc. |
| performed_by | BIGINT | FOREIGN KEY → hms_users(id) | User who performed action |
| details | JSON | NULL | Event details |
| event_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Event timestamp |

**Indexes:** `idx_test_order_id`, `idx_event_type`, `idx_event_at`

---

## Nursing Module

### nursing_notes

Stores nursing documentation.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | Admission reference |
| shift_type | VARCHAR(20) | NOT NULL | MORNING, AFTERNOON, NIGHT |
| note_type | VARCHAR(50) | NOT NULL | GENERAL, MEDICATION, PROCEDURE, INCIDENT |
| content | TEXT | NOT NULL | Note content |
| recorded_by | BIGINT | FOREIGN KEY → hms_users(id) | Recording nurse user ID |
| recorded_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Recording timestamp |
| note_status | VARCHAR(20) | NOT NULL | DRAFT, FINAL, LOCKED |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_ipd_admission_id`, `idx_shift_type`, `idx_note_type`, `idx_note_status`, `idx_hospital_id`

---

### vital_sign_records

Stores patient vital sign records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | Admission reference |
| bp_systolic | INT | NULL | Systolic BP (mmHg) |
| bp_diastolic | INT | NULL | Diastolic BP (mmHg) |
| temperature | DECIMAL(4,1) | NULL | Temperature (°F) |
| pulse | INT | NULL | Pulse rate (bpm) |
| spo2 | INT | NULL | SpO2 percentage |
| respiratory_rate | INT | NULL | Respiratory rate (breaths/min) |
| recorded_by | BIGINT | FOREIGN KEY → hms_users(id) | Recording user |
| recorded_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Recording timestamp |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |

**Indexes:** `idx_ipd_admission_id`, `idx_recorded_at`, `idx_hospital_id`

---

### nurse_assignments

Stores nurse-to-patient assignments.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | Admission reference |
| nurse_id | BIGINT | FOREIGN KEY → hms_users(id) | Assigned nurse user ID |
| shift_type | VARCHAR(20) | NOT NULL | MORNING, AFTERNOON, NIGHT |
| assigned_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Assignment timestamp |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |

**Indexes:** `idx_ipd_admission_id`, `idx_nurse_id`, `idx_shift_type`, `idx_hospital_id`

---

### medication_administrations

Stores medication administration records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| medication_order_id | BIGINT | FOREIGN KEY → medication_orders(id) | Order reference |
| nurse_id | BIGINT | FOREIGN KEY → hms_users(id) | Administering nurse |
| administered_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Administration timestamp |
| dose_given | VARCHAR(100) | NOT NULL | Dose administered |
| remarks | TEXT | NULL | Administration remarks |

**Indexes:** `idx_medication_order_id`, `idx_nurse_id`, `idx_administered_at`

---

### nursing_staff

Stores nursing staff master data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR(50) | UNIQUE, NOT NULL | Staff code |
| name | VARCHAR(200) | NOT NULL | Staff name |
| role | VARCHAR(50) | NOT NULL | STAFF_NURSE, SENIOR_NURSE, WARD_SISTER |
| shift_type | VARCHAR(20) | NOT NULL | MORNING, AFTERNOON, NIGHT |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_code`, `idx_hospital_id`

---

## Prescription Module

### prescriptions

Stores prescription records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| prescription_number | VARCHAR(50) | UNIQUE, NOT NULL | Auto-generated prescription number |
| opd_visit_id | BIGINT | FOREIGN KEY → opd_visits(id) | OPD visit reference |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | IPD admission reference |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| doctor_id | BIGINT | FOREIGN KEY → doctors(id) | Prescribing doctor reference |
| prescription_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Prescription date |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_prescription_number`, `idx_opd_visit_id`, `idx_ipd_admission_id`, `idx_patient_id`, `idx_doctor_id`, `idx_hospital_id`

---

### prescription_items

Stores prescription line items.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| prescription_id | BIGINT | FOREIGN KEY → prescriptions(id) | Prescription reference |
| medicine_name | VARCHAR(200) | NOT NULL | Medicine name |
| dosage | VARCHAR(100) | NOT NULL | Dosage (e.g., 500mg) |
| frequency | VARCHAR(50) | NOT NULL | Frequency (BD, TDS, QD, etc.) |
| duration_days | INT | NOT NULL | Duration in days |
| instructions | TEXT | NULL | Usage instructions |

**Indexes:** `idx_prescription_id`

---

## Dietary Module

### diet_plans

Stores patient diet plans.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| patient_id | BIGINT | FOREIGN KEY → patients(id) | Patient reference |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | IPD admission reference |
| diet_type | VARCHAR(30) | NOT NULL | NORMAL, SOFT, LIQUID, DIABETIC, CARDIAC, RENAL, LOW_SODIUM |
| meal_schedule | JSON | NULL | Meal schedule configuration |
| active | BOOLEAN | DEFAULT TRUE | Active status |
| created_by | BIGINT | FOREIGN KEY → hms_users(id) | Creating user |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_patient_id`, `idx_ipd_admission_id`, `idx_diet_type`

---

## Meals Module

### patient_meals

Stores patient meal records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| ipd_admission_id | BIGINT | FOREIGN KEY → ipd_admissions(id) | Admission reference |
| meal_type | VARCHAR(20) | NOT NULL | BREAKFAST, LUNCH, SNACKS, DINNER |
| meal_date | DATE | NOT NULL | Meal date |
| status | VARCHAR(20) | NOT NULL | PENDING, SERVED, SKIPPED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_ipd_admission_id`, `idx_meal_date`, `idx_meal_type`, `idx_status`

---

## Housekeeping Module

### housekeeping_tasks

Stores housekeeping task records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| bed_id | BIGINT | FOREIGN KEY → beds(id) | Bed reference |
| task_type | VARCHAR(50) | NOT NULL | ROUTINE_CLEANING, TERMINAL_CLEANING, DISINFECTION |
| status | VARCHAR(20) | NOT NULL | PENDING, IN_PROGRESS, COMPLETED |
| assigned_staff | VARCHAR(200) | NULL | Assigned staff name |
| assigned_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Assignment timestamp |
| completed_at | TIMESTAMP | NULL | Completion timestamp |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |

**Indexes:** `idx_bed_id`, `idx_task_type`, `idx_status`, `idx_hospital_id`

---

## Laundry Module

### linen_inventory

Stores linen inventory.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| linen_type | VARCHAR(30) | NOT NULL | SHEET, PILLOW_COVER, BLANKET, GOWN, TOWEL |
| quantity | INT | NOT NULL | Available quantity |
| location | VARCHAR(100) | NOT NULL | Storage location |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_linen_type`, `idx_location`, `idx_hospital_id`

---

## Contact & Enquiry

### contact_messages

Stores website contact messages.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR(200) | NOT NULL | Sender name |
| email | VARCHAR(100) | NOT NULL | Sender email |
| phone | VARCHAR(20) | NULL | Sender phone |
| message | TEXT | NOT NULL | Message content |
| status | VARCHAR(20) | DEFAULT 'PENDING' | PENDING, READ, REPLIED |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_status`, `idx_created_at`

---

### enquiries

Stores patient enquiries.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| patient_name | VARCHAR(200) | NOT NULL | Patient name |
| phone | VARCHAR(20) | NOT NULL | Contact phone |
| enquiry_type | VARCHAR(50) | NOT NULL | GENERAL, APPOINTMENT, BILLING, etc. |
| status | VARCHAR(20) | DEFAULT 'PENDING' | PENDING, IN_PROGRESS, RESOLVED, CLOSED |
| priority | VARCHAR(20) | DEFAULT 'NORMAL' | NORMAL, URGENT, HIGH |
| remarks | TEXT | NULL | Enquiry remarks |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_phone`, `idx_status`, `idx_enquiry_type`, `idx_hospital_id`

---

## Blog Module

### blog_posts

Stores health blog posts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| title | VARCHAR(500) | NOT NULL | Post title |
| content | TEXT | NOT NULL | Post content |
| author | VARCHAR(200) | NOT NULL | Author name |
| published | BOOLEAN | DEFAULT FALSE | Published status |
| published_at | TIMESTAMP | NULL | Publication timestamp |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:** `idx_published`, `idx_published_at`, `idx_hospital_id`

---

## Token Management

### tokens

Stores department token numbers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| token_number | INT | NOT NULL | Token number |
| department_id | BIGINT | FOREIGN KEY → medical_departments(id) | Department reference |
| token_date | DATE | NOT NULL | Token date |
| status | VARCHAR(20) | NOT NULL | PENDING, SERVED, CANCELLED |
| hospital_id | BIGINT | FOREIGN KEY → hospitals(id) | Hospital reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Indexes:** `idx_department_id`, `idx_token_date`, `idx_status`, `idx_hospital_id`

---

## Entity Relationship Diagram (Simplified)

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  hospitals  │◄──────│   patients  │──────►│ appointments│
└─────────────┘       └─────────────┘       └─────────────┘
       │                    │                      │
       │                    │                      ▼
       │                    │               ┌─────────────┐
       │                    ▼               │  opd_visits │
       │             ┌─────────────┐        └─────────────┘
       │             │   doctors   │               │
       │             └─────────────┘               ▼
       │                    │               ┌─────────────┐
       │                    ▼               │ipd_admissions│
       │             ┌─────────────┐        └─────────────┘
       │             │ departments │               │
       │             └─────────────┘               ▼
       │                                    ┌─────────────┐
       │                                    │bed_allocations│
       │                                    └─────────────┘
       │                                            │
       ▼                                            ▼
┌─────────────┐                              ┌─────────────┐
│hms_users    │                              │patient_billing│
└─────────────┘                              └─────────────┘
       │                                            │
       ▼                                            ▼
┌─────────────┐                              ┌─────────────┐
│system_roles │                              │ billing_items│
└─────────────┘                              └─────────────┘
```

---

## Indexes Summary

### Critical Indexes for Performance

| Table | Index Columns | Purpose |
|-------|---------------|---------|
| patients | uhid, registration_number, phone | Patient lookup |
| appointments | appointment_date, doctor_id, status | Daily queue, doctor schedule |
| opd_visits | visit_number, patient_id, visit_date | Visit lookup, patient history |
| ipd_admissions | admission_number, patient_id, admission_status | Admission lookup, active patients |
| beds | bed_status, ward_id | Bed availability |
| billing_items | billing_account_id, status | Billing summary |
| medication_orders | status, ipd_admission_id | Pharmacy queue |
| lab_orders | status, patient_id | Lab queue, patient history |
| nursing_notes | ipd_admission_id, shift_type | Patient care documentation |

---

## Constraints & Triggers

### Auto-Generated Fields

- **UHID:** Generated on patient registration (format: UHIDYYYYMMDDNNNN)
- **Appointment Number:** Generated on booking (format: APPTYYYYMMDDNNNN)
- **Visit Number:** Generated on OPD registration (format: OPDYYYYMMDDNNNN)
- **Admission Number:** Generated on IPD admission (format: IPDYYYYMMDDNNNN)
- **Prescription Number:** Generated on prescription creation

### Default Values

- `active` fields: DEFAULT TRUE
- `status` fields: Appropriate initial status (PENDING, BOOKED, etc.)
- `created_at`: DEFAULT CURRENT_TIMESTAMP
- `updated_at`: ON UPDATE CURRENT_TIMESTAMP

---

## Data Retention

| Entity Type | Retention Policy |
|-------------|------------------|
| Patient records | Permanent (no deletion) |
| Appointments | 7 years |
| OPD visits | 7 years |
| IPD admissions | 10 years |
| Lab results | 5 years |
| Billing records | 7 years |
| Audit logs | 3 years |

---

This schema supports NABH compliance requirements for data retention and audit trails.
