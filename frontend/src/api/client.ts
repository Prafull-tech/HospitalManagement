import axios from 'axios'
import { getAuthRedirect, getAuthClearCallback } from './authRedirect'
import { getCurrentTenantHostAlias } from '../lib/tenantHostAlias'
import { clearStoredAuth, getValidStoredAuth, saveStoredAuth } from '../lib/authStorage'

const baseURL = '/api'

const REQUEST_TIMEOUT_MS = 10000

export const apiClient = axios.create({
  baseURL,
  timeout: REQUEST_TIMEOUT_MS,
  headers: { 'Content-Type': 'application/json' },
})

let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = []

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach((prom) => {
    if (token) prom.resolve(token)
    else prom.reject(error)
  })
  failedQueue = []
}

function clearAuthAndRedirect() {
  clearStoredAuth()
  getAuthClearCallback()?.()
  if (window.location.pathname !== '/login') {
    const go = getAuthRedirect()
    if (typeof go === 'function') go()
    else window.location.href = '/login'
  }
}

apiClient.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    delete (config.headers as Record<string, unknown>)['Content-Type']
  }

  const tenantHostAlias = getCurrentTenantHostAlias()
  if (tenantHostAlias) {
    config.headers = config.headers ?? {}
    config.headers['X-HMS-Tenant-Host'] = tenantHostAlias
  }

  const auth = getValidStoredAuth()
  if (auth?.token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${auth.token}`
  }

  return config
})

apiClient.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config
    const data = err?.response?.data
    const status = err?.response?.status

    if (data && typeof data.detail === 'string' && data.detail.length > 0) {
      data.message = (data.message || 'Error') + ' [' + data.detail + ']'
    }

    if ((status === 401 || status === 403) && !originalRequest?._retry) {
      const auth = getValidStoredAuth()
      const refreshToken = auth?.refreshToken ?? null

      if (refreshToken && !originalRequest.url?.includes('/auth/login') && !originalRequest.url?.includes('/auth/refresh')) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then((token) => {
            originalRequest.headers = originalRequest.headers ?? {}
            originalRequest.headers.Authorization = `Bearer ${token}`
            return apiClient(originalRequest)
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const res = await axios.post(`${baseURL}/auth/refresh`, { refreshToken })
          const {
            token: newToken,
            refreshToken: newRefresh,
            username,
            role,
            fullName,
            email,
            phone,
            active,
            mustChangePassword,
            createdAt,
            hospitalId,
            hospitalCode,
            hospitalName,
            tenantSlug,
            expiresAt,
            sessionExpiresAt,
          } = res.data

          saveStoredAuth({
            username,
            role,
            fullName,
            email,
            phone,
            active,
            mustChangePassword,
            createdAt,
            hospitalId,
            hospitalCode,
            hospitalName,
            tenantSlug,
            token: newToken,
            refreshToken: newRefresh,
            expiresAt,
            sessionExpiresAt,
          })

          originalRequest.headers = originalRequest.headers ?? {}
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          processQueue(null, newToken)
          return apiClient(originalRequest)
        } catch (refreshErr) {
          processQueue(refreshErr, null)
          clearAuthAndRedirect()
          return Promise.reject(refreshErr)
        } finally {
          isRefreshing = false
        }
      }

      clearAuthAndRedirect()
    }

    return Promise.reject(err)
  }
)
