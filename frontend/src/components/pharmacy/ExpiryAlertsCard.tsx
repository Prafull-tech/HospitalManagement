import { useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
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
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load alerts.')
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
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to acknowledge alert.')
    }
  }

  const severityClass = (severity: string) => {
    if (severity === 'CRITICAL') return 'badge bg-danger'
    if (severity === 'WARNING') return 'badge bg-warning text-dark'
    return 'badge bg-info'
  }

  return (
    <div className="card shadow-sm">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div>
          <h3 className="h6 mb-0 fw-bold">Expiry &amp; Critical Alerts</h3>
          <p className="small text-muted mb-0">
            Near-expiry, expired (blocked), min stock breach and high IPD consumption.
          </p>
        </div>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger py-2 mb-2">{error}</div>}
        {loading && <p className="small text-muted mb-2">Loading alerts…</p>}

        <div className="table-responsive" style={{ maxHeight: 400, overflowY: 'auto' }}>
          <table className="table table-sm align-middle">
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
              {alerts.length === 0 && !loading && (
                <tr>
                  <td colSpan={8} className="small text-muted">
                    No active alerts.
                  </td>
                </tr>
              )}
              {alerts.map((a) => (
                <tr key={a.id}>
                  <td>
                    <span className={severityClass(a.severity)}>{a.severity}</span>
                  </td>
                  <td>{a.medicineName}</td>
                  <td>{a.batchNumber}</td>
                  <td>{a.expiryDate}</td>
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
      </div>
    </div>
  )
}

