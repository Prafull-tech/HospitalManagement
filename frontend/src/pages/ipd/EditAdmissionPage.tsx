import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ipdApi } from '../../api/ipd'
import { receptionApi } from '../../api/reception'
import type { IPDAdmissionResponse, AdmissionStatus } from '../../types/ipd'
import type { PatientResponse, PatientRequest } from '../../types/patient'
import styles from './EditAdmissionPage.module.css'

const DISABLEABLE: AdmissionStatus[] = ['ADMITTED', 'ACTIVE', 'TRANSFERRED', 'DISCHARGE_INITIATED']

function toRequest(p: PatientResponse): PatientRequest {
  return {
    fullName: p.fullName ?? '',
    idProofType: p.idProofType ?? undefined,
    idProofNumber: p.idProofNumber ?? undefined,
    dateOfBirth: p.dateOfBirth ?? undefined,
    age: p.age ?? 0,
    ageYears: p.ageYears ?? undefined,
    ageMonths: p.ageMonths ?? undefined,
    ageDays: p.ageDays ?? undefined,
    gender: p.gender ?? '',
    weightKg: p.weightKg ?? undefined,
    heightCm: p.heightCm ?? undefined,
    phone: p.phone ?? undefined,
    address: p.address ?? undefined,
    state: p.state ?? undefined,
    city: p.city ?? undefined,
    district: p.district ?? undefined,
    fatherHusbandName: p.fatherHusbandName ?? undefined,
    referredBy: p.referredBy ?? undefined,
    referredName: p.referredName ?? undefined,
    referredPhone: p.referredPhone ?? undefined,
    consultantName: p.consultantName ?? undefined,
    specialization: p.specialization ?? undefined,
    organisationType: p.organisationType ?? undefined,
    organisationName: p.organisationName ?? undefined,
    remarks: p.remarks ?? undefined,
  }
}

