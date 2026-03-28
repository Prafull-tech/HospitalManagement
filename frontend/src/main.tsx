import React, { useState, useEffect } from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import App from './App'
import { AuthProvider } from './contexts/AuthContext'
import { ThemeProvider } from './contexts/ThemeContext'
import { queryClient } from './lib/queryClient'
import { AppBootstrap } from './components/AppBootstrap'
import { AppErrorBoundary } from './components/AppErrorBoundary'
import 'bootstrap/dist/css/bootstrap.min.css'
import './index.css'

const rootEl = document.getElementById('root')!
rootEl.innerHTML = ''

function FullApp() {
  return (
    <AppErrorBoundary>
      <AppBootstrap>
        <BrowserRouter>
          <QueryClientProvider client={queryClient}>
            <ThemeProvider>
              <AuthProvider>
                <App />
              </AuthProvider>
            </ThemeProvider>
          </QueryClientProvider>
        </BrowserRouter>
      </AppBootstrap>
    </AppErrorBoundary>
  )
}

/** Renders a minimal first frame so React always paints; then mounts full app (isolates mount failures). */
function Root() {
  const [showApp, setShowApp] = useState(false)
  useEffect(() => {
    const id = requestAnimationFrame(() => setShowApp(true))
    return () => cancelAnimationFrame(id)
  }, [])

  if (!showApp) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontFamily: 'sans-serif',
          fontSize: '1rem',
          color: '#333',
        }}
      >
        Loading…
      </div>
    )
  }
  return <FullApp />
}

ReactDOM.createRoot(rootEl).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>,
)
