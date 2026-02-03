import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { departmentsApi } from '../api/doctors'
import type { DoctorResponse, DepartmentResponse, DoctorStatus } from '../types/doctor'
import type { PageResponse } from '../types/doctor'

const STATUS_OPTIONS: { value: DoctorStatus | ''; label: string }[] = [
  { value: '', label: 'All' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'ON_LEAVE', label: 'On Leave' },
]

export function DoctorListPage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [departments, setDepartments] = useState<DepartmentResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState({
    code: '',
    departmentId: '' as number | '',
    status: '' as DoctorStatus | '',
    search: '',
    page: 0,
    size: 20,
  })
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const loadDepartments = () => {
    departmentsApi.list().then(setDepartments).catch(() => setDepartments([]))
  }

  useEffect(() => {
    loadDepartments()
  }, [])

  useEffect(() => {
    setLoading(true)
    setError('')
    doctorsApi
      .list({
        code: filters.code || undefined,
        departmentId: filters.departmentId || undefined,
        status: filters.status || undefined,
        search: filters.search || undefined,
        page: filters.page,
        size: filters.size,
      })
      .then((data: PageResponse<DoctorResponse>) => {
        setDoctors(data.content)
        setTotalPages(data.totalPages)
        setTotalElements(data.totalElements)
      })
      .catch((err) => {
        const msg = err.response?.data?.message
        setError(
          msg === 'An unexpected error occurred'
            ? 'Unable to load doctors. Please ensure the server is running and try again.'
            : msg || 'Failed to load doctors.'
        )
      })
      .finally(() => setLoading(false))
  }, [filters.code, filters.departmentId, filters.status, filters.search, filters.page, filters.size])

  const handleFilterChange = (key: string, value: string | number) => {
    setFilters((prev) => ({ ...prev, [key]: value, page: 0 }))
  }

  const retryLoad = () => {
    setLoading(true)
    loadDepartments()
    doctorsApi
      .list({
        code: filters.code || undefined,
        departmentId: filters.departmentId || undefined,
        status: filters.status || undefined,
        search: filters.search || undefined,
        page: filters.page,
        size: filters.size,
      })
      .then((data: PageResponse<DoctorResponse>) => {
        setDoctors(data.content)
        setTotalPages(data.totalPages)
        setTotalElements(data.totalElements)
        setError('')
      })
      .catch((err) => {
        const msg = err.response?.data?.message
        setError(
          msg === 'An unexpected error occurred'
            ? 'Unable to load doctors. Please ensure the server is running and try again.'
            : msg || 'Failed to load doctors.'
        )
      })
      .finally(() => setLoading(false))
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><a href="/doctors">Doctors</a></li>
          <li className="breadcrumb-item active" aria-current="page">List</li>
        </ol>
      </nav>

      <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <h2 className="h5 mb-0 fw-bold">Doctors</h2>
        <Link to="/doctors/new" className="btn btn-primary">
          Add Doctor
        </Link>
      </div>

      <div className="card shadow-sm">
        <div className="card-header">
          <h3 className="h6 mb-0 fw-bold">Filters</h3>
        </div>
        <div className="card-body">
          <div className="row g-2 align-items-end flex-wrap">
            <div className="col-auto">
              <label className="form-label small mb-0">Code</label>
              <input
                type="text"
                className="form-control form-control-sm"
                placeholder="Code"
                value={filters.code}
                onChange={(e) => handleFilterChange('code', e.target.value)}
              />
            </div>
            <div className="col-auto">
              <label className="form-label small mb-0">Department</label>
              <select
                value={filters.departmentId}
                onChange={(e) => handleFilterChange('departmentId', e.target.value ? Number(e.target.value) : '')}
                className="form-select form-select-sm"
              >
                <option value="">All departments</option>
                {departments.length === 0 && !loading ? (
                  <option value="" disabled>— No departments loaded (click Retry) —</option>
                ) : (
                  departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))
                )}
              </select>
            </div>
            <div className="col-auto">
              <label className="form-label small mb-0">Status</label>
              <select
                value={filters.status}
                onChange={(e) => handleFilterChange('status', e.target.value as DoctorStatus | '')}
                className="form-select form-select-sm"
              >
                {STATUS_OPTIONS.map((o) => (
                  <option key={o.value || 'all'} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-auto">
              <label className="form-label small mb-0">Search</label>
              <input
                type="text"
                className="form-control form-control-sm"
                placeholder="Search by name"
                value={filters.search}
                onChange={(e) => handleFilterChange('search', e.target.value)}
              />
            </div>
            <div className="col-auto">
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => setFilters((p) => ({ ...p, page: 0 }))}
              >
                Search
              </button>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger d-flex align-items-center justify-content-between flex-wrap gap-2 py-2 mb-0">
          <span>{loading ? 'Retrying…' : error}</span>
          <button type="button" className="btn btn-sm btn-outline-danger" onClick={retryLoad} disabled={loading}>
            {loading ? 'Retrying…' : 'Retry'}
          </button>
        </div>
      )}
      {loading && <p className="text-muted mb-0">Loading…</p>}

      {!loading && !error && (
        <>
          <div className="card shadow-sm">
            <div className="table-responsive">
              <table className="table table-striped mb-0">
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Department</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>On Call</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {doctors.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="text-center text-muted py-4">
                        No doctors found
                      </td>
                    </tr>
                  ) : (
                    doctors.map((d) => (
                      <tr key={d.id}>
                        <td>{d.code}</td>
                        <td>{d.fullName}</td>
                        <td>{d.departmentName}</td>
                        <td>{d.doctorType}</td>
                        <td>{d.status}</td>
                        <td>{d.onCall ? 'Yes' : 'No'}</td>
                        <td>
                          <Link to={`/doctors/${d.id}/edit`} className="text-decoration-none me-2">Edit</Link>
                          <Link to={`/doctors/${d.id}/availability`} className="text-decoration-none">Availability</Link>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
          <div className="d-flex align-items-center gap-3 flex-wrap">
            <span className="small text-muted">
              Page {filters.page + 1} of {totalPages || 1} ({totalElements} total)
            </span>
            <nav aria-label="Doctor list pagination">
              <ul className="pagination pagination-sm mb-0">
                <li className="page-item" style={{ pointerEvents: filters.page === 0 ? 'none' : undefined }}>
                  <button
                    type="button"
                    className="page-link"
                    disabled={filters.page === 0}
                    onClick={() => setFilters((p) => ({ ...p, page: p.page - 1 }))}
                  >
                    Previous
                  </button>
                </li>
                <li className="page-item" style={{ pointerEvents: filters.page >= totalPages - 1 ? 'none' : undefined }}>
                  <button
                    type="button"
                    className="page-link"
                    disabled={filters.page >= totalPages - 1}
                    onClick={() => setFilters((p) => ({ ...p, page: p.page + 1 }))}
                  >
                    Next
                  </button>
                </li>
              </ul>
            </nav>
          </div>
        </>
      )}
    </div>
  )
}
