# IPD Admission — Daily Management Integration

All daily management modules are integrated with IPD Admission. Every activity is linked to **IPD Admission Number** (and `ipd_admission_id`). Charges are auto-added to billing; a **timeline view** per patient/admission aggregates all events.

---

## Rules

1. **All activities linked with IPD Admission Number**  
   Every entity in Doctor Orders, Nursing Notes, Pharmacy, Lab, and Billing must reference `ipd_admission_id` (and optionally store `admission_number` for display/print). No standalone orders or charges without an admission.

2. **Charges auto-added to billing**  
   When a billable event occurs (e.g. pharmacy dispense, lab test, doctor order execution), the creating module calls the billing service to add a charge line for that admission. Billing does not duplicate business logic; it records charges posted by other modules.

3. **Timeline view per patient**  
   `GET /api/ipd/admissions/{admissionId}/timeline` returns a chronological list of events for that admission: admission, shift-to-ward, nursing notes, vitals, medications, doctor orders, pharmacy, lab, and billing events. Used for daily management and handover views.

---

## Module Contract

| Module          | Links to IPD              | Posts charges to billing      | Timeline event type   |
|-----------------|---------------------------|-------------------------------|------------------------|
| **Doctor Orders** | `ipd_admission_id`        | On order execution (e.g. lab) | DOCTOR_ORDER          |
| **Nursing Notes** | `ipd_admission_id` (done) | Optional (nursing procedure)  | NURSING_NOTE           |
| **Pharmacy**      | `ipd_admission_id`        | On dispense                   | PHARMACY               |
| **Lab**           | `ipd_admission_id`        | On test/collection            | LAB                    |
| **Billing**       | `ipd_admission_id`        | N/A (receives charges)        | BILLING_CHARGE         |

- **Doctor Orders**: Order entity has `ipd_admission_id`, `doctor_id`, `order_type` (MEDICATION, LAB, PROCEDURE, etc.). When an order is executed (e.g. lab sent, drug dispensed), the executing module posts the charge to billing and the event appears on the timeline.
- **Nursing Notes**: Already linked; notes appear on timeline. Optional charge for procedures.
- **Pharmacy**: Dispensation linked to `ipd_admission_id`; on dispense, call billing to add a charge; event type PHARMACY.
- **Lab**: Lab request/result linked to `ipd_admission_id`; on test done/sent, add charge; event type LAB.
- **Billing**: `AdmissionCharge` (or equivalent) stores `ipd_admission_id`, `admission_number`, `charge_type`, `amount`, `reference_type` (e.g. PHARMACY, LAB), `reference_id`. Timeline includes billing charges so that “what was charged when” is visible per patient.

---

## APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/ipd/admissions/{admissionId}/timeline` | Chronological events for this admission (admission, nursing, vitals, MAR, doctor orders, pharmacy, lab, billing). |
| POST | `/api/billing/admissions/{admissionId}/charges` | Add a charge line for an admission (called by Pharmacy, Lab, Doctor Orders, etc.). |

---

## Timeline Event Types

- `ADMISSION` — Admission created / shift-to-ward
- `NURSING_NOTE` — Nursing note recorded
- `VITAL_SIGN` — Vital signs recorded
- `MEDICATION` — Medication administered (MAR)
- `DOCTOR_ORDER` — Doctor order placed / executed
- `PHARMACY` — Pharmacy dispense
- `LAB` — Lab request / result
- `BILLING_CHARGE` — Charge added to bill

---

## Extending New Modules

1. Add an entity with `ipd_admission_id` (and optional `admission_number`).
2. When the event is billable, call `BillingService.addCharge(admissionId, chargeType, amount, description, referenceType, referenceId)`.
3. Register the event in the timeline: either the Timeline service queries your table by `ipd_admission_id`, or you push an event (e.g. via a shared timeline event store). Current implementation: Timeline service aggregates from known tables (admission, nursing_notes, vital_sign_records, medication_administrations, admission_charges, and when present doctor_orders, pharmacy, lab).
