import { Link } from 'react-router-dom'
import styles from './PlaceholderPage.module.css'

export function InsurancePage() {
  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Insurance / TPA</h1>
        <p className={styles.subtitle}>Insurance claims, TPA approvals, and pre-authorization</p>
      </header>
      <div className={styles.placeholder}>
        <p>Insurance / TPA pre-authorization. Use POST /api/billing/tpa/preauth for pre-auth requests.</p>
        <p>TPA approval status is tracked on IPD admissions and billing accounts.</p>
        <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      </div>
    </div>
  )
}
