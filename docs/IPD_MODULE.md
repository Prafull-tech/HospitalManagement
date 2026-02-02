# IPD (Inpatient Department) Module

IPD manages admitted patients from OPD referral, Emergency, or direct admission. It integrates with Reception (patient & UHID), Doctors (treating doctor), OPD (admission referrals), wards/beds, and will integrate with Billing later.

## Roles

| Role          | Admit | View / Search | Transfer | Discharge |
|---------------|-------|---------------|----------|-----------|
| ADMIN         | ✓     | ✓             | ✓        | ✓         |
| IPD_MANAGER   | ✓     | ✓             | ✓        | ✓         |
| DOCTOR        | —     | ✓             | ✓        | ✓         |
| NURSE         | ✓     | ✓             | ✓        | —         |
| RECEPTIONIST  | ✓ (admission only) | ✓ | —         | —         |
| HELP_DESK     | ✗     | ✓ (view only) | ✗        | ✗         |

*Auth is currently disabled; re-enable and apply `@PreAuthorize` per endpoint when ready.*

---

## Admission & Discharge Workflow

### Admission status lifecycle

1. **ADMITTED** — Patient admitted; bed allocated.
2. **TRANSFERRED** — Patient moved to another ward/bed.
3. **DISCHARGE_INITIATED** — Discharge process started (medical clearance etc.).
4. **DISCHARGED** — Patient discharged; bed released.
5. **CANCELLED** — Admission cancelled.

### Admission types

- **OPD_REFERRAL** — From OPD (optional `opdVisitId` to link).
- **EMERGENCY** — Emergency admission (can bypass OPD).
- **DIRECT** — Direct admission.

### Business rules

- **One active admission per patient** — Cannot admit if patient already has an admission in ADMITTED, TRANSFERRED, or DISCHARGE_INITIATED.
- **One active allocation per bed** — A bed is available only if no active allocation (releasedAt is null).

---

## API Contract

Base path: `/api`. All endpoints return JSON.

| Method | Endpoint | Description | Status codes |
|--------|----------|-------------|--------------|
| POST   | `/api/ipd/admissions` | Admit patient | 201 Created, 400, 404 |
| GET    | `/api/ipd/admissions/{id}` | Get admission by ID | 200 OK, 404 Not Found |
| GET    | `/api/ipd/admissions` | Search admissions (paginated) | 200 OK |
| POST   | `/api/ipd/admissions/{id}/transfer` | Transfer to another bed | 200 OK, 400, 404 |
| POST   | `/api/ipd/admissions/{id}/discharge` | Initiate or complete discharge | 200 OK, 400, 404 |
| GET    | `/api/ipd/wards` | List wards (optional filter by wardType) | 200 OK |
| GET    | `/api/ipd/beds/availability` | List beds with availability flag | 200 OK |

### Query parameters

- **GET /api/ipd/admissions:**  
  `admissionNumber`, `patientUhid`, `status` (ADMITTED \| TRANSFERRED \| DISCHARGE_INITIATED \| DISCHARGED \| CANCELLED), `fromDate`, `toDate` (ISO date-time), `page` (default 0), `size` (default 20).
- **GET /api/ipd/wards:**  
  `wardType` (optional: GENERAL_WARD \| ICU \| PRIVATE \| SEMI_PRIVATE \| HIGH_DEPENDENCY).

---

## Sample JSON

### Request — Admit patient

```json
POST /api/ipd/admissions
Content-Type: application/json

{
  "patientUhid": "HMS-2025-000001",
  "primaryDoctorId": 1,
  "admissionType": "DIRECT",
  "bedId": 1,
  "opdVisitId": null,
  "remarks": "Routine admission"
}
```

### Response — 201 Created

```json
{
  "id": 1,
  "admissionNumber": "IPD-2025-000001",
  "patientUhid": "HMS-2025-000001",
  "patientId": 1,
  "patientName": "John Doe",
  "primaryDoctorId": 1,
  "primaryDoctorName": "Dr. Smith",
  "primaryDoctorCode": "DOC001",
  "admissionType": "DIRECT",
  "admissionStatus": "ADMITTED",
  "admissionDateTime": "2025-01-29T10:00:00",
  "dischargeDateTime": null,
  "opdVisitId": null,
  "remarks": "Routine admission",
  "dischargeRemarks": null,
  "currentWardId": 1,
  "currentWardName": "General Ward 1",
  "currentBedId": 1,
  "currentBedNumber": "B1",
  "createdAt": "2025-01-29T10:00:00Z",
  "updatedAt": "2025-01-29T10:00:00Z"
}
```

### Request — Transfer

```json
POST /api/ipd/admissions/1/transfer
Content-Type: application/json

{
  "bedId": 5,
  "remarks": "Moved to ICU for observation"
}
```

### Request — Discharge (initiate or complete)

```json
POST /api/ipd/admissions/1/discharge
Content-Type: application/json

{
  "dischargeRemarks": "Patient stable, discharged."
}
```

- First call: status becomes **DISCHARGE_INITIATED**.
- Second call: status becomes **DISCHARGED**, dischargeDateTime set, bed released.

### Response — GET /api/ipd/wards (200 OK)

```json
[
  {
    "id": 1,
    "code": "GW-1",
    "name": "General Ward 1",
    "wardType": "GENERAL_WARD",
    "capacity": 10,
    "isActive": true
  }
]
```

### Response — GET /api/ipd/beds/availability (200 OK)

```json
[
  {
    "bedId": 1,
    "bedNumber": "B1",
    "wardId": 1,
    "wardName": "General Ward 1",
    "wardCode": "GW-1",
    "available": true
  },
  {
    "bedId": 2,
    "bedNumber": "B2",
    "wardId": 1,
    "wardName": "General Ward 1",
    "wardCode": "GW-1",
    "available": false
  }
]
```

---

## Database (DB-agnostic)

- **Entities:** `IPDAdmission`, `Ward`, `Bed`, `BedAllocation`. Enums: `AdmissionStatus`, `AdmissionType`, `WardType`.
- **Indexes:** `admission_number` (unique), `admission_status`, `ward_type`; bed/ward/admission indexes as in entities.
- **Schema:** Auto-generated via JPA; no native SQL; works on H2 and MySQL.
- **Data loader:** `IPDDataLoader` seeds sample wards and beds when none exist (dev).

---

## Integration

- **Reception:** Patient must exist; use UHID when admitting.
- **Doctors:** Primary doctor from Doctors module (active doctors).
- **OPD:** OPD referrals can set `referToIpd: true`; link via `opdVisitId` when admitting from OPD.
- **Billing:** Will consume IPD stay duration (admissionDateTime to dischargeDateTime) later.
- **Nursing / Ward:** Depend on IPD admission ID for rounds and ward tasks.

---

## Frontend (React)

- **Routes:** `/ipd` (dashboard), `/ipd/admit`, `/ipd/beds`, `/ipd/admissions`, `/ipd/admissions/:id`.
- **Features:** IPD dashboard, admission form (OPD/Emergency/Direct, ward & bed selection), bed availability view, IPD patient list (search), admission detail with transfer and discharge actions. API via Axios; loading, success, and error states.
