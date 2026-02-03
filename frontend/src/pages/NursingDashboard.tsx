import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

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
    <div className="d-flex flex-column gap-3">
      <div>
        <h2 className="h5 mb-1 fw-bold">Nursing</h2>
        <p className="text-muted small mb-0">Staff, assignments, vitals, MAR, and notes</p>
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-6">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <UserPlusIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">—</div>
                <div className="small text-muted">Nursing Staff</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-info bg-opacity-10 p-2 text-info">
                <ActivityIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">—</div>
                <div className="small text-muted">Vitals Today</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm bg-light">
        <div className="card-body py-3">
          <p className="fw-semibold mb-1">Nursing Department</p>
          <p className="text-muted small mb-0">
            Signed in as {user?.username ?? 'Guest'}. Manage staff, assign nurses, record vitals, MAR, and nursing notes.
          </p>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-12 col-sm-6 col-lg-4">
          <Link to="/nursing/staff" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <UserPlusIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Nursing Staff</span>
                <span className="small text-muted">Create and manage nursing staff</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-sm-6 col-lg-4">
          <Link to="/nursing/assign" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <ClipboardListIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Assign Nurse</span>
                <span className="small text-muted">Assign nurse to IPD admission (ward/shift)</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-sm-6 col-lg-4">
          <Link to="/nursing/vitals" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <ActivityIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Vital Signs</span>
                <span className="small text-muted">Record vitals and view history</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-sm-6 col-lg-4">
          <Link to="/nursing/medications" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <PillIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Medication (MAR)</span>
                <span className="small text-muted">Record medication administration</span>
              </div>
            </div>
          </Link>
        </div>
        <div className="col-12 col-sm-6 col-lg-4">
          <Link to="/nursing/notes" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <FileTextIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Nursing Notes</span>
                <span className="small text-muted">Shift notes and care plan</span>
              </div>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
