import { useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import type { IpdIssueQueueItem } from '../../types/pharmacy'

export function IpdIssueQueueCard() {
  const [items, setItems] = useState<IpdIssueQueueItem[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const load = async (q?: string) => {
    setLoading(true)
    setError('')
    try {
      const data = await pharmacyApi.getIpdIssueQueue(q ? { q } : undefined)
      setItems(data)
      if (data.length > 0 && !data.some((i) => i.indentId === selectedId)) {
        setSelectedId(data[0].indentId)
      }
      if (data.length === 0) setSelectedId(null)
      if (data.length === 0 && !q) {
        setMessage('')
      }
    } catch (err: any) {
      const status = err?.response?.status as number | undefined
      if (status === 401 || status === 403) {
        setError('You are not authorized to view this queue.')
      } else if (status && status >= 500) {
        setError('Service temporarily unavailable. Please try again.')
      } else {
        setError(err?.response?.data?.message || 'Failed to load issue queue.')
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const selected = items.find((i) => i.indentId === selectedId) ?? null

  const handleIssue = async (mode: 'FULL' | 'PARTIAL') => {
    if (!selected) return
    setError('')
    setMessage('')
    try {
      if (mode === 'FULL') {
        await pharmacyApi.issueIndent(selected.indentId)
        setMessage('Medicines issued.')
      } else {
        await pharmacyApi.issueIndentPartial(selected.indentId)
        setMessage('Partial issue recorded.')
      }
      await load()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Issue failed.')
    }
  }

  return (
    <div className="card shadow-sm">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div>
          <h3 className="h6 mb-0 fw-bold">IPD Medicine Issue Queue</h3>
          <p className="small text-muted mb-0">
            ICU / Emergency first. System suggests FEFO batches; pharmacist confirms.
          </p>
        </div>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger py-2 mb-2">{error}</div>}
        {message && <div className="alert alert-success py-2 mb-2">{message}</div>}
        {loading && <p className="small text-muted mb-2">Loading queue…</p>}

        <div className="row g-3">
          <div className="col-12 col-md-4">
            <input
              type="text"
              className="form-control form-control-sm mb-2"
              placeholder="Search IPD #, name, ward…"
              onChange={(e) => {
                const q = e.target.value.trim()
                void load(q || undefined)
              }}
            />
            <div
              style={{
                maxHeight: 360,
                overflowY: 'auto',
                border: '1px solid #eee',
                borderRadius: 8,
              }}
            >
              {items.length === 0 && !loading && (
                <p className="small text-muted px-3 py-2 mb-0">No pending indents.</p>
              )}
              {items.map((i) => (
                <button
                  key={i.indentId}
                  type="button"
                  className={`w-100 text-start px-3 py-2 border-0 ${
                    selectedId === i.indentId ? 'bg-primary bg-opacity-10' : 'bg-white'
                  }`}
                  onClick={() => setSelectedId(i.indentId)}
                >
                  <div className="d-flex justify-content-between">
                    <span className="fw-semibold small">
                      {i.ipdAdmissionNumber} · {i.patientName}
                    </span>
                    <span
                      className={`badge rounded-pill ${
                        i.priority === 'ICU' || i.priority === 'EMERGENCY'
                          ? 'bg-danger'
                          : 'bg-secondary'
                      }`}
                    >
                      {i.priority}
                    </span>
                  </div>
                  <div className="small text-muted">
                    {i.wardName} / {i.bedNumber} · {i.medicineCount} medicines ·{' '}
                    {i.waitingMinutes} min
                    {i.status === 'DELAYED' && (
                      <span className="text-danger fw-semibold"> · Delayed</span>
                    )}
                  </div>
                </button>
              ))}
            </div>
          </div>

          <div className="col-12 col-md-8">
            {!selected && (
              <p className="small text-muted mb-0">
                Select an indent from the queue to view details and issue.
              </p>
            )}
            {selected && (
              <>
                <div className="mb-2">
                  <div className="fw-semibold">
                    {selected.patientName} · {selected.ipdAdmissionNumber}
                  </div>
                  <div className="small text-muted">
                    {selected.wardName} / {selected.bedNumber} · Ordered at{' '}
                    {selected.orderedAtDisplay}
                  </div>
                </div>
                <div className="table-responsive mb-3">
                  <table className="table table-sm align-middle">
                    <thead>
                      <tr>
                        <th>Medicine</th>
                        <th className="text-end">Requested</th>
                        <th className="text-end">Available</th>
                        <th>FEFO batch</th>
                        <th>Expiry</th>
                        <th>LASA</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selected.lines.map((l) => (
                        <tr key={l.medicineCode}>
                          <td>{l.medicineName}</td>
                          <td className="text-end">{l.requestedQty}</td>
                          <td className="text-end">{l.availableQty}</td>
                          <td>{l.nextBatchNumber ?? '—'}</td>
                          <td className={l.expiryRiskClass}>
                            {l.nextBatchExpiryDisplay ?? '—'}
                          </td>
                          <td>
                            {l.lasa && (
                              <span className="badge bg-warning text-dark">LASA</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <div className="d-flex gap-2">
                  <button
                    type="button"
                    className="btn btn-success btn-sm"
                    onClick={() => handleIssue('FULL')}
                  >
                    One-click issue (FEFO)
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm"
                    onClick={() => handleIssue('PARTIAL')}
                  >
                    Mark partial issue
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

