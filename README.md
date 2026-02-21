# Hospital Management System (HMS)

Backend: **Spring Boot 3.x** (Java 17)  
Frontend: **React** (Vite + TypeScript)  
Database: **MySQL** (dev and prod). Configure via `MYSQL_HOST`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`.

## Module: Reception / Front Desk

First and foundational module. Patient registration, UHID generation, and patient search.  
See [docs/RECEPTION_MODULE.md](docs/RECEPTION_MODULE.md) for API contract, sample JSON, and integration notes.

---

## Quick Start

**Recommended:** Use the unified startup script so the frontend starts only after the backend is ready:

```bash
# Windows (Command Prompt)
start.bat

# Windows (PowerShell)
.\start.ps1
```

This will:
1. Start the backend (Spring Boot)
2. Wait for the backend health check to pass
3. Start the frontend (Vite)

### Manual Start (Backend first, then Frontend)

**Backend** (must be running before frontend):

```bash
cd backend
mvn spring-boot:run
```

- Default profile: **dev** (MySQL). Ensure MySQL is running. Create database: `CREATE DATABASE hms;`
- Credentials: Copy `backend/src/main/resources/application-local.yml.example` to `backend/application-local.yml` and set your MySQL username/password. (application-local.yml is gitignored.)
- API base: **http://localhost:8080/api**
- Health check: http://localhost:8080/api/actuator/health

**Frontend** (start only after backend is ready):

```bash
cd frontend
npm install
npm run dev
```

- `npm run dev` waits for the backend health check before starting Vite.
- Use `npm run dev:only` to start the frontend without waiting (e.g. when backend is already running).
- App: **http://localhost:3000**
- Proxy: `/api` → `http://localhost:8080`

**Login:** All pages require authentication. Demo logins: admin/admin123, pharm/pharm123, nurse/nurse123, doctor/doctor123

---

## Backend Structure

```
backend/src/main/java/com/hospital/hms/
├── HmsApplication.java
├── common/
│   ├── entity/       BaseEntity, BaseIdEntity
│   └── exception/    ResourceNotFoundException, GlobalExceptionHandler
├── config/           SecurityConfig
└── reception/
    ├── entity/       Patient
    ├── dto/          PatientRequestDto, PatientResponseDto
    ├── repository/   PatientRepository
    ├── service/       PatientService, UhidGenerator
    └── controller/   ReceptionPatientController
```

## Frontend Structure

```
frontend/src/
├── main.tsx
├── App.tsx
├── api/              client.ts, reception.ts
├── contexts/         AuthContext.tsx
├── components/       Layout, ProtectedRoute
├── pages/            Login, ReceptionDashboard, PatientRegister, PatientSearch, Unauthorized
└── types/            patient.ts
```

---

## API Endpoints (Reception)

| Method | Endpoint                         | Description          |
|--------|----------------------------------|----------------------|
| POST   | /api/reception/patients          | Register patient     |
| GET    | /api/reception/patients/{uhid}   | Get patient by UHID  |
| GET    | /api/reception/patients/search   | Search (uhid/phone/name) |

All require HTTP Basic auth. Roles: ADMIN, RECEPTIONIST (register + search), HELP_DESK (search only).
