/**
 * HMS Sidebar Menu Configuration
 * Scalable, data-driven navigation with role-based visibility.
 * Icon keys map to components in the icon registry.
 * Diagnostic modules (Pathology, Pharmacy, Radiology, Blood Bank) use menuConfig for strict RBAC.
 */

import { MODULE_MENU_CONFIG } from './menuConfig'

/** Build SidebarMenuGroup[] from centralized menuConfig (Pathology, Pharmacy, Radiology, Blood Bank). */
function buildDiagnosticMenuGroups(): SidebarMenuGroup[] {
  return MODULE_MENU_CONFIG.map((module) => ({
    id: module.id,
    label: module.moduleName,
    allowedRoles: module.allowedRoles as HMSRole[],
    items: [
      {
        id: `${module.id}-parent`,
        label: module.moduleName,
        route: module.route,
        end: false,
        icon: module.icon as MenuIconKey,
        allowedRoles: module.allowedRoles as HMSRole[],
        children: module.subMenus.map((sub) => ({
          id: sub.id,
          label: sub.label,
          route: sub.route,
          end: true,
          icon: sub.icon as MenuIconKey,
          allowedRoles: module.allowedRoles as HMSRole[],
        })),
      },
    ],
  }))
}

const DIAGNOSTIC_MENU_GROUPS = buildDiagnosticMenuGroups()

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
  | 'GitBranch'

export type HMSRole =
  | 'ADMIN'
  | 'SUPER_ADMIN'
  | 'HR'
  | 'MEDICAL_SUPERINTENDENT'
  | 'RECEPTIONIST'
  | 'DOCTOR'
  | 'NURSE'
  | 'LAB_TECH'
  | 'LAB_TECHNICIAN'
  | 'LAB_SUPERVISOR'
  | 'PHLEBOTOMIST'
  | 'PHARMACIST'
  | 'PHARMACY_MANAGER'
  | 'STORE_INCHARGE'
  | 'IPD_PHARMACIST'
  | 'BILLING'
  | 'IT_ADMIN'
  | 'HELP_DESK'
  | 'QUALITY_MANAGER'
  | 'RADIOLOGY_TECH'
  | 'BLOOD_BANK_TECH'
  | 'IPD_MANAGER'
  | 'HOUSEKEEPING'
  | 'LAUNDRY'
  | 'KITCHEN'
  | 'FRONT_DESK'

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
  /** Optional icon for group header (e.g. Users for Front Office, GitBranch for Patient Flow) */
  groupIcon?: MenuIconKey
  allowedRoles: HMSRole[]
  items: SidebarMenuItem[]
}

