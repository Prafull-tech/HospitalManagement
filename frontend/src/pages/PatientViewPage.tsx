import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { receptionApi } from '../api/reception'
import type { PatientResponse } from '../types/patient'
import type { ApiError } from '../types/patient'

const HOSPITAL_NAME = 'City General Hospital'
const HOSPITAL_SLOGAN = 'Caring for Life, Every Day'
const HOSPITAL_ADDRESS_1 = '123 Health Avenue, Medical District'
const HOSPITAL_ADDRESS_2 = 'Hyderabad, Telangana – 500 001'
const HOSPITAL_PHONE = '+91-40-2345-6789'
const HOSPITAL_EMAIL = 'info@citygeneralhospital.in'

function PrintHeader({ patient }: { patient: PatientResponse }) {
  const today = new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
  return (
    <div className="print-letterhead-header">
      <div className="print-letterhead-top">
        <div className="print-letterhead-logo-area">
          <svg viewBox="0 0 48 48" width="52" height="52" xmlns="http://www.w3.org/2000/svg">
            <circle cx="24" cy="24" r="23" fill="#1a3a8f" stroke="#ccd6f6" strokeWidth="1"/>
            <rect x="20" y="10" width="8" height="28" rx="2" fill="white"/>
            <rect x="10" y="20" width="28" height="8" rx="2" fill="white"/>
          </svg>
        </div>
        <div className="print-letterhead-title">
          <h1 className="print-hosp-name">{HOSPITAL_NAME}</h1>
          <p className="print-hosp-slogan">{HOSPITAL_SLOGAN}</p>
        </div>
        <div className="print-letterhead-address-area">
          <p>{HOSPITAL_ADDRESS_1}</p>
          <p>{HOSPITAL_ADDRESS_2}</p>
          <p>Ph: {HOSPITAL_PHONE}</p>
        </div>
      </div>
      <div className="print-letterhead-divider" />
      <div className="print-patient-strip">
        <span><strong>Name:</strong> {patient.fullName}</span>
        <span><strong>UHID:</strong> {patient.uhid}</span>
        <span><strong>Age/Sex:</strong> {patient.age ? `${patient.age} / ` : ''}{patient.gender}</span>
        <span><strong>Date:</strong> {today}</span>
      </div>
    </div>
  )
}

function PrintFooter() {
  return (
    <div className="print-letterhead-footer">
      <div className="print-letterhead-footer-bar" />
      <p className="print-letterhead-footer-text">
        {HOSPITAL_ADDRESS_1} &nbsp;|&nbsp; {HOSPITAL_ADDRESS_2} &nbsp;|&nbsp; Ph: {HOSPITAL_PHONE} &nbsp;|&nbsp; {HOSPITAL_EMAIL}
      </p>
    </div>
  )
}

function field(label: string, value: string | number | undefined) {
  if (value === undefined || value === null || value === '') return null
  return (
    <div key={label}>
      <span className="text-muted small">{label}</span>
      <span className="fw-medium">{String(value)}</span>
    </div>
  )
}

function formatDateTime(iso?: string) {
  if (!iso) return undefined
  try {
    const d = new Date(iso)
    return d.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
  } catch {
    return iso
  }
}

