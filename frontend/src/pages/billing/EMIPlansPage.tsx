import { Link } from 'react-router-dom'
import styles from './PlaceholderPage.module.css'

export function EMIPlansPage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>EMI Billing</h1>
        <p className={styles.subtitle}>Create and manage EMI plans for billing accounts</p>
      </header>
      <div className={styles.placeholder}>
        <p>EMI plans are created from the Billing Account page. Go to an IPD admission billing account and create an EMI plan to allow discharge with down payment.</p>
        <Link to="/ipd/admissions" className={styles.backLink}>IPD Admissions</Link>
        {' | '}
        <Link to="/billing" className={styles.backLink}>Back to Billing</Link>
      </div>
    </div>
  )
}
