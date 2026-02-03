import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

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
    <div className="d-flex flex-column gap-3">
      <div>
        <h2 className="h5 mb-1 fw-bold">OPD — Outpatient</h2>
        <p className="text-muted small mb-0">Register visits, manage queue, and complete consultations</p>
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-6">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <ClipboardListIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">—</div>
                <div className="small text-muted">Today&apos;s Visits</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-success bg-opacity-10 p-2 text-success">
                <QueueIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">—</div>
                <div className="small text-muted">In Queue</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm bg-light">
        <div className="card-body py-3">
          <p className="fw-semibold mb-1">Outpatient Department</p>
          <p className="text-muted small mb-0">
            Signed in as {user?.username ?? 'Guest'}. Register visits, manage queue, and complete consultations.
          </p>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-4">
          <Link to="/opd/register" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <UserPlusIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Register OPD Visit</span>
                <span className="small text-muted">Create visit for existing patient (UHID) and assign doctor</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-md-4">
          <Link to="/opd/queue" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <QueueIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Token &amp; Queue</span>
                <span className="small text-muted">View consultation queue by doctor and date</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-md-4">
          <Link to="/opd/visits" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <SearchIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Search Visits</span>
                <span className="small text-muted">Search OPD visits by date, doctor, status, UHID</span>
              </div>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
