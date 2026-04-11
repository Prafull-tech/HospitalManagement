/**
 * Appointment Dashboard – Weekly calendar view with doctor filter.
 */

import { useState, useEffect, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import type { AppointmentDashboard as DashboardType, AppointmentResponse } from '../../types/appointment.types'
import styles from './AppointmentDashboard.module.css'

// ── Constants ────────────────────────────────────────────────────────
const HOURS = Array.from({ length: 13 }, (_, i) => i + 7) // 07:00 – 19:00

const STATUS_LABELS: Record<string, string> = {
  BOOKED: 'Booked',
  CONFIRMED: 'Confirmed',
  PENDING_CONFIRMATION: 'Pending',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  NO_SHOW: 'No Show',
}

// ── Helpers ──────────────────────────────────────────────────────────
function getMonday(d: Date): string {
  const dt = new Date(d)
  const day = dt.getDay()
  dt.setDate(dt.getDate() + (day === 0 ? -6 : 1 - day))
  return dt.toISOString().slice(0, 10)
}

function addDays(iso: string, n: number): string {
  const d = new Date(iso)
  d.setDate(d.getDate() + n)
  return d.toISOString().slice(0, 10)
}

function formatShortDate(iso: string) {
  return new Date(iso).toLocaleDateString(undefined, { weekday: 'short', day: 'numeric' })
}

function formatMonthYear(iso: string) {
  const start = new Date(iso)
  const end = new Date(iso)
  end.setDate(end.getDate() + 6)
  if (start.getMonth() === end.getMonth()) {
    return start.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })
  }
  return `${start.toLocaleDateString(undefined, { month: 'short' })} – ${end.toLocaleDateString(undefined, { month: 'short', year: 'numeric' })}`
}

function formatTime(s: string) {
  if (!s) return '—'
  const m = s.match(/^(\d{1,2}):(\d{2})/)
  return m ? `${m[1].padStart(2, '0')}:${m[2]}` : s
}

function formatDateLabel(iso?: string) {
  if (!iso) return ''
  try {
    return new Date(iso).toLocaleDateString(undefined, {
      weekday: 'long', month: 'short', day: 'numeric', year: 'numeric',
    })
  } catch { return iso }
}

function slotHour(t: string) {
  return parseInt(t?.split(':')[0] ?? '0', 10)
}

function diffDays(fromIso: string, toIso: string) {
  return Math.round((new Date(toIso).getTime() - new Date(fromIso).getTime()) / 86_400_000)
}

function statusThemeClass(status: AppointmentResponse['status']) {
  if (status === 'CONFIRMED' || status === 'COMPLETED') return styles.green
  if (status === 'PENDING_CONFIRMATION' || status === 'BOOKED') return styles.amber
  return styles.red
}

function statusToBadge(status: AppointmentResponse['status']) {
  if (status === 'CONFIRMED' || status === 'COMPLETED') return { cls: 'bg-success', label: 'Confirmed' }
  if (status === 'PENDING_CONFIRMATION') return { cls: 'bg-warning text-dark', label: 'Pending' }
  if (status === 'BOOKED') return { cls: 'bg-secondary', label: 'Booked' }
  if (status === 'NO_SHOW') return { cls: 'bg-danger', label: 'No Show' }
  if (status === 'CANCELLED') return { cls: 'bg-danger', label: 'Cancelled' }
  return { cls: 'bg-secondary', label: STATUS_LABELS[status] ?? status }
}

function calApptClass(status: AppointmentResponse['status']) {
  if (status === 'CONFIRMED' || status === 'COMPLETED') return styles.calApptGreen
  if (status === 'PENDING_CONFIRMATION' || status === 'BOOKED') return styles.calApptAmber
  return styles.calApptRed
}

function priorityFromStatus(s: AppointmentResponse['status']) {
  if (s === 'PENDING_CONFIRMATION') return 'High'
  if (s === 'BOOKED') return 'Medium'
  return 'Low'
}

function priorityBadgeClass(p: string) {
  if (p === 'High') return 'bg-danger'
  if (p === 'Medium') return 'bg-warning text-dark'
  return 'bg-success'
}

