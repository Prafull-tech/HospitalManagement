import { Navigate, useLocation } from 'react-router-dom'
import { useAuth, type Role } from '../contexts/AuthContext'

interface ProtectedRouteProps {
  children: React.ReactNode
  allowedRoles?: Role[]
}

export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, hasRole, user } = useAuth()
  const location = useLocation()
  console.log('ProtectedRoute loading: N/A (no loading state)')
  console.log('ProtectedRoute user:', user?.username ?? 'null')

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (allowedRoles && allowedRoles.length > 0 && !hasRole(...allowedRoles)) {
    return <Navigate to="/unauthorized" replace />
  }

  return <>{children}</>
}
