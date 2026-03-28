import { apiClient } from './client'
import type { BillingAccountView, BillingItemResponse, PaymentRequest } from '../types/billing'

const BASE = '/billing'

export interface TpaPreauthRequest {
  ipdAdmissionId: number
  estimatedAmount: number
  insurerName?: string
  policyNumber?: string
  memberId?: string
}

export interface TpaPreauthResponse {
  ipdAdmissionId: number
  approvalNumber: string
  status: string
  message?: string
}

export interface RefundRequest {
  paymentId: number
  amount: number
  reason?: string
}

export interface BillingDashboardSummary {
  date: string
  todayCollection: number
  paymentCountToday: number
  totalPendingActiveAccounts: number
}

export const billingApi = {
  getAccount(ipdAdmissionId: number): Promise<BillingAccountView> {
    return apiClient.get(`${BASE}/account/${ipdAdmissionId}`).then((res) => res.data)
  },

  /** GET /api/billing/account/opd/{opdVisitId} */
  getOpdAccount(opdVisitId: number): Promise<BillingAccountView> {
    return apiClient.get(`${BASE}/account/opd/${opdVisitId}`).then((res) => res.data)
  },

  /** Alias: GET /api/billing/ipd/{ipdId} */
  getIpdBilling(ipdAdmissionId: number): Promise<BillingAccountView> {
    return apiClient.get(`${BASE}/ipd/${ipdAdmissionId}`).then((res) => res.data)
  },

  getItems(ipdAdmissionId: number): Promise<BillingItemResponse[]> {
    return apiClient.get(`${BASE}/account/${ipdAdmissionId}/items`).then((res) => res.data)
  },

  getOpdItems(opdVisitId: number): Promise<BillingItemResponse[]> {
    return apiClient.get(`${BASE}/account/opd/${opdVisitId}/items`).then((res) => res.data)
  },

  getDashboardSummary(date?: string): Promise<BillingDashboardSummary> {
    return apiClient.get(`${BASE}/dashboard/summary`, { params: date ? { date } : {} }).then((res) => res.data)
  },

  recordPayment(request: PaymentRequest): Promise<BillingAccountView> {
    return apiClient.post(`${BASE}/payment`, request).then((res) => res.data)
  },

  finalize(ipdAdmissionId: number, paymentAmount?: number): Promise<unknown> {
    return apiClient
      .post(`${BASE}/finalize/${ipdAdmissionId}`, null, {
        params: paymentAmount != null ? { paymentAmount } : {},
      })
      .then((res) => res.data)
  },

  tpaPreauth(request: TpaPreauthRequest): Promise<TpaPreauthResponse> {
    return apiClient.post(`${BASE}/tpa/preauth`, request).then((res) => res.data)
  },

  refund(request: RefundRequest): Promise<BillingAccountView> {
    return apiClient.post(`${BASE}/refund`, request).then((res) => res.data)
  },

  /** GET /api/billing/transactions — list payments in date range (for reception dashboard). */
  listTransactions(params: { from?: string; to?: string; page?: number; size?: number }): Promise<{
    content: BillingTransactionItem[]
    totalElements: number
    totalPages: number
    number: number
  }> {
    return apiClient.get(`${BASE}/transactions`, { params }).then((res) => res.data)
  },
}

export interface BillingTransactionItem {
  id: number
  ipdAdmissionId: number
  admissionNumber?: string
  patientName?: string
  patientUhid?: string
  service?: string
  amount: number
  mode: string
  createdAt: string
}
