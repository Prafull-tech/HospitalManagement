import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { receptionApi } from '../api/reception'
import type { PatientRequest } from '../types/patient'
import type { ApiError } from '../types/patient'
import styles from './PatientRegisterPage.module.css'

function UserPlusIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <line x1="19" y1="8" x2="19" y2="14" />
      <line x1="22" y1="11" x2="16" y2="11" />
    </svg>
  )
}

const GENDERS = ['Male', 'Female', 'Other']
const ID_PROOF_TYPES = ['Aadhaar', 'Voter ID', 'PAN', 'Driving License', 'Passport', 'Other']
const REFERRED_BY_OPTIONS = ['Self', 'Doctor', 'Organisation']
const ORGANISATION_TYPES = ['General', 'Corporate', 'Insurance']

function calculateAgeFromDOB(dateOfBirth: string): { years: number; months: number; days: number } {
  if (!dateOfBirth) return { years: 0, months: 0, days: 0 }
  const dob = new Date(dateOfBirth)
  const now = new Date()
  let years = now.getFullYear() - dob.getFullYear()
  let months = now.getMonth() - dob.getMonth()
  let days = now.getDate() - dob.getDate()
  if (days < 0) {
    months--
    const lastMonth = new Date(now.getFullYear(), now.getMonth(), 0)
    days += lastMonth.getDate()
  }
  if (months < 0) {
    years--
    months += 12
  }
  return { years, months, days }
}

