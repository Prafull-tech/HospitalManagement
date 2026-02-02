/**
 * Role-based menu filtering.
 * Pure functions – no JSX, no React. Used by Sidebar to derive visible menu.
 */

import type { HMSRole } from './sidebarMenu'
import type { SidebarMenuItem, SidebarMenuGroup } from './sidebarMenu'

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
 * ADMIN sees everything; others see only groups/items where allowedRoles includes their role.
 */
export function filterMenuByRole(
  groups: SidebarMenuGroup[],
  userRoles: HMSRole[]
): SidebarMenuGroup[] {
  const result: SidebarMenuGroup[] = []
  for (const group of groups) {
    if (!hasAccess(group.allowedRoles, userRoles)) continue
    const filteredItems = filterItems(group.items, userRoles)
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
