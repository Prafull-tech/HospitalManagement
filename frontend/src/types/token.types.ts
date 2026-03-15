export type TokenPriority = 'NORMAL' | 'EMERGENCY' | 'SENIOR' | 'FOLLOWUP' | 'PREGNANT'

export type TokenStatus =
  | 'WAITING'
  | 'CALLED'
  | 'IN_CONSULTATION'
  | 'COMPLETED'
  | 'SKIPPED'

export interface TokenResponse {
  id: number
  tokenNo: string
  patientId: number
  patientName: string
  uhid: string
  doctorId: number
  doctorName: string
  doctorCode: string
  departmentId: number
  departmentName: string
  tokenDate: string
  appointmentId: number | null
  priority: TokenPriority
  status: TokenStatus
  createdAt: string
  calledAt: string | null
  completedAt: string | null
  opdVisitId: number | null
}

export interface TokenGenerateRequest {
  patientId: number
  doctorId: number
  departmentId: number
  priority?: TokenPriority
  appointmentId?: number
}

export interface TokenDisplay {
  currentToken: string | null
  nextToken: string | null
  doctorName: string
  roomNo: string
}

export interface TokenDashboard {
  waiting: TokenResponse[]
  inConsultation: TokenResponse[]
  completed: TokenResponse[]
}
