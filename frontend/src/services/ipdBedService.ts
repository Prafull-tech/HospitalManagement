/**
 * IPD Beds – API service for availability and status updates.
 */

import { apiClient } from '../api/client'
import type { BedAvailabilityItem, BedFiltersParams, BedStatus } from '../types/ipdBed.types'

const BASE = '/ipd'

export const ipdBedService = {
  getAvailability(params?: BedFiltersParams): Promise<BedAvailabilityItem[]> {
    const query: Record<string, string | number | undefined> = {}
    if (params?.wardId != null) query.wardId = params.wardId
    if (params?.wardType) query.wardType = params.wardType
    if (params?.bedStatus) query.bedStatus = params.bedStatus
    if (params?.search?.trim()) query.search = params.search.trim()
    return apiClient
      .get<BedAvailabilityItem[]>(`${BASE}/beds/availability`, { params: query })
      .then((r) => r.data)
  },

  updateStatus(bedId: number, bedStatus: BedStatus): Promise<BedAvailabilityItem> {
    return apiClient
      .put<BedAvailabilityItem>(`${BASE}/beds/${bedId}/status`, { bedStatus })
      .then((r) => r.data)
  },
}
