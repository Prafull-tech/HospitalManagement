# Patient Registration API (`/api/patients`)

Patient Registration module for UHID generation. DB-agnostic JPA design.

## Rules

- **UHID:** Auto-generated, hospital-wide unique. Stored with unique index; duplicates are prevented at DB level.
- **Without UHID → IPD Admission not allowed:** IPD admission requires a valid `patientUhid`; the patient must exist in the system. Admission API returns 404 if patient is not found.

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/patients` | Register new patient (auto-generates UHID) |
| GET    | `/api/patients/{uhid}` | Get patient by UHID |
| GET    | `/api/patients/{uhid}/card` | Get print-ready patient card |

## POST /api/patients

**Request body:** Same as `/api/reception/patients` (PatientRequestDto).

Required: `fullName`, `age`, `gender`. Optional: `phone`, `address`, `dateOfBirth`, `idProofType`, `idProofNumber`, etc.

**Response:** 201 Created with `PatientResponseDto` (includes generated `uhid`, `registrationNumber`).

## GET /api/patients/{uhid}

**Response:** 200 OK with `PatientResponseDto`, or 404 if not found.

## GET /api/patients/{uhid}/card

**Response:** 200 OK with `PatientCardDto` (print-ready fields: uhid, fullName, age, ageDisplay, gender, phone, address, registrationNumber, registrationDate, etc.), or 404 if not found.

Used for printing the Patient Card after registration or for re-print.
