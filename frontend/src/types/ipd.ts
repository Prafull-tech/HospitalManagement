/** IPD admission status (matches backend enum). */
export type AdmissionStatus =
  | 'ADMITTED'
  | 'TRANSFERRED'
  | 'DISCHARGE_INITIATED'
  | 'DISCHARGED'
  | 'CANCELLED'

/** IPD admission type (source). */
export type AdmissionType = 'OPD_REFERRAL' | 'EMERGENCY' | 'DIRECT'

/** Ward type (matches ward module enum). */
export type WardType =
  | 'GENERAL'
  | 'PRIVATE'
  | 'ICU'
  | 'CCU'
  | 'NICU'
  | 'HDU'

export interface WardResponse {
  id: number
  code: string
  name: string
  wardType: WardType
  capacity?: number
  isActive: boolean
}

export interface BedAvailabilityResponse {
  bedId: number
  bedNumber: string
  wardId: number
  wardName: string
  wardCode: string
  available: boolean
}

export interface IPDAdmissionResponse {
  id: number
  admissionNumber: string
  patientUhid: string
  patientId: number
  patientName: string
  primaryDoctorId: number
  primaryDoctorName: string
  primaryDoctorCode: string
  admissionType: AdmissionType
  admissionStatus: AdmissionStatus
  admissionDateTime: string
  dischargeDateTime?: string
  opdVisitId?: number
  remarks?: string
  dischargeRemarks?: string
  currentWardId?: number
  currentWardName?: string
  currentBedId?: number
  currentBedNumber?: string
  createdAt: string
  updatedAt: string
}

export interface IPDAdmissionRequest {
  patientUhid: string
  primaryDoctorId: number
  admissionType: AdmissionType
  bedId: number
  opdVisitId?: number
  remarks?: string
}

export interface IPDTransferRequest {
  bedId: number
  remarks?: string
}

export interface IPDDischargeRequest {
  dischargeRemarks?: string
}

export interface IPDPageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
