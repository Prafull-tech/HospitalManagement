import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import type { HMSRole } from '../config/sidebarMenu'

export type Role = HMSRole

export interface User {
  username: string
  roles: Role[]
}

interface AuthState {
  user: User | null
  login: (username: string, password: string) => void
  logout: () => void
  hasRole: (...roles: Role[]) => boolean
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthState | null>(null)

const DEMO_USERS: Record<string, { password: string; roles: Role[] }> = {
  admin: { password: 'admin123', roles: ['ADMIN'] },
  receptionist: { password: 'rec123', roles: ['RECEPTIONIST'] },
  doctor: { password: 'doc123', roles: ['DOCTOR'] },
  nurse: { password: 'nurse123', roles: ['NURSE'] },
  lab: { password: 'lab123', roles: ['LAB_TECH'] },
  pharmacist: { password: 'pharma123', roles: ['PHARMACIST'] },
  billing: { password: 'bill123', roles: ['BILLING'] },
  itadmin: { password: 'it123', roles: ['IT_ADMIN'] },
  helpdesk: { password: 'help123', roles: ['HELP_DESK'] },
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)

  const login = useCallback((username: string, password: string) => {
    const key = username.toLowerCase()
    const demo = DEMO_USERS[key]
    if (demo && demo.password === password) {
      const u: User = { username: key, roles: demo.roles }
      setUser(u)
      localStorage.setItem('hms_auth', JSON.stringify({ username: key, password }))
      return
    }
    setUser({ username, roles: ['HELP_DESK'] })
    localStorage.setItem('hms_auth', JSON.stringify({ username, password }))
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
        const { username } = JSON.parse(auth)
        const key = (username || '').toLowerCase()
        const demo = DEMO_USERS[key]
        if (demo) {
          setUser({ username: key, roles: demo.roles })
        } else {
          setUser({ username: key, roles: ['HELP_DESK'] })
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
