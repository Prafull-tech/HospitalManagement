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

export const billingApi = {
  getAccount(ipdAdmissionId: number): Promise<BillingAccountView> {
    return apiClient.get(`${BASE}/account/${ipdAdmissionId}`).then((res) => res.data)
  },

  /** Alias: GET /api/billing/ipd/{ipdId} */
  getIpdBilling(ipdAdmissionId: number): Promise<BillingAccountView> {
    return apiClient.get(`${BASE}/ipd/${ipdAdmissionId}`).then((res) => res.data)
  },

  getItems(ipdAdmissionId: number): Promise<BillingItemResponse[]> {
    return apiClient.get(`${BASE}/account/${ipdAdmissionId}/items`).then((res) => res.data)
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
}
