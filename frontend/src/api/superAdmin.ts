import { apiClient } from './client'

// ── Dashboard ──

export interface DashboardSummary {
  totalHospitals: number
  activeHospitals: number
  totalUsers: number
  activeSubscriptions: number
  trialSubscriptions: number
  expiredSubscriptions: number
  totalSubscriptions: number
  estimatedMonthlyEarnings: number
  estimatedCurrentMonthEarnings: number
  planBreakdown: DashboardPlanEarning[]
}

export interface DashboardPlanEarning {
  planId: number
  planCode: string
  planName: string
  activeSubscriptions: number
  trialSubscriptions: number
  estimatedMonthlyEarnings: number
}

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const res = await apiClient.get<DashboardSummary>('/super-admin/dashboard')
  return res.data
}

// ── Hospitals ──

export interface HospitalDto {
  id: number
  hospitalCode: string
  hospitalName: string
  location: string | null
  subdomain: string | null
  customDomain: string | null
  domainVerificationToken: string | null
  domainVerificationStatus: string | null
  domainVerifiedAt: string | null
  certificateStatus: string | null
  certificateRequestedAt: string | null
  certificateIssuedAt: string | null
  certificateExpiresAt: string | null
  lastDomainVerificationError: string | null
  lastCertificateError: string | null
  logoUrl: string | null
  websiteUrl: string | null
  facebookUrl: string | null
  twitterUrl: string | null
  instagramUrl: string | null
  linkedinUrl: string | null
  contactEmail: string | null
  billingEmail: string | null
  contactPhone: string | null
  onboardingStatus: string | null
  active: boolean
}

export type HospitalInput = Omit<HospitalDto, 'id' | 'onboardingStatus'>

export interface CustomDomainCertificateInput {
  status: string
  issuedAt?: string | null
  expiresAt?: string | null
  errorMessage?: string | null
}

export async function listHospitals(activeOnly = false): Promise<HospitalDto[]> {
  const res = await apiClient.get<HospitalDto[]>('/super-admin/hospitals', { params: { activeOnly } })
  return res.data
}

export async function getHospital(id: number): Promise<HospitalDto> {
  const res = await apiClient.get<HospitalDto>(`/super-admin/hospitals/${id}`)
  return res.data
}

export interface HospitalModuleConfigItem {
  moduleCode: string
  moduleName: string
  moduleCategory: string
  enabled: boolean
  inCurrentPlan: boolean
}

export interface HospitalModuleConfig {
  hospitalId: number
  hasActivePlan: boolean
  planId: number | null
  planCode: string | null
  planName: string | null
  modules: HospitalModuleConfigItem[]
}

export interface HospitalModuleUpdateInput {
  modules: Array<{
    moduleCode: string
    enabled: boolean
  }>
}

export async function getHospitalModules(id: number): Promise<HospitalModuleConfig> {
  const res = await apiClient.get<HospitalModuleConfig>(`/super-admin/hospitals/${id}/modules`)
  return res.data
}

export async function updateHospitalModules(id: number, data: HospitalModuleUpdateInput): Promise<HospitalModuleConfig> {
  const res = await apiClient.put<HospitalModuleConfig>(`/super-admin/hospitals/${id}/modules`, data)
  return res.data
}

export async function createHospital(data: HospitalInput): Promise<HospitalDto> {
  const res = await apiClient.post<HospitalDto>('/super-admin/hospitals', data)
  return res.data
}

export async function updateHospital(id: number, data: HospitalInput): Promise<HospitalDto> {
  const res = await apiClient.put<HospitalDto>(`/super-admin/hospitals/${id}`, data)
  return res.data
}

export async function toggleHospitalStatus(id: number, active: boolean): Promise<void> {
  await apiClient.patch(`/super-admin/hospitals/${id}/status`, { active })
}

export async function regenerateHospitalCustomDomainToken(id: number): Promise<HospitalDto> {
  const res = await apiClient.post<HospitalDto>(`/super-admin/hospitals/${id}/custom-domain/regenerate-token`)
  return res.data
}

