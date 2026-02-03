import { useState, useEffect } from 'react'
import { ipdApi } from '../api/ipd'
import { nursingApi } from '../api/nursing'
import type { MedicationAdministrationRequest, MedicationAdministrationResponse } from '../types/nursing'
import type { IPDAdmissionResponse } from '../types/ipd'
import type { NursingStaffResponse } from '../types/nursing'
import styles from './NursingMARPage.module.css'

export function NursingMARPage() {
  const [admissions, setAdmissions] = useState<IPDAdmissionResponse[]>([])
  const [staff, setStaff] = useState<NursingStaffResponse[]>([])
  const [admissionId, setAdmissionId] = useState<number | ''>('')
  const [marList, setMarList] = useState<MedicationAdministrationResponse[]>([])
  const [form, setForm] = useState<MedicationAdministrationRequest>({
    ipdAdmissionId: 0,
    medicationName: '',
    dosage: '',
    route: '',
    administeredById: undefined,
    doctorOrderRef: '',
    remarks: '',
  })
  const [loading, setLoading] = useState(false)
  const [loadingList, setLoadingList] = useState(false)
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

  useEffect(() => {
    if (!admissionId) {
      setMarList([])
      return
    }
    setLoadingList(true)
    nursingApi
      .getMedicationsByAdmission(Number(admissionId))
      .then(setMarList)
      .catch(() => setMarList([]))
      .finally(() => setLoadingList(false))
  }, [admissionId])

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      ipdAdmissionId: admissionId ? Number(admissionId) : 0,
    }))
  }, [admissionId])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'administeredById' ? (value ? Number(value) : undefined) : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.ipdAdmissionId || !form.medicationName?.trim()) {
      setError('Select admission and enter medication name.')
      return
    }
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await nursingApi.recordMedication(form)
      setSuccess('Medication recorded.')
      setForm((prev) => ({
        ...prev,
        medicationName: '',
        dosage: '',
        route: '',
        doctorOrderRef: '',
        remarks: '',
      }))
      nursingApi.getMedicationsByAdmission(form.ipdAdmissionId).then(setMarList).catch(() => {})
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to record medication.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Medication Administration Record (MAR)</h2>
        {error && <div className={styles.error}>{error}</div>}
        {success && <div className={styles.success}>{success}</div>}
        <div className={styles.row}>
          <label>IPD admission</label>
          <select value={admissionId || ''} onChange={(e) => setAdmissionId(e.target.value ? Number(e.target.value) : '')} className={styles.select}>
            <option value="">Select admission</option>
            {admissions.map((a) => (
              <option key={a.id} value={a.id}>
                {a.admissionNumber} — {a.patientName}
              </option>
            ))}
          </select>
        </div>
        {admissionId && (
          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.row}>
              <label><span>Medication name <span className={styles.required}>*</span></span></label>
              <input name="medicationName" value={form.medicationName} onChange={handleChange} placeholder="e.g. Paracetamol" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Dosage</label>
              <input name="dosage" value={form.dosage ?? ''} onChange={handleChange} placeholder="e.g. 500mg" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Route</label>
              <input name="route" value={form.route ?? ''} onChange={handleChange} placeholder="e.g. Oral, IV" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Administered by</label>
              <select name="administeredById" value={form.administeredById ?? ''} onChange={handleChange} className={styles.select}>
                <option value="">—</option>
                {staff.map((s) => (
                  <option key={s.id} value={s.id}>{s.fullName}</option>
                ))}
              </select>
            </div>
            <div className={styles.row}>
              <label>Doctor order ref</label>
              <input name="doctorOrderRef" value={form.doctorOrderRef ?? ''} onChange={handleChange} placeholder="Optional" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Remarks</label>
              <textarea name="remarks" value={form.remarks ?? ''} onChange={handleChange} placeholder="Optional" className={styles.textarea} rows={2} />
            </div>
            <button type="submit" className={styles.submitBtn} disabled={loading}>
              {loading ? 'Recording…' : 'Record administration'}
            </button>
          </form>
        )}
      </div>
      {admissionId && (
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>MAR history</h2>
          {loadingList && <div className={styles.loading}>Loading…</div>}
          {!loadingList && marList.length === 0 && <p className={styles.empty}>No medications recorded yet.</p>}
          {!loadingList && marList.length > 0 && (
            <div className={styles.tableWrap}>
              <table className={`table table-striped ${styles.table}`}>
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Medication</th>
                    <th>Dosage</th>
                    <th>Route</th>
                    <th>By</th>
                  </tr>
                </thead>
                <tbody>
                  {marList.map((m) => (
                    <tr key={m.id}>
                      <td>{m.administeredAt.replace('T', ' ').slice(0, 16)}</td>
                      <td>{m.medicationName}</td>
                      <td>{m.dosage ?? '—'}</td>
                      <td>{m.route ?? '—'}</td>
                      <td>{m.administeredByName ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
