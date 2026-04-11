import axios from 'axios'
import { getAuthRedirect, getAuthClearCallback } from './authRedirect'

const baseURL = '/api'

const REQUEST_TIMEOUT_MS = 10000

export const apiClient = axios.create({
  baseURL,
  timeout: REQUEST_TIMEOUT_MS,
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    delete (config.headers as Record<string, unknown>)['Content-Type']
  }
  const auth = localStorage.getItem('hms_auth')
  if (auth) {
    try {
      const { token } = JSON.parse(auth)
      if (token) {
        config.headers = config.headers ?? {}
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch {
      // ignore
    }
  }
  return config
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
      const auth = localStorage.getItem('hms_auth')
      let refreshToken: string | null = null
      if (auth) {
        try { refreshToken = JSON.parse(auth).refreshToken } catch { /* ignore */ }
      }

      if (refreshToken && !originalRequest.url?.includes('/auth/login') && !originalRequest.url?.includes('/auth/refresh')) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return apiClient(originalRequest)
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const res = await axios.post(`${baseURL}/auth/refresh`, { refreshToken })
          const { token: newToken, refreshToken: newRefresh, username, role, fullName } = res.data
          localStorage.setItem('hms_auth', JSON.stringify({ username, role, fullName, token: newToken, refreshToken: newRefresh }))
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          processQueue(null, newToken)
          return apiClient(originalRequest)
        } catch (refreshErr) {
          processQueue(refreshErr, null)
          localStorage.removeItem('hms_auth')
          getAuthClearCallback()?.()
          if (window.location.pathname !== '/login') {
            const go = getAuthRedirect()
            if (typeof go === 'function') go()
            else window.location.href = '/login'
          }
          return Promise.reject(refreshErr)
        } finally {
          isRefreshing = false
        }
      }

      localStorage.removeItem('hms_auth')
      getAuthClearCallback()?.()
      if (window.location.pathname !== '/login') {
        const go = getAuthRedirect()
        if (typeof go === 'function') go()
        else window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)
