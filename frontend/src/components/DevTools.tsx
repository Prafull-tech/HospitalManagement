import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { DEV_TOOLS_ROUTES } from '../config/devToolsRoutes'
import styles from './DevTools.module.css'

const GROUPS = [...new Set(DEV_TOOLS_ROUTES.map((r) => r.group))]

export function DevTools() {
  const [open, setOpen] = useState(false)
  const location = useLocation()

  if (!import.meta.env.DEV) return null

  return (
    <div className={styles.wrapper}>
      <button
        type="button"
        className={styles.trigger}
        onClick={() => setOpen((o) => !o)}
        title="Dev Tools - Quick navigation"
        aria-label="Open dev tools"
      >
        <WrenchIcon />
        <span>Dev</span>
      </button>
      {open && (
        <>
          <div className={styles.backdrop} onClick={() => setOpen(false)} aria-hidden />
          <div className={styles.panel} role="dialog" aria-label="Dev tools navigation">
            <div className={styles.panelHeader}>
              <h3>Dev Tools</h3>
              <button type="button" className={styles.closeBtn} onClick={() => setOpen(false)} aria-label="Close">
                ×
              </button>
            </div>
            <div className={styles.panelBody}>
              {GROUPS.map((group) => (
                <div key={group} className={styles.group}>
                  <div className={styles.groupLabel}>{group}</div>
                  <div className={styles.groupLinks}>
                    {DEV_TOOLS_ROUTES.filter((r) => r.group === group).map((r) => (
                      <Link
                        key={`${r.group}-${r.path}`}
                        to={r.path}
                        className={location.pathname === r.path ? styles.linkActive : styles.link}
                        onClick={() => setOpen(false)}
                      >
                        {r.label}
                      </Link>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  )
}

function WrenchIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
    </svg>
  )
}
