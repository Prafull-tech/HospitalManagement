/**
 * Doctor queue – Token, Patient, Time, Status.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import { doctorsApi } from '../../api/doctors'
import type { AppointmentResponse } from '../../types/appointment.types'
import type { DoctorResponse } from '../../types/doctor'

const STATUS_LABELS: Record<string, string> = {
  BOOKED: 'Waiting',
  CONFIRMED: 'Waiting',
  PENDING_CONFIRMATION: 'Pending',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  NO_SHOW: 'No Show',
}

function formatTime(s: string) {
  if (!s) return '—'
  const m = s.match(/^(\d{1,2}):(\d{2})/)
  return m ? `${m[1].padStart(2, '0')}:${m[2]}` : s
}

export function AppointmentQueuePage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | ''>('')
  const [queue, setQueue] = useState<AppointmentResponse[]>([])
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((d) => setDoctors(d.content)).catch(() => [])
  }, [])

  useEffect(() => {
    if (!selectedDoctorId) {
      setQueue([])
      return
    }
    setLoading(true)
    setError('')
    appointmentApi
      .getQueue(Number(selectedDoctorId), date)
      .then(setQueue)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load queue.')
        setQueue([])
      })
      .finally(() => setLoading(false))
  }, [selectedDoctorId, date])

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/appointments">Appointments</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Queue</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Appointment Queue</h1>
        <Link to="/front-office/appointments" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
      </div>

      <div className="card shadow-sm">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-4">
              <label className="form-label">Doctor</label>
              <select
                className="form-select"
                value={selectedDoctorId}
                onChange={(e) => setSelectedDoctorId(e.target.value ? Number(e.target.value) : '')}
              >
                <option value="">Select doctor</option>
                {doctors.map((d) => (
                  <option key={d.id} value={d.id}>{d.fullName}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label">Date</label>
              <input
                type="date"
                className="form-control"
                value={date}
                onChange={(e) => setDate(e.target.value)}
              />
            </div>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card border shadow-sm">
        <div className="card-header bg-light py-2">
          <h6 className="mb-0">Queue</h6>
        </div>
        <div className="table-responsive">
          <table className="table table-striped mb-0">
            <thead className="table-light">
              <tr>
                <th>Token</th>
                <th>Patient Name</th>
                <th>UHID</th>
                <th>Appointment Time</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="text-center py-4"><div className="spinner-border spinner-border-sm" /></td></tr>
              ) : queue.length === 0 ? (
                <tr><td colSpan={5} className="text-center text-muted py-4">Select a doctor and date to view queue.</td></tr>
              ) : (
                queue.map((a) => (
                  <tr key={a.id}>
                    <td>{a.tokenNo ?? '—'}</td>
                    <td>{a.patientName}</td>
                    <td>{a.patientUhid}</td>
                    <td>{formatTime(a.slotTime)}</td>
                    <td><span className="badge bg-secondary">{STATUS_LABELS[a.status] ?? a.status}</span></td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
