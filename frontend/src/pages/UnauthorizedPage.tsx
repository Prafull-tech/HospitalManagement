import { Link } from 'react-router-dom'

export function UnauthorizedPage() {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center min-vh-100 p-4 text-center">
      <div className="card shadow-sm" style={{ maxWidth: '420px' }}>
        <div className="card-body p-4">
          <h1 className="h4 text-danger fw-bold mb-2">Access denied</h1>
          <p className="text-muted mb-3">You do not have permission to view this page.</p>
          <Link to="/reception" className="btn btn-primary">Back to Reception</Link>
        </div>
      </div>
    </div>
  )
}
