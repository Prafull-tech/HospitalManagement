export type BillStatus = 'ACTIVE' | 'CLOSED'

export type BillingServiceType =
  | 'BED'
  | 'PHARMACY'
  | 'LAB'
  | 'OT'
  | 'RADIOLOGY'
  | 'CONSULTATION'
  | 'NURSING'
  | 'BLOOD_BANK'
  | 'PHYSIOTHERAPY'
  | 'EMERGENCY'
  | 'PROCEDURE'
  | 'OTHER'

export interface BillingItemResponse {
  id: number
  billingAccountId: number
  serviceType: BillingServiceType
  serviceName: string
  referenceId?: number
  quantity: number
  unitPrice: number
  totalPrice: number
  department?: string
  createdBy?: string
  status: string
  createdAt: string
}

export interface BillingAccountView {
  id: number
  patientId: number
  uhid: string
  patientName: string
  ipdAdmissionId?: number
  admissionNumber?: string
  opdVisitId?: number
  billStatus: BillStatus
  totalAmount: number
  paidAmount: number
  pendingAmount: number
  insuranceType?: string
  tpaApprovalStatus?: string
  totalByServiceType?: Record<BillingServiceType, number>
  items: BillingItemResponse[]
}
