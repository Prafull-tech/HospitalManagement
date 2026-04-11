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
- `npm run dev -- --host 127.0.0.1 --port 4173` now forwards Vite flags correctly.
- App: **http://localhost:5173**
- Proxy: `/api` → `http://localhost:8080`

**Login:** All pages require authentication. Demo logins: admin/admin123, pharm/pharm123, nurse/nurse123, doctor/doctor123

### Production deployment notes

- **Backend:** Run with `SPRING_PROFILES_ACTIVE=prod`. Set **`JWT_SECRET`** or **`HMS_JWT_SECRET`** to a long random value (not the dev placeholder). Set **`MYSQL_PASSWORD`** (and optionally `MYSQL_HOST`, `MYSQL_DATABASE`, `MYSQL_USER`). The app validates these on startup in `prod` (see `ProductionSecurityValidator`). Use TLS to MySQL in production (`useSSL=true` is set in the prod profile datasource URL).
- **Frontend:** Build with `npm run build` and serve static files behind a reverse proxy that forwards `/api` to the Spring Boot service (same-origin keeps the default `baseURL` of `/api` in `client.ts`). Optional: `VITE_SHOW_STUB_SIDEBAR=true` to show sidebar links for stub modules (Radiology, HR placeholders, etc.) in production builds; default is hidden in prod.
- **User registration:** `POST /api/auth/register` requires an **ADMIN** JWT. The `/register` page is for admins creating accounts, not public self-signup.

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
