/**
 * Token Queue – Token No, Patient, Doctor, Priority, Status.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { tokenApi } from '../../api/token'
import { doctorsApi } from '../../api/doctors'
import type { TokenResponse } from '../../types/token.types'
import type { DoctorResponse } from '../../types/doctor'

const PRIORITY_LABELS: Record<string, string> = {
  NORMAL: 'Normal',
  EMERGENCY: 'Emergency',
  SENIOR: 'Senior',
  FOLLOWUP: 'Follow-up',
  PREGNANT: 'Pregnant',
}

const STATUS_LABELS: Record<string, string> = {
  WAITING: 'Waiting',
  CALLED: 'Called',
  IN_CONSULTATION: 'In Consultation',
  COMPLETED: 'Completed',
  SKIPPED: 'Skipped',
}

const STATUS_BADGE: Record<string, string> = {
  WAITING: 'bg-secondary',
  CALLED: 'bg-info',
  IN_CONSULTATION: 'bg-warning text-dark',
  COMPLETED: 'bg-success',
  SKIPPED: 'bg-light text-dark',
}

export function TokenQueuePage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | ''>('')
  const [queue, setQueue] = useState<TokenResponse[]>([])
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((r) => setDoctors(r.content)).catch(() => [])
  }, [])

  useEffect(() => {
    if (!selectedDoctorId) {
      setQueue([])
      return
    }
    setLoading(true)
    setError('')
    tokenApi
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
          <li className="breadcrumb-item"><Link to="/front-office/tokens">Tokens</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Queue</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Token Queue</h1>
        <Link to="/front-office/tokens" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}

      <div className="card shadow-sm">
        <div className="card-body">
          <div className="row g-2 mb-3">
            <div className="col-md-4">
              <label className="form-label">Doctor</label>
              <select
                className="form-select"
                value={selectedDoctorId}
                onChange={(e) => setSelectedDoctorId(e.target.value ? Number(e.target.value) : '')}
              >
                <option value="">Select doctor</option>
                {doctors.map((d) => (
                  <option key={d.id} value={d.id}>{d.fullName} ({d.code})</option>
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

          {loading ? (
            <p className="text-muted mb-0">Loading…</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead>
                  <tr>
                    <th>Token No</th>
                    <th>Patient Name</th>
                    <th>Doctor</th>
                    <th>Priority</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {queue.map((t) => (
                    <tr key={t.id}>
                      <td><strong>{t.tokenNo}</strong></td>
                      <td>{t.patientName}</td>
                      <td>{t.doctorName}</td>
                      <td><span className="badge bg-light text-dark">{PRIORITY_LABELS[t.priority] ?? t.priority}</span></td>
                      <td><span className={`badge ${STATUS_BADGE[t.status] ?? 'bg-secondary'}`}>{STATUS_LABELS[t.status] ?? t.status}</span></td>
                    </tr>
                  ))}
                  {queue.length === 0 && selectedDoctorId && (
                    <tr><td colSpan={5} className="text-muted text-center py-4">No tokens in queue.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
