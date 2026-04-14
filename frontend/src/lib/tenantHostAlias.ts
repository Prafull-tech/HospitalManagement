const tenantAliasStorageKey = 'hms_tenant_host_alias'

const knownTenantDomainSuffixes = ['.hms.com', '.hms.local']

function normalizeHost(value: string | null | undefined) {
  if (!value) return null
  const normalized = value.trim().toLowerCase().replace(/\/$/, '')
  return normalized || null
}

export function getTenantHostAliasFromPath(pathname: string) {
  const segment = pathname.trim().replace(/^\//, '').replace(/\/$/, '').toLowerCase()
  if (!segment || segment.includes('/')) return null
  return knownTenantDomainSuffixes.some((suffix) => segment.endsWith(suffix)) ? segment : null
}

export function getCurrentTenantHostAlias() {
  if (typeof window === 'undefined') return null

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

export function clearTenantHostAlias() {
  if (typeof window === 'undefined') return
  window.sessionStorage.removeItem(tenantAliasStorageKey)
}