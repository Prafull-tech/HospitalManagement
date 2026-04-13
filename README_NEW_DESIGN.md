# Hospital Management System - Verified Current Architecture and SaaS Target Design

## Overview

This document replaces the earlier generic multi-hospital design note with a repository-verified architecture summary.

It serves two purposes:

1. Describe the application as it exists in this codebase today.
2. Define the practical target path for evolving it into a SaaS multi-hospital platform.

The current system is a broad hospital operations platform with strong module coverage, but it is not yet a full multi-tenant SaaS implementation.

---

## Verification Summary

The attached SaaS architecture note was reviewed against the actual repository.

### Verified as implemented

- Spring Boot backend with Java 17
- React frontend with Vite and TypeScript
- JWT-based authentication with refresh tokens
- Phase 1 host-based tenant resolution via hospital subdomain and public tenant bootstrap endpoint
- Phase 2 request-scoped tenant context with hospital fallback from authenticated user context on platform and localhost hosts
- Hospital-scoped enforcement for reception patients, appointments, wards, beds, and IPD admissions
- Phase 3 enterprise custom-domain support with verification challenge serving and certificate lifecycle tracking
- Modular hospital workflow coverage across reception, OPD, IPD, lab, pharmacy, billing, nursing, tokens, wards, and support services
- MySQL as the primary database
- Dockerized local and production-style deployment using `docker-compose.yml`
- Company profile and branding support through system configuration APIs
- Hospital master and hospital-wise bed availability support

### Present in code, but not as full SaaS multi-tenancy

- `Hospital` master data exists
- Some records are scoped by hospital, such as hospital bed availability
- `SUPER_ADMIN` exists as a role in the frontend permission model and some backend configuration endpoints

### Not implemented yet

- Dynamic datasource routing per tenant
- Schema-per-hospital or database-per-hospital isolation
- Full tenant-scoped request context across all modules
- Subscription billing and plan enforcement for hospitals
- Hospital onboarding automation that provisions isolated tenant storage
- Fully automated external DNS validation and certificate issuance orchestration

### Important conclusion

The application is currently best described as a single-deployment modular HMS that is partially multi-hospital aware, not a production-ready SaaS multi-tenant HMS yet.

---

## Current Architecture

### Architecture style

The current application is a modular monolith.

- One Spring Boot backend application
- One React SPA frontend
- One primary MySQL database
- One Redis instance for caching and related infrastructure concerns
- One auxiliary Python invoice-generation component

This is a solid base for future multi-tenant evolution because domain boundaries already exist at package and route level.

### Runtime layout

```text
Frontend (React + Vite / Nginx)
                        |
                        v
Spring Boot API (/api)
                        |
                        +--> MySQL
                        +--> Redis
                        +--> Python invoice generator
```

### Repository-aligned stack

| Layer | Current implementation |
|---|---|
| Frontend | React 18, TypeScript, Vite, React Router |
| Backend | Spring Boot 3.2.5, Java 17 |
| Security | Spring Security, JWT, refresh tokens, BCrypt |
| Database | MySQL |
| Cache | Redis |
| ORM | Spring Data JPA / Hibernate |
| API docs | springdoc OpenAPI / Swagger UI |
| Metrics | Actuator, Prometheus registry |
| Resilience | Resilience4j |
| File / document generation | OpenPDF, Apache POI, Python invoice service |
| Deployment | Dockerfiles for backend and frontend, root Docker Compose |

---

## Backend Design

### Core characteristics

- Context path is `/api`
- Stateless API security model
- JWT authentication filter in the Spring Security chain
- Method-level authorization with `@PreAuthorize`
- Domain-oriented package structure
- Global exception handling
- Production configuration validation for secrets

### Security model

Current security is role-based with partial tenant-aware enforcement.

Implemented behavior:

- Login via `/auth/login`
- Access token plus refresh token issuance
- JWT contains `subject` and `role`
- Security context is populated from the JWT on each request
- Request tenant context is resolved per request from subdomain host, with authenticated hospital fallback on platform and localhost hosts
- Core operational modules now enforce hospital ownership in service and repository lookups for patients, appointments, wards, beds, and IPD admissions
- Enterprise custom domains resolve as tenant hosts, expose an HTTP verification challenge, and track verification plus certificate lifecycle state per hospital
- The public verification path is `/.well-known/hms-domain-verification`; because the backend runs under `/api`, the frontend edge must proxy that root path to `/api/.well-known/hms-domain-verification`
- Most routes require authentication
- Public routes exist for marketing pages, contact, blog, and company profile

Current JWT claim shape is effectively:

```json
{
      "sub": "username",
      "role": "ADMIN",
      "hospitalId": 12,
      "hospitalCode": "H001",
      "tenantSlug": "cityhospital"
}
```

The following are not present today:

- global tenant-scoped authorization checks across all business modules
- datasource switching based on request tenant

