/**
 * Redirects to the user's default dashboard based on role.
 * Pharmacy users → /pharmacy, Lab users → /lab, etc.
 */

import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getDefaultDashboardForUser } from '../config/menuConfig'
import type { HMSRole } from '../config/menuConfig'

export function DashboardRedirect() {
  const { user } = useAuth()
  const userRoles: HMSRole[] = user?.roles?.length ? (user.roles as HMSRole[]) : []
  const defaultDashboard = getDefaultDashboardForUser(userRoles)
  return <Navigate to={defaultDashboard} replace />
}
