import { useState, useEffect } from 'react'
import { ipdApi } from '../api/ipd'
import { nursingApi } from '../api/nursing'
import type { NurseAssignmentRequest, ShiftType } from '../types/nursing'
import type { IPDAdmissionResponse } from '../types/ipd'
import type { NursingStaffResponse } from '../types/nursing'
import styles from './NursingAssignPage.module.css'

const SHIFT_TYPES: { value: ShiftType; label: string }[] = [
  { value: 'MORNING', label: 'Morning' },
  { value: 'EVENING', label: 'Evening' },
  { value: 'NIGHT', label: 'Night' },
]

export function NursingAssignPage() {
  const [admissions, setAdmissions] = useState<IPDAdmissionResponse[]>([])
  const [staff, setStaff] = useState<NursingStaffResponse[]>([])
  const [form, setForm] = useState<NurseAssignmentRequest>({
    nursingStaffId: 0,
    ipdAdmissionId: 0,
    shiftType: 'MORNING',
    assignmentDate: new Date().toISOString().slice(0, 10),
    remarks: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    ipdApi
      .search({ page: 0, size: 200 })
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

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement | HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]:
        name === 'nursingStaffId' || name === 'ipdAdmissionId'
          ? Number(value) || 0
          : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.nursingStaffId || !form.ipdAdmissionId) {
      setError('Select nurse and IPD admission.')
      return
    }
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await nursingApi.createAssignment(form)
      setSuccess('Nurse assigned.')
      setForm((prev) => ({ ...prev, nursingStaffId: 0, ipdAdmissionId: 0 }))
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to assign nurse.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Assign nurse to IPD admission</h2>
        {error && <div className={styles.error}>{error}</div>}
        {success && <div className={styles.success}>{success}</div>}
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.row}>
            <label><span>IPD admission <span className={styles.required}>*</span></span></label>
            <select name="ipdAdmissionId" value={form.ipdAdmissionId || ''} onChange={handleChange} className={styles.select}>
              <option value="">Select admission</option>
              {admissions.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.admissionNumber} — {a.patientName} ({a.currentWardName ?? '—'})
                </option>
              ))}
            </select>
          </div>
          <div className={styles.row}>
            <label><span>Nurse <span className={styles.required}>*</span></span></label>
            <select name="nursingStaffId" value={form.nursingStaffId || ''} onChange={handleChange} className={styles.select}>
              <option value="">Select nurse</option>
              {staff.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.fullName} ({s.code}) — {s.nurseRole.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>
          <div className={styles.row}>
            <label>Shift</label>
            <select name="shiftType" value={form.shiftType} onChange={handleChange} className={styles.select}>
              {SHIFT_TYPES.map((t) => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </div>
          <div className={styles.row}>
            <label>Assignment date</label>
            <input name="assignmentDate" type="date" value={form.assignmentDate} onChange={handleChange} className={styles.input} />
          </div>
          <div className={styles.row}>
            <label>Remarks</label>
            <textarea name="remarks" value={form.remarks ?? ''} onChange={handleChange} placeholder="Optional" className={styles.textarea} rows={2} />
          </div>
          <button type="submit" className={styles.submitBtn} disabled={loading}>
            {loading ? 'Assigning…' : 'Assign Nurse'}
          </button>
        </form>
      </div>
    </div>
  )
}
