# Bed Availability API – Sample JSON

Base path: `POST/GET/PUT/DELETE /api/hospitals/{hospitalId}/bed-availability` and `.../bed-availability/{id}`.

Ward types (enum): `GENERAL`, `SEMI_PRIVATE`, `PRIVATE`, `ICU`, `EMERGENCY`.

---

## 1. POST request (create)

**POST** `/api/hospitals/1/bed-availability`

**General ward:**
```json
{
  "wardType": "GENERAL",
  "totalBeds": 40,
  "occupiedBeds": 28,
  "reservedBeds": 2,
  "underCleaningBeds": 3
}
```

**Semi Private ward:**
```json
{
  "wardType": "SEMI_PRIVATE",
  "totalBeds": 20,
  "occupiedBeds": 14,
  "reservedBeds": 1,
  "underCleaningBeds": 1
}
```

**Private ward:**
```json
{
  "wardType": "PRIVATE",
  "totalBeds": 12,
  "occupiedBeds": 8,
  "reservedBeds": 0,
  "underCleaningBeds": 1
}
```

**ICU:**
```json
{
  "wardType": "ICU",
  "totalBeds": 10,
  "occupiedBeds": 7,
  "reservedBeds": 1,
  "underCleaningBeds": 0
}
```

**Emergency:**
```json
{
  "wardType": "EMERGENCY",
  "totalBeds": 8,
  "occupiedBeds": 5,
  "reservedBeds": 0,
  "underCleaningBeds": 1
}
```

---

## 2. PUT request (update)

**PUT** `/api/hospitals/1/bed-availability/3`

Example: update General ward counts.

```json
{
  "wardType": "GENERAL",
  "totalBeds": 40,
  "occupiedBeds": 30,
  "reservedBeds": 2,
  "underCleaningBeds": 2
}
```

(Vacant is computed server-side: 40 - 30 - 2 - 2 = 6.)

---

## 3. GET response

**GET** `/api/hospitals/1/bed-availability` (list, sorted by ward type)

**200 OK**
```json
[
  {
    "id": 1,
    "hospitalId": 1,
    "wardType": "EMERGENCY",
    "totalBeds": 8,
    "occupied": 5,
    "vacant": 2,
    "reserved": 0,
    "underCleaning": 1,
    "createdAt": "2026-01-30T09:00:00Z",
    "updatedAt": "2026-01-30T09:00:00Z",
    "updatedBy": null
  },
  {
    "id": 2,
    "hospitalId": 1,
    "wardType": "GENERAL",
    "totalBeds": 40,
    "occupied": 28,
    "vacant": 7,
    "reserved": 2,
    "underCleaning": 3,
    "createdAt": "2026-01-30T09:00:00Z",
    "updatedAt": "2026-01-30T09:00:00Z"
  },
  {
    "id": 3,
    "hospitalId": 1,
    "wardType": "ICU",
    "totalBeds": 10,
    "occupied": 7,
    "vacant": 2,
    "reserved": 1,
    "underCleaning": 0,
    "createdAt": "2026-01-30T09:00:00Z",
    "updatedAt": "2026-01-30T09:00:00Z"
  },
  {
    "id": 4,
    "hospitalId": 1,
    "wardType": "PRIVATE",
    "totalBeds": 12,
    "occupied": 8,
    "vacant": 3,
    "reserved": 0,
    "underCleaning": 1,
    "createdAt": "2026-01-30T09:00:00Z",
    "updatedAt": "2026-01-30T09:00:00Z"
  },
  {
    "id": 5,
    "hospitalId": 1,
    "wardType": "SEMI_PRIVATE",
    "totalBeds": 20,
    "occupied": 14,
    "vacant": 4,
    "reserved": 1,
    "underCleaning": 1,
    "createdAt": "2026-01-30T09:00:00Z",
    "updatedAt": "2026-01-30T09:00:00Z"
  }
]
```

**GET** `/api/hospitals/1/bed-availability/2` (single item)

**200 OK**
```json
{
  "id": 2,
  "hospitalId": 1,
  "wardType": "GENERAL",
  "totalBeds": 40,
  "occupied": 28,
  "vacant": 7,
  "reserved": 2,
  "underCleaning": 3,
  "createdAt": "2026-01-30T09:00:00Z",
  "updatedAt": "2026-01-30T09:00:00Z",
  "updatedBy": "ipdmanager"
}
```

**GET** `/api/hospitals/1/bed-availability/2/audit` (read-only audit trail)

**200 OK**
```json
[
  {
    "id": 12,
    "bedAvailabilityId": 2,
    "changedAt": "2026-01-30T14:30:00Z",
    "changedBy": "ipdmanager",
    "performedByRole": "IPD_MANAGER",
    "action": "UPDATE"
  },
  {
    "id": 11,
    "bedAvailabilityId": 2,
    "changedAt": "2026-01-30T09:00:00Z",
    "changedBy": "admin",
    "performedByRole": "ADMIN",
    "action": "CREATE"
  }
]
```

---

## 4. Validation error response

**POST/PUT** with invalid body (e.g. missing `wardType`, negative counts, or occupied + reserved + underCleaning > totalBeds).

**400 Bad Request** – Bean Validation (e.g. missing/blank wardType):
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-01-30T10:15:00Z",
  "errors": {
    "wardType": "Ward type is required (e.g. GENERAL, ICU)"
  }
}
```

**400 Bad Request** – Multiple field errors (e.g. negative values):
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-01-30T10:15:00Z",
  "errors": {
    "totalBeds": "Total beds must be >= 0",
    "occupiedBeds": "Occupied beds must be >= 0"
  }
}
```

**400 Bad Request** – Invalid bed counts (service-level, e.g. sum > total):
```json
{
  "status": 400,
  "message": "Occupied + reserved + under cleaning must not exceed total beds.",
  "timestamp": "2026-01-30T10:15:00Z"
}
```

**400 Bad Request** – Invalid ward type:
```json
{
  "status": 400,
  "message": "Invalid ward type: CCU. Valid values: [GENERAL, SEMI_PRIVATE, PRIVATE, ICU, CCU, NICU, HDU, EMERGENCY]",
  "timestamp": "2026-01-30T10:15:00Z"
}
```

**409 Conflict** – Duplicate hospital + ward type:
```json
{
  "status": 409,
  "message": "Bed availability for ward type GENERAL already exists for this hospital.",
  "timestamp": "2026-01-30T10:15:00Z"
}
```
