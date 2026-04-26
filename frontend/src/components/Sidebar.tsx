/**
 * Grouped sidebar navigation with role-based visibility.
 * Uses config + filter only; no role logic in JSX.
 * Design: vibrant purple SaaS shell with flat section labels,
 * badge counts, collapsible groups, and linked user profile card.
 */

import { useState, useEffect } from 'react'
import { Link, NavLink, useLocation } from 'react-router-dom'
import { SIDEBAR_MENU_GROUPS } from '../config/sidebarMenu'
import {
  filterMenuByRole,
  filterStubMenuItems,
  filterMenuByModulePermissions,
  normalizeUserRoles,
} from '../config/menuFilter'
import type { HMSRole, SidebarMenuItem } from '../config/sidebarMenu'
import { usePermissionsOptional } from '../contexts/PermissionsContext'
import { useFeatureFlagsOptional } from '../contexts/FeatureFlagsContext'
import { SidebarIcon } from './SidebarIcons'
import styles from './Layout.module.css'

const EXPANDED_KEY = 'hms_sidebar_groups_expanded'

function loadExpanded(): Set<string> {
  try {
    const raw = localStorage.getItem(EXPANDED_KEY)
    if (!raw) return new Set()
    const arr = JSON.parse(raw) as string[]
    return new Set(Array.isArray(arr) ? arr : [])
  } catch {
    return new Set()
  }
}

function saveExpanded(set: Set<string>) {
  try {
    localStorage.setItem(EXPANDED_KEY, JSON.stringify([...set]))
  } catch { /* ignore */ }
}

function isRouteActive(pathname: string, route: string | undefined, end: boolean | undefined): boolean {
  if (!route) return false
  const normalized = pathname.replace(/\/$/, '') || '/'
  const r = route.replace(/\/$/, '') || '/'
  if (end !== false) return normalized === r || normalized === r + '/'
  return normalized === r || normalized.startsWith(r + '/')
}

function isGroupActive(pathname: string, items: SidebarMenuItem[]): boolean {
  for (const item of items) {
    if (item.route && isRouteActive(pathname, item.route, item.end)) return true
    if (item.children && isGroupActive(pathname, item.children)) return true
  }
  return false
}

function normalizeFeatureKey(key: string): string {
  return key.trim().replace(/-/g, '_').toUpperCase()
}

function featureKeyForRoute(route?: string): string | null {
  if (!route) return null
  if (route.startsWith('/front-office/appointments')) return 'APPOINTMENTS'
  if (route.startsWith('/billing') || route.startsWith('/patient-flow/billing')) return 'BILLING'
  if (route.startsWith('/lab/reports') || route.startsWith('/lab/view-reports') || route.startsWith('/radiology/reports')) return 'REPORTS'
  if (route.startsWith('/transport')) return 'TRANSPORT'
  return null
}

type FeatureState = {
  enabled: boolean
  uiMode: 'hide' | 'disabled'
}

function resolveFeatureState(route: string | undefined, featuresByKey: Record<string, FeatureState>): FeatureState | null {
  const featureKey = featureKeyForRoute(route)
  if (!featureKey) return null
  return featuresByKey[normalizeFeatureKey(featureKey)] ?? null
}

function filterItemsByFeatureFlags(
  items: SidebarMenuItem[],
  featuresByKey: Record<string, FeatureState>
): SidebarMenuItem[] {
  const result: SidebarMenuItem[] = []
  for (const item of items) {
    const state = resolveFeatureState(item.route, featuresByKey)
    if (state && !state.enabled && state.uiMode === 'hide') continue
    if (item.children && item.children.length > 0) {
      const filteredChildren = filterItemsByFeatureFlags(item.children, featuresByKey)
      if (filteredChildren.length === 0) continue
      result.push({ ...item, children: filteredChildren })
    } else {
      result.push(item)
    }
  }
  return result
}

