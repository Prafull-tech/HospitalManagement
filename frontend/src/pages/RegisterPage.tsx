import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { apiClient } from '../api/client'

const ROLES = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'PHARMACY_MANAGER', label: 'Pharmacy Manager' },
  { value: 'STORE_INCHARGE', label: 'Store In-charge' },
  { value: 'IPD_PHARMACIST', label: 'IPD Pharmacist' },
  { value: 'DOCTOR', label: 'Doctor' },
  { value: 'NURSE', label: 'Nurse' },
  { value: 'IPD_MANAGER', label: 'IPD Manager' },
  { value: 'BILLING', label: 'Billing' },
  { value: 'QUALITY_MANAGER', label: 'Quality Manager' },
]

export function RegisterPage() {
  const [username, setUsername] = useState('')
  const [fullName, setFullName] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [role, setRole] = useState('PHARMACY_MANAGER')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    if (!username.trim() || !fullName.trim() || !password || !confirm) {
      setError('All fields are required.')
      return
    }
    if (password !== confirm) {
      setError('Passwords do not match.')
      return
    }
    try {
      setLoading(true)
      await apiClient.post('/auth/register', {
        username: username.trim(),
        fullName: fullName.trim(),
        password,
        role,
      })
      setSuccess('User registered. You can now sign in.')
      setTimeout(() => navigate('/login'), 1200)
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.errors?.username ||
        'Registration failed.'
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
          'linear-gradient(rgba(15,23,42,0.65), rgba(15,23,42,0.8)), url(https://images.pexels.com/photos/2387419/pexels-photo-2387419.jpeg?auto=compress&cs=tinysrgb&w=1600)',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }}
    >
      <div
        className="card shadow-lg border-0"
        style={{
          width: '100%',
          maxWidth: 480,
          background: 'rgba(15,23,42,0.92)',
          color: '#f9fafb',
          borderRadius: 24,
        }}
      >
        <div className="card-body p-4 p-md-5">
          <div className="text-center mb-4">
            <h1 className="h5 mb-1 fw-bold">Request Access</h1>
            <p className="small text-muted mb-0">
              Register a new HMS user (admin-only in production).
            </p>
          </div>

          <form onSubmit={handleSubmit} className="d-flex flex-column gap-3">
            {error && (
              <div className="alert alert-danger py-2 mb-0 small" role="alert">
                {error}
              </div>
            )}
            {success && (
              <div className="alert alert-success py-2 mb-0 small" role="alert">
                {success}
              </div>
            )}

            <div className="row g-3">
              <div className="col-12 col-md-6">
                <label className="form-label small">Username</label>
                <input
                  className="form-control form-control-sm bg-transparent text-light"
                  style={{ borderRadius: 9999, borderColor: 'rgba(148,163,184,0.6)' }}
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="off"
                />
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label small">Full name</label>
                <input
                  className="form-control form-control-sm bg-transparent text-light"
                  style={{ borderRadius: 9999, borderColor: 'rgba(148,163,184,0.6)' }}
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                />
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label small">Password</label>
                <input
                  type="password"
                  className="form-control form-control-sm bg-transparent text-light"
                  style={{ borderRadius: 9999, borderColor: 'rgba(148,163,184,0.6)' }}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="new-password"
                />
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label small">Confirm password</label>
                <input
                  type="password"
                  className="form-control form-control-sm bg-transparent text-light"
                  style={{ borderRadius: 9999, borderColor: 'rgba(148,163,184,0.6)' }}
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  autoComplete="new-password"
                />
              </div>
              <div className="col-12">
                <label className="form-label small">Role</label>
                <select
                  className="form-select form-select-sm bg-transparent text-light"
                  style={{ borderRadius: 9999, borderColor: 'rgba(148,163,184,0.6)' }}
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                >
                  {ROLES.map((r) => (
                    <option key={r.value} value={r.value}>
                      {r.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-sm mt-2"
              style={{
                borderRadius: 9999,
                background:
                  'linear-gradient(90deg, rgba(59,130,246,0.95), rgba(56,189,248,0.95))',
                border: 'none',
                fontWeight: 600,
              }}
              disabled={loading}
            >
              {loading ? 'Submitting…' : 'Register'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

