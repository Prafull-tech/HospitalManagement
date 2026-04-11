import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { tokenApi } from '../../api/token'
import { doctorsApi } from '../../api/doctors'
import { PatientSearch } from '../../components/reception/PatientSearch'
import type { TokenDashboard as DashboardType, TokenResponse } from '../../types/token.types'
import type { DoctorResponse } from '../../types/doctor'
import type { PatientResponse } from '../../types/patient'
import styles from './TokenOperationsPage.module.css'

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

type Props = {
  defaultView?: 'dashboard' | 'queue'
}

export function TokenOperationsPage({ defaultView = 'dashboard' }: Props) {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | null>(null)
  const [dashboard, setDashboard] = useState<DashboardType | null>(null)
  const [queue, setQueue] = useState<TokenResponse[]>([])
  const [patient, setPatient] = useState<PatientResponse | null>(null)
  const [priority, setPriority] = useState<string>('NORMAL')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [activeView, setActiveView] = useState<'dashboard' | 'queue'>(defaultView)
  const [loading, setLoading] = useState(false)
  const [queueLoading, setQueueLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  const [generateLoading, setGenerateLoading] = useState(false)
  const [error, setError] = useState('')
  const [lastRefresh, setLastRefresh] = useState<Date | null>(null)

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((r) => setDoctors(r.content)).catch(() => [])
  }, [])

  useEffect(() => {
    setActiveView(defaultView)
  }, [defaultView])

  const fetchDashboard = () => {
    if (!selectedDoctorId) {
      setDashboard(null)
      return Promise.resolve()
    }
    setLoading(true)
    setError('')
    return tokenApi
      .getDashboard(selectedDoctorId, date)
      .then((data) => {
        setDashboard(data)
        setLastRefresh(new Date())
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load dashboard.')
        setDashboard(null)
      })
      .finally(() => setLoading(false))
  }

  const fetchQueue = () => {
    if (!selectedDoctorId) {
      setQueue([])
      return Promise.resolve()
    }
    setQueueLoading(true)
    setError('')
    return tokenApi
      .getQueue(selectedDoctorId, date)
      .then((data) => {
        setQueue(data)
        setLastRefresh(new Date())
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load queue.')
        setQueue([])
      })
      .finally(() => setQueueLoading(false))
  }

  const refreshAll = () => Promise.all([fetchDashboard(), fetchQueue()])

  useEffect(() => {
    refreshAll()
  }, [selectedDoctorId, date])

  useEffect(() => {
    if (!selectedDoctorId) return undefined
    const intervalId = window.setInterval(() => {
      refreshAll()
    }, 10000)
    return () => window.clearInterval(intervalId)
  }, [selectedDoctorId, date])

  const handleGenerate = async () => {
    if (!patient || !selectedDoctorId) {
      setError('Please select patient and doctor.')
      return
    }
    const doctor = doctors.find((item) => item.id === selectedDoctorId)
    if (!doctor?.departmentId) {
      setError('Doctor has no department.')
      return
    }
    setGenerateLoading(true)
    setError('')
    try {
      await tokenApi.generate({
        patientId: patient.id,
        doctorId: selectedDoctorId,
        departmentId: doctor.departmentId,
        priority: priority as 'NORMAL' | 'EMERGENCY' | 'SENIOR' | 'FOLLOWUP' | 'PREGNANT',
      })
      setPatient(null)
      await refreshAll()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to generate token.')
    } finally {
      setGenerateLoading(false)
    }
  }

  const handleCallNext = async () => {
    if (!selectedDoctorId) return
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.callNext(selectedDoctorId, date)
      await refreshAll()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to call next token.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleStart = async (tokenId: number) => {
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.startConsultation(tokenId)
      await refreshAll()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to start consultation.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleComplete = async (tokenId: number) => {
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.complete(tokenId)
      await refreshAll()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to complete token.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleSkip = async (tokenId: number) => {
    setActionLoading(true)
    setError('')
    try {
      await tokenApi.skip(tokenId)
      await refreshAll()
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to skip token.')
    } finally {
      setActionLoading(false)
    }
  }

  const doctor = useMemo(
    () => doctors.find((item) => item.id === selectedDoctorId) ?? null,
    [doctors, selectedDoctorId],
  )

  const currentPatients = useMemo(
    () => (dashboard?.inConsultation ?? []).filter((item) => item.status === 'CALLED' || item.status === 'IN_CONSULTATION'),
    [dashboard],
  )

  const queueSummary = useMemo(() => ({
    waiting: dashboard?.waiting?.length ?? 0,
    current: currentPatients.length,
    completed: dashboard?.completed?.length ?? 0,
    total: queue.length,
  }), [dashboard, currentPatients.length, queue.length])

  return (
    <div className="hms-page-shell">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/tokens">Tokens</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Operations</li>
        </ol>
      </nav>

      <div className="hms-page-hero hms-page-hero-dark">
        <div>
          <div className="hms-page-kicker">Realtime Token Desk</div>
          <h1 className="hms-page-title">Token Management</h1>
          <p className="hms-page-subtitle">Generate tokens, monitor live queue, and control doctor flow from one realtime screen.</p>
        </div>
        <div className="hms-page-actions">
          <button
            type="button"
            className={`btn btn-sm ${activeView === 'dashboard' ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => setActiveView('dashboard')}
          >
            Desk View
          </button>
          <button
            type="button"
            className={`btn btn-sm ${activeView === 'queue' ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => setActiveView('queue')}
          >
            Queue View
          </button>
          <a href="/display/tokens" className="btn btn-outline-secondary btn-sm" target="_blank" rel="noopener noreferrer">
            Display Screen
          </a>
        </div>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}

      <div className="hms-metric-grid">
        <div className="hms-metric-card">
          <span className="hms-metric-label">Doctor</span>
          <strong className="hms-metric-value">{doctor?.fullName ?? 'Select Doctor'}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Waiting</span>
          <strong className="hms-metric-value">{queueSummary.waiting}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Current</span>
          <strong className="hms-metric-value">{queueSummary.current}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Last Refresh</span>
          <strong className="hms-metric-value">{lastRefresh ? lastRefresh.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}</strong>
        </div>
      </div>

      <div className={styles.layoutGrid}>
        <section className={`hms-section-card ${activeView === 'dashboard' ? styles.activePanel : ''}`}>
          <div className="hms-section-card-header">
            <div>
              <h2 className="hms-section-title">Generate Token</h2>
              <p className="hms-section-subtitle">Issue a new token and update the queue instantly.</p>
            </div>
          </div>
          <div className="hms-section-card-body d-flex flex-column gap-3">
            <div className="row g-3">
              <div className="col-md-12">
                <PatientSearch
                  value={patient?.uhid}
                  displayName={patient?.fullName}
                  onSelect={(p) => setPatient(p ?? null)}
                  label="Patient"
                  required
                />
              </div>
              <div className="col-md-7">
                <label className="form-label">Doctor</label>
                <select
                  className="form-select"
                  value={selectedDoctorId ?? ''}
                  onChange={(e) => setSelectedDoctorId(e.target.value ? Number(e.target.value) : null)}
                >
                  <option value="">Select doctor</option>
                  {doctors.map((item) => (
                    <option key={item.id} value={item.id}>{item.fullName} ({item.code})</option>
                  ))}
                </select>
              </div>
              <div className="col-md-5">
                <label className="form-label">Priority</label>
                <select className="form-select" value={priority} onChange={(e) => setPriority(e.target.value)}>
                  {Object.entries(PRIORITY_LABELS).map(([key, value]) => (
                    <option key={key} value={key}>{value}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Date</label>
                <input type="date" className="form-control" value={date} onChange={(e) => setDate(e.target.value)} />
              </div>
              <div className="col-md-6 d-flex align-items-end">
                <button
                  type="button"
                  className="btn btn-primary w-100"
                  onClick={handleGenerate}
                  disabled={generateLoading || !patient || !selectedDoctorId}
                >
                  {generateLoading ? 'Generating…' : 'Generate Token'}
                </button>
              </div>
            </div>

            <div className={styles.actionBar}>
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleCallNext}
                disabled={actionLoading || !selectedDoctorId || queueSummary.waiting === 0}
              >
                {actionLoading ? 'Processing…' : 'Call Next Token'}
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => refreshAll()}
                disabled={!selectedDoctorId || loading || queueLoading}
              >
                Refresh Now
              </button>
            </div>

            <div className={styles.currentGrid}>
              {currentPatients.length === 0 ? (
                <div className={styles.emptyState}>No patient currently called or in consultation.</div>
              ) : currentPatients.map((item) => (
                <article key={item.id} className={styles.currentCard}>
                  <div className={styles.currentToken}>{item.tokenNo}</div>
                  <div className={styles.currentName}>{item.patientName}</div>
                  <div className={styles.currentMeta}>{STATUS_LABELS[item.status]}</div>
                  <div className={styles.currentActions}>
                    {item.status === 'CALLED' && (
                      <button type="button" className="btn btn-sm btn-success" onClick={() => handleStart(item.id)} disabled={actionLoading}>
                        Start
                      </button>
                    )}
                    {(item.status === 'CALLED' || item.status === 'IN_CONSULTATION') && (
                      <>
                        <button type="button" className="btn btn-sm btn-primary" onClick={() => handleComplete(item.id)} disabled={actionLoading}>
                          Complete
                        </button>
                        <button type="button" className="btn btn-sm btn-outline-warning" onClick={() => handleSkip(item.id)} disabled={actionLoading}>
                          Skip
                        </button>
                      </>
                    )}
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className={`hms-section-card ${activeView === 'queue' ? styles.activePanel : ''}`}>
          <div className="hms-section-card-header">
            <div>
              <h2 className="hms-section-title">Live Queue</h2>
              <p className="hms-section-subtitle">Auto-refreshes every 10 seconds for realtime token flow.</p>
            </div>
          </div>
          <div className="hms-section-card-body">
            {!selectedDoctorId ? (
              <div className={styles.emptyState}>Select a doctor to view the live queue.</div>
            ) : queueLoading && queue.length === 0 ? (
              <p className="text-muted mb-0">Loading queue…</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>Token No</th>
                      <th>Patient</th>
                      <th>Doctor</th>
                      <th>Priority</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {queue.map((item) => (
                      <tr key={item.id}>
                        <td><strong>{item.tokenNo}</strong></td>
                        <td>
                          <div>{item.patientName}</div>
                          <div className={styles.rowMeta}>{item.uhid}</div>
                        </td>
                        <td>{item.doctorName}</td>
                        <td><span className="badge bg-light text-dark">{PRIORITY_LABELS[item.priority] ?? item.priority}</span></td>
                        <td><span className={`badge ${STATUS_BADGE[item.status] ?? 'bg-secondary'}`}>{STATUS_LABELS[item.status] ?? item.status}</span></td>
                      </tr>
                    ))}
                    {queue.length === 0 && (
                      <tr><td colSpan={5} className="text-center text-muted py-4">No tokens in queue.</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
            )}

            {dashboard && (
              <div className={styles.queueColumns}>
                <div>
                  <h3 className={styles.columnTitle}>Waiting ({dashboard.waiting?.length ?? 0})</h3>
                  <ul className="list-group list-group-flush">
                    {(dashboard.waiting ?? []).slice(0, 8).map((item) => (
                      <li key={item.id} className="list-group-item d-flex justify-content-between py-2">
                        <span><strong>{item.tokenNo}</strong> {item.patientName}</span>
                        <span className="badge bg-secondary">{PRIORITY_LABELS[item.priority] ?? item.priority}</span>
                      </li>
                    ))}
                  </ul>
                </div>
                <div>
                  <h3 className={styles.columnTitle}>In Consultation ({dashboard.inConsultation?.length ?? 0})</h3>
                  <ul className="list-group list-group-flush">
                    {(dashboard.inConsultation ?? []).slice(0, 8).map((item) => (
                      <li key={item.id} className="list-group-item d-flex justify-content-between py-2">
                        <span><strong>{item.tokenNo}</strong> {item.patientName}</span>
                        <span className={`badge ${STATUS_BADGE[item.status] ?? 'bg-secondary'}`}>{STATUS_LABELS[item.status] ?? item.status}</span>
                      </li>
                    ))}
                  </ul>
                </div>
                <div>
                  <h3 className={styles.columnTitle}>Completed ({dashboard.completed?.length ?? 0})</h3>
                  <ul className="list-group list-group-flush">
                    {(dashboard.completed ?? []).slice(0, 8).map((item) => (
                      <li key={item.id} className="list-group-item d-flex justify-content-between py-2">
                        <span><strong>{item.tokenNo}</strong> {item.patientName}</span>
                        <span className="badge bg-success">Done</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  )
}