/**
 * Types for View IPD Admission (read-only).
 * Aligned with GET /api/ipd/admissions/{id}/view response.
 */

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

export type AdmissionType = 'OPD_REFERRAL' | 'EMERGENCY' | 'DIRECT'

export interface ViewAdmissionAdmission {
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
  diagnosis?: string
  depositAmount?: number
  insuranceTpa?: string
  admissionFormDocumentRef?: string
  consentFormDocumentRef?: string
  idProofDocumentRef?: string
  dischargeRemarks?: string
  currentWardId?: number
  currentWardName?: string
  currentRoomNumber?: string
  currentBedId?: number
  currentBedNumber?: string
  createdAt: string
  updatedAt: string
  shiftedToWardAt?: string
  shiftedToWardBy?: string
}

export interface ViewAdmissionPatient {
  id: number
  uhid: string
  registrationNumber: string
  registrationDate?: string
  fullName: string
  dateOfBirth?: string
  age: number
  ageYears?: number
  ageMonths?: number
  ageDays?: number
  gender: string
  phone?: string
  address?: string
  state?: string
  city?: string
  district?: string
  fatherHusbandName?: string
}

export interface ViewAdmissionTimelineEvent {
  eventType: string
  timestamp: string
  title: string
  description: string
  sourceModule: string
  referenceId?: number
}

export interface ViewAdmissionBillingSummary {
  totalCharges: number
  totalDeposit: number
  chargeCount: number
  billingStatus: string
}

export interface ViewAdmissionResponse {
  admission: ViewAdmissionAdmission
  patient: ViewAdmissionPatient
  timeline: ViewAdmissionTimelineEvent[]
  billingSummary: ViewAdmissionBillingSummary
}
