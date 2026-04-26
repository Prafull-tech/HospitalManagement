import { apiClient } from './client'

export interface AdminHospitalUser {
  id: number
  username: string
  fullName: string
  role: string
  email: string | null
  phone: string | null
  active: boolean
  mustChangePassword: boolean
  hospitalId: number
  hospitalName: string
}

export interface CreateAdminHospitalUserInput {
  username: string
  fullName: string
  password: string
  role: string
  email?: string
  phone?: string
}

export async function listAdminHospitalUsers(): Promise<AdminHospitalUser[]> {
  const res = await apiClient.get<AdminHospitalUser[]>('/admin/users')
  return res.data
}

export async function createAdminHospitalUser(data: CreateAdminHospitalUserInput): Promise<AdminHospitalUser> {
  const res = await apiClient.post<AdminHospitalUser>('/admin/users', data)
  return res.data
}

export async function updateAdminHospitalUserStatus(userId: number, active: boolean): Promise<void> {
  await apiClient.patch(`/admin/users/${userId}/status`, { active })
}

export async function resetAdminHospitalUserPassword(userId: number, temporaryPassword: string): Promise<void> {
  await apiClient.post(`/admin/users/${userId}/reset-password`, { temporaryPassword })
}