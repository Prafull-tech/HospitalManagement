import React, { createContext, useContext, useEffect, useState } from 'react'

const FailSafeMs = 2000

interface AppBootstrapContextValue {
  setReady: () => void
}

const AppBootstrapContext = createContext<AppBootstrapContextValue | null>(null)

export function useAppBootstrap() {
  const ctx = useContext(AppBootstrapContext)
  return ctx
}

export function AppBootstrap({ children }: { children: React.ReactNode }) {
  const [ready, setReadyState] = useState(false)
  const [stuck, setStuck] = useState(false)

  const setReady = () => setReadyState(true)

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

  return (
    <AppBootstrapContext.Provider value={{ setReady }}>{children}</AppBootstrapContext.Provider>
  )
}
