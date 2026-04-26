import { useEffect, useMemo, useState } from 'react'
import shared from '../styles/Dashboard.module.css'
import styles from './system-config/SystemConfigShared.module.css'
import { listAuditEvents, type AuditEventDto } from '../api/auditApi'
import { apiErrorWithNetworkHint } from '../utils/apiNetworkError'

function formatDateTime(iso: string) {
  if (!iso) return '—'
  const dt = new Date(iso)
  if (Number.isNaN(dt.getTime())) return iso
  return dt.toLocaleString()
}

export function AdminAuditLogsPage() {
  const [items, setItems] = useState<AuditEventDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(50)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const canPrev = page > 0
  const canNext = page + 1 < totalPages

  const header = useMemo(() => {
    const count = totalElements ? `${totalElements.toLocaleString()} events` : '—'
    return { count }
  }, [totalElements])

  const load = (nextPage = page, nextSize = size) => {
    setLoading(true)
    setError('')
    listAuditEvents({ page: nextPage, size: nextSize })
      .then((resp) => {
        setItems(resp.items || [])
        setPage(resp.page)
        setSize(resp.size)
        setTotalPages(resp.totalPages)
        setTotalElements(resp.totalElements)
      })
      .catch((err) => {
        setError(apiErrorWithNetworkHint('Failed to load audit logs.', err))
        setItems([])
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load(0, size)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h1 className={shared.pageTitle}>Audit Logs</h1>
        <p className={shared.pageSubtitle}>
          View-only trail of key actions captured by the system. {header.count}.
        </p>
      </div>

      <div className={styles.toolbar} style={{ justifyContent: 'space-between', gap: '0.75rem', flexWrap: 'wrap' }}>
        <div className="d-flex align-items-center gap-2 flex-wrap">
          <button type="button" className={styles.secondaryBtn} onClick={() => load(page, size)} disabled={loading}>
            Refresh
          </button>
          <button type="button" className={styles.secondaryBtn} onClick={() => { if (canPrev) load(page - 1, size) }} disabled={loading || !canPrev}>
            Prev
          </button>
          <button type="button" className={styles.secondaryBtn} onClick={() => { if (canNext) load(page + 1, size) }} disabled={loading || !canNext}>
            Next
          </button>
          <span className="text-muted small" style={{ marginLeft: '0.25rem' }}>
            Page {totalPages ? page + 1 : 0} / {totalPages || 0}
          </span>
        </div>

        <div className="d-flex align-items-center gap-2">
          <span className="text-muted small">Page size</span>
          <select
            className={styles.input}
            style={{ width: 110 }}
            value={size}
            onChange={(e) => {
              const nextSize = Number(e.target.value) || 50
              setSize(nextSize)
              load(0, nextSize)
            }}
            disabled={loading}
            aria-label="Audit page size"
          >
            {[25, 50, 100, 200].map((n) => <option key={n} value={n}>{n}</option>)}
          </select>
        </div>
      </div>

      {error ? <div className={styles.errorBanner}>{error}</div> : null}
      {loading ? <div className={styles.loading}>Loading audit logs…</div> : null}

      {!loading && items.length === 0 ? (
        <div className={styles.empty}>No audit events found.</div>
      ) : null}

      {!loading && items.length > 0 ? (
        <div className={styles.card}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>When</th>
                <th>Action</th>
                <th>Entity</th>
                <th>User</th>
                <th>Details</th>
                <th>Correlation</th>
              </tr>
            </thead>
            <tbody>
              {items.map((e) => (
                <tr key={e.id}>
                  <td style={{ whiteSpace: 'nowrap' }}>{formatDateTime(e.createdAt)}</td>
                  <td style={{ fontWeight: 700 }}>{e.action}</td>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    {e.entityType}{e.entityId != null ? ` #${e.entityId}` : ''}
                  </td>
                  <td>{e.username || '—'}</td>
                  <td style={{ maxWidth: 520 }}>
                    <span title={e.details || ''}>
                      {(e.details || '—').slice(0, 160)}
                      {(e.details && e.details.length > 160) ? '…' : ''}
                    </span>
                  </td>
                  <td style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace', fontSize: 12 }}>
                    {e.correlationId || '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  )
}

