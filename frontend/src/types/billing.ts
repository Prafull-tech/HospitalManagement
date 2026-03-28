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

export interface PaymentRequest {
  /** Exactly one of ipdId, opdVisitId, or billingAccountId should be set. */
  ipdId?: number
  opdVisitId?: number
  billingAccountId?: number
  amount: number
  mode: 'Cash' | 'Card' | 'UPI'
  referenceNo?: string
}

export interface BillingItemResponse {
  id: number
  billingAccountId: number
  serviceType: BillingServiceType
  serviceName: string
  referenceId?: number
  quantity: number
  unitPrice: number
  totalPrice: number
  gstPercent?: number
  cgst?: number
  sgst?: number
  igst?: number
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
  /** OPD visit number when this is an OPD bill */
  visitNumber?: string
  billStatus: BillStatus
  totalAmount: number
  paidAmount: number
  pendingAmount: number
  insuranceType?: string
  tpaApprovalStatus?: string
  totalByServiceType?: Record<BillingServiceType, number>
  items: BillingItemResponse[]

  corporate?: boolean
  corporateApproved?: boolean
  emiActive?: boolean
  hasGstSplit?: boolean
}
