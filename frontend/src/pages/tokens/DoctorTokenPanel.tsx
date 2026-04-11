/**
 * Doctor Token Panel – Call Next, Start Consultation, Complete, Skip.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { tokenApi } from '../../api/token'
import { doctorsApi } from '../../api/doctors'
import type { TokenDashboard } from '../../types/token.types'
import type { DoctorResponse } from '../../types/doctor'

const STATUS_LABELS: Record<string, string> = {
  WAITING: 'Waiting',
  CALLED: 'Called',
  IN_CONSULTATION: 'In Consultation',
  COMPLETED: 'Completed',
  SKIPPED: 'Skipped',
}

export function DoctorTokenPanel() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | null>(null)
  const [dashboard, setDashboard] = useState<TokenDashboard | null>(null)
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [loading, setLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((r) => setDoctors(r.content)).catch(() => [])
  }, [])

  const fetchDashboard = () => {
    if (!selectedDoctorId) return
    setLoading(true)
    setError('')
    tokenApi
      .getDashboard(selectedDoctorId, date)
      .then(setDashboard)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load.')
        setDashboard(null)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    fetchDashboard()
  }, [selectedDoctorId, date])

  const handleCallNext = async () => {
    if (!selectedDoctorId) return
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.callNext(selectedDoctorId, date)
      fetchDashboard()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to call next.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleStart = async (tokenId: number) => {
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.startConsultation(tokenId)
      fetchDashboard()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to start.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleComplete = async (tokenId: number) => {
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.complete(tokenId)
      fetchDashboard()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to complete.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleSkip = async (tokenId: number) => {
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.skip(tokenId)
      fetchDashboard()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to skip.')
    } finally {
      setActionLoading(false)
    }
  }

  const calledOrConsultation = (dashboard?.inConsultation ?? []).filter(
    (t) => t.status === 'CALLED' || t.status === 'IN_CONSULTATION'
  )

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/opd">OPD</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Token Panel</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Doctor Token Panel</h1>
        <Link to="/opd/queue" className="btn btn-outline-secondary btn-sm">OPD Queue</Link>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}

      <div className="card shadow-sm">
        <div className="card-body">
          <div className="row g-2 mb-3">
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

          {!selectedDoctorId ? (
            <p className="text-muted mb-0">Select a doctor to manage tokens.</p>
          ) : loading ? (
            <p className="text-muted mb-0">Loading…</p>
          ) : (
            <>
              <div className="mb-4">
                <button
                  type="button"
                  className="btn btn-primary btn-lg"
                  onClick={handleCallNext}
                  disabled={actionLoading || (dashboard?.waiting?.length ?? 0) === 0}
                >
                  {actionLoading ? 'Processing…' : 'Call Next Token'}
                </button>
              </div>

              {calledOrConsultation.length > 0 && (
                <div className="mb-4">
                  <h6>Current Patient(s)</h6>
                  <div className="d-flex flex-wrap gap-2">
                    {calledOrConsultation.map((t) => (
                      <div key={t.id} className="card border-primary" style={{ minWidth: 200 }}>
                        <div className="card-body py-2">
                          <p className="mb-1"><strong>{t.tokenNo}</strong> {t.patientName}</p>
                          <p className="mb-2 small text-muted">{STATUS_LABELS[t.status]}</p>
                          <div className="d-flex gap-1">
                            {t.status === 'CALLED' && (
                              <button
                                type="button"
                                className="btn btn-sm btn-success"
                                onClick={() => handleStart(t.id)}
                                disabled={actionLoading}
                              >
                                Start Consultation
                              </button>
                            )}
                            {(t.status === 'CALLED' || t.status === 'IN_CONSULTATION') && (
                              <>
                                <button
                                  type="button"
                                  className="btn btn-sm btn-primary"
                                  onClick={() => handleComplete(t.id)}
                                  disabled={actionLoading}
                                >
                                  Complete
                                </button>
                                <button
                                  type="button"
                                  className="btn btn-sm btn-outline-warning"
                                  onClick={() => handleSkip(t.id)}
                                  disabled={actionLoading}
                                >
                                  Skip
                                </button>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div>
                <h6>Waiting ({dashboard?.waiting?.length ?? 0})</h6>
                <ul className="list-group">
                  {(dashboard?.waiting ?? []).slice(0, 5).map((t) => (
                    <li key={t.id} className="list-group-item d-flex justify-content-between">
                      <span><strong>{t.tokenNo}</strong> {t.patientName}</span>
                      <span className="badge bg-secondary">Waiting</span>
                    </li>
                  ))}
                  {(dashboard?.waiting?.length ?? 0) > 5 && (
                    <li className="list-group-item text-muted">+{(dashboard?.waiting?.length ?? 0) - 5} more</li>
                  )}
                </ul>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
