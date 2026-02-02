/** OPD visit status (matches backend enum). */
export type VisitStatus =
  | 'REGISTERED'
  | 'IN_CONSULTATION'
  | 'COMPLETED'
  | 'REFERRED'
  | 'CANCELLED'

export interface OPDClinicalNoteResponse {
  id: number
  chiefComplaint?: string
  provisionalDiagnosis?: string
  doctorRemarks?: string
  createdAt: string
  updatedAt: string
}

export interface OPDVisitResponse {
  id: number
  visitNumber: string
  patientUhid: string
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  doctorCode: string
  departmentId: number
  departmentName: string
  visitDate: string
  visitStatus: VisitStatus
  tokenNumber?: number
  referredToDepartmentId?: number
  referredToDoctorId?: number
  referToIpd?: boolean
  referralRemarks?: string
  clinicalNote?: OPDClinicalNoteResponse
  createdAt: string
  updatedAt: string
}

export interface OPDVisitRequest {
  patientUhid: string
  doctorId: number
  visitDate: string
}

export interface OPDClinicalNoteRequest {
  chiefComplaint?: string
  provisionalDiagnosis?: string
  doctorRemarks?: string
}

export interface OPDStatusRequest {
  status: VisitStatus
}

export interface OPDReferRequest {
  referredToDepartmentId?: number
  referredToDoctorId?: number
  referToIpd?: boolean
  referralRemarks?: string
}

export interface OPDPageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
