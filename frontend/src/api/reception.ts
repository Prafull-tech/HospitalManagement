import { apiClient } from './client'
import type { PatientRequest, PatientResponse } from '../types/patient'

const BASE = '/reception/patients'

export const receptionApi = {
  register(data: PatientRequest): Promise<PatientResponse> {
    return apiClient.post(BASE, data).then((res) => res.data)
  },

  getById(id: number): Promise<PatientResponse> {
    return apiClient.get(`${BASE}/by-id/${id}`).then((res) => res.data)
  },

  update(id: number, data: PatientRequest): Promise<PatientResponse> {
    return apiClient.put(`${BASE}/by-id/${id}`, data).then((res) => res.data)
  },

  getByUhid(uhid: string): Promise<PatientResponse> {
    return apiClient.get(`${BASE}/${encodeURIComponent(uhid)}`).then((res) => res.data)
  },

  /** List all patients (paginated). Default page=0, size=500. */
  list(params?: { page?: number; size?: number }): Promise<PatientResponse[]> {
    return apiClient
      .get(BASE, { params: { page: params?.page ?? 0, size: params?.size ?? 500 } })
      .then((res) => res.data)
  },

  /** Search by single query: ID, UHID, mobile/phone, or name. */
  searchQuery(q: string): Promise<PatientResponse[]> {
    if (!q?.trim()) return Promise.resolve([])
    return apiClient.get(`${BASE}/search`, { params: { q: q.trim() } }).then((res) => res.data)
  },

  search(params: { uhid?: string; phone?: string; name?: string }): Promise<PatientResponse[]> {
    return apiClient
      .get(`${BASE}/search`, { params })
      .then((res) => res.data)
  },
}
