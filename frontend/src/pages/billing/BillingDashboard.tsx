import { Link } from 'react-router-dom'
import styles from './BillingDashboard.module.css'

export function BillingDashboard() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Billing & Accounts</h1>
        <p className={styles.subtitle}>Centralized billing, insurance, and financials</p>
      </header>

      <div className={styles.cards}>
        <Link to="/ipd/admissions" className={styles.card}>
          <h2>IPD Billing</h2>
          <p>View and manage billing for IPD admissions. Access patient accounts, finalize bills, and record payments.</p>
        </Link>
        <Link to="/insurance" className={styles.card}>
          <h2>Insurance / TPA</h2>
          <p>Insurance claims, TPA approvals, and pre-authorization tracking.</p>
        </Link>
        <Link to="/payments" className={styles.card}>
          <h2>Payments</h2>
          <p>Payment collection, receipts, and payment history.</p>
        </Link>
        <Link to="/refunds" className={styles.card}>
          <h2>Refunds</h2>
          <p>Refund requests and processing.</p>
        </Link>
      </div>
    </div>
  )
}
