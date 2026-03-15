/**
 * Search appointments by patient name, UHID, doctor, date, token.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import { doctorsApi } from '../../api/doctors'
import type { AppointmentResponse, AppointmentStatus } from '../../types/appointment.types'
import type { DoctorResponse } from '../../types/doctor'

const STATUS_OPTIONS: { value: AppointmentStatus | ''; label: string }[] = [
  { value: '', label: 'All' },
  { value: 'BOOKED', label: 'Booked' },
  { value: 'CONFIRMED', label: 'Confirmed' },
  { value: 'PENDING_CONFIRMATION', label: 'Pending' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'NO_SHOW', label: 'No Show' },
]

function formatTime(s: string) {
  if (!s) return '—'
  const m = s.match(/^(\d{1,2}):(\d{2})/)
  return m ? `${m[1].padStart(2, '0')}:${m[2]}` : s
}

export function AppointmentSearchPage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [filters, setFilters] = useState({
    date: '',
    doctorId: '' as number | '',
    status: '' as AppointmentStatus | '',
    patientUhid: '',
    patientName: '',
  })
  const [result, setResult] = useState<{ content: AppointmentResponse[]; totalElements: number } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)
  const size = 20

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((d) => setDoctors(d.content)).catch(() => [])
  }, [])

  const handleSearch = () => {
    setLoading(true)
    setError('')
    appointmentApi
      .search({
        date: filters.date || undefined,
        doctorId: filters.doctorId || undefined,
        status: filters.status || undefined,
        patientUhid: filters.patientUhid.trim() || undefined,
        patientName: filters.patientName.trim() || undefined,
        page,
        size,
      })
      .then((data) => setResult({ content: data.content, totalElements: data.totalElements }))
      .catch((err) => {
        setError(err.response?.data?.message || 'Search failed.')
        setResult(null)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    if (page > 0 && (filters.date || filters.doctorId || filters.patientUhid || filters.patientName)) {
      handleSearch()
    }
  }, [page])

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/appointments">Appointments</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Search</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Search Appointments</h1>
        <Link to="/front-office/appointments" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
      </div>

      <div className="card shadow-sm">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-2">
              <label className="form-label">Date</label>
              <input
                type="date"
                className="form-control"
                value={filters.date}
                onChange={(e) => setFilters((f) => ({ ...f, date: e.target.value }))}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Doctor</label>
              <select
                className="form-select"
                value={filters.doctorId}
                onChange={(e) => setFilters((f) => ({ ...f, doctorId: e.target.value ? Number(e.target.value) : '' }))}
              >
                <option value="">All</option>
                {doctors.map((d) => (
                  <option key={d.id} value={d.id}>{d.fullName}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={filters.status}
                onChange={(e) => setFilters((f) => ({ ...f, status: (e.target.value || '') as AppointmentStatus | '' }))}
              >
                {STATUS_OPTIONS.map((o) => (
                  <option key={o.value || 'all'} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">UHID</label>
              <input
                type="text"
                className="form-control"
                placeholder="UHID"
                value={filters.patientUhid}
                onChange={(e) => setFilters((f) => ({ ...f, patientUhid: e.target.value }))}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Patient Name</label>
              <input
                type="text"
                className="form-control"
                placeholder="Name"
                value={filters.patientName}
                onChange={(e) => setFilters((f) => ({ ...f, patientName: e.target.value }))}
              />
            </div>
            <div className="col-md-2 d-flex align-items-end">
              <button type="button" className="btn btn-primary w-100" onClick={handleSearch} disabled={loading}>
                {loading ? 'Searching…' : 'Search'}
              </button>
            </div>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {result && (
        <div className="card border shadow-sm">
          <div className="card-header bg-light py-2 d-flex justify-content-between align-items-center">
            <h6 className="mb-0">Results ({result.totalElements})</h6>
          </div>
          <div className="table-responsive">
            <table className="table table-striped mb-0">
              <thead className="table-light">
                <tr>
                  <th>Token</th>
                  <th>Patient</th>
                  <th>UHID</th>
                  <th>Doctor</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {result.content.length === 0 ? (
                  <tr><td colSpan={7} className="text-center text-muted py-4">No appointments found.</td></tr>
                ) : (
                  result.content.map((a) => (
                    <tr key={a.id}>
                      <td>{a.tokenNo ?? '—'}</td>
                      <td>{a.patientName}</td>
                      <td>{a.patientUhid}</td>
                      <td>{a.doctorName}</td>
                      <td>{a.appointmentDate}</td>
                      <td>{formatTime(a.slotTime)}</td>
                      <td><span className="badge bg-secondary">{a.status}</span></td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
