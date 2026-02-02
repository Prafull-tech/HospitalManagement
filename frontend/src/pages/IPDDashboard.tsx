import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import shared from '../styles/Dashboard.module.css'

function BedIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M2 4v16" />
      <path d="M2 8h20a2 2 0 0 1 2 2v10" />
      <path d="M2 17h20" />
      <path d="M6 8V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v4" />
    </svg>
  )
}

function UserPlusIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <line x1="19" y1="8" x2="19" y2="14" />
      <line x1="22" y1="11" x2="16" y2="11" />
    </svg>
  )
}

function ClipboardListIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <rect x="8" y="2" width="8" height="4" rx="1" ry="1" />
      <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
      <path d="M12 11h4" />
      <path d="M12 16h4" />
      <path d="M8 11h.01" />
      <path d="M8 16h.01" />
    </svg>
  )
}

export function IPDDashboard() {
  const { user } = useAuth()

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h2 className={shared.pageTitle}>IPD — Inpatient</h2>
        <p className={shared.pageSubtitle}>Admit patients, allocate beds, transfer, and discharge</p>
      </div>

      <div className={shared.statsRow}>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.primary}`}>
            <BedIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>—</span>
            <span className={shared.statLabel}>Occupied Beds</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.success}`}>
            <ClipboardListIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>—</span>
            <span className={shared.statLabel}>Active Admissions</span>
          </div>
        </div>
      </div>

      <div className={shared.welcomeCard}>
        <p className={shared.welcomeTitle}>Inpatient Department</p>
        <p className={shared.welcomeText}>
          Signed in as {user?.username ?? 'Guest'}. Admit patients, allocate beds, transfer, and discharge.
        </p>
      </div>

      <div className={shared.cardsGrid}>
        <Link to="/ipd/admit" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <UserPlusIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Admit Patient</span>
            <span className={shared.actionCardDesc}>OPD referral, Emergency, or Direct admission with bed allocation</span>
          </div>
        </Link>
        <Link to="/ipd/beds" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <BedIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Bed Availability</span>
            <span className={shared.actionCardDesc}>View wards and available beds</span>
          </div>
        </Link>
        <Link to="/ipd/admissions" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <ClipboardListIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>IPD Patient List</span>
            <span className={shared.actionCardDesc}>Search admissions, transfer or discharge</span>
          </div>
        </Link>
      </div>
    </div>
  )
}