export const SIDEBAR_MENU_GROUPS: SidebarMenuGroup[] = [
  {
    id: 'front-office',
    label: 'Front Office',
    groupIcon: 'Users',
    allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST', 'BILLING'],
    items: [
      { id: 'fo-reception', label: 'Reception', route: '/reception', end: true, icon: 'LayoutDashboard', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST', 'BILLING'] },
      { id: 'fo-register', label: 'Patient Registration', route: '/front-office/register', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      { id: 'fo-appointments', label: 'Appointments', route: '/front-office/appointments', end: false, icon: 'Calendar', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      { id: 'fo-appointments-book', label: 'Book Appointment', route: '/front-office/appointments/book', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      { id: 'fo-appointments-queue', label: 'Appointment Queue', route: '/front-office/appointments/queue', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      { id: 'fo-appointments-search', label: 'Search Appointments', route: '/front-office/appointments/search', end: true, icon: 'Search', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      { id: 'fo-walkin', label: 'Walk-in', route: '/front-office/walkin', end: false, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'], children: [
        { id: 'fo-walkin-dash', label: 'Walk-in Dashboard', route: '/front-office/walkin', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
        { id: 'fo-walkin-register', label: 'Register Walk-in', route: '/front-office/walkin/register', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      ]},
      { id: 'fo-enquiry', label: 'Enquiry Desk', route: '/front-office/enquiry', end: true, icon: 'HelpCircle', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      { id: 'fo-tokens', label: 'Token Management', route: '/front-office/tokens', end: false, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'], children: [
        { id: 'fo-tokens-dash', label: 'Token Dashboard', route: '/front-office/tokens', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
        { id: 'fo-tokens-queue', label: 'Token Queue', route: '/front-office/tokens/queue', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'FRONT_DESK', 'RECEPTIONIST'] },
      ]},
    ],
  },
  {
    id: 'patient-flow',
    label: 'Patient Flow',
    groupIcon: 'GitBranch',
    allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER', 'BILLING'],
    items: [
      { id: 'pf-opd', label: 'OPD Visits', route: '/patient-flow/opd', end: true, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-token-panel', label: 'Doctor Token Panel', route: '/opd/tokens', end: true, icon: 'Queue', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-consultation', label: 'Doctor Consultation', route: '/patient-flow/consultation', end: true, icon: 'Stethoscope', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-lab', label: 'Lab Orders', route: '/patient-flow/lab-orders', end: true, icon: 'FlaskConical', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-radiology', label: 'Radiology Orders', route: '/patient-flow/radiology-orders', end: true, icon: 'ScanLine', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-pharmacy', label: 'Pharmacy Orders', route: '/patient-flow/pharmacy-orders', end: true, icon: 'Pill', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-admission', label: 'Admission', route: '/patient-flow/admission', end: true, icon: 'UserPlus', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-beds', label: 'Bed Allocation', route: '/patient-flow/bed-allocation', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
      { id: 'pf-billing', label: 'Billing', route: '/patient-flow/billing', end: true, icon: 'DollarSign', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER', 'BILLING'] },
      { id: 'pf-discharge', label: 'Discharge', route: '/patient-flow/discharge', end: true, icon: 'FileText', allowedRoles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'FRONT_DESK', 'IPD_MANAGER'] },
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
    id: 'patient-services',
    label: 'Patient Services',
    allowedRoles: ['ADMIN', 'NURSE', 'HOUSEKEEPING', 'LAUNDRY', 'KITCHEN'],
    items: [
      { id: 'housekeeping', label: 'Housekeeping', route: '/housekeeping', end: true, icon: 'Trash2', allowedRoles: ['ADMIN', 'HOUSEKEEPING', 'NURSE'] },
      { id: 'laundry', label: 'Laundry & Linen', route: '/laundry', end: true, icon: 'Package', allowedRoles: ['ADMIN', 'LAUNDRY', 'NURSE'] },
      { id: 'dietary', label: 'Dietary / Kitchen', route: '/dietary', end: true, icon: 'Utensils', allowedRoles: ['ADMIN', 'KITCHEN', 'NURSE'] },
      { id: 'meals', label: 'Patient Meals', route: '/meals', end: true, icon: 'Utensils', allowedRoles: ['ADMIN', 'KITCHEN', 'NURSE'] },
    ],
  },
  ...DIAGNOSTIC_MENU_GROUPS,
  {
    id: 'billing-financials',
    label: 'Billing, Insurance & Financials',
    allowedRoles: ['ADMIN', 'BILLING'],
    items: [
      { id: 'billing', label: 'Billing & Accounts', route: '/billing', end: false, icon: 'DollarSign', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'billing-ipd', label: 'IPD Billing', route: '/billing/ipd', end: true, icon: 'Bed', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'billing-corporate', label: 'Corporate Billing', route: '/billing/corporate', end: true, icon: 'Briefcase', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'billing-emi', label: 'EMI Billing', route: '/billing/emi', end: true, icon: 'CreditCard', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'billing-online', label: 'Online Payment', route: '/billing/payment/online', end: true, icon: 'CreditCard', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'billing-opd-group', label: 'OPD Group Bill', route: '/billing/opd/group', end: true, icon: 'ClipboardList', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'billing-tpa', label: 'Insurance / TPA', route: '/billing/tpa', end: true, icon: 'Shield', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'payments', label: 'Payments', route: '/billing/payments', end: true, icon: 'CreditCard', allowedRoles: ['ADMIN', 'BILLING'] },
      { id: 'refunds', label: 'Refunds', route: '/billing/refunds', end: true, icon: 'RefreshCw', allowedRoles: ['ADMIN', 'BILLING'] },
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
