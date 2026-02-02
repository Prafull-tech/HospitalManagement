import { useState, useEffect, useRef } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { nursingApi } from '../api/nursing'
import type { NursingNoteResponse } from '../types/nursing'
import styles from './NursingNotesPrintPage.module.css'

const HOSPITAL_NAME = 'Hospital Management System'
const REPORT_TITLE = 'Nursing Notes'

export function NursingNotesPrintPage() {
  const [searchParams] = useSearchParams()
  const [notes, setNotes] = useState<NursingNoteResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const printRef = useRef<HTMLDivElement>(null)

  const ipdAdmissionId = searchParams.get('ipdAdmissionId')
  const recordedDateFrom = searchParams.get('recordedDateFrom') ?? ''
  const recordedDateTo = searchParams.get('recordedDateTo') ?? ''
  const shiftType = searchParams.get('shiftType') ?? ''

  useEffect(() => {
    setLoading(true)
    setError('')
    const params: {
      ipdAdmissionId?: number
      recordedDateFrom?: string
      recordedDateTo?: string
      shiftType?: string
    } = {}
    if (ipdAdmissionId) params.ipdAdmissionId = Number(ipdAdmissionId)
    if (recordedDateFrom) params.recordedDateFrom = recordedDateFrom
    if (recordedDateTo) params.recordedDateTo = recordedDateTo
    if (shiftType) params.shiftType = shiftType

    nursingApi
      .getNotesPrint(params)
      .then(setNotes)
      .catch(() => {
        setError('Failed to load notes for print.')
        setNotes([])
      })
      .finally(() => setLoading(false))
  }, [ipdAdmissionId, recordedDateFrom, recordedDateTo, shiftType])

  const handlePrint = () => {
    window.print()
  }

  const handlePdf = () => {
    window.print()
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <p className={styles.message}>Loading notes…</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className={styles.page}>
        <p className={styles.error}>{error}</p>
        <Link to="/nursing/notes" className={styles.backLink}>Back to Nursing Notes</Link>
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <div className={styles.noPrint}>
        <div className={styles.toolbar}>
          <Link to="/nursing/notes" className={styles.backLink}>← Back to Notes</Link>
          <button type="button" className={styles.printBtn} onClick={handlePrint}>
            Print
          </button>
          <button type="button" className={styles.printBtn} onClick={handlePdf}>
            Save as PDF
          </button>
        </div>
      </div>

      <div ref={printRef} className={styles.printArea}>
        <header className={styles.header}>
          <h1 className={styles.hospitalName}>{HOSPITAL_NAME}</h1>
          <h2 className={styles.reportTitle}>{REPORT_TITLE}</h2>
          <p className={styles.printMeta}>
            Generated: {new Date().toLocaleString()}
            {notes.length > 0 && notes[0].patientName && (
              <> · Patient: {notes[0].patientName} ({notes[0].patientUhid ?? ''})</>
            )}
          </p>
        </header>

        {notes.length === 0 ? (
          <p className={styles.empty}>No notes to print.</p>
        ) : (
          <div className={styles.notesSection}>
            {notes.map((n) => (
              <div
                key={n.id}
                className={`${styles.noteBlock} ${n.wardType === 'ICU' || n.wardType === 'CCU' || n.wardType === 'NICU' || n.wardType === 'HDU' ? styles.noteIcu : ''}`}
              >
                <div className={styles.noteMeta}>
                  <span className={styles.shiftBadge}>{n.shiftType}</span>
                  <span>{n.noteType.replace(/_/g, ' ')}</span>
                  <span>{n.recordedAt.replace('T', ' ').slice(0, 16)}</span>
                  {n.recordedByName && <span> — {n.recordedByName}</span>}
                  {n.noteStatus === 'LOCKED' && <span className={styles.lockedLabel}> [Locked]</span>}
                  {n.wardName && n.bedNumber && (
                    <span> · {n.wardName} / Bed {n.bedNumber}</span>
                  )}
                  {n.criticalFlags && <span className={styles.criticalFlags}> · {n.criticalFlags}</span>}
                </div>
                <div className={styles.noteContent}>{n.content}</div>
              </div>
            ))}
          </div>
        )}

        <footer className={styles.footer}>
          <p>{HOSPITAL_NAME} — {REPORT_TITLE} — Confidential</p>
          <p>Page generated on {new Date().toISOString().slice(0, 10)}</p>
        </footer>
      </div>
    </div>
  )
}
