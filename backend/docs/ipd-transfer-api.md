# IPD Transfer / Upgrade API (`/api/ipd/transfers`)

IPD Transfer workflow: **Doctor recommendation → Family consent → Bed selection → Patient shift.**

Examples: General → ICU, ICU → Private.

## Workflow steps

1. **Doctor recommendation** – Recommending doctor, from/to ward type, notes, optional emergency flag.
2. **Family consent** – Consent given, name, relation, mode (WRITTEN, DIGITAL, VERBAL).
3. **Bed selection** – New bed ID (must be available; validated before reserve).
4. **Patient shift** – Execute transfer: old bed → VACANT, new bed → OCCUPIED, admission status → SHIFTED (TRANSFERRED).

## System updates on successful transfer

- **Old bed** → VACANT (stored as `AVAILABLE` in DB).
- **New bed** → OCCUPIED.
- **Admission status** → SHIFTED (stored as `TRANSFERRED`).

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/ipd/transfers` | Full workflow in one call (recommend + consent + bed + shift) |
| POST   | `/api/ipd/transfers/recommend` | Step 1: Doctor recommendation only |
| POST   | `/api/ipd/transfers/consent` | Step 2: Record family consent |
| POST   | `/api/ipd/transfers/confirm-bed` | Step 3: Reserve selected bed |
| POST   | `/api/ipd/transfers/execute` | Step 4: Execute patient shift |
| GET    | `/api/ipd/transfers/{ipdAdmissionId}` | Get transfer history for an admission |

**Access:** POST `/api/ipd/transfers` requires a role that can execute (NURSE or ADMIN). Step endpoints are restricted by role (DOCTOR recommend, IPD_MANAGER approve, NURSE execute). All require authentication.

---

## POST /api/ipd/transfers (full workflow)

Single request performs: recommend → consent → confirm-bed → execute. Validations apply (consent required unless emergency; bed must be available).

### Request body (IPDTransferFullRequestDto)

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| ipdAdmissionId | long | Yes | IPD admission to transfer |
| recommendedByDoctorId | long | Yes | Doctor recommending transfer |
| fromWardType | string | Yes | e.g. GENERAL, ICU |
| toWardType | string | Yes | e.g. ICU, PRIVATE |
| recommendationNotes | string | No | Clinical notes |
| emergencyFlag | boolean | No | If true, consent can be bypassed (service policy) |
| consentGiven | boolean | Yes | Family consent given |
| consentByName | string | No | Name of person giving consent |
| relationToPatient | string | No | e.g. Son, Spouse |
| consentMode | string | Yes | WRITTEN, DIGITAL, or VERBAL |
| newBedId | long | Yes | Target bed (must be available) |
| nurseId | long | No | Nurse performing shift |
| attendantId | long | No | Attendant ID |
| equipmentUsed | string | No | OXYGEN, MONITOR |
| transferStatus | string | No | Default COMPLETED |
| remarks | string | No | Transfer remarks |

**Ward types:** GENERAL, SEMI_PRIVATE, PRIVATE, ICU, CCU, NICU, HDU, EMERGENCY.

### Example 1: General → ICU

```json
{
  "ipdAdmissionId": 1,
  "recommendedByDoctorId": 1,
  "fromWardType": "GENERAL",
  "toWardType": "ICU",
  "recommendationNotes": "Patient requires ICU monitoring; vitals unstable.",
  "emergencyFlag": false,
  "consentGiven": true,
  "consentByName": "John Doe",
  "relationToPatient": "Son",
  "consentMode": "WRITTEN",
  "newBedId": 5,
  "nurseId": 1,
  "equipmentUsed": "OXYGEN",
  "transferStatus": "COMPLETED"
}
```

### Example 2: ICU → Private

```json
{
  "ipdAdmissionId": 2,
  "recommendedByDoctorId": 1,
  "fromWardType": "ICU",
  "toWardType": "PRIVATE",
  "recommendationNotes": "Stable; family requested private room.",
  "emergencyFlag": false,
  "consentGiven": true,
  "consentByName": "Jane Doe",
  "relationToPatient": "Spouse",
  "consentMode": "DIGITAL",
  "newBedId": 12,
  "nurseId": 1
}
```

### Response: 201 Created (IPDTransferFullResponseDto)

```json
{
  "ipdAdmissionId": 1,
  "admissionNumber": "IPD-2025-00001",
  "admissionStatus": "TRANSFERRED",
  "currentBedId": 5,
  "currentBedNumber": "ICU-1",
  "currentWardId": 2,
  "currentWardName": "ICU",
  "recommendationId": 1,
  "consentId": 1,
  "bedReservationId": 1,
  "patientTransferId": 1,
  "oldBedStatus": "VACANT",
  "newBedStatus": "OCCUPIED",
  "systemUpdateSummary": "Old bed → VACANT; New bed → OCCUPIED; Admission status → SHIFTED (TRANSFERRED).",
  "transferredAt": "2025-02-03T10:30:00Z"
}
```

### Error cases

- **404** – IPD admission, doctor, or bed not found.
- **400** – Admission not active; bed not available or already reserved; consent required (non-emergency) but not given; invalid ward type or consent mode.
