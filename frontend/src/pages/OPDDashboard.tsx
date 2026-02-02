import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import shared from '../styles/Dashboard.module.css'

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

function QueueIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M4 6h16" />
      <path d="M4 12h16" />
      <path d="M4 18h16" />
    </svg>
  )
}

function SearchIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35" />
    </svg>
  )
}

export function OPDDashboard() {
  const { user } = useAuth()

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h2 className={shared.pageTitle}>OPD — Outpatient</h2>
        <p className={shared.pageSubtitle}>Register visits, manage queue, and complete consultations</p>
      </div>

      <div className={shared.statsRow}>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.primary}`}>
            <ClipboardListIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>—</span>
            <span className={shared.statLabel}>Today&apos;s Visits</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.success}`}>
            <QueueIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>—</span>
            <span className={shared.statLabel}>In Queue</span>
          </div>
        </div>
      </div>

      <div className={shared.welcomeCard}>
        <p className={shared.welcomeTitle}>Outpatient Department</p>
        <p className={shared.welcomeText}>
          Signed in as {user?.username ?? 'Guest'}. Register visits, manage queue, and complete consultations.
        </p>
      </div>

      <div className={shared.cardsGrid}>
        <Link to="/opd/register" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <UserPlusIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Register OPD Visit</span>
            <span className={shared.actionCardDesc}>Create visit for existing patient (UHID) and assign doctor</span>
          </div>
        </Link>
        <Link to="/opd/queue" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <QueueIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Token & Queue</span>
            <span className={shared.actionCardDesc}>View consultation queue by doctor and date</span>
          </div>
        </Link>
        <Link to="/opd/visits" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <SearchIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Search Visits</span>
            <span className={shared.actionCardDesc}>Search OPD visits by date, doctor, status, UHID</span>
          </div>
        </Link>
      </div>
    </div>
  )
}
