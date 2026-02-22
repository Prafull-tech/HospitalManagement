import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { apiClient } from '../../api/client'
import styles from './PlaceholderPage.module.css'

interface CorporateAccount {
  id: number
  companyName: string
  corporateCode: string
  contactPerson?: string
  creditLimit: number
  billingCycle: string
  active: boolean
}

export function CorporateAccountsPage() {
  const [accounts, setAccounts] = useState<CorporateAccount[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    apiClient.get('/billing/corporate-accounts')
      .then((r) => setAccounts(r.data))
      .catch(() => setAccounts([]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Corporate Accounts</h1>
        <p className={styles.subtitle}>Corporate tie-up billing and credit limits</p>
      </header>
      <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      {loading ? (
        <p className={styles.placeholder}>Loading…</p>
      ) : accounts.length === 0 ? (
        <div className={styles.placeholder}>
          <p>No corporate accounts. Create via API or add admin UI.</p>
        </div>
      ) : (
        <div className={styles.placeholder}>
          <ul style={{ margin: 0, paddingLeft: '1.25rem' }}>
            {accounts.map((a) => (
              <li key={a.id}>
                <strong>{a.companyName}</strong> ({a.corporateCode}) — Limit: ₹{a.creditLimit?.toLocaleString('en-IN')} — {a.billingCycle}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
