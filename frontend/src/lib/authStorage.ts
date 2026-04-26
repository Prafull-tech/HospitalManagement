export const AUTH_STORAGE_KEY = 'hms_auth'

export interface StoredAuth {
  username?: string
  role?: string
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
  token?: string
  refreshToken?: string
  expiresAt?: string
  sessionExpiresAt?: string
}

function decodeJwtExpiry(token?: string): number | null {
  if (!token) return null
  const parts = token.split('.')
  if (parts.length < 2) return null

  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    const payload = JSON.parse(window.atob(padded)) as { exp?: number }
    return typeof payload.exp === 'number' ? payload.exp * 1000 : null
  } catch {
    return null
  }
}

export function getStoredAuth(): StoredAuth | null {
  if (typeof window === 'undefined') return null

  try {
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? JSON.parse(raw) as StoredAuth : null
  } catch {
    return null
  }
}

export function clearStoredAuth() {
  if (typeof window === 'undefined') return
  window.localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function saveStoredAuth(auth: StoredAuth) {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth))
}

export function getStoredAuthExpiryMs(auth: StoredAuth | null): number | null {
  if (!auth) return null

  const candidates = [auth.sessionExpiresAt, auth.expiresAt]
    .map((value) => (value ? Date.parse(value) : Number.NaN))
    .filter((value) => Number.isFinite(value))

  if (candidates.length > 0) {
    return candidates[0]
  }

  return decodeJwtExpiry(auth.token)
}

export function isStoredAuthExpired(auth: StoredAuth | null, now = Date.now()) {
  const expiryMs = getStoredAuthExpiryMs(auth)
  return expiryMs !== null && expiryMs <= now
}

export function getValidStoredAuth(now = Date.now()): StoredAuth | null {
  const auth = getStoredAuth()
  if (!auth) return null
  if (isStoredAuthExpired(auth, now)) {
    clearStoredAuth()
    return null
  }
  return auth
}