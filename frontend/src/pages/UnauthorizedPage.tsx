import { Link } from 'react-router-dom'
import styles from './UnauthorizedPage.module.css'

export function UnauthorizedPage() {
  return (
    <div className={styles.page}>
      <h1>Access denied</h1>
      <p>You do not have permission to view this page.</p>
      <Link to="/reception" className={styles.link}>Back to Reception</Link>
    </div>
  )
}
