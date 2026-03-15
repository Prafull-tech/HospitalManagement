import { useState, useEffect, useRef } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { receptionApi } from '../../api/reception'
import type { PatientResponse } from '../../types/patient'

export function ReceptionPatientsListPage() {
  const [searchParams] = useSearchParams()
  const from = searchParams.get('from') ?? new Date().toISOString().slice(0, 10)
  const to = searchParams.get('to') ?? new Date().toISOString().slice(0, 10)
  const [list, setList] = useState<PatientResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const tableRef = useRef<HTMLTableElement>(null)

  useEffect(() => {
    setLoading(true)
    setError('')
    receptionApi
      .list({ page: 0, size: 1000, from, to })
      .then(setList)
      .catch(() => setError('Failed to load patients.'))
      .finally(() => setLoading(false))
  }, [from, to])

  const handlePrint = () => {
    if (!tableRef.current) return
    const win = window.open('', '_blank')
    if (!win) return
    win.document.write(`
      <!DOCTYPE html><html><head><title>Patients Registered</title>
      <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
      <style>body{ padding: 1rem; font-size: 14px; }</style></head><body>
      <h5>Patients Registered (${from} to ${to})</h5>
      <p>Total: ${list.length}</p>
      ${tableRef.current.outerHTML}
      <script>window.print(); window.close();</script></body></html>
    `)
    win.document.close()
  }

  const handleCSV = () => {
    const headers = ['UHID', 'Patient Name', 'Gender', 'Phone', 'Registration Date']
    const rows = list.map((p) => [
      p.uhid,
      p.fullName,
      p.gender ?? '',
      p.phone ?? '',
      p.registrationDate ?? '',
    ])
    const csv = [headers.join(','), ...rows.map((r) => r.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(','))].join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `patients-${from}-to-${to}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/reception">Reception</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Patients Registered</li>
        </ol>
      </nav>
      <div className="card shadow-sm">
        <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
          <h2 className="h6 mb-0 fw-bold">Patients Registered ({from} to {to})</h2>
          <div className="d-flex gap-2">
            <button type="button" className="btn btn-sm btn-outline-secondary" onClick={handlePrint} disabled={list.length === 0}>Print</button>
            <button type="button" className="btn btn-sm btn-outline-primary" onClick={handleCSV} disabled={list.length === 0}>Download CSV</button>
            <Link to="/reception" className="btn btn-sm btn-outline-secondary">Back to Dashboard</Link>
          </div>
        </div>
        <div className="card-body">
          {error && <div className="alert alert-danger py-2 mb-0" role="alert">{error}</div>}
          {loading && <p className="text-muted mb-0">Loading…</p>}
          {!loading && !error && list.length === 0 && <p className="text-muted mb-0">No patients in this date range.</p>}
          {!loading && !error && list.length > 0 && (
            <div className="table-responsive">
              <table ref={tableRef} className="table table-sm table-striped">
                <thead>
                  <tr>
                    <th>UHID</th>
                    <th>Patient Name</th>
                    <th>Gender</th>
                    <th>Phone</th>
                    <th>Registration Date</th>
                  </tr>
                </thead>
                <tbody>
                  {list.map((p) => (
                    <tr key={p.id}>
                      <td><Link to={`/reception/patient/${p.id}`}>{p.uhid}</Link></td>
                      <td>{p.fullName}</td>
                      <td>{p.gender ?? '—'}</td>
                      <td>{p.phone ?? '—'}</td>
                      <td>{p.registrationDate ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
