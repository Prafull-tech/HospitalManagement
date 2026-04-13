import { useEffect, useState } from 'react'
import {
  listSubscriptions, createSubscription, updateSubscriptionStatus,
  listHospitals, listPlans,
  type HospitalSubscription, type HospitalDto, type SubscriptionPlan
} from '../../api/superAdmin'
import styles from './SuperAdmin.module.css'

const STATUS_BADGE: Record<string, string> = {
  ACTIVE: styles.badgeActive,
  TRIAL: styles.badgeTrial,
  EXPIRED: styles.badgeExpired,
  SUSPENDED: styles.badgeSuspended,
  CANCELLED: styles.badgeInactive,
}

export function SuperAdminSubscriptionsPage() {
  const [subs, setSubs] = useState<HospitalSubscription[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)

  const load = () => {
    setLoading(true)
    listSubscriptions()
      .then(setSubs)
      .catch((e) => setError(e?.response?.data?.message || 'Failed to load subscriptions'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await updateSubscriptionStatus(id, status)
      load()
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Failed to update status')
    }
  }

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>Subscriptions</h1>
        <button className={styles.primaryBtn} onClick={() => setShowModal(true)}>+ New Subscription</button>
      </div>

      {error && <div className={styles.errorBanner}>{error}</div>}
      {loading ? (
        <div className={styles.loading}>Loading…</div>
      ) : subs.length === 0 ? (
        <div className={styles.empty}>No subscriptions found.</div>
      ) : (
        <div className={styles.card}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Hospital</th>
                <th>Plan</th>
                <th>Status</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Billing</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {subs.map((s) => (
                <tr key={s.id}>
                  <td>{s.hospitalName}</td>
                  <td>{s.planName}</td>
                  <td><span className={STATUS_BADGE[s.status] || styles.badgeInactive}>{s.status}</span></td>
                  <td>{s.startDate}</td>
                  <td>{s.endDate || '—'}</td>
                  <td>{s.billingCycle || '—'}</td>
                  <td>
                    <select
                      value={s.status}
                      onChange={(e) => handleStatusChange(s.id, e.target.value)}
                      className={styles.input}
                      style={{ width: 'auto', padding: '0.25rem 0.5rem', fontSize: '0.8125rem' }}
                    >
                      <option value="TRIAL">Trial</option>
                      <option value="ACTIVE">Active</option>
                      <option value="EXPIRED">Expired</option>
                      <option value="SUSPENDED">Suspended</option>
                      <option value="CANCELLED">Cancelled</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && <AddSubscriptionModal onClose={() => setShowModal(false)} onCreated={load} />}
    </div>
  )
}

function AddSubscriptionModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [hospitals, setHospitals] = useState<HospitalDto[]>([])
  const [plans, setPlans] = useState<SubscriptionPlan[]>([])
  const [form, setForm] = useState({ hospitalId: '', planId: '', billingCycle: 'MONTHLY', notes: '' })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([listHospitals(true), listPlans(true)])
      .then(([h, p]) => { setHospitals(h); setPlans(p) })
      .catch(() => setError('Failed to load form data'))
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await createSubscription({
        hospitalId: parseInt(form.hospitalId, 10),
        planId: parseInt(form.planId, 10),
        billingCycle: form.billingCycle,
        notes: form.notes || undefined,
      })
      onCreated()
      onClose()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to create subscription')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h2 className={styles.modalTitle}>New Subscription</h2>
        <form className={styles.form} onSubmit={handleSubmit}>
          {error && <div className={styles.errorBanner}>{error}</div>}
          <div className={styles.formRow}>
            <label className={styles.label}>Hospital *</label>
            <select className={styles.select} required value={form.hospitalId}
              onChange={(e) => setForm({ ...form, hospitalId: e.target.value })}>
              <option value="">Select hospital</option>
              {hospitals.map((h) => <option key={h.id} value={h.id}>{h.hospitalName}</option>)}
            </select>
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Plan *</label>
            <select className={styles.select} required value={form.planId}
              onChange={(e) => setForm({ ...form, planId: e.target.value })}>
              <option value="">Select plan</option>
              {plans.map((p) => <option key={p.id} value={p.id}>{p.planName} — ₹{p.monthlyPrice}/mo</option>)}
            </select>
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Billing Cycle</label>
            <select className={styles.select} value={form.billingCycle}
              onChange={(e) => setForm({ ...form, billingCycle: e.target.value })}>
              <option value="MONTHLY">Monthly</option>
              <option value="QUARTERLY">Quarterly (~5% off)</option>
              <option value="YEARLY">Yearly (~15–20% off)</option>
            </select>
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Notes</label>
            <input className={styles.input} value={form.notes}
              onChange={(e) => setForm({ ...form, notes: e.target.value })} />
          </div>
          <div className={styles.formActions}>
            <button type="button" className={styles.secondaryBtn} onClick={onClose}>Cancel</button>
            <button type="submit" className={styles.primaryBtn} disabled={saving}>
              {saving ? 'Creating…' : 'Create Subscription'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
