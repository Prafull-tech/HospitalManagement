/**
 * HMS Sidebar Menu Configuration
 * Scalable, data-driven navigation with role-based visibility.
 * Icon keys map to components in the icon registry.
 */

export type MenuIconKey =
  | 'LayoutDashboard'
  | 'UserPlus'
  | 'Search'
  | 'ClipboardList'
  | 'Calendar'
  | 'Bed'
  | 'Ambulance'
  | 'Building2'
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
  | 'Megaphone'
  | 'Queue'
  | 'Scissors'
  | 'Activity'
  | 'Droplets'
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
  | 'Layers'
  | 'ShoppingCart'
  | 'AlertTriangle'
  | 'Sparkles'

export type HMSRole =
  | 'ADMIN'
  | 'SUPER_ADMIN'
  | 'HR'
  | 'MEDICAL_SUPERINTENDENT'
  | 'RECEPTIONIST'
  | 'DOCTOR'
  | 'NURSE'
  | 'LAB_TECH'
  | 'PHARMACIST'
  | 'BILLING'
  | 'IT_ADMIN'
  | 'HELP_DESK'

export interface SidebarMenuItem {
  id: string
  label: string
  route?: string
  end?: boolean
  icon: MenuIconKey
  allowedRoles: HMSRole[]
  children?: SidebarMenuItem[]
}

export interface SidebarMenuGroup {
  id: string
  label: string
  allowedRoles: HMSRole[]
  items: SidebarMenuItem[]
}

