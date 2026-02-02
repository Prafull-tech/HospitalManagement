/** Nursing shift type (matches backend enum). */
export type ShiftType = 'MORNING' | 'EVENING' | 'NIGHT'

/** Nursing note status (matches backend enum). */
export type NoteStatus = 'DRAFT' | 'LOCKED'

/** Ward type for nursing notes (matches backend). */
export type WardTypeNursing = 'GENERAL' | 'SEMI_PRIVATE' | 'PRIVATE' | 'ICU' | 'CCU' | 'NICU' | 'HDU' | 'EMERGENCY'

/** Nursing staff role (matches backend enum). */
export type NurseRole =
  | 'CHIEF_NURSING_OFFICER'
  | 'NURSING_SUPERINTENDENT'
  | 'WARD_INCHARGE'
  | 'STAFF_NURSE'
  | 'NURSING_AIDE'

export interface NursingStaffResponse {
  id: number
  code: string
  fullName: string
  nurseRole: NurseRole
  phone?: string
  email?: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface NursingStaffRequest {
  code: string
  fullName: string
  nurseRole: NurseRole
  phone?: string
  email?: string
  isActive?: boolean
}

export interface NurseAssignmentResponse {
  id: number
  nursingStaffId: number
  nursingStaffName: string
  nursingStaffCode: string
  ipdAdmissionId: number
  admissionNumber: string
  shiftType: ShiftType
  assignmentDate: string
  assignedAt?: string
  remarks?: string
  createdAt: string
}

export interface NurseAssignmentRequest {
  nursingStaffId: number
  ipdAdmissionId: number
  shiftType: ShiftType
  assignmentDate: string
  remarks?: string
}

export interface VitalSignResponse {
  id: number
  ipdAdmissionId: number
  recordedAt: string
  bloodPressureSystolic?: number
  bloodPressureDiastolic?: number
  pulse?: number
  temperature?: number
  spo2?: number
  respiration?: number
  recordedById?: number
  recordedByName?: string
  remarks?: string
  createdAt: string
}

export interface VitalSignRequest {
  ipdAdmissionId: number
  recordedAt?: string
  bloodPressureSystolic?: number
  bloodPressureDiastolic?: number
  pulse?: number
  temperature?: number
  spo2?: number
  respiration?: number
  recordedById?: number
  remarks?: string
}

export interface NursingNoteResponse {
  id: number
  ipdAdmissionId: number
  admissionNumber?: string
  shiftType: ShiftType
  noteType: string
  content: string
  recordedAt: string
  recordedById?: number
  recordedByName?: string
  noteStatus: NoteStatus
  lockedAt?: string
  lockedById?: number
  lockedByName?: string
  criticalFlags?: string
  wardType?: WardTypeNursing
  wardName?: string
  bedNumber?: string
  patientName?: string
  patientUhid?: string
  createdAt: string
  updatedAt?: string
}

export interface NursingNoteRequest {
  ipdAdmissionId: number
  shiftType: ShiftType
  noteType: string
  content: string
  recordedAt?: string
  recordedById?: number
  criticalFlags?: string
}

export interface NursingNoteSearchParams {
  patientName?: string
  patientUhid?: string
  bedNumber?: string
  wardType?: WardTypeNursing
  recordedDateFrom?: string
  recordedDateTo?: string
  shiftType?: ShiftType
  noteStatus?: NoteStatus
  page?: number
  size?: number
}

export interface NursingNotePageResponse {
  content: NursingNoteResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface MedicationAdministrationResponse {
  id: number
  ipdAdmissionId: number
  medicationName: string
  dosage?: string
  route?: string
  administeredAt: string
  administeredById?: number
  administeredByName?: string
  doctorOrderRef?: string
  remarks?: string
  createdAt: string
}

export interface MedicationAdministrationRequest {
  ipdAdmissionId: number
  medicationName: string
  dosage?: string
  route?: string
  administeredAt?: string
  administeredById?: number
  doctorOrderRef?: string
  remarks?: string
}
