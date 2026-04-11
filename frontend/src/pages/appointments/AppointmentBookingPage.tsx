/**
 * Book appointment – Visit Type first, then:
 * - New: name, mobile, age, address, gender → register patient then book
 * - Follow-up: search patient by name or number, select from results
 */

import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import { doctorsApi, departmentsApi } from '../../api/doctors'
import { receptionApi } from '../../api/reception'
import { PatientSearch } from '../../components/reception/PatientSearch'
import type { AppointmentRequest } from '../../types/appointment.types'
import type { DoctorResponse, DepartmentResponse } from '../../types/doctor'
import type { PatientResponse } from '../../types/patient'

const today = new Date().toISOString().slice(0, 10)

export function AppointmentBookingPage() {
  const navigate = useNavigate()
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [departments, setDepartments] = useState<DepartmentResponse[]>([])
  const [visitType, setVisitType] = useState<'NEW' | 'FOLLOWUP'>('NEW')
  const [form, setForm] = useState<AppointmentRequest & { patientUhid?: string; patientName?: string }>({
    patientId: 0,
    doctorId: 0,
    departmentId: 0,
    appointmentDate: today,
    slotTime: '09:00',
  })
  const [newPatient, setNewPatient] = useState({
    fullName: '',
    phone: '',
    age: 0,
    address: '',
    gender: 'MALE',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [slotConflict, setSlotConflict] = useState(false)
  const [checkingSlot, setCheckingSlot] = useState(false)

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((d) => setDoctors(d.content)).catch(() => [])
    departmentsApi.list().then(setDepartments).catch(() => [])
  }, [])

  // Check for slot conflicts whenever doctor, date, or time changes
  useEffect(() => {
    setSlotConflict(false)
    if (!form.doctorId || !form.appointmentDate || !form.slotTime) return

    setCheckingSlot(true)
    appointmentApi
      .search({
        doctorId: form.doctorId,
        date: form.appointmentDate,
        page: 0,
        size: 100,
      })
      .then((result) => {
        const normalizedSlot = form.slotTime.length === 5 ? form.slotTime : form.slotTime.slice(0, 5)
        const conflict = result.content.some(
          (a) =>
            a.slotTime.slice(0, 5) === normalizedSlot &&
            a.status !== 'CANCELLED',
        )
        setSlotConflict(conflict)
      })
      .catch(() => {})
      .finally(() => setCheckingSlot(false))
  }, [form.doctorId, form.appointmentDate, form.slotTime])

  useEffect(() => {
    if (visitType !== 'FOLLOWUP') {
      setForm((prev) => ({ ...prev, patientId: 0, patientUhid: undefined, patientName: undefined }))
    }
  }, [visitType])

  const handlePatientSelect = (patient: PatientResponse | null) => {
    setForm((prev) => ({
      ...prev,
      patientId: patient?.id ?? 0,
      patientUhid: patient?.uhid ?? '',
      patientName: patient?.fullName ?? '',
    }))
    setError('')
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: name === 'doctorId' || name === 'departmentId' || name === 'patientId' ? Number(value) : value,
    }))
    if (name === 'doctorId' && value) {
      const doc = doctors.find((d) => d.id === Number(value))
      if (doc) setForm((prev) => ({ ...prev, departmentId: doc.departmentId }))
    }
    setError('')
  }

  const handleNewPatientChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setNewPatient((prev) => ({
      ...prev,
      [name]: name === 'age' ? (value ? parseInt(value, 10) : 0) : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (visitType === 'NEW') {
      if (!newPatient.fullName?.trim()) {
        setError('Please enter patient name.')
        return
      }
      if (!newPatient.phone?.trim()) {
        setError('Please enter mobile number.')
        return
      }
      if (!newPatient.age || newPatient.age < 0) {
        setError('Please enter a valid age.')
        return
      }
    } else {
      if (!form.patientId) {
        setError('Please select a patient.')
        return
      }
    }

    if (!form.doctorId) {
      setError('Please select a doctor.')
      return
    }
    if (!form.departmentId) {
      setError('Please select a department.')
      return
    }

    setLoading(true)
    try {
      let patientId = form.patientId
      if (visitType === 'NEW') {
        const registered = await receptionApi.register({
          fullName: newPatient.fullName.trim(),
          phone: newPatient.phone.trim(),
          age: newPatient.age,
          address: newPatient.address?.trim() || undefined,
          gender: newPatient.gender,
        })
        patientId = registered.id
      }

      const request: AppointmentRequest = {
        patientId,
        doctorId: form.doctorId,
        departmentId: form.departmentId,
        appointmentDate: form.appointmentDate,
        slotTime: form.slotTime,
        visitType,
      }
      const created = await appointmentApi.create(request)
      navigate(`/front-office/appointments?created=${created.id}`)
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to book appointment.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="hms-page-shell">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/front-office/appointments">Appointments</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Book</li>
        </ol>
      </nav>

      <div className="hms-page-hero">
        <div>
          <div className="hms-page-kicker">Booking Desk</div>
          <h1 className="hms-page-title">Book Appointment</h1>
          <p className="hms-page-subtitle">Create a new appointment with the same visual language as Reception so the booking flow feels part of the same system.</p>
        </div>
        <div className="hms-page-actions">
          <Link to="/front-office/appointments" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
        </div>
      </div>

      <div className="hms-section-card">
        <div className="hms-section-card-header">
          <div>
            <h2 className="hms-section-title">Appointment Form</h2>
            <p className="hms-section-subtitle">Visit type, patient details, doctor, and slot selection in one place.</p>
          </div>
        </div>
        <div className="hms-section-card-body">
          <form onSubmit={handleSubmit} className="d-flex flex-column gap-3">
            {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}

            <div>
              <label htmlFor="visitType" className="form-label">Visit Type</label>
              <select
                id="visitType"
                name="visitType"
                className="form-select"
                value={visitType}
                onChange={(e) => setVisitType(e.target.value as 'NEW' | 'FOLLOWUP')}
              >
                <option value="NEW">New</option>
                <option value="FOLLOWUP">Follow-up</option>
              </select>
            </div>

            {visitType === 'NEW' ? (
              <div className="border rounded p-3 bg-light">
                <h6 className="mb-3">New Patient Details</h6>
                <div className="row g-2">
                  <div className="col-md-6">
                    <label htmlFor="fullName" className="form-label">Name *</label>
                    <input
                      type="text"
                      id="fullName"
                      name="fullName"
                      className="form-control"
                      placeholder="Full name"
                      value={newPatient.fullName}
                      onChange={handleNewPatientChange}
                    />
                  </div>
                  <div className="col-md-6">
                    <label htmlFor="phone" className="form-label">Mobile Number *</label>
                    <input
                      type="text"
                      id="phone"
                      name="phone"
                      className="form-control"
                      placeholder="Mobile number"
                      value={newPatient.phone}
                      onChange={handleNewPatientChange}
                    />
                  </div>
                  <div className="col-md-4">
                    <label htmlFor="age" className="form-label">Age *</label>
                    <input
                      type="number"
                      id="age"
                      name="age"
                      className="form-control"
                      min={0}
                      max={150}
                      placeholder="Age"
                      value={newPatient.age || ''}
                      onChange={handleNewPatientChange}
                    />
                  </div>
                  <div className="col-md-4">
                    <label htmlFor="gender" className="form-label">Gender *</label>
                    <select
                      id="gender"
                      name="gender"
                      className="form-select"
                      value={newPatient.gender}
                      onChange={handleNewPatientChange}
                    >
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                  <div className="col-12">
                    <label htmlFor="address" className="form-label">Address</label>
                    <input
                      type="text"
                      id="address"
                      name="address"
                      className="form-control"
                      placeholder="Address"
                      value={newPatient.address}
                      onChange={handleNewPatientChange}
                    />
                  </div>
                </div>
              </div>
            ) : (
              <PatientSearch
                value={form.patientUhid}
                displayName={form.patientName}
                onSelect={handlePatientSelect}
                label="Patient"
                placeholder="Search by name or mobile number"
                required
              />
            )}

            <div>
              <label htmlFor="departmentId" className="form-label">Department</label>
              <select
                id="departmentId"
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

            <div>
              <label htmlFor="doctorId" className="form-label">Doctor</label>
              <select
                id="doctorId"
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

            <div className="row">
              <div className="col-md-6">
                <label htmlFor="appointmentDate" className="form-label">Date</label>
                <input
                  type="date"
                  id="appointmentDate"
                  name="appointmentDate"
                  className="form-control"
                  value={form.appointmentDate}
                  onChange={handleChange}
                />
              </div>
              <div className="col-md-6">
                <label htmlFor="slotTime" className="form-label">Slot Time</label>
                <input
                  type="time"
                  id="slotTime"
                  name="slotTime"
                  className={`form-control ${slotConflict ? 'is-invalid' : ''}`}
                  value={form.slotTime}
                  onChange={handleChange}
                />
                {slotConflict && (
                  <div className="invalid-feedback">
                    This doctor already has an appointment at this time. Please choose a different slot.
                  </div>
                )}
                {checkingSlot && (
                  <div className="form-text text-muted">Checking availability…</div>
                )}
              </div>
            </div>

            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-primary" disabled={loading || slotConflict}>
                {loading ? 'Booking…' : 'Book Appointment'}
              </button>
              <Link to="/front-office/appointments" className="btn btn-outline-secondary">Cancel</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
