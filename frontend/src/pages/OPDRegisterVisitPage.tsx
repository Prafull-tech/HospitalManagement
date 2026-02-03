import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { opdApi } from '../api/opd'
import { PatientSearch } from '../components/reception/PatientSearch'
import type { OPDVisitRequest } from '../types/opd'
import type { DoctorResponse } from '../types/doctor'
import type { PatientResponse } from '../types/patient'

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
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><a href="/opd">OPD</a></li>
          <li className="breadcrumb-item active" aria-current="page">Register Visit</li>
        </ol>
      </nav>

      <h2 className="h5 mb-0 fw-bold">Register OPD Visit</h2>

      <div className="card shadow-sm">
        <div className="card-header">
          <h3 className="h6 mb-0 fw-bold">New visit</h3>
        </div>
        <div className="card-body">
          <form onSubmit={handleSubmit} className="d-flex flex-column gap-3">
            {error && (
              <div className="alert alert-danger py-2 mb-0" role="alert">{error}</div>
            )}
            {visitId && (
              <div className="alert alert-success d-flex align-items-center justify-content-between flex-wrap gap-2 py-2 mb-0">
                <span>OPD visit registered.</span>
                <button type="button" className="btn btn-sm btn-outline-success" onClick={() => navigate(`/opd/visits/${visitId}`)}>
                  Open visit
                </button>
              </div>
            )}
            <div>
              <PatientSearch
                label="Patient"
                value={form.patientUhid}
                displayName={patientName}
                onSelect={handlePatientSelect}
                placeholder="Search by UHID, ID, mobile or name"
                required
              />
            </div>
            <div>
              <label className="form-label small fw-medium">Doctor <span className="text-danger">*</span></label>
              <select
                name="doctorId"
                value={form.doctorId || ''}
                onChange={handleChange}
                className="form-select form-select-sm"
              >
                <option value="">Select doctor</option>
                {doctors.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.fullName} — {d.departmentName} ({d.code})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="form-label small fw-medium">Visit date <span className="text-danger">*</span></label>
              <input
                name="visitDate"
                type="date"
                value={form.visitDate}
                onChange={handleChange}
                className="form-control form-control-sm"
              />
            </div>
            <div className="d-flex align-items-center gap-2">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Registering…' : 'Register OPD Visit'}
              </button>
              {loading && <span className="small text-muted">Please wait…</span>}
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
