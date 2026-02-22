/**
 * Dev tools quick navigation. All app sections for dev environment.
 * Only visible when import.meta.env.DEV is true.
 */

export interface DevRoute {
  label: string
  path: string
  group: string
}

export const DEV_TOOLS_ROUTES: DevRoute[] = [
  // Home
  { label: 'Dashboard', path: '/', group: 'Home' },

  // Front Office
  { label: 'Reception', path: '/reception', group: 'Front Office' },
  { label: 'Patient Register', path: '/reception/register', group: 'Front Office' },
  { label: 'Patient Search', path: '/reception/search', group: 'Front Office' },
  { label: 'Appointments', path: '/appointments', group: 'Front Office' },
  { label: 'Emergency', path: '/emergency', group: 'Front Office' },
  { label: 'Helpdesk', path: '/helpdesk', group: 'Front Office' },
  { label: 'Ambulance', path: '/ambulance', group: 'Front Office' },

  // OPD
  { label: 'OPD', path: '/opd', group: 'OPD' },
  { label: 'Register Visit', path: '/opd/register', group: 'OPD' },
  { label: 'OPD Queue', path: '/opd/queue', group: 'OPD' },
  { label: 'Search Visits', path: '/opd/visits', group: 'OPD' },

  // IPD
  { label: 'IPD', path: '/ipd', group: 'IPD' },
  { label: 'Admission Mgmt', path: '/ipd/admission-management', group: 'IPD' },
  { label: 'Admit Patient', path: '/ipd/admit', group: 'IPD' },
  { label: 'Bed Availability', path: '/ipd/beds', group: 'IPD' },
  { label: 'Hospital Beds', path: '/ipd/hospital-beds', group: 'IPD' },
  { label: 'IPD Admissions', path: '/ipd/admissions', group: 'IPD' },

  // Billing
  { label: 'Billing', path: '/billing', group: 'Billing' },
  { label: 'IPD Billing', path: '/billing/ipd', group: 'Billing' },
  { label: 'Billing Account', path: '/billing/account/1', group: 'Billing' },
  { label: 'Corporate Billing', path: '/billing/corporate', group: 'Billing' },
  { label: 'EMI Billing', path: '/billing/emi', group: 'Billing' },
  { label: 'Online Payment', path: '/billing/payment/online', group: 'Billing' },
  { label: 'OPD Group Bill', path: '/billing/opd/group', group: 'Billing' },
  { label: 'Insurance / TPA', path: '/billing/tpa', group: 'Billing' },
  { label: 'Payments', path: '/billing/payments', group: 'Billing' },
  { label: 'Refunds', path: '/billing/refunds', group: 'Billing' },

  // Diagnostics
  { label: 'Pharmacy', path: '/pharmacy', group: 'Diagnostics' },
  { label: 'Lab', path: '/lab', group: 'Diagnostics' },
  { label: 'Lab Reports', path: '/lab/reports', group: 'Diagnostics' },
  { label: 'Radiology', path: '/radiology', group: 'Diagnostics' },
  { label: 'Blood Bank', path: '/bloodbank', group: 'Diagnostics' },

  // Clinical
  { label: 'Nursing', path: '/nursing', group: 'Clinical' },
  { label: 'Nursing Staff', path: '/nursing/staff', group: 'Clinical' },
  { label: 'Assign Nurse', path: '/nursing/assign', group: 'Clinical' },
  { label: 'Vitals', path: '/nursing/vitals', group: 'Clinical' },
  { label: 'MAR', path: '/nursing/medications', group: 'Clinical' },
  { label: 'Nursing Notes', path: '/nursing/notes', group: 'Clinical' },
  { label: 'Doctors', path: '/doctors', group: 'Clinical' },
  { label: 'Add Doctor', path: '/doctors/new', group: 'Clinical' },
  { label: 'General Ward', path: '/wards/general', group: 'Clinical' },
  { label: 'Private Ward', path: '/wards/private', group: 'Clinical' },
  { label: 'ICU / CCU / NICU', path: '/wards/icu', group: 'Clinical' },
  { label: 'OT', path: '/ot', group: 'Clinical' },
  { label: 'Anesthesia', path: '/anesthesia', group: 'Clinical' },
  { label: 'Physiotherapy', path: '/physiotherapy', group: 'Clinical' },
  { label: 'Dialysis', path: '/dialysis', group: 'Clinical' },

  // Administration
  { label: 'HR', path: '/hr', group: 'Administration' },
  { label: 'MRD', path: '/mrd', group: 'Administration' },
  { label: 'Quality & NABH', path: '/quality', group: 'Administration' },
  { label: 'Legal', path: '/legal', group: 'Administration' },
  { label: 'Reports', path: '/reports', group: 'Administration' },

  // Operations & Maintenance
  { label: 'Biomed', path: '/biomed', group: 'Operations' },
  { label: 'Electrical & Maintenance', path: '/maintenance', group: 'Operations' },
  { label: 'Fire & Safety', path: '/fire-safety', group: 'Operations' },
  { label: 'Security', path: '/security', group: 'Operations' },
  { label: 'Biowaste', path: '/biowaste', group: 'Operations' },
  { label: 'CSSD', path: '/cssd', group: 'Operations' },

  // Housekeeping
  { label: 'Housekeeping', path: '/housekeeping', group: 'Housekeeping' },
  { label: 'Laundry', path: '/laundry', group: 'Housekeeping' },
  { label: 'Dietary', path: '/dietary', group: 'Housekeeping' },
  { label: 'Patient Meals', path: '/patient-meals', group: 'Housekeeping' },

  // IT & Systems
  { label: 'IT Department', path: '/admin', group: 'IT & Systems' },
  { label: 'User & Role Mgmt', path: '/admin/users', group: 'IT & Systems' },
  { label: 'System Config', path: '/admin/config', group: 'IT & Systems' },
  { label: 'Audit Logs', path: '/admin/audit', group: 'IT & Systems' },
  { label: 'Telemedicine', path: '/telemedicine', group: 'IT & Systems' },
  { label: 'Research', path: '/research', group: 'IT & Systems' },
  { label: 'Roles', path: '/admin/config/roles', group: 'IT & Systems' },
  { label: 'Modules', path: '/admin/config/modules', group: 'IT & Systems' },
  { label: 'Permissions', path: '/admin/config/permissions', group: 'IT & Systems' },
  { label: 'Features', path: '/admin/config/features', group: 'IT & Systems' },

  // Inventory
  { label: 'Store & Purchase', path: '/purchase', group: 'Inventory' },
  { label: 'Inventory', path: '/inventory', group: 'Inventory' },
  { label: 'Vendors', path: '/vendors', group: 'Inventory' },
  { label: 'Stock Alerts', path: '/stock-alerts', group: 'Inventory' },

  // Marketing
  { label: 'Marketing / PR', path: '/marketing', group: 'Marketing' },
  { label: 'Health Camps', path: '/health-camps', group: 'Marketing' },
]
