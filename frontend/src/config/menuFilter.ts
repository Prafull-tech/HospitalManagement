/**
 * Role-based menu filtering.
 * Pure functions – no JSX, no React. Used by Sidebar to derive visible menu.
 * Legacy roles are normalized to canonical 11 so existing users still see correct menus.
 */

import type { HMSRole } from './sidebarMenu'
import type { SidebarMenuItem, SidebarMenuGroup } from './sidebarMenu'
import { showStubSidebarItems } from './featureFlags'

/** Map legacy backend roles to canonical roles for menu visibility (no route break). */
export function normalizeUserRoles(userRoles: HMSRole[]): HMSRole[] {
  const set = new Set<HMSRole>(userRoles)
  if (set.has('RECEPTIONIST')) set.add('FRONT_DESK')
  if (set.has('LAB_TECHNICIAN') || set.has('LAB_SUPERVISOR') || set.has('PATHOLOGIST') || set.has('PHLEBOTOMIST')) set.add('LAB_TECH')
  if (set.has('PHARMACY_MANAGER') || set.has('STORE_INCHARGE') || set.has('IPD_PHARMACIST')) set.add('PHARMACIST')
  if (set.has('IPD_MANAGER')) set.add('DOCTOR')
  if (set.has('LAUNDRY') || set.has('KITCHEN')) set.add('HOUSEKEEPING')
  if (set.has('HR')) set.add('HR_MANAGER')
  if (set.has('IT_ADMIN')) set.add('SUPER_ADMIN')
  return [...set]
}

function hasAccess(roles: HMSRole[], userRoles: HMSRole[]): boolean {
  if (userRoles.includes('ADMIN')) return true
  return roles.some((r) => userRoles.includes(r))
}

function filterItems(items: SidebarMenuItem[], userRoles: HMSRole[]): SidebarMenuItem[] {
  const result: SidebarMenuItem[] = []
  for (const item of items) {
    if (!hasAccess(item.allowedRoles, userRoles)) continue
    if (item.children) {
      const filteredChildren = filterItems(item.children, userRoles)
      if (filteredChildren.length > 0) {
        result.push({ ...item, children: filteredChildren })
      }
    } else {
      result.push(item)
    }
  }
  return result
}

/**
 * Returns menu groups and their items visible for the given roles.
 * ADMIN/SUPER_ADMIN see everything; others see only groups/items where allowedRoles includes their role.
 * Uses normalized roles so legacy roles (e.g. RECEPTIONIST) still get correct menus.
 */
export function filterMenuByRole(
  groups: SidebarMenuGroup[],
  userRoles: HMSRole[]
): SidebarMenuGroup[] {
  const normalized = normalizeUserRoles(userRoles)
  const result: SidebarMenuGroup[] = []
  for (const group of groups) {
    if (!hasAccess(group.allowedRoles, normalized)) continue
    const filteredItems = filterItems(group.items, normalized)
    if (filteredItems.length > 0) {
      result.push({ ...group, items: filteredItems })
    }
  }
  return result
}

/**
 * Filter menu by allowed routes (from /api/system/permissions/me).
 * Keeps items whose route is allowed (pathname match) or have no route (parent nodes).
 */
function filterItemsByRoute(
  items: SidebarMenuItem[],
  isRouteAllowed: (path: string) => boolean
): SidebarMenuItem[] {
  const result: SidebarMenuItem[] = []
  for (const item of items) {
    if (item.children && item.children.length > 0) {
      const filteredChildren = filterItemsByRoute(item.children, isRouteAllowed)
      if (filteredChildren.length > 0) {
        result.push({ ...item, children: filteredChildren })
      }
    } else if (item.route && isRouteAllowed(item.route)) {
      result.push(item)
    } else if (!item.route) {
      result.push(item)
    }
  }
  return result
}

export function filterMenuByAllowedRoutes(
  groups: SidebarMenuGroup[],
  isRouteAllowed: (path: string) => boolean
): SidebarMenuGroup[] {
  const result: SidebarMenuGroup[] = []
  for (const group of groups) {
    const filteredItems = filterItemsByRoute(group.items, isRouteAllowed)
    if (filteredItems.length > 0) {
      result.push({ ...group, items: filteredItems })
    }
  }
  return result
}

/** Group ids whose entire sidebar block is stub / not production-ready. */
const STUB_MENU_GROUP_IDS = new Set(['radiology', 'hr-management'])
/** Single items to hide under mixed groups (e.g. Reception). */
const STUB_MENU_ITEM_IDS = new Set(['fo-enquiry', 'fo-visitors'])

/**
 * When {@link showStubSidebarItems} is false, removes stub entries so production menus stay accurate.
 */
export function filterStubMenuItems(groups: SidebarMenuGroup[]): SidebarMenuGroup[] {
  if (showStubSidebarItems()) return groups
  return groups
    .filter((g) => !STUB_MENU_GROUP_IDS.has(g.id))
    .map((g) => ({
      ...g,
      items: g.items.filter((item) => !STUB_MENU_ITEM_IDS.has(item.id)),
    }))
}

/**
 * Filter menu items by permission matrix module codes (after role + stub filters).
 * Items without `moduleCode` stay visible if they passed earlier filters (legacy / dashboard).
 * Parent items with only `children` and no `moduleCode` are kept when any child remains.
 */
export function filterMenuByModulePermissions(
  groups: SidebarMenuGroup[],
  isModuleVisibleForMenu: (moduleCode: string) => boolean
): SidebarMenuGroup[] {
  function filterItems(items: SidebarMenuItem[]): SidebarMenuItem[] {
    const result: SidebarMenuItem[] = []
    for (const item of items) {
      if (item.children && item.children.length > 0) {
        const filteredChildren = filterItems(item.children)
        if (filteredChildren.length === 0) continue
        if (item.moduleCode && !isModuleVisibleForMenu(item.moduleCode)) continue
        result.push({ ...item, children: filteredChildren })
        continue
      }
      if (item.moduleCode && !isModuleVisibleForMenu(item.moduleCode)) continue
      result.push(item)
    }
    return result
  }
  return groups
    .map((g) => ({ ...g, items: filterItems(g.items) }))
    .filter((g) => g.items.length > 0)
}
