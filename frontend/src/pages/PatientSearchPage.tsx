import { useState, useEffect, useRef } from 'react'
import { useSearchParams, Link, useNavigate } from 'react-router-dom'
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
  const navigate = useNavigate()
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
  const [disablingId, setDisablingId] = useState<number | null>(null)
  const resultsPrintRef = useRef<HTMLDivElement>(null)

  const refreshAllPatients = () => {
    setAllLoading(true)
    receptionApi
      .list({ page: 0, size: 500 })
      .then(setAllPatients)
      .catch(() => setAllError('Failed to refresh list.'))
      .finally(() => setAllLoading(false))
  }

  const handleDisable = async (p: PatientResponse) => {
    if (!window.confirm(`Disable patient "${p.fullName}" (${p.uhid})? They will no longer appear in search/list.`)) return
    setDisablingId(p.id)
    try {
      await receptionApi.disable(p.id)
      setResults((prev) => prev.filter((x) => x.id !== p.id))
      refreshAllPatients()
    } catch {
      setError('Failed to disable patient.')
    } finally {
      setDisablingId(null)
    }
  }

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

  function PatientsTable({ patients }: { patients: PatientResponse[] }) {
    return (
      <div className="table-responsive">
        <table className="table table-sm table-hover table-striped align-middle mb-0">
          <thead className="table-light">
            <tr>
              <th scope="col">ID</th>
              <th scope="col">Name</th>
              <th scope="col">Number</th>
              <th scope="col">Gender</th>
              <th scope="col" className="text-end no-print">Actions</th>
            </tr>
          </thead>
          <tbody>
            {patients.map((p) => (
              <tr key={p.id}>
                <td>
                  <Link to={`/reception/patient/${p.id}`} className="fw-semibold text-primary text-decoration-none">{p.uhid}</Link>
                </td>
                <td>{p.fullName}</td>
                <td>{p.phone ?? '—'}</td>
                <td>{p.gender ?? '—'}</td>
                <td className="text-end no-print">
                  <div className="d-flex gap-1 justify-content-end">
                    <button type="button" className="btn btn-sm btn-outline-primary" onClick={() => navigate(`/reception/patient/${p.id}`)}>View</button>
                    <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => navigate(`/reception/patient/${p.id}/edit`)}>Edit</button>
                    <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => handleDisable(p)} disabled={disablingId === p.id}>{disablingId === p.id ? 'Disabling…' : 'Disable'}</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )
  }

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

  const handlePrintPage = () => {
    const prevTitle = document.title
    document.title = 'Search Patient - Hospital Management System'
    window.print()
    document.title = prevTitle
  }

  const handlePrintResults = () => {
    if (!resultsPrintRef.current) return
    const prevTitle = document.title
    document.title = `Patient Search Results - ${results.length} patient(s) found`
    const printContent = resultsPrintRef.current.innerHTML
    const win = window.open('', '_blank')
    if (!win) return
    win.document.write(`
      <!DOCTYPE html>
      <html>
        <head><title>${document.title}</title>
          <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
          <style>body { padding: 1rem; font-size: 14px; } table { width: 100%; } a { color: #0d6efd; text-decoration: none; }</style>
        </head>
        <body>
          <h5 class="text-primary mb-3">${results.length} patient(s) found</h5>
          ${printContent}
          <script>window.print(); window.close();</script>
        </body>
      </html>
    `)
    win.document.close()
    document.title = prevTitle
  }

  return (
    <div className="d-flex flex-column gap-3">
      <h1 className="print-only h5 mb-2">Patient List – Search Patient</h1>
      <nav aria-label="Breadcrumb" className="no-print">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><a href="/reception">Reception</a></li>
          <li className="breadcrumb-item active" aria-current="page">Search Patient</li>
        </ol>
      </nav>

      <div className="card shadow-sm no-print">
        <div className="card-header d-flex align-items-center justify-content-between flex-wrap gap-2">
          <h2 className="h6 mb-0 fw-bold">Search</h2>
          <button
            type="button"
            className="btn btn-outline-secondary btn-sm"
            onClick={handlePrintPage}
            aria-label="Print full page"
          >
            Print page
          </button>
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
          <div className="card-header bg-primary text-white d-flex align-items-center justify-content-between flex-wrap gap-2">
            <h2 className="h6 mb-0 fw-bold">
              {results.length === 0 ? 'No patients found' : `${results.length} patient(s) found`}
            </h2>
            {results.length > 0 && (
              <button type="button" className="btn btn-light btn-sm no-print" onClick={handlePrintResults} aria-label="Print search results">
                Print results
              </button>
            )}
          </div>
          {results.length > 0 && (
            <div className="card-body" ref={resultsPrintRef}>
              <PatientsTable patients={results} />
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
            <PatientsTable patients={allPatients} />
          )}
        </div>
      </div>
    </div>
  )
}
