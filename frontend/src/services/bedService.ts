/**
 * Bed service – single source of truth for ward pages.
 * All ward views derive data from GET /api/ipd/hospital-beds.
 */

import { apiClient } from '../api/client'
import type { HospitalBedItem, WardType } from '../types/bed.types'

const HOSPITAL_BEDS = '/ipd/hospital-beds'

export const bedService = {
  /**
   * Fetch hospital beds. Filter by wardType on server when provided (single ward).
   * For multiple ward types (e.g. ICU/CCU/NICU/HDU), omit wardType and filter client-side.
   */
  getHospitalBeds(
    hospitalId: number,
    params?: { wardType?: WardType }
  ): Promise<HospitalBedItem[]> {
    const query: Record<string, string | number> = { hospitalId }
    if (params?.wardType) query.wardType = params.wardType
    return apiClient
      .get<HospitalBedItem[]>(HOSPITAL_BEDS, { params: query })
      .then((r) => r.data)
  },
}
