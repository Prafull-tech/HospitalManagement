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
        <p>Payments module coming soon. Use Finalize Bill on the billing account page to record payments.</p>
        <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      </div>
    </div>
  )
}
