/**
 * Optional in-app redirect on 401. Set from a component that has useNavigate()
 * so we can redirect without full page reload (avoids showing static "Loading…" again).
 */
let redirectToLogin: (() => void) | null = null
let clearAuthCallback: (() => void) | null = null

export function setAuthRedirect(fn: (() => void) | null) {
  redirectToLogin = fn
}

export function getAuthRedirect(): (() => void) | null {
  return redirectToLogin
}

/** Register callback to clear auth state on 401 (e.g. call logout). */
export function setAuthClearCallback(fn: (() => void) | null) {
  clearAuthCallback = fn
}

export function getAuthClearCallback(): (() => void) | null {
  return clearAuthCallback
}
