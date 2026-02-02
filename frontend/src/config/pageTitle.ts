/**
 * Resolve page title from pathname using sidebar menu config.
 * Fallback to a generic title when no match.
 */

import { SIDEBAR_MENU_GROUPS } from './sidebarMenu'
import type { SidebarMenuItem } from './sidebarMenu'

function findLabelInItems(items: SidebarMenuItem[], pathname: string): string | null {
  for (const item of items) {
    if (item.route) {
      const end = item.end !== false
      const exact = end && (pathname === item.route || pathname === item.route + '/')
      const prefix = !end && (pathname === item.route || pathname.startsWith(item.route + '/'))
      if (exact || prefix) return item.label
    }
    if (item.children) {
      const found = findLabelInItems(item.children, pathname)
      if (found) return found
    }
  }
  return null
}

const DYNAMIC_TITLES: { pattern: RegExp; title: string }[] = [
  { pattern: /^\/doctors\/\d+\/edit/, title: 'Edit Doctor' },
  { pattern: /^\/doctors\/\d+\/availability/, title: 'Doctor Availability' },
  { pattern: /^\/opd\/visits\/\d+/, title: 'OPD Visit' },
  { pattern: /^\/ipd\/admissions\/\d+/, title: 'IPD Admission' },
  { pattern: /^\/admin\/config\/roles/, title: 'Role Management' },
  { pattern: /^\/admin\/config\/modules/, title: 'Module Management' },
  { pattern: /^\/admin\/config\/permissions/, title: 'Permission Matrix' },
  { pattern: /^\/admin\/config\/features/, title: 'Feature Toggles' },
  { pattern: /^\/admin\/config/, title: 'System Configuration' },
]

export function getPageTitleFromPath(pathname: string): string {
  const normalized = pathname.replace(/\/$/, '') || '/'
  for (const { pattern, title } of DYNAMIC_TITLES) {
    if (pattern.test(normalized)) return title
  }
  for (const group of SIDEBAR_MENU_GROUPS) {
    const label = findLabelInItems(group.items, normalized)
    if (label) return label
  }
  if (normalized === '/' || normalized === '/reception') return 'Dashboard'
  return 'HMS'
}
