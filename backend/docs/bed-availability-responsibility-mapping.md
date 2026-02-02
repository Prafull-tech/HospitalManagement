# Bed Availability – Responsibility Mapping (HIS)

Hospital Information System (HIS) responsibility mapping for bed availability updates: who may change data, who verifies, who approves, and how the system records it.

---

## 1. Responsibility matrix

| Role / Actor | Responsibility | Allowed actions | System recording |
|--------------|----------------|-----------------|------------------|
| **Nurse** | Updates occupied and under-cleaning counts from ward | Update `occupiedBeds`, `underCleaningBeds` | `updatedBy`, `updatedAt`; audit log entry with performed-by role |
| **IPD Desk** | Verifies and confirms counts (reconciliation) | Verify; may update counts after verification | `updatedBy`, `updatedAt`; audit log entry (action: VERIFY / UPDATE) |
| **Ward In-charge** | Approves changes before they are considered final | Approve; may update if authorised | `updatedBy`, `updatedAt`; audit log entry (action: APPROVE / UPDATE) |
| **System** | Logs timestamp and user for every change | — | `updatedAt` (timestamp), `updatedBy` (user identifier); audit trail table |

---

## 2. Workflow (intended)

1. **Nurse** updates occupied / under-cleaning as per ward reality.
2. **IPD Desk** verifies and confirms (reconciliation with IPD records).
3. **Ward In-charge** approves the change (optional step; can be enforced by policy).
4. **System** records every change: `updatedBy` (who), `updatedAt` (when), and an audit log row.

Current API roles (ADMIN, IPD_MANAGER, DOCTOR) map to this as follows:

- **Nurse** → map to a role that can only update occupied/cleaning (e.g. NURSE or IPD_MANAGER with scope).
- **IPD Desk** → IPD_MANAGER (verify & confirm).
- **Ward In-charge** → ADMIN or a dedicated WARD_INCHARGE role (approve).

---

## 3. Audit fields on Bed Availability

| Field | Type | Description |
|-------|------|-------------|
| `updatedAt` | Instant | Set by system on every create/update (JPA `@PreUpdate`). |
| `updatedBy` | String | User identifier (e.g. username) who last updated the record. |

---

## 4. Audit trail (history)

Each change to a bed availability record is appended to an **audit log** (read-only history):

- **Bed availability record**: `updatedBy`, `updatedAt` (last update).
- **Audit log table**: one row per change with `changedAt`, `changedBy`, `performedByRole`, `action` (e.g. UPDATE, VERIFY, APPROVE).

**Read-only audit trail API:**  
**GET** `/api/hospitals/{hospitalId}/bed-availability/{id}/audit`  
Returns the list of audit entries for that record (newest first). Each entry includes `changedAt`, `changedBy`, `performedByRole`, `action` (e.g. CREATE, UPDATE).

---

## 5. Summary

- **Nurse** → updates occupied / cleaning; system logs user and timestamp.
- **IPD Desk** → verifies & confirms; system logs user and timestamp.
- **Ward In-charge** → approves changes; system logs user and timestamp.
- **System** → always sets `updatedAt` and stores `updatedBy`; every change is written to the audit trail and exposed via the read-only audit API.
