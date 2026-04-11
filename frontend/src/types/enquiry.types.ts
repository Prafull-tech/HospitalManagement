export type EnquiryStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'ESCALATED'

export type EnquiryCategory =
  | 'APPOINTMENT'
  | 'DOCTOR_INFO'
  | 'DEPARTMENT_INFO'
  | 'BILLING_GUIDANCE'
  | 'ADMISSION_HELP'
  | 'COMPLAINT'
  | 'GENERAL_ENQUIRY'
  | 'OTHER'

export type EnquiryPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

export type EnquiryAuditEventType =
  | 'CREATED'
  | 'ASSIGNED'
  | 'STATUS_CHANGED'
  | 'NOTE_ADDED'
  | 'RESOLVED'
  | 'CLOSED'
  | 'ESCALATED'

export interface EnquiryAuditLogResponse {
  id: number
  eventType: EnquiryAuditEventType
  performedBy: string | null
  eventAt: string
  note: string | null
}

export interface EnquiryResponse {
  id: number
  enquiryNo: string
  patientId: number | null
  patientUhid: string | null
  patientName: string | null
  departmentId: number | null
  departmentName: string | null
  category: EnquiryCategory
  priority: EnquiryPriority
  status: EnquiryStatus
  subject: string
  description: string
  resolution: string | null
  assignedToUser: string | null
  enquirerName: string | null
  phone: string | null
  email: string | null
  createdAt: string
  updatedAt: string
  resolvedAt: string | null
  auditLogs: EnquiryAuditLogResponse[]
}

export interface EnquiryRequest {
  patientId?: number
  departmentId?: number
  category: EnquiryCategory
  priority?: EnquiryPriority
  subject: string
  description: string
  enquirerName?: string
  phone?: string
  email?: string
}

export interface EnquiryAssignRequest {
  departmentId?: number
  assignedToUser?: string
  note?: string
}

export interface EnquiryStatusUpdateRequest {
  status: EnquiryStatus
  resolution?: string
  note?: string
}

export interface EnquiryNoteRequest {
  note: string
}

export interface EnquiryDashboard {
  openCount: number
  inProgressCount: number
  resolvedCount: number
  closedCount: number
  escalatedCount: number
  recentEnquiries: EnquiryResponse[]
  byCategory: Record<string, number>
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
