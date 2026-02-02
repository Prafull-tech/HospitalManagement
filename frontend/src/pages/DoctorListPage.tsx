import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { departmentsApi } from '../api/doctors'
import type { DoctorResponse, DepartmentResponse, DoctorStatus } from '../types/doctor'
import type { PageResponse } from '../types/doctor'
import styles from './DoctorListPage.module.css'

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
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <Link to="/doctors/new" className={styles.addBtn}>
          Add Doctor
        </Link>
      </div>

      <div className={styles.filters}>
        <input
          type="text"
          placeholder="Code"
          value={filters.code}
          onChange={(e) => handleFilterChange('code', e.target.value)}
          className={styles.input}
        />
        <select
          value={filters.departmentId}
          onChange={(e) => handleFilterChange('departmentId', e.target.value ? Number(e.target.value) : '')}
          className={styles.select}
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
        <select
          value={filters.status}
          onChange={(e) => handleFilterChange('status', e.target.value as DoctorStatus | '')}
          className={styles.select}
        >
          {STATUS_OPTIONS.map((o) => (
            <option key={o.value || 'all'} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <input
          type="text"
          placeholder="Search by name"
          value={filters.search}
          onChange={(e) => handleFilterChange('search', e.target.value)}
          className={styles.input}
        />
        <button type="button" onClick={() => setFilters((p) => ({ ...p, page: 0 }))} className={styles.searchBtn}>
          Search
        </button>
      </div>

      {error && (
        <div className={styles.error}>
          <span>{loading ? 'Retrying…' : error}</span>
          <button
            type="button"
            onClick={retryLoad}
            className={styles.retryBtn}
            disabled={loading}
          >
            {loading ? 'Retrying…' : 'Retry'}
          </button>
        </div>
      )}
      {loading && <div className={styles.loading}>Loading…</div>}

      {!loading && !error && (
        <>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
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
                    <td colSpan={7} className={styles.empty}>
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
                        <Link to={`/doctors/${d.id}/edit`} className={styles.link}>
                          Edit
                        </Link>
                        {' | '}
                        <Link to={`/doctors/${d.id}/availability`} className={styles.link}>
                          Availability
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <div className={styles.pagination}>
            <span>
              Page {filters.page + 1} of {totalPages || 1} ({totalElements} total)
            </span>
            <button
              type="button"
              disabled={filters.page === 0}
              onClick={() => setFilters((p) => ({ ...p, page: p.page - 1 }))}
              className={styles.pageBtn}
            >
              Previous
            </button>
            <button
              type="button"
              disabled={filters.page >= totalPages - 1}
              onClick={() => setFilters((p) => ({ ...p, page: p.page + 1 }))}
              className={styles.pageBtn}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  )
}
