/**
 * OPD Queue – all visits listed below with optional filters (doctor, date, status, patient, visit number).
 * UI aligned with /ipd/hospital-beds: breadcrumb, cards, Bootstrap table, form controls.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { opdApi } from '../api/opd'
import type { OPDVisitResponse, VisitStatus } from '../types/opd'
import type { DoctorResponse } from '../types/doctor'

const PAGE_SIZE = 500

const STATUS_OPTIONS: { value: VisitStatus | ''; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'REGISTERED', label: 'Registered' },
  { value: 'IN_CONSULTATION', label: 'In consultation' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'REFERRED', label: 'Referred' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

function statusBadgeClass(s: VisitStatus): string {
  switch (s) {
    case 'REGISTERED':
      return 'bg-primary bg-opacity-10 text-primary'
    case 'IN_CONSULTATION':
      return 'bg-warning bg-opacity-10 text-warning'
    case 'COMPLETED':
      return 'bg-success bg-opacity-10 text-success'
    case 'REFERRED':
      return 'bg-info bg-opacity-10 text-info'
    case 'CANCELLED':
      return 'bg-danger bg-opacity-10 text-danger'
    default:
      return 'bg-secondary bg-opacity-10 text-secondary'
  }
}

export function OPDQueuePage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [visits, setVisits] = useState<OPDVisitResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [filterDoctorId, setFilterDoctorId] = useState<number | ''>('')
  const [filterDate, setFilterDate] = useState('')
  const [filterStatus, setFilterStatus] = useState<VisitStatus | ''>('')
  const [filterPatientName, setFilterPatientName] = useState('')
  const [filterVisitNumber, setFilterVisitNumber] = useState('')

  const hasFilters =
    !!filterDoctorId ||
    !!filterDate ||
    !!filterStatus ||
    !!filterPatientName.trim() ||
    !!filterVisitNumber.trim()

  const loadVisits = (params: {
    doctorId?: number
    visitDate?: string
    status?: VisitStatus
    patientName?: string
    visitNumber?: string
  }) => {
    setLoading(true)
    setError(null)
    opdApi
      .search({
        page: 0,
        size: PAGE_SIZE,
        ...(params.doctorId && { doctorId: params.doctorId }),
        ...(params.visitDate && { visitDate: params.visitDate }),
        ...(params.status && { status: params.status }),
        ...(params.patientName?.trim() && { patientName: params.patientName.trim() }),
        ...(params.visitNumber?.trim() && { visitNumber: params.visitNumber.trim() }),
      })
      .then((data) => setVisits(data.content))
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load visits')
        setVisits([])
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    doctorsApi
      .list({ status: 'ACTIVE', page: 0, size: 200 })
      .then((data) => setDoctors(data.content))
      .catch(() => setDoctors([]))
  }, [])

  useEffect(() => {
    loadVisits({})
  }, [])

  const handleApplyFilters = (e: React.FormEvent) => {
    e.preventDefault()
    loadVisits({
      doctorId: filterDoctorId || undefined,
      visitDate: filterDate || undefined,
      status: filterStatus || undefined,
      patientName: filterPatientName || undefined,
      visitNumber: filterVisitNumber || undefined,
    })
  }

  const handleClearFilters = () => {
    setFilterDoctorId('')
    setFilterDate('')
    setFilterStatus('')
    setFilterPatientName('')
    setFilterVisitNumber('')
    loadVisits({})
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/opd">OPD</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Queue
          </li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">OPD Visits / Queue</h1>
        <Link to="/opd/register" className="btn btn-outline-primary btn-sm">
          Register visit
        </Link>
      </div>

      {/* Filters */}
      <div className="card border shadow-sm">
        <div className="card-body">
          <h6 className="card-title mb-3">Filter by details</h6>
          <form onSubmit={handleApplyFilters} className="row g-2 g-md-3">
            <div className="col-12 col-md-6 col-lg">
              <label htmlFor="opd-filter-doctor" className="form-label small mb-1">
                Doctor
              </label>
              <select
                id="opd-filter-doctor"
                className="form-select form-select-sm"
                value={filterDoctorId || ''}
                onChange={(e) => setFilterDoctorId(e.target.value ? Number(e.target.value) : '')}
              >
                <option value="">Any doctor</option>
                {doctors.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.fullName} — {d.departmentName}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-12 col-md-6 col-lg">
              <label htmlFor="opd-filter-date" className="form-label small mb-1">
                Visit date
              </label>
              <input
                id="opd-filter-date"
                type="date"
                className="form-control form-control-sm"
                value={filterDate}
                onChange={(e) => setFilterDate(e.target.value)}
              />
            </div>
            <div className="col-12 col-md-6 col-lg">
              <label htmlFor="opd-filter-status" className="form-label small mb-1">
                Status
              </label>
              <select
                id="opd-filter-status"
                className="form-select form-select-sm"
                value={filterStatus}
                onChange={(e) => setFilterStatus((e.target.value || '') as VisitStatus | '')}
              >
                {STATUS_OPTIONS.map((o) => (
                  <option key={o.value || 'any'} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-12 col-md-6 col-lg">
              <label htmlFor="opd-filter-name" className="form-label small mb-1">
                Patient name (partial)
              </label>
              <input
                id="opd-filter-name"
                type="text"
                className="form-control form-control-sm"
                placeholder="Name"
                value={filterPatientName}
                onChange={(e) => setFilterPatientName(e.target.value)}
              />
            </div>
            <div className="col-12 col-md-6 col-lg">
              <label htmlFor="opd-filter-visitno" className="form-label small mb-1">
                Visit number
              </label>
              <input
                id="opd-filter-visitno"
                type="text"
                className="form-control form-control-sm"
                placeholder="e.g. OPD-2025-00001"
                value={filterVisitNumber}
                onChange={(e) => setFilterVisitNumber(e.target.value)}
              />
            </div>
            <div className="col-12 col-lg-auto d-flex align-items-end gap-2">
              <button type="submit" className="btn btn-primary btn-sm" disabled={loading}>
                {loading ? 'Loading…' : 'Apply'}
              </button>
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={handleClearFilters}>
                Clear
              </button>
            </div>
          </form>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger d-flex align-items-center justify-content-between flex-wrap gap-2" role="alert">
          <span>{error}</span>
          <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => setError(null)}>
            Dismiss
          </button>
        </div>
      )}

      {/* Results table */}
      <div className="card border shadow-sm">
        <div className="card-header bg-light py-2 d-flex align-items-center justify-content-between">
          <h6 className="mb-0">
            {hasFilters ? `Filtered results (${visits.length})` : `All visits (${visits.length})`}
          </h6>
          <button
            type="button"
            className="btn btn-outline-primary btn-sm"
            onClick={() => loadVisits({
              doctorId: filterDoctorId || undefined,
              visitDate: filterDate || undefined,
              status: filterStatus || undefined,
              patientName: filterPatientName || undefined,
              visitNumber: filterVisitNumber || undefined,
            })}
            disabled={loading}
          >
            {loading ? 'Loading…' : 'Refresh'}
          </button>
        </div>
        {loading ? (
          <div className="card-body">
            <div className="placeholder-glow">
              <span className="placeholder col-12" />
              <span className="placeholder col-12" />
              <span className="placeholder col-12" />
            </div>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table table-striped mb-0">
              <thead className="table-light">
                <tr>
                  <th>Token</th>
                  <th>Visit No</th>
                  <th>Patient</th>
                  <th>UHID</th>
                  <th>Visit Date</th>
                  <th>Doctor</th>
                  <th>Status</th>
                  <th className="text-end">Action</th>
                </tr>
              </thead>
              <tbody>
                {visits.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="text-center text-muted py-4">
                      {hasFilters
                        ? 'No visits match the current filters. Try clearing filters.'
                        : 'No OPD visits yet.'}
                    </td>
                  </tr>
                ) : (
                  visits.map((v) => (
                    <tr key={v.id}>
                      <td className="fw-semibold">{v.tokenNumber ?? '—'}</td>
                      <td>{v.visitNumber}</td>
                      <td>{v.patientName}</td>
                      <td className="text-muted small">{v.patientUhid}</td>
                      <td>{v.visitDate ?? '—'}</td>
                      <td>{v.doctorName ?? '—'}</td>
                      <td>
                        <span className={`badge ${statusBadgeClass(v.visitStatus)}`}>
                          {v.visitStatus.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td className="text-end">
                        <Link to={`/opd/visits/${v.id}`} className="btn btn-sm btn-outline-primary">
                          Open
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
