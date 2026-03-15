/**
 * Waiting room TV display – Current Token, Next Token, Doctor Name, Room No.
 * Auto-refresh every 5 seconds.
 */

import { useState, useEffect } from 'react'
import { tokenApi } from '../../api/token'
import { doctorsApi } from '../../api/doctors'
import type { TokenDisplay } from '../../types/token.types'
import type { DoctorResponse } from '../../types/doctor'

const REFRESH_MS = 5000

export function TokenDisplayScreen() {
  const [displays, setDisplays] = useState<TokenDisplay[]>([])
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [selectedDoctorId, setSelectedDoctorId] = useState<number | null>(null)
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))

  useEffect(() => {
    doctorsApi.list({ status: 'ACTIVE', page: 0, size: 200 }).then((r) => setDoctors(r.content)).catch(() => [])
  }, [])

  const fetch = () => {
    if (selectedDoctorId != null) {
      tokenApi.getCurrent(selectedDoctorId, date).then(setDisplays)
    } else {
      tokenApi.getCurrent(undefined, date).then(setDisplays)
    }
  }

  useEffect(() => {
    fetch()
    const id = setInterval(fetch, REFRESH_MS)
    return () => clearInterval(id)
  }, [selectedDoctorId, date])

  const display = displays[0]

  return (
    <div className="min-vh-100 d-flex flex-column align-items-center justify-content-center bg-dark text-white p-5">
      <div className="text-center mb-4">
        <h1 className="display-4 fw-bold mb-2">OPD Token Display</h1>
        <p className="text-white-50">Waiting Room</p>
      </div>

      <div className="d-flex gap-3 mb-4">
        <select
          className="form-select form-select-lg bg-dark text-white border-secondary"
          style={{ width: 'auto' }}
          value={selectedDoctorId ?? ''}
          onChange={(e) => setSelectedDoctorId(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">All Doctors</option>
          {doctors.map((d) => (
            <option key={d.id} value={d.id}>{d.fullName}</option>
          ))}
        </select>
        <input
          type="date"
          className="form-control form-control-lg bg-dark text-white border-secondary"
          style={{ width: 'auto' }}
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
      </div>

      {selectedDoctorId != null && display ? (
        <div className="text-center" style={{ minWidth: 400 }}>
          <div className="mb-5">
            <p className="text-white-50 mb-1">Doctor</p>
            <h2 className="display-5">{display.doctorName}</h2>
            <p className="text-white-50">{display.roomNo}</p>
          </div>
          <div className="row g-4">
            <div className="col-6">
              <div className="p-4 rounded bg-secondary bg-opacity-25">
                <p className="text-white-50 mb-2">Current Token</p>
                <p className="display-2 fw-bold mb-0">{display.currentToken ?? '—'}</p>
              </div>
            </div>
            <div className="col-6">
              <div className="p-4 rounded bg-secondary bg-opacity-25">
                <p className="text-white-50 mb-2">Next Token</p>
                <p className="display-2 fw-bold mb-0">{display.nextToken ?? '—'}</p>
              </div>
            </div>
          </div>
        </div>
      ) : selectedDoctorId == null && displays.length > 0 ? (
        <div className="row g-4 w-100" style={{ maxWidth: 900 }}>
          {displays.filter((d) => d.doctorName && d.doctorName !== '—').map((d, i) => (
            <div key={i} className="col-md-4">
              <div className="p-4 rounded bg-secondary bg-opacity-25 text-center">
                <h5 className="mb-2">{d.doctorName}</h5>
                <p className="text-white-50 small mb-2">{d.roomNo}</p>
                <div className="d-flex justify-content-center gap-4">
                  <div>
                    <p className="text-white-50 small mb-0">Current</p>
                    <p className="h3 mb-0">{d.currentToken ?? '—'}</p>
                  </div>
                  <div>
                    <p className="text-white-50 small mb-0">Next</p>
                    <p className="h3 mb-0">{d.nextToken ?? '—'}</p>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-white-50">Select a doctor to view tokens.</p>
      )}

      <p className="text-white-50 small mt-5">Auto-refresh every {REFRESH_MS / 1000} seconds</p>
    </div>
  )
}
