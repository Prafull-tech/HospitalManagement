/**
 * Appointment Dashboard – Today's appointments, upcoming, cancelled, no-show, summary cards.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import type { AppointmentDashboard as DashboardType, AppointmentResponse } from '../../types/appointment.types'
import styles from './AppointmentDashboard.module.css'

const STATUS_LABELS: Record<string, string> = {
  BOOKED: 'Booked',
  CONFIRMED: 'Confirmed',
  PENDING_CONFIRMATION: 'Pending',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  NO_SHOW: 'No Show',
}

const SOURCE_LABELS: Record<string, string> = {
  FRONT_DESK: 'Front Desk',
  WALK_IN: 'Walk-in',
  ONLINE: 'Online',
}

function formatTime(s: string) {
  if (!s) return '—'
  const m = s.match(/^(\d{1,2}):(\d{2})/)
  return m ? `${m[1].padStart(2, '0')}:${m[2]}` : s
}

function formatDateLabel(isoDate?: string) {
  if (!isoDate) return ''
  try {
    const d = new Date(isoDate)
    return d.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })
  } catch {
    return isoDate
  }
}

function diffDays(fromIso: string, toIso: string) {
  const from = new Date(fromIso)
  const to = new Date(toIso)
  const ms = to.getTime() - from.getTime()
  return Math.round(ms / (1000 * 60 * 60 * 24))
}

function statusThemeClass(status: AppointmentResponse['status']) {
  if (status === 'CONFIRMED' || status === 'COMPLETED') return styles.green
  if (status === 'PENDING_CONFIRMATION' || status === 'BOOKED') return styles.amber
  return styles.red
}

function statusToBadge(status: AppointmentResponse['status']) {
  if (status === 'CONFIRMED' || status === 'COMPLETED') return { className: 'bg-success', label: 'Confirmed' }
  if (status === 'PENDING_CONFIRMATION') return { className: 'bg-warning', label: 'Pending' }
  if (status === 'BOOKED') return { className: 'bg-secondary', label: 'Booked' }
  if (status === 'NO_SHOW') return { className: 'bg-danger', label: 'No Show' }
  if (status === 'CANCELLED') return { className: 'bg-danger', label: 'Cancelled' }
  return { className: 'bg-secondary', label: STATUS_LABELS[status] ?? status }
}

function priorityFromStatus(status: AppointmentResponse['status']) {
  if (status === 'PENDING_CONFIRMATION') return 'High'
  if (status === 'BOOKED') return 'Medium'
  return 'Low'
}

function priorityBadgeClass(priority: string) {
  if (priority === 'High') return 'bg-danger'
  if (priority === 'Medium') return 'bg-warning text-dark'
  return 'bg-success'
}

export function AppointmentDashboard() {
  const [data, setData] = useState<DashboardType | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))

  const fetchDashboard = () => {
    setLoading(true)
    setError('')
    appointmentApi
      .getDashboard(date)
      .then(setData)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load dashboard.')
        setData(null)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    fetchDashboard()
  }, [date])

  const handleConvert = (id: number) => {
    appointmentApi.convertToOpd(id).then(() => fetchDashboard()).catch(() => {})
  }

  const handleNoShow = (id: number) => {
    appointmentApi.markNoShow(id).then(() => fetchDashboard()).catch(() => {})
  }

  const handleCancel = (id: number) => {
    if (window.confirm('Cancel this appointment?')) {
      appointmentApi.cancel(id).then(() => fetchDashboard()).catch(() => {})
    }
  }

  if (loading && !data) {
    return (
      <div className="d-flex justify-content-center py-5">
        <div className="spinner-border text-primary" role="status"><span className="visually-hidden">Loading…</span></div>
      </div>
    )
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/appointments">Appointments</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Dashboard</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div>
          <h1 className="h4 mb-0">Appointments</h1>
          <div className="text-muted small">Manage all bookings, referrals, and wait lists</div>
        </div>
        <div className="d-flex gap-2 align-items-center">
          <input
            type="date"
            className="form-control form-control-sm"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            style={{ width: 'auto' }}
          />
          <Link to="/front-office/appointments/book" className="btn btn-primary btn-sm">+ Book Appointment</Link>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {data && (
        <>
          <div className="row g-2 g-md-3">
            <div className="col-6 col-md">
              <div className="card border shadow-sm h-100">
                <div className="card-body py-2">
                  <p className="text-muted small mb-0">Total Today</p>
                  <p className="fw-bold mb-0">{data.totalAppointmentsToday}</p>
                </div>
              </div>
            </div>
            <div className="col-6 col-md">
              <div className="card border border-success shadow-sm h-100">
                <div className="card-body py-2">
                  <p className="text-success small mb-0">Walk-ins</p>
                  <p className="fw-bold text-success mb-0">{data.walkIns}</p>
                </div>
              </div>
            </div>
            <div className="col-6 col-md">
              <div className="card border border-info shadow-sm h-100">
                <div className="card-body py-2">
                  <p className="text-info small mb-0">Online Bookings</p>
                  <p className="fw-bold text-info mb-0">{data.onlineBookings}</p>
                </div>
              </div>
            </div>
            <div className="col-6 col-md">
              <div className="card border border-primary shadow-sm h-100">
                <div className="card-body py-2">
                  <p className="text-primary small mb-0">Completed</p>
                  <p className="fw-bold text-primary mb-0">{data.completedConsultations}</p>
                </div>
              </div>
            </div>
            <div className="col-6 col-md">
              <div className="card border border-danger shadow-sm h-100">
                <div className="card-body py-2">
                  <p className="text-danger small mb-0">Cancelled</p>
                  <p className="fw-bold text-danger mb-0">{data.cancelled}</p>
                </div>
              </div>
            </div>
            <div className="col-6 col-md">
              <div className="card border border-warning shadow-sm h-100">
                <div className="card-body py-2">
                  <p className="text-warning small mb-0">No Show</p>
                  <p className="fw-bold text-warning mb-0">{data.noShow}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-3">
            <div className="col-12 col-lg-7">
              <div className="card border shadow-sm h-100">
                <div className="card-header bg-light py-2">
                  <h6 className="mb-0">Today&apos;s Schedule</h6>
                  <div className="text-muted small">
                    {formatDateLabel(date)}
                  </div>
                </div>
                <div className="card-body py-2">
                  {data.todaysAppointments.length === 0 ? (
                    <div className="text-center text-muted py-4">No appointments for this date.</div>
                  ) : (
                    <div>
                      {data.todaysAppointments
                        .slice()
                        .sort((a, b) => a.slotTime.localeCompare(b.slotTime))
                        .map((a) => {
                          const canConvert = (a.status === 'BOOKED' || a.status === 'CONFIRMED') && !a.opdVisitId
                          const canNoShow = a.status === 'BOOKED' || a.status === 'CONFIRMED'
                          const canCancel = a.status === 'BOOKED' || a.status === 'CONFIRMED' || a.status === 'PENDING_CONFIRMATION'
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
                                    <span className={`badge ${badge.className}`}>{badge.label}</span>{' '}
                                    <span className="badge bg-light text-dark">{SOURCE_LABELS[a.source] ?? a.source}</span>
                                  </div>
                                </div>

                                <div className={styles.scheduleRight}>
                                  {(canConvert || canNoShow || canCancel) && (
                                    <div className={styles.scheduleActions}>
                                      {canConvert && (
                                        <button type="button" className="btn btn-sm btn-success" onClick={() => handleConvert(a.id)}>
                                          Convert to OPD
                                        </button>
                                      )}
                                      {canNoShow && (
                                        <button type="button" className="btn btn-sm btn-warning" onClick={() => handleNoShow(a.id)}>
                                          No Show
                                        </button>
                                      )}
                                      {canCancel && (
                                        <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => handleCancel(a.id)}>
                                          Cancel
                                        </button>
                                      )}
                                    </div>
                                  )}
                                </div>
                              </div>
                            </div>
                          )
                        })}
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="col-12 col-lg-5">
              <div className="card border shadow-sm h-100">
                <div className="card-header bg-light py-2 d-flex align-items-center justify-content-between">
                  <h6 className="mb-0">Wait List</h6>
                  <span className="badge bg-light text-dark">{data.upcomingAppointments.length} patients</span>
                </div>
                <div className="table-responsive">
                  <table className="table table-striped mb-0">
                    <thead className="table-light">
                      <tr>
                        <th>Patient</th>
                        <th>For</th>
                        <th>Wait</th>
                        <th>Priority</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.upcomingAppointments.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="text-center text-muted py-4">No wait list.</td>
                        </tr>
                      ) : (
                        data.upcomingAppointments.map((a) => {
                          const waitDays = diffDays(date, a.appointmentDate)
                          const waitLabel = Number.isFinite(waitDays) && waitDays >= 0 ? `${waitDays} day${waitDays === 1 ? '' : 's'}` : '—'
                          const priority = priorityFromStatus(a.status)
                          return (
                            <tr key={a.id}>
                              <td>
                                <div className={styles.waitPatient}>
                                  <div style={{ fontWeight: 600 }}>{a.patientName}</div>
                                  <div className={styles.waitPatientSub}>{a.patientUhid}</div>
                                </div>
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
                <div className="card-body py-2">
                  <div className="d-flex justify-content-center">
                    <Link to="/front-office/appointments/queue" className="btn btn-outline-primary btn-sm">
                      Manage Wait List →
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
