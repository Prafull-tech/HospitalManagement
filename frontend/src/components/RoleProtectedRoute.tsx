/**
 * Role-based route protection.
 * Redirects to /dashboard if user lacks permission for the route.
 * Used for /pharmacy, /lab, /radiology, /bloodbank - modules with strict RBAC.
 */

import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { hasRouteAccess } from '../config/menuConfig'
import { normalizeUserRoles } from '../config/menuFilter'
import type { HMSRole } from '../config/menuConfig'

interface RoleProtectedRouteProps {
  children: React.ReactNode
}

/**
 * Protects a route by checking if the user's role has access to current pathname.
 * If not allowed: redirect to /dashboard.
 */
export function RoleProtectedRoute({ children }: RoleProtectedRouteProps) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()
  const currentPath = location.pathname

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  const userRoles: HMSRole[] = user?.roles?.length
    ? (user.roles as HMSRole[])
    : []
  const normalizedRoles = normalizeUserRoles(userRoles)

  if (!hasRouteAccess(normalizedRoles, currentPath)) {
    return <Navigate to="/dashboard" replace />
  }

  return <>{children}</>
}
