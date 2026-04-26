# Hospital Admin — Roles & Responsibilities
### Mapped to Existing Module Menus

---

## Admin Overview

The **Hospital Admin** is the highest-authority user within a single hospital instance.  
They have full control over all modules listed below and are responsible for:

- Configuring and maintaining the hospital's operational setup
- Managing staff accounts, roles, and permissions
- Monitoring daily operations across all departments
- Ensuring data accuracy, compliance, and security within the hospital
- Reporting operational performance to the Super Admin (platform level)

> ⚠️ Hospital Admin **cannot** access any other hospital's data.  
> ⚠️ Hospital Admin **cannot** modify platform-level settings (managed by Super Admin).

---

## Permission Level Legend

| Symbol | Meaning |
|---|---|
| ✅ Full Access | View + Create + Edit + Delete |
| 🔧 Manage | View + Create + Edit (no delete) |
| 👁️ View Only | Read-only access |
| ❌ No Access | Not accessible |

---

## 1. 📊 Dashboard

**Purpose:** Central overview of the hospital's real-time operational status.

### Admin Responsibilities
- View real-time KPIs: total patients today, appointments, bed occupancy, revenue
- Monitor department-wise activity at a glance
- Track pending tasks and alerts (e.g. unpaid bills, low pharmacy stock, lab results pending)
- Access quick shortcuts to all modules

### Admin Access
| Widget / Feature | Access |
|---|---|
| Today's appointment summary | ✅ Full Access |
| Patient census (OPD/IPD count) | ✅ Full Access |
| Revenue snapshot | ✅ Full Access |
| Department-wise activity | ✅ Full Access |
| Pending alerts & notifications | ✅ Full Access |
| Staff on-duty overview | ✅ Full Access |
| Custom dashboard widgets | ✅ Full Access |

---

## 2. 🩺 Doctor Workspace

**Purpose:** Manage doctor profiles, schedules, consultations, and clinical notes.

### Admin Responsibilities
- Add, edit, and deactivate doctor profiles
- Assign doctors to departments and specialties
- Configure doctor consultation fees and availability slots
- Monitor doctor-wise appointment load and performance
- Manage doctor leave and schedule overrides
- Grant or revoke doctors' access to patient records

### Admin Access
| Feature | Access |
|---|---|
| Add / edit doctor profiles | ✅ Full Access |
| Assign specialties & departments | ✅ Full Access |
| Set consultation fees | ✅ Full Access |
| View / manage doctor schedules | ✅ Full Access |
| Approve doctor leave requests | ✅ Full Access |
| View clinical notes & prescriptions | 👁️ View Only |
| Doctor performance reports | ✅ Full Access |

---

## 3. 🏥 Reception

**Purpose:** Front-desk operations — patient registration, appointments, token management, and visitor handling.

### Admin Responsibilities
- Configure reception workflows and appointment booking rules
- Manage patient registration templates and required fields
- Monitor front-desk staff activity and workload
- Set up appointment slots, time intervals, and booking policies
- Review and resolve patient complaints or escalations logged at reception
- Manage walk-in vs. online appointment rules

### Admin Access
| Feature | Access |
|---|---|
| Patient registration config | ✅ Full Access |
| Appointment slot configuration | ✅ Full Access |
| Token / queue management setup | ✅ Full Access |
| View all appointments | ✅ Full Access |
| Override / cancel appointments | ✅ Full Access |
| Reception staff management | ✅ Full Access |
| Visitor management settings | ✅ Full Access |
| Appointment reports | ✅ Full Access |

---

## 4. 🔄 Patient Flow

**Purpose:** Track patient journey from admission to discharge — OPD, IPD, transfers, and bed assignments.

### Admin Responsibilities
- Configure OPD and IPD workflows
- Manage ward types, bed categories, and room configurations
- Monitor real-time bed occupancy and availability
- Oversee patient admissions, transfers between wards, and discharges
- Set up discharge checklists and clearance processes
- Configure patient flow escalation rules (e.g. ICU transfers)

### Admin Access
| Feature | Access |
|---|---|
| Ward & bed configuration | ✅ Full Access |
| OPD flow setup | ✅ Full Access |
| IPD admission management | ✅ Full Access |
| Patient transfer management | ✅ Full Access |
| Discharge process configuration | ✅ Full Access |
| Real-time bed occupancy view | ✅ Full Access |
| Patient flow reports | ✅ Full Access |

---

## 5. 💊 Pharmacy

**Purpose:** Manage medicine inventory, prescriptions, dispensing, and supplier relationships.