export function EditAdmissionPage() {
  const { id } = useParams<{ id: string }>()
  const [admission, setAdmission] = useState<IPDAdmissionResponse | null>(null)
  const [patient, setPatient] = useState<PatientResponse | null>(null)
  const [form, setForm] = useState<PatientRequest | null>(null)
  const [loading, setLoading] = useState(true)
  const [patientLoading, setPatientLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [disabling, setDisabling] = useState(false)

  const admissionId = id ? Number(id) : null

  useEffect(() => {
    if (!admissionId || Number.isNaN(admissionId)) {
      setError('Invalid admission ID')
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    ipdApi
      .getById(admissionId)
      .then(setAdmission)
      .catch((err) => setError(err.response?.data?.message || 'Failed to load admission'))
      .finally(() => setLoading(false))
  }, [admissionId])

  useEffect(() => {
    if (!admission?.patientId) return
    setPatientLoading(true)
    receptionApi
      .getById(admission.patientId)
      .then((p) => {
        setPatient(p)
        setForm(toRequest(p))
      })
      .catch(() => setPatient(null))
      .finally(() => setPatientLoading(false))
  }, [admission?.patientId])

  const updateField = <K extends keyof PatientRequest>(key: K, value: PatientRequest[K]) => {
    setForm((prev) => (prev ? { ...prev, [key]: value } : null))
  }

  const handleSavePatient = (e: React.FormEvent) => {
    e.preventDefault()
    if (!patient || !form) return
    setSaving(true)
    setError('')
    setSuccess('')
    receptionApi
      .update(patient.id, form)
      .then((updated) => {
        setPatient(updated)
        setForm(toRequest(updated))
        setSuccess('Patient details saved.')
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to save patient details'))
      .finally(() => setSaving(false))
  }

  const handleDisable = () => {
    if (!admissionId || !admission) return
    if (!window.confirm('Disable this admission (set status to Cancelled)? The bed will be released.')) return
    setDisabling(true)
    setError('')
    setSuccess('')
    ipdApi
      .changeAdmissionStatus(admissionId, { toStatus: 'CANCELLED', reason: 'Disabled from edit page' })
      .then((updated) => {
        setAdmission(updated)
        setSuccess('Admission disabled (Cancelled).')
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to disable admission'))
      .finally(() => setDisabling(false))
  }

  const canDisable = admission && DISABLEABLE.includes(admission.admissionStatus)

  if (loading || !admission) {
    return (
      <div className={styles.page}>
        <Link to="/ipd/admissions" className={styles.backLink}>← Back to list</Link>
        {loading && <p className={styles.loading}>Loading…</p>}
        {error && <div className={styles.error}>{error}</div>}
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <Link to="/ipd/admissions" className={styles.backLink}>← Back to list</Link>
      <div className={styles.header}>
        <h1 className={styles.title}>Edit IPD Admission</h1>
        <Link to={`/ipd/admissions/${admission.id}`} className={styles.viewLink}>View details</Link>
      </div>
      {error && <div className={styles.error} role="alert">{error}</div>}
      {success && <div className={styles.success} role="alert">{success}</div>}

      <div className={styles.card}>
        <p className={styles.meta}>
          <strong>{admission.admissionNumber}</strong> — {admission.patientName} ({admission.patientUhid})
        </p>
        <p className={styles.meta}>
          Ward / Bed: {[admission.currentWardName, admission.currentBedNumber].filter(Boolean).join(' / ') || '—'}
        </p>
        <p className={styles.meta}>Status: {admission.admissionStatus}</p>

        {canDisable && (
          <div className={styles.actions}>
            <button
              type="button"
              className={styles.disableBtn}
              onClick={handleDisable}
              disabled={disabling}
            >
              {disabling ? 'Disabling…' : 'Disable registration'}
            </button>
            <p className={styles.hint}>Sets status to Cancelled and releases the bed.</p>
          </div>
        )}
        {!canDisable && (
          <p className={styles.muted}>This admission cannot be disabled (status: {admission.admissionStatus}).</p>
        )}
      </div>

      {/* Patient details form */}
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Patient details</h2>
        {patientLoading && <p className={styles.loading}>Loading patient…</p>}
        {!patientLoading && patient && form && (
          <form onSubmit={handleSavePatient}>
            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Basic information</h3>
              <div className={styles.formGrid}>
                <div className={styles.formGridFull}>
                  <label className={styles.label}>Full name *</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.fullName}
                    onChange={(e) => updateField('fullName', e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label className={styles.label}>Date of birth</label>
                  <input
                    type="date"
                    className={styles.input}
                    value={form.dateOfBirth ?? ''}
                    onChange={(e) => updateField('dateOfBirth', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Age *</label>
                  <input
                    type="number"
                    className={styles.input}
                    min={0}
                    value={form.age}
                    onChange={(e) => updateField('age', Number(e.target.value) || 0)}
                    required
                  />
                </div>
                <div>
                  <label className={styles.label}>Gender *</label>
                  <select
                    className={styles.select}
                    value={form.gender}
                    onChange={(e) => updateField('gender', e.target.value)}
                    required
                  >
                    <option value="">Select</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                <div>
                  <label className={styles.label}>Weight (kg)</label>
                  <input
                    type="number"
                    className={styles.input}
                    min={0}
                    step={0.1}
                    value={form.weightKg ?? ''}
                    onChange={(e) => updateField('weightKg', e.target.value ? Number(e.target.value) : undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Height (cm)</label>
                  <input
                    type="number"
                    className={styles.input}
                    min={0}
                    step={0.1}
                    value={form.heightCm ?? ''}
                    onChange={(e) => updateField('heightCm', e.target.value ? Number(e.target.value) : undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Age (years)</label>
                  <input
                    type="number"
                    className={styles.input}
                    min={0}
                    value={form.ageYears ?? ''}
                    onChange={(e) => updateField('ageYears', e.target.value ? Number(e.target.value) : undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Age (months)</label>
                  <input
                    type="number"
                    className={styles.input}
                    min={0}
                    value={form.ageMonths ?? ''}
                    onChange={(e) => updateField('ageMonths', e.target.value ? Number(e.target.value) : undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Age (days)</label>
                  <input
                    type="number"
                    className={styles.input}
                    min={0}
                    value={form.ageDays ?? ''}
                    onChange={(e) => updateField('ageDays', e.target.value ? Number(e.target.value) : undefined)}
                  />
                </div>
              </div>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>ID proof</h3>
              <div className={styles.formGrid}>
                <div>
                  <label className={styles.label}>ID proof type</label>
                  <input
                    type="text"
                    className={styles.input}
                    placeholder="e.g. Aadhaar, PAN"
                    value={form.idProofType ?? ''}
                    onChange={(e) => updateField('idProofType', e.target.value || undefined)}
                  />
                </div>
                <div className={styles.formGridFull}>
                  <label className={styles.label}>ID proof number</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.idProofNumber ?? ''}
                    onChange={(e) => updateField('idProofNumber', e.target.value || undefined)}
                  />
                </div>
              </div>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Contact & address</h3>
              <div className={styles.formGrid}>
                <div>
                  <label className={styles.label}>Phone</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.phone ?? ''}
                    onChange={(e) => updateField('phone', e.target.value || undefined)}
                  />
                </div>
                <div className={styles.formGridFull}>
                  <label className={styles.label}>Address</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.address ?? ''}
                    onChange={(e) => updateField('address', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>City</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.city ?? ''}
                    onChange={(e) => updateField('city', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>District</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.district ?? ''}
                    onChange={(e) => updateField('district', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>State</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.state ?? ''}
                    onChange={(e) => updateField('state', e.target.value || undefined)}
                  />
                </div>
              </div>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Family / guardian</h3>
              <div className={styles.formGrid}>
                <div className={styles.formGridFull}>
                  <label className={styles.label}>Father / Husband name</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.fatherHusbandName ?? ''}
                    onChange={(e) => updateField('fatherHusbandName', e.target.value || undefined)}
                  />
                </div>
              </div>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Referral</h3>
              <div className={styles.formGrid}>
                <div>
                  <label className={styles.label}>Referred by</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.referredBy ?? ''}
                    onChange={(e) => updateField('referredBy', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Referred name</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.referredName ?? ''}
                    onChange={(e) => updateField('referredName', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Referred phone</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.referredPhone ?? ''}
                    onChange={(e) => updateField('referredPhone', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Consultant name</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.consultantName ?? ''}
                    onChange={(e) => updateField('consultantName', e.target.value || undefined)}
                  />
                </div>
                <div>
                  <label className={styles.label}>Specialization</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.specialization ?? ''}
                    onChange={(e) => updateField('specialization', e.target.value || undefined)}
                  />
                </div>
              </div>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Organisation</h3>
              <div className={styles.formGrid}>
                <div>
                  <label className={styles.label}>Organisation type</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.organisationType ?? ''}
                    onChange={(e) => updateField('organisationType', e.target.value || undefined)}
                  />
                </div>
                <div className={styles.formGridFull}>
                  <label className={styles.label}>Organisation name</label>
                  <input
                    type="text"
                    className={styles.input}
                    value={form.organisationName ?? ''}
                    onChange={(e) => updateField('organisationName', e.target.value || undefined)}
                  />
                </div>
              </div>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Remarks</h3>
              <div className={styles.formGrid}>
                <div className={styles.formGridFull}>
                  <label className={styles.label}>Remarks</label>
                  <textarea
                    className={styles.textarea}
                    value={form.remarks ?? ''}
                    onChange={(e) => updateField('remarks', e.target.value || undefined)}
                  />
                </div>
              </div>
            </section>

            <div className={styles.actions}>
              <button type="submit" className={styles.saveBtn} disabled={saving}>
                {saving ? 'Saving…' : 'Save patient details'}
              </button>
            </div>
          </form>
        )}
        {!patientLoading && !patient && admission?.patientId && (
          <p className={styles.muted}>Could not load patient details.</p>
        )}
      </div>
    </div>
  )
}
