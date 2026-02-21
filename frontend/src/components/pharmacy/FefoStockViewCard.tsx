import { useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import { PharmacyCardLayout } from './shared/PharmacyCardLayout'
import { PharmacyBadge } from './shared/PharmacyBadge'
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
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load stock.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const riskBadgeType = (risk: FefoStockRow['riskLevel']): 'SAFE' | 'NEAR_EXPIRY' | 'CRITICAL' | 'EXPIRED' => {
    if (risk === 'SAFE') return 'SAFE'
    if (risk === 'NEAR_EXPIRY') return 'NEAR_EXPIRY'
    if (risk === 'CRITICAL') return 'CRITICAL'
    if (risk === 'EXPIRED') return 'EXPIRED'
    return 'EXPIRED'
  }

  return (
    <PharmacyCardLayout
      title="FEFO Stock View"
      description="Batches ordered by expiry (FEFO). Expired stock is blocked automatically."
      error={error || undefined}
      loading={loading}
      empty={!loading && rows.length === 0}
      emptyMessage="No stock rows found."
    >
      <div className="d-flex gap-2 mb-3">
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

      <div className="table-responsive" style={{ maxHeight: 420, overflowY: 'auto' }}>
        <table className="table table-bordered table-hover table-sm align-middle">
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
            {rows.map((r) => (
              <tr key={`${r.medicineCode}-${r.batchNumber}`}>
                <td>{r.medicineName}</td>
                <td>{r.batchNumber}</td>
                <td className={r.riskColorClass}>{r.expiryDate}</td>
                <td className="text-end">{r.quantityAvailable}</td>
                <td>{r.fefoRank}</td>
                <td>
                  <PharmacyBadge type={riskBadgeType(r.riskLevel)} label={r.riskLevel} />
                </td>
                <td>{r.lasa ? <PharmacyBadge type="LASA" /> : '—'}</td>
                <td>{r.storageLocation ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </PharmacyCardLayout>
  )
}
