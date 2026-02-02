import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { opdApi } from '../api/opd'
import { doctorsApi } from '../api/doctors'
import type { OPDVisitResponse, VisitStatus } from '../types/opd'
import type { DoctorResponse } from '../types/doctor'
import styles from './OPDSearchVisitsPage.module.css'

const today = new Date().toISOString().slice(0, 10)
const STATUS_OPTIONS: { value: '' | VisitStatus; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'REGISTERED', label: 'Registered' },
  { value: 'IN_CONSULTATION', label: 'In consultation' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'REFERRED', label: 'Referred' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

function statusClass(s: VisitStatus): string {
  switch (s) {
    case 'REGISTERED':
      return styles.statusRegistered
    case 'IN_CONSULTATION':
      return styles.statusInConsultation
    case 'COMPLETED':
      return styles.statusCompleted
    case 'REFERRED':
      return styles.statusReferred
    case 'CANCELLED':
      return styles.statusCancelled
    default:
      return ''
  }
}

export function OPDSearchVisitsPage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [visitDate, setVisitDate] = useState(today)
  const [doctorId, setDoctorId] = useState<number | ''>('')
  const [status, setStatus] = useState<'' | VisitStatus>('')
  const [patientUhid, setPatientUhid] = useState('')
  const [patientName, setPatientName] = useState('')
  const [visitNumber, setVisitNumber] = useState('')
  const [page, setPage] = useState(0)
  const size = 20

  /* Applied filters: search runs when these or page change (submit or pagination). */
  const [applied, setApplied] = useState({
    visitDate: today,
    doctorId: '' as number | '',
    status: '' as '' | VisitStatus,
    patientUhid: '',
    patientName: '',
    visitNumber: '',
  })
  const [result, setResult] = useState<{
    content: OPDVisitResponse[]
    totalElements: number
    totalPages: number
    number: number
  } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    doctorsApi
      .list({ status: 'ACTIVE', page: 0, size: 200 })
      .then((data) => setDoctors(data.content))
      .catch(() => setDoctors([]))
  }, [])

  useEffect(() => {
    setLoading(true)
    setError('')
    const params = {
      visitDate: applied.visitDate || undefined,
      doctorId: applied.doctorId || undefined,
      status: applied.status || undefined,
      patientUhid: applied.patientUhid.trim() || undefined,
      patientName: applied.patientName.trim() || undefined,
      visitNumber: applied.visitNumber.trim() || undefined,
      page,
      size,
    }
    opdApi
      .search(params)
      .then((data) =>
        setResult({
          content: data.content,
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          number: data.number,
        })
      )
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to search visits')
        setResult(null)
      })
      .finally(() => setLoading(false))
  }, [applied.visitDate, applied.doctorId, applied.status, applied.patientUhid, applied.patientName, applied.visitNumber, page])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setApplied({
      visitDate,
      doctorId,
      status,
      patientUhid,
      patientName,
      visitNumber,
    })
    setPage(0)
  }

  const handlePrint = () => {
    if (!result || result.content.length === 0) return
    const win = window.open('', '_blank')
    if (!win) return
    const rows = result.content
      .map(
        (v) =>
          `<tr><td>${v.visitNumber}</td><td>${v.tokenNumber ?? '—'}</td><td>${v.patientName}</td><td>${v.patientUhid}</td><td>${v.doctorName}</td><td>${v.visitDate}</td><td>${v.visitStatus}</td></tr>`
      )
      .join('')
    win.document.write(`
      <!DOCTYPE html><html><head><title>OPD Visits</title>
      <style>body{font-family:sans-serif;padding:1.5rem;} table{border-collapse:collapse;width:100%;} th,td{border:1px solid #ccc;padding:0.5rem 0.75rem;text-align:left;} th{background:#f5f5f5;}</style>
      </head><body>
      <h1>OPD Visits</h1>
      <p>Date: ${applied.visitDate} | Total: ${result.totalElements}</p>
      <table><thead><tr><th>Visit #</th><th>Token</th><th>Patient</th><th>UHID</th><th>Doctor</th><th>Date</th><th>Status</th></tr></thead><tbody>${rows}</tbody></table>
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
            <span className={styles.label}>Visit date</span>
            <input
              type="date"
              value={visitDate}
              onChange={(e) => setVisitDate(e.target.value)}
              className={styles.input}
            />
          </label>
          <label>
            <span className={styles.label}>Doctor</span>
            <select
              value={doctorId ?? ''}
              onChange={(e) => setDoctorId(e.target.value ? Number(e.target.value) : '')}
              className={styles.select}
            >
              <option value="">All doctors</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.fullName} — {d.departmentName}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span className={styles.label}>Status</span>
            <select
              value={status}
              onChange={(e) => setStatus((e.target.value || '') as '' | VisitStatus)}
              className={styles.select}
            >
              {STATUS_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className={styles.filterRow}>
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
            <span className={styles.label}>Visit number</span>
            <input
              type="text"
              value={visitNumber}
              onChange={(e) => setVisitNumber(e.target.value)}
              placeholder="e.g. OPD-2025-000001"
              className={styles.input}
            />
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
              OPD Visits — Total: {result.totalElements}
            </h2>
            <button type="button" onClick={handlePrint} className={styles.printBtn}>
              Print
            </button>
          </div>
          {result.content.length === 0 ? (
            <p className={styles.empty}>No visits found.</p>
          ) : (
            <>
              <div className={styles.tableWrap}>
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th>Visit #</th>
                      <th>Token</th>
                      <th>Patient</th>
                      <th>Doctor</th>
                      <th>Date</th>
                      <th>Status</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.content.map((v) => (
                      <tr key={v.id}>
                        <td className={styles.visitNum}>{v.visitNumber}</td>
                        <td>
                          <span className={styles.tokenBadge}>{v.tokenNumber ?? '—'}</span>
                        </td>
                        <td>
                          <span className={styles.patientName}>{v.patientName}</span>
                          <div className={styles.uhid}>{v.patientUhid}</div>
                        </td>
                        <td>{v.doctorName}</td>
                        <td>{v.visitDate}</td>
                        <td>
                          <span className={`${styles.statusBadge} ${statusClass(v.visitStatus)}`}>
                            {v.visitStatus.replace(/_/g, ' ')}
                          </span>
                        </td>
                        <td>
                          <Link to={`/opd/visits/${v.id}`} className={styles.link}>
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
