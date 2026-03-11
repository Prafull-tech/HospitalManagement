import { useState, useEffect, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { LabOrderItem } from '../../types/lab'

export function SampleProcessingPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState<LabOrderItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      setItems(await labApi.getPendingProcessingItems())
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const handleStartProcessing = useCallback(async (item: LabOrderItem) => {
    setProcessingId(item.id)
    try {
      await labApi.processSampleItem(item.id, 'START')
      if (item.testOrderId) {
        navigate(`/lab/result-entry?orderItemId=${item.id}`)
      } else {
        load()
      }
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed.')
    } finally {
      setProcessingId(null)
    }
  }, [navigate, load])

  const handleMarkProcessed = useCallback(async (item: LabOrderItem) => {
    setProcessingId(item.id)
    try {
      await labApi.processSampleItem(item.id, 'COMPLETE')
      load()
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed.')
    } finally {
      setProcessingId(null)
    }
  }, [load])

  const isCollected = (item: LabOrderItem) => item.testOrderStatus === 'COLLECTED'
  const isInProgress = (item: LabOrderItem) => item.testOrderStatus === 'IN_PROGRESS'

  if (loading) return <div className="container mt-4"><div className="placeholder-glow"><span className="placeholder col-12" style={{ height: 200 }} /></div></div>

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div><h2 className="h5 mb-1 fw-bold">Sample Processing</h2><p className="text-muted small mb-0">Samples collected but not processed.</p></div>
        <Link to="/lab" className="btn btn-outline-primary btn-sm">Back to Lab Dashboard</Link>
      </div>
      {error && <div className="alert alert-danger">{error} <button type="button" className="btn btn-sm btn-outline-danger ms-2" onClick={() => { setLoading(true); load() }}>Retry</button></div>}
      {items.length === 0 ? <div className="alert alert-light border">No samples pending processing.</div> : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead><tr><th>Order #</th><th>UHID</th><th>Patient</th><th>Test</th><th>Collected</th><th>Status</th><th>Priority</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className={item.isPriority ? 'table-danger' : ''}>
                  <td>{item.orderNumber ?? '—'}</td>
                  <td>{item.patientUhid ?? '—'}</td>
                  <td>{item.patientName ?? '—'}</td>
                  <td>{item.testName}</td>
                  <td>{item.sampleCollectedAt ? new Date(item.sampleCollectedAt).toLocaleString() : '—'}</td>
                  <td><span className={`badge ${isInProgress(item) ? 'bg-info' : 'bg-secondary'}`}>{item.status}</span></td>
                  <td>{item.isPriority ? <span className="badge bg-danger">Emergency</span> : <span className="badge bg-secondary">Normal</span>}</td>
                  <td>
                    {isCollected(item) && (
                      <button type="button" className="btn btn-primary btn-sm me-1" disabled={processingId === item.id} onClick={() => handleStartProcessing(item)}>
                        {processingId === item.id ? 'Starting…' : 'Start Processing'}
                      </button>
                    )}
                    {isInProgress(item) && (
                      <button type="button" className="btn btn-success btn-sm" disabled={processingId === item.id} onClick={() => handleMarkProcessed(item)}>
                        {processingId === item.id ? 'Marking…' : 'Mark Processed'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
