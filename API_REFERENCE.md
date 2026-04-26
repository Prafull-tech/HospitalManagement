# API Reference

Complete REST API documentation for the Hospital Management System.

**Base URL:** `http://localhost:8080/api`

---

## Table of Contents

1. [Authentication](#authentication)
2. [Patient Management](#patient-management)
3. [Appointment Management](#appointment-management)
4. [Doctor Management](#doctor-management)
5. [OPD Module](#opd-module)
6. [IPD Module](#ipd-module)
7. [Ward & Bed Management](#ward--bed-management)
8. [Billing Module](#billing-module)
9. [Pharmacy Module](#pharmacy-module)
10. [Laboratory Module](#laboratory-module)
11. [Nursing Module](#nursing-module)
12. [Prescription Module](#prescription-module)
13. [Dashboard APIs](#dashboard-apis)
14. [System Administration](#system-administration)
15. [Audit Trail](#audit-trail)

---

## Authentication

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "uuid-string",
  "expiresIn": 1800,
  "user": {
    "username": "string",
    "role": "ADMIN",
    "hospitalId": 1
  }
}
```

### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "uuid-string"
}
```

### Logout
```http
POST /api/auth/logout
Authorization: Bearer <token>
```

### Change Password
```http
POST /api/auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "string",
  "newPassword": "string"
}
```

### Current User Profile
```http
GET /api/auth/me
Authorization: Bearer <token>
```

### Update Profile
```http
PUT /api/auth/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "string",
  "email": "string",
  "phone": "string"
}
```

### Register User (Admin)
```http
POST /api/auth/register
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "string",
  "password": "string",
  "role": "DOCTOR",
  "hospitalId": 1
}
```

---

## Patient Management

### Register New Patient
```http
POST /api/patients
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "string",
  "age": 30,
  "gender": "MALE",
  "phone": "string",
  "address": "string",
  "bloodGroup": "O+"
}
```

**Response:**
```json
{
  "uhid": "UHID202401010001",
  "registrationNumber": "REG001",
  "fullName": "string",
  "message": "Patient registered successfully"
}
```

### Get Patient by UHID
```http
GET /api/patients/{uhid}
Authorization: Bearer <token>
```

### Get Patient Card
```http
GET /api/patients/{uhid}/card
Authorization: Bearer <token>
```

---

## Appointment Management

### Get Appointment Dashboard
```http
GET /api/appointments/dashboard
Authorization: Bearer <token>
```

### Create Appointment
```http
POST /api/appointments
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "doctorId": 1,
  "departmentId": 1,
  "appointmentDate": "2024-01-15",
  "slotTime": "10:00",
  "visitType": "NEW",
  "source": "FRONT_DESK"
}
```

### Create Walk-in Appointment
```http
POST /api/appointments/walkin
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "doctorId": 1,
  "departmentId": 1
}
```

### Create Online Appointment
```http
POST /api/appointments/online
Content-Type: application/json

{
  "patientId": 1,
  "doctorId": 1,
  "appointmentDate": "2024-01-15",
  "slotTime": "10:00"
}
```

### Reschedule Appointment
```http
PUT /api/appointments/{id}/reschedule
Authorization: Bearer <token>
Content-Type: application/json

{
  "newAppointmentDate": "2024-01-16",
  "newSlotTime": "11:00"
}
```

### Cancel Appointment
```http
PUT /api/appointments/{id}/cancel
Authorization: Bearer <token>
```

### Mark No-Show
```http
PUT /api/appointments/{id}/no-show
Authorization: Bearer <token>
```

### Convert to OPD
```http
POST /api/appointments/{id}/convert-to-opd
Authorization: Bearer <token>
```

### Confirm Appointment
```http
PUT /api/appointments/{id}/confirm
Authorization: Bearer <token>
```

### Search Appointments
```http
GET /api/appointments/search?doctorId=1&date=2024-01-15&status=BOOKED
Authorization: Bearer <token>
```

### Get Doctor Queue
```http
GET /api/appointments/queue/{doctorId}
Authorization: Bearer <token>
```

### Get Appointment by ID
```http
GET /api/appointments/{id}
Authorization: Bearer <token>
```

---

## Doctor Management

### Create Doctor
```http
POST /api/doctors
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "DOC001",
  "fullName": "Dr. John Doe",
  "departmentId": 1,
  "specialization": "Cardiology",
  "doctorType": "CONSULTANT",
  "status": "ACTIVE",
  "email": "doctor@hospital.com",
  "phone": "string"
}
```

### Update Doctor
```http
PUT /api/doctors/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "Dr. John Smith",
  "specialization": "Interventional Cardiology"
}
```

### Get Doctor by ID
```http
GET /api/doctors/{id}
Authorization: Bearer <token>
```

### Search Doctors
```http
GET /api/doctors?departmentId=1&status=ACTIVE&search=cardio
Authorization: Bearer <token>
```

### Update Doctor Availability
```http
POST /api/doctors/{id}/availability
Authorization: Bearer <token>
Content-Type: application/json

{
  "schedules": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00",
      "endTime": "17:00",
      "slotDurationMinutes": 15
    }
  ]
}
```

### Get Doctor Availability
```http
GET /api/doctors/{id}/availability
Authorization: Bearer <token>
```

### Get Current Doctor Profile
```http
GET /api/doctors/me
Authorization: Bearer <token>
```

### Update Current Doctor Availability
```http
PUT /api/doctors/me/availability
Authorization: Bearer <token>
```

### Get Current Doctor's Appointments
```http
GET /api/doctors/me/appointments
Authorization: Bearer <token>
```

---

## OPD Module

### Register OPD Visit
```http
POST /api/opd/visits
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "doctorId": 1,
  "visitType": "OPD",
  "chiefComplaint": "Chest pain since 2 days"
}
```

**Response:**
```json
{
  "visitNumber": "OPD202401150001",
  "tokenNumber": 15,
  "visitId": 100
}
```

### Get OPD Visit by ID
```http
GET /api/opd/visits/{id}
Authorization: Bearer <token>
```

### Search OPD Visits
```http
GET /api/opd/visits?patientId=1&doctorId=1&fromDate=2024-01-01&toDate=2024-01-31
Authorization: Bearer <token>
```

### Get OPD Queue
```http
GET /api/opd/visits/queue?doctorId=1
Authorization: Bearer <token>
```

### Update Visit Status
```http
PUT /api/opd/visits/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "visitStatus": "COMPLETED"
}
```

### Add Clinical Notes
```http
POST /api/opd/visits/{id}/notes
Authorization: Bearer <token>
Content-Type: application/json

{
  "chiefComplaint": "string",
  "provisionalDiagnosis": "string",
  "doctorRe remarks": "string",
  "advice": "string"
}
```

### Refer Patient
```http
POST /api/opd/visits/{id}/refer
Authorization: Bearer <token>
Content-Type: application/json

{
  "referredToDoctorId": 2,
  "referralReason": "string"
}
```

---

## IPD Module

### Admit Patient
```http
POST /api/ipd/admissions
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "primaryDoctorId": 1,
  "admissionType": "ELECTIVE",
  "diagnosis": "Acute myocardial infarction",
  "priority": "P2",
  "wardId": 1,
  "bedId": 1
}
```

**Response:**
```json
{
  "admissionNumber": "IPD202401150001",
  "admissionId": 50,
  "message": "Patient admitted successfully"
}
```

### Get IPD Admission by ID
```http
GET /api/ipd/admissions/{id}
Authorization: Bearer <token>
```

### Get IPD Admission View
```http
GET /api/ipd/admissions/{id}/view
Authorization: Bearer <token>
```

### Get Patient Timeline
```http
GET /api/ipd/admissions/{id}/timeline
Authorization: Bearer <token>
```

### Search IPD Admissions
```http
GET /api/ipd/admissions?patientId=1&status=ACTIVE&fromDate=2024-01-01
Authorization: Bearer <token>
```

### Transfer Patient
```http
POST /api/ipd/admissions/{id}/transfer
Authorization: Bearer <token>
Content-Type: application/json

{
  "newWardId": 2,
  "newBedId": 5,
  "transferReason": "string"
}
```

### Initiate Discharge
```http
POST /api/ipd/admissions/{id}/discharge
Authorization: Bearer <token>
Content-Type: application/json

{
  "dischargeSummary": "string",
  "dischargeAdvice": "string",
  "followUpDate": "2024-02-01"
}
```

### Priority Override
```http
PUT /api/ipd/admissions/{id}/priority-override
Authorization: Bearer <token>
Content-Type: application/json

{
  "newPriority": "P1",
  "overrideReason": "Emergency - critical condition"
}
```

---

## Ward & Bed Management

### Create Ward
```http
POST /api/wards
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "ICU01",
  "name": "Intensive Care Unit",
  "wardType": "ICU",
  "floor": "2nd Floor",
  "capacity": 10,
  "dailyChargePerBed": 5000.00
}
```

### List Wards
```http
GET /api/wards
Authorization: Bearer <token>
```

### Get Ward by ID
```http
GET /api/wards/{id}
Authorization: Bearer <token>
```

### Update Ward
```http
PUT /api/wards/{id}
Authorization: Bearer <token>
```

### Create Bed
```http
POST /api/beds
Authorization: Bearer <token>
Content-Type: application/json

{
  "wardId": 1,
  "roomId": 1,
  "bedNumber": "ICU-BED-01",
  "bedStatus": "AVAILABLE"
}
```

### List Beds
```http
GET /api/beds?wardId=1&status=AVAILABLE
Authorization: Bearer <token>
```

### Get Available Beds
```http
GET /api/beds/available?wardType=ICU
Authorization: Bearer <token>
```

### Update Bed Status
```http
PUT /api/beds/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "bedStatus": "OCCUPIED"
}
```

### Create Room
```http
POST /api/rooms
Authorization: Bearer <token>
Content-Type: application/json

{
  "wardId": 1,
  "roomNumber": "ICU-101",
  "capacity": 1,
  "roomType": "SINGLE",
  "status": "AVAILABLE"
}
```

### List Rooms
```http
GET /api/rooms?wardId=1
Authorization: Bearer <token>
```

### Update Room
```http
PUT /api/rooms/{id}
Authorization: Bearer <token>
```

---

## Billing Module

### Add Billing Item
```http
POST /api/billing/add-item
Authorization: Bearer <token>
Content-Type: application/json

{
  "ipdAdmissionId": 50,
  "serviceType": "BED",
  "serviceName": "ICU Bed Charge - Day 1",
  "quantity": 1,
  "unitPrice": 5000.00,
  "totalPrice": 5000.00
}
```

### Get Billing Account (IPD)
```http
GET /api/billing/account/{ipdId}
Authorization: Bearer <token>
```

### Get IPD Billing Details
```http
GET /api/billing/ipd/{ipdId}
Authorization: Bearer <token>
```

### Get Billing Account Items
```http
GET /api/billing/account/{ipdId}/items
Authorization: Bearer <token>
```

### Get OPD Billing Account
```http
GET /api/billing/account/opd/{opdId}
Authorization: Bearer <token>
```

### Get OPD Billing Items
```http
GET /api/billing/account/opd/{opdId}/items
Authorization: Bearer <token>
```

### Get Billing Dashboard Summary
```http
GET /api/billing/dashboard/summary
Authorization: Bearer <token>
```

### Get Billing Transactions
```http
GET /api/billing/transactions?ipdId=50
Authorization: Bearer <token>
```

### Record Payment
```http
POST /api/billing/payment
Authorization: Bearer <token>
Content-Type: application/json

{
  "ipdAdmissionId": 50,
  "amount": 10000.00,
  "mode": "CASH",
  "referenceNo": "string"
}
```

### Finalize Bill (Discharge)
```http
POST /api/billing/finalize/{ipdId}
Authorization: Bearer <token>
```

---

## Pharmacy Module

### Get IPD Issue Queue
```http
GET /api/pharmacy/ipd/issue-queue
Authorization: Bearer <token>
```

### Get Issue Queue
```http
GET /api/pharmacy/issue-queue
Authorization: Bearer <token>
```

### Get Pending Orders for IPD
```http
GET /api/pharmacy/pending/{ipdId}
Authorization: Bearer <token>
```

### Get Batch Suggestion
```http
GET /api/pharmacy/batch/suggest/{medicineId}
Authorization: Bearer <token>
```

### Issue Medication
```http
POST /api/pharmacy/issue
Authorization: Bearer <token>
Content-Type: application/json

{
  "medicationOrderId": 100,
  "batchId": 5,
  "quantity": 10
}
```

### Create Medication Order
```http
POST /api/pharmacy/medication-orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "ipdAdmissionId": 50,
  "medicines": [
    {
      "medicineId": 10,
      "dosage": "500mg",
      "frequency": "BD",
      "quantity": 20,
      "priority": "NORMAL"
    }
  ]
}
```

### Issue IPD Indent
```http
POST /api/pharmacy/ipd/indents/{id}/issue
Authorization: Bearer <token>
```

### Get FEFO Stock
```http
GET /api/pharmacy/stock/fefo?medicineId=10
Authorization: Bearer <token>
```

### Get Stock Alerts
```http
GET /api/pharmacy/alerts
Authorization: Bearer <token>
```

### Acknowledge Alert
```http
POST /api/pharmacy/alerts/{id}/ack
Authorization: Bearer <token>
```

### Get Today's Summary
```http
GET /api/pharmacy/summary/today
Authorization: Bearer <token>
```

### Create Medicine
```http
POST /api/pharmacy/medicines
Authorization: Bearer <token>
Content-Type: application/json

{
  "medicineCode": "MED001",
  "medicineName": "Paracetamol",
  "category": "TABLET",
  "strength": "500mg",
  "form": "TABLET",
  "minStock": 100,
  "unitPrice": 1.50
}
```

### List Medicines
```http
GET /api/pharmacy/medicines?search=paracetamol
Authorization: Bearer <token>
```

### Update Medicine
```http
PUT /api/pharmacy/medicines/{id}
Authorization: Bearer <token>
```

### Delete Medicine
```http
DELETE /api/pharmacy/medicines/{id}
Authorization: Bearer <token>
```

### Get Medicine by Barcode
```http
GET /api/pharmacy/medicines/barcode/{barcode}
Authorization: Bearer <token>
```

### Lookup Medicine by Barcode
```http
GET /api/pharmacy/medicines/lookup/{barcode}
Authorization: Bearer <token>
```

### Add Medicine Manually
```http
POST /api/pharmacy/medicines/manual
Authorization: Bearer <token>
Content-Type: application/json

{
  "medicineCode": "MED001",
  "quantity": 100,
  "batchNumber": "BATCH123",
  "expiryDate": "2025-12-31"
}
```

### Add Medicine via Barcode
```http
POST /api/pharmacy/medicines/barcode
Authorization: Bearer <token>
Content-Type: application/json

{
  "barcode": "1234567890",
  "quantity": 50
}
```

### Add Existing Medicine Stock
```http
POST /api/pharmacy/medicines/existing
Authorization: Bearer <token>
Content-Type: application/json

{
  "medicineId": 10,
  "batchNumber": "BATCH123",
  "quantity": 100,
  "expiryDate": "2025-12-31"
}
```

### Import Medicine Master
```http
POST /api/pharmacy/medicines/import/master
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <CSV file>
```

### Import Stock
```http
POST /api/pharmacy/medicines/import/stock
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <CSV file>
```

### Get Invoice
```http
GET /api/pharmacy/invoice/{invoiceNumber}
Authorization: Bearer <token>
```

### Record Purchase
```http
POST /api/pharmacy/stock/purchase
Authorization: Bearer <token>
Content-Type: application/json

{
  "supplierId": 1,
  "items": [
    {
      "medicineId": 10,
      "quantity": 100,
      "unitPrice": 1.50,
      "batchNumber": "BATCH123",
      "expiryDate": "2025-12-31"
    }
  ]
}
```

### Record Sale
```http
POST /api/pharmacy/stock/sell
Authorization: Bearer <token>
Content-Type: application/json

{
  "items": [
    {
      "medicineId": 10,
      "quantity": 10,
      "unitPrice": 2.00
    }
  ]
}
```

---

## Laboratory Module

### Create Test Master
```http
POST /api/lab/test-masters
Authorization: Bearer <token>
Content-Type: application/json

{
  "testCode": "CBC",
  "testName": "Complete Blood Count",
  "sampleType": "BLOOD",
  "turnaroundTimeHours": 24,
  "price": 500.00,
  "categoryId": 1
}
```

### List Test Masters
```http
GET /api/lab/test-masters
Authorization: Bearer <token>
```

### Get Test Master by ID
```http
GET /api/lab/test-masters/{id}
Authorization: Bearer <token>
```

### Update Test Master
```http
PUT /api/lab/test-masters/{id}
Authorization: Bearer <token>
```

### Delete Test Master
```http
DELETE /api/lab/test-masters/{id}
Authorization: Bearer <token>
```

### Create Lab Order
```http
POST /api/lab/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "patientId": 1,
  "ipdAdmissionId": 50,
  "opdVisitId": 100,
  "orderedByDoctorId": 1,
  "priority": "URGENT",
  "tests": [
    {
      "testId": 1,
      "sampleType": "BLOOD"
    }
  ]
}
```

### List Lab Orders
```http
GET /api/lab/orders?status=PENDING
Authorization: Bearer <token>
```

### Get Lab Order by ID
```http
GET /api/lab/orders/{id}
Authorization: Bearer <token>
```

### Get Lab Orders for IPD
```http
GET /api/lab/orders/ipd/{ipdId}
Authorization: Bearer <token>
```

### Get Lab Orders for OPD
```http
GET /api/lab/orders/opd/{opdId}
Authorization: Bearer <token>
```

### Get Lab Orders for Patient
```http
GET /api/lab/orders/patient/{patientId}
Authorization: Bearer <token>
```

### Process Sample
```http
PUT /api/lab/sample/process/{orderItemId}
Authorization: Bearer <token>
```

### Collect Sample
```http
POST /api/lab/samples/collect
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderItemId": 1,
  "collectedBy": "string",
  "collectionTime": "2024-01-15T10:30:00"
}
```

### Reject Sample
```http
POST /api/lab/samples/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderItemId": 1,
  "rejectionReason": "Hemolyzed sample"
}
```

### Enter Lab Result
```http
POST /api/lab/result
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderItemId": 1,
  "testValue": "12.5",
  "unit": "g/dL",
  "referenceRange": "12.0-16.0"
}
```

### Create Lab Results (Batch)
```http
POST /api/lab/results
Authorization: Bearer <token>
Content-Type: application/json

{
  "results": [
    {
      "orderItemId": 1,
      "testValue": "12.5",
      "unit": "g/dL"
    }
  ]
}
```

### Verify Result
```http
PUT /api/lab/result/verify/{orderItemId}
Authorization: Bearer <token>
```

### Generate Report
```http
POST /api/lab/reports/generate
Authorization: Bearer <token>
Content-Type: application/json

{
  "testOrderId": 100
}
```

### Verify Report
```http
POST /api/lab/reports/{reportId}/verify
Authorization: Bearer <token>
```

### Release Report
```http
POST /api/lab/reports/{reportId}/release
Authorization: Bearer <token>
```

### Get Report PDF
```http
GET /api/lab/report/{orderId}/pdf
Authorization: Bearer <token>
```

### Get Lab Dashboard Summary
```http
GET /api/lab/dashboard/summary
Authorization: Bearer <token>
```

### Get Lab Dashboard Metrics
```http
GET /api/lab/dashboard/metrics
Authorization: Bearer <token>
```

### Search Reports
```http
GET /api/lab/reports?patientId=1&fromDate=2024-01-01
Authorization: Bearer <token>
```

---

## Nursing Module

### Create Nursing Note
```http
POST /api/nursing/notes
Authorization: Bearer <token>
Content-Type: application/json

{
  "ipdAdmissionId": 50,
  "shiftType": "MORNING",
  "noteType": "GENERAL",
  "content": "Patient stable, vitals within normal limits"
}
```

### Search Nursing Notes
```http
GET /api/nursing/notes?ipdAdmissionId=50&shiftType=MORNING
Authorization: Bearer <token>
```

### Get Nursing Note by ID
```http
GET /api/nursing/notes/{id}
Authorization: Bearer <token>
```

### Lock Nursing Note
```http
PUT /api/nursing/notes/{id}/lock
Authorization: Bearer <token>
```

### Record Vital Signs
```http
POST /api/nursing/vital-signs
Authorization: Bearer <token>
Content-Type: application/json

{
  "ipdAdmissionId": 50,
  "bpSystolic": 120,
  "bpDiastolic": 80,
  "temperature": 98.6,
  "pulse": 72,
  "spo2": 98,
  "respiratoryRate": 16
}
```

### Get Vital Signs
```http
GET /api/nursing/vital-signs?ipdAdmissionId=50&fromDate=2024-01-15
Authorization: Bearer <token>
```

---

## Prescription Module

### Create Prescription
```http
POST /api/prescriptions
Authorization: Bearer <token>
Content-Type: application/json

{
  "opdVisitId": 100,
  "ipdAdmissionId": 50,
  "patientId": 1,
  "items": [
    {
      "medicineName": "Paracetamol",
      "dosage": "500mg",
      "frequency": "BD",
      "durationDays": 5
    }
  ]
}
```

### List Prescriptions
```http
GET /api/prescriptions?patientId=1
Authorization: Bearer <token>
```

### Get Prescription by ID
```http
GET /api/prescriptions/{id}
Authorization: Bearer <token>
```

### Get Prescriptions by Patient
```http
GET /api/prescriptions/patient/{patientId}
Authorization: Bearer <token>
```

---

## Dashboard APIs

### Get Dashboard Summary
```http
GET /api/dashboard/summary
Authorization: Bearer <token>
```

### Get OPD Dashboard
```http
GET /api/dashboard/opd
Authorization: Bearer <token>
```

### Get IPD Dashboard
```http
GET /api/dashboard/ipd
Authorization: Bearer <token>
```

### Get Pharmacy Dashboard
```http
GET /api/dashboard/pharmacy
Authorization: Bearer <token>
```

### Get Lab Dashboard
```http
GET /api/dashboard/lab
Authorization: Bearer <token>
```

---

## System Administration

### Super Admin - Create Hospital
```http
POST /api/superadmin/hospitals
Authorization: Bearer <token>
Content-Type: application/json

{
  "hospitalCode": "HOSP001",
  "hospitalName": "City Hospital",
  "subdomain": "cityhospital",
  "logoUrl": "https://..."
}
```

### Super Admin - List Hospitals
```http
GET /api/superadmin/hospitals
Authorization: Bearer <token>
```

### Super Admin - Update Hospital
```http
PUT /api/superadmin/hospitals/{id}
Authorization: Bearer <token>
```

### Super Admin - Create Subscription Plan
```http
POST /api/superadmin/plans
Authorization: Bearer <token>
Content-Type: application/json

{
  "planCode": "BASIC",
  "monthlyPrice": 9999.00,
  "maxUsers": 10,
  "maxBeds": 50,
  "enabledModules": ["OPD", "IPD", "PHARMACY"]
}
```

### Super Admin - List Plans
```http
GET /api/superadmin/plans
Authorization: Bearer <token>
```

### Super Admin - Create Subscription
```http
POST /api/superadmin/subscriptions
Authorization: Bearer <token>
Content-Type: application/json

{
  "hospitalId": 1,
  "planId": 1,
  "startDate": "2024-01-01",
  "endDate": "2025-01-01"
}
```

### Get System Modules
```http
GET /api/system/modules
Authorization: Bearer <token>
```

### Create System Module
```http
POST /api/system/modules
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "OPD",
  "name": "Out-Patient Department",
  "moduleCategory": "CLINICAL",
  "routePath": "/opd"
}
```

### Get System Roles
```http
GET /api/system/roles
Authorization: Bearer <token>
```

### Create System Role
```http
POST /api/system/roles
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "NURSE",
  "name": "Nurse",
  "systemRole": "NURSE"
}
```

### Get Feature Toggles
```http
GET /api/system/feature-toggles
Authorization: Bearer <token>
```

### Update Feature Toggle
```http
PUT /api/system/feature-toggles/{key}
Authorization: Bearer <token>
Content-Type: application/json

{
  "enabled": true,
  "hospitalId": 1
}
```

---

## Audit Trail

### Get Audit Events
```http
GET /api/audit/events?entityType=APPOINTMENT&entityId=100
Authorization: Bearer <token>
```

### Get Audit Event by ID
```http
GET /api/audit/events/{id}
Authorization: Bearer <token>
```

---

## Response Codes

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Server Error |

### Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "patientId",
      "message": "Patient ID is required"
    }
  ],
  "path": "/api/patients"
}
```

---

## Authentication Flow

```
1. POST /api/auth/login → Get accessToken + refreshToken
2. Include accessToken in Authorization header: "Bearer <token>"
3. When accessToken expires (30 min), use refreshToken to get new accessToken
4. POST /api/auth/refresh with refreshToken
5. On logout, POST /api/auth/logout to invalidate tokens
```

---

## Rate Limiting

- Login endpoint: 10 requests per minute per IP
- Other endpoints: No rate limiting (authenticated requests)

---

## Versioning

API version is included in the path: `/api/v1/...` (currently v1 is default)
