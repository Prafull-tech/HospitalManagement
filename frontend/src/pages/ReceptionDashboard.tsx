import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { dashboardApi } from '../api/dashboard'
import type { DashboardStatsDto } from '../types/dashboard'
import shared from '../styles/Dashboard.module.css'
import styles from './ReceptionDashboard.module.css'

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
      <table>
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
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h2 className={shared.pageTitle}>Reception</h2>
        <p className={shared.pageSubtitle}>Patient registration and lookup</p>
      </div>

      <div className={styles.dateFilter}>
        <label>
          <span className={styles.dateLabel}>From</span>
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className={styles.dateInput}
          />
        </label>
        <label>
          <span className={styles.dateLabel}>To</span>
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className={styles.dateInput}
          />
        </label>
        <button type="button" onClick={handlePrint} className={styles.printBtn} disabled={!stats}>
          Print
        </button>
        <button type="button" onClick={handleDownload} className={styles.downloadBtn} disabled={!stats}>
          Download
        </button>
      </div>

      {error && <div className={styles.error}>{error}</div>}
      {loading && <div className={styles.loading}>Loading…</div>}

      <div className={shared.statsRow}>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.primary}`}>
            <UsersIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>{stats != null ? stats.totalPatientsRegistered : '—'}</span>
            <span className={shared.statLabel}>Patients Registered (date range)</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.success}`}>
            <ClipboardIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>{stats != null ? stats.totalOPDVisits : '—'}</span>
            <span className={shared.statLabel}>Total OPD Visits</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.info}`}>
            <UsersIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>{stats != null ? stats.totalAdmitted : '—'}</span>
            <span className={shared.statLabel}>Total Admitted</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.warning}`}>
            <ClipboardIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>{stats != null ? stats.totalDischarged : '—'}</span>
            <span className={shared.statLabel}>Total Discharged</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.primary}`}>
            <UsersIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>{stats != null ? stats.totalCurrentlyAdmitted : '—'}</span>
            <span className={shared.statLabel}>Currently Admitted (IPD)</span>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={`${shared.statIconWrap} ${shared.success}`}>
            <ClipboardIcon />
          </div>
          <div className={shared.statContent}>
            <span className={shared.statValue}>{stats != null ? `₹${stats.totalCollection.toFixed(2)}` : '—'}</span>
            <span className={shared.statLabel}>Total Collection</span>
          </div>
        </div>
      </div>

      <div className={shared.welcomeCard}>
        <p className={shared.welcomeTitle}>Welcome back</p>
        <p className={shared.welcomeText}>Signed in as {user?.username ?? 'Guest'}. Choose an action below.</p>
      </div>

      <div className={shared.cardsGrid}>
        {canRegister && (
          <Link to="/reception/register" className={shared.actionCard}>
            <span className={shared.actionCardIcon}>
              <UserPlusIcon />
            </span>
            <div className={shared.actionCardBody}>
              <span className={shared.actionCardTitle}>Register Patient</span>
              <span className={shared.actionCardDesc}>Create new patient and get UHID</span>
            </div>
          </Link>
        )}
        <Link to="/reception/search" className={shared.actionCard}>
          <span className={shared.actionCardIcon}>
            <SearchIcon />
          </span>
          <div className={shared.actionCardBody}>
            <span className={shared.actionCardTitle}>Search Patient</span>
            <span className={shared.actionCardDesc}>Find by UHID, phone, or name</span>
          </div>
        </Link>
      </div>
    </div>
  )
}
