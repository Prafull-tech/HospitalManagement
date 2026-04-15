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
  login: (username: string, password: string) => Promise<void>
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

  const login = useCallback(async (username: string, password: string) => {
    const res = await apiClient.post('/auth/login', { username, password })
    const data = res.data as {
      token: string; refreshToken: string; username: string; role: string
      fullName: string; email?: string; phone?: string; active?: boolean; createdAt?: string
      hospitalId?: number | null; hospitalCode?: string; hospitalName?: string; tenantSlug?: string
      expiresAt?: string; sessionExpiresAt?: string
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
      expiresAt: data.expiresAt,
      sessionExpiresAt: data.sessionExpiresAt,
    }
    setUser(u)
    saveStoredAuth({
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
      expiresAt: data.expiresAt,
      sessionExpiresAt: data.sessionExpiresAt,
    })
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