export const SIDEBAR_MENU_GROUPS: SidebarMenuGroup[] = [
  {
    id: 'front-office',
    label: 'Front Office & Patient Flow',
    allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE', 'HELP_DESK'],
    items: [
      { id: 'reception-dash', label: 'Reception / Front Desk', route: '/reception', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'HELP_DESK'] },
      { id: 'patient-reg', label: 'Patient Registration (UHID)', route: '/reception/register', end: false, icon: 'UserPlus', allowedRoles: ['ADMIN', 'RECEPTIONIST'] },
      { id: 'patient-search', label: 'Patient Search', route: '/reception/search', end: false, icon: 'Search', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE', 'HELP_DESK'] },
      { id: 'appointment', label: 'Appointment Scheduling', route: '/appointments', end: true, icon: 'Calendar', allowedRoles: ['ADMIN', 'RECEPTIONIST'] },
      { id: 'opd', label: 'OPD', route: '/opd', end: true, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
      { id: 'opd-register', label: 'Register OPD Visit', route: '/opd/register', end: false, icon: 'UserPlus', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
      { id: 'opd-queue', label: 'OPD Queue', route: '/opd/queue', end: false, icon: 'Queue', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
      { id: 'opd-visits', label: 'Search OPD Visits', route: '/opd/visits', end: true, icon: 'Search', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
      { id: 'ipd', label: 'IPD', route: '/ipd', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE'] },
      { id: 'ipd-admit', label: 'Admit Patient', route: '/ipd/admit', end: false, icon: 'UserPlus', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
      { id: 'ipd-beds', label: 'Bed Availability', route: '/ipd/beds', end: false, icon: 'Bed', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE'] },
      { id: 'ipd-hospital-beds', label: 'Hospital Bed Availability', route: '/ipd/hospital-beds', end: false, icon: 'Building2', allowedRoles: ['ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE', 'HELP_DESK'] },
      { id: 'ipd-admissions', label: 'IPD Admissions', route: '/ipd/admissions', end: true, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE'] },
      { id: 'emergency', label: 'Emergency / Casualty', route: '/emergency', end: true, icon: 'AlertCircle', allowedRoles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE'] },
      { id: 'helpdesk', label: 'Patient Relations / Helpdesk', route: '/helpdesk', end: true, icon: 'HelpCircle', allowedRoles: ['ADMIN', 'HELP_DESK'] },
      { id: 'ambulance', label: 'Ambulance / Transport', route: '/ambulance', end: true, icon: 'Ambulance', allowedRoles: ['ADMIN', 'RECEPTIONIST'] },
    ],
  },
  {
    id: 'clinical-care',
    label: 'Clinical Care Services',
    allowedRoles: ['ADMIN', 'HR', 'MEDICAL_SUPERINTENDENT', 'DOCTOR', 'NURSE'],
    items: [
      { id: 'doctors', label: 'Doctors / Medical Staff', route: '/doctors', end: true, icon: 'Stethoscope', allowedRoles: ['ADMIN', 'HR', 'MEDICAL_SUPERINTENDENT', 'DOCTOR', 'RECEPTIONIST'] },
      { id: 'doctors-new', label: 'Add Doctor', route: '/doctors/new', end: false, icon: 'UserPlus', allowedRoles: ['ADMIN', 'HR', 'MEDICAL_SUPERINTENDENT'] },
      { id: 'nursing', label: 'Nursing Department', route: '/nursing', end: true, icon: 'Heart', allowedRoles: ['ADMIN', 'NURSE'] },
      { id: 'nursing-staff', label: 'Nursing Staff', route: '/nursing/staff', end: false, icon: 'Users', allowedRoles: ['ADMIN', 'NURSE'] },
      { id: 'nursing-assign', label: 'Assign Nurse', route: '/nursing/assign', end: false, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'NURSE'] },
      { id: 'nursing-vitals', label: 'Vital Signs', route: '/nursing/vitals', end: false, icon: 'Activity', allowedRoles: ['ADMIN', 'NURSE'] },
      { id: 'nursing-mar', label: 'MAR', route: '/nursing/medications', end: false, icon: 'Pill', allowedRoles: ['ADMIN', 'NURSE'] },
      { id: 'nursing-notes', label: 'Nursing Notes', route: '/nursing/notes', end: false, icon: 'FileText', allowedRoles: ['ADMIN', 'NURSE'] },
      { id: 'nursing-notes-search', label: 'Search Nursing Notes', route: '/nursing/notes/search', end: false, icon: 'Search', allowedRoles: ['ADMIN', 'NURSE'] },
      {
        id: 'wards',
        label: 'Wards',
        icon: 'Bed',
        allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'],
        children: [
          { id: 'ward-general', label: 'General Ward', route: '/wards/general', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'] },
          { id: 'ward-private', label: 'Private Ward', route: '/wards/private', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'] },
          { id: 'ward-icu', label: 'ICU / CCU / NICU / HDU', route: '/wards/icu', end: true, icon: 'Heart', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'] },
        ],
      },
      { id: 'ot', label: 'Operation Theatre (OT)', route: '/ot', end: true, icon: 'Scissors', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'] },
      { id: 'anesthesia', label: 'Anesthesia Department', route: '/anesthesia', end: true, icon: 'Activity', allowedRoles: ['ADMIN', 'DOCTOR'] },
      { id: 'physio', label: 'Physiotherapy / Rehabilitation', route: '/physiotherapy', end: true, icon: 'Activity', allowedRoles: ['ADMIN', 'DOCTOR'] },
      { id: 'dialysis', label: 'Dialysis Unit', route: '/dialysis', end: true, icon: 'Droplets', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE'] },
    ],
  },
  {
    id: 'diagnostics-pharmacy',
    label: 'Diagnostics & Pharmacy',
    allowedRoles: ['ADMIN', 'LAB_TECH', 'PHARMACIST', 'DOCTOR'],
    items: [
      { id: 'pathology', label: 'Pathology / Laboratory', route: '/lab', end: true, icon: 'FlaskConical', allowedRoles: ['ADMIN', 'LAB_TECH', 'DOCTOR'] },
      { id: 'radiology', label: 'Radiology / Imaging', route: '/radiology', end: true, icon: 'ScanLine', allowedRoles: ['ADMIN', 'LAB_TECH', 'DOCTOR'] },
      { id: 'pharmacy', label: 'Pharmacy / Medical Store', route: '/pharmacy', end: true, icon: 'Pill', allowedRoles: ['ADMIN', 'PHARMACIST', 'DOCTOR'] },
      { id: 'blood-bank', label: 'Blood Bank', route: '/blood-bank', end: true, icon: 'Droplets', allowedRoles: ['ADMIN', 'LAB_TECH'] },
    ],
  },
  {
    id: 'billing-financials',
    label: 'Billing, Insurance & Financials',
    allowedRoles: ['ADMIN', 'BILLING'],
    items: [
      { id: 'billing', label: 'Billing & Accounts', route: '/billing', end: true, icon: 'DollarSign', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'insurance', label: 'Insurance / TPA', route: '/insurance', end: true, icon: 'Shield', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'payments', label: 'Payments', route: '/payments', end: true, icon: 'CreditCard', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'refunds', label: 'Refunds', route: '/refunds', end: true, icon: 'RefreshCw', allowedRoles: ['ADMIN', 'BILLING'] },
    ],
  },
  {
    id: 'administration',
    label: 'Administration & Compliance',
    allowedRoles: ['ADMIN'],
    items: [
      { id: 'hr', label: 'Human Resources (HR)', route: '/hr', end: true, icon: 'Users', allowedRoles: ['ADMIN'] },
      { id: 'mrd', label: 'Medical Records Department (MRD)', route: '/mrd', end: true, icon: 'FileText', allowedRoles: ['ADMIN'] },
      { id: 'nabh', label: 'Quality & NABH', route: '/quality', end: true, icon: 'BarChart', allowedRoles: ['ADMIN'] },
      { id: 'legal', label: 'Legal / Compliance', route: '/legal', end: true, icon: 'Scale', allowedRoles: ['ADMIN'] },
      { id: 'reports', label: 'Reports & Audits', route: '/reports', end: true, icon: 'BarChart', allowedRoles: ['ADMIN'] },
    ],
  },
  {
    id: 'operations-maintenance',
    label: 'Operations & Maintenance',
    allowedRoles: ['ADMIN'],
    items: [
      { id: 'biomed', label: 'Biomedical Engineering', route: '/biomed', end: true, icon: 'Wrench', allowedRoles: ['ADMIN'] },
      { id: 'electrical', label: 'Electrical & Maintenance', route: '/maintenance', end: true, icon: 'Zap', allowedRoles: ['ADMIN'] },
      { id: 'fire-safety', label: 'Fire & Safety', route: '/fire-safety', end: true, icon: 'AlertCircle', allowedRoles: ['ADMIN'] },
      { id: 'security', label: 'Security', route: '/security', end: true, icon: 'Lock', allowedRoles: ['ADMIN'] },
      { id: 'biowaste', label: 'Biomedical Waste Management', route: '/biowaste', end: true, icon: 'Trash2', allowedRoles: ['ADMIN'] },
      { id: 'cssd', label: 'CSSD', route: '/cssd', end: true, icon: 'Layers', allowedRoles: ['ADMIN'] },
    ],
  },
  {
    id: 'housekeeping',
    label: 'Housekeeping & Patient Services',
    allowedRoles: ['ADMIN'],
    items: [
      { id: 'housekeeping', label: 'Housekeeping', route: '/housekeeping', end: true, icon: 'Building2', allowedRoles: ['ADMIN'] },
      { id: 'laundry', label: 'Laundry & Linen', route: '/laundry', end: true, icon: 'Layers', allowedRoles: ['ADMIN'] },
      { id: 'dietary', label: 'Dietary / Kitchen', route: '/dietary', end: true, icon: 'Utensils', allowedRoles: ['ADMIN'] },
      { id: 'patient-meals', label: 'Patient Meals', route: '/patient-meals', end: true, icon: 'Utensils', allowedRoles: ['ADMIN'] },
    ],
  },
  {
    id: 'it-systems',
    label: 'IT, Systems & Digital Health',
    allowedRoles: ['ADMIN', 'IT_ADMIN'],
    items: [
      { id: 'it-dash', label: 'IT Department / HIS Management', route: '/admin', end: true, icon: 'Monitor', allowedRoles: ['ADMIN', 'IT_ADMIN'] },
      { id: 'users-roles', label: 'User & Role Management', route: '/admin/users', end: true, icon: 'Users', allowedRoles: ['ADMIN', 'IT_ADMIN'] },
      { id: 'system-config', label: 'System Configuration', route: '/admin/config', end: true, icon: 'Settings', allowedRoles: ['ADMIN', 'SUPER_ADMIN', 'IT_ADMIN'] },
      { id: 'audit-logs', label: 'Audit Logs', route: '/admin/audit', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'IT_ADMIN'] },
      { id: 'telemedicine', label: 'Telemedicine', route: '/telemedicine', end: true, icon: 'Monitor', allowedRoles: ['ADMIN', 'IT_ADMIN'] },
      { id: 'research', label: 'Research & Training', route: '/research', end: true, icon: 'BookOpen', allowedRoles: ['ADMIN', 'IT_ADMIN'] },
    ],
  },
  {
    id: 'inventory',
    label: 'Inventory & Procurement',
    allowedRoles: ['ADMIN'],
    items: [
      { id: 'purchase', label: 'Store & Purchase', route: '/purchase', end: true, icon: 'ShoppingCart', allowedRoles: ['ADMIN'] },
      { id: 'inventory', label: 'Inventory Management', route: '/inventory', end: true, icon: 'Package', allowedRoles: ['ADMIN'] },
      { id: 'vendors', label: 'Vendor Management', route: '/vendors', end: true, icon: 'Truck', allowedRoles: ['ADMIN'] },
      { id: 'stock-alerts', label: 'Stock Alerts', route: '/stock-alerts', end: true, icon: 'AlertTriangle', allowedRoles: ['ADMIN'] },
    ],
  },
  {
    id: 'marketing',
    label: 'Marketing & Outreach',
    allowedRoles: ['ADMIN'],
    items: [
      { id: 'pr', label: 'Public Relations / Marketing', route: '/marketing', end: true, icon: 'Megaphone', allowedRoles: ['ADMIN'] },
      { id: 'camps', label: 'Community Health Camps', route: '/health-camps', end: true, icon: 'Sparkles', allowedRoles: ['ADMIN'] },
    ],
  },
]
