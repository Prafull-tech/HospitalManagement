# Doctors / Medical Staff Module

Manages medical professionals and departments. Integrates with OPD, IPD, Emergency, OT, ICU, and Nursing.

## Roles (when auth enabled)

| Role                     | Access |
|--------------------------|--------|
| ADMIN                    | Full   |
| MEDICAL_SUPERINTENDENT   | Full   |
| MEDICAL_DIRECTOR         | Full   |
| HOD                      | Department scope |
| DOCTOR                   | Own profile / availability |
| HR                       | Read-only |

## Enums

- **DoctorType:** CONSULTANT, RMO, RESIDENT, DUTY_DOCTOR  
- **DoctorStatus:** ACTIVE, INACTIVE, ON_LEAVE  

## API Contract

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST   | /api/doctors | Add doctor | 201 Created, 400 Bad Request |
| PUT    | /api/doctors/{id} | Update doctor | 200 OK, 404 Not Found |
| GET    | /api/doctors/{id} | View doctor (with availability) | 200 OK, 404 Not Found |
| GET    | /api/doctors | List/search doctors (code, departmentId, status, search, page, size) | 200 OK (page) |
| POST   | /api/doctors/{id}/availability | Add/update OPD slot (day 1–7, startTime, endTime, onCall) | 201 Created, 404 Not Found |
| GET    | /api/doctors/{id}/availability | List availability | 200 OK |
| GET    | /api/departments | List departments | 200 OK |
| GET    | /api/departments/{id} | Get department | 200 OK, 404 Not Found |
| POST   | /api/departments | Create department | 201 Created |
| PUT    | /api/departments/{id} | Update department | 200 OK, 404 Not Found |

## Sample JSON

### Request – Add doctor

```json
POST /api/doctors
{
  "code": "DOC001",
  "fullName": "Dr. Jane Smith",
  "departmentId": 1,
  "specialization": "Cardiology",
  "doctorType": "CONSULTANT",
  "status": "ACTIVE",
  "phone": "+91-9876543210",
  "email": "jane.smith@hospital.com",
  "qualifications": "MD, DM Cardiology",
  "onCall": false
}
```

### Response – 201 Created

```json
{
  "id": 1,
  "code": "DOC001",
  "fullName": "Dr. Jane Smith",
  "departmentId": 1,
  "departmentName": "Cardiology",
  "departmentCode": "CARD",
  "specialization": "Cardiology",
  "doctorType": "CONSULTANT",
  "status": "ACTIVE",
  "phone": "+91-9876543210",
  "email": "jane.smith@hospital.com",
  "qualifications": "MD, DM Cardiology",
  "onCall": false,
  "createdAt": "2025-01-29T12:00:00Z",
  "updatedAt": "2025-01-29T12:00:00Z",
  "availability": []
}
```

### Request – Add availability (OPD slot)

```json
POST /api/doctors/1/availability
{
  "dayOfWeek": 1,
  "startTime": "09:00",
  "endTime": "17:00",
  "onCall": false
}
```

`dayOfWeek`: 1 = Monday, 7 = Sunday (ISO).

### Response – List departments

```json
GET /api/departments
[
  {
    "id": 1,
    "code": "CARD",
    "name": "Cardiology",
    "description": null,
    "hodDoctorId": null,
    "hodDoctorName": null
  }
]
```

## Database (JPA-generated)

- **medical_departments:** id, code (unique), name, description, hod_doctor_id (FK to doctors)  
- **doctors:** id, code (unique), full_name, department_id (FK), specialization, doctor_type, status, phone, email, qualifications, on_call, created_at, updated_at  
- **doctor_availability:** id, doctor_id (FK), day_of_week (1–7), start_time, end_time, on_call, created_at, updated_at  

Indexes: doctor code, department_id, status; availability (doctor_id, day_of_week) unique.

## Integration with OPD / IPD / Emergency

- **OPD:** Appointments reference `Doctor` (id or code) and use `DoctorAvailability` for slot rules.  
- **IPD:** Admissions can assign consultant/RMO from this module.  
- **Emergency / ICU:** On-call doctors filtered by `onCall` and availability.  
- **OT:** Surgeon and anesthetist are doctors from this module; OT module stores doctor ids.  
- **Nursing:** Can list doctors by department for handover/rounds.

## React structure

- **api/doctors.ts** – doctorsApi (list, getById, create, update, getAvailability, addAvailability), departmentsApi (list, getById)  
- **types/doctor.ts** – DoctorResponse, DoctorRequest, DepartmentResponse, DoctorAvailabilityRequest/Response, PageResponse  
- **pages/DoctorListPage.tsx** – filters (code, department, status, search), table, pagination, Edit / Availability links  
- **pages/DoctorFormPage.tsx** – Add/Edit form, department dropdown, doctor type, status, on-call  
- **pages/DoctorAvailabilityPage.tsx** – Add OPD slot (day, start/end time, on-call), list current slots  

Sidebar: Doctors, Add Doctor. Routes: /doctors, /doctors/new, /doctors/:id/edit, /doctors/:id/availability.
