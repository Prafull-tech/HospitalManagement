import { useState } from 'react'
import { Link } from 'react-router-dom'
import { apiClient } from '../../api/client'
import styles from './PlaceholderPage.module.css'

export function OpdGroupBillingPage() {
  const [visitIds, setVisitIds] = useState('')
  const [chargePerVisit, setChargePerVisit] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const ids = visitIds.split(/[\s,]+/).map((s) => Number(s.trim())).filter((n) => !Number.isNaN(n))
    if (ids.length === 0 || !chargePerVisit || Number(chargePerVisit) <= 0) {
      setError('Enter comma-separated visit IDs and charge per visit')
      return
    }
    setLoading(true)
    setError('')
    setSuccess(false)
    try {
      await apiClient.post('/opd/billing/group', {
        visitIds: ids,
        consultationChargePerVisit: Number(chargePerVisit),
      })
      setSuccess(true)
      setVisitIds('')
      setChargePerVisit('')
    } catch (err: unknown) {
      const ex = err as { response?: { data?: { message?: string } }; message?: string }
      setError(ex.response?.data?.message || ex.message || 'Failed to create group bill')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>OPD Group Billing</h1>
        <p className={styles.subtitle}>Group multiple OPD visits into one bill</p>
      </header>
      <Link to="/billing" className={styles.backLink}>← Back to Billing</Link>
      <form onSubmit={handleSubmit} style={{ marginTop: '1rem', maxWidth: 400 }}>
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', marginBottom: 0.35, fontWeight: 500 }}>Visit IDs (comma-separated)</label>
          <input
            type="text"
            value={visitIds}
            onChange={(e) => setVisitIds(e.target.value)}
            placeholder="e.g. 1, 2, 3"
            style={{ width: '100%', padding: '0.5rem 0.75rem', borderRadius: 4, border: '1px solid var(--hms-card-border)' }}
          />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', marginBottom: 0.35, fontWeight: 500 }}>Consultation charge per visit (₹)</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={chargePerVisit}
            onChange={(e) => setChargePerVisit(e.target.value)}
            style={{ width: '100%', padding: '0.5rem 0.75rem', borderRadius: 4, border: '1px solid var(--hms-card-border)' }}
          />
        </div>
        {error && <p style={{ color: 'var(--hms-error)', marginBottom: '0.5rem' }}>{error}</p>}
        {success && <p style={{ color: 'var(--hms-success)', marginBottom: '0.5rem' }}>Group bill created successfully.</p>}
        <button type="submit" disabled={loading} style={{ padding: '0.5rem 1rem', cursor: loading ? 'wait' : 'pointer' }}>
          {loading ? 'Creating…' : 'Create Group Bill'}
        </button>
      </form>
    </div>
  )
}
