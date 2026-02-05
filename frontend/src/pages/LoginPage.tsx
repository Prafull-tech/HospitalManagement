import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/reception'

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!username.trim() || !password) {
      setError('Username and password are required.')
      return
    }
    try {
      setLoading(true)
      await login(username.trim(), password)
      navigate(from, { replace: true })
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'Invalid username or password.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      className="min-vh-100 d-flex align-items-center justify-content-center"
      style={{
        backgroundImage:
          'linear-gradient(rgba(15,23,42,0.65), rgba(15,23,42,0.8)), url(https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=1600)',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }}
    >
      <div
        className="card shadow-lg border-0"
        style={{
          width: '100%',
          maxWidth: 420,
          background: 'rgba(15,23,42,0.9)',
          color: '#f9fafb',
          borderRadius: 24,
        }}
      >
        <div className="card-body p-4 p-md-5">
          <div className="text-center mb-4">
            <h1 className="h5 mb-1 fw-bold">HMS Login</h1>
            <p
              className="small mb-0"
              style={{ color: 'rgba(248,250,252,0.78)' }}
            >
              Have an account? Sign in to continue.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="d-flex flex-column gap-3">
            {error && (
              <div className="alert alert-danger py-2 mb-0 small" role="alert">
                {error}
              </div>
            )}

            <div>
              <label className="form-label small">Username</label>
              <input
                type="text"
                className="form-control form-control-sm bg-transparent text-light"
                style={{ borderRadius: 9999, borderColor: 'rgba(148,163,184,0.6)' }}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                placeholder="e.g. pharm, nurse"
              />
            </div>
            <div>
              <label className="form-label small">Password</label>
              <input
                type="password"
                className="form-control form-control-sm bg-transparent text-light"
                style={{ borderRadius: 9999, borderColor: 'rgba(148,163,184,0.6)' }}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              className="btn btn-sm mt-2"
              style={{
                borderRadius: 9999,
                background:
                  'linear-gradient(90deg, rgba(249,115,22,0.95), rgba(248,150,30,0.95))',
                border: 'none',
                fontWeight: 600,
              }}
              disabled={loading}
            >
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          <div className="d-flex justify-content-between align-items-center mt-3 small">
            <div className="form-check">
              <input
                id="rememberMe"
                type="checkbox"
                className="form-check-input"
                defaultChecked
              />
              <label
                className="form-check-label"
                htmlFor="rememberMe"
                style={{ color: 'rgba(248,250,252,0.72)' }}
              >
                Remember me
              </label>
            </div>
            <button
              className="btn btn-link btn-sm p-0"
              type="button"
              style={{ color: 'rgba(248,250,252,0.72)' }}
            >
              Forgot password
            </button>
          </div>

          <hr className="border-secondary my-3" />

          <div
            className="text-center small"
            style={{ color: 'rgba(248,250,252,0.78)' }}
          >
            New to HMS?{' '}
            <Link to="/register" className="link-light text-decoration-underline">
              Request access
            </Link>
          </div>

          <p
            className="small mt-3 mb-0"
            style={{ color: 'rgba(248,250,252,0.8)' }}
          >
            Dev users (profile=dev): admin/admin123, pharm/pharm123, store/store123, ipdph/ipdph123, doctor/doctor123, nurse/nurse123, ipd/ipd123, bill/bill123, quality/quality123
          </p>
        </div>
      </div>
    </div>
  )
}
