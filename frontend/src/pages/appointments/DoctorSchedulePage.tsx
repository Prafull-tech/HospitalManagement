/**
 * Doctor schedule management – Admin only. Define working slots per day.
 */

import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { doctorScheduleApi } from '../../api/appointment'
import { doctorsApi } from '../../api/doctors'
import type { DoctorScheduleResponse, DoctorScheduleRequest } from '../../types/appointment.types'
import type { DoctorResponse } from '../../types/doctor'

const DAY_NAMES = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

export function DoctorSchedulePage() {
  const { doctorId } = useParams<{ doctorId: string }>()
  const [doctor, setDoctor] = useState<DoctorResponse | null>(null)
  const [schedules, setSchedules] = useState<DoctorScheduleResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState<DoctorScheduleRequest>({
    doctorId: Number(doctorId) || 0,
    dayOfWeek: 1,
    startTime: '09:00',
    endTime: '13:00',
    slotDurationMinutes: 10,
    maxPatients: 20,
  })

  useEffect(() => {
    if (doctorId) {
      doctorsApi.getById(Number(doctorId)).then(setDoctor).catch(() => setDoctor(null))
      doctorScheduleApi.getByDoctorId(Number(doctorId)).then(setSchedules).catch(() => setSchedules([]))
      setForm((prev) => ({ ...prev, doctorId: Number(doctorId) }))
    }
  }, [doctorId])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await doctorScheduleApi.create(form)
      if (doctorId) doctorScheduleApi.getByDoctorId(Number(doctorId)).then(setSchedules)
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to add schedule.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/doctors">Doctors</Link></li>
          <li className="breadcrumb-item"><Link to={`/doctors/${doctorId}/edit`}>Doctor</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Schedule</li>
        </ol>
      </nav>

      <h1 className="h4 mb-0">Doctor Schedule – {doctor?.fullName ?? 'Loading…'}</h1>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card shadow-sm">
        <div className="card-header bg-light py-2">
          <h6 className="mb-0">Add Slot</h6>
        </div>
        <div className="card-body">
          <form onSubmit={handleSubmit} className="row g-2">
            <div className="col-md-2">
              <label className="form-label">Day</label>
              <select
                className="form-select"
                value={form.dayOfWeek}
                onChange={(e) => setForm((prev) => ({ ...prev, dayOfWeek: Number(e.target.value) }))}
              >
                {[1, 2, 3, 4, 5, 6, 7].map((d) => (
                  <option key={d} value={d}>{DAY_NAMES[d]}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Start</label>
              <input
                type="time"
                className="form-control"
                value={form.startTime}
                onChange={(e) => setForm((prev) => ({ ...prev, startTime: e.target.value }))}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">End</label>
              <input
                type="time"
                className="form-control"
                value={form.endTime}
                onChange={(e) => setForm((prev) => ({ ...prev, endTime: e.target.value }))}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Slot (min)</label>
              <input
                type="number"
                className="form-control"
                min={5}
                max={60}
                value={form.slotDurationMinutes ?? 10}
                onChange={(e) => setForm((prev) => ({ ...prev, slotDurationMinutes: Number(e.target.value) }))}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Max Patients</label>
              <input
                type="number"
                className="form-control"
                min={1}
                value={form.maxPatients ?? ''}
                onChange={(e) => setForm((prev) => ({ ...prev, maxPatients: e.target.value ? Number(e.target.value) : undefined }))}
              />
            </div>
            <div className="col-md-2 d-flex align-items-end">
              <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                {loading ? 'Adding…' : 'Add'}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div className="card border shadow-sm">
        <div className="card-header bg-light py-2">
          <h6 className="mb-0">Current Schedule</h6>
        </div>
        <div className="table-responsive">
          <table className="table table-striped mb-0">
            <thead className="table-light">
              <tr>
                <th>Day</th>
                <th>Start</th>
                <th>End</th>
                <th>Slot (min)</th>
                <th>Max Patients</th>
              </tr>
            </thead>
            <tbody>
              {schedules.length === 0 ? (
                <tr><td colSpan={5} className="text-center text-muted py-4">No schedule defined.</td></tr>
              ) : (
                schedules.map((s) => (
                  <tr key={s.id}>
                    <td>{DAY_NAMES[s.dayOfWeek]}</td>
                    <td>{s.startTime}</td>
                    <td>{s.endTime}</td>
                    <td>{s.slotDurationMinutes}</td>
                    <td>{s.maxPatients ?? '—'}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
