import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import type { HMSRole } from '../config/sidebarMenu'
import { apiClient } from '../api/client'
import { setAuthClearCallback } from '../api/authRedirect'

export type Role = HMSRole

export interface User {
  username: string
  roles: Role[]
  fullName?: string
}

interface AuthState {
  user: User | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  hasRole: (...roles: Role[]) => boolean
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthState | null>(null)

function getInitialUser(): User | null {
  if (typeof window === 'undefined') return null
  try {
    const auth = localStorage.getItem('hms_auth')
    if (!auth) return null
    const parsed = JSON.parse(auth) as { username?: string; role?: string; fullName?: string }
    if (parsed?.username && parsed?.role) {
      return { username: parsed.username, roles: [parsed.role as Role], fullName: parsed.fullName }
    }
  } catch {
    // ignore
  }
  return null
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  console.log('Auth init start')
  const [user, setUser] = useState<User | null>(getInitialUser)
  const token =
    typeof window === 'undefined'
      ? null
      : (() => {
          try {
            const auth = localStorage.getItem('hms_auth')
            if (!auth) return null
            const p = JSON.parse(auth) as { token?: string }
            return p?.token ?? null
          } catch {
            return null
          }
        })()
  console.log('Token:', token != null ? '[present]' : 'null')
  console.log('Auth success (user from localStorage):', user?.username ?? 'none')
  console.log('Auth loading set false (sync init only)')

  const login = useCallback(async (username: string, password: string) => {
    const res = await apiClient.post('/auth/login', { username, password })
    const { token, role, fullName } = res.data as {
      token: string
      username: string
      role: string
      fullName: string
    }
    const u: User = { username, roles: [role as Role], fullName }
    setUser(u)
    localStorage.setItem('hms_auth', JSON.stringify({ username, role, fullName, token }))
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    localStorage.removeItem('hms_auth')
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

  const value: AuthState = {
    user,
    login,
    logout,
    hasRole,
    isAuthenticated: !!user,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

