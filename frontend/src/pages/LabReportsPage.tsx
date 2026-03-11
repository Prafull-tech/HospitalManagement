import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { labApi } from '../api/lab'
import type { LabReport } from '../types/lab'

export function LabReportsPage() {
  const [reports, setReports] = useState<LabReport[]>([])
  const [loading, setLoading] = useState(true)
  const [uhid, setUhid] = useState('')
  const [patientName, setPatientName] = useState('')
  const [testName, setTestName] = useState('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  const handleSearch = useCallback(async () => {
    setLoading(true)
    try {
      const list = await labApi.searchReports({
        uhid: uhid.trim() || undefined,
        patientName: patientName.trim() || undefined,
        testName: testName.trim() || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
      })
      setReports(list)
    } catch (err: unknown) {
      setReports([])
    } finally {
      setLoading(false)
    }
  }, [uhid, patientName, testName, fromDate, toDate])

  useEffect(() => {
    handleSearch()
  }, [])

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h5 mb-1 fw-bold">View Reports</h2>
          <p className="text-muted small mb-0">Search lab reports by UHID, patient name, date, or test name.</p>
        </div>
        <Link to="/lab" className="btn btn-outline-primary btn-sm">Back to Lab Dashboard</Link>
      </div>

      <div className="card shadow-sm mb-4">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-2"><input className="form-control form-control-sm" placeholder="UHID" value={uhid} onChange={(e) => setUhid(e.target.value)} /></div>
            <div className="col-md-2"><input className="form-control form-control-sm" placeholder="Patient name" value={patientName} onChange={(e) => setPatientName(e.target.value)} /></div>
            <div className="col-md-2"><input className="form-control form-control-sm" placeholder="Test name" value={testName} onChange={(e) => setTestName(e.target.value)} /></div>
            <div className="col-md-2"><input type="date" className="form-control form-control-sm" value={fromDate} onChange={(e) => setFromDate(e.target.value)} /></div>
            <div className="col-md-2"><input type="date" className="form-control form-control-sm" value={toDate} onChange={(e) => setToDate(e.target.value)} /></div>
            <div className="col-md-2"><button type="button" className="btn btn-primary btn-sm w-100" onClick={handleSearch} disabled={loading}>{loading ? 'Searching…' : 'Search'}</button></div>
          </div>
        </div>
      </div>

      {loading ? <div className="placeholder-glow"><span className="placeholder col-12" style={{ height: 100 }} /></div> : reports.length === 0 ? (
        <div className="alert alert-light border">No reports found.</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead><tr><th>Report #</th><th>Order #</th><th>Patient</th><th>UHID</th><th>Test</th><th>Status</th><th>Released</th><th>Actions</th></tr></thead>
            <tbody>
              {reports.map((r) => (
                <tr key={r.id}>
                  <td>{r.reportNumber}</td><td>{r.orderNumber}</td><td>{r.patientName ?? '—'}</td><td>{r.patientUhid ?? '—'}</td><td>{r.testName ?? '—'}</td><td><span className="badge bg-secondary">{r.status}</span></td><td>{r.releasedAt ? new Date(r.releasedAt).toLocaleString() : '—'}</td>
                  <td><Link to={`/lab/report/${r.testOrderId}`} className="btn btn-outline-primary btn-sm">View</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