### Implemented backend domains

The codebase already contains substantial domain coverage.

- Auth and token management
- Reception and patient registration
- Appointments and doctor scheduling
- OPD workflows
- IPD admissions, transfers, discharge, and admission priority
- Ward, room, and bed management
- Hospital master and hospital bed availability
- Doctors and departments
- Lab workflow and PDF reporting
- Pharmacy and stock operations
- Billing, refunds, TPA, corporate, EMI, and online payment hooks
- Nursing notes, assignments, vitals, and medication administration
- Enquiry desk and walk-in flow
- Token queue and display system
- Laundry, dietary, meals, and housekeeping
- Blog and contact public content
- System configuration for roles, modules, permissions, features, and company profile

---

## Frontend Design

### Current application structure

The frontend is a single React SPA with route-based module segregation.

Key application traits:

- Protected route system for authenticated areas
- Role-protected module access
- Public marketing and content pages under shared public layout
- Dashboard redirection based on effective role access
- Feature flags and permissions bootstrapped at runtime
- Configurable company profile branding loaded from the backend

### Current route groups

Major route groups visible in the application include:

- `/home`, `/blog`, `/contact`, `/signup`, `/login`
- `/reception`
- `/front-office`
- `/opd`
- `/ipd`
- `/lab`
- `/pharmacy`
- `/billing`
- `/nursing`
- `/doctors`
- `/wards`
- `/housekeeping`, `/laundry`, `/dietary`, `/meals`
- `/admin/config`

### Branding model today

Branding is currently global application branding, not tenant branding.

The frontend loads a public company profile with fields such as:

- company name
- brand name
- logo text or URL
- support email
- support phone
- address text

This supports white-label style configuration for one deployed HMS instance, but not per-subdomain hospital branding.

---

## Data Design

### Current persistence model

The active design is a shared single database model.

- Main database name defaults to `hms`
- JPA entities persist into one datasource
- No tenant resolver or tenant-specific schema routing is configured
- Flyway is prepared for production use but disabled in dev by default

### Hospital model in current code

There is a `Hospital` entity used as a master record with fields equivalent to:

```text
Hospital {
      id
      hospitalCode
      hospitalName
      subdomain
      location
      isActive
      deleted
}
```

This is useful groundwork for future multi-hospital support, but it is not enough by itself to provide tenant isolation.

### Current multi-hospital capability

Current repository support for hospital scoping is now meaningful but still incomplete.

- Hospital master CRUD exists
- Hospital subdomain resolution exists for Phase 1 tenant routing
- Enterprise custom-domain exact-host resolution exists for tenant routing
- Public tenant bootstrap exists for host-aware branding
- Request tenant context exists for Phase 2 enforcement
- Patients, appointments, wards, beds, and IPD admissions are hospital-owned in the backend enforcement path
- Hospital-specific bed availability APIs exist
- Some workflows are hospital-aware at data model level

However, the application does not yet enforce a global hospital context across all modules. OPD, billing, lab, pharmacy, nursing, and other linked flows still need the same hospital-scoping pass to complete Phase 2, and certificate issuance is tracked operationally rather than fully automated.

---

## Deployment Design

### Current deployment model

The repository already includes deployment assets.

- `backend/Dockerfile`
- `frontend/Dockerfile`
- root `docker-compose.yml`

Current Docker Compose services:

- MySQL 8
- Redis 7
- Spring Boot backend
- Frontend served on Nginx

This means the current application is already container-ready for a single-instance deployment.

### Environment configuration highlights

- Spring profile: `dev` or `prod`
- MySQL host, port, database, user, password
- JWT secret
- CORS origins or patterns
- Redis host and port
- invoice generator paths
- payment provider keys

---

## Current Role Model

The application already has a broad operational role matrix.

Examples visible in code:

- `ADMIN`
- `SUPER_ADMIN`
- `RECEPTIONIST`
- `FRONT_DESK`
- `DOCTOR`
- `NURSE`
- `BILLING`
- `IPD_MANAGER`
- `PHARMACIST`
- `PHARMACY_MANAGER`
- `LAB_TECH`
- `LAB_TECHNICIAN`
- `PATHOLOGIST`
- `RADIOLOGY_TECH`
- `BLOOD_BANK_TECH`
- `HOUSEKEEPING`
- `HR_MANAGER`

Current authorization is role-centric. It is not yet role-plus-tenant scoped.

---

## Gap Analysis Against SaaS Architecture

The reviewed SaaS architecture note is directionally useful, but it should be treated as target state, not current state.

### Gap 1: tenant identification

Target state:

- `admin.hms.com`
- `cityhospital.hms.com`
- `apollo.hms.com`

Current state:

- Subdomain parsing and tenant resolution exist in backend
- Frontend tenant bootstrap exists for host-aware branding and access gating
- Enterprise custom-domain routing exists in backend
- Domain verification challenge serving and certificate lifecycle tracking exist
- External certificate automation is still pending

