import { apiClient } from './client'
import type {
  DoctorResponse,
  DoctorRequest,
  DoctorAvailabilityRequest,
  DoctorAvailabilityResponse,
  DepartmentResponse,
  PageResponse,
  DoctorStatus,
} from '../types/doctor'

const DOCTORS = '/doctors'
const DEPARTMENTS = '/departments'

export const doctorsApi = {
  list(params: {
    code?: string
    departmentId?: number
    status?: DoctorStatus | ''
    search?: string
    page?: number
    size?: number
  }): Promise<PageResponse<DoctorResponse>> {
    const safe: Record<string, string | number> = {}
    if (params.page !== undefined && params.page !== null) safe.page = params.page
    if (params.size !== undefined && params.size !== null) safe.size = params.size
    if (params.code != null && String(params.code).trim() !== '') safe.code = String(params.code).trim()
    if (params.departmentId != null && params.departmentId !== 0) safe.departmentId = params.departmentId
    if (params.status != null && params.status !== '') safe.status = params.status as DoctorStatus
    if (params.search != null && String(params.search).trim() !== '') safe.search = String(params.search).trim()
    return apiClient.get(DOCTORS, { params: safe }).then((res) => res.data)
  },

  getById(id: number): Promise<DoctorResponse> {
    return apiClient.get(`${DOCTORS}/${id}`).then((res) => res.data)
  },

  create(data: DoctorRequest): Promise<DoctorResponse> {
    return apiClient.post(DOCTORS, data).then((res) => res.data)
  },

  update(id: number, data: DoctorRequest): Promise<DoctorResponse> {
    return apiClient.put(`${DOCTORS}/${id}`, data).then((res) => res.data)
  },

  getAvailability(doctorId: number): Promise<DoctorAvailabilityResponse[]> {
    return apiClient.get(`${DOCTORS}/${doctorId}/availability`).then((res) => res.data)
  },

  addAvailability(doctorId: number, data: DoctorAvailabilityRequest): Promise<DoctorAvailabilityResponse> {
    return apiClient.post(`${DOCTORS}/${doctorId}/availability`, data).then((res) => res.data)
  },
}

export const departmentsApi = {
  list(): Promise<DepartmentResponse[]> {
    return apiClient.get(DEPARTMENTS).then((res) => res.data)
  },

  getById(id: number): Promise<DepartmentResponse> {
    return apiClient.get(`${DEPARTMENTS}/${id}`).then((res) => res.data)
  },
}
