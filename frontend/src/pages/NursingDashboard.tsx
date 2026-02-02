import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import shared from '../styles/Dashboard.module.css'

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

function ActivityIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
    </svg>
  )
}

function PillIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="m10.5 20.5 10-10a4.95 4.95 0 1 0-7-7l-10 10a4.95 4.95 0 1 0 7 7Z" />
      <path d="m8.5 8.5 7 7" />
    </svg>
  )
}

function FileTextIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
      <path d="M14 2v6h6" />
      <path d="M16 13H8" />
      <path d="M16 17H8" />
      <path d="M10 9H8" />
    </svg>
  )
}

export function NursingDashboard() {
  const { user } = useAuth()

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h2 className={shared.pageTitle}>Nursing</h2>
        <p className={shared.pageSubtitle}>Staff, assignments, vitals, MAR, and notes</p>
      </div>

      <div className={shared.statsRow}>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.primary}`}>
            <UserPlusIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>—</span>
            <span className={shared.statLabel}>Nursing Staff</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.info}`}>
            <ActivityIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>—</span>
            <span className={shared.statLabel}>Vitals Today</span>
          </div>
        </div>
      </div>

      <div className={shared.welcomeCard}>
        <p className={shared.welcomeTitle}>Nursing Department</p>
        <p className={shared.welcomeText}>
          Signed in as {user?.username ?? 'Guest'}. Manage staff, assign nurses, record vitals, MAR, and nursing notes.
        </p>
      </div>

      <div className={shared.cardsGrid}>
        <Link to="/nursing/staff" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <UserPlusIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Nursing Staff</span>
            <span className={shared.actionCardDesc}>Create and manage nursing staff</span>
          </div>
        </Link>
        <Link to="/nursing/assign" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <ClipboardListIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Assign Nurse</span>
            <span className={shared.actionCardDesc}>Assign nurse to IPD admission (ward/shift)</span>
          </div>
        </Link>
        <Link to="/nursing/vitals" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <ActivityIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Vital Signs</span>
            <span className={shared.actionCardDesc}>Record vitals and view history</span>
          </div>
        </Link>
        <Link to="/nursing/medications" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <PillIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Medication (MAR)</span>
            <span className={shared.actionCardDesc}>Record medication administration</span>
          </div>
        </Link>
        <Link to="/nursing/notes" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <FileTextIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Nursing Notes</span>
            <span className={shared.actionCardDesc}>Shift notes and care plan</span>
          </div>
        </Link>
      </div>
    </div>
  )
}
