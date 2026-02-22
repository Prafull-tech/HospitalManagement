import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { billingApi } from '../../api/billing'
import type { BillingAccountView, PaymentRequest } from '../../types/billing'
import styles from './BillingAccountPage.module.css'

function formatCurrency(n: number): string {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(n)
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const SERVICE_LABELS: Record<string, string> = {
  BED: 'Bed',
  PHARMACY: 'Pharmacy',
  LAB: 'Lab',
  OT: 'OT',
  RADIOLOGY: 'Radiology',
  CONSULTATION: 'Doctor',
  NURSING: 'Nursing',
  BLOOD_BANK: 'Blood Bank',
  PHYSIOTHERAPY: 'Physiotherapy',
  EMERGENCY: 'Emergency',
  PROCEDURE: 'Procedure',
  OTHER: 'Other',
}

export function BillingAccountPage() {
  const { id } = useParams<{ id: string }>()
  const ipdId = id ? Number(id) : null

  const [data, setData] = useState<BillingAccountView | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [paymentSubmitting, setPaymentSubmitting] = useState(false)
  const [paymentForm, setPaymentForm] = useState<PaymentRequest>({
    ipdId: 0,
    amount: 0,
    mode: 'Cash',
    referenceNo: '',
  })

  const refreshAccount = () => {
    if (!ipdId || Number.isNaN(ipdId)) return
    billingApi.getAccount(ipdId).then(setData).catch(() => {})
  }

  useEffect(() => {
    if (!ipdId || Number.isNaN(ipdId)) {
      setError('Invalid admission ID')
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    billingApi
      .getAccount(ipdId)
      .then(setData)
      .catch((err) => {
        setError(err.response?.data?.message || err.message || 'Failed to load billing')
      })
      .finally(() => setLoading(false))
  }, [ipdId])

  useEffect(() => {
    if (ipdId && showPaymentModal) {
      setPaymentForm((f) => ({ ...f, ipdId, amount: data?.pendingAmount ?? 0 }))
    }
  }, [ipdId, showPaymentModal, data?.pendingAmount])

  if (loading) {
    return (
      <div className={styles.page}>
        <header className={styles.header}>
          <h1 className={styles.title}>Billing Account</h1>
        </header>
        <div className={styles.skeleton}>Loading…</div>
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className={styles.page}>
        <header className={styles.header}>
          <h1 className={styles.title}>Billing Account</h1>
        </header>
        <div className={styles.error} role="alert">
          {error || 'Not found'}
        </div>
        <Link to="/ipd/admissions" className={styles.actionBtn}>
          Back to Admissions
        </Link>
      </div>
    )
  }

  const byType = data.totalByServiceType || {}

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1 className={styles.title}>Billing — {data.admissionNumber || data.uhid}</h1>
          <p className={styles.breadcrumb}>
            <Link to="/ipd">IPD</Link>
            <span>→</span>
            <Link to="/ipd/admissions">Admissions</Link>
            <span>→</span>
            <Link to={`/ipd/admissions/${ipdId}`}>View</Link>
            <span>→</span>
            Billing
          </p>
        </div>
        <div className={styles.actions}>
          <Link to={`/ipd/admissions/${ipdId}`} className={styles.actionBtn}>
            Back to Admission
          </Link>
          <Link to={`/ipd/discharge/${ipdId}`} className={styles.actionBtnPrimary}>
            Discharge
          </Link>
        </div>
      </header>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Patient</h2>
        <p><strong>{data.patientName}</strong> — UHID: {data.uhid}</p>
        <div className={styles.badges}>
          {data.corporateApproved && <span className={styles.badge}>✔ Corporate Covered</span>}
          {data.emiActive && <span className={styles.badge}>✔ EMI Active</span>}
          {data.hasGstSplit && <span className={styles.badge}>✔ GST Split (Pharmacy)</span>}
        </div>
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Summary by Department</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Department</th>
              <th>Amount</th>
            </tr>
          </thead>
          <tbody>
            {Object.entries(byType).map(([k, v]) => (
              <tr key={k}>
                <td>{SERVICE_LABELS[k] || k}</td>
                <td>{formatCurrency(Number(v))}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Totals</h2>
        <div className={styles.totals}>
          <div className={styles.totalRow}>
            <span>Total Amount</span>
            <span>{formatCurrency(data.totalAmount)}</span>
          </div>
          <div className={styles.totalRow}>
            <span>Paid Amount</span>
            <span className={styles.paid}>{formatCurrency(data.paidAmount)}</span>
          </div>
          <div className={styles.totalRow}>
            <span>Pending Amount</span>
            <span className={data.pendingAmount > 0 ? styles.pending : ''}>
              {formatCurrency(data.pendingAmount)}
            </span>
          </div>
        </div>
        {data.pendingAmount > 0 && (
          <div className={styles.paymentAction}>
            <button
              type="button"
              className={styles.actionBtnPrimary}
              onClick={() => setShowPaymentModal(true)}
            >
              Collect Payment
            </button>
          </div>
        )}
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Item Details</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Date</th>
              <th>Service</th>
              <th>Qty</th>
              <th>Unit Price</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {data.items.map((item) => (
              <tr key={item.id}>
                <td>{formatDateTime(item.createdAt)}</td>
                <td>
                  {item.serviceName}
                  {item.cgst != null && item.cgst > 0 && (
                    <span className={styles.gstNote}>
                      {' '}(CGST {formatCurrency(item.cgst)} + SGST {formatCurrency(item.sgst ?? 0)})
                    </span>
                  )}
                </td>
                <td>{item.quantity}</td>
                <td>{formatCurrency(item.unitPrice)}</td>
                <td>{formatCurrency(item.totalPrice)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {showPaymentModal && (
        <div className={styles.modalOverlay} onClick={() => !paymentSubmitting && setShowPaymentModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h2 className={styles.cardTitle}>Collect Payment</h2>
            <form
              onSubmit={async (e) => {
                e.preventDefault()
                if (!ipdId || paymentForm.amount <= 0) return
                setPaymentSubmitting(true)
                setError('')
                try {
                  await billingApi.recordPayment({
                    ipdId,
                    amount: paymentForm.amount,
                    mode: paymentForm.mode,
                    referenceNo: paymentForm.referenceNo || undefined,
                  })
                  refreshAccount()
                  setShowPaymentModal(false)
                } catch (err: unknown) {
                  const ex = err as { response?: { data?: { message?: string } }; message?: string }
                  setError(ex.response?.data?.message || ex.message || 'Payment failed')
                } finally {
                  setPaymentSubmitting(false)
                }
              }}
            >
              <div className={styles.formGroup}>
                <label>Amount (₹)</label>
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={paymentForm.amount || ''}
                  onChange={(e) => setPaymentForm((f) => ({ ...f, amount: Number(e.target.value) || 0 }))}
                  required
                />
              </div>
              <div className={styles.formGroup}>
                <label>Mode</label>
                <select
                  value={paymentForm.mode}
                  onChange={(e) => setPaymentForm((f) => ({ ...f, mode: e.target.value as PaymentRequest['mode'] }))}
                >
                  <option value="Cash">Cash</option>
                  <option value="Card">Card</option>
                  <option value="UPI">UPI</option>
                </select>
              </div>
              <div className={styles.formGroup}>
                <label>Reference No (optional)</label>
                <input
                  type="text"
                  placeholder="Transaction ID / Cheque No"
                  value={paymentForm.referenceNo || ''}
                  onChange={(e) => setPaymentForm((f) => ({ ...f, referenceNo: e.target.value }))}
                />
              </div>
              {error && <div className={styles.error}>{error}</div>}
              <div className={styles.formActions}>
                <button type="submit" className={styles.actionBtnPrimary} disabled={paymentSubmitting || paymentForm.amount <= 0}>
                  {paymentSubmitting ? 'Processing…' : 'Record Payment'}
                </button>
                <button type="button" className={styles.actionBtn} onClick={() => setShowPaymentModal(false)} disabled={paymentSubmitting}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
