export type DischargeType = 'NORMAL' | 'LAMA' | 'REFERRED' | 'EXPIRED'

export interface DischargePendingItem {
  id: number
  description: string
  status: string
}

export interface DischargeStatus {
  ipdAdmissionId: number
  admissionNumber: string
  patientId: number
  uhid: string
  patientName: string
  bedId?: number
  bedNumber?: string
  wardName?: string
  admittedDate: string
  dischargeDate?: string
  dischargeType: DischargeType
  admissionStatus: string

  doctorClearance: boolean
  nursingClearance: boolean
  pharmacyClearance: boolean
  labClearance: boolean
  billingClearance: boolean
  insuranceClearance: boolean
  housekeepingClearance: boolean
  linenClearance: boolean
  dietaryClearance: boolean

  pendingPharmacyCount: number
  pendingLabCount: number
  billingTotal?: number
  billingPendingAmount?: number
  billingPaid: boolean

  pendingPharmacy: DischargePendingItem[]
  pendingLab: DischargePendingItem[]

  allClearancesComplete: boolean
  canFinalizeDischarge: boolean

  diagnosisSummary?: string
  treatmentSummary?: string
  procedures?: string
  advice?: string
  followUp?: string
  medicinesOnDischarge?: string
}

export interface DischargeSummaryRequest {
  diagnosisSummary?: string
  treatmentSummary?: string
  procedures?: string
  advice?: string
  followUp?: string
  medicinesOnDischarge?: string
}
