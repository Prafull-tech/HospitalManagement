/**
 * Hospital-wise Bed Availability – types for APIs and UI.
 * Aligns with backend Hospital, BedAvailabilityRequestDto, BedAvailabilityResponseDto.
 * Data format: Ward Type | Total Beds | Occupied | Vacant | Reserved | Under Cleaning
 */

export type WardType =
  | 'GENERAL'
  | 'SEMI_PRIVATE'
  | 'PRIVATE'
  | 'ICU'
  | 'CCU'
  | 'NICU'
  | 'HDU'
  | 'EMERGENCY'

export const WARD_TYPE_LABELS: Record<WardType, string> = {
  GENERAL: 'General',
  SEMI_PRIVATE: 'Semi Private',
  PRIVATE: 'Private',
  ICU: 'ICU',
  CCU: 'CCU',
  NICU: 'NICU',
  HDU: 'HDU',
  EMERGENCY: 'Emergency',
}

export interface HospitalItem {
  id: number
  hospitalCode: string
  hospitalName: string
  location?: string
  active: boolean
}

export interface WardTypeMasterItem {
  id: number
  name: string
  active: boolean
}

/** GET response shape: Ward Type | Total Beds | Occupied | Vacant | Reserved | Under Cleaning */
export interface BedAvailabilityItem {
  id: number
  hospitalId: number
  wardType: string
  totalBeds: number
  occupied: number
  vacant: number
  reserved: number
  underCleaning: number
  createdAt?: string
  updatedAt?: string
}

export interface BedAvailabilityFormValues {
  wardType: string
  totalBeds: number
  occupiedBeds: number
  reservedBeds: number
  underCleaningBeds: number
}

/** Vacant = Total Beds - (Occupied + Reserved + Under Cleaning) */
export function computeVacant(values: {
  totalBeds: number
  occupiedBeds: number
  reservedBeds: number
  underCleaningBeds: number
}): number {
  const used = values.occupiedBeds + values.reservedBeds + values.underCleaningBeds
  return Math.max(0, values.totalBeds - used)
}

/** Validation: Occupied + Reserved + Under Cleaning <= Total Beds */
export function validateCounts(values: BedAvailabilityFormValues): string | null {
  const { totalBeds, occupiedBeds, reservedBeds, underCleaningBeds } = values
  if (totalBeds < 0 || occupiedBeds < 0 || reservedBeds < 0 || underCleaningBeds < 0) {
    return 'All counts must be >= 0'
  }
  if (occupiedBeds + reservedBeds + underCleaningBeds > totalBeds) {
    return 'Occupied + Reserved + Under Cleaning must be <= Total Beds'
  }
  return null
}

/** Roles that can edit/delete (CRUD). View-only for DOCTOR, HELP_DESK. */
export type BedAvailabilityRole =
  | 'SUPER_ADMIN'
  | 'ADMIN'
  | 'IPD_MANAGER'
  | 'NURSING_SUPERINTENDENT'
  | 'DOCTOR'
  | 'HELP_DESK'

export const CAN_EDIT_ROLES: BedAvailabilityRole[] = [
  'SUPER_ADMIN',
  'ADMIN',
  'IPD_MANAGER',
  'NURSING_SUPERINTENDENT',
]

export const CAN_DELETE_ROLES: BedAvailabilityRole[] = ['SUPER_ADMIN', 'ADMIN']

export function canEdit(role: string): boolean {
  return CAN_EDIT_ROLES.includes(role as BedAvailabilityRole)
}

export function canDelete(role: string): boolean {
  return CAN_DELETE_ROLES.includes(role as BedAvailabilityRole)
}
