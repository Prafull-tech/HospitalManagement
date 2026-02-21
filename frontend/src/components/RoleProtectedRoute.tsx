/**
 * Role-based route protection.
 * Redirects to /unauthorized (403) if user lacks permission for the route.
 * Used for /pharmacy, /lab, /radiology, /bloodbank - modules with strict RBAC.
 */

import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { hasRouteAccess } from '../config/menuConfig'
import type { HMSRole } from '../config/menuConfig'

interface RoleProtectedRouteProps {
  children: React.ReactNode
}

/**
 * Protects a route by checking if the user's role has access to current pathname.
 * If not allowed: redirect to /unauthorized (403).
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
  // Normalize: LAB_TECHNICIAN can access lab (treat as LAB_TECH)
  const normalizedRoles = userRoles.includes('LAB_TECHNICIAN')
    ? [...userRoles, 'LAB_TECH' as HMSRole]
    : userRoles

  if (!hasRouteAccess(normalizedRoles, currentPath)) {
    return (
      <Navigate
        to="/unauthorized"
        state={{ from: location, attemptedPath: currentPath }}
        replace
      />
    )
  }

  return <>{children}</>
}