export async function verifyHospitalCustomDomain(id: number): Promise<HospitalDto> {
  const res = await apiClient.post<HospitalDto>(`/super-admin/hospitals/${id}/custom-domain/verify`)
  return res.data
}

export async function requestHospitalCertificate(id: number): Promise<HospitalDto> {
  const res = await apiClient.post<HospitalDto>(`/super-admin/hospitals/${id}/custom-domain/certificate/request`)
  return res.data
}

export async function updateHospitalCertificate(id: number, data: CustomDomainCertificateInput): Promise<HospitalDto> {
  const res = await apiClient.patch<HospitalDto>(`/super-admin/hospitals/${id}/custom-domain/certificate`, data)
  return res.data
}

// ── Hospital Users ──

export interface HospitalUser {
  id: number
  username: string
  fullName: string
  role: string
  email: string | null
  phone: string | null
  active: boolean
  hospitalId: number
  hospitalName: string
}

export async function getHospitalUsers(hospitalId: number): Promise<HospitalUser[]> {
  const res = await apiClient.get<HospitalUser[]>(`/super-admin/hospitals/${hospitalId}/users`)
  return res.data
}

export interface CreateHospitalUserInput {
  username: string
  fullName: string
  password: string
  role: string
  email?: string
  phone?: string
}

export async function createHospitalUser(hospitalId: number, data: CreateHospitalUserInput): Promise<HospitalUser> {
  const res = await apiClient.post<HospitalUser>(`/super-admin/hospitals/${hospitalId}/users`, data)
  return res.data
}

export async function toggleUserStatus(hospitalId: number, userId: number, active: boolean): Promise<void> {
  await apiClient.patch(`/super-admin/hospitals/${hospitalId}/users/${userId}/status`, { active })
}

// ── Subscription Plans ──

export interface SubscriptionPlan {
  id: number
  planCode: string
  planName: string
  description: string | null
  monthlyPrice: number
  quarterlyPrice: number | null
  yearlyPrice: number | null
  maxUsers: number | null
  maxBeds: number | null
  enabledModules: string | null
  active: boolean
  trialDays: number | null
}

export type SubscriptionPlanInput = Omit<SubscriptionPlan, 'id'>

export async function listPlans(activeOnly = false): Promise<SubscriptionPlan[]> {
  const res = await apiClient.get<SubscriptionPlan[]>('/super-admin/subscriptions/plans', { params: { activeOnly } })
  return res.data
}

export async function createPlan(data: SubscriptionPlanInput): Promise<SubscriptionPlan> {
  const res = await apiClient.post<SubscriptionPlan>('/super-admin/subscriptions/plans', data)
  return res.data
}

export async function updatePlan(id: number, data: SubscriptionPlanInput): Promise<SubscriptionPlan> {
  const res = await apiClient.put<SubscriptionPlan>(`/super-admin/subscriptions/plans/${id}`, data)
  return res.data
}

// ── Hospital Subscriptions ──

export interface HospitalSubscription {
  id: number
  hospitalId: number
  hospitalName: string
  hospitalCode: string
  planId: number
  planName: string
  planCode: string
  status: string
  startDate: string
  endDate: string | null
  trialEndDate: string | null
  billingCycle: string | null
  notes: string | null
}

export interface CreateSubscriptionInput {
  hospitalId: number
  planId: number
  status?: string
  startDate?: string
  endDate?: string
  trialEndDate?: string
  billingCycle?: string
  notes?: string
}

export async function listSubscriptions(): Promise<HospitalSubscription[]> {
  const res = await apiClient.get<HospitalSubscription[]>('/super-admin/subscriptions')
  return res.data
}

export async function createSubscription(data: CreateSubscriptionInput): Promise<HospitalSubscription> {
  const res = await apiClient.post<HospitalSubscription>('/super-admin/subscriptions', data)
  return res.data
}

export async function updateSubscriptionStatus(id: number, status: string): Promise<HospitalSubscription> {
  const res = await apiClient.patch<HospitalSubscription>(`/super-admin/subscriptions/${id}/status`, { status })
  return res.data
}
