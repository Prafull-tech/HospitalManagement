# IPD (Inpatient Department) Module

IPD manages admitted patients from OPD referral, Emergency, or direct admission. It integrates with Reception (patient & UHID), Doctors (treating doctor), OPD (admission referrals), wards/beds, and will integrate with Billing later.

---

## 11-Step Admission → Discharge SOP (Mapping)

| Step | SOP Step | Implementation |
|------|----------|----------------|
| 1 | Patient Registration (UHID) | Reception module: patient registration, UHID auto-generated. IPD admit requires valid UHID (patient must exist). |
| 2 | OPD / Emergency Consultation | OPD visit with `admissionRecommended`, `admissionRecommendedAt`, `admissionRecommendedBy`. Only doctor can recommend; stored with visit. |
| 3 | Admission Priority (P1–P4) | `AdmissionPriorityEvaluationService`, `PriorityCode` (P1 Emergency, P2 Serious, P3 Stable, P4 Elective). Queue order: `AdmissionQueueService`. |
| 4 | Bed Availability Check | `GET /api/ipd/hospital-beds` (ward-wise). Bed statuses: VACANT, OCCUPIED, RESERVED, CLEANING. Only VACANT (`selectableForAdmission`, `vacantOnly=true`) selectable. View + select only; no allocation yet. |
| 5 | IPD Admit Patient | `POST /api/ipd/admit`: UHID, treating doctor, ward type, bed, admission date/time, diagnosis; optional deposit, insurance; document refs. IPD number generated (e.g. IPD-2025-00001). Bed → RESERVED, admission status → ADMITTED. |
| 6 | Patient Shift to Ward | `POST /api/ipd/{admissionId}/shift-to-ward` (nursing). Bed → OCCUPIED, admission status → ACTIVE. Shift timestamp mandatory. |
| 7 | Treatment & Daily Management | Doctor orders, nursing notes, pharmacy, lab, billing linked by IPD admission ID. Timeline: `GET /api/ipd/admissions/{id}/timeline`. Charges auto-added to billing. |
| 8 | Transfer / Upgrade | `POST /api/ipd/admissions/{id}/transfer`. Old bed → VACANT, new bed → OCCUPIED, admission status → TRANSFERRED (SHIFTED). |
| 9 | Billing & Insurance | Billing module: charges per admission. **Rule:** Billing clearance should be verified before final discharge (integrate in discharge flow when billing API is ready). |
| 10 | Discharge Process | `POST /api/ipd/admissions/{id}/discharge` (initiate then complete). Admission status → DISCHARGED, bed → VACANT (released). |
| 11 | Post-Discharge (MRD) | Archive and audit via status history and admission/transfer/discharge audit logs. MRD integration when module is implemented. |

**Status types (SOP):** ACTIVE, DISCHARGED, SHIFTED (TRANSFERRED), REFERRED, LAMA, EXPIRED — plus ADMITTED, DISCHARGE_INITIATED, CANCELLED for workflow.

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

1. **ADMITTED** — Patient admitted (paperwork); bed RESERVED.
2. **ACTIVE** — Nursing staff performed shift-to-ward; bed OCCUPIED. Initial assessment and vitals can be recorded.
3. **TRANSFERRED** — Patient moved to another ward/bed (Shifted).
4. **DISCHARGE_INITIATED** — Discharge process started (medical clearance etc.).
5. **DISCHARGED** — Patient discharged; bed released.
6. **CANCELLED** — Admission cancelled.
7. **REFERRED** — Patient referred elsewhere.
8. **LAMA** — Left against medical advice.
9. **EXPIRED** — Deceased.

### Admission types

- **OPD_REFERRAL** — From OPD (optional `opdVisitId` to link).
- **EMERGENCY** — Emergency admission (can bypass OPD).
- **DIRECT** — Direct admission.

### Business rules

- **One active admission per patient** — Cannot admit if patient already has an admission in ADMITTED, TRANSFERRED, or DISCHARGE_INITIATED.
- **One active allocation per bed** — A bed is available only if no active allocation (releasedAt is null).

---

## Hospital Bed Availability (IPD admission)

**Read-only.** Used for bed selection during IPD admission; no allocation or status update in this API.

- **Endpoint:** `GET /api/ipd/hospital-beds?hospitalId={id}`
- **Ward types:** General, Semi-Private, Private, ICU, Emergency (and CCU, NICU, HDU if configured).
- **Bed statuses:** VACANT, OCCUPIED, RESERVED, CLEANING (and MAINTENANCE, ISOLATION). Response includes `bedStatus` (enum) and `bedStatusDisplay` (e.g. "VACANT" for AVAILABLE).
- **Selection rule:** Only beds with status **VACANT** (AVAILABLE) may be selected for admission. Response includes `selectableForAdmission: true` for such beds.
- **Query params:** `hospitalId` (optional, for API consistency), `wardType` (optional: GENERAL, SEMI_PRIVATE, PRIVATE, ICU, EMERGENCY), `vacantOnly` (optional, default false — when true, returns only VACANT beds for selection).

---

## API Contract

Base path: `/api`. All endpoints return JSON.

| Method | Endpoint | Description | Status codes |
|--------|----------|-------------|--------------|
| POST   | `/api/ipd/admissions` | Admit patient | 201 Created, 400, 404 |
| GET    | `/api/ipd/admissions/{id}` | Get admission by ID | 200 OK, 404 Not Found |
| GET    | `/api/ipd/admissions` | Search admissions (paginated) | 200 OK |
| GET    | `/api/ipd/admissions/search` | Search by admission #, UHID, patient name, status (paginated) | 200 OK |
| POST   | `/api/ipd/admissions/{id}/transfer` | Transfer to another bed | 200 OK, 400, 404 |
| POST   | `/api/ipd/admissions/{id}/discharge` | Initiate or complete discharge | 200 OK, 400, 404 |
| GET    | `/api/ipd/wards` | List wards (optional filter by wardType) | 200 OK |
| GET    | `/api/ipd/hospital-beds` | Bed availability by ward (read-only; selection only) | 200 OK |
| POST   | `/api/ipd/admit` | IPD Admit Patient (mandatory: UHID, doctor, ward type, bed, admission date/time, diagnosis). Bed set to RESERVED on submit; IPD number generated (e.g. IPD-2025-000001). | 201 Created, 400, 404 |
| POST   | `/api/ipd/{admissionId}/shift-to-ward` | Nursing staff performs shift: bed → OCCUPIED, admission status → ACTIVE. Shift timestamp mandatory. **Nursing roles only.** | 200 OK, 400, 403, 404 |
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
