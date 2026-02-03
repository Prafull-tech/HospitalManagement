import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { dashboardApi } from '../api/dashboard'
import type { DashboardStatsDto } from '../types/dashboard'

function UserPlusIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <line x1="19" y1="8" x2="19" y2="14" />
      <line x1="22" y1="11" x2="16" y2="11" />
    </svg>
  )
}

function SearchIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35" />
    </svg>
  )
}

function UsersIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}

function ClipboardIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <rect x="8" y="2" width="8" height="4" rx="1" ry="1" />
      <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
      <path d="M12 11h4" />
      <path d="M12 16h4" />
      <path d="M8 11h.01" />
      <path d="M8 16h.01" />
    </svg>
  )
}

const today = new Date().toISOString().slice(0, 10)

export function ReceptionDashboard() {
  const { user, hasRole } = useAuth()
  const canRegister = !user || hasRole('ADMIN', 'RECEPTIONIST')
  const [fromDate, setFromDate] = useState(today)
  const [toDate, setToDate] = useState(today)
  const [stats, setStats] = useState<DashboardStatsDto | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    dashboardApi
      .getStats({ fromDate, toDate })
      .then(setStats)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load stats')
        setStats(null)
      })
      .finally(() => setLoading(false))
  }, [fromDate, toDate])

  const handlePrint = () => {
    if (!stats) return
    const win = window.open('', '_blank')
    if (!win) return
    win.document.write(`
      <!DOCTYPE html><html><head><title>Hospital Stats</title>
      <style>body{font-family:sans-serif;padding:1.5rem;} table{border-collapse:collapse;width:100%;} th,td{border:1px solid #ccc;padding:0.5rem 1rem;text-align:left;} th{background:#f5f5f5;}</style>
      </head><body>
      <h1>Hospital Statistics Report</h1>
      <p>From: ${stats.fromDate} &nbsp; To: ${stats.toDate}</p>
      <table class="table table-striped">
        <tr><th>Metric</th><th>Count</th></tr>
        <tr><td>Patients Registered</td><td>${stats.totalPatientsRegistered}</td></tr>
        <tr><td>Total OPD Visits</td><td>${stats.totalOPDVisits}</td></tr>
        <tr><td>Total Admitted</td><td>${stats.totalAdmitted}</td></tr>
        <tr><td>Total Discharged</td><td>${stats.totalDischarged}</td></tr>
        <tr><td>Currently Admitted (IPD)</td><td>${stats.totalCurrentlyAdmitted}</td></tr>
        <tr><td>Total Collection</td><td>₹${stats.totalCollection.toFixed(2)}</td></tr>
      </table>
      </body></html>
    `)
    win.document.close()
    win.focus()
    setTimeout(() => { win.print(); win.close(); }, 250)
  }

  const handleDownload = () => {
    if (!stats) return
    const lines = [
      'Hospital Statistics Report',
      `From: ${stats.fromDate} To: ${stats.toDate}`,
      '',
      'Metric,Count',
      `Total Patients Registered,${stats.totalPatientsRegistered}`,
      `Total OPD Visits,${stats.totalOPDVisits}`,
      `Total Admitted,${stats.totalAdmitted}`,
      `Total Discharged,${stats.totalDischarged}`,
      `Currently Admitted (IPD),${stats.totalCurrentlyAdmitted}`,
      `Total Collection,${stats.totalCollection}`,
    ]
    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `hospital-stats-${stats.fromDate}-to-${stats.toDate}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="d-flex flex-column gap-3">
      <div>
        <h2 className="h5 mb-1 fw-bold">Reception</h2>
        <p className="text-muted small mb-0">Patient registration and lookup</p>
      </div>

      <div className="card shadow-sm">
        <div className="card-header">
          <h3 className="h6 mb-0 fw-bold">Date range &amp; report</h3>
        </div>
        <div className="card-body">
          <div className="row g-2 align-items-end flex-wrap">
            <div className="col-auto">
              <label className="form-label small mb-0">From</label>
              <input
                type="date"
                className="form-control form-control-sm"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
              />
            </div>
            <div className="col-auto">
              <label className="form-label small mb-0">To</label>
              <input
                type="date"
                className="form-control form-control-sm"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
              />
            </div>
            <div className="col-auto">
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={handlePrint} disabled={!stats}>
                Print
              </button>
              <button type="button" className="btn btn-outline-primary btn-sm" onClick={handleDownload} disabled={!stats}>
                Download CSV
              </button>
            </div>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0" role="alert">{error}</div>}
      {loading && <p className="text-muted mb-0">Loading…</p>}

      <div className="row g-3">
        <div className="col-12 col-md-6 col-xl-4">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <UsersIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">{stats != null ? stats.totalPatientsRegistered : '—'}</div>
                <div className="small text-muted">Patients Registered (date range)</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6 col-xl-4">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-success bg-opacity-10 p-2 text-success">
                <ClipboardIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">{stats != null ? stats.totalOPDVisits : '—'}</div>
                <div className="small text-muted">Total OPD Visits</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6 col-xl-4">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-info bg-opacity-10 p-2 text-info">
                <UsersIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">{stats != null ? stats.totalAdmitted : '—'}</div>
                <div className="small text-muted">Total Admitted</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6 col-xl-4">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-warning bg-opacity-10 p-2 text-warning">
                <ClipboardIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">{stats != null ? stats.totalDischarged : '—'}</div>
                <div className="small text-muted">Total Discharged</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6 col-xl-4">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <UsersIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">{stats != null ? stats.totalCurrentlyAdmitted : '—'}</div>
                <div className="small text-muted">Currently Admitted (IPD)</div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-6 col-xl-4">
          <div className="card shadow-sm h-100">
            <div className="card-body d-flex align-items-start gap-3">
              <div className="rounded-3 bg-success bg-opacity-10 p-2 text-success">
                <ClipboardIcon />
              </div>
              <div>
                <div className="fs-4 fw-bold">{stats != null ? `₹${stats.totalCollection.toFixed(2)}` : '—'}</div>
                <div className="small text-muted">Total Collection</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm bg-light">
        <div className="card-body py-3">
          <p className="fw-semibold mb-1">Welcome back</p>
          <p className="text-muted small mb-0">Signed in as {user?.username ?? 'Guest'}. Choose an action below.</p>
        </div>
      </div>

      <div className="row g-3">
        {canRegister && (
          <div className="col-12 col-md-6">
            <Link to="/reception/register" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
              <div className="card-body d-flex align-items-center gap-3">
                <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                  <UserPlusIcon />
                </span>
                <div>
                  <span className="fw-bold d-block">Register Patient</span>
                  <span className="small text-muted">Create new patient and get UHID</span>
                </div>
              </div>
            </Link>
          </div>
        )}
        <div className="col-12 col-md-6">
          <Link to="/reception/search" className="card shadow-sm text-decoration-none text-body h-100 border-primary border-opacity-25">
            <div className="card-body d-flex align-items-center gap-3">
              <span className="rounded-3 bg-primary bg-opacity-10 p-2 text-primary">
                <SearchIcon />
              </span>
              <div>
                <span className="fw-bold d-block">Search Patient</span>
                <span className="small text-muted">Find by UHID, phone, or name</span>
              </div>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
