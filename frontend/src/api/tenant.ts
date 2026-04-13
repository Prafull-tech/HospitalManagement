import { apiClient } from './client'

export interface TenantContextDto {
  host: string | null
  platformHost: boolean
  tenantResolved: boolean
  hospitalId: number | null
  hospitalCode: string | null
  hospitalName: string | null
  tenantSlug: string | null
  customDomain: string | null
  resolvedBy: 'SUBDOMAIN' | 'CUSTOM_DOMAIN' | null
  domainVerificationStatus: string | null
  certificateStatus: string | null
  certificateExpiresAt: string | null
  logoUrl: string | null
  contactEmail: string | null
  contactPhone: string | null
  active: boolean | null
}

export async function getTenantContext(): Promise<TenantContextDto> {
  const res = await apiClient.get<TenantContextDto>('/public/tenant-context')
  return res.data
}