import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { receptionApi } from '../api/reception'
import type { PatientResponse } from '../types/patient'
import type { ApiError } from '../types/patient'

function field(label: string, value: string | number | undefined) {
  if (value === undefined || value === null || value === '') return null
  return (
    <div key={label} className="mb-2">
      <span className="text-muted small">{label}:</span>{' '}
      <span>{String(value)}</span>
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
      <h1 className="print-only h5 mb-2">Patient Details – {patient.fullName}</h1>
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
          <div className="row">
            <div className="col-md-6">
              <h6 className="text-muted border-bottom pb-1 mb-2">Registration</h6>
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
            <div className="col-md-6">
              <h6 className="text-muted border-bottom pb-1 mb-2">Contact &amp; Physical</h6>
              {field('Phone', patient.phone)}
              {field('Address', patient.address)}
              {field('City', patient.city)}
              {field('District', patient.district)}
              {field('State', patient.state)}
              {field('Weight (kg)', patient.weightKg)}
              {field('Height (cm)', patient.heightCm)}
              <h6 className="text-muted border-bottom pb-1 mb-2 mt-3">Referral &amp; Other</h6>
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
  )
}