// ── Main Component ────────────────────────────────────────────────────
export function AppointmentDashboard() {
  const today = new Date().toISOString().slice(0, 10)

  const [weekStart, setWeekStart] = useState(() => getMonday(new Date()))
  const [selectedDate, setSelectedDate] = useState(today)
  const [doctorFilter, setDoctorFilter] = useState('ALL')
  const [weekData, setWeekData] = useState<Record<string, DashboardType>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const weekDays = useMemo(
    () => Array.from({ length: 7 }, (_, i) => addDays(weekStart, i)),
    [weekStart],
  )

  const fetchWeek = () => {
    setLoading(true)
    setError('')
    const days = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i))
    Promise.allSettled(
      days.map((d) =>
        appointmentApi.getDashboard(d).then((r) => [d, r] as [string, DashboardType]),
      ),
    )
      .then((results) => {
        const map: Record<string, DashboardType> = {}
        let failCount = 0
        results.forEach((r) => {
          if (r.status === 'fulfilled') {
            const [d, data] = r.value
            map[d] = data
          } else {
            failCount++
          }
        })
        setWeekData(map)
        if (failCount === days.length) {
          setError('Failed to load appointments. Please check if you are logged in.')
        } else if (failCount > 0) {
          setError(`Some days failed to load (${failCount}/7).`)
        }
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchWeek() }, [weekStart]) // eslint-disable-line react-hooks/exhaustive-deps

  const dayDash = weekData[selectedDate]

  const allWeekAppts = useMemo(() => {
    const all: (AppointmentResponse & { date: string })[] = []
    weekDays.forEach((d) => {
      weekData[d]?.todaysAppointments.forEach((a) => all.push({ ...a, date: d }))
    })
    return all
  }, [weekData, weekDays])

  const doctors = useMemo(() => {
    const map = new Map<number, string>()
    allWeekAppts.forEach((a) => map.set(a.doctorId, a.doctorName))
    return Array.from(map.entries()).sort((a, b) => a[1].localeCompare(b[1]))
  }, [allWeekAppts])

  const filteredAppts = useMemo(
    () =>
      doctorFilter === 'ALL'
        ? allWeekAppts
        : allWeekAppts.filter((a) => a.doctorId === Number(doctorFilter)),
    [allWeekAppts, doctorFilter],
  )

  // date → hour → appointments[]
  const cellMap = useMemo(() => {
    const map: Record<string, Record<number, typeof filteredAppts>> = {}
    filteredAppts.forEach((a) => {
      const h = slotHour(a.slotTime)
      if (!map[a.date]) map[a.date] = {}
      if (!map[a.date][h]) map[a.date][h] = []
      map[a.date][h].push(a)
    })
    return map
  }, [filteredAppts])

  const handleConvert = (id: number) =>
    appointmentApi.convertToOpd(id).then(() => fetchWeek()).catch(() => {})
  const handleNoShow = (id: number) =>
    appointmentApi.markNoShow(id).then(() => fetchWeek()).catch(() => {})
  const handleCancel = (id: number) => {
    if (window.confirm('Cancel this appointment?')) {
      appointmentApi.cancel(id).then(() => fetchWeek()).catch(() => {})
    }
  }

  const daySchedule = useMemo(() => {
    const appts = dayDash?.todaysAppointments ?? []
    const filtered =
      doctorFilter === 'ALL' ? appts : appts.filter((a) => a.doctorId === Number(doctorFilter))
    return filtered.slice().sort((a, b) => a.slotTime.localeCompare(b.slotTime))
  }, [dayDash, doctorFilter])

  return (
    <div className="hms-page-shell">
      {/* Breadcrumb */}
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/front-office/appointments">Appointments</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">Dashboard</li>
        </ol>
      </nav>

      {/* Header */}
      <div className="hms-page-hero">
        <div>
          <div className="hms-page-kicker">Scheduling</div>
          <h1 className="hms-page-title">Appointments</h1>
          <div className="hms-page-subtitle">Manage all bookings, referrals, and wait lists with the same Reception-style layout and visual rhythm.</div>
        </div>
        <div className="hms-page-actions">
          <Link to="/front-office/appointments/book" className="btn btn-primary btn-sm">
            + Book Appointment
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {/* Summary cards – reflect selected day */}
      {dayDash && (
        <div className="hms-metric-grid">
          {[
            { label: 'Total Today', value: dayDash.totalAppointmentsToday, color: '' },
            { label: 'Walk-ins', value: dayDash.walkIns, color: 'success' },
            { label: 'Online', value: dayDash.onlineBookings, color: 'info' },
            { label: 'Completed', value: dayDash.completedConsultations, color: 'primary' },
            { label: 'Cancelled', value: dayDash.cancelled, color: 'danger' },
            { label: 'No Show', value: dayDash.noShow, color: 'warning' },
          ].map(({ label, value, color }) => (
            <div key={label} className="hms-metric-card">
              <span className={`hms-metric-label ${color ? `text-${color}` : ''}`}>{label}</span>
              <strong className={`hms-metric-value ${color ? `text-${color}` : ''}`}>{value}</strong>
            </div>
          ))}
        </div>
      )}

      {/* Calendar toolbar */}
      <div className="d-flex flex-wrap align-items-center gap-2">
        <button
          type="button"
          className="btn btn-outline-secondary btn-sm"
          onClick={() => setWeekStart((w) => addDays(w, -7))}
        >
          ‹ Prev Week
        </button>
        <span className="fw-semibold">{formatMonthYear(weekStart)}</span>
        <button
          type="button"
          className="btn btn-outline-secondary btn-sm"
          onClick={() => setWeekStart((w) => addDays(w, 7))}
        >
          Next Week ›
        </button>
        <button
          type="button"
          className="btn btn-outline-primary btn-sm"
          onClick={() => { setWeekStart(getMonday(new Date())); setSelectedDate(today) }}
        >
          Today
        </button>

        <div className="ms-auto d-flex align-items-center gap-2">
          <label htmlFor="doc-filter" className="small text-muted mb-0 text-nowrap">
            Doctor:
          </label>
          <select
            id="doc-filter"
            className="form-select form-select-sm"
            style={{ minWidth: 200 }}
            value={doctorFilter}
            onChange={(e) => setDoctorFilter(e.target.value)}
          >
            <option value="ALL">All Doctors</option>
            {doctors.map(([id, name]) => (
              <option key={id} value={id}>{name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* ── Weekly Calendar Grid ── */}
      <div className="card border shadow-sm overflow-hidden">
        <div className={styles.calGrid}>
          {/* Corner */}
          <div className={styles.calCorner} />

          {/* Day column headers */}
          {weekDays.map((d) => {
            const isToday = d === today
            const isSelected = d === selectedDate
            const count = cellMap[d]
              ? Object.values(cellMap[d]).reduce((s, a) => s + a.length, 0)
              : 0
            return (
              <button
                key={d}
                type="button"
                className={[
                  styles.calDayHeader,
                  isSelected ? styles.calDaySelected : '',
                  isToday ? styles.calDayToday : '',
                ].join(' ')}
                onClick={() => setSelectedDate(d)}
              >
                <span className={styles.calDayLabel}>{formatShortDate(d)}</span>
                {count > 0 && <span className={styles.calDayBadge}>{count}</span>}
              </button>
            )
          })}

          {/* Hour rows */}
          {HOURS.map((h) => (
            <div key={h} className={styles.calRow}>
              <div className={styles.calTimeLabel}>
                {h.toString().padStart(2, '0')}:00
              </div>
              {weekDays.map((d) => {
                const appts = cellMap[d]?.[h] ?? []
                const isSelected = d === selectedDate
                return (
                  <div
                    key={`${d}-${h}`}
                    className={`${styles.calCell} ${isSelected ? styles.calCellSelected : ''}`}
                    role="button"
                    tabIndex={-1}
                    onClick={() => setSelectedDate(d)}
                    onKeyDown={(e) => e.key === 'Enter' && setSelectedDate(d)}
                  >
                    {appts.map((a) => (
                      <div
                        key={a.id}
                        className={`${styles.calAppt} ${calApptClass(a.status)}`}
                        title={`${formatTime(a.slotTime)} · ${a.patientName} · ${a.doctorName}`}
                      >
                        <span className={styles.calApptTime}>{formatTime(a.slotTime)}</span>
                        <span className={styles.calApptName}>{a.patientName}</span>
                        <span className={styles.calApptDoc}>{a.doctorName}</span>
                      </div>
                    ))}
                  </div>
                )
              })}
            </div>
          ))}
        </div>

        {loading && (
          <div className="text-center py-2 text-muted small border-top">
            <span className="spinner-border spinner-border-sm me-1" role="status" />
            Loading week…
          </div>
        )}
      </div>

      {/* ── Selected day detail + Wait List ── */}
      <div className="row g-3">
        <div className="col-12 col-lg-7">
          <div className="card border shadow-sm h-100">
            <div className="card-header bg-light py-2 d-flex align-items-center justify-content-between">
              <h6 className="mb-0">{formatDateLabel(selectedDate)}</h6>
              <span className="badge bg-secondary">
                {daySchedule.length} appt{daySchedule.length !== 1 ? 's' : ''}
              </span>
            </div>
            <div className="card-body py-2">
              {daySchedule.length === 0 ? (
                <div className="text-center text-muted py-4">No appointments for this date.</div>
              ) : (
                daySchedule.map((a) => {
                  const canConvert = (a.status === 'BOOKED' || a.status === 'CONFIRMED') && !a.opdVisitId
                  const canNoShow = a.status === 'BOOKED' || a.status === 'CONFIRMED'
                  const canCancel =
                    a.status === 'BOOKED' ||
                    a.status === 'CONFIRMED' ||
                    a.status === 'PENDING_CONFIRMATION'
                  const badge = statusToBadge(a.status)
                  return (
                    <div key={a.id} className={styles.scheduleSlot}>
                      <div className={styles.scheduleTime}>{formatTime(a.slotTime)}</div>
                      <div className={`${styles.scheduleEvent} ${statusThemeClass(a.status)}`}>
                        <div className={styles.scheduleMain}>
                          <div className={styles.scheduleName}>{a.patientName}</div>
                          <div className={styles.scheduleSub}>
                            {a.departmentName} · {a.doctorName}
                          </div>
                          <div className="mt-2">
                            <span className={`badge ${badge.cls}`}>{badge.label}</span>
                          </div>
                        </div>
                        <div className={styles.scheduleRight}>
                          <div className={styles.scheduleActions}>
                            {canConvert && (
                              <button
                                type="button"
                                className="btn btn-sm btn-success"
                                onClick={() => handleConvert(a.id)}
                              >
                                Convert to OPD
                              </button>
                            )}
                            {canNoShow && (
                              <button
                                type="button"
                                className="btn btn-sm btn-warning"
                                onClick={() => handleNoShow(a.id)}
                              >
                                No Show
                              </button>
                            )}
                            {canCancel && (
                              <button
                                type="button"
                                className="btn btn-sm btn-outline-danger"
                                onClick={() => handleCancel(a.id)}
                              >
                                Cancel
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  )
                })
              )}
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-5">
          <div className="card border shadow-sm h-100">
            <div className="card-header bg-light py-2 d-flex align-items-center justify-content-between">
              <h6 className="mb-0">Wait List</h6>
              <span className="badge bg-light text-dark">
                {dayDash?.upcomingAppointments?.length ?? 0} patients
              </span>
            </div>
            <div className="table-responsive">
              <table className="table table-striped mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Patient</th>
                    <th>Department</th>
                    <th>Wait</th>
                    <th>Priority</th>
                  </tr>
                </thead>
                <tbody>
                  {!dayDash || dayDash.upcomingAppointments.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="text-center text-muted py-4">No wait list.</td>
                    </tr>
                  ) : (
                    dayDash.upcomingAppointments.map((a) => {
                      const waitDays = diffDays(selectedDate, a.appointmentDate)
                      const waitLabel =
                        Number.isFinite(waitDays) && waitDays >= 0 ? `${waitDays}d` : '—'
                      const priority = priorityFromStatus(a.status)
                      return (
                        <tr key={a.id}>
                          <td>
                            <div className="fw-semibold">{a.patientName}</div>
                            <div className={styles.waitPatientSub}>{a.patientUhid}</div>
                          </td>
                          <td>{a.departmentName}</td>
                          <td>{waitLabel}</td>
                          <td>
                            <span className={`badge ${priorityBadgeClass(priority)}`}>{priority}</span>
                          </td>
                        </tr>
                      )
                    })
                  )}
                </tbody>
              </table>
            </div>
            <div className="card-body py-2 text-center">
              <Link to="/front-office/appointments/queue" className="btn btn-outline-primary btn-sm">
                Manage Wait List →
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
