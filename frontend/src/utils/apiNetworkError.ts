import axios from 'axios'

const BACKEND_HINT =
  'Cannot reach the API (port 8080). Start the Spring Boot backend and keep the "HMS Backend" window open if you used start.bat.'

/** Use when a request fails so users see a clear message if the backend is not running. */
export function apiErrorWithNetworkHint(fallbackMessage: string, err: unknown): string {
  const network =
    axios.isAxiosError(err) &&
    (!err.response || err.code === 'ECONNREFUSED' || err.code === 'ERR_NETWORK')
  return network ? BACKEND_HINT : fallbackMessage
}
