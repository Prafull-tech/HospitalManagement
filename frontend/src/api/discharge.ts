import { apiClient } from './client'
import type { DischargeStatus, DischargeSummaryRequest, DischargeType } from '../types/discharge'

const BASE = '/ipd/discharge'
const BILLING = '/billing'

export const dischargeApi = {
  getStatus(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.get(`${BASE}/${ipdAdmissionId}/status`).then((res) => res.data)
  },

  recordDoctorClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/doctor-clearance`).then((res) => res.data)
  },

  recordNursingClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/nursing-clearance`).then((res) => res.data)
  },

  recordPharmacyClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/pharmacy-clearance`).then((res) => res.data)
  },

  recordLabClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/lab-clearance`).then((res) => res.data)
  },

  recordBillingClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/billing-clearance`).then((res) => res.data)
  },

  recordHousekeepingClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/housekeeping-clearance`).then((res) => res.data)
  },

  recordLinenClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/linen-clearance`).then((res) => res.data)
  },

  recordDietaryClearance(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/dietary-clearance`).then((res) => res.data)
  },

  recordInsuranceClearance(ipdAdmissionId: number, adminOverride = false): Promise<DischargeStatus> {
    return apiClient
      .post(`${BASE}/${ipdAdmissionId}/insurance-clearance`, null, {
        params: { adminOverride },
      })
      .then((res) => res.data)
  },

  saveSummary(ipdAdmissionId: number, body: DischargeSummaryRequest): Promise<DischargeStatus> {
    return apiClient.post(`${BASE}/${ipdAdmissionId}/summary`, body).then((res) => res.data)
  },

  finalizeDischarge(ipdAdmissionId: number, dischargeType?: DischargeType): Promise<DischargeStatus> {
    return apiClient
      .post(`${BASE}/${ipdAdmissionId}/finalize`, null, {
        params: dischargeType ? { dischargeType } : {},
      })
      .then((res) => res.data)
  },

  /** POST /api/billing/finalize/{ipdAdmissionId} — Finalize bill and set billing clearance. */
  finalizeBill(ipdAdmissionId: number): Promise<DischargeStatus> {
    return apiClient.post(`${BILLING}/finalize/${ipdAdmissionId}`).then((res) => res.data)
  },
}
