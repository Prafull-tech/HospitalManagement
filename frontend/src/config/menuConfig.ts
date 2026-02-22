/**
 * HMS Centralized Menu Configuration
 * Role-Based Sidebar Segregation - each module defines allowedRoles.
 * No hardcoded role checks in JSX; Sidebar filters by user role.
 */

export type MenuIconKey =
  | 'LayoutDashboard'
  | 'FileText'
  | 'FlaskConical'
  | 'Pill'
  | 'Queue'
  | 'Package'
  | 'AlertTriangle'
  | 'BarChart'
  | 'Layers'
  | 'RefreshCw'
  | 'ScanLine'
  | 'ClipboardList'
  | 'Monitor'
  | 'Users'
  | 'Droplets'

export type HMSRole =
  | 'ADMIN'
  | 'SUPER_ADMIN'
  | 'LAB_TECH'
  | 'LAB_TECHNICIAN'
  | 'LAB_SUPERVISOR'
  | 'QUALITY_MANAGER'
  | 'DOCTOR'
  | 'PHARMACY_MANAGER'
  | 'STORE_INCHARGE'
  | 'IPD_PHARMACIST'
  | 'PHARMACIST'
  | 'RADIOLOGY_TECH'
  | 'BLOOD_BANK_TECH'
  | 'RECEPTIONIST'
  | 'NURSE'
  | 'BILLING'
  | 'HELP_DESK'
  | 'HR'
  | 'MEDICAL_SUPERINTENDENT'
  | 'IT_ADMIN'
  | 'PHLEBOTOMIST'
  | 'IPD_MANAGER'

export interface SubMenuItem {
  id: string
  label: string
  route: string
  icon: MenuIconKey
}

export interface ModuleMenuConfig {
  id: string
  moduleName: string
  route: string
  icon: MenuIconKey
  allowedRoles: HMSRole[]
  subMenus: SubMenuItem[]
}

/**
 * Centralized menu configuration with strict role segregation.
 * Each module is visible ONLY to its allowedRoles.
 */
