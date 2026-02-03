import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { nursingApi } from '../api/nursing'
import type {
  NursingNoteResponse,
  NursingNoteSearchParams,
  WardTypeNursing,
  ShiftType,
  NoteStatus,
} from '../types/nursing'
import styles from './NursingNotesSearchPage.module.css'

const WARD_OPTIONS: { value: '' | WardTypeNursing; label: string }[] = [
  { value: '', label: 'Any ward' },
  { value: 'GENERAL', label: 'General Ward' },
  { value: 'PRIVATE', label: 'Private Ward' },
  { value: 'SEMI_PRIVATE', label: 'Semi-private' },
  { value: 'ICU', label: 'ICU' },
  { value: 'CCU', label: 'CCU' },
  { value: 'NICU', label: 'NICU' },
  { value: 'HDU', label: 'HDU' },
  { value: 'EMERGENCY', label: 'Emergency' },
]

const SHIFT_OPTIONS: { value: '' | ShiftType; label: string }[] = [
  { value: '', label: 'Any shift' },
  { value: 'MORNING', label: 'Morning' },
  { value: 'EVENING', label: 'Evening' },
  { value: 'NIGHT', label: 'Night' },
]

const STATUS_OPTIONS: { value: '' | NoteStatus; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'DRAFT', label: 'Draft' },
  { value: 'LOCKED', label: 'Locked' },
]

const PAGE_SIZE = 20

