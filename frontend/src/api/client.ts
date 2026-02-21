import axios from 'axios'
import { getAuthRedirect, getAuthClearCallback } from './authRedirect'

const baseURL = '/api'

const REQUEST_TIMEOUT_MS = 10000 // 10 seconds – avoid infinite loading when backend is down

export const apiClient = axios.create({
  baseURL,
  timeout: REQUEST_TIMEOUT_MS,
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT bearer token when available; for FormData, let browser set Content-Type with boundary
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

// Redirect to login on 401 (in-app navigate when available to avoid full reload / static loader)
apiClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem('hms_auth')
      getAuthClearCallback()?.() // Clear auth state so LoginPage doesn't redirect back
      if (window.location.pathname !== '/login') {
        const go = getAuthRedirect()
        if (typeof go === 'function') {
          go()
        } else {
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(err)
  }
)
