import { apiClient } from './client'
import type {
  AppointmentResponse,
  AppointmentRequest,
  WalkInAppointmentRequest,
  OnlineAppointmentRequest,
  RescheduleRequest,
  CancelRequest,
  AppointmentDashboard,
  DoctorScheduleResponse,
  DoctorScheduleRequest,
} from '../types/appointment.types'
import type { AppointmentStatus } from '../types/appointment.types'

const APPOINTMENTS = '/appointments'
const DOCTOR_SCHEDULE = '/doctors/schedule'

export const appointmentApi = {
  getDashboard(date?: string): Promise<AppointmentDashboard> {
    return apiClient.get(`${APPOINTMENTS}/dashboard`, { params: date ? { date } : {} }).then((r) => r.data)
  },

  create(data: AppointmentRequest): Promise<AppointmentResponse> {
    return apiClient.post(APPOINTMENTS, data).then((r) => r.data)
  },

  createWalkIn(data: WalkInAppointmentRequest): Promise<AppointmentResponse> {
    return apiClient.post(`${APPOINTMENTS}/walkin`, data).then((r) => r.data)
  },

  createOnline(data: OnlineAppointmentRequest): Promise<AppointmentResponse> {
    return apiClient.post(`${APPOINTMENTS}/online`, data).then((r) => r.data)
  },

  reschedule(id: number, data: RescheduleRequest): Promise<AppointmentResponse> {
    return apiClient.put(`${APPOINTMENTS}/${id}/reschedule`, data).then((r) => r.data)
  },

  cancel(id: number, data?: CancelRequest): Promise<AppointmentResponse> {
    return apiClient.put(`${APPOINTMENTS}/${id}/cancel`, data ?? {}).then((r) => r.data)
  },

  markNoShow(id: number): Promise<AppointmentResponse> {
    return apiClient.put(`${APPOINTMENTS}/${id}/no-show`).then((r) => r.data)
  },

  convertToOpd(id: number): Promise<{ id: number; visitNumber: string }> {
    return apiClient.post(`${APPOINTMENTS}/${id}/convert-to-opd`).then((r) => r.data)
  },

  confirmOnline(id: number): Promise<AppointmentResponse> {
    return apiClient.put(`${APPOINTMENTS}/${id}/confirm`).then((r) => r.data)
  },

  search(params: {
    date?: string
    doctorId?: number
    status?: AppointmentStatus
    patientUhid?: string
    patientName?: string
    page?: number
    size?: number
  }): Promise<{ content: AppointmentResponse[]; totalElements: number; totalPages: number }> {
    return apiClient.get(`${APPOINTMENTS}/search`, { params }).then((r) => r.data)
  },

  getQueue(doctorId: number, date?: string): Promise<AppointmentResponse[]> {
    return apiClient.get(`${APPOINTMENTS}/queue/${doctorId}`, { params: date ? { date } : {} }).then((r) => r.data)
  },

  getById(id: number): Promise<AppointmentResponse> {
    return apiClient.get(`${APPOINTMENTS}/${id}`).then((r) => r.data)
  },
}

export const doctorScheduleApi = {
  create(data: DoctorScheduleRequest): Promise<DoctorScheduleResponse> {
    return apiClient.post(DOCTOR_SCHEDULE, data).then((r) => r.data)
  },

  getByDoctorId(doctorId: number): Promise<DoctorScheduleResponse[]> {
    return apiClient.get(`${DOCTOR_SCHEDULE}/${doctorId}`).then((r) => r.data)
  },

  deleteByDoctorId(doctorId: number): Promise<void> {
    return apiClient.delete(`${DOCTOR_SCHEDULE}/${doctorId}`).then(() => undefined)
  },
}
