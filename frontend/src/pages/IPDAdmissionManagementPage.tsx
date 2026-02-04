import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { ipdApi } from '../api/ipd'
import { receptionApi } from '../api/reception'
import { PatientSearch } from '../components/reception/PatientSearch'
import type { IPDAdmissionRequest, AdmissionType, BedAvailabilityResponse, WardType } from '../types/ipd'
import type { DoctorResponse } from '../types/doctor'
import type { PatientResponse } from '../types/patient'
import type { PatientRequest } from '../types/patient'
import type { ApiError } from '../types/patient'
import styles from './IPDAdmissionManagementPage.module.css'
import patientFormStyles from './PatientRegisterPage.module.css'

const ADMISSION_TYPES: { value: AdmissionType; label: string }[] = [
  { value: 'DIRECT', label: 'New Admission' },
  { value: 'OPD_REFERRAL', label: 'OPD Referral' },
  { value: 'EMERGENCY', label: 'Emergency' },
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

function formatAgeDisplay(patient: PatientResponse): string {
  const age = patient.age
  const yrs = patient.ageYears ?? age
  const months = patient.ageMonths ?? 0
  const days = patient.ageDays ?? 0
  if (months || days) return `${yrs}.${months} yrs`
  return `${age} yrs`
}

type TabId = 'old' | 'new'

export function IPDAdmissionManagementPage() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<TabId>('old')

  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [beds, setBeds] = useState<BedAvailabilityResponse[]>([])
  const [admissionForm, setAdmissionForm] = useState<IPDAdmissionRequest & { wardType?: WardType; referenceDoctorId?: number }>({
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
    referenceDoctorId: undefined,
  })

  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null)
  const [newPatientRegistered, setNewPatientRegistered] = useState<PatientResponse | null>(null)
  const [admissionLoading, setAdmissionLoading] = useState(false)
  const [admissionError, setAdmissionError] = useState('')
  const [admissionSuccess, setAdmissionSuccess] = useState<{ id: number; admissionNumber: string } | null>(null)

  const [patientForm, setPatientForm] = useState<PatientRequest>({
    fullName: '',
    idProofType: '',
    idProofNumber: '',
    dateOfBirth: '',
    age: 0,
    ageYears: undefined,
    ageMonths: undefined,
    ageDays: undefined,
    gender: 'Male',
    weightKg: undefined,
    heightCm: undefined,
    phone: '',
    address: '',
    state: '',
    city: '',
    district: '',
    fatherHusbandName: '',
    referredBy: '',
    referredName: '',
    referredPhone: '',
    consultantName: '',
    specialization: '',
    organisationType: '',
    organisationName: '',
    remarks: '',
  })
  const [patientFormLoading, setPatientFormLoading] = useState(false)
  const [patientFormError, setPatientFormError] = useState('')

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((d) => setDoctors(d.content)).catch(() => [])
  }, [])

  useEffect(() => {
    if (!admissionForm.wardType) {
      setBeds([])
      return
    }
    ipdApi
      .getHospitalBeds({ wardType: admissionForm.wardType, vacantOnly: true })
      .then(setBeds)
      .catch(() => setBeds([]))
  }, [admissionForm.wardType])

  const effectivePatient = activeTab === 'new' && newPatientRegistered ? newPatientRegistered : selectedPatient
  const patientUhid = effectivePatient?.uhid ?? admissionForm.patientUhid

  const handlePatientSelect = (patient: PatientResponse | null) => {
    setSelectedPatient(patient)
    if (patient) {
      setAdmissionForm((prev) => ({ ...prev, patientUhid: patient.uhid }))
    } else {
      setAdmissionForm((prev) => ({ ...prev, patientUhid: '' }))
    }
    setAdmissionError('')
  }

  const selectableBeds = beds.filter((b) => b.selectableForAdmission ?? b.available)

  const handleAdmissionChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setAdmissionForm((prev) => {
      const next = { ...prev, [name]: value }
      if (name === 'wardType') next.bedId = 0
      if (name === 'primaryDoctorId' || name === 'bedId' || name === 'referenceDoctorId') {
        next[name as keyof typeof next] = value === '' ? (name === 'referenceDoctorId' ? undefined : 0) : Number(value)
      }
      if (name === 'depositAmount') {
        const n = value === '' ? undefined : Number(value)
        next.depositAmount = n != null && !Number.isNaN(n) ? n : undefined
      }
      return next
    })
    setAdmissionError('')
  }

  const handlePatientFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setPatientForm((prev) => ({ ...prev, [name]: value }))
    setPatientFormError('')
  }

  const handleRegisterPatient = async (e: React.FormEvent) => {
    e.preventDefault()
    setPatientFormError('')
    if (!patientForm.fullName?.trim()) {
      setPatientFormError('Full name is required.')
      return
    }
    if (patientForm.age == null || patientForm.age < 0 || patientForm.age > 150) {
      setPatientFormError('Age must be between 0 and 150.')
      return
    }
    setPatientFormLoading(true)
    try {
      const created = await receptionApi.register({
        ...patientForm,
        fullName: patientForm.fullName.trim(),
        age: patientForm.age,
        gender: patientForm.gender,
      })
      setNewPatientRegistered(created)
      setAdmissionForm((prev) => ({ ...prev, patientUhid: created.uhid }))
      setPatientForm({
        fullName: '',
        idProofType: '',
        idProofNumber: '',
        dateOfBirth: '',
        age: 0,
        ageYears: undefined,
        ageMonths: undefined,
        ageDays: undefined,
        gender: 'Male',
        weightKg: undefined,
        heightCm: undefined,
        phone: '',
        address: '',
        state: '',
        city: '',
        district: '',
        fatherHusbandName: '',
        referredBy: '',
        referredName: '',
        referredPhone: '',
        consultantName: '',
        specialization: '',
        organisationType: '',
        organisationName: '',
        remarks: '',
      })
    } catch (err: unknown) {
      const ax = err as { response?: { data?: ApiError } }
      const data = ax.response?.data
      if (data?.errors) {
        setPatientFormError(Object.entries(data.errors).map(([k, v]) => `${k}: ${v}`).join('. '))
      } else {
        setPatientFormError(data?.message || 'Registration failed.')
      }
    } finally {
      setPatientFormLoading(false)
    }
  }

  const handleAddIPDAdmission = async (e: React.FormEvent) => {
    e.preventDefault()
    setAdmissionError('')
    setAdmissionSuccess(null)
    const uhid = patientUhid?.trim()
    if (!uhid) {
      setAdmissionError('Please select or search for a patient.')
      return
    }
    if (!admissionForm.primaryDoctorId) {
      setAdmissionError('Please select Consultant Doctor.')
      return
    }
    if (!admissionForm.wardType) {
      setAdmissionError('Please select ward type first, then assign a bed.')
      return
    }
    if (!admissionForm.bedId) {
      setAdmissionError('Please assign a bed.')
      return
    }
    if (!admissionForm.admissionDateTime?.trim()) {
      setAdmissionError('Admission date & time is required.')
      return
    }
    if (!admissionForm.diagnosis?.trim()) {
      setAdmissionError('Diagnosis / notes are required.')
      return
    }
    setAdmissionLoading(true)
    try {
      const created = await ipdApi.admit({
        patientUhid: uhid,
        primaryDoctorId: admissionForm.primaryDoctorId,
        admissionType: admissionForm.admissionType,
        bedId: admissionForm.bedId,
        admissionDateTime: admissionForm.admissionDateTime.trim(),
        diagnosis: admissionForm.diagnosis.trim(),
        opdVisitId: admissionForm.opdVisitId || undefined,
        remarks: admissionForm.remarks?.trim() || undefined,
        depositAmount: admissionForm.depositAmount,
        insuranceTpa: admissionForm.insuranceTpa?.trim() || undefined,
        admissionFormDocumentRef: admissionForm.admissionFormDocumentRef?.trim() || undefined,
        consentFormDocumentRef: admissionForm.consentFormDocumentRef?.trim() || undefined,
        idProofDocumentRef: admissionForm.idProofDocumentRef?.trim() || undefined,
      })
      setAdmissionSuccess({ id: created.id, admissionNumber: created.admissionNumber })
      setAdmissionForm((prev) => ({
        ...prev,
        patientUhid: '',
        primaryDoctorId: 0,
        bedId: 0,
        wardType: undefined,
        diagnosis: '',
        admissionDateTime: toIsoDateTimeLocal(new Date()),
      }))
      setSelectedPatient(null)
      setNewPatientRegistered(null)
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string; errors?: Record<string, string> } } }
      const data = ax.response?.data
      if (data?.errors) {
        setAdmissionError(Object.entries(data.errors).map(([k, v]) => `${k}: ${v}`).join('. '))
      } else {
        setAdmissionError(data?.message || 'Failed to add IPD admission.')
      }
    } finally {
      setAdmissionLoading(false)
    }
  }

  const showAdmissionForm = activeTab === 'old' || (activeTab === 'new' && newPatientRegistered)
  const canSubmitAdmission =
    patientUhid &&
    admissionForm.primaryDoctorId &&
    admissionForm.wardType &&
    admissionForm.bedId &&
    admissionForm.admissionDateTime &&
    admissionForm.diagnosis?.trim()

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>IPD Admission Management</h1>
      </header>

      <div className={styles.tabs}>
        <button
          type="button"
          className={`${styles.tab} ${activeTab === 'old' ? styles.tabActive : ''}`}
          onClick={() => setActiveTab('old')}
        >
          Old Patient / IPD Admission
        </button>
        <button
          type="button"
          className={`${styles.tab} ${activeTab === 'new' ? styles.tabActive : ''}`}
          onClick={() => setActiveTab('new')}
        >
          New Patient Registration
        </button>
      </div>

      <div className={styles.card}>
        {activeTab === 'new' && newPatientRegistered && (
          <div className={styles.banner} role="alert">
            New patient registered ({newPatientRegistered.registrationNumber}). Now select bed for IPD Admission.
          </div>
        )}

        {activeTab === 'new' && !newPatientRegistered && (
          <div className={styles.patientFormWrap}>
            <h2 className={patientFormStyles.sectionTitle}>New Patient Registration (Step 1 of 2)</h2>
            {patientFormError && <div className={styles.error}>{patientFormError}</div>}
            <form onSubmit={handleRegisterPatient} className={patientFormStyles.form}>
              <div className={patientFormStyles.row}>
                <label>
                  <span>Patient Name <span className={patientFormStyles.required}>*</span></span>
                  <input
                    name="fullName"
                    value={patientForm.fullName}
                    onChange={handlePatientFormChange}
                    required
                    placeholder="Full patient name"
                  />
                </label>
                <label>
                  <span>Date of Birth</span>
                  <input
                    type="date"
                    name="dateOfBirth"
                    value={patientForm.dateOfBirth}
                    onChange={handlePatientFormChange}
                    max={new Date().toISOString().split('T')[0]}
                  />
                </label>
              </div>
              <div className={patientFormStyles.row}>
                <label>
                  <span>Age (Years) <span className={patientFormStyles.required}>*</span></span>
                  <input
                    type="number"
                    name="age"
                    value={patientForm.age || ''}
                    onChange={handlePatientFormChange}
                    required
                    min={0}
                    max={150}
                    placeholder="Age in years"
                  />
                </label>
                <label>
                  <span>Gender <span className={patientFormStyles.required}>*</span></span>
                  <select name="gender" value={patientForm.gender} onChange={handlePatientFormChange} required>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </label>
              </div>
              <div className={patientFormStyles.row}>
                <label>
                  <span>Phone</span>
                  <input name="phone" value={patientForm.phone} onChange={handlePatientFormChange} placeholder="Contact number" />
                </label>
                <label>
                  <span>Address</span>
                  <input name="address" value={patientForm.address} onChange={handlePatientFormChange} placeholder="Address" />
                </label>
              </div>
              <div className={patientFormStyles.actions}>
                <button type="submit" className={patientFormStyles.submit} disabled={patientFormLoading}>
                  {patientFormLoading ? 'Registering…' : 'Register Patient'}
                </button>
                <Link to="/reception/register" className={styles.linkToSearch}>
                  Full registration form
                </Link>
              </div>
            </form>
          </div>
        )}

        {showAdmissionForm && (
          <form onSubmit={handleAddIPDAdmission}>
            <h2 className={styles.stepTitle}>
              IPD Admission Details {activeTab === 'new' && newPatientRegistered ? '(Step 2 of 2)' : ''}
            </h2>
            {admissionError && <div className={styles.error}>{admissionError}</div>}
            {admissionSuccess && (
              <div className={styles.success}>
                IPD Admission added: <strong>{admissionSuccess.admissionNumber}</strong>
                <button type="button" onClick={() => navigate(`/ipd/admissions/${admissionSuccess.id}`)}>
                  View admission
                </button>
              </div>
            )}

            <div className={styles.row}>
              {activeTab === 'old' ? (
                <>
                  <PatientSearch
                    label="Search by MR ID / Select Patient *"
                    value={admissionForm.patientUhid}
                    displayName={selectedPatient ? `${selectedPatient.fullName} (${selectedPatient.registrationNumber}, ${formatAgeDisplay(selectedPatient)})` : null}
                    onSelect={handlePatientSelect}
                    placeholder="(e.g., MR123456)"
                    required
                  />
                  <span className={styles.hint}>
                    <Link to="/reception/search">Full patient search</Link>
                  </span>
                </>
              ) : (
                newPatientRegistered && (
                  <div className={styles.row}>
                    <label>
                      <span>Patient</span>
                      <div style={{ padding: '0.5rem 0', color: 'var(--hms-text)' }}>
                        <strong>{newPatientRegistered.fullName}</strong> ({newPatientRegistered.registrationNumber}, {formatAgeDisplay(newPatientRegistered)})
                      </div>
                    </label>
                    <span className={styles.eligible}>Patient is eligible for New Admission.</span>
                  </div>
                )
              )}
            </div>

            <div className={styles.row}>
              <label>
                Admission Date & Time <span className={styles.required}>*</span>
                <input
                  name="admissionDateTime"
                  type="datetime-local"
                  value={admissionForm.admissionDateTime ?? ''}
                  onChange={handleAdmissionChange}
                  className={styles.input}
                  required
                />
              </label>
            </div>

            <div className={styles.row}>
              <label>
                Consultant Doctor <span className={styles.required}>*</span>
                <select
                  name="primaryDoctorId"
                  value={admissionForm.primaryDoctorId || ''}
                  onChange={handleAdmissionChange}
                  className={styles.select}
                  required
                >
                  <option value="">Select Doctor</option>
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
                  value={admissionForm.wardType ?? ''}
                  onChange={handleAdmissionChange}
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
                Assign Bed <span className={styles.required}>*</span>
                <select
                  name="bedId"
                  value={admissionForm.bedId || ''}
                  onChange={handleAdmissionChange}
                  className={styles.select}
                  required
                >
                  <option value="">Select Bed</option>
                  {selectableBeds.map((b) => (
                    <option key={b.bedId} value={b.bedId}>
                      {b.wardName} — {b.bedNumber}
                    </option>
                  ))}
                </select>
                <span className={styles.bedRateHint}>Daily rate can be configured per bed later.</span>
              </label>
            </div>

            <div className={styles.row}>
              <label>
                Admission Type <span className={styles.required}>*</span>
                <select
                  name="admissionType"
                  value={admissionForm.admissionType}
                  onChange={handleAdmissionChange}
                  className={styles.select}
                >
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
                Charge Category
                <select className={styles.select} disabled aria-label="Charge category (coming soon)">
                  <option value="">Select Charge Category</option>
                </select>
              </label>
              <label>
                Charge
                <select className={styles.select} disabled aria-label="Charge (coming soon)">
                  <option value="">Select Charge</option>
                </select>
              </label>
            </div>

            <div className={styles.row}>
              <label>
                Reference Doctor
                <select
                  name="referenceDoctorId"
                  value={admissionForm.referenceDoctorId ?? ''}
                  onChange={handleAdmissionChange}
                  className={styles.select}
                >
                  <option value="">Select/Add Doctor</option>
                  {doctors.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.fullName}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <div className={styles.row}>
              <label>
                Notes (diagnosis, initial condition, etc.) <span className={styles.required}>*</span>
                <textarea
                  name="diagnosis"
                  value={admissionForm.diagnosis ?? ''}
                  onChange={handleAdmissionChange}
                  placeholder="Admission notes, initial condition, etc."
                  className={styles.textarea}
                  rows={3}
                  required
                />
              </label>
            </div>

            <div className={styles.submitRow}>
              <button
                type="submit"
                className={styles.submitBtn}
                disabled={admissionLoading || !canSubmitAdmission || selectableBeds.length === 0}
              >
                {admissionLoading ? 'Adding…' : 'Add IPD Admission'}
              </button>
              {admissionLoading && <span className={styles.loading}>Please wait…</span>}
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
