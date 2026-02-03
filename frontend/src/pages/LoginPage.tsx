import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/reception'

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!username.trim() || !password) {
      setError('Username and password are required.')
      return
    }
    login(username.trim(), password)
    navigate(from, { replace: true })
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center p-3 bg-light">
      <div className="card shadow-sm" style={{ width: '100%', maxWidth: '380px' }}>
        <div className="card-body p-4">
          <h1 className="h4 mb-1 fw-bold text-primary">HMS</h1>
          <p className="text-muted small mb-4">Hospital Management System — Reception</p>
          <form onSubmit={handleSubmit} className="d-flex flex-column gap-3">
            {error && (
              <div className="alert alert-danger py-2 mb-0" role="alert">
                {error}
              </div>
            )}
            <div className="mb-0">
              <label className="form-label small fw-medium">Username</label>
              <input
                type="text"
                className="form-control"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                placeholder="e.g. receptionist"
              />
            </div>
            <div className="mb-0">
              <label className="form-label small fw-medium">Password</label>
              <input
                type="password"
                className="form-control"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                placeholder="••••••••"
              />
            </div>
            <button type="submit" className="btn btn-primary mt-2">
              Sign in
            </button>
          </form>
          <p className="small text-muted mt-3 mb-0">
            Demo: admin / admin123, receptionist / rec123, helpdesk / help123
          </p>
        </div>
      </div>
    </div>
  )
}
