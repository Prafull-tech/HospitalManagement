import { useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import { PharmacyCardLayout } from './shared/PharmacyCardLayout'
import { PharmacyBadge } from './shared/PharmacyBadge'
import type { ExpiryAlert } from '../../types/pharmacy'

export function ExpiryAlertsCard() {
  const [alerts, setAlerts] = useState<ExpiryAlert[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await pharmacyApi.getAlerts()
      setAlerts(data)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load alerts.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const handleAck = async (id: number) => {
    try {
      await pharmacyApi.acknowledgeAlert(id)
      await load()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to acknowledge alert.'))
    }
  }

  const severityType = (s: string): 'CRITICAL' | 'WARNING' | 'INFO' => {
    if (s === 'CRITICAL') return 'CRITICAL'
    if (s === 'WARNING') return 'WARNING'
    return 'INFO'
  }

  return (
    <PharmacyCardLayout
      title="Expiry & Critical Alerts"
      description="Near-expiry, expired (blocked), min stock breach and high IPD consumption."
      error={error || undefined}
      loading={loading}
      empty={!loading && alerts.length === 0}
      emptyMessage="No active alerts."
    >
      <div className="table-responsive" style={{ maxHeight: 420, overflowY: 'auto' }}>
        <table className="table table-hover table-sm align-middle">
          <thead>
            <tr>
              <th>Severity</th>
              <th>Medicine</th>
              <th>Batch</th>
              <th>Expiry</th>
              <th className="text-end">Qty</th>
              <th>Risk</th>
              <th>Location</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {alerts.map((a) => (
              <tr key={a.id}>
                <td>
                  <PharmacyBadge type={severityType(a.severity)} label={a.severity} />
                </td>
                <td>{a.medicineName}</td>
                <td>{a.batchNumber}</td>
                <td className={a.riskLevel === 'EXPIRED' ? 'text-danger fw-bold' : ''}>{a.expiryDate}</td>
                <td className="text-end">{a.quantityRemaining}</td>
                <td>{a.riskLevel}</td>
                <td>{a.storageLocation ?? '—'}</td>
                <td className="text-end">
                  {!a.acknowledged && (
                    <button
                      type="button"
                      className="btn btn-outline-secondary btn-sm"
                      onClick={() => void handleAck(a.id)}
                    >
                      Acknowledge
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </PharmacyCardLayout>
  )
}
