/**
 * HMS Central Menu Configuration
 * Centralized role-based sidebar. Each menu has label, route, icon, allowedRoles, children.
 * Visibility: filter by user role; ADMIN sees all; SUPER_ADMIN sees System Configuration.
 * Optional `moduleCode` matches system_modules.code — when /permissions/me returns matrix data,
 * items are further filtered by VIEW + non-HIDDEN (see Sidebar + PermissionsContext).
 * Existing routes preserved – only structure and visibility are controlled.
 */

/** Canonical roles for RBAC (spec). Legacy roles mapped in menuFilter for backward compatibility. */
export type HMSRole =
  | 'ADMIN'
  | 'SUPER_ADMIN'
  | 'FRONT_DESK'
  | 'DOCTOR'
  | 'NURSE'
  | 'PHARMACIST'
  | 'LAB_TECH'
  | 'RADIOLOGY_TECH'
  | 'BILLING'
  | 'HOUSEKEEPING'
  | 'HR_MANAGER'
  | 'HR'
  | 'HELP_DESK'
  | 'MEDICAL_SUPERINTENDENT'
  // Legacy – kept for auth/backend; map to canonical in filterMenuByRole
  | 'RECEPTIONIST'
  | 'LAB_TECHNICIAN'
  | 'LAB_SUPERVISOR'
  | 'PATHOLOGIST'
  | 'QUALITY_MANAGER'
  | 'PHLEBOTOMIST'
  | 'PHARMACY_MANAGER'
  | 'STORE_INCHARGE'
  | 'IPD_PHARMACIST'
  | 'IPD_MANAGER'
  | 'IT_ADMIN'
  | 'BLOOD_BANK_TECH'
  | 'LAUNDRY'
  | 'KITCHEN'

export type MenuIconKey =
  | 'LayoutDashboard'
  | 'UserPlus'
  | 'Search'
  | 'ClipboardList'
  | 'Calendar'
  | 'Bed'
  | 'Stethoscope'
  | 'Heart'
  | 'Pill'
  | 'FlaskConical'
  | 'ScanLine'
  | 'DollarSign'
  | 'Shield'
  | 'Settings'
  | 'Users'
  | 'FileText'
  | 'BarChart'
  | 'Wrench'
  | 'Trash2'
  | 'Utensils'
  | 'Monitor'
  | 'Package'
  | 'Droplets'
  | 'Megaphone'
  | 'Queue'
  | 'Scissors'
  | 'Truck'
  | 'HelpCircle'
  | 'CreditCard'
  | 'RefreshCw'
  | 'Briefcase'
  | 'Scale'
  | 'AlertCircle'
  | 'Zap'
  | 'Lock'
  | 'BookOpen'
  | 'AlertTriangle'
  | 'Layers'
  | 'GitBranch'
  | 'Activity'
  | 'Building2'
  | 'Ambulance'
  | 'ShoppingCart'
  | 'Sparkles'

export interface SidebarMenuItem {
  id: string
  label: string
  route?: string
  end?: boolean
  icon: MenuIconKey
  allowedRoles: HMSRole[]
  /** Aligns with Admin → Modules / Permission Matrix (`system_modules.code`). Omit for role-only items (e.g. Dashboard). */
  moduleCode?: string
  badge?: number | string
  children?: SidebarMenuItem[]
}

export interface SidebarMenuGroup {
  id: string
  label: string
  groupIcon?: MenuIconKey
  allowedRoles: HMSRole[]
  items: SidebarMenuItem[]
}

