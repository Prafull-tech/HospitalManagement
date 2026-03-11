import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { LabOrderItem, LabResult } from '../../types/lab'

export function LabResultVerificationPage() {
  const [items, setItems] = useState<LabOrderItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedItemId, setSelectedItemId] = useState<number | null>(null)
  const [results, setResults] = useState<LabResult[]>([])
  const [processingId, setProcessingId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      setItems(await labApi.getPendingVerificationItems())
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const loadResults = useCallback(async (orderItemId: number) => {
    try {
      const res = await labApi.getResultsByOrderItem(orderItemId)
      setResults(res)
    } catch {
      setResults([])
    }
  }, [])

  const handleVerify = useCallback(async (item: LabOrderItem) => {
    setProcessingId(item.id)
    try {
      await labApi.verifyResult(item.id, 'VERIFY')
      setSelectedItemId(null)
      load()
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Verification failed.')
    } finally {
      setProcessingId(null)
    }
  }, [load])

  const handleReject = useCallback(async (item: LabOrderItem) => {
    if (!confirm('Reject this result? The test will need to be repeated.')) return
    setProcessingId(item.id)
    try {
      await labApi.verifyResult(item.id, 'REJECT')
      setSelectedItemId(null)
      load()
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Reject failed.')
    } finally {
      setProcessingId(null)
    }
  }, [load])

  const openModal = (item: LabOrderItem) => {
    setSelectedItemId(item.id)
    loadResults(item.id)
  }

  const selectedItem = items.find((i) => i.id === selectedItemId)

  if (loading) return <div className="container mt-4"><div className="placeholder-glow"><span className="placeholder col-12" style={{ height: 200 }} /></div></div>

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div><h2 className="h5 mb-1 fw-bold">Result Verification</h2><p className="text-muted small mb-0">Only senior technician or pathologist can verify. Verify or reject results.</p></div>
        <Link to="/lab" className="btn btn-outline-primary btn-sm">Back to Lab Dashboard</Link>
      </div>
      {error && <div className="alert alert-danger">{error} <button type="button" className="btn btn-sm btn-outline-danger ms-2" onClick={() => { setLoading(true); load() }}>Retry</button></div>}
      {items.length === 0 ? <div className="alert alert-light border">No results pending verification.</div> : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead><tr><th>Order #</th><th>UHID</th><th>Patient</th><th>Test</th><th>Result entered</th><th>Technician</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td>{item.orderNumber ?? '—'}</td>
                  <td>{item.patientUhid ?? '—'}</td>
                  <td>{item.patientName ?? '—'}</td>
                  <td>{item.testName}</td>
                  <td>{item.resultEnteredAt ? new Date(item.resultEnteredAt).toLocaleString() : '—'}</td>
                  <td>{item.resultEnteredBy ?? '—'}</td>
                  <td>
                    <button type="button" className="btn btn-outline-primary btn-sm me-1" onClick={() => openModal(item)}>View</button>
                    <button type="button" className="btn btn-success btn-sm me-1" disabled={processingId === item.id} onClick={() => handleVerify(item)}>{processingId === item.id ? 'Verifying…' : 'Verify Result'}</button>
                    <button type="button" className="btn btn-danger btn-sm" disabled={processingId === item.id} onClick={() => handleReject(item)}>{processingId === item.id ? 'Rejecting…' : 'Reject Result'}</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selectedItemId && selectedItem && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.4)' }} role="dialog">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Result details</h5>
                <button type="button" className="btn-close" onClick={() => setSelectedItemId(null)} aria-label="Close" />
              </div>
              <div className="modal-body">
                <p className="mb-2"><strong>{selectedItem.orderNumber}</strong> · {selectedItem.patientName} · {selectedItem.testName}</p>
                {results.length > 0 ? (
                  <table className="table table-sm"><thead><tr><th>Parameter</th><th>Value</th><th>Unit</th><th>Range</th><th>Remarks</th></tr></thead><tbody>
                    {results.map((r) => <tr key={r.id}><td>{r.parameterName ?? 'Result'}</td><td>{r.resultValue}</td><td>{r.unit}</td><td>{r.normalRange}</td><td>{r.remarks}</td></tr>)}
                  </tbody></table>
                ) : <p className="text-muted small">No result details.</p>}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => setSelectedItemId(null)}>Close</button>
                <button type="button" className="btn btn-success" onClick={() => handleVerify(selectedItem)} disabled={processingId === selectedItem.id}>Verify Result</button>
                <button type="button" className="btn btn-danger" onClick={() => handleReject(selectedItem)} disabled={processingId === selectedItem.id}>Reject Result</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
