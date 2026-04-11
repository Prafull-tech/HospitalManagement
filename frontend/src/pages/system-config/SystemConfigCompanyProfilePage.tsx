import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { companyProfileApi } from '../../api/system'
import { apiErrorWithNetworkHint } from '../../utils/apiNetworkError'
import type { CompanyProfileResponse, CompanyProfileRequest } from '../../types/system'
import styles from './SystemConfigShared.module.css'
import shared from '../../styles/Dashboard.module.css'

const EMPTY_FORM: CompanyProfileRequest = {
  companyName: '',
  brandName: '',
  logoText: '',
  logoUrl: '',
  supportEmail: '',
  supportPhone: '',
  addressText: '',
}

export function SystemConfigCompanyProfilePage() {
  const [form, setForm] = useState<CompanyProfileRequest>(EMPTY_FORM)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const load = () => {
    setLoading(true)
    setError('')
    setSuccess('')
    companyProfileApi
      .get()
      .then((profile: CompanyProfileResponse) => {
        setForm({
          companyName: profile.companyName ?? '',
          brandName: profile.brandName ?? '',
          logoText: profile.logoText ?? '',
          logoUrl: profile.logoUrl ?? '',
          supportEmail: profile.supportEmail ?? '',
          supportPhone: profile.supportPhone ?? '',
          addressText: profile.addressText ?? '',
        })
      })
      .catch((err: unknown) => {
        setError(apiErrorWithNetworkHint('Failed to load company profile.', err))
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const setField = (field: keyof CompanyProfileRequest, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      await companyProfileApi.update(form)
      setSuccess('Company profile updated.')
      load()
    } catch (err: unknown) {
      setError(apiErrorWithNetworkHint('Failed to save company profile.', err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h1 className={shared.pageTitle}>Company Profile</h1>
        <p className={shared.pageSubtitle}>
          Update the brand name, logo, and company contact details used across the public website.
        </p>
      </div>

      <nav className={styles.tabs}>
        <Link to="/admin/config/company-profile" className={styles.tabActive}>Company Profile</Link>
        <Link to="/admin/config/roles" className={styles.tab}>Roles</Link>
        <Link to="/admin/config/modules" className={styles.tab}>Modules</Link>
        <Link to="/admin/config/permissions" className={styles.tab}>Permissions</Link>
        <Link to="/admin/config/features" className={styles.tab}>Feature Toggles</Link>
      </nav>

      {error && <div className={styles.errorBanner}>{error}<button type="button" className={styles.retryBtn} onClick={load}>Retry</button></div>}
      {success && <div className={styles.card} style={{ padding: '0.85rem 1rem', color: 'var(--hms-success)' }}>{success}</div>}
      {loading ? <div className={styles.loading}>Loading…</div> : (
        <div className={styles.card}>
          <form className={styles.form} onSubmit={handleSubmit}>
            <div className={styles.formRow}>
              <label className={styles.label}>Company Name</label>
              <input className={styles.input} value={form.companyName} onChange={(e) => setField('companyName', e.target.value)} />
            </div>
            <div className={styles.formRow}>
              <label className={styles.label}>Brand Name</label>
              <input className={styles.input} value={form.brandName} onChange={(e) => setField('brandName', e.target.value)} />
            </div>
            <div className={styles.formRow}>
              <label className={styles.label}>Logo Text</label>
              <input className={styles.input} value={form.logoText ?? ''} onChange={(e) => setField('logoText', e.target.value)} placeholder="Shown when no logo image URL is set" />
            </div>
            <div className={styles.formRow}>
              <label className={styles.label}>Logo Image URL</label>
              <input className={styles.input} value={form.logoUrl ?? ''} onChange={(e) => setField('logoUrl', e.target.value)} placeholder="https://.../logo.png" />
            </div>
            <div className={styles.formRow}>
              <label className={styles.label}>Support Email</label>
              <input className={styles.input} type="email" value={form.supportEmail ?? ''} onChange={(e) => setField('supportEmail', e.target.value)} />
            </div>
            <div className={styles.formRow}>
              <label className={styles.label}>Support Phone</label>
              <input className={styles.input} value={form.supportPhone ?? ''} onChange={(e) => setField('supportPhone', e.target.value)} />
            </div>
            <div className={styles.formRow}>
              <label className={styles.label}>Address</label>
              <textarea className={styles.input} rows={4} value={form.addressText ?? ''} onChange={(e) => setField('addressText', e.target.value)} />
            </div>
            <div className={styles.modalActions}>
              <button type="button" className={styles.secondaryBtn} onClick={load} disabled={saving}>Reset</button>
              <button type="submit" className={styles.primaryBtn} disabled={saving}>{saving ? 'Saving…' : 'Save Company Profile'}</button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}