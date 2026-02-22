import { Link } from 'react-router-dom'
import styles from './PlaceholderPage.module.css'

export function PaymentsPage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Payments</h1>
        <p className={styles.subtitle}>Payment collection and receipts</p>
      </header>
      <div className={styles.placeholder}>
        <p>Payment collection (Cash / Card / UPI). Use POST /api/billing/payment to record payments.</p>
        <p>Or use &quot;Collect Payment&quot; on the <Link to="/billing/ipd" className={styles.backLink}>Billing Account</Link> page.</p>
        <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      </div>
    </div>
  )
}
