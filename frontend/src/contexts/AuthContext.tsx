import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import type { HMSRole } from '../config/sidebarMenu'
import { apiClient } from '../api/client'
import { setAuthClearCallback } from '../api/authRedirect'

export type Role = HMSRole

export interface User {
  username: string
  roles: Role[]
  fullName?: string
  email?: string
  phone?: string
  active?: boolean
  createdAt?: string
  hospitalId?: number | null
  hospitalCode?: string
  hospitalName?: string
  tenantSlug?: string
}

interface AuthState {
  user: User | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  updateUser: (patch: Partial<User>) => void
  hasRole: (...roles: Role[]) => boolean
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthState | null>(null)

function getInitialUser(): User | null {
  if (typeof window === 'undefined') return null
  try {
    const auth = localStorage.getItem('hms_auth')
    if (!auth) return null
    const parsed = JSON.parse(auth) as {
      username?: string; role?: string; fullName?: string
      email?: string; phone?: string; active?: boolean; createdAt?: string
      hospitalId?: number | null; hospitalCode?: string; hospitalName?: string; tenantSlug?: string
    }
    if (parsed?.username && parsed?.role) {
      return {
        username: parsed.username,
        roles: [parsed.role as Role],
        fullName: parsed.fullName,
        email: parsed.email,
        phone: parsed.phone,
        active: parsed.active,
        createdAt: parsed.createdAt,
        hospitalId: parsed.hospitalId,
        hospitalCode: parsed.hospitalCode,
        hospitalName: parsed.hospitalName,
        tenantSlug: parsed.tenantSlug,
      }
    }
  } catch { /* ignore */ }
  return null
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(getInitialUser)

  const login = useCallback(async (username: string, password: string) => {
    const res = await apiClient.post('/auth/login', { username, password })
    const data = res.data as {
      token: string; refreshToken: string; username: string; role: string
      fullName: string; email?: string; phone?: string; active?: boolean; createdAt?: string
      hospitalId?: number | null; hospitalCode?: string; hospitalName?: string; tenantSlug?: string
    }
    const u: User = {
      username: data.username,
      roles: [data.role as Role],
      fullName: data.fullName,
      email: data.email,
      phone: data.phone,
      active: data.active,
      createdAt: data.createdAt,
      hospitalId: data.hospitalId,
      hospitalCode: data.hospitalCode,
      hospitalName: data.hospitalName,
      tenantSlug: data.tenantSlug,
    }
    setUser(u)
    localStorage.setItem('hms_auth', JSON.stringify({
      username: data.username,
      role: data.role,
      fullName: data.fullName,
      email: data.email,
      phone: data.phone,
      active: data.active,
      createdAt: data.createdAt,
      hospitalId: data.hospitalId,
      hospitalCode: data.hospitalCode,
      hospitalName: data.hospitalName,
      tenantSlug: data.tenantSlug,
      token: data.token,
      refreshToken: data.refreshToken,
    }))
  }, [])

  const logout = useCallback(() => {
    const auth = localStorage.getItem('hms_auth')
    if (auth) {
      try {
        const { refreshToken } = JSON.parse(auth)
        if (refreshToken) { apiClient.post('/auth/logout').catch(() => {}) }
      } catch { /* ignore */ }
    }
    setUser(null)
    localStorage.removeItem('hms_auth')
  }, [])

  const updateUser = useCallback((patch: Partial<User>) => {
    setUser((prev) => {
      if (!prev) return prev
      const updated = { ...prev, ...patch }
      const stored = localStorage.getItem('hms_auth')
      if (stored) {
        try {
          const parsed = JSON.parse(stored)
          localStorage.setItem('hms_auth', JSON.stringify({ ...parsed, ...patch, roles: undefined, role: parsed.role }))
        } catch { /* ignore */ }
      }
      return updated
    })
  }, [])

  useEffect(() => {
    setAuthClearCallback(logout)
    return () => setAuthClearCallback(null)
  }, [logout])

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
