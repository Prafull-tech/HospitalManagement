import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { dashboardApi } from '../api/dashboard'
import { appointmentApi } from '../api/appointment'
import type { DashboardStatsDto } from '../types/dashboard'
import type { AppointmentDashboard as AppointmentDashboardType, AppointmentResponse } from '../types/appointment.types'
import styles from './ReceptionDashboardAdmin.module.css'

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
  const [rangePreset, setRangePreset] = useState<'LAST_7_DAYS' | 'LAST_30_DAYS' | 'THIS_YEAR' | 'PREVIOUS_YEAR'>('LAST_7_DAYS')
  const [stats, setStats] = useState<DashboardStatsDto | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [appointmentDash, setAppointmentDash] = useState<AppointmentDashboardType | null>(null)
  const [appointmentsLoading, setAppointmentsLoading] = useState(false)
  const [appointmentsError, setAppointmentsError] = useState('')

  function isoDate(d: Date) {
    return d.toISOString().slice(0, 10)
  }

  function computeRange(preset: typeof rangePreset) {
    const now = new Date()
    const to = isoDate(now)
    if (preset === 'LAST_30_DAYS') {
      const from = new Date(now)
      from.setDate(from.getDate() - 29)
      return { from: isoDate(from), to }
    }
    if (preset === 'THIS_YEAR') {
      const from = new Date(now.getFullYear(), 0, 1)
      return { from: isoDate(from), to }
    }
    if (preset === 'PREVIOUS_YEAR') {
      const year = now.getFullYear() - 1
      const from = new Date(year, 0, 1)
      const prevEnd = new Date(year, 11, 31)
      return { from: isoDate(from), to: isoDate(prevEnd) }
    }
    // Default LAST_7_DAYS
    const from = new Date(now)
    from.setDate(from.getDate() - 6)
    return { from: isoDate(from), to }
  }

  function formatTime(s: string) {
    if (!s) return '—'
    const m = s.match(/^(\d{1,2}):(\d{2})/)
    return m ? `${m[1].padStart(2, '0')}:${m[2]}` : s
  }

  function clamp(n: number, min: number, max: number) {
    return Math.max(min, Math.min(max, n))
  }

  function pct(n: number, d: number) {
    if (!d) return 0
    return clamp(Math.round((n / d) * 100), 0, 100)
  }

  function scheduleTheme(a: AppointmentResponse['status']) {
    if (a === 'CONFIRMED' || a === 'COMPLETED') return styles.scheduleEventGreen
    if (a === 'BOOKED' || a === 'PENDING_CONFIRMATION') return styles.scheduleEventAmber
    return styles.scheduleEventRed
  }

  function formatMoney(amount: number) {
    // Keep it simple + consistent across browsers.
    return `₹${amount.toLocaleString(undefined, { maximumFractionDigits: 0 })}`
  }

  useEffect(() => {
    const r = computeRange(rangePreset)
    setFromDate(r.from)
    setToDate(r.to)
  }, [rangePreset])

  useEffect(() => {
    setError('')
    setLoading(true)
    let cancelled = false
    const safetyTimer = setTimeout(() => {
      if (cancelled) return
      setLoading(false)
      setError('Request timed out. Is the backend running at http://localhost:8080?')
      setStats(null)
    }, 5000)
    dashboardApi
      .getStats({ fromDate, toDate })
      .then((data) => {
        if (!cancelled) {
          setStats(data)
          setError('')
        }
      })
      .catch((err) => {
        if (cancelled) return
        const msg = err.response?.data?.message || err.code === 'ECONNABORTED'
          ? 'Request timed out. Is the backend running at http://localhost:8080?'
          : 'Failed to load stats. Is the backend running?'
        setError(msg)
        setStats(null)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
        clearTimeout(safetyTimer)
      })
    return () => {
      cancelled = true
      clearTimeout(safetyTimer)
    }
  }, [fromDate, toDate])

  useEffect(() => {
    setAppointmentsError('')
    setAppointmentsLoading(true)
    appointmentApi
      .getDashboard(today)
      .then((d) => setAppointmentDash(d))
      .catch((err) => {
        setAppointmentsError(err.response?.data?.message || 'Failed to load schedule.')
        setAppointmentDash(null)
      })
      .finally(() => setAppointmentsLoading(false))
  }, [])

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

  const handlePrintPage = () => {
    const prevTitle = document.title
    document.title = 'Reception - Hospital Management System'
    window.print()
    document.title = prevTitle
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
      <div className={styles.pageHeader}>
        <div className="d-flex align-items-start justify-content-between gap-2 flex-wrap">
          <div>
            <h1>Good morning, {user?.username ?? 'Admin'}</h1>
            <p>
              Here's what's happening with HMS today —{' '}
              {new Date().toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
            </p>
          </div>
          <div className="d-flex gap-2 align-items-center">
            <button type="button" className="btn btn-outline-secondary btn-sm" onClick={handlePrintPage} aria-label="Print full reception page">
              Print page
            </button>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0" role="alert">{error}</div>}
      {loading && !stats && <p className="text-muted mb-0 small">Loading stats…</p>}

      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={`${styles.statIcon} ${styles.statIconPurple}`}>
              <UsersIcon />
            </div>
            <span className={`${styles.statChange} ${styles.statChangeUp}`}>↑ 12%</span>
          </div>
          <div className={styles.statValue}>{stats ? stats.totalPatientsRegistered : '—'}</div>
          <div className={styles.statLabel}>Total Patients</div>
          <div className={styles.progress}>
            <div
              className={styles.progressFill}
              style={{ width: stats ? `${pct(stats.totalOPDVisits, stats.totalPatientsRegistered)}%` : '0%' }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={`${styles.statIcon} ${styles.statIconGreen}`}>
              <ClipboardIcon />
            </div>
            <span className={`${styles.statChange} ${styles.statChangeUp}`}>↑ 8%</span>
          </div>
          <div className={styles.statValue}>{stats ? stats.totalOPDVisits : '—'}</div>
          <div className={styles.statLabel}>Today&apos;s Appointments</div>
          <div className={styles.progress}>
            <div
              className={`${styles.progressFill} ${styles.progressFillGreen}`}
              style={{ width: stats ? `${pct(stats.totalOPDVisits, stats.totalPatientsRegistered)}%` : '0%' }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={`${styles.statIcon} ${styles.statIconAmber}`}>
              <UsersIcon />
            </div>
            <span className={`${styles.statChange} ${styles.statChangeUp}`}>↓ 3%</span>
          </div>
          <div className={styles.statValue}>{stats ? stats.totalCurrentlyAdmitted : '—'}</div>
          <div className={styles.statLabel}>Active Beds / IPD</div>
          <div className={styles.progress}>
            <div
              className={`${styles.progressFill} ${styles.progressFillAmber}`}
              style={{ width: stats ? `${pct(stats.totalCurrentlyAdmitted, stats.totalAdmitted || 1)}%` : '0%' }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <div className={`${styles.statIcon} ${styles.statIconInfo}`}>
              <ClipboardIcon />
            </div>
            <span className={`${styles.statChange} ${styles.statChangeUp}`}>↑ 21%</span>
          </div>
          <div className={styles.statValue}>{stats ? formatMoney(stats.totalCollection) : '—'}</div>
          <div className={styles.statLabel}>Total Collection</div>
          <div className={styles.progress}>
            <div
              className={`${styles.progressFill} ${styles.progressFillGreen}`}
              style={{ width: stats ? `${pct(stats.totalCollection, stats.totalCollection || 1)}%` : '0%' }}
            />
          </div>
        </div>
      </div>

      <div className={styles.grid2}>
        <div className={styles.cardShell}>
          <div className={styles.cardHeader}>
            <div className={styles.cardTitle}>Revenue Overview</div>
            <select
              className={styles.selectSmall}
              value={rangePreset}
              onChange={(e) => setRangePreset(e.target.value as typeof rangePreset)}
              aria-label="Revenue range"
            >
              <option value="LAST_7_DAYS">Last 7 days</option>
              <option value="LAST_30_DAYS">Last 30 days</option>
              <option value="THIS_YEAR">This year</option>
              <option value="PREVIOUS_YEAR">Previous year</option>
            </select>
          </div>

          <div className={styles.flexRow}>
            <div>
              <div className={styles.statValue} style={{ fontSize: 26 }}>
                {stats ? formatMoney(stats.totalCollection) : '—'}
              </div>
              <div className={styles.textMutedSmall}>Total collection in selected range</div>
            </div>
            <span className={`${styles.statChange} ${styles.statChangeUp}`} style={{ alignSelf: 'flex-start', marginTop: 6 }}>
              ↑ 21%
            </span>
          </div>

          <div className={styles.miniChart}>
            {[45, 60, 40, 75, 55, 90, 70, 80, 65, 85, 78, 95].map((v, i) => (
              <div
                key={i}
                className={`${styles.bar} ${i === 6 ? styles.barActive : ''}`}
                style={{ height: `${(v / 100) * 100}%` }}
              />
            ))}
          </div>

          <div className={styles.daysRow} aria-hidden>
            <span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span><span style={{ color: 'var(--hms-primary)', fontWeight: 800 }}>Today</span>
          </div>

          <div className={styles.bottomActions}>
            <div className="d-flex gap-2 flex-wrap">
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={handlePrint} disabled={!stats}>
                Print
              </button>
              <button type="button" className="btn btn-outline-primary btn-sm" onClick={handleDownload} disabled={!stats}>
                Download CSV
              </button>
            </div>
          </div>
        </div>

        <div className={styles.cardShell}>
          <div className={styles.cardHeader}>
            <div className={styles.cardTitle}>Patient Distribution</div>
            <div className={styles.cardAction}>Details →</div>
          </div>

          {stats ? (
            (() => {
              const inpatient = stats.totalCurrentlyAdmitted
              const outpatient = stats.totalOPDVisits
              const transport = Math.max(0, stats.totalAdmitted - stats.totalCurrentlyAdmitted)
              const total = inpatient + outpatient + transport || 1
              const r = 48
              const c = 2 * Math.PI * r
              const lenIn = (inpatient / total) * c
              const lenOut = (outpatient / total) * c
              const lenTr = (transport / total) * c
              const fmt = new Intl.NumberFormat(undefined, { notation: 'compact', maximumFractionDigits: 1 })
              return (
                <div className="d-flex align-items-center gap-16" style={{ gap: 18 }}>
                  <div className={styles.donutWrap}>
                    <svg width="120" height="120" viewBox="0 0 120 120">
                      <circle cx="60" cy="60" r="48" fill="none" stroke="var(--hms-bg-elevated)" strokeWidth="14" />
                      <circle
                        cx="60"
                        cy="60"
                        r="48"
                        fill="none"
                        stroke="var(--hms-primary)"
                        strokeWidth="14"
                        strokeDasharray={`${lenIn} ${c - lenIn}`}
                        strokeDashoffset="0"
                        strokeLinecap="round"
                      />
                      <circle
                        cx="60"
                        cy="60"
                        r="48"
                        fill="none"
                        stroke="var(--hms-accent)"
                        strokeWidth="14"
                        strokeDasharray={`${lenOut} ${c - lenOut}`}
                        strokeDashoffset={-lenIn}
                        strokeLinecap="round"
                      />
                      <circle
                        cx="60"
                        cy="60"
                        r="48"
                        fill="none"
                        stroke="var(--hms-success)"
                        strokeWidth="14"
                        strokeDasharray={`${lenTr} ${c - lenTr}`}
                        strokeDashoffset={-(lenIn + lenOut)}
                        strokeLinecap="round"
                      />
                    </svg>
                    <div className={styles.donutCenter}>
                      <div className={styles.donutVal}>{fmt.format(total)}</div>
                      <div className={styles.donutSub}>Total</div>
                    </div>
                  </div>

                  <div className={styles.patientsLegend}>
                    <div className={styles.legendItem}>
                      <div className={styles.legendDot} style={{ background: 'var(--hms-primary)' }} />
                      <div style={{ flex: 1 }}>
                        Inpatient
                        <div className="small" style={{ color: 'var(--hms-text-muted)' }}>
                          {inpatient.toLocaleString()} · {Math.round((inpatient / total) * 100)}%
                        </div>
                      </div>
                      <div style={{ fontWeight: 800 }}>{fmt.format(inpatient)}</div>
                    </div>
                    <div className={styles.legendItem}>
                      <div className={styles.legendDot} style={{ background: 'var(--hms-accent)' }} />
                      <div style={{ flex: 1 }}>
                        Outpatient
                        <div className="small" style={{ color: 'var(--hms-text-muted)' }}>
                          {outpatient.toLocaleString()} · {Math.round((outpatient / total) * 100)}%
                        </div>
                      </div>
                      <div style={{ fontWeight: 800 }}>{fmt.format(outpatient)}</div>
                    </div>
                    <div className={styles.legendItem}>
                      <div className={styles.legendDot} style={{ background: 'var(--hms-success)' }} />
                      <div style={{ flex: 1 }}>
                        Transport
                        <div className="small" style={{ color: 'var(--hms-text-muted)' }}>
                          {transport.toLocaleString()} · {Math.round((transport / total) * 100)}%
                        </div>
                      </div>
                      <div style={{ fontWeight: 800 }}>{fmt.format(transport)}</div>
                    </div>
                  </div>
                </div>
              )
            })()
          ) : (
            <div className="text-muted small">—</div>
          )}
        </div>
      </div>

      <div className={styles.grid2}>
        <div className={styles.cardShell}>
          <div className={styles.cardHeader}>
            <div className={styles.cardTitle}>Today&apos;s Schedule</div>
            <div className={styles.cardAction}>View all →</div>
          </div>

          {appointmentsLoading && (
            <div className="text-muted small">Loading schedule…</div>
          )}
          {!appointmentsLoading && appointmentsError && (
            <div className="alert alert-danger py-2 mb-0" role="alert">{appointmentsError}</div>
          )}

          {!appointmentsLoading && !appointmentsError && (
            <>
              {(!appointmentDash || appointmentDash.todaysAppointments.length === 0) ? (
                <div className="text-muted small">No appointments today.</div>
              ) : (
                appointmentDash.todaysAppointments
                  .slice()
                  .sort((a, b) => a.slotTime.localeCompare(b.slotTime))
                  .slice(0, 6)
                  .map((a) => (
                    <div key={a.id} className={styles.scheduleSlot}>
                      <div className={styles.scheduleTime}>{formatTime(a.slotTime)}</div>
                      <div className={`${styles.scheduleEvent} ${scheduleTheme(a.status)}`}>
                        <div className={styles.scheduleName}>{a.patientName}</div>
                        <div className={styles.scheduleSub}>{a.departmentName} · {a.doctorName}</div>
                      </div>
                    </div>
                  ))
              )}
            </>
          )}
        </div>

        <div className={styles.cardShell}>
          <div className={styles.cardHeader}>
            <div className={styles.cardTitle}>Recent Activity</div>
            <div className={styles.cardAction}>View all →</div>
          </div>

          {(() => {
            const appts = appointmentDash?.todaysAppointments ?? []
            const first = appts[0]
            const items = [
              first
                ? {
                    dot: 'var(--hms-success)',
                    title: `New appointment scheduled — ${first.patientName}`,
                    time: '2 minutes ago',
                  }
                : {
                    dot: 'var(--hms-success)',
                    title: 'New appointment scheduled',
                    time: '2 minutes ago',
                  },
              {
                dot: 'var(--hms-primary)',
                title: `Invoice paid — ${stats ? formatMoney(stats.totalCollection) : '₹0'}`,
                time: '18 minutes ago · Auto-billing',
              },
              {
                dot: 'var(--hms-accent-orange)',
                title: 'Transport delay — Vehicle V-07',
                time: '34 minutes ago · Route 8-South',
              },
              {
                dot: '#3B82F6',
                title: 'Staff roster updated',
                time: '1 hour ago · Next week schedule',
              },
              {
                dot: 'var(--hms-error)',
                title: 'Blood bank alert — O- inventory low',
                time: '2 hours ago · Requires restock',
              },
            ]
            return (
              <div>
                {items.map((it, idx) => (
                  <div className={styles.activityItem} key={idx}>
                    <div className={styles.activityDot} style={{ background: it.dot }} />
                    <div className={styles.activityContent}>
                      <div className={styles.activityTitle}>{it.title}</div>
                      <div className={styles.activityTime}>{it.time}</div>
                    </div>
                  </div>
                ))}
              </div>
            )
          })()}
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
