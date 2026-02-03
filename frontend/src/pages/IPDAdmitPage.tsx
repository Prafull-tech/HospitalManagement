import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useNavigate } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { ipdApi } from '../api/ipd'
import { PatientSearch } from '../components/reception/PatientSearch'
import type { IPDAdmissionRequest, AdmissionType, BedAvailabilityResponse, WardType } from '../types/ipd'
import type { DoctorResponse } from '../types/doctor'
import type { PatientResponse } from '../types/patient'
import styles from './IPDAdmitPage.module.css'

const ADMISSION_TYPES: { value: AdmissionType; label: string }[] = [
  { value: 'OPD_REFERRAL', label: 'OPD Referral' },
  { value: 'EMERGENCY', label: 'Emergency' },
  { value: 'DIRECT', label: 'Direct Admission' },
]

const WARD_TYPES: { value: WardType; label: string }[] = [
  { value: 'GENERAL', label: 'General' },
  { value: 'SEMI_PRIVATE', label: 'Semi-Private' },
  { value: 'PRIVATE', label: 'Private' },
  { value: 'ICU', label: 'ICU' },
  { value: 'EMERGENCY', label: 'Emergency' },
]

function toIsoDateTimeLocal(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function IPDAdmitPage() {
  const navigate = useNavigate()
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [beds, setBeds] = useState<BedAvailabilityResponse[]>([])
  const [form, setForm] = useState<IPDAdmissionRequest & { wardType?: WardType }>({
    patientUhid: '',
    primaryDoctorId: 0,
    admissionType: 'DIRECT',
    bedId: 0,
    wardType: undefined,
    admissionDateTime: toIsoDateTimeLocal(new Date()),
    diagnosis: '',
    remarks: '',
    depositAmount: undefined,
    insuranceTpa: '',
    admissionFormDocumentRef: '',
    consentFormDocumentRef: '',
    idProofDocumentRef: '',
  })
  const [patientName, setPatientName] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [admissionId, setAdmissionId] = useState<number | null>(null)
  const [admissionNumber, setAdmissionNumber] = useState<string | null>(null)

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((d) => setDoctors(d.content)).catch(() => [])
  }, [])

  useEffect(() => {
    if (!form.wardType) {
      setBeds([])
      return
    }
    ipdApi
      .getHospitalBeds({ wardType: form.wardType, vacantOnly: true })
      .then(setBeds)
      .catch(() => setBeds([]))
  }, [form.wardType])

  const handlePatientSelect = (patient: PatientResponse | null) => {
    if (patient) {
      setForm((prev) => ({ ...prev, patientUhid: patient.uhid }))
      setPatientName(patient.fullName)
    } else {
      setForm((prev) => ({ ...prev, patientUhid: '' }))
      setPatientName(null)
    }
    setError('')
  }

  const selectableBeds = beds.filter((b) => b.selectableForAdmission ?? b.available)
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setForm((prev) => {
      const next = { ...prev, [name]: value }
      if (name === 'wardType') {
        next.bedId = 0
      }
      if (name === 'primaryDoctorId' || name === 'bedId') {
        next[name] = Number(value) || 0
      }
      if (name === 'depositAmount') {
        const n = value === '' ? undefined : Number(value)
        next.depositAmount = n != null && !Number.isNaN(n) ? n : undefined
      }
      return next
    })
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setAdmissionId(null)
    setAdmissionNumber(null)
    if (!form.patientUhid?.trim()) {
      setError('Patient UHID is required.')
      return
    }
    if (!form.primaryDoctorId) {
      setError('Please select primary doctor.')
      return
    }
    if (!form.wardType) {
      setError('Please select ward type.')
      return
    }
    if (!form.bedId) {
      setError('Please select a bed.')
      return
    }
    if (!form.admissionDateTime?.trim()) {
      setError('Admission date & time is required.')
      return
    }
    if (!form.diagnosis?.trim()) {
      setError('Diagnosis is required.')
      return
    }
    setLoading(true)
    try {
      const created = await ipdApi.admit({
        patientUhid: form.patientUhid.trim(),
        primaryDoctorId: form.primaryDoctorId,
        admissionType: form.admissionType,
        bedId: form.bedId,
        admissionDateTime: form.admissionDateTime.trim(),
        diagnosis: form.diagnosis.trim(),
        opdVisitId: form.opdVisitId || undefined,
        remarks: form.remarks?.trim() || undefined,
        depositAmount: form.depositAmount,
        insuranceTpa: form.insuranceTpa?.trim() || undefined,
        admissionFormDocumentRef: form.admissionFormDocumentRef?.trim() || undefined,
        consentFormDocumentRef: form.consentFormDocumentRef?.trim() || undefined,
        idProofDocumentRef: form.idProofDocumentRef?.trim() || undefined,
      })
      setAdmissionId(created.id)
      setAdmissionNumber(created.admissionNumber)
      setForm((prev) => ({
        ...prev,
        patientUhid: '',
        primaryDoctorId: 0,
        bedId: 0,
        diagnosis: '',
        admissionDateTime: toIsoDateTimeLocal(new Date()),
      }))
      setPatientName(null)
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
        <h2 className={styles.title}>IPD Admit Patient</h2>
        {error && <div className={styles.error}>{error}</div>}
        {admissionId && admissionNumber && (
          <div className={styles.success}>
            Admitted. IPD Admission Number: <strong>{admissionNumber}</strong>
            <button type="button" onClick={() => navigate(`/ipd/admissions/${admissionId}`)}>
              View admission
            </button>
          </div>
        )}

        <div className={styles.row}>
          <PatientSearch
            label="UHID"
            value={form.patientUhid}
            displayName={patientName}
            onSelect={handlePatientSelect}
            placeholder="Search by UHID, ID, mobile or name"
            required
          />
          <span className={styles.hint}>
            <Link to="/reception/search">Full patient search</Link>
          </span>
        </div>

        <div className={styles.row}>
          <label>
            Primary doctor <span className={styles.required}>*</span>
            <select
              name="primaryDoctorId"
              value={form.primaryDoctorId || ''}
              onChange={handleChange}
              className={styles.select}
              required
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
            Ward type <span className={styles.required}>*</span>
            <select
              name="wardType"
              value={form.wardType ?? ''}
              onChange={handleChange}
              className={styles.select}
              required
            >
              <option value="">Select ward type</option>
              {WARD_TYPES.map((w) => (
                <option key={w.value} value={w.value}>
                  {w.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className={styles.row}>
          <label>
            Bed number <span className={styles.required}>*</span>
            <select name="bedId" value={form.bedId || ''} onChange={handleChange} className={styles.select} required>
              <option value="">Select bed (VACANT only)</option>
              {selectableBeds.map((b) => (
                <option key={b.bedId} value={b.bedId}>
                  {b.wardName} — {b.bedNumber}
                </option>
              ))}
            </select>
            {form.wardType && selectableBeds.length === 0 && !loading && (
              <span className={styles.hint}>No VACANT beds in this ward type.</span>
            )}
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

        <div className={styles.row}>
          <label>
            Admission date & time <span className={styles.required}>*</span>
            <input
              name="admissionDateTime"
              type="datetime-local"
              value={form.admissionDateTime ?? ''}
              onChange={handleChange}
              className={styles.input}
              required
            />
          </label>
        </div>

        <div className={styles.row}>
          <label>
            Diagnosis <span className={styles.required}>*</span>
            <textarea
              name="diagnosis"
              value={form.diagnosis ?? ''}
              onChange={handleChange}
              placeholder="Enter diagnosis"
              className={styles.textarea}
              rows={3}
              required
            />
          </label>
        </div>

        <div className={styles.row}>
          <label>
            Deposit amount (optional)
            <input
              name="depositAmount"
              type="number"
              min={0}
              step={0.01}
              value={form.depositAmount ?? ''}
              onChange={handleChange}
              placeholder="0.00"
              className={styles.input}
            />
          </label>
        </div>

        <div className={styles.row}>
          <label>
            Insurance / TPA (optional)
            <input
              name="insuranceTpa"
              type="text"
              value={form.insuranceTpa ?? ''}
              onChange={handleChange}
              placeholder="Insurance or TPA name"
              className={styles.input}
            />
          </label>
        </div>

        <div className={styles.sectionLabel}>Documents (ref or ID)</div>
        <div className={styles.row}>
          <label>
            Admission form
            <input
              name="admissionFormDocumentRef"
              type="text"
              value={form.admissionFormDocumentRef ?? ''}
              onChange={handleChange}
              placeholder="Document ref or ID"
              className={styles.input}
            />
          </label>
        </div>
        <div className={styles.row}>
          <label>
            Consent form
            <input
              name="consentFormDocumentRef"
              type="text"
              value={form.consentFormDocumentRef ?? ''}
              onChange={handleChange}
              placeholder="Document ref or ID"
              className={styles.input}
            />
          </label>
        </div>
        <div className={styles.row}>
          <label>
            ID proof
            <input
              name="idProofDocumentRef"
              type="text"
              value={form.idProofDocumentRef ?? ''}
              onChange={handleChange}
              placeholder="Document ref or ID"
              className={styles.input}
            />
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
          <button
            type="submit"
            className={styles.submitBtn}
            disabled={loading || selectableBeds.length === 0 || !form.wardType}
          >
            {loading ? 'Admitting…' : 'Admit Patient'}
          </button>
          {loading && <span className={styles.loading}>Please wait…</span>}
        </div>
      </form>
    </div>
  )
}