export function PatientRegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState<PatientRequest>({
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
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [successData, setSuccessData] = useState<{ uhid: string; registrationNumber: string } | null>(null)
  const [ageMode, setAgeMode] = useState<'auto' | 'manual'>('auto')

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    if (name === 'dateOfBirth' && ageMode === 'auto') {
      const calculated = calculateAgeFromDOB(value)
      setForm((prev) => ({
        ...prev,
        [name]: value,
        age: calculated.years,
        ageYears: calculated.years,
        ageMonths: calculated.months,
        ageDays: calculated.days,
      }))
    } else if (name.startsWith('age') && name !== 'age') {
      const numValue = value === '' ? undefined : Number(value)
      setForm((prev) => ({ ...prev, [name]: numValue }))
      if (name === 'ageYears' || name === 'ageMonths' || name === 'ageDays') {
        const years = name === 'ageYears' ? numValue : form.ageYears ?? 0
        setForm((prev) => ({ ...prev, age: years ?? 0 }))
      }
    } else {
      const numValue = name === 'age' || name === 'weightKg' || name === 'heightCm' ? (value === '' ? undefined : Number(value)) : value
      setForm((prev) => ({ ...prev, [name]: numValue }))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccessData(null)
    if (!form.fullName?.trim()) {
      setError('Full name is required.')
      return
    }
    if (!form.age || form.age < 0 || form.age > 150) {
      setError('Age must be between 0 and 150.')
      return
    }
    if (!form.gender) {
      setError('Gender is required.')
      return
    }
    setLoading(true)
    try {
      const payload: PatientRequest = {
        fullName: form.fullName.trim(),
        idProofType: form.idProofType || undefined,
        idProofNumber: form.idProofNumber || undefined,
        dateOfBirth: form.dateOfBirth || undefined,
        age: form.age,
        ageYears: form.ageYears,
        ageMonths: form.ageMonths,
        ageDays: form.ageDays,
        gender: form.gender,
        weightKg: form.weightKg,
        heightCm: form.heightCm,
        phone: form.phone?.trim() || undefined,
        address: form.address?.trim() || undefined,
        state: form.state?.trim() || undefined,
        city: form.city?.trim() || undefined,
        district: form.district?.trim() || undefined,
        fatherHusbandName: form.fatherHusbandName?.trim() || undefined,
        referredBy: form.referredBy || undefined,
        referredName: form.referredName?.trim() || undefined,
        referredPhone: form.referredPhone?.trim() || undefined,
        consultantName: form.consultantName?.trim() || undefined,
        specialization: form.specialization?.trim() || undefined,
        organisationType: form.organisationType || undefined,
        organisationName: form.organisationName?.trim() || undefined,
        remarks: form.remarks?.trim() || undefined,
      }
      const created = await receptionApi.register(payload)
      setSuccessData({ uhid: created.uhid, registrationNumber: created.registrationNumber })
      setForm({
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
      const ax = err as { response?: { data?: ApiError; status?: number } }
      const data = ax.response?.data
      if (data?.errors) {
        setError(Object.entries(data.errors).map(([k, v]) => `${k}: ${v}`).join('. '))
      } else {
        setError(data?.message || 'Registration failed. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <form onSubmit={handleSubmit} className={styles.form}>
        {error && <div className={styles.error}>{error}</div>}
        {successData && (
          <div className={styles.success}>
            Patient registered successfully.
            <div style={{ width: '100%', marginTop: '0.5rem' }}>
              <strong>UHID:</strong> {successData.uhid} | <strong>Registration No:</strong> {successData.registrationNumber}
            </div>
            <button type="button" onClick={() => navigate(`/reception/search?uhid=${successData.uhid}`)}>
              View patient
            </button>
          </div>
        )}

        {/* Registration Details Section */}
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Registration Details</h2>
          <div className={styles.row}>
            <label>
              <span>Reg Date</span>
              <input
                type="text"
                value={new Date().toLocaleString()}
                readOnly
                aria-readonly="true"
                className={styles.displayOnly}
              />
            </label>
            <label>
              <span>Patient Name <span className={styles.required}>*</span></span>
              <input
                name="fullName"
                value={form.fullName}
                onChange={handleChange}
                required
                placeholder="Full patient name"
              />
            </label>
          </div>
          <p className={styles.hint}>Registration No and UHID will be generated after you submit.</p>
          <div className={styles.row}>
            <label>
              <span>Patient ID Proof</span>
              <select name="idProofType" value={form.idProofType} onChange={handleChange}>
                <option value="">Select</option>
                {ID_PROOF_TYPES.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </label>
            <label>
              <span>ID Proof Number</span>
              <input
                name="idProofNumber"
                value={form.idProofNumber}
                onChange={handleChange}
                placeholder="ID proof number"
                disabled={!form.idProofType}
              />
            </label>
          </div>
          <div className={styles.row}>
            <label>
              <span>Date of Birth</span>
              <input
                type="date"
                name="dateOfBirth"
                value={form.dateOfBirth}
                onChange={handleChange}
                max={new Date().toISOString().split('T')[0]}
              />
            </label>
            <label>
              <span>Age Mode</span>
              <select value={ageMode} onChange={(e) => setAgeMode(e.target.value as 'auto' | 'manual')}>
                <option value="auto">Auto-calculated</option>
                <option value="manual">Manual</option>
              </select>
            </label>
          </div>
          {ageMode === 'auto' ? (
            <div className={styles.row}>
              <label>
                <span>Age (Y/M/D)</span>
                <input
                  type="text"
                  value={`${form.ageYears ?? 0}Y ${form.ageMonths ?? 0}M ${form.ageDays ?? 0}D`}
                  readOnly
                  aria-readonly="true"
                  className={styles.displayOnly}
                />
              </label>
              <label>
                <span>Age (Years) <span className={styles.required}>*</span></span>
                <input
                  type="number"
                  name="age"
                  value={form.age || ''}
                  onChange={handleChange}
                  required
                  min={0}
                  max={150}
                  placeholder="Age in years"
                />
              </label>
            </div>
          ) : (
            <div className={styles.row}>
              <label>
                <span>Age (Years) <span className={styles.required}>*</span></span>
                <input
                  type="number"
                  name="age"
                  value={form.age || ''}
                  onChange={handleChange}
                  required
                  min={0}
                  max={150}
                  placeholder="Age in years"
                />
              </label>
              <label>
                <span>Age (Months)</span>
                <input
                  type="number"
                  name="ageMonths"
                  value={form.ageMonths || ''}
                  onChange={handleChange}
                  min={0}
                  max={11}
                  placeholder="Months"
                />
              </label>
              <label>
                <span>Age (Days)</span>
                <input
                  type="number"
                  name="ageDays"
                  value={form.ageDays || ''}
                  onChange={handleChange}
                  min={0}
                  max={30}
                  placeholder="Days"
                />
              </label>
            </div>
          )}
          <div className={styles.row}>
            <label>
              <span>Gender <span className={styles.required}>*</span></span>
              <select name="gender" value={form.gender} onChange={handleChange} required>
                {GENDERS.map((g) => (
                  <option key={g} value={g}>{g}</option>
                ))}
              </select>
            </label>
          </div>
        </div>

        {/* Physical & Contact Details Section */}
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Physical & Contact Details</h2>
          <div className={styles.row}>
            <label>
              <span>Weight (kg)</span>
              <input
                type="number"
                name="weightKg"
                value={form.weightKg || ''}
                onChange={handleChange}
                min={0}
                step="0.1"
                placeholder="Weight in kg"
              />
            </label>
            <label>
              <span>Height (cm)</span>
              <input
                type="number"
                name="heightCm"
                value={form.heightCm || ''}
                onChange={handleChange}
                min={0}
                step="0.1"
                placeholder="Height in cm"
              />
            </label>
          </div>
          <div className={styles.row}>
            <label>
              <span>Phone No</span>
              <input
                name="phone"
                value={form.phone}
                onChange={handleChange}
                placeholder="Contact number"
              />
            </label>
            <label>
              <span>Father/Husband Name</span>
              <input
                name="fatherHusbandName"
                value={form.fatherHusbandName}
                onChange={handleChange}
                placeholder="Guardian/spouse name"
              />
            </label>
          </div>
          <label className={styles.textareaWrap}>
            <span>Address</span>
            <textarea
              name="address"
              value={form.address}
              onChange={handleChange}
              rows={3}
              placeholder="Complete address"
            />
          </label>
          <div className={`${styles.row} ${styles.rowThree}`}>
            <label>
              <span>State</span>
              <input
                name="state"
                value={form.state}
                onChange={handleChange}
                placeholder="State"
              />
            </label>
            <label>
              <span>City</span>
              <input
                name="city"
                value={form.city}
                onChange={handleChange}
                placeholder="City"
              />
            </label>
            <label>
              <span>District</span>
              <input
                name="district"
                value={form.district}
                onChange={handleChange}
                placeholder="District"
              />
            </label>
          </div>
        </div>

        {/* Referral & Consultant Section */}
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Referral & Consultant</h2>
          <div className={styles.row}>
            <label>
              <span>Referred By</span>
              <select name="referredBy" value={form.referredBy} onChange={handleChange}>
                <option value="">Select</option>
                {REFERRED_BY_OPTIONS.map((o) => (
                  <option key={o} value={o}>{o}</option>
                ))}
              </select>
            </label>
            <label>
              <span>Referred Name</span>
              <input
                name="referredName"
                value={form.referredName}
                onChange={handleChange}
                placeholder="Referral doctor/entity"
                disabled={!form.referredBy}
              />
            </label>
          </div>
          <div className={styles.row}>
            <label>
              <span>Referred Phone</span>
              <input
                name="referredPhone"
                value={form.referredPhone}
                onChange={handleChange}
                placeholder="Referral contact"
                disabled={!form.referredBy}
              />
            </label>
            <label>
              <span>Consultant Name</span>
              <input
                name="consultantName"
                value={form.consultantName}
                onChange={handleChange}
                placeholder="Consulting doctor"
              />
            </label>
          </div>
          <div className={styles.row}>
            <label>
              <span>Specialization</span>
              <input
                name="specialization"
                value={form.specialization}
                onChange={handleChange}
                placeholder="Doctor specialization"
              />
            </label>
          </div>
        </div>

        {/* Organisation / Insurance Section */}
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Organisation / Insurance</h2>
          <div className={styles.row}>
            <label>
              <span>Organisation Type</span>
              <select name="organisationType" value={form.organisationType} onChange={handleChange}>
                <option value="">Select</option>
                {ORGANISATION_TYPES.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </label>
            <label>
              <span>Organisation Name</span>
              <input
                name="organisationName"
                value={form.organisationName}
                onChange={handleChange}
                placeholder="Company/Insurance name"
                disabled={!form.organisationType}
              />
            </label>
          </div>
          <label className={styles.textareaWrap}>
            <span>Remarks</span>
            <textarea
              name="remarks"
              value={form.remarks}
              onChange={handleChange}
              rows={3}
              placeholder="Additional notes"
            />
          </label>
        </div>

        {/* Action Buttons */}
        <div className={styles.actions}>
          <button type="submit" disabled={loading} className={styles.submit}>
            <span className={styles.submitIcon}><UserPlusIcon /></span>
            {loading ? 'Registering…' : 'Register'}
          </button>
          <button type="button" onClick={() => navigate('/reception')} className={styles.cancel}>
            Exit
          </button>
        </div>
      </form>
    </div>
  )
}
