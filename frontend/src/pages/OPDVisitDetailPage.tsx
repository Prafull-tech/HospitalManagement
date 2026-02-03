import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { opdApi } from '../api/opd'
import { doctorsApi } from '../api/doctors'
import { departmentsApi } from '../api/doctors'
import type {
  OPDVisitResponse,
  VisitStatus,
  OPDClinicalNoteRequest,
  OPDReferRequest,
} from '../types/opd'
import type { DoctorResponse, DepartmentResponse } from '../types/doctor'
import styles from './OPDVisitDetailPage.module.css'

const VISIT_STATUSES: VisitStatus[] = [
  'REGISTERED',
  'IN_CONSULTATION',
  'COMPLETED',
  'REFERRED',
  'CANCELLED',
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

export function OPDVisitDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [visit, setVisit] = useState<OPDVisitResponse | null>(null)
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [departments, setDepartments] = useState<DepartmentResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [notesForm, setNotesForm] = useState<OPDClinicalNoteRequest>({
    chiefComplaint: '',
    provisionalDiagnosis: '',
    doctorRemarks: '',
  })
  const [notesSubmitting, setNotesSubmitting] = useState(false)

  const [statusForm, setStatusForm] = useState<VisitStatus | ''>('')
  const [statusSubmitting, setStatusSubmitting] = useState(false)

  const [referForm, setReferForm] = useState<OPDReferRequest>({
    referredToDepartmentId: undefined,
    referredToDoctorId: undefined,
    referToIpd: false,
    referralRemarks: '',
  })
  const [referSubmitting, setReferSubmitting] = useState(false)

  const visitId = id ? Number(id) : null

  useEffect(() => {
    if (!visitId) return
    setLoading(true)
    setError('')
    opdApi
      .getById(visitId)
      .then((v) => {
        setVisit(v)
        setNotesForm({
          chiefComplaint: v.clinicalNote?.chiefComplaint ?? '',
          provisionalDiagnosis: v.clinicalNote?.provisionalDiagnosis ?? '',
          doctorRemarks: v.clinicalNote?.doctorRemarks ?? '',
        })
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load visit'))
      .finally(() => setLoading(false))
  }, [visitId])

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 300 }).then((d) => setDoctors(d.content)).catch(() => [])
    departmentsApi.list().then(setDepartments).catch(() => [])
  }, [])

  const handleUpdateStatus = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!visitId || !statusForm) return
    setStatusSubmitting(true)
    setSuccess('')
    setError('')
    try {
      const updated = await opdApi.updateStatus(visitId, { status: statusForm })
      setVisit(updated)
      setSuccess('Status updated.')
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to update status.')
    } finally {
      setStatusSubmitting(false)
    }
  }

  const handleSaveNotes = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!visitId) return
    setNotesSubmitting(true)
    setSuccess('')
    setError('')
    try {
      await opdApi.addNotes(visitId, notesForm)
      const updated = await opdApi.getById(visitId)
      setVisit(updated)
      setNotesForm({
        chiefComplaint: updated.clinicalNote?.chiefComplaint ?? '',
        provisionalDiagnosis: updated.clinicalNote?.provisionalDiagnosis ?? '',
        doctorRemarks: updated.clinicalNote?.doctorRemarks ?? '',
      })
      setSuccess('Clinical notes saved.')
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to save notes.')
    } finally {
      setNotesSubmitting(false)
    }
  }

  const handleRefer = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!visitId) return
    setReferSubmitting(true)
    setSuccess('')
    setError('')
    try {
      const updated = await opdApi.refer(visitId, {
        referredToDepartmentId: referForm.referredToDepartmentId || undefined,
        referredToDoctorId: referForm.referredToDoctorId || undefined,
        referToIpd: referForm.referToIpd || false,
        referralRemarks: referForm.referralRemarks?.trim() || undefined,
      })
      setVisit(updated)
      setSuccess('Referral recorded.')
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to refer.')
    } finally {
      setReferSubmitting(false)
    }
  }

  const handlePrint = () => {
    if (!visit) return
    const win = window.open('', '_blank')
    if (!win) return
    const note = visit.clinicalNote
    const noteHtml = note
      ? `<tr><td>Chief complaint</td><td>${note.chiefComplaint || '—'}</td></tr>
         <tr><td>Provisional diagnosis</td><td>${note.provisionalDiagnosis || '—'}</td></tr>
         <tr><td>Doctor remarks</td><td>${note.doctorRemarks || '—'}</td></tr>`
      : ''
    win.document.write(`
      <!DOCTYPE html><html><head><title>OPD Visit ${visit.visitNumber}</title>
      <style>body{font-family:sans-serif;padding:1.5rem;} table{border-collapse:collapse;width:100%;max-width:600px;} th,td{border:1px solid #ccc;padding:0.5rem 1rem;text-align:left;} th{background:#f5f5f5;width:180px;}</style>
      </head><body>
      <h1>OPD Visit Details</h1>
      <table class="table table-striped">
        <tr><th>Visit number</th><td>${visit.visitNumber}</td></tr>
        <tr><th>Token</th><td>${visit.tokenNumber ?? '—'}</td></tr>
        <tr><th>Status</th><td>${visit.visitStatus}</td></tr>
        <tr><th>Patient</th><td>${visit.patientName} (${visit.patientUhid})</td></tr>
        <tr><th>Doctor</th><td>${visit.doctorName} — ${visit.departmentName}</td></tr>
        <tr><th>Visit date</th><td>${visit.visitDate}</td></tr>
        ${noteHtml}
      </table>
      </body></html>
    `)
    win.document.close()
    win.focus()
    setTimeout(() => { win.print(); win.close(); }, 250)
  }

  if (loading || !visit) {
    return (
      <div className={styles.page}>
        <Link to="/opd" className={styles.backLink}>← OPD</Link>
        {loading && <div className={styles.loading}>Loading visit…</div>}
        {error && <div className={styles.error}>{error}</div>}
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <Link to="/opd" className={styles.backLink}>← OPD</Link>
      {error && <div className={styles.error}>{error}</div>}
      {success && <div className={styles.success}>{success}</div>}

      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h2 className={styles.cardTitle}>Visit details</h2>
          <button type="button" onClick={handlePrint} className={styles.printBtn}>Print</button>
        </div>
        <div className={styles.grid}>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Visit number</span>
            <span className={styles.fieldValue}>{visit.visitNumber}</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Token</span>
            <span className={styles.fieldValue}>{visit.tokenNumber ?? '—'}</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Status</span>
            <span className={`${styles.statusBadge} ${statusClass(visit.visitStatus)}`}>
              {visit.visitStatus.replace(/_/g, ' ')}
            </span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Patient</span>
            <span className={styles.fieldValue}>{visit.patientName} ({visit.patientUhid})</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Doctor</span>
            <span className={styles.fieldValue}>{visit.doctorName} — {visit.departmentName}</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Visit date</span>
            <span className={styles.fieldValue}>{visit.visitDate}</span>
          </div>
        </div>

        <div className={styles.actions}>
          <form onSubmit={handleUpdateStatus} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <select
              value={statusForm}
              onChange={(e) => setStatusForm((e.target.value || '') as VisitStatus)}
              className={styles.select}
              style={{ width: 'auto', minWidth: '160px' }}
            >
              <option value="">Update status</option>
              {VISIT_STATUSES.map((s) => (
                <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
              ))}
            </select>
            <button type="submit" className={`${styles.btn} ${styles.btnPrimary}`} disabled={!statusForm || statusSubmitting}>
              {statusSubmitting ? 'Updating…' : 'Update'}
            </button>
          </form>
        </div>
      </div>

      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Clinical notes</h2>
        {visit.clinicalNote && (
          <div style={{ marginBottom: '1rem' }}>
            <p className={styles.fieldLabel}>Chief complaint</p>
            <p className={visit.clinicalNote.chiefComplaint ? styles.noteText : styles.noteEmpty}>
              {visit.clinicalNote.chiefComplaint || '—'}
            </p>
            <p className={styles.fieldLabel}>Provisional diagnosis</p>
            <p className={visit.clinicalNote.provisionalDiagnosis ? styles.noteText : styles.noteEmpty}>
              {visit.clinicalNote.provisionalDiagnosis || '—'}
            </p>
            <p className={styles.fieldLabel}>Doctor remarks</p>
            <p className={visit.clinicalNote.doctorRemarks ? styles.noteText : styles.noteEmpty}>
              {visit.clinicalNote.doctorRemarks || '—'}
            </p>
          </div>
        )}
        <form onSubmit={handleSaveNotes}>
          <div className={styles.formRow}>
            <label>Chief complaint</label>
            <textarea
              className={styles.textarea}
              value={notesForm.chiefComplaint ?? ''}
              onChange={(e) => setNotesForm((p) => ({ ...p, chiefComplaint: e.target.value }))}
              placeholder="Chief complaint"
              rows={2}
            />
          </div>
          <div className={styles.formRow}>
            <label>Provisional diagnosis</label>
            <input
              className={styles.input}
              value={notesForm.provisionalDiagnosis ?? ''}
              onChange={(e) => setNotesForm((p) => ({ ...p, provisionalDiagnosis: e.target.value }))}
              placeholder="Provisional diagnosis"
            />
          </div>
          <div className={styles.formRow}>
            <label>Doctor remarks</label>
            <textarea
              className={styles.textarea}
              value={notesForm.doctorRemarks ?? ''}
              onChange={(e) => setNotesForm((p) => ({ ...p, doctorRemarks: e.target.value }))}
              placeholder="Remarks"
              rows={3}
            />
          </div>
          <button type="submit" className={`${styles.btn} ${styles.btnPrimary}`} disabled={notesSubmitting}>
            {notesSubmitting ? 'Saving…' : 'Save notes'}
          </button>
        </form>
      </div>

      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Refer visit</h2>
        <form onSubmit={handleRefer}>
          <div className={styles.formRow}>
            <label>Refer to department</label>
            <select
              className={styles.select}
              value={referForm.referredToDepartmentId ?? ''}
              onChange={(e) => setReferForm((p) => ({
                ...p,
                referredToDepartmentId: e.target.value ? Number(e.target.value) : undefined,
                referredToDoctorId: undefined,
              }))}
            >
              <option value="">—</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name}</option>
              ))}
            </select>
          </div>
          <div className={styles.formRow}>
            <label>Refer to doctor</label>
            <select
              className={styles.select}
              value={referForm.referredToDoctorId ?? ''}
              onChange={(e) => setReferForm((p) => ({
                ...p,
                referredToDoctorId: e.target.value ? Number(e.target.value) : undefined,
              }))}
            >
              <option value="">—</option>
              {doctors
                .filter((d) => !referForm.referredToDepartmentId || d.departmentId === referForm.referredToDepartmentId)
                .map((d) => (
                  <option key={d.id} value={d.id}>{d.fullName} — {d.departmentName}</option>
                ))}
            </select>
          </div>
          <div className={styles.checkboxRow}>
            <input
              type="checkbox"
              id="referToIpd"
              checked={referForm.referToIpd ?? false}
              onChange={(e) => setReferForm((p) => ({ ...p, referToIpd: e.target.checked }))}
            />
            <label htmlFor="referToIpd">Refer to IPD (admission request)</label>
          </div>
          <div className={styles.formRow}>
            <label>Referral remarks</label>
            <textarea
              className={styles.textarea}
              value={referForm.referralRemarks ?? ''}
              onChange={(e) => setReferForm((p) => ({ ...p, referralRemarks: e.target.value }))}
              placeholder="Remarks"
              rows={2}
            />
          </div>
          <button type="submit" className={`${styles.btn} ${styles.btnPrimary}`} disabled={referSubmitting}>
            {referSubmitting ? 'Submitting…' : 'Submit referral'}
          </button>
        </form>
      </div>
    </div>
  )
}