function getInitials(name: string): string {
  return name
    .split(/[\s_@.]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase()
}

function roleLabel(roles: HMSRole[]): string {
  const priority: Partial<Record<HMSRole, string>> = {
    SUPER_ADMIN: 'Super Administrator',
    ADMIN: 'Administrator',
    DOCTOR: 'Doctor',
    NURSE: 'Nurse',
    PHARMACIST: 'Pharmacist',
    LAB_TECH: 'Lab Technician',
    RADIOLOGY_TECH: 'Radiology Tech',
    BILLING: 'Billing Staff',
    FRONT_DESK: 'Front Desk',
    HOUSEKEEPING: 'Housekeeping',
    HR_MANAGER: 'HR Manager',
  }
  for (const r of roles) {
    if (priority[r]) return priority[r]!
  }
  return roles[0] ?? 'Staff'
}

function ChevronIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="m6 9 6 6 6-6" />
    </svg>
  )
}

interface SidebarProps {
  userRoles: HMSRole[]
  collapsed: boolean
  username?: string
  hospitalName?: string
}

export function Sidebar({ userRoles, collapsed, username, hospitalName }: SidebarProps) {
  const location = useLocation()
  const perms = usePermissionsOptional()
  const featureFlags = useFeatureFlagsOptional()
  const [collapsedGroups, setCollapsedGroups] = useState<Set<string>>(loadExpanded)

  const normalized = normalizeUserRoles(userRoles)
  const skipPermissionMatrix = normalized.includes('ADMIN') || normalized.includes('SUPER_ADMIN')

  let filteredGroups = filterStubMenuItems(filterMenuByRole(SIDEBAR_MENU_GROUPS, userRoles))
  if (!skipPermissionMatrix && perms?.hasUsablePermissionData) {
    filteredGroups = filterMenuByModulePermissions(filteredGroups, perms.isModuleVisibleForMenu)
  }
  const featuresByKey = featureFlags?.featuresByKey ?? {}
  filteredGroups = filteredGroups
    .map((group) => ({ ...group, items: filterItemsByFeatureFlags(group.items, featuresByKey) }))
    .filter((group) => group.items.length > 0)

  useEffect(() => {
    saveExpanded(collapsedGroups)
  }, [collapsedGroups])

  const toggleGroup = (id: string) => {
    setCollapsedGroups((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  const displayName = username ?? 'Admin User'
  const initials = getInitials(displayName)
  const role = roleLabel(userRoles)
  const tenantBrandName = hospitalName?.trim() || 'HMS'
  const tenantBrandSubtext = hospitalName?.trim() ? 'Hospital Workspace' : 'Software'
  const tenantInitials = getInitials(tenantBrandName).slice(0, 2) || 'H'

  return (
    <aside className={`${styles.sidebar} ${collapsed ? styles.sidebarCollapsed : ''}`} aria-label="Main navigation">
      {/* Header / Logo */}
      <div className={styles.sidebarHeader}>
        <Link to="/" className={styles.logo} title={collapsed ? `${tenantBrandName} – Home` : undefined}>
          <span className={styles.logoIcon}>{tenantInitials}</span>
          {!collapsed && (
            <span className={styles.logoMeta}>
              <span className={styles.logoText}>{tenantBrandName}</span>
              <span className={styles.logoSub}>{tenantBrandSubtext}</span>
            </span>
          )}
        </Link>
      </div>

      {/* Navigation */}
      <nav className={styles.sidebarNav}>
        {filteredGroups.map((group) => {
          const hasActiveChild = isGroupActive(location.pathname, group.items)
          // A group is open if: not manually collapsed, OR it has an active child (auto-expand)
          const isOpen = !collapsedGroups.has(group.id) || hasActiveChild

          return (
            <div key={group.id} className={styles.navGroup}>
              {!collapsed && (
                <button
                  type="button"
                  className={styles.navGroupHeader}
                  onClick={() => toggleGroup(group.id)}
                  aria-expanded={isOpen}
                  aria-controls={`nav-group-${group.id}`}
                >
                  <span className={styles.navGroupLabel}>{group.label}</span>
                  <span className={`${styles.navGroupChevron} ${isOpen ? styles.navGroupChevronOpen : ''}`}>
                    <ChevronIcon />
                  </span>
                </button>
              )}
              <div
                id={`nav-group-${group.id}`}
                className={styles.navGroupItems}
                data-expanded={String(isOpen || collapsed)}
              >
                <div className={styles.navGroupItemsInner}>
                  {group.items.map((item) => (
                    <MenuItem
                      key={item.id}
                      item={item}
                      pathname={location.pathname}
                      collapsed={collapsed}
                      featuresByKey={featuresByKey}
                    />
                  ))}
                </div>
              </div>
            </div>
          )
        })}
      </nav>

      {/* User profile card — links to /profile */}
      <div className={styles.sidebarFooter}>
        <Link
          to="/profile"
          className={styles.sidebarProfile}
          title={collapsed ? `${displayName} – ${role}` : 'My Profile'}
        >
          <span className={styles.sidebarAvatar}>{initials}</span>
          {!collapsed && (
            <span className={styles.sidebarProfileInfo}>
              <span className={styles.sidebarProfileName}>{displayName}</span>
              <span className={styles.sidebarProfileRole}>{role}</span>
            </span>
          )}
        </Link>
      </div>
    </aside>
  )
}

function MenuItem({
  item,
  pathname,
  collapsed,
  featuresByKey,
}: {
  item: SidebarMenuItem
  pathname: string
  collapsed: boolean
  featuresByKey: Record<string, FeatureState>
}) {
  if (item.children && item.children.length > 0) {
    const isActive = isGroupActive(pathname, item.children)
    return (
      <div className={styles.navNestedWrap} title={collapsed ? item.label : undefined}>
        <div
          className={`${styles.navSubsectionHeader} ${isActive ? styles.navSubsectionHasActiveChild : ''}`}
          role="presentation"
        >
          <span className={styles.navIcon} aria-hidden>
            <SidebarIcon name={item.icon} />
          </span>
          {!collapsed && <span className={styles.navLabel}>{item.label}</span>}
        </div>
        {!collapsed &&
          item.children.map((child) => {
            const featureState = resolveFeatureState(child.route, featuresByKey)
            const isDisabled = !!featureState && !featureState.enabled && featureState.uiMode === 'disabled'
            if (isDisabled) {
              return (
                <div
                  key={child.id}
                  className={`${styles.navItem} ${styles.navItemNested} ${styles.navItemDisabled}`}
                  title={`${child.label} (disabled)`}
                  aria-disabled
                >
                  <span className={styles.navIcon}><SidebarIcon name={child.icon} /></span>
                  <span className={styles.navLabel}>{child.label}</span>
                </div>
              )
            }
            return (
              <NavLink
                key={child.id}
                to={child.route!}
                end={child.end !== false}
                className={({ isActive }) =>
                  `${styles.navItem} ${styles.navItemNested} ${styles.navItemLink} ${isActive ? styles.navItemActive : ''}`
                }
                title={collapsed ? child.label : undefined}
              >
                <span className={styles.navIcon}><SidebarIcon name={child.icon} /></span>
                <span className={styles.navLabel}>{child.label}</span>
                {child.badge !== undefined && (
                  <span className={styles.navBadge}>{child.badge}</span>
                )}
              </NavLink>
            )
          })}
      </div>
    )
  }

  if (!item.route) return null

  const featureState = resolveFeatureState(item.route, featuresByKey)
  const isDisabled = !!featureState && !featureState.enabled && featureState.uiMode === 'disabled'

  if (isDisabled) {
    return (
      <div
        className={`${styles.navItem} ${styles.navItemDisabled}`}
        title={`${item.label} (disabled)`}
        aria-disabled
      >
        <span className={styles.navIcon}><SidebarIcon name={item.icon} /></span>
        {!collapsed && <span className={styles.navLabel}>{item.label}</span>}
      </div>
    )
  }

  return (
    <NavLink
      to={item.route}
      end={item.end !== false}
      className={({ isActive }) =>
        `${styles.navItem} ${styles.navItemLink} ${isActive ? styles.navItemActive : ''}`
      }
      title={collapsed ? item.label : undefined}
    >
      <span className={styles.navIcon}><SidebarIcon name={item.icon} /></span>
      {!collapsed && <span className={styles.navLabel}>{item.label}</span>}
      {!collapsed && item.badge !== undefined && (
        <span className={styles.navBadge}>{item.badge}</span>
      )}
    </NavLink>
  )
}