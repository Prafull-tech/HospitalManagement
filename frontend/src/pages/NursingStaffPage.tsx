import { useState, useEffect } from 'react'
import { nursingApi } from '../api/nursing'
import type { NursingStaffResponse, NursingStaffRequest, NurseRole } from '../types/nursing'
import styles from './NursingStaffPage.module.css'

const NURSE_ROLES: { value: NurseRole; label: string }[] = [
  { value: 'CHIEF_NURSING_OFFICER', label: 'Chief Nursing Officer' },
  { value: 'NURSING_SUPERINTENDENT', label: 'Nursing Superintendent' },
  { value: 'WARD_INCHARGE', label: 'Ward Incharge' },
  { value: 'STAFF_NURSE', label: 'Staff Nurse' },
  { value: 'NURSING_AIDE', label: 'Nursing Aide' },
]

export function NursingStaffPage() {
  const [list, setList] = useState<NursingStaffResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<NursingStaffRequest>({
    code: '',
    fullName: '',
    nurseRole: 'STAFF_NURSE',
    phone: '',
    email: '',
    isActive: true,
  })
  const [submitting, setSubmitting] = useState(false)
  const [success, setSuccess] = useState('')

  useEffect(() => {
    nursingApi
      .listStaff(true)
      .then(setList)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load staff')
        setList([])
      })
      .finally(() => setLoading(false))
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]:
        name === 'nurseRole'
          ? value
          : name === 'isActive'
            ? (e.target as HTMLInputElement).checked
            : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.code?.trim() || !form.fullName?.trim()) {
      setError('Code and full name are required.')
      return
    }
    setSubmitting(true)
    setError('')
    setSuccess('')
    try {
      await nursingApi.createStaff({
        code: form.code.trim(),
        fullName: form.fullName.trim(),
        nurseRole: form.nurseRole,
        phone: form.phone?.trim() || undefined,
        email: form.email?.trim() || undefined,
        isActive: form.isActive ?? true,
      })
      setSuccess('Staff added.')
      setForm({ code: '', fullName: '', nurseRole: 'STAFF_NURSE', phone: '', email: '', isActive: true })
      setShowForm(false)
      nursingApi.listStaff(true).then(setList).catch(() => {})
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to add staff.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h2 className={styles.cardTitle}>Nursing Staff</h2>
          <button type="button" className={styles.addBtn} onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : 'Add Staff'}
          </button>
        </div>
        {error && <div className={styles.error}>{error}</div>}
        {success && <div className={styles.success}>{success}</div>}
        {showForm && (
          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.row}>
              <label>Code <span className={styles.required}>*</span></label>
              <input name="code" value={form.code} onChange={handleChange} placeholder="e.g. NS001" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label><span>Full name <span className={styles.required}>*</span></span></label>
              <input name="fullName" value={form.fullName} onChange={handleChange} placeholder="Full name" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Role</label>
              <select name="nurseRole" value={form.nurseRole} onChange={handleChange} className={styles.select}>
                {NURSE_ROLES.map((r) => (
                  <option key={r.value} value={r.value}>{r.label}</option>
                ))}
              </select>
            </div>
            <div className={styles.row}>
              <label>Phone</label>
              <input name="phone" value={form.phone ?? ''} onChange={handleChange} placeholder="Phone" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>Email</label>
              <input name="email" type="email" value={form.email ?? ''} onChange={handleChange} placeholder="Email" className={styles.input} />
            </div>
            <div className={styles.row}>
              <label>
                <input name="isActive" type="checkbox" checked={form.isActive ?? true} onChange={handleChange} />
                Active
              </label>
            </div>
            <button type="submit" className={styles.submitBtn} disabled={submitting}>
              {submitting ? 'Adding…' : 'Add Staff'}
            </button>
          </form>
        )}
      </div>
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Staff list</h2>
        {loading && <div className={styles.loading}>Loading…</div>}
        {!loading && list.length === 0 && <p className={styles.empty}>No nursing staff. Add staff above.</p>}
        {!loading && list.length > 0 && (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Name</th>
                  <th>Role</th>
                  <th>Phone</th>
                  <th>Active</th>
                </tr>
              </thead>
              <tbody>
                {list.map((s) => (
                  <tr key={s.id}>
                    <td>{s.code}</td>
                    <td>{s.fullName}</td>
                    <td>{s.nurseRole.replace(/_/g, ' ')}</td>
                    <td>{s.phone ?? '—'}</td>
                    <td>{s.isActive ? 'Yes' : 'No'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
