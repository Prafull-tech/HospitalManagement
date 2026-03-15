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

  /** List all patients (paginated). Optional from/to filter by registration date range. */
  list(params?: { page?: number; size?: number; from?: string; to?: string }): Promise<PatientResponse[]> {
    return apiClient
      .get(BASE, { params: { page: params?.page ?? 0, size: params?.size ?? 500, from: params?.from, to: params?.to } })
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

  disable(id: number): Promise<PatientResponse> {
    return apiClient.patch(`${BASE}/by-id/${id}/disable`).then((res) => res.data)
  },

  enable(id: number): Promise<PatientResponse> {
    return apiClient.patch(`${BASE}/by-id/${id}/enable`).then((res) => res.data)
  },
}
