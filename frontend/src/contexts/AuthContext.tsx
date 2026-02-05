import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import type { HMSRole } from '../config/sidebarMenu'
import { apiClient } from '../api/client'

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

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)

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

  const hasRole = useCallback(
    (...roles: Role[]) => {
      if (!user) return false
      return roles.some((r) => user.roles.includes(r))
    },
    [user]
  )

  useEffect(() => {
    const auth = localStorage.getItem('hms_auth')
    if (auth) {
      try {
        const parsed = JSON.parse(auth) as { username: string; role?: string; fullName?: string }
        if (parsed?.username && parsed?.role) {
          setUser({ username: parsed.username, roles: [parsed.role as Role], fullName: parsed.fullName })
        } else {
          setUser(null)
        }
      } catch {
        setUser(null)
      }
    }
  }, [])

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

