/**
 * Centralized API error extraction for Pharmacy module.
 * Never expose raw stack traces to UI.
 */

export interface ApiErrorResponse {
  response?: {
    data?: { message?: string; errors?: Record<string, string> }
    status?: number
  }
}

export function getPharmacyErrorMessage(err: unknown, fallback: string): string {
  const apiErr = err as ApiErrorResponse
  const data = apiErr?.response?.data
  const msg = data?.message
  const errors = data?.errors
  if (typeof msg === 'string' && msg.trim()) {
    if (errors && typeof errors === 'object' && Object.keys(errors).length > 0) {
      const fieldErrors = Object.entries(errors)
        .map(([field, text]) => `${field}: ${text}`)
        .join('. ')
      return `${msg}. ${fieldErrors}`
    }
    return msg
  }
  const status = apiErr?.response?.status
  if (status === 401 || status === 403) return 'You are not authorized for this action.'
  if (status && status >= 500) return 'Service temporarily unavailable. Please try again.'
  return fallback
}
