import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listHospitals, createHospital, toggleHospitalStatus, type HospitalDto, type HospitalInput } from '../../api/superAdmin'
import styles from './SuperAdmin.module.css'

function getVisibleHospitalFormError(error: string, customDomain: string | null | undefined) {
  if (!error) {
    return ''
  }
  if (!(customDomain ?? '').trim() && error.includes('platform-managed HMS subdomains')) {
    return ''
  }
  return error
}

export function SuperAdminHospitalsPage() {
  const [hospitals, setHospitals] = useState<HospitalDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)

  const load = () => {
    setLoading(true)
    listHospitals(false)
      .then(setHospitals)
      .catch((e) => setError(e?.response?.data?.message || 'Failed to load hospitals'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleToggleStatus = async (id: number, currentActive: boolean) => {
    try {
      await toggleHospitalStatus(id, !currentActive)
      load()
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Failed to update status')
    }
  }

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>Hospitals</h1>
        <button className={styles.primaryBtn} onClick={() => setShowModal(true)}>+ Add Hospital</button>
      </div>

      {error && <div className={styles.errorBanner}>{error}</div>}
      {loading ? (
        <div className={styles.loading}>Loading…</div>
      ) : hospitals.length === 0 ? (
        <div className={styles.empty}>No hospitals found.</div>
      ) : (
        <div className={styles.card}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Tenant Route</th>
                <th>Location</th>
                <th>Contact</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {hospitals.map((h) => (
                <tr key={h.id}>
                  <td>{h.hospitalCode}</td>
                  <td><Link to={`/super-admin/hospitals/${h.id}`} className={styles.textBtn}>{h.hospitalName}</Link></td>
                  <td>{h.customDomain || (h.subdomain ? `${h.subdomain}.hms.com` : '—')}</td>
                  <td>{h.location || '—'}</td>
                  <td>{h.contactEmail || h.contactPhone || '—'}</td>
                  <td>
                    <span className={h.active ? styles.badgeActive : styles.badgeInactive}>
                      {h.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <button className={styles.textBtn} onClick={() => handleToggleStatus(h.id, h.active)}>
                      {h.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && <AddHospitalModal onClose={() => setShowModal(false)} onCreated={load} />}
    </div>
  )
}

function AddHospitalModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState<HospitalInput>({
    hospitalCode: '',
    hospitalName: '',
    location: '',
    subdomain: '',
    customDomain: '',
    domainVerificationToken: null,
    domainVerificationStatus: null,
    domainVerifiedAt: null,
    certificateStatus: null,
    certificateRequestedAt: null,
    certificateIssuedAt: null,
    certificateExpiresAt: null,
    lastDomainVerificationError: null,
    lastCertificateError: null,
    logoUrl: '',
    websiteUrl: '',
    facebookUrl: '',
    twitterUrl: '',
    instagramUrl: '',
    linkedinUrl: '',
    contactEmail: '',
    billingEmail: '',
    contactPhone: '',
    active: true,
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const visibleError = getVisibleHospitalFormError(error, form.customDomain)

  const updateForm = (patch: Partial<HospitalInput>) => {
    setError('')
    setForm((current) => ({ ...current, ...patch }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await createHospital({
        ...form,
        location: form.location || null,
        subdomain: form.subdomain || null,
        customDomain: form.customDomain || null,
        logoUrl: form.logoUrl || null,
        websiteUrl: form.websiteUrl || null,
        facebookUrl: form.facebookUrl || null,
        twitterUrl: form.twitterUrl || null,
        instagramUrl: form.instagramUrl || null,
        linkedinUrl: form.linkedinUrl || null,
        contactEmail: form.contactEmail || null,
        billingEmail: form.billingEmail || null,
        contactPhone: form.contactPhone || null,
      })
      onCreated()
      onClose()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to create hospital')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h2 className={styles.modalTitle}>Add Hospital</h2>
        <form className={styles.form} onSubmit={handleSubmit}>
          {visibleError && <div className={styles.errorBanner}>{visibleError}</div>}
          <div className={styles.formRow}>
            <label className={styles.label}>Hospital Code *</label>
            <input className={styles.input} required value={form.hospitalCode}
              onChange={(e) => updateForm({ hospitalCode: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Hospital Name *</label>
            <input className={styles.input} required value={form.hospitalName}
              onChange={(e) => updateForm({ hospitalName: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Location</label>
            <input className={styles.input} value={form.location || ''}
              onChange={(e) => updateForm({ location: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Subdomain</label>
            <input className={styles.input} value={form.subdomain || ''}
              onChange={(e) => updateForm({ subdomain: e.target.value.toLowerCase() })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Enterprise Custom Domain</label>
            <input className={styles.input} value={form.customDomain || ''}
              onChange={(e) => updateForm({ customDomain: e.target.value.toLowerCase() })}
              placeholder="example: app.cityhospital.com"
            />
            <div className={styles.fieldHelp}>Use this only for external hospital-owned domains. For `cityhospital.hms.com`, set `subdomain` to `cityhospital` instead.</div>
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Logo URL</label>
            <input className={styles.input} value={form.logoUrl || ''}
              onChange={(e) => updateForm({ logoUrl: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Contact Email</label>
            <input className={styles.input} type="email" value={form.contactEmail || ''}
              onChange={(e) => updateForm({ contactEmail: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Contact Phone</label>
            <input className={styles.input} value={form.contactPhone || ''}
              onChange={(e) => updateForm({ contactPhone: e.target.value })} />
          </div>
          <div className={styles.formActions}>
            <button type="button" className={styles.secondaryBtn} onClick={onClose}>Cancel</button>
            <button type="submit" className={styles.primaryBtn} disabled={saving}>
              {saving ? 'Creating…' : 'Create Hospital'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
