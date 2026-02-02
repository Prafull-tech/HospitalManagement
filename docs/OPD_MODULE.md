# OPD (Outpatient Department) Module

OPD manages outpatient visits without hospital admission. It integrates with Reception (patient UHID), Doctors (availability & assignment), and will integrate with Pharmacy, Lab, and Billing later.

## Roles

| Role          | Register Visit | View / Search | Update Status | Clinical Notes | Refer |
|---------------|----------------|---------------|---------------|----------------|-------|
| ADMIN         | ✓              | ✓             | ✓             | ✓              | ✓     |
| OPD_MANAGER   | ✓              | ✓             | ✓             | ✓              | ✓     |
| DOCTOR        | —              | ✓             | ✓             | ✓              | ✓     |
| OPD_NURSE     | ✓              | ✓             | ✓             | —              | —     |
| RECEPTIONIST  | ✓ (limited)    | ✓             | —             | —              | —     |
| HELP_DESK     | ✗              | ✓ (view only) | ✗             | ✗              | ✗     |

*Auth is currently disabled; re-enable and apply `@PreAuthorize` per endpoint when ready.*

---

## Visit Workflow

1. **REGISTERED** — OPD visit created for an existing patient (UHID), doctor assigned, visit number and token generated.
2. **IN_CONSULTATION** — Patient is with the doctor; clinical notes can be added/updated.
3. **COMPLETED** — Consultation finished; no further status change required.
4. **REFERRED** — Patient referred to another department/doctor or to IPD (admission request). Status set to REFERRED when referral is submitted.
5. **CANCELLED** — Visit cancelled.

Token number is auto-generated per doctor per day (resets daily). Visit number format: `OPD-YYYY-XXXXXX` (e.g. `OPD-2025-000001`).

---

## API Contract

Base path: `/api` (context) + `/opd/visits` (controller mapping). All endpoints return JSON.

| Method | Endpoint | Description | Status codes |
|--------|----------|-------------|--------------|
| POST   | `/api/opd/visits` | Register OPD visit | 201 Created, 400 Bad Request |
| GET    | `/api/opd/visits/{id}` | Get OPD visit by ID | 200 OK, 404 Not Found |
| GET    | `/api/opd/visits` | Search OPD visits (paginated) | 200 OK |
| GET    | `/api/opd/visits/queue` | Get consultation queue for a doctor/date | 200 OK |
| PUT    | `/api/opd/visits/{id}/status` | Update visit status | 200 OK, 400, 404 |
| POST   | `/api/opd/visits/{id}/notes` | Add/update clinical notes | 201 Created, 400, 404 |
| POST   | `/api/opd/visits/{id}/refer` | Refer visit (department/doctor/IPD) | 200 OK, 400, 404 |

### Query parameters

- **GET /api/opd/visits (search):**  
  `visitDate` (ISO date), `doctorId`, `status` (REGISTERED \| IN_CONSULTATION \| COMPLETED \| REFERRED \| CANCELLED), `patientUhid`, `visitNumber`, `page` (default 0), `size` (default 20).
- **GET /api/opd/visits/queue:**  
  `doctorId` (required), `visitDate` (optional, default today).

---

## Sample JSON

### Request — Register OPD visit

```json
POST /api/opd/visits
Content-Type: application/json

{
  "patientUhid": "HMS-2025-000001",
  "doctorId": 1,
  "visitDate": "2025-01-29"
}
```

`visitDate` is optional; if omitted, today’s date is used.

### Response — 201 Created (OPD visit)

```json
{
  "id": 1,
  "visitNumber": "OPD-2025-000001",
  "patientUhid": "HMS-2025-000001",
  "patientId": 1,
  "patientName": "John Doe",
  "doctorId": 1,
  "doctorName": "Dr. Smith",
  "doctorCode": "DOC001",
  "departmentId": 1,
  "departmentName": "General Medicine",
  "visitDate": "2025-01-29",
  "visitStatus": "REGISTERED",
  "tokenNumber": 1,
  "referredToDepartmentId": null,
  "referredToDoctorId": null,
  "referToIpd": false,
  "referralRemarks": null,
  "clinicalNote": null,
  "createdAt": "2025-01-29T10:00:00Z",
  "updatedAt": "2025-01-29T10:00:00Z"
}
```

### Response — Get visit (200 OK)

Same structure as above. If clinical notes exist, `clinicalNote` is populated:

```json
"clinicalNote": {
  "id": 1,
  "chiefComplaint": "Fever and cough",
  "provisionalDiagnosis": "Upper respiratory infection",
  "doctorRemarks": "Prescribed rest and medication.",
  "createdAt": "2025-01-29T10:30:00Z",
  "updatedAt": "2025-01-29T10:30:00Z"
}
```

### Request — Update status

```json
PUT /api/opd/visits/1/status
Content-Type: application/json

{
  "status": "IN_CONSULTATION"
}
```

Allowed values: `REGISTERED`, `IN_CONSULTATION`, `COMPLETED`, `REFERRED`, `CANCELLED`.

### Request — Add/update clinical notes

```json
POST /api/opd/visits/1/notes
Content-Type: application/json

{
  "chiefComplaint": "Fever and cough",
  "provisionalDiagnosis": "Upper respiratory infection",
  "doctorRemarks": "Prescribed rest and medication."
}
```

All fields are optional; only provided fields are updated.

### Request — Refer visit

```json
POST /api/opd/visits/1/refer
Content-Type: application/json

{
  "referredToDepartmentId": 2,
  "referredToDoctorId": 5,
  "referToIpd": false,
  "referralRemarks": "For specialist opinion."
}
```

Any of the fields can be omitted. `referToIpd: true` indicates IPD admission request.

### Response — Search (200 OK)

Paginated response:

```json
{
  "content": [
    {
      "id": 1,
      "visitNumber": "OPD-2025-000001",
      "patientUhid": "HMS-2025-000001",
      "patientName": "John Doe",
      "doctorId": 1,
      "doctorName": "Dr. Smith",
      "departmentName": "General Medicine",
      "visitDate": "2025-01-29",
      "visitStatus": "REGISTERED",
      "tokenNumber": 1,
      "clinicalNote": null,
      "createdAt": "2025-01-29T10:00:00Z",
      "updatedAt": "2025-01-29T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Response — Queue (200 OK)

Array of OPD visit objects (same structure as search `content` items), ordered by token number for the given doctor and date.

---

## Database (DB-agnostic)

- **OPD visits:** JPA entities `OPDVisit`, `OPDClinicalNote`, `OPDToken`; schema auto-generated.
- **Indexes:** `visit_date`, `doctor_id`, `visit_status` for performance on both H2 and MySQL.
- **Token:** Generated per doctor per day; reset each day. No native SQL; JPQL/derived queries only.

---

## Integration

- **Reception:** Patient must exist; use UHID from Reception (e.g. `HMS-YYYY-XXXXXX`) when registering OPD visit.
- **Doctors:** Doctor and department come from Doctors module; use active doctors for assignment.
- **IPD:** Refer with `referToIpd: true` for admission request (IPD module to consume later).
- **Pharmacy / Lab:** Will consume OPD visit ID for prescriptions and lab orders (future).

---

## Frontend (React)

- **Routes:** `/opd` (dashboard), `/opd/register`, `/opd/queue`, `/opd/visits` (search), `/opd/visits/:id` (detail/consultation).
- **Features:** OPD dashboard, register visit (UHID + doctor + date), token/queue by doctor and date, search visits with filters, visit detail with status update, clinical notes form, referral form. API via Axios; loading, success, and error states handled.
