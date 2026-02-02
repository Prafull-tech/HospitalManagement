/**
 * IPD Beds – types for availability, filters, and role-based actions.
 * Aligns with backend BedAvailabilityResponseDto and BedStatus.
 * Status categories per SOP: Vacant, Occupied, Reserved, Cleaning, Maintenance, Isolation.
 */

export type BedStatus =
  | 'AVAILABLE'   // Vacant – bed is free
  | 'OCCUPIED'   // Patient admitted
  | 'RESERVED'   // Blocked for surgery/emergency
  | 'CLEANING'   // Under housekeeping
  | 'MAINTENANCE' // Under repair
  | 'ISOLATION'  // Infection control

export type WardType = 'GENERAL' | 'SEMI_PRIVATE' | 'PRIVATE' | 'ICU' | 'CCU' | 'NICU' | 'HDU' | 'EMERGENCY'

/** Display labels for bed status (NABH / SOP terminology). */
export const BED_STATUS_LABELS: Record<BedStatus, string> = {
  AVAILABLE: 'Vacant',
  OCCUPIED: 'Occupied',
  RESERVED: 'Reserved',
  CLEANING: 'Cleaning',
  MAINTENANCE: 'Maintenance',
  ISOLATION: 'Isolation',
}

export interface BedAvailabilityItem {
  bedId: number
  bedNumber: string
  wardId: number
  wardName: string
  wardCode: string
  wardType?: WardType
  roomId?: number
  roomNumber?: string
  bedStatus: BedStatus
  available: boolean
  updatedAt?: string
  patientName?: string
  patientUhid?: string
  admissionNumber?: string
  admissionId?: number
}

export interface BedFiltersParams {
  wardId?: number
  wardType?: WardType
  wardName?: string
  bedStatus?: BedStatus
  floor?: string
  search?: string
}

export interface SummaryCounts {
  total: number
  available: number   // Vacant
  occupied: number
  reserved: number
  cleaning: number
  maintenance: number
  isolation: number
}

/** Ward-wise census row: Ward Type | Total | Occupied | Vacant | Reserved | Under Cleaning | etc. */
export interface WardCensusRow {
  wardType: WardType
  wardTypeLabel: string
  total: number
  occupied: number
  vacant: number
  reserved: number
  cleaning: number
  maintenance: number
  isolation: number
}

/** Roles that can perform each action. ADMIN has all. */
export const BED_ACTION_ROLES: Record<BedAction, string[]> = {
  viewDetails: ['ADMIN', 'IPD_MANAGER', 'NURSING_SUPERINTENDENT', 'WARD_MANAGER', 'DOCTOR', 'HELP_DESK', 'NURSE', 'RECEPTIONIST'],
  allocateBed: ['ADMIN', 'IPD_MANAGER'],
  changeStatus: ['ADMIN', 'IPD_MANAGER', 'NURSING_SUPERINTENDENT', 'WARD_MANAGER', 'NURSE'],
  markMaintenance: ['ADMIN'],
  transferPatient: ['ADMIN', 'IPD_MANAGER'],
}

export type BedAction = 'viewDetails' | 'allocateBed' | 'changeStatus' | 'markMaintenance' | 'transferPatient'

export type HMSRoleForBeds =
  | 'ADMIN'
  | 'IPD_MANAGER'
  | 'NURSING_SUPERINTENDENT'
  | 'WARD_MANAGER'
  | 'DOCTOR'
  | 'HELP_DESK'
  | 'NURSE'
  | 'RECEPTIONIST'

export function canPerformBedAction(
  userRoles: string[],
  action: BedAction
): boolean {
  if (userRoles.includes('ADMIN')) return true
  const allowed = BED_ACTION_ROLES[action] ?? []
  return allowed.some((r) => userRoles.includes(r))
}
