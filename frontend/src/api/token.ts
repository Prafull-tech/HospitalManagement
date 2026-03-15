import { apiClient } from './client'
import type {
  TokenResponse,
  TokenGenerateRequest,
  TokenDisplay,
  TokenDashboard,
} from '../types/token.types'

const BASE = '/tokens'

export const tokenApi = {
  generate(data: TokenGenerateRequest): Promise<TokenResponse> {
    return apiClient.post(`${BASE}/generate`, data).then((r) => r.data)
  },

  getQueue(doctorId: number, date?: string): Promise<TokenResponse[]> {
    return apiClient
      .get(`${BASE}/queue/${doctorId}`, { params: date ? { date } : {} })
      .then((r) => r.data)
  },

  getCurrent(doctorId?: number, date?: string): Promise<TokenDisplay[]> {
    const params: Record<string, string | number> = {}
    if (date) params.date = date
    if (doctorId != null) params.doctorId = doctorId
    return apiClient.get(`${BASE}/current`, { params }).then((r) => r.data)
  },

  callNext(doctorId: number, date?: string): Promise<TokenResponse> {
    return apiClient
      .put(`${BASE}/call-next/${doctorId}`, null, { params: date ? { date } : {} })
      .then((r) => r.data)
  },

  startConsultation(tokenId: number): Promise<TokenResponse> {
    return apiClient.put(`${BASE}/start/${tokenId}`).then((r) => r.data)
  },

  complete(tokenId: number): Promise<TokenResponse> {
    return apiClient.put(`${BASE}/complete/${tokenId}`).then((r) => r.data)
  },

  skip(tokenId: number): Promise<TokenResponse> {
    return apiClient.put(`${BASE}/skip/${tokenId}`).then((r) => r.data)
  },

  getDashboard(doctorId: number, date?: string): Promise<TokenDashboard> {
    return apiClient
      .get(`${BASE}/dashboard`, { params: { doctorId, ...(date ? { date } : {}) } })
      .then((r) => r.data)
  },
}
