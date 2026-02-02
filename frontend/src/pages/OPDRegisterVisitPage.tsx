import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { receptionApi } from '../api/reception'
import { doctorsApi } from '../api/doctors'
import { opdApi } from '../api/opd'
import type { OPDVisitRequest } from '../types/opd'
import type { DoctorResponse } from '../types/doctor'
import styles from './OPDRegisterVisitPage.module.css'

const today = new Date().toISOString().slice(0, 10)

export function OPDRegisterVisitPage() {
  const navigate = useNavigate()
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [form, setForm] = useState<OPDVisitRequest>({
    patientUhid: '',
    doctorId: 0,
    visitDate: today,
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [patientName, setPatientName] = useState<string | null>(null)
  const [visitId, setVisitId] = useState<number | null>(null)

  useEffect(() => {
    doctorsApi
      .list({ status: 'ACTIVE', page: 0, size: 200 })
      .then((data) => setDoctors(data.content))
      .catch(() => setDoctors([]))
  }, [])

  useEffect(() => {
    if (!form.patientUhid?.trim()) {
      setPatientName(null)
      return
    }
    receptionApi
      .getByUhid(form.patientUhid.trim())
      .then((p) => setPatientName(p.fullName))
      .catch(() => setPatientName(null))
  }, [form.patientUhid])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'doctorId' ? Number(value) : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setVisitId(null)
    if (!form.patientUhid?.trim()) {
      setError('Patient UHID is required.')
      return
    }
    if (!form.doctorId) {
      setError('Please select a doctor.')
      return
    }
    if (!form.visitDate) {
      setError('Visit date is required.')
      return
    }
    setLoading(true)
    try {
      const created = await opdApi.register({
        patientUhid: form.patientUhid.trim(),
        doctorId: form.doctorId,
        visitDate: form.visitDate,
      })
      setVisitId(created.id)
      setForm((prev) => ({ ...prev, patientUhid: '', doctorId: 0 }))
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string; errors?: Record<string, string> } } }
      const data = ax.response?.data
      if (data?.errors) {
        setError(Object.entries(data.errors).map(([k, v]) => `${k}: ${v}`).join('. '))
      } else {
        setError(data?.message || 'Failed to register OPD visit.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <form onSubmit={handleSubmit} className={styles.form}>
        {error && <div className={styles.error}>{error}</div>}
        {visitId && (
          <div className={styles.success}>
            OPD visit registered.
            <button type="button" onClick={() => navigate(`/opd/visits/${visitId}`)}>
              Open visit
            </button>
          </div>
        )}
        <div className={styles.row}>
          <label>
            <span>Patient UHID <span className={styles.required}>*</span></span>
            <input
              name="patientUhid"
              value={form.patientUhid}
              onChange={handleChange}
              placeholder="e.g. HMS-2025-000001"
              className={styles.input}
            />
            <span className={styles.patientHint}>
              {patientName ? `Patient: ${patientName}` : 'Enter UHID from Reception. '}
              <Link to="/reception/search" className={styles.link}>Search patient</Link>
            </span>
          </label>
        </div>
        <div className={styles.row}>
          <label>
            Doctor <span className={styles.required}>*</span>
            <select
              name="doctorId"
              value={form.doctorId || ''}
              onChange={handleChange}
              className={styles.select}
            >
              <option value="">Select doctor</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.fullName} — {d.departmentName} ({d.code})
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className={styles.row}>
          <label>
            <span>Visit date <span className={styles.required}>*</span></span>
            <input
              name="visitDate"
              type="date"
              value={form.visitDate}
              onChange={handleChange}
              className={styles.input}
            />
          </label>
        </div>
        <div className={styles.submitRow}>
          <button type="submit" className={styles.submitBtn} disabled={loading}>
            {loading ? 'Registering…' : 'Register OPD Visit'}
          </button>
          {loading && <span className={styles.loading}>Please wait…</span>}
        </div>
      </form>
    </div>
  )
}