### Admin Responsibilities
- Configure medicine catalog (categories, generic names, brands)
- Set stock thresholds for low-stock alerts
- Manage suppliers and purchase orders
- Monitor medicine expiry dates and trigger disposal workflows
- Approve high-value or controlled-substance dispensing
- Configure pharmacy billing integration
- Review daily dispensing reports and stock movement

### Admin Access
| Feature | Access |
|---|---|
| Medicine catalog management | ✅ Full Access |
| Stock threshold configuration | ✅ Full Access |
| Supplier & purchase order management | ✅ Full Access |
| View prescription & dispensing logs | ✅ Full Access |
| Approve controlled substance requests | ✅ Full Access |
| Expiry management | ✅ Full Access |
| Pharmacy billing settings | ✅ Full Access |
| Inventory & stock reports | ✅ Full Access |

---

## 6. 🧪 Laboratory

**Purpose:** Manage diagnostic test catalog, sample collection, result processing, and report delivery.

### Admin Responsibilities
- Configure test catalog (test names, codes, reference ranges, pricing)
- Manage lab staff and technician assignments
- Set up sample collection workflows and turnaround time (TAT) standards
- Monitor pending tests and overdue results
- Configure result delivery (patient portal, email, SMS)
- Manage external lab integrations if tests are outsourced
- Review lab revenue and test volume reports

### Admin Access
| Feature | Access |
|---|---|
| Test catalog configuration | ✅ Full Access |
| Lab staff & technician management | ✅ Full Access |
| Sample collection workflow setup | ✅ Full Access |
| Monitor pending / overdue tests | ✅ Full Access |
| Result approval before delivery | ✅ Full Access |
| External lab integration setup | ✅ Full Access |
| Result delivery configuration | ✅ Full Access |
| Lab revenue & volume reports | ✅ Full Access |

---

## 7. 🩻 Radiology

**Purpose:** Manage imaging services — X-ray, MRI, CT scan, Ultrasound, and report management.

### Admin Responsibilities
- Configure imaging modalities and equipment catalog
- Manage radiologist profiles and report assignment
- Set imaging service pricing and billing integration
- Monitor scan request queue and turnaround times
- Configure PACS/DICOM integration (if applicable)
- Manage image storage and archive policies
- Oversee report delivery to referring doctors and patients

### Admin Access
| Feature | Access |
|---|---|
| Imaging modality configuration | ✅ Full Access |
| Radiologist profile management | ✅ Full Access |
| Scan pricing & billing setup | ✅ Full Access |
| Monitor scan request queue | ✅ Full Access |
| Report assignment & delivery config | ✅ Full Access |
| PACS / DICOM integration settings | ✅ Full Access |
| Image archive policy management | ✅ Full Access |
| Radiology revenue reports | ✅ Full Access |

---

## 8. 💰 Billing

**Purpose:** Manage all financial transactions — OPD/IPD billing, insurance claims, payments, and invoicing.

### Admin Responsibilities
- Configure billing rules (service charges, consultation fees, room charges)
- Set up insurance providers and claim submission workflows
- Monitor outstanding payments and overdue invoices
- Approve billing discounts and waivers (with policy limits)
- Configure tax settings and invoice templates
- Oversee daily cash collection and payment reconciliation
- Generate and review financial reports (daily, weekly, monthly)
- Manage refund requests and approval workflows

### Admin Access
| Feature | Access |
|---|---|
| Billing rules & charge configuration | ✅ Full Access |
| Insurance provider management | ✅ Full Access |
| Invoice generation & management | ✅ Full Access |
| Discount / waiver approval | ✅ Full Access |
| Tax & invoice template setup | ✅ Full Access |
| Payment reconciliation | ✅ Full Access |
| Refund request approval | ✅ Full Access |
| Revenue & financial reports | ✅ Full Access |

---

## 9. 🧹 Housekeeping & Services

**Purpose:** Manage hospital cleanliness, maintenance requests, laundry, and ancillary services.

### Admin Responsibilities
- Configure housekeeping zones (wards, OT, corridors, etc.)
- Assign housekeeping staff to zones and shifts
- Manage service request categories (cleaning, maintenance, repair)
- Monitor open service requests and resolution times
- Set service SLA standards and escalation rules
- Manage laundry and linen inventory tracking
- Review housekeeping staff performance reports

### Admin Access
| Feature | Access |
|---|---|
| Zone & area configuration | ✅ Full Access |
| Staff assignment & shift management | ✅ Full Access |
| Service request category setup | ✅ Full Access |
| Monitor open requests & SLA | ✅ Full Access |
| Escalation rule configuration | ✅ Full Access |
| Linen & laundry management | ✅ Full Access |
| Housekeeping performance reports | ✅ Full Access |

