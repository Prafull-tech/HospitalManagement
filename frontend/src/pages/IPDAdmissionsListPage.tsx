import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ipdApi } from '../api/ipd'
import type { IPDAdmissionResponse, AdmissionStatus } from '../types/ipd'
import styles from './IPDAdmissionsListPage.module.css'

const STATUS_OPTIONS: { value: '' | AdmissionStatus; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'ADMITTED', label: 'Admitted' },
  { value: 'TRANSFERRED', label: 'Transferred' },
  { value: 'DISCHARGE_INITIATED', label: 'Discharge initiated' },
  { value: 'DISCHARGED', label: 'Discharged' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

function statusClass(s: AdmissionStatus): string {
  switch (s) {
    case 'ADMITTED':
      return styles.statusAdmitted
    case 'TRANSFERRED':
      return styles.statusTransferred
    case 'DISCHARGE_INITIATED':
      return styles.statusDischargeInitiated
    case 'DISCHARGED':
      return styles.statusDischarged
    case 'CANCELLED':
      return styles.statusCancelled
    default:
      return ''
  }
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
      .then((data) =>
        setResult({
          content: data.content,
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          number: data.number,
        })
      )
      .catch((err) => {
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

  const handlePrint = () => {
    if (!result || result.content.length === 0) return
    const win = window.open('', '_blank')
    if (!win) return
    const rows = result.content
      .map(
        (a) =>
          `<tr><td>${a.admissionNumber}</td><td>${a.patientName}</td><td>${a.patientUhid}</td><td>${a.primaryDoctorName}</td><td>${a.currentWardName ?? '—'} — ${a.currentBedNumber ?? '—'}</td><td>${a.admissionDateTime.replace('T', ' ').slice(0, 16)}</td><td>${a.admissionStatus}</td></tr>`
      )
      .join('')
    win.document.write(`
      <!DOCTYPE html><html><head><title>IPD Admissions</title>
      <style>body{font-family:sans-serif;padding:1.5rem;} table{border-collapse:collapse;width:100%;} th,td{border:1px solid #ccc;padding:0.5rem 0.75rem;text-align:left;} th{background:#f5f5f5;}</style>
      </head><body>
      <h1>IPD Admissions</h1>
      <p>Total: ${result.totalElements}</p>
      <table><thead><tr><th>Admission #</th><th>Patient</th><th>UHID</th><th>Doctor</th><th>Ward / Bed</th><th>Admitted</th><th>Status</th></tr></thead><tbody>${rows}</tbody></table>
      </body></html>
    `)
    win.document.close()
    win.focus()
    setTimeout(() => { win.print(); win.close(); }, 250)
  }

  return (
    <div className={styles.page}>
      <form onSubmit={handleSearch} className={styles.filters}>
        <div className={styles.filterRow}>
          <label>
            <span className={styles.label}>Admission #</span>
            <input
              type="text"
              value={admissionNumber}
              onChange={(e) => setAdmissionNumber(e.target.value)}
              placeholder="e.g. IPD-2025-000001"
              className={styles.input}
            />
          </label>
          <label>
            <span className={styles.label}>Patient UHID</span>
            <input
              type="text"
              value={patientUhid}
              onChange={(e) => setPatientUhid(e.target.value)}
              placeholder="e.g. HMS-2025-000001"
              className={styles.input}
            />
          </label>
          <label>
            <span className={styles.label}>Patient name</span>
            <input
              type="text"
              value={patientName}
              onChange={(e) => setPatientName(e.target.value)}
              placeholder="Search by name"
              className={styles.input}
            />
          </label>
          <label>
            <span className={styles.label}>Status</span>
            <select
              value={status}
              onChange={(e) => setStatus((e.target.value || '') as '' | AdmissionStatus)}
              className={styles.select}
            >
              {STATUS_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" className={styles.searchBtn}>
            Search
          </button>
        </div>
      </form>

      {error && <div className={styles.error}>{error}</div>}
      {loading && <div className={styles.loading}>Loading…</div>}

      {!loading && result && (
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <h2 className={styles.cardTitle}>
              IPD Admissions — Total: {result.totalElements}
            </h2>
            <button type="button" onClick={handlePrint} className={styles.printBtn}>
              Print
            </button>
          </div>
          {result.content.length === 0 ? (
            <p className={styles.empty}>No admissions found.</p>
          ) : (
            <>
              <div className={styles.tableWrap}>
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th>Admission #</th>
                      <th>Patient</th>
                      <th>Doctor</th>
                      <th>Ward / Bed</th>
                      <th>Admitted</th>
                      <th>Status</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.content.map((a) => (
                      <tr key={a.id}>
                        <td className={styles.admissionNum}>{a.admissionNumber}</td>
                        <td>
                          <span className={styles.patientName}>{a.patientName}</span>
                          <div className={styles.uhid}>{a.patientUhid}</div>
                        </td>
                        <td>{a.primaryDoctorName}</td>
                        <td>
                          {a.currentWardName && a.currentBedNumber
                            ? `${a.currentWardName} — ${a.currentBedNumber}`
                            : '—'}
                        </td>
                        <td>{a.admissionDateTime.replace('T', ' ').slice(0, 16)}</td>
                        <td>
                          <span className={`${styles.statusBadge} ${statusClass(a.admissionStatus)}`}>
                            {a.admissionStatus.replace(/_/g, ' ')}
                          </span>
                        </td>
                        <td>
                          <Link to={`/ipd/admissions/${a.id}`} className={styles.link}>
                            Open
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {result.totalPages > 1 && (
                <div className={styles.pagination}>
                  <button
                    type="button"
                    className={styles.pageBtn}
                    disabled={page <= 0}
                    onClick={() => setPage((p) => p - 1)}
                  >
                    Previous
                  </button>
                  <span className={styles.pageInfo}>
                    Page {result.number + 1} of {result.totalPages}
                  </span>
                  <button
                    type="button"
                    className={styles.pageBtn}
                    disabled={page >= result.totalPages - 1}
                    onClick={() => setPage((p) => p + 1)}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
