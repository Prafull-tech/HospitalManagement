import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import type { HMSRole } from '../config/sidebarMenu'
import { apiClient } from '../api/client'
import { setAuthClearCallback } from '../api/authRedirect'
import { clearStoredAuth, getStoredAuth, getStoredAuthExpiryMs, getValidStoredAuth, saveStoredAuth } from '../lib/authStorage'

export type Role = HMSRole

export interface User {
  username: string
  roles: Role[]
  fullName?: string
  email?: string
  phone?: string
  active?: boolean
  mustChangePassword?: boolean
  createdAt?: string
  hospitalId?: number | null
  hospitalCode?: string
  hospitalName?: string
  tenantSlug?: string
  expiresAt?: string
  sessionExpiresAt?: string
}

interface AuthState {
  user: User | null
  login: (usernameOrEmail: string, password: string, hospitalSlug?: string | null) => Promise<User>
  logout: () => void
  updateUser: (patch: Partial<User>) => void
  hasRole: (...roles: Role[]) => boolean
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthState | null>(null)

function getInitialUser(): User | null {
  const parsed = getValidStoredAuth()
  if (parsed?.username && parsed?.role) {
    return {
      username: parsed.username,
      roles: [parsed.role as Role],
      fullName: parsed.fullName,
      email: parsed.email,
      phone: parsed.phone,
      active: parsed.active,
      mustChangePassword: parsed.mustChangePassword,
      createdAt: parsed.createdAt,
      hospitalId: parsed.hospitalId,
      hospitalCode: parsed.hospitalCode,
      hospitalName: parsed.hospitalName,
      tenantSlug: parsed.tenantSlug,
      expiresAt: parsed.expiresAt,
      sessionExpiresAt: parsed.sessionExpiresAt,
    }
  }
  return null
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(getInitialUser)

  const clearAuthState = useCallback(() => {
    setUser(null)
    clearStoredAuth()
  }, [])

  const login = useCallback(async (usernameOrEmail: string, password: string, hospitalSlug?: string | null) => {
    if (hospitalSlug) {
      const res = await apiClient.post('/auth/hospital/login', { email: usernameOrEmail, password, hospitalSlug })
      const payload = res.data as { success: boolean; message: string; data?: any }
      const data = payload.data as {
        token: string
        user: { id: string; name: string; email: string; role: string }
        hospital: { id: number; name: string; slug: string; logoUrl?: string | null }
      }
      const u: User = {
        username: data.user.email,
        roles: [data.user.role.toUpperCase() as Role],
        fullName: data.user.name,
        email: data.user.email,
        hospitalId: data.hospital?.id,
        hospitalName: data.hospital?.name,
        tenantSlug: data.hospital?.slug,
      }
      setUser(u)
      saveStoredAuth({
        username: u.username,
        role: u.roles[0],
        fullName: u.fullName,
        email: u.email,
        hospitalId: u.hospitalId,
        hospitalName: u.hospitalName,
        tenantSlug: u.tenantSlug,
        token: data.token,
      })
      return u
    }

    const res = await apiClient.post('/auth/super-admin/login', { username: usernameOrEmail, password })
    const payload = res.data as { success: boolean; message: string; data?: any }
    const data = payload.data as {
      token: string
      user: { id: number; name: string; email?: string; role: string }
      hospital: null
    }
    const u: User = {
      username: usernameOrEmail,
      roles: [data.user.role as Role],
      fullName: data.user.name,
      email: data.user.email,
    }
    setUser(u)
    saveStoredAuth({
      username: u.username,
      role: u.roles[0],
      fullName: u.fullName,
      email: u.email,
      token: data.token,
    })
    return u
  }, [])

  const logout = useCallback(() => {
    const auth = getStoredAuth()
    if (auth?.refreshToken) {
      apiClient.post('/auth/logout').catch(() => {})
    }
    clearAuthState()
  }, [clearAuthState])

  const updateUser = useCallback((patch: Partial<User>) => {
    setUser((prev) => {
      if (!prev) return prev
      const updated = { ...prev, ...patch }
      const stored = getStoredAuth()
      if (stored) {
        saveStoredAuth({ ...stored, ...patch, role: stored.role })
      }
      return updated
    })
  }, [])

  useEffect(() => {
    setAuthClearCallback(logout)
    return () => setAuthClearCallback(null)
  }, [logout])

  useEffect(() => {
    const expiryMs = getStoredAuthExpiryMs(getStoredAuth())
    if (!user || expiryMs === null) return

    const timeoutMs = expiryMs - Date.now()
    if (timeoutMs <= 0) {
      clearAuthState()
      return
    }

    const timer = window.setTimeout(() => {
      clearAuthState()
    }, timeoutMs)

    return () => window.clearTimeout(timer)
  }, [clearAuthState, user])

  const hasRole = useCallback(
    (...roles: Role[]) => {
      if (!user) return false
      return roles.some((r) => user.roles.includes(r))
    },
    [user]
  )

  const value: AuthState = { user, login, logout, updateUser, hasRole, isAuthenticated: !!user }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
