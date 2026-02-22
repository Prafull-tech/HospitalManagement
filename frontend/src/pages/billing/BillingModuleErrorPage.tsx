import { Link } from 'react-router-dom'
import styles from './PlaceholderPage.module.css'

/**
 * Shown when a billing route fails to load (e.g. API error, module not available).
 */
export function BillingModuleErrorPage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Billing Module Not Loaded</h1>
        <p className={styles.subtitle}>The billing module could not be loaded. Please check your connection and try again.</p>
      </header>
      <div className={styles.placeholder}>
        <p role="alert">Billing Module Not Loaded</p>
        <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      </div>
    </div>
  )
}
