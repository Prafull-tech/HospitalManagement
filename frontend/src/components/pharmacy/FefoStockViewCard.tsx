import { useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import type { FefoStockRow } from '../../types/pharmacy'

export function FefoStockViewCard() {
  const [rows, setRows] = useState<FefoStockRow[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = async (q?: string) => {
    setLoading(true)
    setError('')
    try {
      const data = await pharmacyApi.getFefoStock(q ? { q } : undefined)
      setRows(data)
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load stock.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const riskBadgeClass = (risk: FefoStockRow['riskLevel']) => {
    if (risk === 'SAFE') return 'badge bg-success'
    if (risk === 'NEAR_EXPIRY') return 'badge bg-warning text-dark'
    if (risk === 'CRITICAL') return 'badge bg-danger'
    if (risk === 'EXPIRED') return 'badge bg-secondary'
    return 'badge bg-secondary'
  }

  return (
    <div className="card shadow-sm">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div>
          <h3 className="h6 mb-0 fw-bold">FEFO Stock View</h3>
          <p className="small text-muted mb-0">
            Batches ordered by expiry (FEFO). Expired stock is blocked automatically.
          </p>
        </div>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger py-2 mb-2">{error}</div>}
        {loading && <p className="small text-muted mb-2">Loading stock…</p>}

        <div className="d-flex gap-2 mb-2 flex-wrap">
          <input
            type="text"
            className="form-control form-control-sm"
            style={{ maxWidth: 260 }}
            placeholder="Search medicine or batch…"
            onChange={(e) => {
              const q = e.target.value.trim()
              void load(q || undefined)
            }}
          />
        </div>

        <div className="table-responsive" style={{ maxHeight: 400, overflowY: 'auto' }}>
          <table className="table table-sm align-middle">
            <thead>
              <tr>
                <th>Medicine</th>
                <th>Batch</th>
                <th>Expiry</th>
                <th className="text-end">Qty</th>
                <th>FEFO Rank</th>
                <th>Risk</th>
                <th>LASA</th>
                <th>Location</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 && !loading && (
                <tr>
                  <td colSpan={8} className="small text-muted">
                    No stock rows found.
                  </td>
                </tr>
              )}
              {rows.map((r) => (
                <tr key={`${r.medicineCode}-${r.batchNumber}`}>
                  <td>{r.medicineName}</td>
                  <td>{r.batchNumber}</td>
                  <td className={r.riskColorClass}>{r.expiryDate}</td>
                  <td className="text-end">{r.quantityAvailable}</td>
                  <td>{r.fefoRank}</td>
                  <td>
                    <span className={riskBadgeClass(r.riskLevel)}>{r.riskLevel}</span>
                  </td>
                  <td>
                    {r.lasa && (
                      <span className="badge bg-warning text-dark">LASA</span>
                    )}
                  </td>
                  <td>{r.storageLocation ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

