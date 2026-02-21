import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { billingApi } from '../../api/billing'
import type { BillingAccountView } from '../../types/billing'
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
  BED: 'Bed / Room',
  PHARMACY: 'Pharmacy',
  LAB: 'Laboratory',
  OT: 'OT',
  RADIOLOGY: 'Radiology',
  CONSULTATION: 'Consultation',
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
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Summary by Department</h2>
        <div className={styles.grid}>
          {Object.entries(byType).map(([k, v]) => (
            <div key={k} className={styles.summaryRow}>
              <span>{SERVICE_LABELS[k] || k}</span>
              <span>{formatCurrency(Number(v))}</span>
            </div>
          ))}
        </div>
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Totals</h2>
        <div className={styles.totals}>
          <div className={styles.totalRow}>
            <span>Total Amount</span>
            <span>{formatCurrency(data.totalAmount)}</span>
          </div>
          <div className={styles.totalRow}>
            <span>Paid</span>
            <span className={styles.paid}>{formatCurrency(data.paidAmount)}</span>
          </div>
          <div className={styles.totalRow}>
            <span>Pending</span>
            <span className={data.pendingAmount > 0 ? styles.pending : ''}>
              {formatCurrency(data.pendingAmount)}
            </span>
          </div>
        </div>
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
                <td>{item.serviceName}</td>
                <td>{item.quantity}</td>
                <td>{formatCurrency(item.unitPrice)}</td>
                <td>{formatCurrency(item.totalPrice)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}
