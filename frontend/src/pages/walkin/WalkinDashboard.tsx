/**
 * Walk-in Dashboard – Walk-ins today, waiting, consulted, emergency.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { walkinApi } from '../../api/walkin'
import type { WalkInDashboard as DashboardType } from '../../types/walkin.types'

export function WalkinDashboard() {
  const [stats, setStats] = useState<DashboardType | null>(null)
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    walkinApi
      .getDashboard(date)
      .then(setStats)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load dashboard.')
        setStats(null)
      })
      .finally(() => setLoading(false))
  }, [date])

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/walkin">Walk-in</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Dashboard</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Walk-in Registration</h1>
        <Link to="/front-office/walkin/register" className="btn btn-primary">
          Register Walk-in
        </Link>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}

      <div className="row g-3">
        <div className="col-md-3">
          <div className="card shadow-sm border-primary">
            <div className="card-body">
              <h6 className="text-muted mb-1">Walk-ins Today</h6>
              <p className="h3 mb-0">{loading ? '—' : (stats?.walkInsToday ?? 0)}</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card shadow-sm border-warning">
            <div className="card-body">
              <h6 className="text-muted mb-1">Patients Waiting</h6>
              <p className="h3 mb-0">{loading ? '—' : (stats?.patientsWaiting ?? 0)}</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card shadow-sm border-info">
            <div className="card-body">
              <h6 className="text-muted mb-1">In Consultation</h6>
              <p className="h3 mb-0">{loading ? '—' : (stats?.inConsultation ?? 0)}</p>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card shadow-sm border-success">
            <div className="card-body">
              <h6 className="text-muted mb-1">Consulted</h6>
              <p className="h3 mb-0">{loading ? '—' : (stats?.patientsConsulted ?? 0)}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm">
        <div className="card-header d-flex align-items-center justify-content-between">
          <span>Today&apos;s Summary</span>
          <input
            type="date"
            className="form-control form-control-sm"
            style={{ width: 'auto' }}
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-6">
              <p className="mb-0">
                <strong>Emergency Walk-ins:</strong>{' '}
                <span className="badge bg-danger">{loading ? '—' : (stats?.emergencyWalkIns ?? 0)}</span>
              </p>
            </div>
            <div className="col-md-6">
              <Link to="/front-office/tokens" className="btn btn-outline-secondary btn-sm">
                View Token Queue
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
