import { apiClient } from '../api/client'
import type { ViewAdmissionResponse } from '../types/ipdAdmission.types'

/**
 * Fetch full read-only view for IPD admission detail page.
 * GET /api/ipd/admissions/{admissionId}/view
 * Returns 404 if admission not found.
 */
export function getAdmissionView(admissionId: number): Promise<ViewAdmissionResponse> {
  return apiClient.get(`/ipd/admissions/${admissionId}/view`).then((res) => res.data)
}
