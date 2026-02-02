# NABH Compliance & Bed Allotment SOP

Reference document for NABH-aligned bed management: compliance points, Bed Allotment SOP, and mapping to HMS features.

---

## NABH Compliance Points

| Point | Description | HMS Support |
|-------|-------------|-------------|
| ✔ **Daily bed census** | Regular count of beds by status (occupied, vacant, reserved, under cleaning) | Hospital-wise Bed Availability: `GET /api/hospitals/{id}/bed-availability`; ward types General, Semi-Private, Private, ICU, Emergency |
| ✔ **Transparent allotment** | Clear, documented allocation process | Bed Allotment SOP (below); audit trail `GET .../bed-availability/{id}/audit`; role-based access (ADMIN, IPD_MANAGER, DOCTOR) |
| ✔ **No discrimination** | Fair allocation by category, not by non-clinical factors | Category-based (General / Semi-Private / Private / ICU / Emergency); one record per Hospital + Ward Type |
| ✔ **Emergency priority** | Emergency patients get immediate bed; registration later | SOP Emergency Exception; Emergency ward type in bed availability; admission flow can prioritise Emergency |
| ✔ **Infection isolation** | Isolation beds / cohorting as per policy | Ward/bed configuration (wards, rooms, beds); isolation flag can be extended in bed/ward master |

---

## 1. Bed Allotment SOP (Standard Operating Procedure)

### Purpose
To ensure **fair, transparent, and timely** allotment of hospital beds.

### Scope
Applies to **OPD, IPD, Emergency, ICU, and Admission Desk**.

### Responsibility
- **IPD Manager**
- **Admission Desk Staff**
- **Ward In-charge**
- **Nursing Supervisor**

---

### Procedure

| Step | Activity | HMS / System Link |
|------|----------|-------------------|
| **Step 1: Admission Request** | Doctor recommends admission; Admission slip issued | OPD → IPD referral; admission slip (document/print) |
| **Step 2: Bed Availability Check** | Check HIS/Bed Register; Confirm vacant bed | **Bed Availability API**: list by hospital, ward type (General/Semi-Private/Private/ICU/Emergency); vacant = total − (occupied + reserved + under cleaning) |
| **Step 3: Patient Category** | General / Semi-Private / Private / ICU / Emergency | Ward types in bed availability and IPD admission (ward/bed selection by category) |
| **Step 4: Documentation** | Admission Form; Consent Form; Deposit Receipt | Patient registration; IPD admission form; consent & deposit in process (forms/docs) |
| **Step 5: Bed Allocation** | Bed number assigned; Ward informed; Patient shifted | **IPD Admission**: bed allocation; ward/bed assignment; nursing/ward handover |
| **Step 6: System Update** | Update HIS/Register; Inform billing & nursing | **Bed Availability**: update occupied/reserved/under cleaning via `PUT .../bed-availability/{id}`; audit trail (updatedBy, updatedAt); billing & nursing can consume same data |

---

### Emergency Exception
- **Emergency patients get immediate bed.**
- **Registration done later.**

*Implementation:* Reserve/allocate Emergency ward beds via Bed Availability; admission flow can allow “emergency allocation first, registration follow-up”.

---

### Records Maintained
| Record | Where in HMS |
|--------|----------------|
| Bed Register | Hospital-wise Bed Availability (list + audit trail) |
| Admission Register | IPD Admissions (list/detail APIs and data) |
| Shift Handover Sheet | Nursing handover / handover documentation (process) |

---

## 2. Responsibility Mapping (Bed Updates)

| Role | Responsibility | System Recording |
|------|----------------|------------------|
| **Nurse** | Updates occupied / under-cleaning counts | `updatedBy`, `updatedAt`; audit log |
| **IPD Desk** | Verifies & confirms bed census | Same; role IPD_MANAGER in audit |
| **Ward In-charge** | Approves changes | Same; role ADMIN / Ward In-charge when configured |
| **System** | Logs timestamp & user | `updatedAt`, `updatedBy`; read-only audit API |

*Detail:* `backend/docs/bed-availability-responsibility-mapping.md`

---

## 3. API Summary (Bed Availability)

| Method | Path | Use in SOP |
|--------|------|------------|
| GET | `/api/hospitals/{id}/bed-availability` | **Step 2** – Bed availability check; daily census |
| GET | `/api/hospitals/{id}/bed-availability/{id}/audit` | Transparency; audit trail |
| PUT | `/api/hospitals/{id}/bed-availability/{id}` | **Step 6** – System update (occupied/reserved/under cleaning) |

Ward types: **GENERAL**, **SEMI_PRIVATE**, **PRIVATE**, **ICU**, **EMERGENCY** (align with Patient Category in SOP).

---

*This document is for compliance reference and training. Implement policy (e.g. emergency priority, isolation) in workflows and configuration as per hospital policy.*
