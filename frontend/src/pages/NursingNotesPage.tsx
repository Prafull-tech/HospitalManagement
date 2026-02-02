import { useState, useEffect, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { ipdApi } from '../api/ipd'
import { nursingApi } from '../api/nursing'
import type { NursingNoteRequest, NursingNoteResponse, ShiftType, WardTypeNursing } from '../types/nursing'
import type { IPDAdmissionResponse, WardResponse } from '../types/ipd'
import type { NursingStaffResponse } from '../types/nursing'
import styles from './NursingNotesPage.module.css'

const NOTE_TYPES = [
  { value: 'GENERAL_OBSERVATION', label: 'General observation' },
  { value: 'VITALS_SUMMARY', label: 'Vitals summary' },
  { value: 'MEDICATION_REFERENCE', label: 'Medication administration (reference)' },
  { value: 'SPECIAL_INSTRUCTIONS', label: 'Special instructions' },
  { value: 'SHIFT_NOTE', label: 'Shift note' },
  { value: 'CARE_PLAN', label: 'Care plan' },
]

const SHIFT_OPTIONS: { value: ShiftType; label: string }[] = [
  { value: 'MORNING', label: 'Morning (06–14)' },
  { value: 'EVENING', label: 'Evening (14–22)' },
  { value: 'NIGHT', label: 'Night (22–06)' },
]

type WardTab = 'ALL' | 'GENERAL' | 'PRIVATE' | 'ICU'

const ICU_WARD_TYPES: WardTypeNursing[] = ['ICU', 'CCU', 'NICU', 'HDU']

function getCurrentShift(): ShiftType {
  const hour = new Date().getHours()
  if (hour >= 6 && hour < 14) return 'MORNING'
  if (hour >= 14 && hour < 22) return 'EVENING'
  return 'NIGHT'
}

function mapWardTypeToTab(wardType: string | undefined): WardTab | null {
  if (!wardType) return null
  const u = wardType.toUpperCase()
  if (u === 'GENERAL') return 'GENERAL'
  if (u === 'PRIVATE' || u === 'SEMI_PRIVATE') return 'PRIVATE'
  if (['ICU', 'CCU', 'NICU', 'HDU'].includes(u)) return 'ICU'
  return null
}

export function NursingNotesPage() {
  const [wards, setWards] = useState<WardResponse[]>([])
  const [admissions, setAdmissions] = useState<IPDAdmissionResponse[]>([])
  const [staff, setStaff] = useState<NursingStaffResponse[]>([])
  const [wardTab, setWardTab] = useState<WardTab>('ALL')
  const [patientNameFilter, setPatientNameFilter] = useState('')
  const [selectedAdmissionId, setSelectedAdmissionId] = useState<number | null>(null)
  const [notes, setNotes] = useState<NursingNoteResponse[]>([])
  const [editingNoteId, setEditingNoteId] = useState<number | null>(null)
  const [form, setForm] = useState<NursingNoteRequest>({
    ipdAdmissionId: 0,
    shiftType: getCurrentShift(),
    noteType: 'GENERAL_OBSERVATION',
    content: '',
    recordedById: undefined,
    criticalFlags: '',
  })
  const [loading, setLoading] = useState(false)
  const [loadingList, setLoadingList] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const wardIdToType = useMemo(() => {
    const m: Record<number, string> = {}
    wards.forEach((w) => {
      m[w.id] = w.wardType
    })
    return m
  }, [wards])

  const admissionsByWard = useMemo(() => {
    return admissions.filter((a) => {
      const wType = a.currentWardId != null ? wardIdToType[a.currentWardId] : undefined
      const tab = mapWardTypeToTab(wType)
      if (wardTab === 'ALL') return true
      if (!tab) return false
      return tab === wardTab
    })
  }, [admissions, wardTab, wardIdToType])

  const filteredPatients = useMemo(() => {
    const q = patientNameFilter.trim().toLowerCase()
    if (!q) return admissionsByWard
    return admissionsByWard.filter(
      (a) =>
        a.patientName?.toLowerCase().includes(q) ||
        a.patientUhid?.toLowerCase().includes(q) ||
        a.admissionNumber?.toLowerCase().includes(q)
    )
  }, [admissionsByWard, patientNameFilter])

  useEffect(() => {
    ipdApi.listWards().then(setWards).catch(() => setWards([]))
    ipdApi
      .search({ page: 0, size: 500 })
      .then((r) =>
        setAdmissions(
          r.content.filter((a) =>
            ['ADMITTED', 'TRANSFERRED', 'DISCHARGE_INITIATED'].includes(a.admissionStatus)
          )
        )
      )
      .catch(() => setAdmissions([]))
    nursingApi.listStaff(true).then(setStaff).catch(() => setStaff([]))
  }, [])

  useEffect(() => {
    if (!selectedAdmissionId) {
      setNotes([])
      setEditingNoteId(null)
      return
    }
    setLoadingList(true)
    nursingApi
      .getNotesByAdmission(selectedAdmissionId)
      .then(setNotes)
      .catch(() => setNotes([]))
      .finally(() => setLoadingList(false))
  }, [selectedAdmissionId])

  useEffect(() => {
    if (selectedAdmissionId) {
      setForm((prev) => ({
        ...prev,
        ipdAdmissionId: selectedAdmissionId,
      }))
    }
  }, [selectedAdmissionId])

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement | HTMLTextAreaElement | HTMLInputElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'recordedById' ? (value ? Number(value) : undefined) : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.ipdAdmissionId || !form.content?.trim()) {
      setError('Select patient and enter content.')
      return
    }
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      if (editingNoteId) {
        await nursingApi.updateNote(editingNoteId, form)
        setSuccess('Note updated.')
        setEditingNoteId(null)
      } else {
        await nursingApi.createNote(form)
        setSuccess('Note saved.')
      }
      setForm((prev) => ({ ...prev, content: '' }))
      if (form.ipdAdmissionId)
        nursingApi.getNotesByAdmission(form.ipdAdmissionId).then(setNotes).catch(() => {})
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to save note.')
    } finally {
      setLoading(false)
    }
  }

  const startEdit = (n: NursingNoteResponse) => {
    if (n.noteStatus !== 'DRAFT') return
    setEditingNoteId(n.id)
    setForm({
      ipdAdmissionId: n.ipdAdmissionId,
      shiftType: n.shiftType,
      noteType: n.noteType,
      content: n.content,
      recordedById: n.recordedById,
      criticalFlags: n.criticalFlags ?? '',
    })
  }

  const cancelEdit = () => {
    setEditingNoteId(null)
    setForm((prev) => ({
      ...prev,
      content: '',
      noteType: 'GENERAL_OBSERVATION',
      shiftType: getCurrentShift(),
      criticalFlags: '',
    }))
  }

  const handleLock = async (id: number) => {
    try {
      await nursingApi.lockNote(id)
      setSuccess('Note locked.')
      if (selectedAdmissionId) nursingApi.getNotesByAdmission(selectedAdmissionId).then(setNotes).catch(() => {})
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to lock note.')
    }
  }

  const selectedAdmission = admissions.find((a) => a.id === selectedAdmissionId)
  const isIcuWard = selectedAdmission?.currentWardId != null && ICU_WARD_TYPES.includes(wardIdToType[selectedAdmission.currentWardId] as WardTypeNursing)

  return (
    <div className={styles.page}>
      <div className={styles.headerRow}>
        <h2 className={styles.cardTitle}>Nursing Notes</h2>
        <Link to="/nursing/notes/search" className={styles.searchLink}>
          Search notes →
        </Link>
      </div>

      <div className={styles.card}>
        <div className={styles.tabs}>
          {(['ALL', 'GENERAL', 'PRIVATE', 'ICU'] as WardTab[]).map((tab) => (
            <button
              key={tab}
              type="button"
              className={`${styles.tab} ${wardTab === tab ? styles.tabActive : ''}`}
              onClick={() => setWardTab(tab)}
            >
              {tab === 'ALL' ? 'All wards' : tab === 'ICU' ? 'ICU / CCU / NICU / HDU' : tab}
            </button>
          ))}
        </div>

        <div className={styles.twoCol}>
          <div>
            <input
              type="text"
              className={styles.patientSearch}
              placeholder="Search by patient name, UHID…"
              value={patientNameFilter}
              onChange={(e) => setPatientNameFilter(e.target.value)}
              aria-label="Search patients"
            />
            <div className={styles.patientList}>
              {filteredPatients.length === 0 && (
                <p className={styles.empty}>No patients in this ward.</p>
              )}
              {filteredPatients.map((a) => (
                <div
                  key={a.id}
                  role="button"
                  tabIndex={0}
                  className={`${styles.patientRow} ${selectedAdmissionId === a.id ? styles.patientRowSelected : ''}`}
                  onClick={() => setSelectedAdmissionId(a.id)}
                  onKeyDown={(e) => e.key === 'Enter' && setSelectedAdmissionId(a.id)}
                >
                  <div className={styles.patientInfo}>
                    <span className={styles.patientName}>{a.patientName}</span>
                    <span className={styles.patientMeta}>
                      {a.admissionNumber} · {a.patientUhid} · {a.currentWardName ?? '—'} / {a.currentBedNumber ?? '—'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div>
            {!selectedAdmissionId && (
              <p className={styles.empty}>Select a patient to view or add notes.</p>
            )}
            {selectedAdmissionId && selectedAdmission && (
              <>
                {error && <div className={styles.error}>{error}</div>}
                {success && <div className={styles.success}>{success}</div>}
                <form onSubmit={handleSubmit} className={styles.form}>
                  {editingNoteId ? (
                    <p className={styles.editHeading}>Editing note (DRAFT)</p>
                  ) : (
                    <p className={styles.editHeading}>
                      New note — {selectedAdmission.patientName} · {selectedAdmission.currentWardName} / {selectedAdmission.currentBedNumber}
                    </p>
                  )}
                  <div className={styles.row}>
                    <label>Shift</label>
                    <select name="shiftType" value={form.shiftType} onChange={handleChange} className={styles.select}>
                      {SHIFT_OPTIONS.map((s) => (
                        <option key={s.value} value={s.value}>{s.label}</option>
                      ))}
                    </select>
                  </div>
                  <div className={styles.row}>
                    <label>Note type</label>
                    <select name="noteType" value={form.noteType} onChange={handleChange} className={styles.select}>
                      {NOTE_TYPES.map((t) => (
                        <option key={t.value} value={t.value}>{t.label}</option>
                      ))}
                    </select>
                  </div>
                  <div className={styles.row}>
                    <label><span>Content <span className={styles.required}>*</span></span></label>
                    <textarea name="content" value={form.content} onChange={handleChange} placeholder="Shift notes, care plan remarks…" className={styles.textarea} rows={5} required />
                  </div>
                  {isIcuWard && (
                    <div className={styles.row}>
                      <label>Critical flags (e.g. Ventilator, Infusion, Oxygen)</label>
                      <input name="criticalFlags" value={form.criticalFlags ?? ''} onChange={handleChange} placeholder="Comma-separated" className={styles.input} />
                    </div>
                  )}
                  <div className={styles.row}>
                    <label>Recorded by</label>
                    <select name="recordedById" value={form.recordedById ?? ''} onChange={handleChange} className={styles.select}>
                      <option value="">—</option>
                      {staff.map((s) => (
                        <option key={s.id} value={s.id}>{s.fullName}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <button type="submit" className={styles.submitBtn} disabled={loading}>
                      {loading ? 'Saving…' : editingNoteId ? 'Update note' : 'Save note'}
                    </button>
                    {editingNoteId && (
                      <button type="button" className={styles.secondaryBtn} onClick={cancelEdit}>
                        Cancel
                      </button>
                    )}
                  </div>
                </form>

                <h3 className={styles.cardTitle} style={{ marginTop: '1.5rem' }}>Notes timeline</h3>
                {loadingList && <div className={styles.loading}>Loading…</div>}
                {!loadingList && notes.length === 0 && <p className={styles.empty}>No notes yet.</p>}
                {!loadingList && notes.length > 0 && (
                  <div className={styles.timeline}>
                    {notes.map((n) => (
                      <div
                        key={n.id}
                        className={`${styles.timelineItem} ${n.noteStatus === 'LOCKED' ? styles.noteLocked : ''} ${n.wardType === 'ICU' || n.wardType === 'CCU' || n.wardType === 'NICU' || n.wardType === 'HDU' ? styles.noteIcu : ''}`}
                      >
                        <div className={styles.timelineMeta}>
                          <span className={styles.shiftBadge}>{n.shiftType}</span>
                          <span className={styles.noteType}>{n.noteType.replace(/_/g, ' ')}</span>
                          <span className={styles.noteTime}>{n.recordedAt.replace('T', ' ').slice(0, 16)}</span>
                          {n.recordedByName && <span> — {n.recordedByName}</span>}
                          {n.noteStatus === 'LOCKED' && <span className={styles.lockedBadge}>Locked</span>}
                          {n.criticalFlags && <span className={styles.criticalFlags}>{n.criticalFlags}</span>}
                        </div>
                        <div className={styles.timelineContent}>{n.content}</div>
                        <div className={styles.noteActions}>
                          {n.noteStatus === 'DRAFT' && (
                            <button type="button" className={styles.secondaryBtn} onClick={() => startEdit(n)}>
                              Edit
                            </button>
                          )}
                          {n.noteStatus === 'DRAFT' && (
                            <button type="button" className={styles.secondaryBtn} onClick={() => handleLock(n.id)}>
                              Lock
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                <div className={styles.cardHeader} style={{ marginTop: '1rem' }}>
                  <Link
                    to={`/nursing/notes/print?ipdAdmissionId=${selectedAdmissionId}`}
                    className={styles.printBtn}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Print notes
                  </Link>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