export function NursingNotesSearchPage() {
  const [patientName, setPatientName] = useState('')
  const [patientUhid, setPatientUhid] = useState('')
  const [bedNumber, setBedNumber] = useState('')
  const [wardType, setWardType] = useState<'' | WardTypeNursing>('')
  const [recordedDateFrom, setRecordedDateFrom] = useState('')
  const [recordedDateTo, setRecordedDateTo] = useState('')
  const [shiftType, setShiftType] = useState<'' | ShiftType>('')
  const [noteStatus, setNoteStatus] = useState<'' | NoteStatus>('')
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<{
    content: NursingNoteResponse[]
    totalElements: number
    totalPages: number
    number: number
    size: number
  } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const search = (pageNum: number = 0) => {
    const params: NursingNoteSearchParams = {
      page: pageNum,
      size: PAGE_SIZE,
    }
    if (patientName.trim()) params.patientName = patientName.trim()
    if (patientUhid.trim()) params.patientUhid = patientUhid.trim()
    if (bedNumber.trim()) params.bedNumber = bedNumber.trim()
    if (wardType) params.wardType = wardType
    if (recordedDateFrom) params.recordedDateFrom = recordedDateFrom
    if (recordedDateTo) params.recordedDateTo = recordedDateTo
    if (shiftType) params.shiftType = shiftType
    if (noteStatus) params.noteStatus = noteStatus

    setLoading(true)
    setError('')
    nursingApi
      .searchNotes(params)
      .then((data) =>
        setResult({
          content: data.content,
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          number: data.number,
          size: data.size,
        })
      )
      .catch((err) => {
        setError(err.response?.data?.message || 'Search failed.')
        setResult(null)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    search(page)
  }, [page])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
    search(0)
  }

  const handlePrintSelection = (notes: NursingNoteResponse[]) => {
    if (notes.length === 0) return
    const ids = notes.map((n) => n.ipdAdmissionId)
    const uniq = [...new Set(ids)]
    const params = new URLSearchParams()
    if (uniq.length === 1) {
      params.set('ipdAdmissionId', String(uniq[0]))
    } else {
      if (recordedDateFrom) params.set('recordedDateFrom', recordedDateFrom)
      if (recordedDateTo) params.set('recordedDateTo', recordedDateTo)
      if (shiftType) params.set('shiftType', shiftType)
    }
    window.open(`/nursing/notes/print?${params.toString()}`, '_blank', 'noopener,noreferrer')
  }

  return (
    <div className={styles.page}>
      <div className={styles.headerRow}>
        <h2 className={styles.pageTitle}>Search Nursing Notes</h2>
        <Link to="/nursing/notes" className={styles.backLink}>
          ← Back to Notes
        </Link>
      </div>

      <form onSubmit={handleSubmit} className={styles.filters}>
        <div className={styles.filterGrid}>
          <label className={styles.label}>
            Patient name (partial)
            <input
              type="text"
              value={patientName}
              onChange={(e) => setPatientName(e.target.value)}
              placeholder="Search by name"
              className={styles.input}
            />
          </label>
          <label className={styles.label}>
            UHID
            <input
              type="text"
              value={patientUhid}
              onChange={(e) => setPatientUhid(e.target.value)}
              placeholder="UHID"
              className={styles.input}
            />
          </label>
          <label className={styles.label}>
            Bed no
            <input
              type="text"
              value={bedNumber}
              onChange={(e) => setBedNumber(e.target.value)}
              placeholder="Bed number"
              className={styles.input}
            />
          </label>
          <label className={styles.label}>
            Ward type
            <select value={wardType} onChange={(e) => setWardType((e.target.value || '') as '' | WardTypeNursing)} className={styles.select}>
              {WARD_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>{o.label}</option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            Date from
            <input
              type="date"
              value={recordedDateFrom}
              onChange={(e) => setRecordedDateFrom(e.target.value)}
              className={styles.input}
            />
          </label>
          <label className={styles.label}>
            Date to
            <input
              type="date"
              value={recordedDateTo}
              onChange={(e) => setRecordedDateTo(e.target.value)}
              className={styles.input}
            />
          </label>
          <label className={styles.label}>
            Shift
            <select value={shiftType} onChange={(e) => setShiftType((e.target.value || '') as '' | ShiftType)} className={styles.select}>
              {SHIFT_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>{o.label}</option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            Status
            <select value={noteStatus} onChange={(e) => setNoteStatus((e.target.value || '') as '' | NoteStatus)} className={styles.select}>
              {STATUS_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>{o.label}</option>
              ))}
            </select>
          </label>
        </div>
        <button type="submit" className={styles.searchBtn} disabled={loading}>
          {loading ? 'Searching…' : 'Search'}
        </button>
      </form>

      {error && <div className={styles.error}>{error}</div>}

      {!loading && result && (
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <h3 className={styles.cardTitle}>
              Results — {result.totalElements} note{result.totalElements !== 1 ? 's' : ''}
            </h3>
            {result.content.length > 0 && (
              <button
                type="button"
                className={styles.printBtn}
                onClick={() => handlePrintSelection(result.content)}
              >
                Print / PDF
              </button>
            )}
          </div>
          {result.content.length === 0 ? (
            <p className={styles.empty}>No notes found.</p>
          ) : (
            <>
              <div className={styles.tableWrap}>
                <table className={`table table-striped ${styles.table}`}>
                  <thead>
                    <tr>
                      <th>Date / Time</th>
                      <th>Patient</th>
                      <th>UHID</th>
                      <th>Ward / Bed</th>
                      <th>Shift</th>
                      <th>Type</th>
                      <th>Status</th>
                      <th>Content</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.content.map((n) => (
                      <tr
                        key={n.id}
                        className={
                          n.wardType === 'ICU' || n.wardType === 'CCU' || n.wardType === 'NICU' || n.wardType === 'HDU'
                            ? styles.rowIcu
                            : ''
                        }
                      >
                        <td className={styles.cellDate}>{n.recordedAt.replace('T', ' ').slice(0, 16)}</td>
                        <td>{n.patientName ?? '—'}</td>
                        <td className={styles.cellUhid}>{n.patientUhid ?? '—'}</td>
                        <td>{n.wardName ?? '—'} / {n.bedNumber ?? '—'}</td>
                        <td><span className={styles.shiftBadge}>{n.shiftType}</span></td>
                        <td>{n.noteType.replace(/_/g, ' ')}</td>
                        <td>
                          <span className={n.noteStatus === 'LOCKED' ? styles.statusLocked : styles.statusDraft}>
                            {n.noteStatus}
                          </span>
                        </td>
                        <td className={styles.cellContent}>{n.content.slice(0, 80)}{n.content.length > 80 ? '…' : ''}</td>
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
