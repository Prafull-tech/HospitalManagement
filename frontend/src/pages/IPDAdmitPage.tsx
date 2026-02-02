import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useNavigate } from 'react-router-dom'
import { receptionApi } from '../api/reception'
import { doctorsApi } from '../api/doctors'
import { ipdApi } from '../api/ipd'
import type { IPDAdmissionRequest, AdmissionType, BedAvailabilityResponse } from '../types/ipd'
import type { DoctorResponse } from '../types/doctor'
import styles from './IPDAdmitPage.module.css'

const ADMISSION_TYPES: { value: AdmissionType; label: string }[] = [
  { value: 'OPD_REFERRAL', label: 'OPD Referral' },
  { value: 'EMERGENCY', label: 'Emergency' },
  { value: 'DIRECT', label: 'Direct Admission' },
]

export function IPDAdmitPage() {
  const [searchParams] = useSearchParams()
  const opdVisitIdParam = searchParams.get('opdVisitId')
  const navigate = useNavigate()

  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [beds, setBeds] = useState<BedAvailabilityResponse[]>([])
  const [form, setForm] = useState<IPDAdmissionRequest>({
    patientUhid: '',
    primaryDoctorId: 0,
    admissionType: 'DIRECT',
    bedId: 0,
    opdVisitId: opdVisitIdParam ? Number(opdVisitIdParam) : undefined,
    remarks: '',
  })
  const [patientName, setPatientName] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [admissionId, setAdmissionId] = useState<number | null>(null)

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((d) => setDoctors(d.content)).catch(() => [])
    ipdApi.getBedAvailability().then(setBeds).catch(() => setBeds([]))
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

  const availableBeds = beds.filter((b) => b.available)
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'primaryDoctorId' || name === 'bedId' ? Number(value) || 0 : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setAdmissionId(null)
    if (!form.patientUhid?.trim()) {
      setError('Patient UHID is required.')
      return
    }
    if (!form.primaryDoctorId) {
      setError('Please select primary doctor.')
      return
    }
    if (!form.bedId) {
      setError('Please select a bed.')
      return
    }
    setLoading(true)
    try {
      const created = await ipdApi.admit({
        patientUhid: form.patientUhid.trim(),
        primaryDoctorId: form.primaryDoctorId,
        admissionType: form.admissionType,
        bedId: form.bedId,
        opdVisitId: form.opdVisitId || undefined,
        remarks: form.remarks?.trim() || undefined,
      })
      setAdmissionId(created.id)
      setForm((prev) => ({ ...prev, patientUhid: '', primaryDoctorId: 0, bedId: 0 }))
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string; errors?: Record<string, string> } } }
      const data = ax.response?.data
      if (data?.errors) {
        setError(Object.entries(data.errors).map(([k, v]) => `${k}: ${v}`).join('. '))
      } else {
        setError(data?.message || 'Failed to admit patient.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <form onSubmit={handleSubmit} className={styles.form}>
        {error && <div className={styles.error}>{error}</div>}
        {admissionId && (
          <div className={styles.success}>
            Patient admitted.
            <button type="button" onClick={() => navigate(`/ipd/admissions/${admissionId}`)}>
              View admission
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
            <span className={styles.hint}>
              {patientName ? `Patient: ${patientName}` : 'Enter UHID from Reception. '}
              <Link to="/reception/search">Search patient</Link>
            </span>
          </label>
        </div>
        <div className={styles.row}>
          <label>
            Admission type <span className={styles.required}>*</span>
            <select name="admissionType" value={form.admissionType} onChange={handleChange} className={styles.select}>
              {ADMISSION_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </label>
        </div>
        {form.admissionType === 'OPD_REFERRAL' && (
          <div className={styles.row}>
            <label>
              <span>OPD Visit ID (optional)</span>
              <input
                name="opdVisitId"
                type="number"
                value={form.opdVisitId ?? ''}
                onChange={handleChange}
                placeholder="Link to OPD visit"
                className={styles.input}
              />
            </label>
          </div>
        )}
        <div className={styles.row}>
          <label>
            <span>Primary doctor <span className={styles.required}>*</span></span>
            <select
              name="primaryDoctorId"
              value={form.primaryDoctorId || ''}
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
            <span>Bed <span className={styles.required}>*</span></span>
            <select name="bedId" value={form.bedId || ''} onChange={handleChange} className={styles.select}>
              <option value="">Select bed</option>
              {availableBeds.map((b) => (
                <option key={b.bedId} value={b.bedId}>
                  {b.wardName} — {b.bedNumber}
                </option>
              ))}
            </select>
            {availableBeds.length === 0 && !loading && (
              <span className={styles.hint}>No available beds. Add wards/beds or discharge a patient.</span>
            )}
          </label>
        </div>
        <div className={styles.row}>
          <label>
            Remarks
            <textarea
              name="remarks"
              value={form.remarks ?? ''}
              onChange={handleChange}
              placeholder="Optional remarks"
              className={styles.textarea}
              rows={2}
            />
          </label>
        </div>
        <div className={styles.submitRow}>
          <button type="submit" className={styles.submitBtn} disabled={loading || availableBeds.length === 0}>
            {loading ? 'Admitting…' : 'Admit Patient'}
          </button>
          {loading && <span className={styles.loading}>Please wait…</span>}
        </div>
      </form>
    </div>
  )
}
