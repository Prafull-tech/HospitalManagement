import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'
import { getPageTitleFromPath } from '../config/pageTitle'
import type { HMSRole } from '../config/sidebarMenu'
import { Sidebar } from './Sidebar'
import styles from './Layout.module.css'

function SunIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41" />
    </svg>
  )
}

function MoonIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
  )
}

function MenuIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <line x1="3" y1="12" x2="21" y2="12" />
      <line x1="3" y1="6" x2="21" y2="6" />
      <line x1="3" y1="18" x2="21" y2="18" />
    </svg>
  )
}

export function Layout() {
  const { user } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const location = useLocation()
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)

  const userRoles: HMSRole[] = user?.roles?.length ? user.roles : ['ADMIN']
  const pageTitle = getPageTitleFromPath(location.pathname)

  return (
    <div className={styles.wrapper}>
      <Sidebar userRoles={userRoles} collapsed={sidebarCollapsed} />

      <div className={styles.mainWrap}>
        <header className={styles.topbar}>
          <button
            type="button"
            onClick={() => setSidebarCollapsed((c) => !c)}
            className={styles.sidebarToggle}
            title={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
            aria-label={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            <MenuIcon />
          </button>
          <h1 className={styles.pageTitle}>{pageTitle}</h1>
          <div className={styles.topbarRight}>
            <button
              type="button"
              onClick={toggleTheme}
              className={styles.themeToggle}
              title={theme === 'light' ? 'Switch to night' : 'Switch to day'}
              aria-label={theme === 'light' ? 'Dark mode' : 'Light mode'}
            >
              {theme === 'light' ? <MoonIcon /> : <SunIcon />}
              <span className={styles.themeLabel}>{theme === 'light' ? 'Night' : 'Day'}</span>
            </button>
            <div className={styles.userBadge}>
              <span className={styles.userName}>{user?.username ?? 'Guest'}</span>
            </div>
          </div>
        </header>

        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  )
}
