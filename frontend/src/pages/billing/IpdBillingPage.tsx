import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ipdApi } from '../../api/ipd'
import type { IPDAdmissionResponse } from '../../types/ipd'
import styles from './PlaceholderPage.module.css'

export function IpdBillingPage() {
  const navigate = useNavigate()
  const [admissions, setAdmissions] = useState<IPDAdmissionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    ipdApi
      .searchAdmissions({ page: 0, size: 50, status: 'ACTIVE' })
      .then((res) => setAdmissions(res.content ?? []))
      .catch((err) => {
        setError(err.response?.data?.message || err.message || 'Failed to load admissions')
        setAdmissions([])
      })
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>IPD Billing</h1>
        <p className={styles.subtitle}>View and manage billing for IPD admissions</p>
      </header>
      <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      {loading ? (
        <p className={styles.placeholder}>Loading…</p>
      ) : error ? (
        <div className={styles.placeholder}>
          <p role="alert">{error}</p>
          <p>Billing Module Not Loaded — check backend connection.</p>
        </div>
      ) : admissions.length === 0 ? (
        <div className={styles.placeholder}>
          <p>No active admissions. Admit a patient to manage billing.</p>
          <Link to="/ipd/admit" className={styles.backLink}>Admit Patient</Link>
        </div>
      ) : (
        <div className={styles.placeholder}>
          <ul style={{ margin: 0, paddingLeft: '1.25rem', listStyle: 'none' }}>
            {admissions.map((a) => (
              <li key={a.id} style={{ marginBottom: '0.5rem' }}>
                <strong>{a.admissionNumber ?? a.id}</strong> — {a.patientName ?? 'Unknown'} (UHID: {a.patientUhid ?? '—'})
                {' '}
                <button
                  type="button"
                  onClick={() => navigate(`/billing/account/${a.id}`)}
                  style={{ marginLeft: '0.5rem', padding: '0.25rem 0.5rem', cursor: 'pointer' }}
                >
                  Open Billing
                </button>
                {' '}
                <Link to={`/ipd/discharge/${a.id}`} className={styles.backLink}>Discharge</Link>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