/** Central menu config: Dashboard → Front Office → Patient Flow → Pharmacy → Lab → Radiology → Billing → Housekeeping → HR → System Config */
export const SIDEBAR_MENU_GROUPS: SidebarMenuGroup[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    groupIcon: 'LayoutDashboard',
    allowedRoles: ['ADMIN', 'SUPER_ADMIN', 'FRONT_DESK', 'DOCTOR', 'NURSE', 'PHARMACIST', 'LAB_TECH', 'RADIOLOGY_TECH', 'BILLING', 'HOUSEKEEPING', 'HR_MANAGER'],
    items: [
      { id: 'dash-main', label: 'Dashboard', route: '/dashboard', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'SUPER_ADMIN', 'FRONT_DESK', 'DOCTOR', 'NURSE', 'PHARMACIST', 'LAB_TECH', 'RADIOLOGY_TECH', 'BILLING', 'HOUSEKEEPING', 'HR_MANAGER'] },
    ],
  },
  {
    id: 'front-office',
    label: 'Reception',
    groupIcon: 'Users',
    allowedRoles: ['ADMIN', 'FRONT_DESK'],
    items: [
      { id: 'fo-register', label: 'Patient Registration', route: '/front-office/register', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
      { id: 'fo-walkin', label: 'Walk-in Registration', route: '/front-office/walkin', end: false, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK'], children: [
        { id: 'fo-walkin-dash', label: 'Walk-in Dashboard', route: '/front-office/walkin', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
        { id: 'fo-walkin-register', label: 'Register Walk-in', route: '/front-office/walkin/register', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
      ]},
      { id: 'fo-appointments', label: 'Appointments', route: '/front-office/appointments', end: false, icon: 'Calendar', allowedRoles: ['ADMIN', 'FRONT_DESK'], children: [
        { id: 'fo-appointments-dash', label: 'Appointment Dashboard', route: '/front-office/appointments', end: true, icon: 'Calendar', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
        { id: 'fo-appointments-book', label: 'Book Appointment', route: '/front-office/appointments/book', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
        { id: 'fo-appointments-queue', label: 'Appointment Queue', route: '/front-office/appointments/queue', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
        { id: 'fo-appointments-search', label: 'Search Appointments', route: '/front-office/appointments/search', end: true, icon: 'Search', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
      ]},
      { id: 'fo-tokens', label: 'Token Management', route: '/front-office/tokens', end: false, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK'], children: [
        { id: 'fo-tokens-dash', label: 'Token Dashboard', route: '/front-office/tokens', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
        { id: 'fo-tokens-queue', label: 'Token Queue', route: '/front-office/tokens/queue', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
      ]},
      { id: 'fo-enquiry', label: 'Enquiry Desk', route: '/front-office/enquiry', end: true, icon: 'HelpCircle', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
      { id: 'fo-visitors', label: 'Visitor Management', route: '/front-office/visitors', end: true, icon: 'Users', allowedRoles: ['ADMIN', 'FRONT_DESK'], moduleCode: 'RECEPTION' },
    ],
  },
  {
    id: 'patient-flow',
    label: 'Patient Flow',
    groupIcon: 'GitBranch',
    allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'],
    items: [
      { id: 'pf-opd', label: 'OPD Visits', route: '/patient-flow/opd', end: true, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'OPD' },
      { id: 'pf-token-panel', label: 'Doctor Token Panel', route: '/opd/tokens', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'OPD' },
      { id: 'pf-consultation', label: 'Doctor Consultation', route: '/patient-flow/consultation', end: true, icon: 'Stethoscope', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'OPD' },
      {
        id: 'pf-doctor-orders',
        label: 'Doctor Orders',
        icon: 'ClipboardList',
        allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'],
        children: [
          { id: 'pf-lab-orders', label: 'Lab Orders', route: '/patient-flow/lab-orders', end: true, icon: 'FlaskConical', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'LAB' },
          { id: 'pf-radiology-orders', label: 'Radiology Orders', route: '/patient-flow/radiology-orders', end: true, icon: 'ScanLine', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'RADIOLOGY' },
          { id: 'pf-pharmacy-orders', label: 'Pharmacy Orders', route: '/patient-flow/pharmacy-orders', end: true, icon: 'Pill', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'PHARMACY' },
        ],
      },
      { id: 'pf-admission', label: 'Admission (IPD)', route: '/patient-flow/admission', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'IPD' },
      { id: 'pf-bed-allocation', label: 'Bed Allocation', route: '/patient-flow/bed-allocation', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'IPD' },
      { id: 'pf-treatment', label: 'Treatment Management', route: '/patient-flow/treatment', end: true, icon: 'Heart', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'NURSING' },
      { id: 'pf-transfer', label: 'Transfer / Bed Change', route: '/ipd/beds', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'IPD' },
      { id: 'pf-discharge', label: 'Discharge Management', route: '/patient-flow/discharge', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'], moduleCode: 'IPD' },
    ],
  },
  {
    id: 'pharmacy',
    label: 'Pharmacy',
    groupIcon: 'Pill',
    allowedRoles: ['ADMIN', 'PHARMACIST'],
    items: [
      { id: 'pharm-dash', label: 'Pharmacy Dashboard', route: '/pharmacy', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'PHARMACIST'], moduleCode: 'PHARMACY' },
      { id: 'pharm-queue', label: 'IPD Medicine Issue Queue', route: '/pharmacy?tab=ISSUE_QUEUE', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'PHARMACIST'], moduleCode: 'PHARMACY' },
      { id: 'pharm-fefo', label: 'FEFO Stock View', route: '/pharmacy?tab=FEFO_STOCK', end: true, icon: 'Package', allowedRoles: ['ADMIN', 'PHARMACIST'], moduleCode: 'PHARMACY' },
      { id: 'pharm-alerts', label: 'Expiry Alerts', route: '/pharmacy?tab=ALERTS', end: true, icon: 'AlertTriangle', allowedRoles: ['ADMIN', 'PHARMACIST'], moduleCode: 'PHARMACY' },
      { id: 'pharm-master', label: 'Medicine Master', route: '/pharmacy?tab=MEDICINE_LIST', end: true, icon: 'Pill', allowedRoles: ['ADMIN', 'PHARMACIST'], moduleCode: 'PHARMACY' },
      { id: 'pharm-racks', label: 'Rack Management', route: '/pharmacy?tab=RACK_MANAGEMENT', end: true, icon: 'Layers', allowedRoles: ['ADMIN', 'PHARMACIST'], moduleCode: 'PHARMACY' },
      { id: 'pharm-transactions', label: 'Stock Transactions', route: '/pharmacy?tab=STOCK_TRANSACTIONS', end: true, icon: 'RefreshCw', allowedRoles: ['ADMIN', 'PHARMACIST'], moduleCode: 'PHARMACY' },
    ],
  },
  {
    id: 'laboratory',
    label: 'Laboratory',
    groupIcon: 'FlaskConical',
    allowedRoles: ['ADMIN', 'LAB_TECH'],
    items: [
      { id: 'lab-dash', label: 'Lab Dashboard', route: '/lab', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'LAB_TECH'], moduleCode: 'LAB' },
      { id: 'lab-collection', label: 'Sample Collection', route: '/lab/collection', end: true, icon: 'Package', allowedRoles: ['ADMIN', 'LAB_TECH'], moduleCode: 'LAB' },
      { id: 'lab-processing', label: 'Sample Processing', route: '/lab/sample-processing', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'LAB_TECH'], moduleCode: 'LAB' },
      { id: 'lab-result-entry', label: 'Result Entry', route: '/lab/result-entry', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'LAB_TECH'], moduleCode: 'LAB' },
      { id: 'lab-verification', label: 'Result Verification', route: '/lab/result-verification', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'LAB_TECH'], moduleCode: 'LAB' },
      { id: 'lab-reports', label: 'View Reports', route: '/lab/view-reports', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'LAB_TECH'], moduleCode: 'LAB' },
      { id: 'lab-test-master', label: 'Test Master', route: '/lab/test-master', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'LAB_TECH'], moduleCode: 'LAB' },
    ],
  },
  {
    id: 'radiology',
    label: 'Radiology',
    groupIcon: 'ScanLine',
    allowedRoles: ['ADMIN', 'RADIOLOGY_TECH'],
    items: [
      { id: 'rad-dash', label: 'Radiology Dashboard', route: '/radiology', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'RADIOLOGY_TECH'], moduleCode: 'RADIOLOGY' },
      { id: 'rad-orders', label: 'Scan Orders', route: '/radiology/requests', end: true, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'RADIOLOGY_TECH'], moduleCode: 'RADIOLOGY' },
      { id: 'rad-upload', label: 'Image Upload', route: '/radiology/reports', end: true, icon: 'ScanLine', allowedRoles: ['ADMIN', 'RADIOLOGY_TECH'], moduleCode: 'RADIOLOGY' },
      { id: 'rad-reporting', label: 'Reporting', route: '/radiology/reports', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'RADIOLOGY_TECH'], moduleCode: 'RADIOLOGY' },
      { id: 'rad-view', label: 'View Reports', route: '/radiology/view', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'RADIOLOGY_TECH'], moduleCode: 'RADIOLOGY' },
    ],
  },
  {
    id: 'billing',
    label: 'Billing',
    groupIcon: 'DollarSign',
    allowedRoles: ['ADMIN', 'BILLING'],
    items: [
      { id: 'bill-dash', label: 'Billing Dashboard', route: '/billing', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'BILLING'], moduleCode: 'BILLING' },
      { id: 'bill-ipd', label: 'IPD Billing', route: '/billing/ipd', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'BILLING'], moduleCode: 'BILLING' },
      { id: 'bill-opd', label: 'OPD Billing', route: '/billing/opd/group', end: true, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'BILLING'], moduleCode: 'BILLING' },
      { id: 'bill-corporate', label: 'Corporate Billing', route: '/billing/corporate', end: true, icon: 'Briefcase', allowedRoles: ['ADMIN', 'BILLING'], moduleCode: 'BILLING' },
      { id: 'bill-tpa', label: 'TPA Insurance', route: '/billing/tpa', end: true, icon: 'Shield', allowedRoles: ['ADMIN', 'BILLING'], moduleCode: 'BILLING' },
      { id: 'bill-payments', label: 'Payments', route: '/billing/payments', end: true, icon: 'CreditCard', allowedRoles: ['ADMIN', 'BILLING'], moduleCode: 'BILLING' },
      { id: 'bill-refunds', label: 'Refunds', route: '/billing/refunds', end: true, icon: 'RefreshCw', allowedRoles: ['ADMIN', 'BILLING'], moduleCode: 'BILLING' },
    ],
  },
  {
    id: 'housekeeping-services',
    label: 'Housekeeping & Services',
    groupIcon: 'Trash2',
    allowedRoles: ['ADMIN', 'HOUSEKEEPING'],
    items: [
      { id: 'hk-tasks', label: 'Housekeeping Tasks', route: '/housekeeping', end: true, icon: 'Trash2', allowedRoles: ['ADMIN', 'HOUSEKEEPING'], moduleCode: 'HOUSEKEEPING' },
      { id: 'hk-laundry', label: 'Laundry & Linen', route: '/laundry', end: true, icon: 'Package', allowedRoles: ['ADMIN', 'HOUSEKEEPING'], moduleCode: 'HOUSEKEEPING' },
      { id: 'hk-dietary', label: 'Dietary / Kitchen', route: '/dietary', end: true, icon: 'Utensils', allowedRoles: ['ADMIN', 'HOUSEKEEPING'], moduleCode: 'HOUSEKEEPING' },
      { id: 'hk-meals', label: 'Patient Meals', route: '/meals', end: true, icon: 'Utensils', allowedRoles: ['ADMIN', 'HOUSEKEEPING'], moduleCode: 'HOUSEKEEPING' },
    ],
  },
  {
    id: 'hr-management',
    label: 'HR Management',
    groupIcon: 'Users',
    allowedRoles: ['ADMIN', 'HR_MANAGER'],
    items: [
      { id: 'hr-employees', label: 'Employees', route: '/hr', end: true, icon: 'Users', allowedRoles: ['ADMIN', 'HR_MANAGER'], moduleCode: 'HR' },
      { id: 'hr-attendance', label: 'Attendance', route: '/hr/attendance', end: true, icon: 'Calendar', allowedRoles: ['ADMIN', 'HR_MANAGER'], moduleCode: 'HR' },
      { id: 'hr-shifts', label: 'Shift Management', route: '/hr/shifts', end: true, icon: 'Activity', allowedRoles: ['ADMIN', 'HR_MANAGER'], moduleCode: 'HR' },
      { id: 'hr-payroll', label: 'Payroll', route: '/hr/payroll', end: true, icon: 'DollarSign', allowedRoles: ['ADMIN', 'HR_MANAGER'], moduleCode: 'HR' },
    ],
  },
  {
    id: 'system-config',
    label: 'System Configuration',
    groupIcon: 'Settings',
    allowedRoles: ['ADMIN', 'SUPER_ADMIN'],
    items: [
      { id: 'sys-users', label: 'User Management', route: '/admin/users', end: true, icon: 'Users', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'SYSTEM_CONFIG' },
      { id: 'sys-roles', label: 'Roles & Permissions', route: '/admin/config/roles', end: true, icon: 'Shield', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'SYSTEM_CONFIG' },
      { id: 'sys-depts', label: 'Departments', route: '/admin/config/modules', end: true, icon: 'Building2', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'SYSTEM_CONFIG' },
      { id: 'sys-doctors', label: 'Doctor Setup', route: '/doctors', end: true, icon: 'Stethoscope', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'DOCTORS' },
      { id: 'sys-wards', label: 'Ward Setup', route: '/wards/general', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'WARDS' },
      { id: 'sys-beds', label: 'Bed Setup', route: '/ipd/beds', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'WARDS' },
      { id: 'sys-settings', label: 'Hospital Settings', route: '/admin/config/company-profile', end: true, icon: 'Settings', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'SYSTEM_CONFIG' },
      { id: 'sys-audit', label: 'Audit Logs', route: '/admin/audit', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'SUPER_ADMIN'], moduleCode: 'SYSTEM_CONFIG' },
    ],
  },
]
