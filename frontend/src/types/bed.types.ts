/**
 * Bed types for ward pages. Single source of truth: hospital-beds API.
 * Aligns with GET /api/ipd/hospital-beds response (wardType, bedId, bedNumber, bedStatus, patientId, patientName, uhid, ipdAdmissionId).
 */

export type BedStatus =
  | 'AVAILABLE'   // Vacant
  | 'OCCUPIED'
  | 'RESERVED'
  | 'CLEANING'
  | 'MAINTENANCE'
  | 'ISOLATION'

export type WardType = 'GENERAL' | 'SEMI_PRIVATE' | 'PRIVATE' | 'ICU' | 'CCU' | 'NICU' | 'HDU' | 'EMERGENCY'

/** One bed from hospital-beds API (ipdAdmissionId = admissionId in API). */
export interface HospitalBedItem {
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
  patientId?: number
  patientName?: string
  patientUhid?: string
  uhid?: string
  admissionNumber?: string
  admissionId?: number
  ipdAdmissionId?: number
}

/** Summary counts derived from filtered hospital-beds (no duplicate state). */
export interface WardSummaryCounts {
  total: number
  occupied: number
  vacant: number
  reserved: number
  cleaning: number
  maintenance?: number
  isolation?: number
}

/** Display labels for bed status (NABH / SOP). */
export const BED_STATUS_LABELS: Record<BedStatus, string> = {
  AVAILABLE: 'Vacant',
  OCCUPIED: 'Occupied',
  RESERVED: 'Reserved',
  CLEANING: 'Cleaning',
  MAINTENANCE: 'Maintenance',
  ISOLATION: 'Isolation',
}

/** Roles for General Ward actions (spec: View=DOCTOR,NURSE; Add Note=NURSE; Transfer=IPD_MANAGER; Mark Cleaning/Vacant=NURSE,WARD_INCHARGE). */
export const GENERAL_WARD_ACTION_ROLES: Record<GeneralWardAction, string[]> = {
  viewPatient: ['ADMIN', 'IPD_MANAGER', 'DOCTOR', 'NURSE', 'WARD_INCHARGE', 'NURSING_SUPERINTENDENT', 'WARD_MANAGER'],
  addNursingNote: ['ADMIN', 'NURSE', 'WARD_INCHARGE', 'NURSING_SUPERINTENDENT', 'WARD_MANAGER'],
  transferPatient: ['ADMIN', 'IPD_MANAGER'],
  markCleaningOrVacant: ['ADMIN', 'NURSE', 'WARD_INCHARGE', 'IPD_MANAGER', 'NURSING_SUPERINTENDENT', 'WARD_MANAGER'],
}

export type GeneralWardAction = 'viewPatient' | 'addNursingNote' | 'transferPatient' | 'markCleaningOrVacant'

export function canPerformGeneralWardAction(userRoles: string[], action: GeneralWardAction): boolean {
  if (userRoles.includes('ADMIN')) return true
  const allowed = GENERAL_WARD_ACTION_ROLES[action] ?? []
  return allowed.some((r) => userRoles.includes(r))
}
