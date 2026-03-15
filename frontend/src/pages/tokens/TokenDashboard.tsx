/**
 * Token Management Dashboard – Generate tokens, view queue, link to display.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { tokenApi } from '../../api/token'
import { doctorsApi } from '../../api/doctors'
import { PatientSearch } from '../../components/reception/PatientSearch'
import type { TokenDashboard as DashboardType } from '../../types/token.types'
import type { DoctorResponse } from '../../types/doctor'
import type { PatientResponse } from '../../types/patient'

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

export function TokenDashboard() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | null>(null)
  const [dashboard, setDashboard] = useState<DashboardType | null>(null)
  const [patient, setPatient] = useState<PatientResponse | null>(null)
  const [priority, setPriority] = useState<string>('NORMAL')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [loading, setLoading] = useState(false)
  const [generateLoading, setGenerateLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((r) => setDoctors(r.content)).catch(() => [])
  }, [])

  useEffect(() => {
    if (!selectedDoctorId) {
      setDashboard(null)
      return
    }
    setLoading(true)
    setError('')
    tokenApi
      .getDashboard(selectedDoctorId, date)
      .then(setDashboard)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load dashboard.')
        setDashboard(null)
      })
      .finally(() => setLoading(false))
  }, [selectedDoctorId, date])

  const handleGenerate = async () => {
    if (!patient || !selectedDoctorId) {
      setError('Please select patient and doctor.')
      return
    }
    const doc = doctors.find((d) => d.id === selectedDoctorId)
    if (!doc?.departmentId) {
      setError('Doctor has no department.')
      return
    }
    setGenerateLoading(true)
    setError('')
    try {
      await tokenApi.generate({
        patientId: patient.id,
        doctorId: selectedDoctorId,
        departmentId: doc.departmentId,
        priority: priority as 'NORMAL' | 'EMERGENCY' | 'SENIOR' | 'FOLLOWUP',
      })
      setPatient(null)
      if (selectedDoctorId) {
        tokenApi.getDashboard(selectedDoctorId, date).then(setDashboard)
      }
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to generate token.')
    } finally {
      setGenerateLoading(false)
    }
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/tokens">Tokens</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Dashboard</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Token Management</h1>
        <div className="d-flex gap-2">
          <Link to="/front-office/tokens/queue" className="btn btn-outline-primary btn-sm">Queue</Link>
          <a href="/display/tokens" className="btn btn-outline-secondary btn-sm" target="_blank" rel="noopener noreferrer">
            Display Screen
          </a>
        </div>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}

      <div className="card shadow-sm">
        <div className="card-header">Generate Token</div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-6">
              <PatientSearch
                value={patient?.uhid}
                displayName={patient?.fullName}
                onSelect={(p) => setPatient(p ?? null)}
                label="Patient"
                required
              />
            </div>
            <div className="col-md-4">
              <label className="form-label">Doctor</label>
              <select
                className="form-select"
                value={selectedDoctorId ?? ''}
                onChange={(e) => setSelectedDoctorId(e.target.value ? Number(e.target.value) : null)}
              >
                <option value="">Select doctor</option>
                {doctors.map((d) => (
                  <option key={d.id} value={d.id}>{d.fullName} ({d.code})</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Priority</label>
              <select
                className="form-select"
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
              >
                {Object.entries(PRIORITY_LABELS).map(([k, v]) => (
                  <option key={k} value={k}>{v}</option>
                ))}
              </select>
            </div>
            <div className="col-12">
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleGenerate}
                disabled={generateLoading || !patient || !selectedDoctorId}
              >
                {generateLoading ? 'Generating…' : 'Generate Token'}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm">
        <div className="card-header d-flex align-items-center justify-content-between">
          <span>Live Queue</span>
          <div className="d-flex gap-2 align-items-center">
            <input
              type="date"
              className="form-control form-control-sm"
              style={{ width: 'auto' }}
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />
            <select
              className="form-select form-select-sm"
              style={{ width: 'auto' }}
              value={selectedDoctorId ?? ''}
              onChange={(e) => setSelectedDoctorId(e.target.value ? Number(e.target.value) : null)}
            >
              <option value="">Select doctor</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>{d.fullName}</option>
              ))}
            </select>
          </div>
        </div>
        <div className="card-body">
          {!selectedDoctorId ? (
            <p className="text-muted mb-0">Select a doctor to view queue.</p>
          ) : loading ? (
            <p className="text-muted mb-0">Loading…</p>
          ) : dashboard ? (
            <div className="row g-3">
              <div className="col-md-4">
                <h6 className="text-primary">Waiting ({dashboard.waiting?.length ?? 0})</h6>
                <ul className="list-group list-group-flush">
                  {(dashboard.waiting ?? []).slice(0, 10).map((t) => (
                    <li key={t.id} className="list-group-item d-flex justify-content-between py-2">
                      <span><strong>{t.tokenNo}</strong> {t.patientName}</span>
                      <span className="badge bg-secondary">{PRIORITY_LABELS[t.priority] ?? t.priority}</span>
                    </li>
                  ))}
                  {(dashboard.waiting?.length ?? 0) > 10 && (
                    <li className="list-group-item py-2 text-muted">+{(dashboard.waiting?.length ?? 0) - 10} more</li>
                  )}
                </ul>
              </div>
              <div className="col-md-4">
                <h6 className="text-warning">In Consultation ({dashboard.inConsultation?.length ?? 0})</h6>
                <ul className="list-group list-group-flush">
                  {(dashboard.inConsultation ?? []).map((t) => (
                    <li key={t.id} className="list-group-item d-flex justify-content-between py-2">
                      <span><strong>{t.tokenNo}</strong> {t.patientName}</span>
                      <span className="badge bg-warning text-dark">{STATUS_LABELS[t.status] ?? t.status}</span>
                    </li>
                  ))}
                </ul>
              </div>
              <div className="col-md-4">
                <h6 className="text-success">Completed ({dashboard.completed?.length ?? 0})</h6>
                <ul className="list-group list-group-flush">
                  {(dashboard.completed ?? []).slice(0, 10).map((t) => (
                    <li key={t.id} className="list-group-item d-flex justify-content-between py-2">
                      <span><strong>{t.tokenNo}</strong> {t.patientName}</span>
                      <span className="badge bg-success">Done</span>
                    </li>
                  ))}
                  {(dashboard.completed?.length ?? 0) > 10 && (
                    <li className="list-group-item py-2 text-muted">+{(dashboard.completed?.length ?? 0) - 10} more</li>
                  )}
                </ul>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  )
}
