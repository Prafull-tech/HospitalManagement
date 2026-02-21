/**
 * Centralized Pharmacy permission configuration.
 * Must match backend @PreAuthorize annotations exactly.
 * No hardcoded role checks in JSX - use usePharmacyPermissions().
 */

import type { Role } from '../contexts/AuthContext'

/** Add, Edit, Import, Disable medicines; Purchase (Stock In); Rack management */
export const MASTER_ROLES: Role[] = ['ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE']

/** Sell (Stock Out); Issue indents */
export const SELL_ROLES: Role[] = ['ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST']
