import { apiClient } from './client'

export interface UserProfile {
  username: string
  fullName: string
  role: string
  email: string
  phone: string
  active: boolean
  mustChangePassword?: boolean
  createdAt: string
  hospitalId?: number | null
  hospitalCode?: string
  hospitalName?: string
  tenantSlug?: string
}

export interface UpdateProfileDto {
  fullName: string
  email?: string
  phone?: string
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  await apiClient.post('/auth/change-password', { currentPassword, newPassword })
}

export async function changeTemporaryPassword(newPassword: string): Promise<void> {
  await apiClient.post('/auth/change-temporary-password', { newPassword })
}

export async function getProfile(): Promise<UserProfile> {
  const res = await apiClient.get<UserProfile>('/auth/me')
  return res.data
}

export async function updateProfile(data: UpdateProfileDto): Promise<UserProfile> {
  const res = await apiClient.put<UserProfile>('/auth/profile', data)
  return res.data
}