### Gap 2: tenant isolation

Target state:

- schema-per-hospital or database-per-hospital

Current state:

- single datasource
- single schema approach in practice
- no datasource router

### Gap 3: tenant-aware auth

Target state:

- JWT includes hospital or tenant claim
- every request validated against tenant context

Current state:

- JWT includes username, role, hospitalId, hospitalCode, and tenantSlug
- Request tenant context exists and is enforced across key operational modules, but not yet across the full application surface

### Gap 4: super-admin SaaS control plane

Target state:

- create hospitals
- assign plans
- activate and suspend tenants
- view cross-hospital analytics

Current state:

- hospital master CRUD exists
- company profile and admin configuration exist
- no SaaS subscription lifecycle or full tenant provisioning workflow

### Gap 5: tenant branding

Target state:

- hospital branding loaded by tenant slug or domain

Current state:

- one global company profile shared by the deployed instance

---

## Recommended Design Position

The right design posture for this project is:

### Current product statement

An enterprise modular HMS for one hospital group deployment, with early multi-hospital foundation pieces already present.

### Target product statement

A multi-tenant SaaS HMS where each hospital is isolated, independently branded, administratively managed, and commercially billed from a central control plane.

---

## Recommended SaaS Evolution Path

The application should not jump directly to schema-per-tenant across all modules without first introducing an explicit tenant model into the business and security layers.

### Phase 1 - Normalize hospital ownership

Objective: make every operational record explicitly hospital-owned.

- add hospital reference to all core operational entities where missing
- standardize hospital lookup and validation services
- ensure all APIs work with authenticated hospital context internally
- make hospital assignment explicit for users

This phase creates correctness before isolation.

### Phase 2 - Tenant-aware authentication and request context

Objective: add tenant context without changing storage isolation yet.

- include `hospitalId` and `tenantSlug` in JWT
- add a request tenant context filter
- validate that authenticated users can only access their own hospital data
- enforce hospital scoping in core operational modules first: patients, appointments, wards/beds, IPD admissions
- separate super-admin flows from hospital-admin flows

This phase is now partially implemented. The system has a real tenant request context and a first core-module enforcement slice, but the remaining modules still need to adopt the same hospital-scoped pattern before Phase 2 can be considered complete.

### Phase 3 - Tenant routing and branding

Objective: move to real tenant-facing SaaS behavior.

- support wildcard DNS and subdomain parsing
- resolve tenant by host header
- load company branding by tenant
- direct `admin` subdomain to the control plane

### Phase 4 - Storage isolation

Objective: strengthen compliance and operational separation.

Recommended options:

1. Schema-per-hospital if operational simplicity is acceptable.
2. Database-per-hospital if strong compliance and portability are required.

For healthcare-sensitive deployments, database-per-hospital is the safer long-term option even though operational complexity is higher.

### Phase 5 - SaaS subscriptions and onboarding

Objective: complete platformization.

- subscription plans and feature entitlements
- tenant provisioning workflow
- default hospital admin invitation flow
- tenant lifecycle management
- billing and payment reconciliation per hospital

---

## Target SaaS Reference Model

When the application is ready for full SaaS, the architecture should look like this:

```text
Super Admin Portal
            |
            +--> Tenant Registry / Master DB
            |
            +--> Provisioning Service
            |
Hospital Subdomain -> Tenant Resolver -> API Gateway / Spring Boot App
                                                                                                                   -> Tenant Context
                                                                                                                   -> Tenant-scoped services
                                                                                                                   -> Tenant DB or tenant schema
```

### Master platform data

- hospitals
- hospital domains and slugs
- subscription plans
- hospital subscriptions
- billing and payment ledger
- platform audit logs
- super-admin users

### Tenant operational data

- patients
- appointments
- admissions
- lab orders and reports
- billing accounts and payments
- pharmacy inventory and dispensing
- nursing data
- wards and beds

---

## Practical Design Recommendation for This Repo

The current application should be documented and communicated as:

"A production-capable modular HMS with hospital master support and SaaS readiness foundations, but not yet a fully isolated multi-tenant SaaS platform."

This wording is accurate, defensible, and aligned with the repository.

---

## Immediate Next Design Actions

Recommended next engineering steps:

1. Introduce a first-class tenant model and hospital ownership audit across all modules.
2. Add `hospitalId` to JWT and enforce hospital-scoped access in the service layer.
3. Define whether the target isolation model is shared-schema, schema-per-tenant, or database-per-tenant before further SaaS UI work.
4. Split global company profile from hospital profile so branding can become tenant-specific.
5. Create a dedicated super-admin control plane instead of overloading current admin configuration.

---

## Document Status

Version: 2.0

Status: verified against repository structure and core implementation on April 12, 2026.