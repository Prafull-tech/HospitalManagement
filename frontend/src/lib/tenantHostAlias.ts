const tenantAliasStorageKey = 'hms_tenant_host_alias'

const knownTenantDomainSuffixes = ['.hms.com', '.hms.local']
// Treat auth + marketing routes as "platform context" so we don't accidentally
// reuse a previous tenant alias (stored in sessionStorage) on the login screen.
const platformRoutePrefixes = ['/super-admin', '/home', '/login', '/signup', '/contact', '/blog', '/register', '/unauthorized']

function isLocalHostname(hostname: string | null | undefined) {
  if (!hostname) return true
  const normalizedHost = hostname.trim().toLowerCase()
  return normalizedHost === 'localhost'
    || normalizedHost === '127.0.0.1'
    || normalizedHost === '0.0.0.0'
    || normalizedHost.endsWith('.localhost')
}

function getTenantBaseDomain() {
  const configuredBaseDomain = (import.meta.env.VITE_TENANT_BASE_DOMAIN || '').trim().toLowerCase()
  if (configuredBaseDomain) {
    return configuredBaseDomain.replace(/^\./, '')
  }

  if (typeof window !== 'undefined' && isLocalHostname(window.location.hostname)) {
    return 'hms.local'
  }

  return 'hms.com'
}

function normalizeHost(value: string | null | undefined) {
  if (!value) return null
  const normalized = value.trim().toLowerCase().replace(/\/$/, '')
  return normalized || null
}

function isPlatformRoutePath(pathname: string | null | undefined) {
  if (!pathname) return true
  const normalizedPath = pathname.trim()
  if (!normalizedPath || normalizedPath === '/') return true
  return platformRoutePrefixes.some((prefix) => normalizedPath === prefix || normalizedPath.startsWith(prefix + '/'))
}

export function getTenantHostAliasFromPath(pathname: string) {
  const segment = pathname.trim().replace(/^\//, '').replace(/\/$/, '').toLowerCase()
  if (!segment || segment.includes('/')) return null
  return knownTenantDomainSuffixes.some((suffix) => segment.endsWith(suffix)) ? segment : null
}

export function getCurrentTenantHostAlias() {
  if (typeof window === 'undefined') return null

  if (isPlatformRoutePath(window.location.pathname)) {
    window.sessionStorage.removeItem(tenantAliasStorageKey)
    return null
  }

  const aliasFromPath = getTenantHostAliasFromPath(window.location.pathname)
  if (aliasFromPath) {
    window.sessionStorage.setItem(tenantAliasStorageKey, aliasFromPath)
    return aliasFromPath
  }

  return normalizeHost(window.sessionStorage.getItem(tenantAliasStorageKey))
}

export function setTenantHostAlias(alias: string | null) {
  if (typeof window === 'undefined') return

  const normalized = normalizeHost(alias)
  if (!normalized) {
    window.sessionStorage.removeItem(tenantAliasStorageKey)
    return
  }

  window.sessionStorage.setItem(tenantAliasStorageKey, normalized)
}

export function buildTenantHostAlias(tenantSlug: string | null | undefined) {
  const normalizedSlug = normalizeHost(tenantSlug)
  if (!normalizedSlug) {
    return null
  }
  if (knownTenantDomainSuffixes.some((suffix) => normalizedSlug.endsWith(suffix))) {
    return normalizedSlug
  }
  return `${normalizedSlug}.${getTenantBaseDomain()}`
}

export function clearTenantHostAlias() {
  if (typeof window === 'undefined') return
  window.sessionStorage.removeItem(tenantAliasStorageKey)
}