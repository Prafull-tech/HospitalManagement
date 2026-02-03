# IPD Admission Status Master API (`/api/ipd/status-master`)

Controlled status transitions and audit of all IPD admission status changes.

## Statuses (master list)

- **ACTIVE** – Patient in ward, bed OCCUPIED
- **DISCHARGED** – Discharge completed
- **SHIFTED** – Display name for TRANSFERRED (patient transferred to another bed/ward)
- **REFERRED** – Referred to another facility
- **LAMA** – Left Against Medical Advice
- **EXPIRED** – Deceased

Additional lifecycle statuses: ADMITTED, DISCHARGE_INITIATED, CANCELLED, TRANSFERRED (stored value for SHIFTED).

## Rules

1. **Controlled transitions** – Only allowed from → to transitions are accepted. Invalid transitions return 400.
2. **Audit** – Every status change is recorded (admission_id, from_status, to_status, changed_at, changed_by, reason).

## Allowed transitions (summary)

| From            | Allowed to                                                                 |
|-----------------|----------------------------------------------------------------------------|
| (initial)       | ADMITTED                                                                   |
| ADMITTED        | ACTIVE, CANCELLED                                                          |
| ACTIVE          | TRANSFERRED (SHIFTED), DISCHARGE_INITIATED, REFERRED, LAMA, EXPIRED        |
| TRANSFERRED     | ACTIVE, DISCHARGE_INITIATED, REFERRED, LAMA, EXPIRED                       |
| DISCHARGE_INITIATED | DISCHARGED                                                             |
| DISCHARGED, CANCELLED, REFERRED, LAMA, EXPIRED | (terminal – no outgoing)                    |

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/ipd/status-master` | List all statuses (code + display name) |
| GET    | `/api/ipd/status-master/transitions?from=ACTIVE` | Allowed target statuses from a given status |
| PATCH  | `/api/ipd/status-master/admissions/{admissionId}/status` | Change status (validated + audited) |
| GET    | `/api/ipd/status-master/admissions/{admissionId}/audit` | Status change audit log for an admission |

---

## GET /api/ipd/status-master

**Response:** 200 OK – Set of `{ "code": "ACTIVE", "displayName": "ACTIVE" }`, etc. TRANSFERRED has displayName `"SHIFTED"`.

---

## GET /api/ipd/status-master/transitions

**Query:** `from` (optional) – AdmissionStatus enum value (e.g. ACTIVE, null for initial).

**Response:** 200 OK – `{ "fromStatus": "ACTIVE", "allowedToStatuses": [ { "code": "TRANSFERRED", "displayName": "SHIFTED" }, ... ] }`.

---

## PATCH /api/ipd/status-master/admissions/{admissionId}/status

**Request body:**

```json
{
  "toStatus": "REFERRED",
  "reason": "Referred to cardiology at XYZ hospital"
}
```

- `toStatus` (required): One of ACTIVE, TRANSFERRED, DISCHARGE_INITIATED, DISCHARGED, CANCELLED, REFERRED, LAMA, EXPIRED.
- `reason` (optional): Audit reason (max 500 chars).

**Response:** 200 OK – Full `IPDAdmissionResponseDto` (admission with updated status).

**Side effects:** When `toStatus` is DISCHARGED, discharge datetime is set and bed allocation is released (bed → AVAILABLE).

**Errors:** 400 if transition not allowed; 404 if admission not found.

---

## GET /api/ipd/status-master/admissions/{admissionId}/audit

**Response:** 200 OK – List of audit entries, newest first:

```json
[
  {
    "admissionId": 1,
    "fromStatus": "ACTIVE",
    "toStatus": "REFERRED",
    "changedAt": "2025-02-03T12:00:00Z",
    "changedBy": "doctor1",
    "reason": "Referred to cardiology"
  }
]
```

All status changes (admit, shift-to-ward, transfer, discharge, and direct PATCH) are audited.
