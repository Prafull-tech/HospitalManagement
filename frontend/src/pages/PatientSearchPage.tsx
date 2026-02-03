import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { receptionApi } from '../api/reception'
import type { PatientResponse } from '../types/patient'
import type { ApiError } from '../types/patient'

function SearchIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35" />
    </svg>
  )
}

export function PatientSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const initialUhid = searchParams.get('uhid') ?? ''
  const [uhid, setUhid] = useState(initialUhid)
  const [phone, setPhone] = useState('')
  const [name, setName] = useState('')
  const [results, setResults] = useState<PatientResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searched, setSearched] = useState(false)

  const [allPatients, setAllPatients] = useState<PatientResponse[]>([])
  const [allLoading, setAllLoading] = useState(true)
  const [allError, setAllError] = useState('')

  useEffect(() => {
    setUhid(initialUhid)
  }, [initialUhid])

  useEffect(() => {
    let cancelled = false
    setAllLoading(true)
    setAllError('')
    receptionApi
      .list({ page: 0, size: 500 })
      .then((data) => {
        if (!cancelled) setAllPatients(data)
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          const ax = err as { response?: { data?: ApiError } }
          setAllError(ax.response?.data?.message || 'Failed to load patient list.')
        }
      })
      .finally(() => {
        if (!cancelled) setAllLoading(false)
      })
    return () => { cancelled = true }
  }, [])

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setResults([])
    setSearched(true)
    if (!uhid.trim() && !phone.trim() && !name.trim()) {
      setError('Enter UHID, phone, or name to search.')
      return
    }
    setLoading(true)
    try {
      const data = await receptionApi.search({
        uhid: uhid.trim() || undefined,
        phone: phone.trim() || undefined,
        name: name.trim() || undefined,
      })
      setResults(data)
    } catch (err: unknown) {
      const ax = err as { response?: { data?: ApiError } }
      setError(ax.response?.data?.message || 'Search failed.')
    } finally {
      setLoading(false)
    }
  }

  const clearSearch = () => {
    setUhid('')
    setPhone('')
    setName('')
    setResults([])
    setError('')
    setSearched(false)
    setSearchParams({})
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><a href="/reception">Reception</a></li>
          <li className="breadcrumb-item active" aria-current="page">Search Patient</li>
        </ol>
      </nav>

      <div className="card shadow-sm">
        <div className="card-header">
          <h2 className="h6 mb-0 fw-bold">Search</h2>
        </div>
        <div className="card-body">
          <form onSubmit={handleSearch} className="d-flex flex-column gap-3">
            {error && (
              <div className="alert alert-danger py-2 mb-0" role="alert">{error}</div>
            )}
            <div className="row g-2">
              <div className="col-md-4">
                <label className="form-label small fw-medium">UHID</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={uhid}
                  onChange={(e) => setUhid(e.target.value)}
                  placeholder="e.g. HMS-2025-000001"
                />
              </div>
              <div className="col-md-4">
                <label className="form-label small fw-medium">Phone</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="Phone number"
                />
              </div>
              <div className="col-md-4">
                <label className="form-label small fw-medium">Name (partial)</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Full or partial name"
                />
              </div>
            </div>
            <div className="d-flex gap-2">
              <button type="submit" disabled={loading} className="btn btn-primary">
                <span className="me-1"><SearchIcon /></span>
                {loading ? 'Searching…' : 'Search'}
              </button>
              <button type="button" onClick={clearSearch} className="btn btn-outline-secondary">
                Clear
              </button>
            </div>
          </form>
        </div>
      </div>

      {searched && !loading && (
        <div className="card shadow-sm">
          <div className="card-header bg-primary text-white">
            <h2 className="h6 mb-0 fw-bold">
              {results.length === 0 ? 'No patients found' : `${results.length} patient(s) found`}
            </h2>
          </div>
          {results.length > 0 && (
            <div className="card-body">
              <ul className="list-group list-group-flush list-group-numbered">
                {results.map((p) => (
                  <li key={p.id} className="list-group-item d-flex flex-column gap-1">
                    <div className="d-flex align-items-center gap-2">
                      <span className="fw-semibold text-primary">{p.uhid}</span>
                      <span className="fw-medium">{p.fullName}</span>
                    </div>
                    <small className="text-muted">
                      {p.age} yrs · {p.gender}
                      {p.phone && ` · ${p.phone}`}
                    </small>
                    {p.address && <small className="text-muted">{p.address}</small>}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      <div className="card shadow-sm">
        <div className="card-header bg-primary text-white">
          <h2 className="h6 mb-0 fw-bold">
            All patients
            {!allLoading && !allError && ` (${allPatients.length})`}
          </h2>
        </div>
        <div className="card-body">
          {allError && <div className="alert alert-danger py-2 mb-0" role="alert">{allError}</div>}
          {allLoading && <p className="text-muted mb-0">Loading patient list…</p>}
          {!allLoading && !allError && allPatients.length === 0 && (
            <p className="text-muted mb-0">No patients registered yet.</p>
          )}
          {!allLoading && !allError && allPatients.length > 0 && (
            <ul className="list-group list-group-flush list-group-numbered">
              {allPatients.map((p) => (
                <li key={p.id} className="list-group-item d-flex flex-column gap-1">
                  <div className="d-flex align-items-center gap-2">
                    <span className="fw-semibold text-primary">{p.uhid}</span>
                    <span className="fw-medium">{p.fullName}</span>
                  </div>
                  <small className="text-muted">
                    {p.age} yrs · {p.gender}
                    {p.phone && ` · ${p.phone}`}
                  </small>
                  {p.address && <small className="text-muted">{p.address}</small>}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  )
}
