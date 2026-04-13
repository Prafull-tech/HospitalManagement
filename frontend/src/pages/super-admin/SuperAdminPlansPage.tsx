import { useEffect, useState } from 'react'
import { listPlans, createPlan, updatePlan, type SubscriptionPlan, type SubscriptionPlanInput } from '../../api/superAdmin'
import styles from './SuperAdmin.module.css'

export function SuperAdminPlansPage() {
  const [plans, setPlans] = useState<SubscriptionPlan[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editPlan, setEditPlan] = useState<SubscriptionPlan | null>(null)
  const [showModal, setShowModal] = useState(false)

  const load = () => {
    setLoading(true)
    listPlans(false)
      .then(setPlans)
      .catch((e) => setError(e?.response?.data?.message || 'Failed to load plans'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openEdit = (plan: SubscriptionPlan) => {
    setEditPlan(plan)
    setShowModal(true)
  }

  const openAdd = () => {
    setEditPlan(null)
    setShowModal(true)
  }

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>Subscription Plans</h1>
        <button className={styles.primaryBtn} onClick={openAdd}>+ Add Plan</button>
      </div>

      {error && <div className={styles.errorBanner}>{error}</div>}
      {loading ? (
        <div className={styles.loading}>Loading…</div>
      ) : plans.length === 0 ? (
        <div className={styles.empty}>No plans found.</div>
      ) : (
        <div className={styles.card}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Monthly</th>
                <th>Quarterly</th>
                <th>Yearly</th>
                <th>Max Users</th>
                <th>Max Beds</th>
                <th>Trial Days</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {plans.map((p) => (
                <tr key={p.id}>
                  <td>{p.planCode}</td>
                  <td>{p.planName}</td>
                  <td>₹{p.monthlyPrice}</td>
                  <td>{p.quarterlyPrice != null ? `₹${p.quarterlyPrice}` : '—'}</td>
                  <td>{p.yearlyPrice != null ? `₹${p.yearlyPrice}` : '—'}</td>
                  <td>{p.maxUsers ?? 'Unlimited'}</td>
                  <td>{p.maxBeds ?? 'Unlimited'}</td>
                  <td>{p.trialDays ?? '—'}</td>
                  <td>
                    <span className={p.active ? styles.badgeActive : styles.badgeInactive}>
                      {p.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <button className={styles.textBtn} onClick={() => openEdit(p)}>Edit</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <PlanFormModal
          existing={editPlan}
          onClose={() => { setShowModal(false); setEditPlan(null) }}
          onSaved={load}
        />
      )}
    </div>
  )
}

function PlanFormModal({ existing, onClose, onSaved }: {
  existing: SubscriptionPlan | null
  onClose: () => void
  onSaved: () => void
}) {
  const isEdit = !!existing

  // Derive initial discount percentages from existing prices
  const deriveDiscount = (monthly: number, total: number | null, months: number): number => {
    if (total == null || monthly <= 0) return 0
    const fullPrice = monthly * months
    const disc = ((fullPrice - total) / fullPrice) * 100
    return Math.round(disc * 100) / 100
  }

  const [form, setForm] = useState<SubscriptionPlanInput>({
    planCode: existing?.planCode ?? '',
    planName: existing?.planName ?? '',
    description: existing?.description ?? '',
    monthlyPrice: existing?.monthlyPrice ?? 0,
    quarterlyPrice: existing?.quarterlyPrice ?? null,
    yearlyPrice: existing?.yearlyPrice ?? null,
    maxUsers: existing?.maxUsers ?? null,
    maxBeds: existing?.maxBeds ?? null,
    enabledModules: existing?.enabledModules ?? null,
    active: existing?.active ?? true,
    trialDays: existing?.trialDays ?? null,
  })
  const [quarterlyDiscount, setQuarterlyDiscount] = useState(
    deriveDiscount(existing?.monthlyPrice ?? 0, existing?.quarterlyPrice ?? null, 3)
  )
  const [yearlyDiscount, setYearlyDiscount] = useState(
    deriveDiscount(existing?.monthlyPrice ?? 0, existing?.yearlyPrice ?? null, 12)
  )
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const calcPrice = (monthly: number, discountPct: number, months: number): number | null => {
    if (monthly <= 0) return null
    const full = monthly * months
    return Math.round(full * (1 - discountPct / 100))
  }

  const handleMonthlyChange = (val: number) => {
    setForm({
      ...form,
      monthlyPrice: val,
      quarterlyPrice: calcPrice(val, quarterlyDiscount, 3),
      yearlyPrice: calcPrice(val, yearlyDiscount, 12),
    })
  }

  const handleQuarterlyDiscountChange = (pct: number) => {
    setQuarterlyDiscount(pct)
    setForm({ ...form, quarterlyPrice: calcPrice(form.monthlyPrice, pct, 3) })
  }

  const handleYearlyDiscountChange = (pct: number) => {
    setYearlyDiscount(pct)
    setForm({ ...form, yearlyPrice: calcPrice(form.monthlyPrice, pct, 12) })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (isEdit && existing) {
        await updatePlan(existing.id, form)
      } else {
        await createPlan(form)
      }
      onSaved()
      onClose()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to save plan')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h2 className={styles.modalTitle}>{isEdit ? 'Edit Plan' : 'Add Plan'}</h2>
        <form className={styles.form} onSubmit={handleSubmit}>
          {error && <div className={styles.errorBanner}>{error}</div>}
          <div className={styles.formRow}>
            <label className={styles.label}>Plan Code *</label>
            <input className={styles.input} required value={form.planCode}
              onChange={(e) => setForm({ ...form, planCode: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Plan Name *</label>
            <input className={styles.input} required value={form.planName}
              onChange={(e) => setForm({ ...form, planName: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Description</label>
            <input className={styles.input} value={form.description ?? ''}
              onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Monthly Price *</label>
            <input className={styles.input} type="number" required min={0} step={0.01}
              value={form.monthlyPrice}
              onChange={(e) => handleMonthlyChange(parseFloat(e.target.value) || 0)} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Quarterly Discount %</label>
            <div className={styles.priceRow}>
              <input className={styles.inputSmall} type="number" min={0} max={100} step={0.5}
                value={quarterlyDiscount}
                onChange={(e) => handleQuarterlyDiscountChange(parseFloat(e.target.value) || 0)} />
              <span className={styles.pricePreview}>
                Quarterly Price: ₹{form.quarterlyPrice != null ? form.quarterlyPrice.toLocaleString('en-IN') : '—'}
              </span>
            </div>
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Yearly Discount %</label>
            <div className={styles.priceRow}>
              <input className={styles.inputSmall} type="number" min={0} max={100} step={0.5}
                value={yearlyDiscount}
                onChange={(e) => handleYearlyDiscountChange(parseFloat(e.target.value) || 0)} />
              <span className={styles.pricePreview}>
                Yearly Price: ₹{form.yearlyPrice != null ? form.yearlyPrice.toLocaleString('en-IN') : '—'}
              </span>
            </div>
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Max Users</label>
            <input className={styles.input} type="number" min={0}
              value={form.maxUsers ?? ''}
              onChange={(e) => setForm({ ...form, maxUsers: e.target.value ? parseInt(e.target.value, 10) : null })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Max Beds</label>
            <input className={styles.input} type="number" min={0}
              value={form.maxBeds ?? ''}
              onChange={(e) => setForm({ ...form, maxBeds: e.target.value ? parseInt(e.target.value, 10) : null })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Trial Days</label>
            <input className={styles.input} type="number" min={0}
              value={form.trialDays ?? ''}
              onChange={(e) => setForm({ ...form, trialDays: e.target.value ? parseInt(e.target.value, 10) : null })} />
          </div>
          <div className={styles.formActions}>
            <button type="button" className={styles.secondaryBtn} onClick={onClose}>Cancel</button>
            <button type="submit" className={styles.primaryBtn} disabled={saving}>
              {saving ? 'Saving…' : (isEdit ? 'Update Plan' : 'Create Plan')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