export function PatientViewPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [patient, setPatient] = useState<PatientResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) {
      setError('Invalid patient ID')
      setLoading(false)
      return
    }
    const numId = Number(id)
    if (Number.isNaN(numId)) {
      setError('Invalid patient ID')
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    receptionApi
      .getById(numId)
      .then(setPatient)
      .catch((err: unknown) => {
        const ax = err as { response?: { data?: ApiError } }
        setError(ax.response?.data?.message || 'Failed to load patient.')
      })
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <div className="p-3">Loading…</div>
  if (error) {
    return (
      <div className="p-3">
        <div className="alert alert-danger">{error}</div>
        <Link to="/reception/search">Back to Search</Link>
      </div>
    )
  }
  if (!patient) return null

  const ageParts: string[] = []
  if (patient.ageYears != null) ageParts.push(`${patient.ageYears} yrs`)
  if (patient.ageMonths != null) ageParts.push(`${patient.ageMonths} months`)
  if (patient.ageDays != null) ageParts.push(`${patient.ageDays} days`)
  const ageDetail = ageParts.length > 0 ? ageParts.join(', ') : undefined

  const handlePrintPage = () => {
    const prevTitle = document.title
    document.title = `Patient - ${patient.fullName} (${patient.uhid})`
    window.print()
    document.title = prevTitle
  }

  return (
    <div className="d-flex flex-column gap-3">
      {/* Print-only letterhead header */}
      <div className="print-only">
        <PrintHeader patient={patient} />
      </div>

      <nav aria-label="Breadcrumb" className="no-print">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/reception">Reception</Link></li>
          <li className="breadcrumb-item"><Link to="/reception/search">Search Patient</Link></li>
          <li className="breadcrumb-item active" aria-current="page">View Patient</li>
        </ol>
      </nav>

      <div className="card shadow-sm">
        <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
          <h2 className="h6 mb-0 fw-bold">{patient.fullName}</h2>
          <div className="d-flex gap-2 no-print">
            <button type="button" className="btn btn-sm btn-outline-secondary" onClick={handlePrintPage} aria-label="Print page">
              Print
            </button>
            <button type="button" className="btn btn-sm btn-primary" onClick={() => navigate(`/reception/patient/${patient.id}/edit`)}>Edit</button>
            <Link to="/reception/search" className="btn btn-sm btn-outline-secondary">Back to Search</Link>
          </div>
        </div>
        <div className="card-body">
          <div className="row g-0">
            {/* ── Left Panel: Registration ── */}
            <div className="col-md-6 pe-md-4 border-end-md">
              <h6 className="section-panel-title">Registration</h6>
              <div className="section-panel-grid">
                {field('UHID', patient.uhid)}
                {field('Registration No', patient.registrationNumber)}
                {field('Registration Date', patient.registrationDate)}
                {field('Full Name', patient.fullName)}
                {field('ID Proof Type', patient.idProofType)}
                {field('ID Proof Number', patient.idProofNumber)}
                {field('Date of Birth', patient.dateOfBirth)}
                {field('Age', patient.age)}
                {field('Age (Yrs / Months / Days)', ageDetail)}
                {field('Gender', patient.gender)}
                {field('Father/Husband Name', patient.fatherHusbandName)}
                {field('Created', formatDateTime(patient.createdAt))}
                {field('Last Updated', formatDateTime(patient.updatedAt))}
              </div>
            </div>

            {/* ── Right Panel: Contact + Referral ── */}
            <div className="col-md-6 ps-md-4">
              <h6 className="section-panel-title">Contact &amp; Physical</h6>
              <div className="section-panel-grid">
                {field('Phone', patient.phone)}
                {field('Address', patient.address)}
                {field('City', patient.city)}
                {field('District', patient.district)}
                {field('State', patient.state)}
                {field('Weight (kg)', patient.weightKg)}
                {field('Height (cm)', patient.heightCm)}
              </div>

              <h6 className="section-panel-title mt-3">Referral &amp; Other</h6>
              <div className="section-panel-grid">
                {field('Referred By', patient.referredBy)}
                {field('Referred Name', patient.referredName)}
                {field('Referred Phone', patient.referredPhone)}
                {field('Consultant Name', patient.consultantName)}
                {field('Specialization', patient.specialization)}
                {field('Organisation Type', patient.organisationType)}
                {field('Organisation Name', patient.organisationName)}
                {field('Remarks', patient.remarks)}
                {field('Status', patient.active === false ? 'Disabled' : 'Active')}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Print-only footer */}
      <div className="print-only">
        <PrintFooter />
      </div>

      {/* Print watermark */}
      <div className="print-watermark print-only" aria-hidden="true">
        <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
          <circle cx="24" cy="24" r="23" fill="none" stroke="#1a3a8f" strokeWidth="1.5"/>
          <rect x="20" y="10" width="8" height="28" rx="2" fill="#1a3a8f"/>
          <rect x="10" y="20" width="28" height="8" rx="2" fill="#1a3a8f"/>
        </svg>
      </div>
    </div>
  )
}
