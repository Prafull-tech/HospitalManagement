import { Link } from 'react-router-dom'
import styles from './PlaceholderPage.module.css'

export function RefundsPage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Refunds</h1>
        <p className={styles.subtitle}>Refund requests and processing</p>
      </header>
      <div className={styles.placeholder}>
        <p>Refund requests. Use POST /api/billing/refund with paymentId, amount, and reason.</p>
        <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      </div>
    </div>
  )
}
