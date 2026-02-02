# Hospital Management System (HMS)

Backend: **Spring Boot 3.x** (Java 17)  
Frontend: **React** (Vite + TypeScript)  
Database: **Dual configuration** — H2 (dev) and MySQL (staging/prod). All code is DB-agnostic.

## Module: Reception / Front Desk

First and foundational module. Patient registration, UHID generation, and patient search.  
See [docs/RECEPTION_MODULE.md](docs/RECEPTION_MODULE.md) for API contract, sample JSON, and integration notes.

---

## Quick Start

### Backend

```bash
cd backend
mvn spring-boot:run
```

- Default profile: **dev** (H2 in-memory).
- API base: **http://localhost:8080/api**
- H2 console: http://localhost:8080/h2-console (dev only)

**MySQL (prod):**

```bash
export SPRING_PROFILES_ACTIVE=prod
export MYSQL_HOST=localhost
export MYSQL_DATABASE=hms
export MYSQL_USER=hms
export MYSQL_PASSWORD=hms
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

- App: **http://localhost:3000**
- Proxy: `/api` → `http://localhost:8080` (start backend first)

**Demo logins:** admin / admin123, receptionist / rec123, helpdesk / help123

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
