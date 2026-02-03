import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

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
    <div className="d-flex flex-column gap-3">
      <div>
        <h2 className="h5 mb-1 fw-bold">IPD — Inpatient</h2>
        <p className="text-muted small mb-0">Admit patients, allocate beds, transfer, and discharge</p>
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-6">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <BedIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">—</div>
                <div className="small text-muted">Occupied Beds</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-success bg-opacity-10 p-2 text-success">
                <ClipboardListIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">—</div>
                <div className="small text-muted">Active Admissions</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm bg-light">
        <div className="card-body py-3">
          <p className="fw-semibold mb-1">Inpatient Department</p>
          <p className="text-muted small mb-0">
            Signed in as {user?.username ?? 'Guest'}. Admit patients, allocate beds, transfer, and discharge.
          </p>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-4">
          <Link to="/ipd/admit" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <UserPlusIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Admit Patient</span>
                <span className="small text-muted">OPD referral, Emergency, or Direct admission with bed allocation</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-md-4">
          <Link to="/ipd/beds" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <BedIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Bed Availability</span>
                <span className="small text-muted">View wards and available beds</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-md-4">
          <Link to="/ipd/admissions" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <ClipboardListIcon />
              </span>
              <div>
                <span className="fw-bold d-block">IPD Patient List</span>
                <span className="small text-muted">Search admissions, transfer or discharge</span>
              </div>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
