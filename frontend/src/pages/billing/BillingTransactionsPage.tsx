import { useState, useEffect, useRef } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { billingApi, type BillingTransactionItem } from '../../api/billing'

export function BillingTransactionsPage() {
  const [searchParams] = useSearchParams()
  const from = searchParams.get('from') ?? new Date().toISOString().slice(0, 10)
  const to = searchParams.get('to') ?? new Date().toISOString().slice(0, 10)
  const [data, setData] = useState<{ content: BillingTransactionItem[]; totalElements: number } | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const tableRef = useRef<HTMLTableElement>(null)

  useEffect(() => {
    setLoading(true)
    setError('')
    billingApi
      .listTransactions({ from, to, page: 0, size: 500 })
      .then((res) => setData({ content: res.content ?? [], totalElements: res.totalElements ?? 0 }))
      .catch(() => setError('Failed to load transactions.'))
      .finally(() => setLoading(false))
  }, [from, to])

  const list = data?.content ?? []

  const handlePrint = () => {
    if (!tableRef.current) return
    const win = window.open('', '_blank')
    if (!win) return
    win.document.write(`
      <!DOCTYPE html><html><head><title>Billing Transactions</title>
      <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"/>
      <style>body{ padding: 1rem; font-size: 14px; }</style></head><body>
      <h5>Billing Transactions (${from} to ${to})</h5>
      <p>Total: ${list.length}</p>
      ${tableRef.current.outerHTML}
      <script>window.print(); window.close();</script></body></html>
    `)
    win.document.close()
  }

  const handleCSV = () => {
    const headers = ['Bill No', 'Patient', 'Admission No', 'Service', 'Amount', 'Payment Mode', 'Date']
    const rows = list.map((t) => [
      t.id,
      t.patientName ?? '',
      t.admissionNumber ?? '',
      t.service ?? 'IPD Payment',
      t.amount,
      t.mode ?? '',
      t.createdAt ?? '',
    ])
    const csv = [headers.join(','), ...rows.map((r) => r.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(','))].join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `billing-transactions-${from}-to-${to}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item"><Link to="/billing">Billing</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Transactions</li>
        </ol>
      </nav>
      <div className="card shadow-sm">
        <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
          <h2 className="h6 mb-0 fw-bold">Billing Transactions ({from} to {to})</h2>
          <div className="d-flex gap-2">
            <button type="button" className="btn btn-sm btn-outline-secondary" onClick={handlePrint} disabled={list.length === 0}>Print</button>
            <button type="button" className="btn btn-sm btn-outline-primary" onClick={handleCSV} disabled={list.length === 0}>Download CSV</button>
            <Link to="/billing" className="btn btn-sm btn-outline-secondary">Back to Billing</Link>
          </div>
        </div>
        <div className="card-body">
          {error && <div className="alert alert-danger py-2 mb-0" role="alert">{error}</div>}
          {loading && <p className="text-muted mb-0">Loading…</p>}
          {!loading && !error && list.length === 0 && <p className="text-muted mb-0">No transactions in this date range.</p>}
          {!loading && !error && list.length > 0 && (
            <div className="table-responsive">
              <table ref={tableRef} className="table table-sm table-striped">
                <thead>
                  <tr>
                    <th>Bill No</th>
                    <th>Patient</th>
                    <th>Admission No</th>
                    <th>Service</th>
                    <th>Amount</th>
                    <th>Payment Mode</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {list.map((t) => (
                    <tr key={t.id}>
                      <td>{t.id}</td>
                      <td>{t.patientName ?? '—'}</td>
                      <td>{t.admissionNumber ? <Link to={`/billing/account/${t.ipdAdmissionId}`}>{t.admissionNumber}</Link> : '—'}</td>
                      <td>{t.service ?? 'IPD Payment'}</td>
                      <td>₹{Number(t.amount).toFixed(2)}</td>
                      <td>{t.mode ?? '—'}</td>
                      <td>{t.createdAt ? new Date(t.createdAt).toLocaleString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
