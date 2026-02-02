import { apiClient } from './client'
import type { DashboardStatsDto } from '../types/dashboard'

export const dashboardApi = {
  getStats(params: { fromDate?: string; toDate?: string }): Promise<DashboardStatsDto> {
    return apiClient.get('/dashboard/stats', { params }).then((res) => res.data)
  },
}
