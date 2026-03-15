/**
 * Appointment Dashboard – Today's appointments, upcoming, cancelled, no-show, summary cards.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import type { AppointmentDashboard as DashboardType, AppointmentResponse } from '../../types/appointment.types'

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

function AppointmentRow({ a, onConvert, onNoShow, onCancel }: {
  a: AppointmentResponse
  onConvert?: (id: number) => void
  onNoShow?: (id: number) => void
  onCancel?: (id: number) => void
}) {
  const canConvert = (a.status === 'BOOKED' || a.status === 'CONFIRMED') && !a.opdVisitId
  const canNoShow = a.status === 'BOOKED' || a.status === 'CONFIRMED'
  const canCancel = a.status === 'BOOKED' || a.status === 'CONFIRMED' || a.status === 'PENDING_CONFIRMATION'

  return (
    <tr>
      <td>{a.tokenNo ?? '—'}</td>
      <td>{a.patientName}</td>
      <td>{a.patientUhid}</td>
      <td>{a.doctorName}</td>
      <td>{formatTime(a.slotTime)}</td>
      <td><span className="badge bg-secondary">{STATUS_LABELS[a.status] ?? a.status}</span></td>
      <td><span className="badge bg-light text-dark">{SOURCE_LABELS[a.source] ?? a.source}</span></td>
      <td>
        {canConvert && onConvert && (
          <button type="button" className="btn btn-sm btn-success me-1" onClick={() => onConvert(a.id)}>
            Convert to OPD
          </button>
        )}
        {canNoShow && onNoShow && (
          <button type="button" className="btn btn-sm btn-warning me-1" onClick={() => onNoShow(a.id)}>
            No Show
          </button>
        )}
        {canCancel && onCancel && (
          <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => onCancel(a.id)}>
            Cancel
          </button>
        )}
      </td>
    </tr>
  )
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
        <h1 className="h4 mb-0">Appointment Dashboard</h1>
        <div className="d-flex gap-2 align-items-center">
          <input
            type="date"
            className="form-control form-control-sm"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            style={{ width: 'auto' }}
          />
          <Link to="/front-office/appointments/book" className="btn btn-primary btn-sm">Book Appointment</Link>
          <Link to="/front-office/appointments/queue" className="btn btn-outline-primary btn-sm">Queue</Link>
          <Link to="/front-office/appointments/search" className="btn btn-outline-secondary btn-sm">Search</Link>
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

          <div className="card border shadow-sm">
            <div className="card-header bg-light py-2">
              <h6 className="mb-0">Today&apos;s Appointments</h6>
            </div>
            <div className="table-responsive">
              <table className="table table-striped mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Token</th>
                    <th>Patient</th>
                    <th>UHID</th>
                    <th>Doctor</th>
                    <th>Time</th>
                    <th>Status</th>
                    <th>Source</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {data.todaysAppointments.length === 0 ? (
                    <tr><td colSpan={8} className="text-center text-muted py-4">No appointments for this date.</td></tr>
                  ) : (
                    data.todaysAppointments.map((a) => (
                      <AppointmentRow
                        key={a.id}
                        a={a}
                        onConvert={handleConvert}
                        onNoShow={handleNoShow}
                        onCancel={handleCancel}
                      />
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {data.upcomingAppointments.length > 0 && (
            <div className="card border shadow-sm">
              <div className="card-header bg-light py-2">
                <h6 className="mb-0">Upcoming (Next Day)</h6>
              </div>
              <div className="table-responsive">
                <table className="table table-striped mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>Token</th>
                      <th>Patient</th>
                      <th>UHID</th>
                      <th>Doctor</th>
                      <th>Time</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.upcomingAppointments.map((a) => (
                      <tr key={a.id}>
                        <td>{a.tokenNo ?? '—'}</td>
                        <td>{a.patientName}</td>
                        <td>{a.patientUhid}</td>
                        <td>{a.doctorName}</td>
                        <td>{formatTime(a.slotTime)}</td>
                        <td><span className="badge bg-secondary">{STATUS_LABELS[a.status] ?? a.status}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
