import { useCallback, useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import { PharmacyCardLayout } from './shared/PharmacyCardLayout'
import { PharmacyBadge } from './shared/PharmacyBadge'
import type { StockTransactionResponse } from '../../types/pharmacy'

interface StockTransactionsCardProps {
  refetchTrigger?: number
}

function formatTransactionDate(d: string): string {
  try {
    return new Date(d).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return d
  }
}

export function StockTransactionsCard({ refetchTrigger = 0 }: StockTransactionsCardProps) {
  const [list, setList] = useState<StockTransactionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await pharmacyApi.listStockTransactions({ limit: 50 })
      setList(data)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load transactions.'))
      setList([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load, refetchTrigger])

  const headerAction = (
    <button type="button" className="btn btn-outline-secondary btn-sm" onClick={load}>
      Refresh
    </button>
  )

  return (
    <PharmacyCardLayout
      title="Stock Transactions"
      description="Recent purchases and sales (last 30 days)."
      headerAction={headerAction}
      error={error || undefined}
      loading={loading}
      empty={!loading && list.length === 0}
      emptyMessage="No transactions yet. Use Purchase or Sell to record stock movements."
    >
      <div className="table-responsive" style={{ maxHeight: 420, overflowY: 'auto' }}>
        <table className="table table-bordered table-hover table-sm align-middle">
          <thead>
            <tr>
              <th>Date</th>
              <th>Type</th>
              <th>Medicine</th>
              <th className="text-end">Qty</th>
              <th>Batch</th>
              <th>Supplier / Ref</th>
              <th>By</th>
            </tr>
          </thead>
          <tbody>
            {list.map((t) => (
              <tr key={t.id}>
                <td className="small">{formatTransactionDate(t.performedAt)}</td>
                <td>
                  <PharmacyBadge type={t.transactionType === 'PURCHASE' ? 'PURCHASE' : 'SELL'} label={t.transactionType} />
                </td>
                <td>
                  <span className="fw-semibold">{t.medicineCode}</span>
                  <span className="text-muted ms-1">{t.medicineName}</span>
                </td>
                <td className="text-end">{t.quantity}</td>
                <td className="small">{t.batchNumber || '—'}</td>
                <td className="small">{t.transactionType === 'PURCHASE' ? t.supplier || '—' : t.reference || '—'}</td>
                <td className="small text-muted">{t.performedBy}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </PharmacyCardLayout>
  )
}
