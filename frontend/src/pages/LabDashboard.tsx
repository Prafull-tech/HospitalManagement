import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { labApi } from '../api/lab'
import type {
  LabDashboardResponse,
  LabDashboardSummary,
  TestOrder,
  SampleCollectionRequest,
} from '../types/lab'

const SAFETY_TIMEOUT_MS = 8000

function LabDashboardSkeleton() {
  return (
    <div className="container mt-4">
      <h2 className="mb-4">Laboratory Dashboard</h2>
      <div className="row mb-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="col-md-3">
            <div className="card">
              <div className="card-body">
                <div className="placeholder-glow">
                  <span className="placeholder col-6" />
                  <span className="placeholder col-4 d-block mt-2" />
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
      <div className="placeholder-glow">
        <span className="placeholder col-12" style={{ height: 200 }} />
      </div>
    </div>
  )
}

function CollectSampleModal({
  order,
  onClose,
  onSuccess,
}: {
  order: TestOrder
  onClose: () => void
  onSuccess: () => void
}) {
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
      const req: SampleCollectionRequest = {
        testOrderId: order.id,
        wardName: wardName.trim() || undefined,
        bedNumber: bedNumber.trim() || undefined,
        remarks: remarks.trim() || undefined,
      }
      await labApi.collectSample(order.id, req)
      onSuccess()
      onClose()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to mark sample collected.'
      setError(msg)
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
              <p className="small text-muted mb-2">
                {order.orderNumber} · {order.patientName} · {order.testName}
              </p>
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <div className="mb-2">
                <label className="form-label form-label-sm">Ward (optional)</label>
                <input
                  className="form-control form-control-sm"
                  value={wardName}
                  onChange={(e) => setWardName(e.target.value)}
                  placeholder="e.g. General Ward"
                />
              </div>
              <div className="mb-2">
                <label className="form-label form-label-sm">Bed (optional)</label>
                <input
                  className="form-control form-control-sm"
                  value={bedNumber}
                  onChange={(e) => setBedNumber(e.target.value)}
                  placeholder="e.g. B-12"
                />
              </div>
              <div>
                <label className="form-label form-label-sm">Remarks (optional)</label>
                <textarea
                  className="form-control form-control-sm"
                  rows={2}
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  placeholder="Any remarks"
                />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={saving}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>
                {saving ? 'Saving…' : 'Mark collected'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

function RejectSampleModal({
  order,
  onClose,
  onSuccess,
}: {
  order: TestOrder
  onClose: () => void
  onSuccess: () => void
}) {
  const [reason, setReason] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!reason.trim()) {
      setError('Rejection reason is required.')
      return
    }
    setSaving(true)
    setError('')
    try {
      await labApi.rejectSample(order.id, reason.trim())
      onSuccess()
      onClose()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to reject sample.'
      setError(msg)
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
              <p className="small text-muted mb-2">
                {order.orderNumber} · {order.patientName} · {order.testName}
              </p>
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <div>
                <label className="form-label form-label-sm">Rejection reason <span className="text-danger">*</span></label>
                <textarea
                  className="form-control form-control-sm"
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="e.g. Hemolysed sample, Insufficient volume"
                  required
                />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={saving}>
                Cancel
              </button>
              <button type="submit" className="btn btn-danger btn-sm" disabled={saving || !reason.trim()}>
                {saving ? 'Rejecting…' : 'Reject sample'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export function LabDashboard() {
  const navigate = useNavigate()
  const [dashboard, setDashboard] = useState<LabDashboardResponse | null>(null)
  const [summary, setSummary] = useState<LabDashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<'overview' | 'pending' | 'processing' | 'verification' | 'breaches' | 'emergency'>('overview')
  const [collectOrder, setCollectOrder] = useState<TestOrder | null>(null)
  const [rejectOrder, setRejectOrder] = useState<TestOrder | null>(null)
  const [processingOrderId, setProcessingOrderId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      const [dash, sum] = await Promise.all([
        labApi.getDashboard(),
        labApi.getDashboardSummary(),
      ])
      setDashboard(dash)
      setSummary(sum)
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load lab dashboard.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    const safetyTimer = setTimeout(() => {
      if (!cancelled) setLoading(false)
    }, SAFETY_TIMEOUT_MS)
    load().then(() => {
      if (!cancelled) clearTimeout(safetyTimer)
    })
    return () => {
      cancelled = true
      clearTimeout(safetyTimer)
    }
  }, [load])

  const handleCollectSuccess = useCallback(() => {
    setLoading(true)
    load()
  }, [load])

  const handleStartProcessing = useCallback(async (orderId: number) => {
    setProcessingOrderId(orderId)
    try {
      await labApi.startProcessing(orderId)
      navigate(`/lab/results?orderId=${orderId}`)
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to start processing.'
      alert(msg)
    } finally {
      setProcessingOrderId(null)
    }
  }, [navigate])

  if (loading && !summary) {
    return <LabDashboardSkeleton />
  }

  if (error && !summary) {
    return (
      <div className="container mt-4">
        <div className="alert alert-danger" role="alert">
          {error}
          <button type="button" className="btn btn-sm btn-outline-danger ms-2" onClick={() => { setLoading(true); load() }}>
            Retry
          </button>
        </div>
      </div>
    )
  }

  if (!dashboard) {
    return null
  }

  return (
    <div className="container mt-4">
      <h2 className="mb-4">Laboratory Dashboard</h2>

      {/* Metric cards */}
      <div className="row mb-4 g-2">
        <div className="col-6 col-md-4 col-lg">
          <div className="card text-white bg-primary">
            <div className="card-body">
              <h5 className="card-title">Pending Collection</h5>
              <h2>{dashboard.pendingCollection}</h2>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-4 col-lg">
          <div className="card text-white bg-secondary">
            <div className="card-body">
              <h5 className="card-title">Pending Processing</h5>
              <h2>{dashboard.pendingProcessing}</h2>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-4 col-lg">
          <div className="card text-white bg-warning">
            <div className="card-body">
              <h5 className="card-title">Pending Verification</h5>
              <h2>{dashboard.pendingVerification}</h2>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-4 col-lg">
          <div className="card text-white bg-danger">
            <div className="card-body">
              <h5 className="card-title">TAT Breaches</h5>
              <h2>{dashboard.tatBreaches}</h2>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-4 col-lg">
          <div className="card text-white bg-info">
            <div className="card-body">
              <h5 className="card-title">Emergency Samples</h5>
              <h2>{dashboard.emergencySamples}</h2>
            </div>
          </div>
        </div>
      </div>

      {/* Today's activity */}
      <div className="card bg-light mb-4">
        <div className="card-body">
          <h5 className="card-title">Today&apos;s activity</h5>
          <ul className="list-unstyled mb-0">
            <li>Total tests ordered: <strong>{dashboard.todayOrdered}</strong></li>
            <li>Tests collected: <strong>{dashboard.todayCollected}</strong></li>
            <li>Tests completed: <strong>{dashboard.todayCompleted}</strong></li>
            <li>Tests verified: <strong>{dashboard.todayVerified}</strong></li>
            <li>TAT compliance: <strong>{(dashboard.tatCompliancePercent ?? 0).toFixed(1)}%</strong></li>
          </ul>
        </div>
      </div>

      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'overview' ? 'active' : ''}`} type="button" onClick={() => setActiveTab('overview')}>
            Overview
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'pending' ? 'active' : ''}`} type="button" onClick={() => setActiveTab('pending')}>
            Pending Collection ({dashboard.pendingCollection})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'processing' ? 'active' : ''}`} type="button" onClick={() => setActiveTab('processing')}>
            Pending Processing ({dashboard.pendingProcessing})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'verification' ? 'active' : ''}`} type="button" onClick={() => setActiveTab('verification')}>
            Pending Verification ({dashboard.pendingVerification})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'breaches' ? 'active' : ''}`} type="button" onClick={() => setActiveTab('breaches')}>
            TAT Breaches ({dashboard.tatBreaches})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'emergency' ? 'active' : ''}`} type="button" onClick={() => setActiveTab('emergency')}>
            Emergency ({dashboard.emergencySamples})
          </button>
        </li>
      </ul>

      <div className="tab-content">
        {activeTab === 'overview' && (
          <div>
            <p className="text-muted small mb-0">Laboratory dashboard provides real-time visibility into test orders, sample collection, processing, and TAT compliance. NABH audit trail is recorded for collection, verification, and release.</p>
          </div>
        )}

        {activeTab === 'processing' && (
          <div>
            <h4 className="mb-3">Pending processing</h4>
            <p className="text-muted small mb-2">Samples collected, awaiting testing. Start processing to begin result entry.</p>
            {(summary?.pendingProcessing?.length ?? 0) === 0 ? (
              <div className="alert alert-light border">No samples pending processing.</div>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped table-hover">
                  <thead>
                    <tr>
                      <th>Order #</th>
                      <th>UHID / IPD No</th>
                      <th>Patient</th>
                      <th>Test</th>
                      <th>Sample type</th>
                      <th>Collected at</th>
                      <th>Priority</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(summary?.pendingProcessing ?? []).map((order) => (
                      <tr key={order.id}>
                        <td>{order.orderNumber}</td>
                        <td>{order.patientUhid} {order.ipdAdmissionNumber ? ` / ${order.ipdAdmissionNumber}` : ''}</td>
                        <td>{order.patientName}</td>
                        <td>{order.testName}</td>
                        <td>{order.sampleType ?? '—'}</td>
                        <td>{order.sampleCollectedAt ? new Date(order.sampleCollectedAt).toLocaleString() : '—'}</td>
                        <td>{order.isPriority ? <span className="badge bg-danger">Emergency</span> : <span className="badge bg-secondary">Normal</span>}</td>
                        <td>
                          <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            disabled={processingOrderId === order.id}
                            onClick={() => handleStartProcessing(order.id)}
                          >
                            {processingOrderId === order.id ? 'Starting…' : 'Start processing'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {activeTab === 'pending' && (
          <div>
            <h4 className="mb-3">Pending sample collection</h4>
            <p className="text-muted small mb-2">Work queue for phlebotomists. Mark sample collected or reject with reason.</p>
            {summary.pendingCollection.length === 0 ? (
              <div className="alert alert-light border">No pending samples. Queue is clear.</div>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped table-hover">
                  <thead>
                    <tr>
                      <th>Order #</th>
                      <th>UHID / IPD No</th>
                      <th>Patient</th>
                      <th>Test</th>
                      <th>Sample type</th>
                      <th>Ward / OPD</th>
                      <th>Ordered</th>
                      <th>Priority</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(summary?.pendingCollection ?? []).map((order) => (
                      <tr key={order.id}>
                        <td>{order.orderNumber}</td>
                        <td>{order.patientUhid} {order.ipdAdmissionNumber ? ` / ${order.ipdAdmissionNumber}` : ''}</td>
                        <td>{order.patientName}</td>
                        <td>{order.testName}</td>
                        <td>{order.sampleType ?? '—'}</td>
                        <td>{order.wardName ?? order.opdVisitNumber ?? '—'}</td>
                        <td>{new Date(order.orderedAt).toLocaleString()}</td>
                        <td>{order.isPriority ? <span className="badge bg-danger">Emergency / ICU</span> : <span className="badge bg-secondary">Normal</span>}</td>
                        <td>
                          <button type="button" className="btn btn-success btn-sm me-1" onClick={() => setCollectOrder(order)}>Mark collected</button>
                          <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => setRejectOrder(order)}>Reject</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {activeTab === 'verification' && (
          <div>
            <h4 className="mb-3">Pending verification</h4>
            <p className="text-muted small mb-2">Queue for lab supervisor. Technician cannot verify own report.</p>
            {(!summary?.pendingVerification || summary.pendingVerification.length === 0) ? (
              <div className="alert alert-light border">No tests pending verification.</div>
            ) : (
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Patient</th>
                    <th>Test</th>
                    <th>Result entered at</th>
                    <th>Technician</th>
                  </tr>
                </thead>
                <tbody>
                    {(summary?.pendingVerification ?? []).map((order) => (
                    <tr key={order.id}>
                      <td>{order.orderNumber}</td>
                      <td>{order.patientName}</td>
                      <td>{order.testName}</td>
                      <td>{order.resultEnteredAt ? new Date(order.resultEnteredAt).toLocaleString() : '—'}</td>
                      <td>{order.resultEnteredBy ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'breaches' && (
          <div>
            <h4 className="mb-3">TAT breaches</h4>
            <p className="text-muted small mb-2">Quality &amp; NABH monitoring. Link to NABH Audit Dashboard.</p>
            {(!summary?.tatBreaches || summary.tatBreaches.length === 0) ? (
              <div className="alert alert-light border">No TAT breaches. All within target.</div>
            ) : (
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Patient</th>
                    <th>Test</th>
                    <th>Breach reason</th>
                  </tr>
                </thead>
                <tbody>
                    {(summary?.tatBreaches ?? []).map((order) => (
                    <tr key={order.id}>
                      <td>{order.orderNumber}</td>
                      <td>{order.patientName}</td>
                      <td>{order.testName}</td>
                      <td>{order.tatBreachReason || 'TAT exceeded'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'emergency' && (
          <div>
            <h4 className="mb-3">Emergency samples</h4>
            <p className="text-muted small mb-2">ICU / Emergency first. Sorted by urgency; highlight in red/amber.</p>
            {(!summary?.emergencySamples || summary.emergencySamples.length === 0) ? (
              <div className="alert alert-light border">No emergency samples in queue.</div>
            ) : (
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Patient</th>
                    <th>Test</th>
                    <th>Ordered at</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                    {(summary?.emergencySamples ?? []).map((order) => (
                    <tr key={order.id} className={order.isPriority ? 'table-danger' : ''}>
                      <td>{order.orderNumber}</td>
                      <td>{order.patientName}</td>
                      <td>{order.testName}</td>
                      <td>{new Date(order.orderedAt).toLocaleString()}</td>
                      <td><span className="badge bg-danger">Emergency / ICU</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {collectOrder && <CollectSampleModal order={collectOrder} onClose={() => setCollectOrder(null)} onSuccess={handleCollectSuccess} />}
      {rejectOrder && <RejectSampleModal order={rejectOrder} onClose={() => setRejectOrder(null)} onSuccess={handleCollectSuccess} />}
    </div>
  )
}
