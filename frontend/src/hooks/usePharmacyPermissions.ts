import { useAuth } from '../contexts/AuthContext'
import { MASTER_ROLES, SELL_ROLES } from '../config/pharmacyPermissions'

/**
 * Centralized pharmacy permission helper.
 * Matches backend @PreAuthorize exactly.
 * Use this hook - no hardcoded role checks in JSX.
 */
export function usePharmacyPermissions() {
  const { hasRole } = useAuth()

  const canManageMedicineMaster = hasRole(...MASTER_ROLES)
  const canSell = hasRole(...SELL_ROLES)

  return {
    canAddMedicine: canManageMedicineMaster,
    canImportMedicines: canManageMedicineMaster,
    canEditMedicine: canManageMedicineMaster,
    canDisableMedicine: canManageMedicineMaster,
    canManageMedicineMaster,
    canPurchase: canManageMedicineMaster,
    canSell,
  }
}

