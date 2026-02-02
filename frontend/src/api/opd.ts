import { apiClient } from './client'
import type {
  OPDVisitResponse,
  OPDVisitRequest,
  OPDClinicalNoteResponse,
  OPDClinicalNoteRequest,
  OPDStatusRequest,
  OPDReferRequest,
  OPDPageResponse,
  VisitStatus,
} from '../types/opd'

const BASE = '/opd/visits'

export const opdApi = {
  register(data: OPDVisitRequest): Promise<OPDVisitResponse> {
    return apiClient.post(BASE, data).then((res) => res.data)
  },

  getById(id: number): Promise<OPDVisitResponse> {
    return apiClient.get(`${BASE}/${id}`).then((res) => res.data)
  },

  search(params: {
    visitDate?: string
    doctorId?: number
    status?: VisitStatus
    patientUhid?: string
    patientName?: string
    visitNumber?: string
    page?: number
    size?: number
  }): Promise<OPDPageResponse<OPDVisitResponse>> {
    return apiClient.get(BASE, { params }).then((res) => res.data)
  },

  getQueue(doctorId: number, visitDate?: string): Promise<OPDVisitResponse[]> {
    return apiClient
      .get(`${BASE}/queue`, { params: { doctorId, visitDate } })
      .then((res) => res.data)
  },

  updateStatus(id: number, data: OPDStatusRequest): Promise<OPDVisitResponse> {
    return apiClient.put(`${BASE}/${id}/status`, data).then((res) => res.data)
  },

  addNotes(id: number, data: OPDClinicalNoteRequest): Promise<OPDClinicalNoteResponse> {
    return apiClient.post(`${BASE}/${id}/notes`, data).then((res) => res.data)
  },

  refer(id: number, data: OPDReferRequest): Promise<OPDVisitResponse> {
    return apiClient.post(`${BASE}/${id}/refer`, data).then((res) => res.data)
  },
}
