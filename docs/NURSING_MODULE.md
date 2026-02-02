# Nursing Department Module

The Nursing Department manages continuous patient care for IPD patients. It integrates with IPD (admissions & beds), OPD (day-care optional), Doctors (orders reference), and Ward/ICU.

## Roles

| Role                   | Staff Mgmt | Assign | Vitals | MAR | Notes |
|------------------------|------------|--------|--------|-----|-------|
| ADMIN                  | ✓          | ✓      | ✓      | ✓   | ✓     |
| CHIEF_NURSING_OFFICER  | ✓          | ✓      | ✓      | ✓   | ✓     |
| NURSING_SUPERINTENDENT | ✓          | ✓      | ✓      | ✓   | ✓     |
| WARD_INCHARGE          | —          | ✓      | ✓      | ✓   | ✓     |
| STAFF_NURSE            | —          | ✓      | ✓      | ✓   | ✓     |
| NURSING_AIDE           | —          | ✓      | ✓      | —   | —     |
| DOCTOR                 | —          | view   | view   | view| view  |

*Auth is currently disabled; re-enable and apply `@PreAuthorize` when ready.*

---

## Nursing Workflow

1. **Nursing staff** — Create and manage nursing staff (code, name, role, phone, email). Roles: CHIEF_NURSING_OFFICER, NURSING_SUPERINTENDENT, WARD_INCHARGE, STAFF_NURSE, NURSING_AIDE.
2. **Nurse assignment** — Assign a nurse to an IPD admission for a shift (MORNING, EVENING, NIGHT) and date. Only **active** IPD admissions (ADMITTED, TRANSFERRED, DISCHARGE_INITIATED) are allowed.
3. **Vital signs** — Record BP (systolic/diastolic), pulse, temperature, SpO2, respiration with timestamp. Time-ordered history per admission.
4. **MAR (Medication Administration Record)** — Record medicine administration (name, dosage, route, time, administered by). Doctor order ref is optional (link to orders later).
5. **Nursing notes** — Shift notes and care plan (note type: SHIFT_NOTE, CARE_PLAN, GENERAL). Timestamped and optionally linked to staff.

---

## API Contract

Base path: `/api`. All endpoints return JSON.

| Method | Endpoint | Description | Status codes |
|--------|----------|-------------|--------------|
| POST   | `/api/nursing/staff` | Create nursing staff | 201 Created, 400 |
| GET    | `/api/nursing/staff` | List staff (activeOnly, nurseRole) | 200 OK |
| GET    | `/api/nursing/staff/{id}` | Get staff by ID | 200 OK, 404 |
| POST   | `/api/nursing/assignments` | Assign nurse to IPD admission | 201 Created, 400, 404 |
| GET    | `/api/nursing/assignments/by-admission/{ipdAdmissionId}` | List assignments for admission | 200 OK |
| POST   | `/api/nursing/vitals` | Record vital signs | 201 Created, 400, 404 |
| GET    | `/api/nursing/vitals/{ipdAdmissionId}` | Get vitals for admission (time-ordered) | 200 OK |
| POST   | `/api/nursing/notes` | Create nursing note | 201 Created, 400, 404 |
| GET    | `/api/nursing/notes/{ipdAdmissionId}` | Get notes for admission | 200 OK |
| POST   | `/api/nursing/medications` | Record medication (MAR) | 201 Created, 400, 404 |
| GET    | `/api/nursing/medications/{ipdAdmissionId}` | Get MAR for admission | 200 OK |

### Query parameters

- **GET /api/nursing/staff:** `activeOnly` (default true), `nurseRole` (optional).

---

## Sample JSON

### Request — Create nursing staff

```json
POST /api/nursing/staff
Content-Type: application/json

{
  "code": "NS001",
  "fullName": "Jane Nurse",
  "nurseRole": "STAFF_NURSE",
  "phone": "+91-9876543210",
  "email": "jane@hospital.com",
  "isActive": true
}
```

### Request — Assign nurse

```json
POST /api/nursing/assignments
Content-Type: application/json

{
  "nursingStaffId": 1,
  "ipdAdmissionId": 1,
  "shiftType": "MORNING",
  "assignmentDate": "2025-01-29",
  "remarks": "Routine assignment"
}
```

### Request — Record vitals

```json
POST /api/nursing/vitals
Content-Type: application/json

{
  "ipdAdmissionId": 1,
  "recordedAt": "2025-01-29T10:30:00",
  "bloodPressureSystolic": 120,
  "bloodPressureDiastolic": 80,
  "pulse": 72,
  "temperature": 36.8,
  "spo2": 98,
  "respiration": 16,
  "recordedById": 1,
  "remarks": null
}
```

### Request — Nursing note

```json
POST /api/nursing/notes
Content-Type: application/json

{
  "ipdAdmissionId": 1,
  "noteType": "SHIFT_NOTE",
  "content": "Patient stable. Vitals within normal limits. Diet as ordered.",
  "recordedAt": "2025-01-29T14:00:00",
  "recordedById": 1
}
```

### Request — Medication (MAR)

```json
POST /api/nursing/medications
Content-Type: application/json

{
  "ipdAdmissionId": 1,
  "medicationName": "Paracetamol",
  "dosage": "500mg",
  "route": "Oral",
  "administeredAt": "2025-01-29T08:00:00",
  "administeredById": 1,
  "doctorOrderRef": "ORD-001",
  "remarks": null
}
```

---

## Database (DB-agnostic)

- **Entities:** `NursingStaff`, `NurseAssignment`, `VitalSignRecord`, `NursingNote`, `MedicationAdministration`. Enums: `ShiftType` (MORNING, EVENING, NIGHT), `NurseRole`.
- **Indexes:** `ipd_admission_id`, `recorded_at`, `nursing_staff_id` (and entity-specific indexes). No native SQL; JPQL/derived queries only. Works on H2 and MySQL.
- **Business rules:** Only active IPD admissions (ADMITTED, TRANSFERRED, DISCHARGE_INITIATED) for assignments, vitals, notes, MAR. Vitals returned time-ordered (recordedAt desc).

---

## Integration

- **IPD:** IPD admission ID is mandatory for assignments, vitals, notes, and MAR. Nursing module reads admission from IPD module.
- **OPD:** OPD vitals are optional (day-care); current implementation is IPD-focused. Can extend later with optional OPD visit ID.
- **Doctors:** Doctor orders will be linked via `doctorOrderRef` in MAR (reference only for now).
- **Billing:** Will consume nursing services later (e.g. vitals rounds, MAR entries).

---

## Frontend (React)

- **Routes:** `/nursing` (dashboard), `/nursing/staff`, `/nursing/assign`, `/nursing/vitals`, `/nursing/medications`, `/nursing/notes`.
- **Features:** Nursing dashboard; staff list and add form; assign nurse (admission, nurse, shift, date); vitals entry and history with basic bar chart (pulse, SpO2); MAR form and history; nursing notes editor (shift note, care plan) and history. API via Axios; loading, success, and error states. Shift-based assignment (MORNING/EVENING/NIGHT).
