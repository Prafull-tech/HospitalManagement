import { apiClient } from './client'
import type {
  EnquiryAssignRequest,
  EnquiryDashboard,
  EnquiryNoteRequest,
  EnquiryRequest,
  EnquiryResponse,
  EnquiryStatus,
  EnquiryCategory,
  EnquiryPriority,
  EnquiryStatusUpdateRequest,
  PageResponse,
} from '../types/enquiry.types'

const BASE = '/enquiries'

export const enquiryApi = {
  create(data: EnquiryRequest): Promise<EnquiryResponse> {
    return apiClient.post(BASE, data).then((res) => res.data)
  },

  getById(id: number): Promise<EnquiryResponse> {
    return apiClient.get(`${BASE}/${id}`).then((res) => res.data)
  },

  search(params: {
    status?: EnquiryStatus | ''
    category?: EnquiryCategory | ''
    priority?: EnquiryPriority | ''
    departmentId?: number | ''
    assignedToUser?: string
    createdFrom?: string
    createdTo?: string
    patientUhid?: string
    query?: string
    page?: number
    size?: number
  }): Promise<PageResponse<EnquiryResponse>> {
    const safe: Record<string, string | number> = {}
    if (params.status) safe.status = params.status
    if (params.category) safe.category = params.category
    if (params.priority) safe.priority = params.priority
    if (params.departmentId) safe.departmentId = Number(params.departmentId)
    if (params.assignedToUser?.trim()) safe.assignedToUser = params.assignedToUser.trim()
    if (params.createdFrom) safe.createdFrom = params.createdFrom
    if (params.createdTo) safe.createdTo = params.createdTo
    if (params.patientUhid?.trim()) safe.patientUhid = params.patientUhid.trim()
    if (params.query?.trim()) safe.query = params.query.trim()
    safe.page = params.page ?? 0
    safe.size = params.size ?? 20
    return apiClient.get(`${BASE}/search`, { params: safe }).then((res) => res.data)
  },

  getDashboard(): Promise<EnquiryDashboard> {
    return apiClient.get(`${BASE}/dashboard`).then((res) => res.data)
  },

  assign(id: number, data: EnquiryAssignRequest): Promise<EnquiryResponse> {
    return apiClient.patch(`${BASE}/${id}/assign`, data).then((res) => res.data)
  },

  updateStatus(id: number, data: EnquiryStatusUpdateRequest): Promise<EnquiryResponse> {
    return apiClient.patch(`${BASE}/${id}/status`, data).then((res) => res.data)
  },

  addNote(id: number, data: EnquiryNoteRequest): Promise<EnquiryResponse> {
    return apiClient.post(`${BASE}/${id}/notes`, data).then((res) => res.data)
  },
}
