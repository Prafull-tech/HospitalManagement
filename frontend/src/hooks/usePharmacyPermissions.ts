import { useAuth } from '../contexts/AuthContext'
import type { Role } from '../contexts/AuthContext'

/**
 * Centralised pharmacy permission helper.
 * Policy:
 * - PHARMACY_MANAGER, STORE_INCHARGE: can add/edit/disable medicines
 * - IPD_PHARMACIST: issue only, no medicine master changes
 * - Others (DOCTOR, NURSE, ADMIN, etc.): view-only
 */

const MASTER_ROLES: Role[] = ['PHARMACY_MANAGER', 'STORE_INCHARGE']

export function usePharmacyPermissions() {
  const { hasRole } = useAuth()

  const canManageMedicineMaster = hasRole(...MASTER_ROLES)

  return {
    canAddMedicine: canManageMedicineMaster,
    canEditMedicine: hasRole('PHARMACY_MANAGER'),
    canDisableMedicine: hasRole('PHARMACY_MANAGER'),
  }
}

