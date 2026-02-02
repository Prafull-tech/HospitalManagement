# Nursing Notes Search API — Sample Request & Response

**Endpoint:** `GET /api/nursing/search/notes`

**Query parameters:**

| Parameter  | Type   | Required | Description                                      |
|------------|--------|----------|---------------------------------------------------|
| q          | string | no       | Patient name or UHID (partial / fuzzy, case-insensitive) |
| wardType   | enum   | no       | GENERAL, PRIVATE, ICU, CCU, NICU, HDU, etc.       |
| bedNo      | string | no       | Bed number (partial match)                        |
| shift      | enum   | no       | MORNING, EVENING, NIGHT                           |
| status     | enum   | no       | DRAFT, LOCKED                                    |
| fromDate   | date   | no       | ISO date (YYYY-MM-DD), inclusive                  |
| toDate     | date   | no       | ISO date (YYYY-MM-DD), inclusive                  |
| page       | int    | no       | Zero-based page index (default 0)                 |
| size       | int    | no       | Page size (default 20)                            |

**Sample request:**

```
GET /api/nursing/search/notes?q=john&wardType=GENERAL&shift=MORNING&fromDate=2025-01-01&toDate=2025-02-01&page=0&size=20
```

**Sample response (200 OK):**

```json
{
  "content": [
    {
      "noteId": 1,
      "ipdAdmissionId": 101,
      "patientName": "John Doe",
      "uhid": "HMS-2025-000001",
      "wardType": "GENERAL",
      "wardName": "Ward A",
      "bedNo": "B-12",
      "shift": "MORNING",
      "noteDateTime": "2025-02-01T08:30:00",
      "lastUpdated": "2025-02-01T08:35:00",
      "status": "DRAFT",
      "noteType": "GENERAL_OBSERVATION",
      "content": "Patient stable. Vitals within normal range.",
      "recordedByName": "Nurse Smith"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

**Security:** Role-based access (STAFF_NURSE, WARD_INCHARGE, NURSING_SUPERINTENDENT, DOCTOR, ADMIN). Method-level security can restrict by ward when enabled.