---

## 10. 👥 HR Management

**Purpose:** Manage all hospital staff — hiring, attendance, leave, payroll, and performance.

### Admin Responsibilities
- Add, edit, and deactivate all staff accounts (doctors, nurses, technicians, admin staff, etc.)
- Define roles and permission sets for each staff category
- Manage staff onboarding and document verification
- Configure attendance tracking (biometric / manual)
- Manage leave policies and approve/reject leave requests
- Configure payroll rules, salary structures, and deductions
- Track staff performance and generate appraisal reports
- Manage staff training schedules and certifications

### Admin Access
| Feature | Access |
|---|---|
| Staff account creation & management | ✅ Full Access |
| Role & permission configuration | ✅ Full Access |
| Onboarding workflow setup | ✅ Full Access |
| Attendance configuration & tracking | ✅ Full Access |
| Leave policy setup & approvals | ✅ Full Access |
| Payroll configuration | ✅ Full Access |
| Performance & appraisal management | ✅ Full Access |
| Training & certification tracking | ✅ Full Access |

---

## 11. ⚙️ Hospital Configuration

**Purpose:** Core hospital settings — branding, system preferences, integrations, and module toggles.

### Admin Responsibilities
- Update hospital profile details (name, logo, address, contact info)
- Manage social media links and public-facing information
- Configure notification templates (email, SMS, WhatsApp)
- Enable or disable modules based on hospital subscription plan
- Set up integrations (payment gateway, SMS provider, lab equipment, etc.)
- Manage audit logs and access history
- Configure data backup and retention policies
- Set hospital-wide operational settings (OPD hours, holidays, working days)
- Manage subscription and plan details (view only — changes via Super Admin)

### Admin Access
| Feature | Access |
|---|---|
| Hospital profile & branding | ✅ Full Access |
| Social media & contact config | ✅ Full Access |
| Notification template management | ✅ Full Access |
| Module enable / disable | ✅ Full Access |
| Integration settings | ✅ Full Access |
| Audit logs & access history | 👁️ View Only |
| Backup & retention settings | ✅ Full Access |
| OPD hours & holiday calendar | ✅ Full Access |
| Subscription / plan details | 👁️ View Only |

---

## Summary — Admin Access Across All Modules

| Module | Create | Read | Update | Delete |
|---|:---:|:---:|:---:|:---:|
| Dashboard | — | ✅ | ✅ | — |
| Doctor Workspace | ✅ | ✅ | ✅ | ✅ |
| Reception | ✅ | ✅ | ✅ | ✅ |
| Patient Flow | ✅ | ✅ | ✅ | ✅ |
| Pharmacy | ✅ | ✅ | ✅ | ✅ |
| Laboratory | ✅ | ✅ | ✅ | ✅ |
| Radiology | ✅ | ✅ | ✅ | ✅ |
| Billing | ✅ | ✅ | ✅ | 🔧 (restricted) |
| Housekeeping & Services | ✅ | ✅ | ✅ | ✅ |
| HR Management | ✅ | ✅ | ✅ | ✅ |
| Hospital Configuration | ✅ | ✅ | ✅ | 🔧 (restricted) |

> 🔧 **Restricted delete** means Admin can archive or deactivate but not permanently delete (e.g. billing records, configuration history) — to maintain audit compliance.

---

## What Admin Cannot Do

| Restricted Action | Who Can Do It |
|---|---|
| Create a new hospital | Super Admin only |
| Delete this hospital | Super Admin only |
| Change subscription plan | Super Admin only |
| View other hospitals' data | Super Admin only |
| Modify platform-level settings | Super Admin only |
| Access Super Admin panel | Super Admin only |

---

## Recommended Sub-roles Under Admin

As the system grows, the Admin may delegate to these sub-roles:

| Sub-role | Manages |
|---|---|
| **Reception Manager** | Reception + Patient Flow |
| **Pharmacy Manager** | Pharmacy module only |
| **Lab Manager** | Laboratory module only |
| **Radiology Manager** | Radiology module only |
| **Billing Manager** | Billing module only |
| **HR Manager** | HR Management only |
| **Housekeeping Supervisor** | Housekeeping & Services only |

Each sub-role has access **only to their assigned module** and cannot touch Hospital Configuration or HR beyond their own profile.

---

*Document version 1.0 — Hospital Admin Role Plan*