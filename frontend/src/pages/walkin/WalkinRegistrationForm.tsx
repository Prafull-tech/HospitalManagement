/**
 * Walk-in Registration Form – Search patient, register new, OPD visit, token.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { walkinApi } from '../../api/walkin'
import { doctorsApi, departmentsApi } from '../../api/doctors'
import { PatientSearch } from '../../components/reception/PatientSearch'
import type { WalkInRegisterRequest } from '../../types/walkin.types'
import type { DoctorResponse, DepartmentResponse } from '../../types/doctor'
import type { PatientResponse } from '../../types/patient'

const PRIORITY_OPTIONS = [
  { value: 'NORMAL', label: 'Normal' },
  { value: 'EMERGENCY', label: 'Emergency' },
  { value: 'SENIOR', label: 'Senior Citizen' },
  { value: 'PREGNANT', label: 'Pregnant' },
  { value: 'FOLLOWUP', label: 'Follow-up' },
]

export function WalkinRegistrationForm() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [departments, setDepartments] = useState<DepartmentResponse[]>([])
  const [patient, setPatient] = useState<PatientResponse | null>(null)
  const [visitType, setVisitType] = useState<'NEW' | 'FOLLOWUP'>('NEW')
  const [priority, setPriority] = useState<string>('NORMAL')
  const [form, setForm] = useState({
    fullName: '',
    gender: 'MALE',
    age: 0,
    mobile: '',
    address: '',
    city: '',
    state: '',
    pincode: '',
    idProofType: 'AADHAAR',
    idProofNumber: '',
    doctorId: 0,
    departmentId: 0,
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [successResult, setSuccessResult] = useState<{ tokenNo: string; patientName: string; uhid: string } | null>(null)

  useEffect(() => {
    doctorsApi
      .list({ status: 'ACTIVE', page: 0, size: 200 })
      .then((r) => {
        const list = Array.isArray(r?.content) ? r.content : []
        setDoctors(list)
        // #region agent log
        fetch('http://127.0.0.1:7243/ingest/aef12ba4-e33a-42fa-8d18-6d760473dcb7', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Debug-Session-Id': 'bce3bb',
          },
          body: JSON.stringify({
            sessionId: 'bce3bb',
            runId: 'post-fix',
            hypothesisId: 'H1',
            location: 'WalkinRegistrationForm.tsx:doctorsEffect',
            message: 'Loaded doctors for walk-in form',
            data: { count: list.length },
            timestamp: Date.now(),
          }),
        }).catch(() => {})
        // #endregion
      })
      .catch((err) => {
        setDoctors([])
        // #region agent log
        fetch('http://127.0.0.1:7243/ingest/aef12ba4-e33a-42fa-8d18-6d760473dcb7', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Debug-Session-Id': 'bce3bb',
          },
          body: JSON.stringify({
            sessionId: 'bce3bb',
            runId: 'post-fix',
            hypothesisId: 'H1',
            location: 'WalkinRegistrationForm.tsx:doctorsEffect',
            message: 'Failed to load doctors for walk-in form',
            data: { error: String(err && (err as Error).message) },
            timestamp: Date.now(),
          }),
        }).catch(() => {})
        // #endregion
      })
    departmentsApi
      .list()
      .then((list) => {
        const arr = Array.isArray(list) ? list : []
        setDepartments(arr)
        // #region agent log
        fetch('http://127.0.0.1:7243/ingest/aef12ba4-e33a-42fa-8d18-6d760473dcb7', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Debug-Session-Id': 'bce3bb',
          },
          body: JSON.stringify({
            sessionId: 'bce3bb',
            runId: 'post-fix',
            hypothesisId: 'H2',
            location: 'WalkinRegistrationForm.tsx:departmentsEffect',
            message: 'Loaded departments for walk-in form',
            data: { count: arr.length },
            timestamp: Date.now(),
          }),
        }).catch(() => {})
        // #endregion
      })
      .catch((err) => {
        setDepartments([])
        // #region agent log
        fetch('http://127.0.0.1:7243/ingest/aef12ba4-e33a-42fa-8d18-6d760473dcb7', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Debug-Session-Id': 'bce3bb',
          },
          body: JSON.stringify({
            sessionId: 'bce3bb',
            runId: 'post-fix',
            hypothesisId: 'H2',
            location: 'WalkinRegistrationForm.tsx:departmentsEffect',
            message: 'Failed to load departments for walk-in form',
            data: { error: String(err && (err as Error).message) },
            timestamp: Date.now(),
          }),
        }).catch(() => {})
        // #endregion
      })
  }, [])

  const handlePatientSelect = (p: PatientResponse | null) => {
    setPatient(p ?? null)
    setError('')
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'age' || name === 'doctorId' || name === 'departmentId' ? (value ? Number(value) : 0) : value,
    }))
    if (name === 'doctorId' && value) {
      const doc = doctors.find((d) => d.id === Number(value))
      if (doc) setForm((prev) => ({ ...prev, departmentId: doc.departmentId }))
    }
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccessResult(null)

    if (patient) {
      if (!form.doctorId) {
        setError('Please select a doctor.')
        return
      }
      if (!form.departmentId) {
        setError('Please select a department.')
        return
      }
    } else {
      if (!form.fullName?.trim()) {
        setError('Please enter patient name.')
        return
      }
      if (!form.gender) {
        setError('Please select gender.')
        return
      }
      if (!form.age || form.age < 0) {
        setError('Please enter a valid age.')
        return
      }
      if (!form.doctorId) {
        setError('Please select a doctor.')
        return
      }
      if (!form.departmentId) {
        setError('Please select a department.')
        return
      }
    }

    setLoading(true)
    try {
      const request: WalkInRegisterRequest = {
        doctorId: form.doctorId,
        departmentId: form.departmentId,
        visitType,
        priority: priority as WalkInRegisterRequest['priority'],
      }

      if (patient) {
        request.patientId = patient.id
      } else {
        request.fullName = form.fullName.trim()
        request.gender = form.gender
        request.age = form.age
        request.mobile = form.mobile?.trim() || undefined
        request.address = form.address?.trim() || undefined
        request.city = form.city?.trim() || undefined
        request.state = form.state?.trim() || undefined
        request.pincode = form.pincode?.trim() || undefined
        request.idProofType = form.idProofType || undefined
        request.idProofNumber = form.idProofNumber?.trim() || undefined
      }

      const result = await walkinApi.register(request)
      setSuccessResult({
        tokenNo: result.token.tokenNo,
        patientName: result.patientName,
        uhid: result.patientUhid,
      })
      setPatient(null)
      setForm((prev) => ({
        ...prev,
        fullName: '',
        age: 0,
        mobile: '',
        address: '',
        city: '',
        state: '',
        pincode: '',
        idProofNumber: '',
      }))
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to register walk-in.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/walkin">Walk-in</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Register</li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Walk-in Registration</h1>
        <Link to="/front-office/walkin" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
      </div>

      {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}
      {successResult && (
        <div className="alert alert-success py-3">
          <h6 className="mb-2">Registration Successful</h6>
          <p className="mb-1"><strong>Token:</strong> {successResult.tokenNo}</p>
          <p className="mb-1"><strong>Patient:</strong> {successResult.patientName}</p>
          <p className="mb-0"><strong>UHID:</strong> {successResult.uhid}</p>
          <p className="small text-muted mt-2 mb-0">Patient has been added to the doctor queue.</p>
        </div>
      )}

      <div className="card shadow-sm">
        <div className="card-body">
          <form onSubmit={handleSubmit} className="d-flex flex-column gap-3">
            <div>
              <label className="form-label">Search Existing Patient</label>
              <PatientSearch
                value={patient?.uhid}
                displayName={patient?.fullName}
                onSelect={handlePatientSelect}
                placeholder="Search by UHID, mobile, name or Aadhaar"
              />
              {patient && (
                <button type="button" className="btn btn-link btn-sm p-0 mt-1" onClick={() => setPatient(null)}>
                  Register as new patient instead
                </button>
              )}
            </div>

            {!patient && (
              <div className="border rounded p-3 bg-light">
                <h6 className="mb-3">New Patient Details</h6>
                <div className="row g-2">
                  <div className="col-md-6">
                    <label className="form-label">Patient Name *</label>
                    <input
                      type="text"
                      name="fullName"
                      className="form-control"
                      placeholder="Full name"
                      value={form.fullName}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label">Gender *</label>
                    <select name="gender" className="form-select" value={form.gender} onChange={handleChange}>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                  <div className="col-md-3">
                    <label className="form-label">Age *</label>
                    <input
                      type="number"
                      name="age"
                      className="form-control"
                      min={0}
                      max={150}
                      value={form.age || ''}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Mobile</label>
                    <input
                      type="text"
                      name="mobile"
                      className="form-control"
                      placeholder="Mobile number"
                      value={form.mobile}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Aadhaar / ID Proof</label>
                    <input
                      type="text"
                      name="idProofNumber"
                      className="form-control"
                      placeholder="Aadhaar or ID number"
                      value={form.idProofNumber}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="col-12">
                    <label className="form-label">Address</label>
                    <input
                      type="text"
                      name="address"
                      className="form-control"
                      placeholder="Address"
                      value={form.address}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">City</label>
                    <input
                      type="text"
                      name="city"
                      className="form-control"
                      value={form.city}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">State</label>
                    <input
                      type="text"
                      name="state"
                      className="form-control"
                      value={form.state}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Pincode</label>
                    <input
                      type="text"
                      name="pincode"
                      className="form-control"
                      value={form.pincode}
                      onChange={handleChange}
                    />
                  </div>
                </div>
              </div>
            )}

            <div className="row g-2">
              <div className="col-md-4">
                <label className="form-label">Department *</label>
                <select
                  name="departmentId"
                  className="form-select"
                  value={form.departmentId || ''}
                  onChange={handleChange}
                >
                  <option value="">Select department</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>{d.name}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Doctor *</label>
                <select
                  name="doctorId"
                  className="form-select"
                  value={form.doctorId || ''}
                  onChange={handleChange}
                >
                  <option value="">Select doctor</option>
                  {doctors
                    .filter((d) => !form.departmentId || d.departmentId === form.departmentId)
                    .map((d) => (
                      <option key={d.id} value={d.id}>{d.fullName} ({d.code})</option>
                    ))}
                </select>
              </div>
              <div className="col-md-2">
                <label className="form-label">Visit Type</label>
                <select
                  className="form-select"
                  value={visitType}
                  onChange={(e) => setVisitType(e.target.value as 'NEW' | 'FOLLOWUP')}
                >
                  <option value="NEW">New</option>
                  <option value="FOLLOWUP">Follow-up</option>
                </select>
              </div>
              <div className="col-md-2">
                <label className="form-label">Priority</label>
                <select
                  className="form-select"
                  value={priority}
                  onChange={(e) => setPriority(e.target.value)}
                >
                  {PRIORITY_OPTIONS.map((o) => (
                    <option key={o.value} value={o.value}>{o.label}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Registering…' : 'Register & Generate Token'}
              </button>
              <Link to="/front-office/walkin" className="btn btn-outline-secondary">Cancel</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
