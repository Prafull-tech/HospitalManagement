# Hospital Management System (HMS)

## Project Overview

A comprehensive, enterprise-grade **SaaS Hospital Management System** built with Java Spring Boot 3.2.5 and Java 17. The system supports multi-tenant deployments for multiple hospitals with NABH compliance, robust audit trails, and modular architecture covering all major hospital operations.

### Quick Facts

| Attribute | Value |
|-----------|-------|
| **Framework** | Spring Boot 3.2.5 |
| **Language** | Java 17 |
| **Database** | MySQL (Flyway migrations) |
| **Authentication** | JWT + Refresh Token |
| **Architecture** | Multi-tenant SaaS |
| **Total Java Files** | 677 |
| **Entities** | 166 |
| **Controllers** | 65 |
| **Service Classes** | 81 |

### Base Configuration

- **API Context Path:** `/api`
- **Server Port:** 8080
- **Package:** `com.hospital.hms`

---

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Module Overview](#module-overview)
3. [Key Features](#key-features)
4. [Technology Stack](#technology-stack)
5. [Getting Started](#getting-started)

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                            │
│  (Web App / Mobile App / Third-party Integrations)              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                          │
│              (CORS, Rate Limiting, Tenant Resolution)           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Security Layer                               │
│         (JWT Authentication, RBAC, Method Security)             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Controller Layer                             │
│              (REST APIs - 65 Controllers)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Service Layer                                │
│         (Business Logic - 81 Service Classes)                   │
│    + Billing Engine + Rules Engine + Audit Service             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Repository Layer                              │
│              (JPA + QueryDSL for complex queries)               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Database                                   │
│              MySQL with Flyway Migrations                       │
└─────────────────────────────────────────────────────────────────┘
```

### Multi-Tenancy Model

```
┌─────────────────────────────────────────────────────────────┐
│                    Tenant Resolution                        │
├─────────────────────────────────────────────────────────────┤
│  Request → Subdomain (hms.local) → Hospital Lookup         │
│          OR Custom Domain → Hospital Lookup                │
├─────────────────────────────────────────────────────────────┤
│  TenantContextService provides:                             │
│  - Current Hospital ID                                      │
│  - Hospital Code                                            │
│  - Tenant Slug                                              │
└─────────────────────────────────────────────────────────────┘
```

All queries are automatically scoped by `hospital_id` ensuring data isolation between tenants.

---

## Module Overview

### Core Modules

| Module | Description | Key Entities |
|--------|-------------|--------------|
| **auth** | Authentication, JWT, RBAC | AppUser, RefreshToken, SystemRole |
| **reception** | Patient registration | Patient |
| **appointment** | Appointment scheduling | Appointment, AppointmentAuditLog |
| **doctor** | Doctor master & schedules | Doctor, MedicalDepartment, DoctorSchedule |
| **opd** | Out-Patient Department | OPDVisit, OPDToken, OPDClinicalNote |
| **ipd** | In-Patient Department | IPDAdmission, BedAllocation |
| **ward** | Ward, room, bed management | Ward, Room, Bed |
| **billing** | Centralized billing engine | PatientBillingAccount, BillingItem, Payment |
| **pharmacy** | Medication management | MedicineMaster, MedicationOrder, StockTransaction |
| **lab** | Laboratory Information System | LabOrder, LabResult, LabReport, TestMaster |
| **nursing** | Nursing care documentation | NursingNote, VitalSignRecord, NurseAssignment |
| **prescription** | Prescription management | Prescription, PrescriptionItem |
| **dietary** | Patient diet plans | DietPlan |
| **meals** | Patient meal management | PatientMeal |
| **housekeeping** | Bed cleaning tasks | HousekeepingTask |
| **laundry** | Linen management | LinenInventory |
| **superadmin** | Subscription & tenant management | Hospital, SubscriptionPlan |
| **system** | System modules & roles | SystemModule, FeatureToggle |
| **dashboard** | Aggregated dashboard data | - |

### Supporting Modules

| Module | Description |
|--------|-------------|
| **contact** | Contact messages management |
| **enquiry** | Patient enquiries handling |
| **hospital** | Hospital/branch master data |
| **payment** | Online payments (Razorpay/Stripe) |
| **blog** | Health blog content |
| **walkin** | Walk-in patient handling |
| **token** | Token management system |

---

## Key Features

### 1. Multi-Tenant SaaS Architecture
- Subdomain-based tenant resolution
- Per-hospital data isolation
- Custom domain support
- Subscription-based feature toggles

### 2. Role-Based Access Control (RBAC)
- 25+ predefined roles (ADMIN, DOCTOR, NURSE, RECEPTIONIST, etc.)
- Module-level permissions (view, create, edit, delete)
- Method-level security with `@PreAuthorize`

### 3. Patient Journey Management
```
Registration → OPD → Consultation → Lab/Pharmacy → (Optional) IPD Admission → Discharge → Billing
```

### 4. Centralized Billing Engine
- Event-driven charge capture from all modules
- Support for corporate billing, EMI plans, insurance/TPA
- Real-time billing account updates

### 5. Pharmacy Management
- FEFO (First-Expiry-First-Out) inventory
- Batch tracking with expiry dates
- Barcode scanning support
- Stock alerts for low inventory

### 6. Laboratory Information System
- Complete order-to-report workflow
- Sample collection tracking
- Result verification workflow
- PDF report generation

### 7. IPD Priority System
- P1 (Critical) to P4 (Routine) priority codes
- Rule-based auto-evaluation
- Override capability with audit trail

### 8. Audit & Compliance
- Comprehensive audit logs for all modules
- NABH compliance ready
- Token version invalidation for security

---

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.2.5
- **Language:** Java 17
- **Security:** Spring Security + JWT
- **ORM:** Hibernate/JPA
- **Query:** QueryDSL for complex queries
- **Database Migration:** Flyway

### Database
- **RDBMS:** MySQL 8.0+
- **Connection Pool:** HikariCP

### External Dependencies
- **JWT:** io.jsonwebtoken (jjwt)
- **Password Hashing:** BCrypt
- **PDF Generation:** (for lab reports, invoices)
- **Payment Gateway:** Razorpay/Stripe integration

### Development Tools
- **Build Tool:** Maven/Gradle
- **IDE:** IntelliJ IDEA / Eclipse
- **API Testing:** Postman / Swagger

---

## Getting Started

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd HospitalManagement
   ```

2. **Configure database**
   ```bash
   # Update application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hms_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. **Run Flyway migrations**
   ```bash
   # Migrations auto-run on startup
   ```

4. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the API**
   ```
   http://localhost:8080/api
   ```

### Default Credentials

After initial setup, use the super admin credentials to configure hospitals and subscriptions.

---

## Project Structure

```
backend/src/main/java/com/hospital/hms/
├── appointment/          # Appointment scheduling
├── auth/                 # Authentication & authorization
├── billing/              # Billing engine
├── common/               # Shared utilities
├── config/               # Security & CORS config
├── dashboard/            # Dashboard APIs
├── doctor/               # Doctor management
├── ipd/                  # In-Patient Department
├── lab/                  # Laboratory module
├── opd/                  # Out-Patient Department
├── pharmacy/             # Pharmacy management
├── prescription/         # Prescription module
├── reception/            # Patient registration
├── superadmin/           # Tenant management
├── system/               # System configuration
├── tenant/               # Multi-tenancy support
└── ward/                 # Ward & bed management
```

---

## API Documentation

API endpoints are organized by module. See [API_REFERENCE.md](./API_REFERENCE.md) for complete endpoint documentation.

### Key Endpoints

| Module | Base Path | Description |
|--------|-----------|-------------|
| Auth | `/api/auth` | Login, logout, token refresh |
| Patients | `/api/patients` | Patient registration |
| Appointments | `/api/appointments` | Booking, rescheduling |
| OPD | `/api/opd` | OPD visits, clinical notes |
| IPD | `/api/ipd` | Admissions, transfers, discharge |
| Billing | `/api/billing` | Billing items, payments |
| Pharmacy | `/api/pharmacy` | Medication orders, stock |
| Lab | `/api/lab` | Lab orders, results, reports |

---

## Support

For issues and feature requests, please contact the development team.
