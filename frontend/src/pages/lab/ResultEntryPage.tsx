import { useState, useEffect, useCallback } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { labApi } from '../../api/lab'
import type { TestOrder, LabResultRequest, ResultParameter } from '../../types/lab'

export function ResultEntryPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const orderIdParam = searchParams.get('orderId')
  const [order, setOrder] = useState<TestOrder | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [parameters, setParameters] = useState<ResultParameter[]>([
    { parameterName: 'Result', resultValue: '', unit: '', normalRange: '', flag: '', isCritical: false },
  ])
  const [remarks, setRemarks] = useState('')
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    if (!orderIdParam) return
    const id = parseInt(orderIdParam, 10)
    if (isNaN(id)) return
    setError(null)
    try {
      const o = await labApi.getOrder(id)
      setOrder(o)
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to load order.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }, [orderIdParam])

  useEffect(() => {
    load()
  }, [load])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!order) return
    const params = parameters.filter((p) => (p.resultValue ?? '').trim())
    if (params.length === 0) {
      alert('Enter at least one result value.')
      return
    }
    setSaving(true)
    try {
      const req: LabResultRequest = {
        testOrderId: order.id,
        parameters: params,
        remarks: remarks.trim() || undefined,
      }
      await labApi.enterResults(req)
      navigate(`/lab/verification`)
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to save results.'
      alert(msg)
    } finally {
      setSaving(false)
    }
  }

  const addParameter = () => {
    setParameters((p) => [...p, { parameterName: '', resultValue: '', unit: '', normalRange: '', flag: '', isCritical: false }])
  }

  const updateParameter = (index: number, field: keyof ResultParameter, value: string | boolean) => {
    setParameters((p) => {
      const next = [...p]
      next[index] = { ...next[index], [field]: value }
      return next
    })
  }

  if (loading && !order) {
    return (
      <div className="container mt-4">
        <div className="placeholder-glow">
          <span className="placeholder col-12" style={{ height: 200 }} />
        </div>
      </div>
    )
  }

  if (!orderIdParam || !order) {
    return (
      <div className="container mt-4">
        <div className="alert alert-warning">
          No order selected. Please select from <Link to="/lab/sample-processing">Sample Processing</Link> or <Link to="/lab">Dashboard</Link>.
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
            {order.orderNumber} · {order.patientName} · {order.testName}
          </p>
        </div>
        <Link to="/lab/sample-processing" className="btn btn-outline-primary btn-sm">Back to Processing</Link>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="card shadow-sm mb-3">
          <div className="card-body">
            <h5 className="card-title">Test parameters</h5>
            {parameters.map((param, i) => (
              <div key={i} className="row g-2 mb-2">
                <div className="col-md-3">
                  <input
                    className="form-control form-control-sm"
                    placeholder="Parameter name"
                    value={param.parameterName ?? ''}
                    onChange={(e) => updateParameter(i, 'parameterName', e.target.value)}
                  />
                </div>
                <div className="col-md-2">
                  <input
                    className="form-control form-control-sm"
                    placeholder="Value"
                    value={param.resultValue ?? ''}
                    onChange={(e) => updateParameter(i, 'resultValue', e.target.value)}
                  />
                </div>
                <div className="col-md-2">
                  <input
                    className="form-control form-control-sm"
                    placeholder="Unit"
                    value={param.unit ?? ''}
                    onChange={(e) => updateParameter(i, 'unit', e.target.value)}
                  />
                </div>
                <div className="col-md-2">
                  <input
                    className="form-control form-control-sm"
                    placeholder="Normal range"
                    value={param.normalRange ?? ''}
                    onChange={(e) => updateParameter(i, 'normalRange', e.target.value)}
                  />
                </div>
                <div className="col-md-1">
                  <select
                    className="form-select form-select-sm"
                    value={param.flag ?? ''}
                    onChange={(e) => updateParameter(i, 'flag', e.target.value)}
                  >
                    <option value="">—</option>
                    <option value="H">H</option>
                    <option value="L">L</option>
                    <option value="HH">HH</option>
                    <option value="LL">LL</option>
                  </select>
                </div>
              </div>
            ))}
            <button type="button" className="btn btn-outline-secondary btn-sm" onClick={addParameter}>
              + Add parameter
            </button>
          </div>
        </div>
        <div className="mb-3">
          <label className="form-label">Remarks</label>
          <textarea
            className="form-control"
            rows={2}
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="Optional remarks"
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving…' : 'Save & Submit for Verification'}
        </button>
      </form>
    </div>
  )
}
