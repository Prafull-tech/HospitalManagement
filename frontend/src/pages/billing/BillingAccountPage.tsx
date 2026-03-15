import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { billingApi } from '../../api/billing'
import type { BillingAccountView, BillingItemResponse, PaymentRequest } from '../../types/billing'
import styles from './BillingAccountPage.module.css'

/** Hospital details for NABH bill header/footer (configurable via env or app config later) */
const HOSPITAL_BILL = {
  name: 'Hope Haven Hospital',
  address: '855 Howard Street, Dutton, MI 49316',
  phone: '(123) 456-1238',
  email: 'hopedutton@hopehaven.com',
  nabhLabel: 'NABH Accredited',
  gstin: '', // e.g. '29AAAAA0000A1Z5' when applicable
}

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

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-IN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

/** Build NABH-compliant patient bill HTML for printing */
function buildNabhBillHtml(data: BillingAccountView, formatCurr: (n: number) => string): string {
  const billDate = new Date().toLocaleDateString('en-IN', { day: '2-digit', month: '2-digit', year: 'numeric' })
  const hospital = HOSPITAL_BILL
  const items = data.items || []

  const rows = items
    .map(
      (item: BillingItemResponse, i: number) => `
    <tr>
      <td>${i + 1}</td>
      <td>${formatDate(item.createdAt)}</td>
      <td>${escapeHtml(item.serviceName)}${item.cgst != null && item.cgst > 0 ? `<br/><small>CGST + SGST</small>` : ''}</td>
      <td class="text-end">${item.quantity}</td>
      <td class="text-end">${formatCurr(item.unitPrice)}</td>
      <td class="text-end">${formatCurr(item.totalPrice)}</td>
    </tr>`
    )
    .join('')

  return `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8"/>
  <title>Patient Bill - ${escapeHtml(data.patientName)}</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: Arial, sans-serif; font-size: 12px; color: #222; padding: 16px; max-width: 800px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 2px solid #0d9488; }
    .hospital-name { font-size: 18px; font-weight: 700; color: #0d9488; margin: 0 0 4px 0; }
    .nabh-badge { font-size: 10px; font-weight: 600; color: #059669; text-transform: uppercase; letter-spacing: 0.05em; margin-top: 4px; }
    .hospital-details { text-align: right; font-size: 11px; color: #444; line-height: 1.4; }
    .bill-title { text-align: center; font-size: 16px; font-weight: 700; margin: 14px 0 8px 0; text-transform: uppercase; letter-spacing: 0.03em; }
    .bill-meta { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 11px; color: #555; }
    .patient-block { margin-bottom: 14px; padding: 8px 10px; background: #f8fafc; border-radius: 6px; font-size: 11px; }
    .patient-block strong { display: block; margin-bottom: 4px; }
    table { width: 100%; border-collapse: collapse; margin: 10px 0; }
    th, td { padding: 6px 8px; text-align: left; border: 1px solid #e2e8f0; }
    th { background: #0d9488; color: #fff; font-size: 11px; font-weight: 600; text-transform: uppercase; }
    .text-end { text-align: right; }
    .totals { margin-top: 12px; margin-left: auto; width: 280px; font-size: 12px; }
    .totals tr { border: none; }
    .totals td { border: none; padding: 4px 0; }
    .totals td:first-child { color: #555; }
    .totals td:last-child { text-align: right; font-weight: 600; }
    .balance-row td:last-child { font-size: 14px; color: #b45309; }
    .declaration { margin-top: 16px; padding: 10px; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 6px; font-size: 10px; color: #166534; }
    .footer { margin-top: 20px; padding-top: 10px; border-top: 1px solid #e2e8f0; text-align: center; font-size: 10px; color: #64748b; }
  </style>
</head>
<body>
  <div class="header">
    <div>
      <p class="hospital-name">${escapeHtml(hospital.name)}</p>
      ${hospital.nabhLabel ? `<p class="nabh-badge">${escapeHtml(hospital.nabhLabel)}</p>` : ''}
    </div>
    <div class="hospital-details">
      <div>${escapeHtml(hospital.address)}</div>
      <div>Phone: ${escapeHtml(hospital.phone)}</div>
      <div>Email: ${escapeHtml(hospital.email)}</div>
      ${hospital.gstin ? `<div>GSTIN: ${escapeHtml(hospital.gstin)}</div>` : ''}
    </div>
  </div>
  <h1 class="bill-title">Patient Bill / Invoice</h1>
  <div class="bill-meta">
    <span>Bill Date: ${billDate}</span>
    <span>Admission No: ${escapeHtml(data.admissionNumber || data.uhid)}</span>
  </div>
  <div class="patient-block">
    <strong>Patient: ${escapeHtml(data.patientName)}</strong>
    UHID: ${escapeHtml(data.uhid)}
  </div>
  <table>
    <thead>
      <tr>
        <th>#</th>
        <th>Date</th>
        <th>Particulars</th>
        <th class="text-end">Qty</th>
        <th class="text-end">Rate (₹)</th>
        <th class="text-end">Amount (₹)</th>
      </tr>
    </thead>
    <tbody>
      ${rows || '<tr><td colspan="6" class="text-end">No line items</td></tr>'}
    </tbody>
  </table>
  <table class="totals">
    <tr><td>Total Bill Amount</td><td>${formatCurr(data.totalAmount)}</td></tr>
    <tr><td>Less: Payment Received</td><td>${formatCurr(data.paidAmount)}</td></tr>
    <tr class="balance-row"><td>Balance Payable</td><td>${formatCurr(data.pendingAmount)}</td></tr>
  </table>
  <div class="declaration">
    This bill is issued in compliance with NABH standards. All charges are as per hospital tariff. For queries contact the billing desk.
  </div>
  <div class="footer">
    ${escapeHtml(hospital.name)} | ${escapeHtml(hospital.address)}
  </div>
  <script>window.onload = function() { window.print(); }</script>
</body>
</html>`
}

function escapeHtml(s: string | undefined): string {
  if (s == null || s === '') return ''
  const el = document.createElement('div')
  el.textContent = s
  return el.innerHTML
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
          <button
            type="button"
            className={styles.actionBtn}
            onClick={() => {
              const win = window.open('', '_blank')
              if (!win) return
              win.document.write(buildNabhBillHtml(data, formatCurrency))
              win.document.close()
            }}
          >
            Print Bill (NABH)
          </button>
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
