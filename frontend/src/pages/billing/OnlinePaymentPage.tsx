import { Link } from 'react-router-dom'
import styles from './PlaceholderPage.module.css'

export function OnlinePaymentPage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Online Payment</h1>
        <p className={styles.subtitle}>Razorpay / UPI / Card payment integration</p>
      </header>
      <div className={styles.placeholder}>
        <p>Razorpay / UPI / Card integration. Use POST /api/payment/create-order and POST /api/payment/confirm.</p>
        <p>Or use &quot;Collect Payment&quot; on the <Link to="/billing/ipd" className={styles.backLink}>Billing Account</Link> page with UPI/Card mode.</p>
        <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      </div>
    </div>
  )
}
