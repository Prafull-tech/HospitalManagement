import { apiClient } from './client'
import type { WalkInRegisterRequest, WalkInRegisterResponse, WalkInDashboard } from '../types/walkin.types'

const BASE = '/walkin'

export const walkinApi = {
  register(data: WalkInRegisterRequest): Promise<WalkInRegisterResponse> {
    return apiClient.post(`${BASE}/register`, data).then((r) => r.data)
  },

  getDashboard(date?: string): Promise<WalkInDashboard> {
    return apiClient.get(`${BASE}/dashboard`, { params: date ? { date } : {} }).then((r) => r.data)
  },
}
