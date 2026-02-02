/**
 * Hospital-wise Bed Availability – API service.
 * POST /api/hospitals/{hospitalId}/beds
 * PUT  /api/hospitals/{hospitalId}/beds/{id}
 * GET  /api/hospitals/{hospitalId}/beds
 * DELETE /api/hospitals/{hospitalId}/beds/{id}
 */

import { apiClient } from '../api/client'
import type {
  HospitalItem,
  BedAvailabilityItem,
  BedAvailabilityFormValues,
  WardTypeMasterItem,
} from '../types/bedAvailability.types'

const HOSPITALS = '/hospitals'

export const bedAvailabilityService = {
  listHospitals(activeOnly = true): Promise<HospitalItem[]> {
    return apiClient
      .get<HospitalItem[]>(HOSPITALS, { params: { activeOnly } })
      .then((r) => r.data)
  },

  getHospital(id: number): Promise<HospitalItem> {
    return apiClient.get<HospitalItem>(`${HOSPITALS}/${id}`).then((r) => r.data)
  },

  listBeds(hospitalId: number): Promise<BedAvailabilityItem[]> {
    return apiClient
      .get<BedAvailabilityItem[]>(`${HOSPITALS}/${hospitalId}/bed-availability`)
      .then((r) => r.data)
  },

  create(hospitalId: number, body: BedAvailabilityFormValues): Promise<BedAvailabilityItem> {
    return apiClient
      .post<BedAvailabilityItem>(`${HOSPITALS}/${hospitalId}/bed-availability`, body)
      .then((r) => r.data)
  },

  update(
    hospitalId: number,
    id: number,
    body: BedAvailabilityFormValues
  ): Promise<BedAvailabilityItem> {
    return apiClient
      .put<BedAvailabilityItem>(`${HOSPITALS}/${hospitalId}/bed-availability/${id}`, body)
      .then((r) => r.data)
  },

  delete(hospitalId: number, id: number): Promise<void> {
    return apiClient.delete(`${HOSPITALS}/${hospitalId}/bed-availability/${id}`)
  },

  listWardTypes(activeOnly = true): Promise<WardTypeMasterItem[]> {
    return apiClient
      .get<WardTypeMasterItem[]>('/ward-types', { params: { activeOnly } })
      .then((r) => r.data)
  },
}
