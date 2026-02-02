/**
 * Grouped sidebar navigation with role-based visibility.
 * Uses config + filter only; no role logic in JSX.
 */

import { useState, useEffect } from 'react'
import { Link, NavLink, useLocation } from 'react-router-dom'
import { SIDEBAR_MENU_GROUPS } from '../config/sidebarMenu'
import { filterMenuByRole, filterMenuByAllowedRoutes } from '../config/menuFilter'
import type { HMSRole, SidebarMenuItem } from '../config/sidebarMenu'
import { usePermissionsOptional } from '../contexts/PermissionsContext'
import { SidebarIcon } from './SidebarIcons'
import styles from './Layout.module.css'

const STORAGE_KEY = 'hms_sidebar_expanded'

function loadExpanded(): Set<string> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return new Set()
    const arr = JSON.parse(raw) as string[]
    return new Set(Array.isArray(arr) ? arr : [])
  } catch {
    return new Set()
  }
}

function saveExpanded(set: Set<string>) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify([...set]))
  } catch {
    // ignore
  }
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

interface SidebarProps {
  userRoles: HMSRole[]
  collapsed: boolean
}

const COLLAPSED_KEY = 'hms_sidebar_user_collapsed'

function loadUserCollapsed(): Set<string> {
  try {
    const raw = localStorage.getItem(COLLAPSED_KEY)
    if (!raw) return new Set()
    const arr = JSON.parse(raw) as string[]
    return new Set(Array.isArray(arr) ? arr : [])
  } catch {
    return new Set()
  }
}

function saveUserCollapsed(set: Set<string>) {
  try {
    localStorage.setItem(COLLAPSED_KEY, JSON.stringify([...set]))
  } catch {
    // ignore
  }
}

export function Sidebar({ userRoles, collapsed }: SidebarProps) {
  const location = useLocation()
  const permissions = usePermissionsOptional()
  const [expanded, setExpanded] = useState<Set<string>>(loadExpanded)
  const [userCollapsed, setUserCollapsed] = useState<Set<string>>(loadUserCollapsed)

  const byRole = filterMenuByRole(SIDEBAR_MENU_GROUPS, userRoles)
  const filteredGroups =
    permissions?.hasPermissionData && permissions.isRouteAllowed
      ? filterMenuByAllowedRoutes(byRole, permissions.isRouteAllowed)
      : byRole

  useEffect(() => {
    saveExpanded(expanded)
  }, [expanded])

  useEffect(() => {
    saveUserCollapsed(userCollapsed)
  }, [userCollapsed])

  const toggle = (id: string) => {
    const wouldBeOpen = expanded.has(id) || isGroupActive(location.pathname, filteredGroups.find((g) => g.id === id)?.items ?? [])
    if (wouldBeOpen && !userCollapsed.has(id)) {
      setExpanded((prev) => {
        const next = new Set(prev)
        next.delete(id)
        return next
      })
      setUserCollapsed((prev) => new Set(prev).add(id))
    } else {
      setUserCollapsed((prev) => {
        const next = new Set(prev)
        next.delete(id)
        return next
      })
      setExpanded((prev) => new Set(prev).add(id))
    }
  }

  return (
    <aside className={`${styles.sidebar} ${collapsed ? styles.sidebarCollapsed : ''}`} aria-label="Main navigation">
      <div className={styles.sidebarHeader}>
        <Link to="/" className={styles.logo} title={collapsed ? 'HMS – Home' : undefined}>
          <span className={styles.logoText}>HMS</span>
          {!collapsed && <span className={styles.logoSub}>Hospital</span>}
        </Link>
      </div>
      <nav className={styles.sidebarNav}>
        {filteredGroups.map((group) => {
          const isOpen =
            (expanded.has(group.id) || isGroupActive(location.pathname, group.items)) &&
            !userCollapsed.has(group.id)
          const showGroupLabel = !collapsed

          return (
            <div key={group.id} className={styles.navGroup}>
              {showGroupLabel && (
                <button
                  type="button"
                  className={styles.navGroupHeader}
                  onClick={() => toggle(group.id)}
                  aria-expanded={isOpen}
                  aria-controls={`nav-group-${group.id}`}
                  title={group.label}
                >
                  <span className={styles.navGroupLabel}>{group.label}</span>
                  <span className={`${styles.navGroupChevron} ${isOpen ? styles.navGroupChevronOpen : ''}`} aria-hidden>
                    <ChevronIcon />
                  </span>
                </button>
              )}
              <div
                id={`nav-group-${group.id}`}
                className={styles.navGroupItems}
                data-expanded={isOpen || collapsed}
                data-collapsed={collapsed}
                role="region"
                aria-label={group.label}
              >
                <div className={styles.navGroupItemsInner}>
                  {group.items.map((item) => (
                    <MenuItem
                      key={item.id}
                      item={item}
                      pathname={location.pathname}
                      collapsed={collapsed}
                    />
                  ))}
                </div>
              </div>
            </div>
          )
        })}
      </nav>
    </aside>
  )
}

function ChevronIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="m6 9 6 6 6-6" />
    </svg>
  )
}

function MenuItem({
  item,
  pathname,
  collapsed,
}: {
  item: SidebarMenuItem
  pathname: string
  collapsed: boolean
}) {
  if (item.children && item.children.length > 0) {
    const isActive = isGroupActive(pathname, item.children)
    return (
      <div className={styles.navNestedWrap} title={collapsed ? item.label : undefined}>
        <div className={`${styles.navItem} ${styles.navItemParent} ${isActive ? styles.navItemActive : ''}`}>
          <span className={styles.navIcon}>
            <SidebarIcon name={item.icon} />
          </span>
          {!collapsed && <span className={styles.navLabel}>{item.label}</span>}
        </div>
        {!collapsed &&
          item.children.map((child) => (
            <NavLink
              key={child.id}
              to={child.route!}
              end={child.end !== false}
              className={({ isActive }) =>
                `${styles.navItem} ${styles.navItemNested} ${isActive ? styles.navItemActive : ''}`
              }
              title={child.label}
            >
              <span className={styles.navIcon}>
                <SidebarIcon name={child.icon} />
              </span>
              <span className={styles.navLabel}>{child.label}</span>
            </NavLink>
          ))}
      </div>
    )
  }

  if (!item.route) return null

  return (
    <NavLink
      to={item.route}
      end={item.end !== false}
      className={({ isActive }) => `${styles.navItem} ${isActive ? styles.navItemActive : ''}`}
      title={item.label}
    >
      <span className={styles.navIcon}>
        <SidebarIcon name={item.icon} />
      </span>
      {!collapsed && <span className={styles.navLabel}>{item.label}</span>}
    </NavLink>
  )
}
