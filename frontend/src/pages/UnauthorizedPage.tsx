import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getDefaultDashboardForUser } from '../config/menuConfig'
import type { HMSRole } from '../config/menuConfig'

export function UnauthorizedPage() {
  const { user } = useAuth()
  const userRoles: HMSRole[] = user?.roles?.length ? (user.roles as HMSRole[]) : []
  const dashboardPath = getDefaultDashboardForUser(userRoles)

  return (
    <div className="d-flex flex-column align-items-center justify-content-center min-vh-100 p-4 text-center">
      <div className="card shadow-sm" style={{ maxWidth: '420px' }}>
        <div className="card-body p-4">
          <h1 className="h4 text-danger fw-bold mb-2">403 – Access denied</h1>
          <p className="text-muted mb-3">You do not have permission to view this page.</p>
          <Link to={dashboardPath} className="btn btn-primary">Back to Dashboard</Link>
        </div>
      </div>
    </div>
  )
}
