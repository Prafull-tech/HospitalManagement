import { apiClient } from './client'

export type AuditEventDto = {
  id: number
  entityType: string
  entityId: number | null
  action: string
  username: string | null
  details: string | null
  ipAddress: string | null
  correlationId: string | null
  createdAt: string
}

export type AuditEventPageResponse = {
  items: AuditEventDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export async function listAuditEvents(params?: { page?: number; size?: number }): Promise<AuditEventPageResponse> {
  const res = await apiClient.get<AuditEventPageResponse>('/admin/audit', { params })
  return res.data
}

