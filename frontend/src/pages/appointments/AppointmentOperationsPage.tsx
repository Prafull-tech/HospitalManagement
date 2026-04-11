import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import { doctorsApi } from '../../api/doctors'
import type { AppointmentResponse, AppointmentStatus } from '../../types/appointment.types'
import type { DoctorResponse } from '../../types/doctor'
import styles from './AppointmentOperationsPage.module.css'

const STATUS_OPTIONS: { value: AppointmentStatus | ''; label: string }[] = [
  { value: '', label: 'All Statuses' },
  { value: 'BOOKED', label: 'Booked' },
  { value: 'CONFIRMED', label: 'Confirmed' },
  { value: 'PENDING_CONFIRMATION', label: 'Pending' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'NO_SHOW', label: 'No Show' },
]

const QUEUE_STATUS_LABELS: Record<string, string> = {
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

function statusBadgeClass(status: AppointmentResponse['status']) {
  if (status === 'CONFIRMED' || status === 'COMPLETED') return 'bg-success'
  if (status === 'PENDING_CONFIRMATION') return 'bg-warning text-dark'
  if (status === 'BOOKED') return 'bg-secondary'
  return 'bg-danger'
}

type Filters = {
  date: string
  doctorId: number | ''
  status: AppointmentStatus | ''
  patientUhid: string
  patientName: string
}

type Props = {
  defaultView?: 'search' | 'queue'
}

export function AppointmentOperationsPage({ defaultView = 'search' }: Props) {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [filters, setFilters] = useState<Filters>({
    date: new Date().toISOString().slice(0, 10),
    doctorId: '',
    status: '',
    patientUhid: '',
    patientName: '',
  })
  const [activeView, setActiveView] = useState<'search' | 'queue'>(defaultView)
  const [searchResult, setSearchResult] = useState<{ content: AppointmentResponse[]; totalElements: number } | null>(null)
  const [queue, setQueue] = useState<AppointmentResponse[]>([])
  const [loadingSearch, setLoadingSearch] = useState(false)
  const [loadingQueue, setLoadingQueue] = useState(false)
  const [searchError, setSearchError] = useState('')
  const [queueError, setQueueError] = useState('')
  const [page, setPage] = useState(0)
  const [lastQueueRefresh, setLastQueueRefresh] = useState<Date | null>(null)
  const size = 20

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((d) => setDoctors(d.content)).catch(() => [])
  }, [])

  useEffect(() => {
    setActiveView(defaultView)
  }, [defaultView])

  const selectedDoctor = useMemo(
    () => doctors.find((doctor) => doctor.id === filters.doctorId) ?? null,
    [doctors, filters.doctorId],
  )

  const queueSummary = useMemo(() => {
    const waiting = queue.filter((item) => item.status === 'BOOKED' || item.status === 'CONFIRMED').length
    const pending = queue.filter((item) => item.status === 'PENDING_CONFIRMATION').length
    const served = queue.filter((item) => item.status === 'COMPLETED').length
    return { waiting, pending, served, total: queue.length }
  }, [queue])

  const runSearch = () => {
    setLoadingSearch(true)
    setSearchError('')
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
      .then((data) => setSearchResult({ content: data.content, totalElements: data.totalElements }))
      .catch((err) => {
        setSearchError(err.response?.data?.message || 'Search failed.')
        setSearchResult(null)
      })
      .finally(() => setLoadingSearch(false))
  }

  const runQueueRefresh = () => {
    if (!filters.doctorId) {
      setQueue([])
      setQueueError('')
      return
    }
    setLoadingQueue(true)
    setQueueError('')
    appointmentApi
      .getQueue(Number(filters.doctorId), filters.date || undefined)
      .then((data) => {
        setQueue(data)
        setLastQueueRefresh(new Date())
      })
      .catch((err) => {
        setQueueError(err.response?.data?.message || 'Failed to load queue.')
        setQueue([])
      })
      .finally(() => setLoadingQueue(false))
  }

  useEffect(() => {
    setPage(0)
  }, [filters.date, filters.doctorId, filters.status, filters.patientUhid, filters.patientName])

  useEffect(() => {
    runSearch()
  }, [page, filters.date, filters.doctorId, filters.status, filters.patientUhid, filters.patientName])

  useEffect(() => {
    runQueueRefresh()
  }, [filters.doctorId, filters.date])

  useEffect(() => {
    if (!filters.doctorId) return undefined
    const intervalId = window.setInterval(() => {
      runQueueRefresh()
    }, 15000)
    return () => window.clearInterval(intervalId)
  }, [filters.doctorId, filters.date])

  return (
    <div className="hms-page-shell">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/appointments">Appointments</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Operations</li>
        </ol>
      </nav>

      <div className="hms-page-hero hms-page-hero-dark">
        <div>
          <div className="hms-page-kicker">Live Desk</div>
          <h1 className="hms-page-title">Appointment Search and Queue</h1>
          <p className="hms-page-subtitle">One screen for live queue monitoring, doctor filtering, and appointment lookup.</p>
        </div>
        <div className="hms-page-actions">
          <button
            type="button"
            className={`btn btn-sm ${activeView === 'search' ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => setActiveView('search')}
          >
            Search View
          </button>
          <button
            type="button"
            className={`btn btn-sm ${activeView === 'queue' ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => setActiveView('queue')}
          >
            Queue View
          </button>
          <Link to="/front-office/appointments" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
        </div>
      </div>

      <div className="hms-section-card">
        <div className="hms-section-card-header">
          <div>
            <h2 className="hms-section-title">Shared Filters</h2>
            <p className="hms-section-subtitle">These filters drive both queue tracking and appointment search.</p>
          </div>
        </div>
        <div className="hms-section-card-body">
        <div className="row g-2 g-md-3">
          <div className="col-md-2">
            <label className="form-label">Date</label>
            <input
              type="date"
              className="form-control"
              value={filters.date}
              onChange={(e) => setFilters((f) => ({ ...f, date: e.target.value }))}
            />
          </div>
          <div className="col-md-3">
            <label className="form-label">Doctor</label>
            <select
              className="form-select"
              value={filters.doctorId}
              onChange={(e) => setFilters((f) => ({ ...f, doctorId: e.target.value ? Number(e.target.value) : '' }))}
            >
              <option value="">All Doctors</option>
              {doctors.map((doctor) => (
                <option key={doctor.id} value={doctor.id}>{doctor.fullName}</option>
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
              {STATUS_OPTIONS.map((option) => (
                <option key={option.value || 'all'} value={option.value}>{option.label}</option>
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
          <div className="col-md-3">
            <label className="form-label">Patient Name</label>
            <input
              type="text"
              className="form-control"
              placeholder="Name"
              value={filters.patientName}
              onChange={(e) => setFilters((f) => ({ ...f, patientName: e.target.value }))}
            />
          </div>
        </div>
      </div>
      </div>

      <div className="hms-metric-grid">
        <div className="hms-metric-card">
          <span className="hms-metric-label">Selected Doctor</span>
          <strong className="hms-metric-value">{selectedDoctor?.fullName ?? 'All Doctors'}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Queue Waiting</span>
          <strong className="hms-metric-value">{queueSummary.waiting}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Pending</span>
          <strong className="hms-metric-value">{queueSummary.pending}</strong>
        </div>
        <div className="hms-metric-card">
          <span className="hms-metric-label">Last Refresh</span>
          <strong className="hms-metric-value">{lastQueueRefresh ? lastQueueRefresh.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}</strong>
        </div>
      </div>

      {(queueError || searchError) && (
        <div className="d-flex flex-column gap-2">
          {queueError && <div className="alert alert-danger mb-0">{queueError}</div>}
          {searchError && <div className="alert alert-danger mb-0">{searchError}</div>}
        </div>
      )}

      <div className={styles.panelGrid}>
        <section className={`${styles.panel} ${activeView === 'queue' ? styles.panelActive : ''}`}>
          <div className={styles.panelHeader}>
            <div>
              <h2 className={styles.panelTitle}>Live Queue</h2>
              <p className={styles.panelText}>Auto-refreshes every 15 seconds for the selected doctor and date.</p>
            </div>
            <button type="button" className="btn btn-outline-primary btn-sm" onClick={runQueueRefresh} disabled={loadingQueue || !filters.doctorId}>
              {loadingQueue ? 'Refreshing…' : 'Refresh Queue'}
            </button>
          </div>
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th>Token</th>
                  <th>Patient</th>
                  <th>UHID</th>
                  <th>Time</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {loadingQueue ? (
                  <tr><td colSpan={5} className="text-center py-4"><div className="spinner-border spinner-border-sm" /></td></tr>
                ) : !filters.doctorId ? (
                  <tr><td colSpan={5} className="text-center text-muted py-4">Choose a doctor to start live queue tracking.</td></tr>
                ) : queue.length === 0 ? (
                  <tr><td colSpan={5} className="text-center text-muted py-4">No queue entries for this doctor and date.</td></tr>
                ) : (
                  queue.map((appointment) => (
                    <tr key={appointment.id}>
                      <td>{appointment.tokenNo ?? '—'}</td>
                      <td>{appointment.patientName}</td>
                      <td>{appointment.patientUhid}</td>
                      <td>{formatTime(appointment.slotTime)}</td>
                      <td><span className={`badge ${statusBadgeClass(appointment.status)}`}>{QUEUE_STATUS_LABELS[appointment.status] ?? appointment.status}</span></td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className={`${styles.panel} ${activeView === 'search' ? styles.panelActive : ''}`}>
          <div className={styles.panelHeader}>
            <div>
              <h2 className={styles.panelTitle}>Appointment Search</h2>
              <p className={styles.panelText}>Search updates automatically from the same filters used for the queue.</p>
            </div>
            <button type="button" className="btn btn-outline-primary btn-sm" onClick={runSearch} disabled={loadingSearch}>
              {loadingSearch ? 'Searching…' : 'Refresh Search'}
            </button>
          </div>
          <div className={styles.resultsMeta}>
            <span>Results: {searchResult?.totalElements ?? 0}</span>
            <span>Page: {page + 1}</span>
          </div>
          <div className="table-responsive">
            <table className="table table-striped align-middle mb-0">
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
                {loadingSearch ? (
                  <tr><td colSpan={7} className="text-center py-4"><div className="spinner-border spinner-border-sm" /></td></tr>
                ) : !searchResult || searchResult.content.length === 0 ? (
                  <tr><td colSpan={7} className="text-center text-muted py-4">No appointments found for the current filters.</td></tr>
                ) : (
                  searchResult.content.map((appointment) => (
                    <tr key={appointment.id}>
                      <td>{appointment.tokenNo ?? '—'}</td>
                      <td>{appointment.patientName}</td>
                      <td>{appointment.patientUhid}</td>
                      <td>{appointment.doctorName}</td>
                      <td>{appointment.appointmentDate}</td>
                      <td>{formatTime(appointment.slotTime)}</td>
                      <td><span className={`badge ${statusBadgeClass(appointment.status)}`}>{appointment.status}</span></td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <div className={styles.paginationBar}>
            <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={page === 0 || loadingSearch}>
              Previous
            </button>
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm"
              onClick={() => setPage((current) => current + 1)}
              disabled={loadingSearch || !!searchResult && (page + 1) * size >= searchResult.totalElements}
            >
              Next
            </button>
          </div>
        </section>
      </div>
    </div>
  )
}