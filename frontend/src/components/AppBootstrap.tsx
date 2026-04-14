import React, { createContext, useContext, useEffect, useState } from 'react'
import { getTenantContext, type TenantContextDto } from '../api/tenant'
import { getTenantHostAliasFromPath, setTenantHostAlias } from '../lib/tenantHostAlias'

const FailSafeMs = 2000

interface AppBootstrapContextValue {
  setReady: () => void
  tenant: TenantContextDto | null
  tenantLoading: boolean
}

const AppBootstrapContext = createContext<AppBootstrapContextValue | null>(null)

export function useAppBootstrap() {
  const ctx = useContext(AppBootstrapContext)
  return ctx
}

export function AppBootstrap({ children }: { children: React.ReactNode }) {
  const [ready, setReadyState] = useState(false)
  const [stuck, setStuck] = useState(false)
  const [tenant, setTenant] = useState<TenantContextDto | null>(null)
  const [tenantLoading, setTenantLoading] = useState(true)

  const setReady = () => setReadyState(true)

  useEffect(() => {
    let active = true

    const tenantAlias = typeof window !== 'undefined'
      ? getTenantHostAliasFromPath(window.location.pathname)
      : null

    if (tenantAlias) {
      setTenantHostAlias(tenantAlias)
    }

    getTenantContext()
      .then((data) => {
        if (!active) return
        setTenant(data)
        if (data.tenantResolved && data.hospitalName) {
          document.title = `${data.hospitalName} | HMS`
        } else if (data.platformHost) {
          document.title = 'HMS Platform | Login'
        }
      })
      .catch(() => {
        if (!active) return
        setTenant(null)
      })
      .finally(() => {
        if (active) {
          setTenantLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const t = setTimeout(() => setStuck(true), FailSafeMs)
    return () => clearTimeout(t)
  }, [])

  if (stuck && !ready) {
    return (
      <div
        data-hms="fail-safe"
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          padding: '1.5rem',
          background: 'var(--hms-bg, #f5f5f5)',
          color: 'var(--hms-text, #111)',
          textAlign: 'center',
        }}
      >
        <h2 style={{ marginBottom: '0.5rem' }}>Unable to initialize application</h2>
        <p style={{ color: 'var(--hms-text-secondary, #666)', marginBottom: '1rem' }}>
          The app did not finish loading in time. Check the console and Network tab. Is the backend
          running at http://localhost:8080?
        </p>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => {
            setReadyState(true)
            setStuck(false)
          }}
        >
          Try again
        </button>
      </div>
    )
  }

  if (!tenantLoading && tenant?.tenantResolved && tenant.active === false) {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          padding: '1.5rem',
          background: 'var(--hms-bg, #f5f5f5)',
          color: 'var(--hms-text, #111)',
          textAlign: 'center',
        }}
      >
        <h2 style={{ marginBottom: '0.5rem' }}>Hospital domain inactive</h2>
        <p style={{ color: 'var(--hms-text-secondary, #666)' }}>
          This hospital workspace is currently inactive. Contact the platform administrator.
        </p>
      </div>
    )
  }

  if (!tenantLoading && tenant && !tenant.platformHost && !tenant.tenantResolved) {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          padding: '1.5rem',
          background: 'var(--hms-bg, #f5f5f5)',
          color: 'var(--hms-text, #111)',
          textAlign: 'center',
        }}
      >
        <h2 style={{ marginBottom: '0.5rem' }}>Domain not connected</h2>
        <p style={{ color: 'var(--hms-text-secondary, #666)', maxWidth: '40rem' }}>
          {tenant.host || 'This host'} is not mapped to a hospital workspace yet. If you are onboarding a custom domain,
          complete domain verification and certificate issuance from the platform admin portal.
        </p>
      </div>
    )
  }

  return (
    <AppBootstrapContext.Provider value={{ setReady, tenant, tenantLoading }}>{children}</AppBootstrapContext.Provider>
  )
}
