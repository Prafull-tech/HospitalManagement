import { Link } from 'react-router-dom'

export function LabReportsPage() {
  return (
    <div className="d-flex flex-column gap-3">
      <div>
        <h2 className="h5 mb-1 fw-bold">View Reports</h2>
        <p className="text-muted small mb-0">Lab test reports and results.</p>
      </div>
      <div className="card shadow-sm">
        <div className="card-body">
          <p className="text-muted mb-0 small">Report listing and search will be implemented here.</p>
          <Link to="/lab" className="btn btn-outline-primary btn-sm mt-2">
            Back to Lab Dashboard
          </Link>
        </div>
      </div>
    </div>
  )
}
