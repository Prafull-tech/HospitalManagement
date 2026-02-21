import { useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import { PharmacyCardLayout } from './shared/PharmacyCardLayout'
import type { PharmacySummary } from '../../types/pharmacy'

export function PharmacySummaryCard() {
  const [summary, setSummary] = useState<PharmacySummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await pharmacyApi.getTodaySummary()
      setSummary(data)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load summary.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const handleExport = async (format: 'PDF' | 'XLSX') => {
    try {
      const res = await pharmacyApi.exportTodaySummary(format)
      const blob = new Blob([res.data], {
        type:
          format === 'PDF'
            ? 'application/pdf'
            : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `pharmacy-summary-${summary?.date ?? ''}.${format.toLowerCase()}`
      a.click()
      URL.revokeObjectURL(url)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to export summary.'))
    }
  }

  const headerAction = (
    <div className="d-flex gap-2">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm"
        onClick={() => void handleExport('PDF')}
        disabled={!summary}
      >
        Export PDF
      </button>
      <button
        type="button"
        className="btn btn-outline-primary btn-sm"
        onClick={() => void handleExport('XLSX')}
        disabled={!summary}
      >
        Export Excel
      </button>
    </div>
  )

  return (
    <PharmacyCardLayout
      title="Today's Pharmacy Summary"
      description="Auto-generated snapshot for NABH: indents, issues, overrides and alerts."
      headerAction={headerAction}
      error={error || undefined}
      loading={loading}
      empty={!loading && !summary && !error}
      emptyMessage="No summary available for today yet."
    >
      {summary && (
        <div className="row g-3">
          <div className="col-6 col-md-3">
            <div className="border rounded p-2 h-100">
              <div className="small text-muted">Indents received</div>
              <div className="fs-5 fw-bold">{summary.totalIndentsReceived}</div>
            </div>
          </div>
          <div className="col-6 col-md-3">
            <div className="border rounded p-2 h-100">
              <div className="small text-muted">Indents issued</div>
              <div className="fs-5 fw-bold">{summary.totalIndentsIssued}</div>
            </div>
          </div>
          <div className="col-6 col-md-3">
            <div className="border rounded p-2 h-100">
              <div className="small text-muted">Pending indents</div>
              <div className="fs-5 fw-bold">{summary.pendingIndents}</div>
            </div>
          </div>
          <div className="col-6 col-md-3">
            <div className="border rounded p-2 h-100">
              <div className="small text-muted">Medicines issued</div>
              <div className="fs-5 fw-bold">{summary.medicinesIssuedCount}</div>
            </div>
          </div>
          <div className="col-6 col-md-3">
            <div className="border rounded p-2 h-100">
              <div className="small text-muted">Stock adjustments</div>
              <div className="fs-5 fw-bold">{summary.stockAdjustmentsCount}</div>
            </div>
          </div>
          <div className="col-6 col-md-3">
            <div className="border rounded p-2 h-100">
              <div className="small text-muted">Overrides</div>
              <div className="fs-5 fw-bold">{summary.overridesCount}</div>
            </div>
          </div>
          <div className="col-6 col-md-3">
            <div className="border rounded p-2 h-100">
              <div className="small text-muted">High-risk alerts</div>
              <div className="fs-5 fw-bold">{summary.highRiskAlerts}</div>
            </div>
          </div>
        </div>
      )}
    </PharmacyCardLayout>
  )
}
