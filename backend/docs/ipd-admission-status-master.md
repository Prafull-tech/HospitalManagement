# IPD Admission Status Master

Controlled status transitions and audit of all status changes for IPD admissions.

## Master statuses (primary lifecycle)

| Code (DB/API) | Display name | Description |
|---------------|--------------|-------------|
| ACTIVE        | ACTIVE       | Patient in ward, bed OCCUPIED |
| DISCHARGED    | DISCHARGED   | Discharge completed |
| TRANSFERRED   | SHIFTED      | Transferred to another bed/ward |
| REFERRED      | REFERRED     | Referred to another facility |
| LAMA          | LAMA         | Left against medical advice |
| EXPIRED       | EXPIRED      | Death in hospital |

Internal workflow also uses: **ADMITTED** (paperwork done, bed reserved), **DISCHARGE_INITIATED** (discharge in progress), **CANCELLED** (admission cancelled).

## Rules

1. **Status transitions are controlled**  
   Only allowed transitions (see below) can be performed. Invalid transitions return 400 with a message listing allowed targets.

2. **All status changes are audited**  
   Every change is recorded in `admission_status_audit_log` with: `admission_id`, `from_status`, `to_status`, `changed_at`, `changed_by`, `reason`.

## Allowed transitions

| From (current status) | Allowed target statuses |
|-----------------------|--------------------------|
| *(initial)*           | ADMITTED |
| ADMITTED              | ACTIVE, CANCELLED |
| ACTIVE                | TRANSFERRED (SHIFTED), DISCHARGE_INITIATED, REFERRED, LAMA, EXPIRED |
| TRANSFERRED (SHIFTED) | ACTIVE, DISCHARGE_INITIATED, REFERRED, LAMA, EXPIRED |
| DISCHARGE_INITIATED   | DISCHARGED |
| DISCHARGED            | *(none – terminal)* |
| CANCELLED             | *(none – terminal)* |
| REFERRED              | *(none – terminal)* |
| LAMA                  | *(none – terminal)* |
| EXPIRED               | *(none – terminal)* |

## APIs (base path: `/api/ipd/status-master`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/ipd/status-master` | List all statuses (code + displayName). Use `?masterOnly=true` for only the 6 primary statuses. |
| GET    | `/api/ipd/status-master/transitions?from=ACTIVE` | Allowed transitions from a status. `from` optional (null = initial). |
| PATCH  | `/api/ipd/status-master/admissions/{admissionId}/status` | Change status (validated + audited). Body: `{ "toStatus": "REFERRED", "reason": "..." }`. |
| GET    | `/api/ipd/status-master/admissions/{admissionId}/audit` | Status change audit log for the admission (newest first). |

### List statuses (master only)

```http
GET /api/ipd/status-master?masterOnly=true
```

Response: set of `{ "code": "ACTIVE", "displayName": "ACTIVE" }`, etc. `TRANSFERRED` is returned with `displayName: "SHIFTED"`.

### Get allowed transitions

```http
GET /api/ipd/status-master/transitions?from=ACTIVE
```

Response:

```json
{
  "fromStatus": "ACTIVE",
  "allowedToStatuses": [
    { "code": "TRANSFERRED", "displayName": "SHIFTED" },
    { "code": "DISCHARGE_INITIATED", "displayName": "DISCHARGE INITIATED" },
    { "code": "REFERRED", "displayName": "REFERRED" },
    { "code": "LAMA", "displayName": "LAMA" },
    { "code": "EXPIRED", "displayName": "EXPIRED" }
  ]
}
```

### Change status (validated + audited)

```http
PATCH /api/ipd/status-master/admissions/1/status
Content-Type: application/json

{
  "toStatus": "REFERRED",
  "reason": "Referred to cardiology at XYZ hospital"
}
```

- Validates transition (e.g. ACTIVE → REFERRED is allowed).
- Updates admission status.
- Records one row in `admission_status_audit_log` with `changedBy` from authentication and `reason`.
- For `toStatus: DISCHARGED`, also releases bed and sets discharge datetime.

### Get audit log

```http
GET /api/ipd/status-master/admissions/1/audit
```

Response: list of entries (newest first), e.g.:

```json
[
  {
    "admissionId": 1,
    "fromStatus": "ACTIVE",
    "toStatus": "TRANSFERRED",
    "fromStatusDisplay": "ACTIVE",
    "toStatusDisplay": "SHIFTED",
    "changedAt": "2025-02-03T11:00:00Z",
    "changedBy": "nurse1",
    "reason": "Transfer executed from recommendation 2"
  },
  {
    "admissionId": 1,
    "fromStatus": null,
    "toStatus": "ADMITTED",
    "fromStatusDisplay": null,
    "toStatusDisplay": "ADMITTED",
    "changedAt": "2025-02-02T09:00:00Z",
    "changedBy": null,
    "reason": "Admission created"
  }
]
```

## Implementation notes

- **Workflow-driven changes** (admit, shift-to-ward, transfer, discharge) are performed by their respective services; each calls `AdmissionStatusMasterService.recordStatusChange(...)` after updating the admission so every change is audited.
- **Direct status change** (e.g. ACTIVE → REFERRED) is done via PATCH above; validation and audit are handled inside `AdmissionStatusMasterService.changeStatus(...)`.
