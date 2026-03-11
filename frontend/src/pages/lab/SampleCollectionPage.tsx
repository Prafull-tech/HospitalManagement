import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { TestOrder, SampleCollectionRequest } from '../../types/lab'

function CollectSampleModal({ order, onClose, onSuccess }: { order: TestOrder; onClose: () => void; onSuccess: () => void }) {
  const [wardName, setWardName] = useState(order.wardName ?? '')
  const [bedNumber, setBedNumber] = useState(order.bedNumber ?? '')
  const [remarks, setRemarks] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const req: SampleCollectionRequest = { testOrderId: order.id, wardName: wardName.trim() || undefined, bedNumber: bedNumber.trim() || undefined, remarks: remarks.trim() || undefined }
      await labApi.collectSample(order.id, req)
      onSuccess()
      onClose()
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to mark sample collected.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.4)' }} role="dialog">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Mark sample collected</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              <p className="small text-muted mb-2">{order.orderNumber} · {order.patientName} · {order.testName}</p>
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <div className="mb-2">
                <label className="form-label form-label-sm">Ward (optional)</label>
                <input className="form-control form-control-sm" value={wardName} onChange={(e) => setWardName(e.target.value)} placeholder="e.g. General Ward" />
              </div>
              <div className="mb-2">
                <label className="form-label form-label-sm">Bed (optional)</label>
                <input className="form-control form-control-sm" value={bedNumber} onChange={(e) => setBedNumber(e.target.value)} placeholder="e.g. B-12" />
              </div>
              <div>
                <label className="form-label form-label-sm">Remarks (optional)</label>
                <textarea className="form-control form-control-sm" rows={2} value={remarks} onChange={(e) => setRemarks(e.target.value)} placeholder="Any remarks" />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={saving}>Cancel</button>
              <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>{saving ? 'Saving…' : 'Mark collected'}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

function RejectSampleModal({ order, onClose, onSuccess }: { order: TestOrder; onClose: () => void; onSuccess: () => void }) {
  const [reason, setReason] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!reason.trim()) { setError('Rejection reason is required.'); return }
    setSaving(true)
    setError('')
    try {
      await labApi.rejectSample(order.id, reason.trim())
      onSuccess()
      onClose()
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to reject sample.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.4)' }} role="dialog">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Reject sample</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              <p className="small text-muted mb-2">{order.orderNumber} · {order.patientName} · {order.testName}</p>
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <div>
                <label className="form-label form-label-sm">Rejection reason <span className="text-danger">*</span></label>
                <textarea className="form-control form-control-sm" rows={3} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="e.g. Hemolysed sample" required />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={saving}>Cancel</button>
              <button type="submit" className="btn btn-danger btn-sm" disabled={saving || !reason.trim()}>{saving ? 'Rejecting…' : 'Reject sample'}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export function SampleCollectionPage() {
  const [orders, setOrders] = useState<TestOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [collectOrder, setCollectOrder] = useState<TestOrder | null>(null)
  const [rejectOrder, setRejectOrder] = useState<TestOrder | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      setOrders(await labApi.getPendingCollection())
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  if (loading) return <div className="container mt-4"><div className="placeholder-glow"><span className="placeholder col-12" style={{ height: 200 }} /></div></div>

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div><h2 className="h5 mb-1 fw-bold">Sample Collection</h2><p className="text-muted small mb-0">Orders awaiting sample collection.</p></div>
        <Link to="/lab" className="btn btn-outline-primary btn-sm">Back to Lab Dashboard</Link>
      </div>
      {error && <div className="alert alert-danger">{error} <button type="button" className="btn btn-sm btn-outline-danger ms-2" onClick={() => { setLoading(true); load() }}>Retry</button></div>}
      {orders.length === 0 ? <div className="alert alert-light border">No pending samples.</div> : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead><tr><th>Order #</th><th>UHID</th><th>Patient</th><th>Test</th><th>Specimen</th><th>Ordered</th><th>Priority</th><th>Actions</th></tr></thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td>{o.orderNumber}</td><td>{o.patientUhid}</td><td>{o.patientName}</td><td>{o.testName}</td><td>{o.sampleType ?? '—'}</td>
                  <td>{new Date(o.orderedAt).toLocaleString()}</td><td>{o.isPriority ? <span className="badge bg-danger">Emergency</span> : <span className="badge bg-secondary">Normal</span>}</td>
                  <td><button type="button" className="btn btn-success btn-sm me-1" onClick={() => setCollectOrder(o)}>Collect</button><button type="button" className="btn btn-outline-danger btn-sm" onClick={() => setRejectOrder(o)}>Reject</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {collectOrder && <CollectSampleModal order={collectOrder} onClose={() => setCollectOrder(null)} onSuccess={load} />}
      {rejectOrder && <RejectSampleModal order={rejectOrder} onClose={() => setRejectOrder(null)} onSuccess={load} />}
    </div>
  )
}
