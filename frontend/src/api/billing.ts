import { apiClient } from './client'
import type { BillingAccountView } from '../types/billing'

const BASE = '/billing'

export const billingApi = {
  getAccount(ipdAdmissionId: number): Promise<BillingAccountView> {
    return apiClient.get(`${BASE}/account/${ipdAdmissionId}`).then((res) => res.data)
  },

  finalize(ipdAdmissionId: number, paymentAmount?: number): Promise<unknown> {
    return apiClient
      .post(`${BASE}/finalize/${ipdAdmissionId}`, null, {
        params: paymentAmount != null ? { paymentAmount } : {},
      })
      .then((res) => res.data)
  },
}
