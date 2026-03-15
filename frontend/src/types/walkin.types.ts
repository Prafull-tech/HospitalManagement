import type { TokenResponse } from './token.types'

export interface WalkInRegisterRequest {
  patientId?: number
  fullName?: string
  gender?: string
  age?: number
  mobile?: string
  email?: string
  address?: string
  city?: string
  state?: string
  pincode?: string
  idProofType?: string
  idProofNumber?: string
  doctorId: number
  departmentId: number
  visitType?: 'NEW' | 'FOLLOWUP'
  priority?: 'NORMAL' | 'EMERGENCY' | 'SENIOR' | 'FOLLOWUP' | 'PREGNANT'
}

export interface WalkInRegisterResponse {
  patientUhid: string
  patientName: string
  opdVisitId: number
  visitNumber: string
  token: TokenResponse
}

export interface WalkInDashboard {
  walkInsToday: number
  patientsWaiting: number
  patientsConsulted: number
  inConsultation: number
  emergencyWalkIns: number
}
