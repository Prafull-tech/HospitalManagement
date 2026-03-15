export type AppointmentStatus =
  | 'BOOKED'
  | 'CONFIRMED'
  | 'PENDING_CONFIRMATION'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW'

export type AppointmentSource = 'FRONT_DESK' | 'WALK_IN' | 'ONLINE'

export type AppointmentVisitType = 'NEW' | 'FOLLOWUP'

export interface AppointmentResponse {
  id: number
  patientId: number
  patientUhid: string
  patientName: string
  doctorId: number
  doctorName: string
  doctorCode: string
  departmentId: number
  departmentName: string
  appointmentDate: string
  slotTime: string
  tokenNo: number | null
  status: AppointmentStatus
  source: AppointmentSource
  visitType: AppointmentVisitType | null
  createdBy: string | null
  cancelReason: string | null
  opdVisitId: number | null
  createdAt: string
  updatedAt: string
}

export interface AppointmentRequest {
  patientId: number
  doctorId: number
  departmentId: number
  appointmentDate: string
  slotTime: string
  visitType?: AppointmentVisitType
}

export interface WalkInAppointmentRequest {
  patientUhid: string
  doctorId: number
  appointmentDate?: string
  slotTime?: string
}

export interface OnlineAppointmentRequest {
  patientUhid: string
  doctorId: number
  appointmentDate: string
  slotTime: string
  visitType?: AppointmentVisitType
}

export interface RescheduleRequest {
  appointmentDate?: string
  slotTime?: string
  doctorId?: number
}

export interface CancelRequest {
  reason?: string
}

export interface AppointmentDashboard {
  date: string
  totalAppointmentsToday: number
  walkIns: number
  onlineBookings: number
  completedConsultations: number
  cancelled: number
  noShow: number
  todaysAppointments: AppointmentResponse[]
  upcomingAppointments: AppointmentResponse[]
  cancelledAppointments: AppointmentResponse[]
  noShowPatients: AppointmentResponse[]
}

export interface DoctorScheduleResponse {
  id: number
  doctorId: number
  doctorName: string
  dayOfWeek: number
  startTime: string
  endTime: string
  slotDurationMinutes: number
  maxPatients: number | null
}

export interface DoctorScheduleRequest {
  doctorId: number
  dayOfWeek: number
  startTime: string
  endTime: string
  slotDurationMinutes?: number
  maxPatients?: number
}
