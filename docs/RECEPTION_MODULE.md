# Reception / Front Desk Module

First and foundational module of the HMS. OPD, IPD, Billing, Emergency, and Clinical modules will depend on this.

## Roles

| Role          | Register Patient | Search / View Patient |
|---------------|------------------|------------------------|
| ADMIN         | ✓                | ✓                      |
| RECEPTIONIST  | ✓                | ✓                      |
| HELP_DESK     | ✗                | ✓ (view/search only)   |

## UHID Rule

- **Format:** `HMS-YYYY-XXXXXX` (e.g. `HMS-2025-000001`)
- Generated in service layer (`UhidGenerator`), not in DB
- Unique and DB-independent; stored in `patients.uhid` with unique index

---

## API Contract

| Method | Endpoint                         | Description           | Roles                    | Status codes        |
|--------|----------------------------------|-----------------------|--------------------------|---------------------|
| POST   | `/api/reception/patients`        | Register new patient  | ADMIN, RECEPTIONIST      | 201 Created, 400 Bad Request |
| GET    | `/api/reception/patients/{uhid}`| Get patient by UHID   | ADMIN, RECEPTIONIST, HELP_DESK | 200 OK, 404 Not Found |
| GET    | `/api/reception/patients/search`| Search patients       | ADMIN, RECEPTIONIST, HELP_DESK | 200 OK (array)      |

**Query params for search:** `uhid`, `phone`, `name` (at least one; priority: uhid > phone > name).

**Authentication:** HTTP Basic. Demo users: `admin`/`admin123`, `receptionist`/`rec123`, `helpdesk`/`help123`.

---

## Sample JSON

### Request — Register Patient

```json
POST /api/reception/patients
Content-Type: application/json

{
  "fullName": "John Doe",
  "age": 35,
  "gender": "Male",
  "phone": "+91-9876543210",
  "address": "123, Main Street, City"
}
```

### Response — 201 Created

```json
{
  "id": 1,
  "uhid": "HMS-2025-000001",
  "fullName": "John Doe",
  "age": 35,
  "gender": "Male",
  "phone": "+91-9876543210",
  "address": "123, Main Street, City",
  "createdAt": "2025-01-29T10:00:00Z",
  "updatedAt": "2025-01-29T10:00:00Z"
}
```

### Response — Get by UHID (200 OK)

Same structure as above.

### Response — Search (200 OK)

Array of the same object:

```json
[
  {
    "id": 1,
    "uhid": "HMS-2025-000001",
    "fullName": "John Doe",
    "age": 35,
    "gender": "Male",
    "phone": "+91-9876543210",
    "address": "123, Main Street, City",
    "createdAt": "2025-01-29T10:00:00Z",
    "updatedAt": "2025-01-29T10:00:00Z"
  }
]
```

### Error — 400 Validation

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-01-29T10:00:00Z",
  "errors": {
    "fullName": "Full name is required",
    "age": "Age is required"
  }
}
```

### Error — 404 Not Found

```json
{
  "status": 404,
  "message": "Patient not found with UHID: HMS-2025-999999",
  "timestamp": "2025-01-29T10:00:00Z"
}
```

---

## Database Schema (JPA-generated)

Tables are created/updated by Hibernate (`ddl-auto: update`). DB-agnostic types only.

### `patients`

| Column      | Type         | Constraints        |
|-------------|--------------|--------------------|
| id          | BIGINT       | PK, AUTO_INCREMENT |
| uhid        | VARCHAR(50)  | NOT NULL, UNIQUE   |
| full_name   | VARCHAR(255) | NOT NULL           |
| age         | INT          | NOT NULL           |
| gender      | VARCHAR(20)  | NOT NULL           |
| phone       | VARCHAR(20)  |                    |
| address     | VARCHAR(500) |                    |
| created_at  | TIMESTAMP    | NOT NULL           |
| updated_at  | TIMESTAMP    | NOT NULL           |

**Indexes:** `idx_patient_uhid` (unique on `uhid`), `idx_patient_phone` (on `phone`).

### Base entities

- `BaseEntity`: `created_at`, `updated_at` (no id)
- `BaseIdEntity`: extends `BaseEntity`, adds `id` (Long, IDENTITY)

---

## Integration with OPD / IPD Later

- **OPD:** OPD visit/consultation will reference `Patient` by `uhid` (or `patient_id`). Reception ensures the patient exists and has a UHID before OPD registration.
- **IPD:** Admission will link to `Patient` (UHID). Same flow: patient must be registered at reception first.
- **Billing:** All billing will use UHID/patient id from this module; no billing logic in Reception.
- **Emergency:** Emergency can create a “quick registration” that reuses the same `Patient` entity and UHID generator; or emergency module calls Reception API to register and then proceeds.

**Shared:** All modules use the same `Patient` entity and UHID. No duplicate patient master in OPD/IPD.