export const MODULE_MENU_CONFIG: ModuleMenuConfig[] = [
  {
    id: 'pathology-lab',
    moduleName: 'Pathology / Laboratory',
    route: '/lab',
    icon: 'FlaskConical',
    allowedRoles: ['LAB_TECH', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'DOCTOR', 'QUALITY_MANAGER', 'ADMIN'],
    subMenus: [
      { id: 'lab-dashboard', label: 'Lab Dashboard', route: '/lab', icon: 'LayoutDashboard' },
      { id: 'lab-reports', label: 'View Reports', route: '/lab/reports', icon: 'FileText' },
    ],
  },
  {
    id: 'pharmacy',
    moduleName: 'Pharmacy / Medical Store',
    route: '/pharmacy',
    icon: 'Pill',
    allowedRoles: ['PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'ADMIN'],
    subMenus: [
      { id: 'pharmacy-queue', label: 'IPD Medicine Issue Queue', route: '/pharmacy?tab=ISSUE_QUEUE', icon: 'Queue' },
      { id: 'pharmacy-fefo', label: 'FEFO Stock View', route: '/pharmacy?tab=FEFO_STOCK', icon: 'Package' },
      { id: 'pharmacy-alerts', label: 'Expiry & Critical Alerts', route: '/pharmacy?tab=ALERTS', icon: 'AlertTriangle' },
      { id: 'pharmacy-summary', label: "Today's Summary", route: '/pharmacy?tab=SUMMARY', icon: 'BarChart' },
      { id: 'pharmacy-medicines', label: 'Medicine List', route: '/pharmacy?tab=MEDICINE_LIST', icon: 'Pill' },
      { id: 'pharmacy-racks', label: 'Rack Management', route: '/pharmacy?tab=RACK_MANAGEMENT', icon: 'Layers' },
      { id: 'pharmacy-transactions', label: 'Stock Transactions', route: '/pharmacy?tab=STOCK_TRANSACTIONS', icon: 'RefreshCw' },
    ],
  },
  {
    id: 'radiology',
    moduleName: 'Radiology / Imaging',
    route: '/radiology',
    icon: 'ScanLine',
    allowedRoles: ['RADIOLOGY_TECH', 'DOCTOR', 'ADMIN'],
    subMenus: [
      { id: 'radiology-dashboard', label: 'Radiology Dashboard', route: '/radiology', icon: 'LayoutDashboard' },
      { id: 'radiology-imaging', label: 'Imaging Reports', route: '/radiology/reports', icon: 'FileText' },
      { id: 'radiology-requests', label: 'Scan Requests', route: '/radiology/requests', icon: 'ClipboardList' },
      { id: 'radiology-view', label: 'View Radiology Reports', route: '/radiology/view', icon: 'Monitor' },
    ],
  },
  {
    id: 'blood-bank',
    moduleName: 'Blood Bank',
    route: '/bloodbank',
    icon: 'Droplets',
    allowedRoles: ['BLOOD_BANK_TECH', 'ADMIN'],
    subMenus: [
      { id: 'blood-donor', label: 'Donor Management', route: '/bloodbank/donors', icon: 'Users' },
      { id: 'blood-inventory', label: 'Blood Inventory', route: '/bloodbank/inventory', icon: 'Package' },
      { id: 'blood-issue', label: 'Issue Blood Units', route: '/bloodbank/issue', icon: 'Droplets' },
      { id: 'blood-requests', label: 'Request Management', route: '/bloodbank/requests', icon: 'ClipboardList' },
      { id: 'blood-expiry', label: 'Blood Expiry Alerts', route: '/bloodbank/alerts', icon: 'AlertTriangle' },
    ],
  },
]

/**
 * Route-to-roles mapping for route protection.
 * Used by RoleProtectedRoute to block unauthorized URL access.
 */
export const ROUTE_PERMISSIONS: Record<string, HMSRole[]> = {
  '/reception': ['ADMIN', 'RECEPTIONIST', 'HELP_DESK', 'DOCTOR', 'NURSE'],
  '/billing': ['ADMIN', 'BILLING'],
  '/billing/ipd': ['ADMIN', 'BILLING'],
  '/billing/corporate': ['ADMIN', 'BILLING'],
  '/billing/emi': ['ADMIN', 'BILLING'],
  '/billing/payment/online': ['ADMIN', 'BILLING'],
  '/billing/opd/group': ['ADMIN', 'BILLING'],
  '/billing/tpa': ['ADMIN', 'BILLING'],
  '/billing/payments': ['ADMIN', 'BILLING'],
  '/billing/refunds': ['ADMIN', 'BILLING'],
  '/lab': ['LAB_TECH', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'DOCTOR', 'QUALITY_MANAGER', 'ADMIN'],
  '/lab/reports': ['LAB_TECH', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'DOCTOR', 'QUALITY_MANAGER', 'ADMIN'],
  '/pharmacy': ['PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'ADMIN'],
  '/radiology': ['RADIOLOGY_TECH', 'DOCTOR', 'ADMIN'],
  '/radiology/reports': ['RADIOLOGY_TECH', 'DOCTOR', 'ADMIN'],
  '/radiology/requests': ['RADIOLOGY_TECH', 'DOCTOR', 'ADMIN'],
  '/radiology/view': ['RADIOLOGY_TECH', 'DOCTOR', 'ADMIN'],
  '/bloodbank': ['BLOOD_BANK_TECH', 'ADMIN'],
  '/bloodbank/donors': ['BLOOD_BANK_TECH', 'ADMIN'],
  '/bloodbank/inventory': ['BLOOD_BANK_TECH', 'ADMIN'],
  '/bloodbank/issue': ['BLOOD_BANK_TECH', 'ADMIN'],
  '/bloodbank/requests': ['BLOOD_BANK_TECH', 'ADMIN'],
  '/bloodbank/alerts': ['BLOOD_BANK_TECH', 'ADMIN'],
}

/**
 * Get allowed roles for a route (exact or prefix match).
 */
export function getAllowedRolesForRoute(pathname: string): HMSRole[] | null {
  const normalized = pathname.replace(/\/$/, '') || '/'
  // Exact match first
  if (ROUTE_PERMISSIONS[normalized]) {
    return ROUTE_PERMISSIONS[normalized]
  }
  // Prefix match (e.g. /radiology/123 -> /radiology)
  const segments = normalized.split('/').filter(Boolean)
  for (let i = segments.length; i >= 1; i--) {
    const prefix = '/' + segments.slice(0, i).join('/')
    if (ROUTE_PERMISSIONS[prefix]) {
      return ROUTE_PERMISSIONS[prefix]
    }
  }
  return null
}

/**
 * Check if user has access to a route.
 */
export function hasRouteAccess(userRoles: HMSRole[], pathname: string): boolean {
  const allowed = getAllowedRolesForRoute(pathname)
  if (!allowed) return true // Routes not in ROUTE_PERMISSIONS are allowed (e.g. /reception)
  if (userRoles.includes('ADMIN')) return true
  return allowed.some((r) => userRoles.includes(r))
}

/**
 * Filter module config by user roles.
 * Returns only modules the user is authorized to access.
 */
export function filterModulesByRole(
  config: ModuleMenuConfig[],
  userRoles: HMSRole[]
): ModuleMenuConfig[] {
  return config.filter((module) => {
    if (userRoles.includes('ADMIN')) return true
    return module.allowedRoles.some((r) => userRoles.includes(r))
  })
}

/** Default dashboards in priority order for redirect (module-specific first, then reception). */
const DEFAULT_DASHBOARD_ORDER = ['/pharmacy', '/lab', '/radiology', '/bloodbank', '/reception']

/**
 * Get the first dashboard route the user has access to.
 * Used for index redirect and "Back to Dashboard" on 403.
 */
export function getDefaultDashboardForUser(userRoles: HMSRole[]): string {
  const normalized = userRoles.includes('LAB_TECHNICIAN')
    ? [...userRoles, 'LAB_TECH' as HMSRole]
    : userRoles
  for (const route of DEFAULT_DASHBOARD_ORDER) {
    if (hasRouteAccess(normalized, route)) return route
  }
  return '/reception' // Fallback; reception may show limited content
}
