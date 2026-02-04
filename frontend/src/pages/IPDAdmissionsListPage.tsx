import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ipdApi } from '../api/ipd'
import type { IPDAdmissionResponse, AdmissionStatus } from '../types/ipd'
import styles from './IPDAdmissionsListPage.module.css'

const STATUS_OPTIONS: { value: '' | AdmissionStatus; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'ADMITTED', label: 'Admitted' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'TRANSFERRED', label: 'Shifted' },
  { value: 'DISCHARGE_INITIATED', label: 'Discharge initiated' },
  { value: 'DISCHARGED', label: 'Discharged' },
  { value: 'REFERRED', label: 'Referred' },
  { value: 'LAMA', label: 'LAMA' },
  { value: 'EXPIRED', label: 'Expired' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

/** Display label for status badge (Active, Discharged, Shifted, Referred, LAMA, Expired). */
function statusBadgeLabel(s: AdmissionStatus): string {
  switch (s) {
    case 'ACTIVE':
      return 'Active'
    case 'ADMITTED':
      return 'Admitted'
    case 'DISCHARGED':
      return 'Discharged'
    case 'TRANSFERRED':
      return 'Shifted'
    case 'REFERRED':
      return 'Referred'
    case 'LAMA':
      return 'LAMA'
    case 'EXPIRED':
      return 'Expired'
    case 'DISCHARGE_INITIATED':
      return 'Discharge initiated'
    case 'CANCELLED':
      return 'Cancelled'
    default:
      return s.replace(/_/g, ' ')
  }
}

/** Statuses that can be changed to CANCELLED (disable registration). */
const DISABLEABLE_STATUSES: AdmissionStatus[] = [
  'ADMITTED',
  'ACTIVE',
  'TRANSFERRED',
  'DISCHARGE_INITIATED',
]
function canDisableAdmission(status: AdmissionStatus): boolean {
  return DISABLEABLE_STATUSES.includes(status)
}

function statusClass(s: AdmissionStatus): string {
  switch (s) {
    case 'ADMITTED':
      return styles.statusAdmitted
    case 'ACTIVE':
      return styles.statusActive
    case 'TRANSFERRED':
      return styles.statusShifted
    case 'DISCHARGE_INITIATED':
      return styles.statusDischargeInitiated
    case 'DISCHARGED':
      return styles.statusDischarged
    case 'REFERRED':
      return styles.statusReferred
    case 'LAMA':
      return styles.statusLama
    case 'EXPIRED':
      return styles.statusExpired
    case 'CANCELLED':
      return styles.statusCancelled
    default:
      return ''
  }
}

function openPrintWindow(result: { content: IPDAdmissionResponse[]; totalElements: number }) {
  const rows = result.content
    .map(
      (a) =>
        `<tr><td>${a.admissionNumber ?? ''}</td><td>${a.patientName ?? ''}</td><td>${a.patientUhid ?? ''}</td><td>${a.primaryDoctorName ?? ''}</td><td>${a.currentWardName ?? '—'} — ${a.currentBedNumber ?? '—'}</td><td>${a.admissionDateTime ? String(a.admissionDateTime).replace('T', ' ').slice(0, 16) : '—'}</td><td>${statusBadgeLabel(a.admissionStatus)}</td></tr>`
    )
    .join('')
  const win = window.open('', '_blank')
  if (!win) return win
  win.document.write(`
    <!DOCTYPE html><html><head><title>IPD Admissions List</title>
    <style>body{font-family:sans-serif;padding:1.5rem;} table{border-collapse:collapse;width:100%;} th,td{border:1px solid #ccc;padding:0.5rem 0.75rem;text-align:left;} th{background:#f5f5f5;}</style>
    </head><body>
    <h1>IPD Admissions</h1>
    <p>Total: ${result.totalElements}</p>
    <table class="table table-striped"><thead><tr><th>Admission #</th><th>Patient</th><th>UHID</th><th>Doctor</th><th>Ward / Bed</th><th>Admitted</th><th>Status</th></tr></thead><tbody>${rows}</tbody></table>
    </body></html>
  `)
  win.document.close()
  return win
}

export function IPDAdmissionsListPage() {
  const [admissionNumber, setAdmissionNumber] = useState('')
  const [patientUhid, setPatientUhid] = useState('')
  const [patientName, setPatientName] = useState('')
  const [status, setStatus] = useState<'' | AdmissionStatus>('')
  const [page, setPage] = useState(0)
  const size = 20
  const [applied, setApplied] = useState({
    admissionNumber: '',
    patientUhid: '',
    patientName: '',
    status: '' as '' | AdmissionStatus,
  })
  const [result, setResult] = useState<{
    content: IPDAdmissionResponse[]
    totalElements: number
    totalPages: number
    number: number
  } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [disablingId, setDisablingId] = useState<number | null>(null)

  useEffect(() => {
    setLoading(true)
    setError('')
    ipdApi
      .search({
        admissionNumber: applied.admissionNumber || undefined,
        patientUhid: applied.patientUhid || undefined,
        patientName: applied.patientName.trim() || undefined,
        status: applied.status || undefined,
        page,
        size,
      })
      .then((data) => {
        // #region agent log
        fetch('http://127.0.0.1:7243/ingest/aef12ba4-e33a-42fa-8d18-6d760473dcb7',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'IPDAdmissionsListPage.tsx:then',message:'search ok',data:{contentLength:Array.isArray(data?.content)?data.content.length:null},timestamp:Date.now(),sessionId:'debug-session',hypothesisId:'H2'})}).catch(()=>{});
        // #endregion
        const content = Array.isArray(data?.content) ? data.content : []
        setResult({
          content,
          totalElements: typeof data?.totalElements === 'number' ? data.totalElements : content.length,
          totalPages: typeof data?.totalPages === 'number' ? data.totalPages : 1,
          number: typeof data?.number === 'number' ? data.number : 0,
        })
      })
      .catch((err) => {
        // #region agent log
        fetch('http://127.0.0.1:7243/ingest/aef12ba4-e33a-42fa-8d18-6d760473dcb7',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'IPDAdmissionsListPage.tsx:catch',message:'search failed',data:{status:err?.response?.status,message:err?.response?.data?.message},timestamp:Date.now(),sessionId:'debug-session',hypothesisId:'H1,H5'})}).catch(()=>{});
        // #endregion
        setError(err.response?.data?.message || 'Failed to search admissions')
        setResult(null)
      })
      .finally(() => setLoading(false))
  }, [applied.admissionNumber, applied.patientUhid, applied.patientName, applied.status, page])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setApplied({ admissionNumber, patientUhid, patientName, status })
    setPage(0)
  }

  const handleDisable = (admissionId: number) => {
    if (!window.confirm('Disable this admission (set status to Cancelled)? The bed will be released.')) return
    setDisablingId(admissionId)
    setError('')
    ipdApi
      .changeAdmissionStatus(admissionId, { toStatus: 'CANCELLED', reason: 'Disabled from list' })
      .then(() => {
        ipdApi
          .search({
            admissionNumber: applied.admissionNumber || undefined,
            patientUhid: applied.patientUhid || undefined,
            patientName: applied.patientName.trim() || undefined,
            status: applied.status || undefined,
            page,
            size,
          })
          .then((data) => {
            const content = Array.isArray(data?.content) ? data.content : []
            setResult({
              content,
              totalElements: typeof data?.totalElements === 'number' ? data.totalElements : content.length,
              totalPages: typeof data?.totalPages === 'number' ? data.totalPages : 1,
              number: typeof data?.number === 'number' ? data.number : 0,
            })
          })
          .catch(() => {})
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to disable admission')
      })
      .finally(() => setDisablingId(null))
  }

  const handlePrint = () => {
    if (!result || result.content.length === 0) return
    const win = openPrintWindow(result)
    if (win) {
      win.focus()
      setTimeout(() => {
        win.print()
        win.close()
      }, 250)
    }
  }

  const handleExportPdf = () => {
    if (!result || result.content.length === 0) return
    const win = openPrintWindow(result)
    if (win) {
      win.focus()
      setTimeout(() => {
        win.print()
        // Keep window open so user can choose "Save as PDF" in print dialog
        // win.close() after print is cancelled to avoid double-close
      }, 300)
    }
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><a href="/ipd">IPD</a></li>
          <li className="breadcrumb-item"><a href="/ipd/admissions">Admissions</a></li>
          <li className="breadcrumb-item active" aria-current="page">List</li>
        </ol>
      </nav>

      <h2 className="h5 mb-0 fw-bold">IPD Admissions</h2>

      <div className="card shadow-sm">
        <div className="card-header">
          <h3 className="h6 mb-0 fw-bold">Filters</h3>
        </div>
        <div className="card-body">
          <form onSubmit={handleSearch} className="row g-2 align-items-end flex-wrap">
            <div className="col-auto">
              <label className="form-label small mb-0">Admission #</label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={admissionNumber}
                onChange={(e) => setAdmissionNumber(e.target.value)}
                placeholder="e.g. IPD-2025-000001"
              />
            </div>
            <div className="col-auto">
              <label className="form-label small mb-0">UHID</label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={patientUhid}
                onChange={(e) => setPatientUhid(e.target.value)}
                placeholder="e.g. HMS-2025-000001"
              />
            </div>
            <div className="col-auto">
              <label className="form-label small mb-0">Patient name</label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={patientName}
                onChange={(e) => setPatientName(e.target.value)}
                placeholder="Search by name"
              />
            </div>
            <div className="col-auto">
              <label className="form-label small mb-0">Status</label>
              <select
                value={status}
                onChange={(e) => setStatus((e.target.value || '') as '' | AdmissionStatus)}
                className="form-select form-select-sm"
              >
                {STATUS_OPTIONS.map((o) => (
                  <option key={o.value || 'any'} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-auto">
              <button type="submit" className="btn btn-primary btn-sm">
                Search
              </button>
            </div>
          </form>
        </div>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0" role="alert">{error}</div>}
      {loading && <p className="text-muted mb-0">Loading…</p>}

      {!loading && result && (
        <div className="card shadow-sm">
          <div className="card-header d-flex flex-wrap align-items-center justify-content-between gap-2">
            <h3 className="h6 mb-0 fw-bold">Admitted patients — Total: {result.totalElements}</h3>
            <div className="d-flex gap-2">
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={handlePrint}>
                Print IPD list
              </button>
              <button type="button" className="btn btn-outline-primary btn-sm" onClick={handleExportPdf}>
                Export PDF
              </button>
            </div>
          </div>
          {result.content.length === 0 ? (
            <div className="card-body">
              <p className="text-muted mb-0 text-center py-3">No admissions found. Admit a patient from <Link to="/ipd/admission-management">IPD Admission Management</Link> or <Link to="/ipd/admit">Admit Patient</Link>.</p>
            </div>
          ) : (
            <>
              <div className="table-responsive">
                <table className="table table-striped mb-0">
                  <thead>
                    <tr>
                      <th>Admission #</th>
                      <th>Patient</th>
                      <th>Doctor</th>
                      <th>Ward / Bed</th>
                      <th>Admitted</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.content.map((a) => (
                      <tr key={a.id}>
                        <td className="fw-medium">{a.admissionNumber ?? '—'}</td>
                        <td>
                          <span className="fw-medium">{a.patientName ?? '—'}</span>
                          <div className="small text-muted">{a.patientUhid ?? '—'}</div>
                        </td>
                        <td>{a.primaryDoctorName ?? '—'}</td>
                        <td>
                          {[a.currentWardName, a.currentBedNumber].filter(Boolean).join(' / ') || '—'}
                        </td>
                        <td>{a.admissionDateTime ? String(a.admissionDateTime).replace('T', ' ').slice(0, 16) : '—'}</td>
                        <td>
                          <span className={`badge ${statusClass(a.admissionStatus)}`}>
                            {statusBadgeLabel(a.admissionStatus)}
                          </span>
                        </td>
                        <td>
                          <div className="d-flex gap-2 flex-wrap">
                            <Link to={`/ipd/admissions/${a.id}`} className="text-decoration-none">View</Link>
                            <Link to={`/ipd/admissions/${a.id}/edit`} className="text-decoration-none">Edit</Link>
                            {canDisableAdmission(a.admissionStatus) && (
                              <button
                                type="button"
                                className="btn btn-link btn-sm p-0 text-danger text-decoration-none"
                                onClick={() => handleDisable(a.id)}
                                disabled={disablingId === a.id}
                              >
                                {disablingId === a.id ? '…' : 'Disable'}
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {result.totalPages > 1 && (
                <div className="card-footer d-flex align-items-center gap-3 flex-wrap">
                  <span className="small text-muted">
                    Page {result.number + 1} of {result.totalPages}
                  </span>
                  <nav aria-label="Admissions pagination">
                    <ul className="pagination pagination-sm mb-0">
                      <li className="page-item">
                        <button
                          type="button"
                          className="page-link"
                          disabled={page <= 0}
                          onClick={() => setPage((p) => p - 1)}
                        >
                          Previous
                        </button>
                      </li>
                      <li className="page-item">
                        <button
                          type="button"
                          className="page-link"
                          disabled={page >= result.totalPages - 1}
                          onClick={() => setPage((p) => p + 1)}
                        >
                          Next
                        </button>
                      </li>
                    </ul>
                  </nav>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
