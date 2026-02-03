/** IPD admission status (matches backend enum). */
export type AdmissionStatus =
  | 'ADMITTED'
  | 'ACTIVE'
  | 'TRANSFERRED'
  | 'DISCHARGE_INITIATED'
  | 'DISCHARGED'
  | 'CANCELLED'
  | 'REFERRED'
  | 'LAMA'
  | 'EXPIRED'

/** IPD admission type (source). */
export type AdmissionType = 'OPD_REFERRAL' | 'EMERGENCY' | 'DIRECT'

/** Ward type (matches ward module enum). */
export type WardType =
  | 'GENERAL'
  | 'SEMI_PRIVATE'
  | 'PRIVATE'
  | 'ICU'
  | 'CCU'
  | 'NICU'
  | 'HDU'
  | 'EMERGENCY'

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
  wardType?: WardType
  available: boolean
  selectableForAdmission?: boolean
  bedStatusDisplay?: string
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
  diagnosis?: string
  depositAmount?: number
  insuranceTpa?: string
  admissionFormDocumentRef?: string
  consentFormDocumentRef?: string
  idProofDocumentRef?: string
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
  admissionDateTime: string
  diagnosis: string
  wardType?: WardType
  opdVisitId?: number
  remarks?: string
  depositAmount?: number
  insuranceTpa?: string
  admissionFormDocumentRef?: string
  consentFormDocumentRef?: string
  idProofDocumentRef?: string
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
