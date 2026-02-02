import { apiClient } from './client'
import type { PatientRequest, PatientResponse } from '../types/patient'

const BASE = '/reception/patients'

export const receptionApi = {
  register(data: PatientRequest): Promise<PatientResponse> {
    return apiClient.post(BASE, data).then((res) => res.data)
  },

  getByUhid(uhid: string): Promise<PatientResponse> {
    return apiClient.get(`${BASE}/${encodeURIComponent(uhid)}`).then((res) => res.data)
  },

  search(params: { uhid?: string; phone?: string; name?: string }): Promise<PatientResponse[]> {
    return apiClient
      .get(`${BASE}/search`, { params })
      .then((res) => res.data)
  },
}
