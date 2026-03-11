import { useState, useEffect, useCallback } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { LabOrderItem, LabResultEntryRequest } from '../../types/lab'

export function LabResultEntryPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const orderItemIdParam = searchParams.get('orderItemId')
  const [item, setItem] = useState<LabOrderItem | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [testValue, setTestValue] = useState('')
  const [unit, setUnit] = useState('')
  const [referenceRange, setReferenceRange] = useState('')
  const [remarks, setRemarks] = useState('')
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    if (!orderItemIdParam) return
    const id = parseInt(orderItemIdParam, 10)
    if (isNaN(id)) return
    setError(null)
    try {
      const i = await labApi.getOrderItem(id)
      setItem(i)
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }, [orderItemIdParam])

  useEffect(() => {
    load()
  }, [load])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!item || !testValue.trim()) {
      alert('Enter a result value.')
      return
    }
    setSaving(true)
    try {
      const req: LabResultEntryRequest = {
        orderItemId: item.id,
        testValue: testValue.trim(),
        unit: unit.trim() || undefined,
        referenceRange: referenceRange.trim() || undefined,
        remarks: remarks.trim() || undefined,
      }
      await labApi.enterResult(req)
      navigate('/lab/result-verification')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to save.'
      alert(msg)
    } finally {
      setSaving(false)
    }
  }

  if (loading && !item) {
    return (
      <div className="container mt-4">
        <div className="placeholder-glow">
          <span className="placeholder col-12" style={{ height: 200 }} />
        </div>
      </div>
    )
  }

  if (!orderItemIdParam || !item) {
    return (
      <div className="container mt-4">
        <div className="alert alert-warning">
          No order item selected. Please select from <Link to="/lab/sample-processing">Sample Processing</Link> or <Link to="/lab">Dashboard</Link>.
        </div>
      </div>
    )
  }

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h5 mb-1 fw-bold">Result Entry</h2>
          <p className="text-muted small mb-0">
            {item.orderNumber ?? '—'} · {item.patientName ?? '—'} · {item.testName}
          </p>
        </div>
        <Link to="/lab/sample-processing" className="btn btn-outline-primary btn-sm">Back to Processing</Link>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="card shadow-sm mb-3">
          <div className="card-body">
            <h5 className="card-title">Test result</h5>
            <div className="row g-2 mb-2">
              <div className="col-md-4">
                <label className="form-label small">Result value</label>
                <input
                  className="form-control"
                  placeholder="testValue"
                  value={testValue}
                  onChange={(e) => setTestValue(e.target.value)}
                  required
                />
              </div>
              <div className="col-md-4">
                <label className="form-label small">Unit</label>
                <input
                  className="form-control"
                  placeholder="unit"
                  value={unit}
                  onChange={(e) => setUnit(e.target.value)}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label small">Reference range</label>
                <input
                  className="form-control"
                  placeholder="referenceRange"
                  value={referenceRange}
                  onChange={(e) => setReferenceRange(e.target.value)}
                />
              </div>
            </div>
            <div className="mb-2">
              <label className="form-label small">Remarks</label>
              <textarea
                className="form-control"
                rows={2}
                value={remarks}
                onChange={(e) => setRemarks(e.target.value)}
                placeholder="Optional remarks"
              />
            </div>
          </div>
        </div>
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving…' : 'Save & Submit for Verification'}
        </button>
      </form>
    </div>
  )
}
