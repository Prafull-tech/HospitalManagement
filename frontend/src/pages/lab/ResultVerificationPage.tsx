import { useState, useEffect, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { TestOrder, LabResult, LabReport } from '../../types/lab'

export function ResultVerificationPage() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState<TestOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [verifyOrderId, setVerifyOrderId] = useState<number | null>(null)
  const [signature, setSignature] = useState('')
  const [report, setReport] = useState<LabReport | null>(null)
  const [results, setResults] = useState<LabResult[]>([])

  const load = useCallback(async () => {
    setError(null)
    try {
      setOrders(await labApi.getPendingVerification())
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const loadOrderDetails = useCallback(async (orderId: number) => {
    try {
      const [rpt, res] = await Promise.all([labApi.getReportByOrder(orderId).catch(() => null), labApi.getResultsByOrder(orderId)])
      setReport(rpt)
      setResults(res)
    } catch {
      setReport(null)
      setResults([])
    }
  }, [])

  const handleVerify = useCallback(async (orderId: number) => {
    if (!signature.trim()) { alert('Supervisor signature is required.'); return }
    try {
      const rpt = await labApi.getReportByOrder(orderId)
      await labApi.verifyReport(rpt.id, signature.trim())
      setVerifyOrderId(null)
      setSignature('')
      load()
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Verification failed.')
    }
  }, [signature, load])

  const handleRelease = useCallback(async (orderId: number) => {
    try {
      const rpt = await labApi.getReportByOrder(orderId)
      await labApi.releaseReport(rpt.id)
      setVerifyOrderId(null)
      load()
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Release failed.')
    }
  }, [load])

  const handleGenerateReport = useCallback(async (orderId: number) => {
    try {
      await labApi.generateReport(orderId)
      loadOrderDetails(orderId)
      load()
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to generate report.')
    }
  }, [loadOrderDetails, load])

  if (loading) return <div className="container mt-4"><div className="placeholder-glow"><span className="placeholder col-12" style={{ height: 200 }} /></div></div>

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div><h2 className="h5 mb-1 fw-bold">Result Verification</h2><p className="text-muted small mb-0">Senior technician / pathologist verification. Verify or reject results.</p></div>
        <Link to="/lab" className="btn btn-outline-primary btn-sm">Back to Lab Dashboard</Link>
      </div>
      {error && <div className="alert alert-danger">{error} <button type="button" className="btn btn-sm btn-outline-danger ms-2" onClick={() => { setLoading(true); load() }}>Retry</button></div>}
      {orders.length === 0 ? <div className="alert alert-light border">No tests pending verification.</div> : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead><tr><th>Order #</th><th>Patient</th><th>Test</th><th>Result entered</th><th>Technician</th><th>Actions</th></tr></thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td>{o.orderNumber}</td><td>{o.patientName}</td><td>{o.testName}</td>
                  <td>{o.resultEnteredAt ? new Date(o.resultEnteredAt).toLocaleString() : '—'}</td><td>{o.resultEnteredBy ?? '—'}</td>
                  <td>
                    <button type="button" className="btn btn-outline-primary btn-sm me-1" onClick={() => { setVerifyOrderId(o.id); loadOrderDetails(o.id) }}>View</button>
                    <button type="button" className="btn btn-success btn-sm" onClick={() => navigate(`/lab/report/${o.id}`)}>View Report</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {verifyOrderId && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.4)' }} role="dialog">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Verify / Release</h5>
                <button type="button" className="btn-close" onClick={() => { setVerifyOrderId(null); setSignature('') }} aria-label="Close" />
              </div>
              <div className="modal-body">
                {results.length > 0 && (
                  <div className="mb-3">
                    <h6>Results</h6>
                    <table className="table table-sm"><thead><tr><th>Parameter</th><th>Value</th><th>Unit</th><th>Range</th><th>Flag</th></tr></thead><tbody>
                      {results.map((r) => <tr key={r.id}><td>{r.parameterName}</td><td>{r.resultValue}</td><td>{r.unit}</td><td>{r.normalRange}</td><td>{r.flag ? <span className="badge bg-warning">{r.flag}</span> : '—'}</td></tr>)}
                    </tbody></table>
                  </div>
                )}
                {!report && <button type="button" className="btn btn-outline-secondary btn-sm mb-2" onClick={() => handleGenerateReport(verifyOrderId)}>Generate Report First</button>}
                {report && report.status === 'DRAFT' && (
                  <div className="mb-2">
                    <label className="form-label">Supervisor signature</label>
                    <input className="form-control" value={signature} onChange={(e) => setSignature(e.target.value)} placeholder="Your name" />
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => { setVerifyOrderId(null); setSignature('') }}>Close</button>
                {report?.status === 'DRAFT' && <button type="button" className="btn btn-primary" onClick={() => handleVerify(verifyOrderId)} disabled={!signature.trim()}>Verify</button>}
                {report?.status === 'VERIFIED' && <button type="button" className="btn btn-success" onClick={() => handleRelease(verifyOrderId)}>Release Report</button>}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
