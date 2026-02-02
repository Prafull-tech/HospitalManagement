import axios from 'axios'

const baseURL = '/api'

export const apiClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

/* Auth disabled for now. When re-enabling: add request interceptor to send Basic auth from localStorage. */
apiClient.interceptors.request.use((config) => {
  const auth = localStorage.getItem('hms_auth')
  if (auth) {
    try {
      const { username, password } = JSON.parse(auth)
      if (username && password) {
        config.auth = { username, password }
      }
    } catch {
      // ignore
    }
  }
  return config
})

/* Auth disabled: no redirect on 401. When re-enabling: redirect to /login on 401. */
apiClient.interceptors.response.use(
  (res) => res,
  (err) => Promise.reject(err)
)
