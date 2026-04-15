import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  getHospital, getHospitalUsers, createHospitalUser, toggleUserStatus, updateHospital,
  getHospitalModules, updateHospitalModules,
  regenerateHospitalCustomDomainToken, verifyHospitalCustomDomain, requestHospitalCertificate, updateHospitalCertificate,
  type HospitalDto, type HospitalUser, type CreateHospitalUserInput, type HospitalInput,
  type HospitalModuleConfig, type HospitalModuleConfigItem
} from '../../api/superAdmin'
import { fileToLogoDataUrl, normalizeLogoSrc } from '../../lib/logoImage'
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

const HOSPITAL_ROLES = [
  'ADMIN', 'RECEPTIONIST', 'FRONT_DESK', 'DOCTOR', 'NURSE', 'BILLING',
  'IPD_MANAGER', 'IPD_PHARMACIST', 'PHARMACIST', 'PHARMACY_MANAGER',
  'STORE_INCHARGE', 'QUALITY_MANAGER', 'LAB_TECH', 'LAB_TECHNICIAN',
  'LAB_SUPERVISOR', 'PATHOLOGIST', 'PHLEBOTOMIST', 'RADIOLOGY_TECH',
  'BLOOD_BANK_TECH', 'HOUSEKEEPING', 'HELP_DESK',
]

function buildModuleDraft(config: HospitalModuleConfig | null) {
  if (!config) {
    return {} as Record<string, boolean>
  }
  return config.modules.reduce<Record<string, boolean>>((draft, module) => {
    draft[module.moduleCode] = module.enabled
    return draft
  }, {})
}

export function SuperAdminHospitalDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [hospital, setHospital] = useState<HospitalDto | null>(null)
  const [users, setUsers] = useState<HospitalUser[]>([])
  const [moduleConfig, setModuleConfig] = useState<HospitalModuleConfig | null>(null)
  const [moduleDraft, setModuleDraft] = useState<Record<string, boolean>>({})
  const [moduleLoading, setModuleLoading] = useState(true)
  const [moduleSaving, setModuleSaving] = useState(false)
  const [moduleError, setModuleError] = useState('')
  const [moduleSuccess, setModuleSuccess] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)

  const hId = id ? parseInt(id, 10) : 0
  const adminCount = users.filter((user) => user.role === 'ADMIN').length
  const doctorCount = users.filter((user) => user.role === 'DOCTOR').length
  const nurseCount = users.filter((user) => user.role === 'NURSE').length

  const load = () => {
    if (!hId) return
    setLoading(true)
    Promise.all([getHospital(hId), getHospitalUsers(hId)])
      .then(([h, u]) => { setHospital(h); setUsers(u) })
      .catch((e) => setError(e?.response?.data?.message || 'Failed to load hospital'))
      .finally(() => setLoading(false))
  }

  const loadModuleConfig = () => {
    if (!hId) return
    setModuleLoading(true)
    setModuleError('')
    getHospitalModules(hId)
      .then((config) => {
        setModuleConfig(config)
        setModuleDraft(buildModuleDraft(config))
      })
      .catch((e) => setModuleError(e?.response?.data?.message || 'Failed to load hospital modules'))
      .finally(() => setModuleLoading(false))
  }

  useEffect(() => {
    load()
    loadModuleConfig()
  }, [id])

  const handleToggleUser = async (userId: number, currentActive: boolean) => {
    try {
      await toggleUserStatus(hId, userId, !currentActive)
      load()
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Failed to update user status')
    }
  }

  const hasModuleChanges = moduleConfig
    ? moduleConfig.modules.some((module) => moduleDraft[module.moduleCode] !== module.enabled)
    : false

  const handleModuleToggle = (moduleCode: string, enabled: boolean) => {
    setModuleSuccess('')
    setModuleDraft((current) => ({ ...current, [moduleCode]: enabled }))
  }

  const handleModuleReset = () => {
    setModuleError('')
    setModuleSuccess('')
    setModuleDraft(buildModuleDraft(moduleConfig))
  }

  const handleModuleSave = async () => {
    if (!moduleConfig) {
      return
    }

    const changedModules = moduleConfig.modules
      .filter((module) => moduleDraft[module.moduleCode] !== module.enabled)
      .map((module) => ({
        moduleCode: module.moduleCode,
        enabled: moduleDraft[module.moduleCode],
      }))

    if (changedModules.length === 0) {
      setModuleSuccess('No changes to save.')
      return
    }

    setModuleSaving(true)
    setModuleError('')
    setModuleSuccess('')
    try {
      const updatedConfig = await updateHospitalModules(hId, { modules: changedModules })
      setModuleConfig(updatedConfig)
      setModuleDraft(buildModuleDraft(updatedConfig))
      setModuleSuccess('Hospital module configuration updated.')
    } catch (e: any) {
      setModuleError(e?.response?.data?.message || 'Failed to update hospital modules')
    } finally {
      setModuleSaving(false)
    }
  }

  if (loading) return <div className={styles.loading}>Loading…</div>
  if (error) return <div className={styles.errorBanner}>{error}</div>
  if (!hospital) return <div className={styles.empty}>Hospital not found</div>
  const hospitalLogoSrc = normalizeLogoSrc(hospital.logoUrl)

  return (
    <div>
      <div className={styles.pageHeader}>
        <div>
          <Link to="/super-admin/hospitals" className={styles.textBtn}>← Back to Hospitals</Link>
          <h1 className={styles.pageTitle}>{hospital.hospitalName}</h1>
        </div>
        <button className={styles.secondaryBtn} onClick={() => setShowEditModal(true)}>Edit Details</button>
      </div>

      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Code</p>
          <p className={styles.statValue} style={{ fontSize: '1.25rem' }}>{hospital.hospitalCode}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Location</p>
          <p className={styles.statValue} style={{ fontSize: '1.25rem' }}>{hospital.location || '—'}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Tenant URL</p>
          <p className={styles.statValue} style={{ fontSize: '1rem' }}>{hospital.customDomain || (hospital.subdomain ? `${hospital.subdomain}.hms.com` : '—')}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Status</p>
          <p className={styles.statValue} style={{ fontSize: '1.25rem' }}>
            <span className={hospital.active ? styles.badgeActive : styles.badgeInactive}>
              {hospital.active ? 'Active' : 'Inactive'}
            </span>
          </p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Users</p>
          <p className={styles.statValue}>{users.length}</p>
        </div>
      </div>

      <div className={styles.detailGrid}>
        <section className={styles.sectionCard}>
          <h2 className={styles.detailTitle}>Hospital Profile</h2>
          {hospitalLogoSrc ? <img src={hospitalLogoSrc} alt={hospital.hospitalName} className={styles.logoPreview} /> : null}
          <div className={styles.fieldList}>
            <div>
              <span className={styles.fieldLabel}>Name</span>
              <div className={styles.fieldValue}>{hospital.hospitalName}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Address / Location</span>
              <div className={styles.fieldValue}>{hospital.location || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Website</span>
              <div className={styles.fieldValue}>{hospital.websiteUrl || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Subdomain</span>
              <div className={styles.fieldValue}>{hospital.subdomain ? `${hospital.subdomain}.hms.com` : '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Enterprise Custom Domain</span>
              <div className={styles.fieldValue}>{hospital.customDomain || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Onboarding Status</span>
              <div className={styles.fieldValue}>{hospital.onboardingStatus || 'PENDING'}</div>
            </div>
          </div>
        </section>

        <section className={styles.sectionCard}>
          <h2 className={styles.detailTitle}>Social Media</h2>
          <div className={styles.fieldList}>
            <div>
              <span className={styles.fieldLabel}>Facebook</span>
              <div className={styles.fieldValue}>{hospital.facebookUrl || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Twitter</span>
              <div className={styles.fieldValue}>{hospital.twitterUrl || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Instagram</span>
              <div className={styles.fieldValue}>{hospital.instagramUrl || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>LinkedIn</span>
              <div className={styles.fieldValue}>{hospital.linkedinUrl || '—'}</div>
            </div>
          </div>
        </section>

        <section className={styles.sectionCard}>
          <h2 className={styles.detailTitle}>Contact & Billing</h2>
          <div className={styles.fieldList}>
            <div>
              <span className={styles.fieldLabel}>Support Email</span>
              <div className={styles.fieldValue}>{hospital.contactEmail || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Billing Email</span>
              <div className={styles.fieldValue}>{hospital.billingEmail || '—'}</div>
            </div>
            <div>
              <span className={styles.fieldLabel}>Contact Phone</span>
              <div className={styles.fieldValue}>{hospital.contactPhone || '—'}</div>
            </div>
          </div>
        </section>

        <section className={styles.sectionCard}>
          <CustomDomainPanel hospital={hospital} onUpdated={load} />
        </section>

        <section className={styles.sectionCard}>
          <h2 className={styles.detailTitle}>Staff & Roles</h2>
          <div className={styles.roleSummary}>
            <div className={styles.rolePill}>Admins<strong>{adminCount}</strong></div>
            <div className={styles.rolePill}>Doctors<strong>{doctorCount}</strong></div>
            <div className={styles.rolePill}>Nurses<strong>{nurseCount}</strong></div>
            <div className={styles.rolePill}>Total Users<strong>{users.length}</strong></div>
          </div>
        </section>

        <section className={`${styles.sectionCard} ${styles.sectionCardFull}`}>
          <ModuleConfigurationSection
            config={moduleConfig}
            draft={moduleDraft}
            loading={moduleLoading}
            saving={moduleSaving}
            error={moduleError}
            success={moduleSuccess}
            hasChanges={hasModuleChanges}
            onToggle={handleModuleToggle}
            onReset={handleModuleReset}
            onSave={handleModuleSave}
          />
        </section>
      </div>

      <div className={styles.pageHeader} style={{ marginTop: '2rem' }}>
        <h2 style={{ fontWeight: 600 }}>Hospital Users</h2>
        <button className={styles.primaryBtn} onClick={() => setShowModal(true)}>+ Add User</button>
      </div>

      {users.length === 0 ? (
        <div className={styles.empty}>No users assigned to this hospital.</div>
      ) : (
        <div className={styles.card}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Username</th>
                <th>Full Name</th>
                <th>Role</th>
                <th>Email</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.username}</td>
                  <td>{u.fullName}</td>
                  <td>{u.role}</td>
                  <td>{u.email || '—'}</td>
                  <td>
                    <span className={u.active ? styles.badgeActive : styles.badgeInactive}>
                      {u.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <button
                      className={styles.textBtn}
                      onClick={() => handleToggleUser(u.id, u.active)}
                    >
                      {u.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <AddUserModal
          hospitalId={hId}
          onClose={() => setShowModal(false)}
          onCreated={load}
        />
      )}

      {showEditModal && (
        <EditHospitalModal
          hospital={hospital}
          onClose={() => setShowEditModal(false)}
          onSaved={load}
        />
      )}
    </div>
  )
}

function ModuleConfigurationSection({
  config,
  draft,
  loading,
  saving,
  error,
  success,
  hasChanges,
  onToggle,
  onReset,
  onSave,
}: {
  config: HospitalModuleConfig | null
  draft: Record<string, boolean>
  loading: boolean
  saving: boolean
  error: string
  success: string
  hasChanges: boolean
  onToggle: (moduleCode: string, enabled: boolean) => void
  onReset: () => void
  onSave: () => void
}) {
  const groupedModules = (config?.modules || []).reduce<Record<string, HospitalModuleConfigItem[]>>((groups, module) => {
    if (!groups[module.moduleCategory]) {
      groups[module.moduleCategory] = []
    }
    groups[module.moduleCategory].push(module)
    return groups
  }, {})

  return (
    <>
      <div className={styles.moduleHeader}>
        <div>
          <h2 className={styles.detailTitle}>Module Configuration</h2>
          <p className={styles.fieldHelp}>
            Super admin can enable or disable any module for this hospital. The assigned plan is shown only as a reference.
          </p>
        </div>
        <div className={styles.modulePlanMeta}>
          <span className={styles.fieldLabel}>Current Plan</span>
          <div className={styles.fieldValue}>{config?.hasActivePlan ? (config.planName || config.planCode || 'Assigned plan') : 'No active plan assigned'}</div>
        </div>
      </div>

      {error && <div className={styles.errorBanner}>{error}</div>}
      {success && <div className={styles.successBanner}>{success}</div>}

      {loading ? (
        <div className={styles.loading}>Loading module configuration…</div>
      ) : (
        <>
          <div className={styles.moduleCategoryGrid}>
            {Object.entries(groupedModules).map(([category, modules]) => (
              <div key={category} className={styles.moduleCategoryCard}>
                <div className={styles.moduleCategoryTitle}>{category.split('_').join(' ')}</div>
                <div className={styles.moduleList}>
                  {modules.map((module) => {
                    const checked = draft[module.moduleCode] ?? module.enabled
                    return (
                      <div key={module.moduleCode} className={styles.moduleRow}>
                        <div className={styles.moduleInfo}>
                          <div className={styles.moduleName}>{module.moduleName}</div>
                          <div className={styles.moduleCode}>{module.moduleCode}</div>
                        </div>
                        <div className={styles.moduleState}>
                          <label className={styles.moduleToggle}>
                            <input
                              type="checkbox"
                              checked={checked}
                              onChange={(e) => onToggle(module.moduleCode, e.target.checked)}
                              disabled={saving}
                            />
                            <span>{checked ? 'Enabled' : 'Disabled'}</span>
                          </label>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            ))}
          </div>

          <div className={styles.formActions}>
            <button type="button" className={styles.secondaryBtn} onClick={onReset} disabled={saving || !hasChanges}>Reset</button>
            <button type="button" className={styles.primaryBtn} onClick={onSave} disabled={saving || !hasChanges}>
              {saving ? 'Saving…' : 'Save Module Changes'}
            </button>
          </div>
        </>
      )}
    </>
  )
}

function EditHospitalModal({ hospital, onClose, onSaved }: {
  hospital: HospitalDto
  onClose: () => void
  onSaved: () => void
}) {
  const [form, setForm] = useState<HospitalInput>({
    hospitalCode: hospital.hospitalCode,
    hospitalName: hospital.hospitalName,
    location: hospital.location || '',
    subdomain: hospital.subdomain || '',
    customDomain: hospital.customDomain || '',
    domainVerificationToken: hospital.domainVerificationToken,
    domainVerificationStatus: hospital.domainVerificationStatus,
    domainVerifiedAt: hospital.domainVerifiedAt,
    certificateStatus: hospital.certificateStatus,
    certificateRequestedAt: hospital.certificateRequestedAt,
    certificateIssuedAt: hospital.certificateIssuedAt,
    certificateExpiresAt: hospital.certificateExpiresAt,
    lastDomainVerificationError: hospital.lastDomainVerificationError,
    lastCertificateError: hospital.lastCertificateError,
    logoUrl: hospital.logoUrl || '',
    websiteUrl: hospital.websiteUrl || '',
    facebookUrl: hospital.facebookUrl || '',
    twitterUrl: hospital.twitterUrl || '',
    instagramUrl: hospital.instagramUrl || '',
    linkedinUrl: hospital.linkedinUrl || '',
    contactEmail: hospital.contactEmail || '',
    billingEmail: hospital.billingEmail || '',
    contactPhone: hospital.contactPhone || '',
    active: hospital.active,
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [uploadingLogo, setUploadingLogo] = useState(false)
  const visibleError = getVisibleHospitalFormError(error, form.customDomain)
  const logoPreviewSrc = normalizeLogoSrc(form.logoUrl)

  const updateForm = (patch: Partial<HospitalInput>) => {
    setError('')
    setForm((current) => ({ ...current, ...patch }))
  }

  const handleLogoFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const selected = event.target.files?.[0]
    if (!selected) return

    setUploadingLogo(true)
    setError('')
    try {
      const dataUrl = await fileToLogoDataUrl(selected)
      updateForm({ logoUrl: dataUrl })
    } catch (e: any) {
      setError(e?.message || 'Unable to upload logo image')
    } finally {
      setUploadingLogo(false)
      event.target.value = ''
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await updateHospital(hospital.id, {
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
      onSaved()
      onClose()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to update hospital')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalHeader}>
          <h2 className={styles.modalTitle}>Edit Hospital Details</h2>
          <button type="button" className={styles.modalCloseBtn} onClick={onClose} aria-label="Close edit hospital dialog">×</button>
        </div>
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
              onChange={(e) => updateForm({ logoUrl: e.target.value })}
              placeholder="https://.../logo.png"
            />
            <div className={styles.formActions}>
              <label className={styles.secondaryBtn}>
                {uploadingLogo ? 'Uploading…' : 'Upload image'}
                <input
                  type="file"
                  accept="image/png,image/jpeg,image/webp,image/svg+xml"
                  onChange={handleLogoFileChange}
                  style={{ display: 'none' }}
                  disabled={uploadingLogo || saving}
                />
              </label>
              {form.logoUrl ? (
                <button
                  type="button"
                  className={styles.textBtn}
                  onClick={() => updateForm({ logoUrl: '' })}
                >
                  Clear logo
                </button>
              ) : null}
            </div>
            <div className={styles.fieldHelp}>You can paste a logo URL or upload an image (max 300 KB).</div>
            {logoPreviewSrc ? <img src={logoPreviewSrc} alt="Logo preview" className={styles.logoPreview} /> : null}
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Website</label>
            <input className={styles.input} value={form.websiteUrl || ''}
              onChange={(e) => updateForm({ websiteUrl: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Facebook URL</label>
            <input className={styles.input} value={form.facebookUrl || ''}
              onChange={(e) => updateForm({ facebookUrl: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Twitter URL</label>
            <input className={styles.input} value={form.twitterUrl || ''}
              onChange={(e) => updateForm({ twitterUrl: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Instagram URL</label>
            <input className={styles.input} value={form.instagramUrl || ''}
              onChange={(e) => updateForm({ instagramUrl: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>LinkedIn URL</label>
            <input className={styles.input} value={form.linkedinUrl || ''}
              onChange={(e) => updateForm({ linkedinUrl: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Support Email</label>
            <input className={styles.input} type="email" value={form.contactEmail || ''}
              onChange={(e) => updateForm({ contactEmail: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Billing Email</label>
            <input className={styles.input} type="email" value={form.billingEmail || ''}
              onChange={(e) => updateForm({ billingEmail: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Contact Phone</label>
            <input className={styles.input} value={form.contactPhone || ''}
              onChange={(e) => updateForm({ contactPhone: e.target.value })} />
          </div>
          <div className={styles.formActions}>
            <button type="button" className={styles.secondaryBtn} onClick={onClose}>Cancel</button>
            <button type="submit" className={styles.primaryBtn} disabled={saving}>
              {saving ? 'Saving…' : 'Save Details'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

function CustomDomainPanel({ hospital, onUpdated }: { hospital: HospitalDto; onUpdated: () => void }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [certificateForm, setCertificateForm] = useState({
    status: hospital.certificateStatus || 'NOT_REQUESTED',
    issuedAt: hospital.certificateIssuedAt ? hospital.certificateIssuedAt.slice(0, 19) : '',
    expiresAt: hospital.certificateExpiresAt ? hospital.certificateExpiresAt.slice(0, 19) : '',
    errorMessage: hospital.lastCertificateError || '',
  })

  const challengePath = '/.well-known/hms-domain-verification'

  const runAction = async (action: () => Promise<unknown>, successMessage: string) => {
    setBusy(true)
    setError('')
    setMessage('')
    try {
      await action()
      setMessage(successMessage)
      onUpdated()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Operation failed')
    } finally {
      setBusy(false)
    }
  }

  const handleCertificateSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    await runAction(
      () => updateHospitalCertificate(hospital.id, {
        status: certificateForm.status,
        issuedAt: certificateForm.issuedAt ? new Date(certificateForm.issuedAt).toISOString() : null,
        expiresAt: certificateForm.expiresAt ? new Date(certificateForm.expiresAt).toISOString() : null,
        errorMessage: certificateForm.errorMessage || null,
      }),
      'Certificate lifecycle updated.'
    )
  }

  if (!hospital.customDomain) {
    return (
      <>
        <h2 className={styles.detailTitle}>Enterprise Custom Domain</h2>
        <div className={styles.fieldList}>
          <div>
            <span className={styles.fieldLabel}>Configured Domain</span>
            <div className={styles.fieldValue}>Not configured</div>
          </div>
          <div>
            <span className={styles.fieldLabel}>Tenant Route</span>
            <div className={styles.fieldValue}>{hospital.subdomain ? `${hospital.subdomain}.hms.com` : 'No subdomain configured'}</div>
          </div>
        </div>
        <p className={styles.fieldHelp}>This hospital is using an HMS-managed tenant subdomain, so enterprise-domain verification is not required.</p>
        <p className={styles.fieldHelp}>Platform admins continue to manage this hospital from `localhost:3000` or the admin host. Hospital staff sign in on `{hospital.subdomain ? `${hospital.subdomain}.hms.com` : 'the hospital subdomain'}` once DNS is pointed to the platform.</p>
      </>
    )
  }

  return (
    <>
      <h2 className={styles.detailTitle}>Enterprise Custom Domain</h2>
      {error && <div className={styles.errorBanner}>{error}</div>}
      {message && <div className={styles.successBanner}>{message}</div>}
      <div className={styles.fieldList}>
        <div>
          <span className={styles.fieldLabel}>Configured Domain</span>
          <div className={styles.fieldValue}>{hospital.customDomain || '—'}</div>
        </div>
        <div>
          <span className={styles.fieldLabel}>Verification Status</span>
          <div className={styles.fieldValue}>{hospital.domainVerificationStatus || 'NOT_CONFIGURED'}</div>
        </div>
        <div>
          <span className={styles.fieldLabel}>Certificate Status</span>
          <div className={styles.fieldValue}>{hospital.certificateStatus || 'NOT_REQUESTED'}</div>
        </div>
        <div>
          <span className={styles.fieldLabel}>Verification Token</span>
          <div className={styles.codeBlock}>{hospital.domainVerificationToken || '—'}</div>
        </div>
        <div>
          <span className={styles.fieldLabel}>HTTP Challenge Path</span>
          <div className={styles.codeBlock}>{challengePath}</div>
        </div>
        <div>
          <span className={styles.fieldLabel}>Certificate Expires At</span>
          <div className={styles.fieldValue}>{hospital.certificateExpiresAt || '—'}</div>
        </div>
      </div>
      <p className={styles.fieldHelp}>Use enterprise custom domain only for externally managed hosts such as `ehr.cityhospital.com`. HMS-managed hosts like `cityhospital.hms.com` belong in the subdomain field.</p>
      {hospital.lastDomainVerificationError ? <p className={styles.inlineError}>{hospital.lastDomainVerificationError}</p> : null}
      <div className={styles.inlineActions}>
        <button type="button" className={styles.secondaryBtn} disabled={busy || !hospital.customDomain} onClick={() => runAction(() => regenerateHospitalCustomDomainToken(hospital.id), 'Verification token regenerated.')}>Regenerate Token</button>
        <button type="button" className={styles.primaryBtn} disabled={busy || !hospital.customDomain} onClick={() => runAction(() => verifyHospitalCustomDomain(hospital.id), 'Domain verified successfully.')}>Verify Domain</button>
        <button type="button" className={styles.secondaryBtn} disabled={busy || !hospital.customDomain} onClick={() => runAction(() => requestHospitalCertificate(hospital.id), 'Certificate request submitted.')}>Request Certificate</button>
      </div>

      <form className={styles.inlineForm} onSubmit={handleCertificateSubmit}>
        <div className={styles.formRow}>
          <label className={styles.label}>Certificate Status</label>
          <select className={styles.select} value={certificateForm.status} onChange={(e) => setCertificateForm((prev) => ({ ...prev, status: e.target.value }))}>
            {['NOT_REQUESTED', 'REQUESTED', 'ISSUED', 'EXPIRING_SOON', 'EXPIRED', 'FAILED'].map((status) => (
              <option key={status} value={status}>{status}</option>
            ))}
          </select>
        </div>
        <div className={styles.formRow}>
          <label className={styles.label}>Issued At</label>
          <input className={styles.input} type="datetime-local" value={certificateForm.issuedAt} onChange={(e) => setCertificateForm((prev) => ({ ...prev, issuedAt: e.target.value }))} />
        </div>
        <div className={styles.formRow}>
          <label className={styles.label}>Expires At</label>
          <input className={styles.input} type="datetime-local" value={certificateForm.expiresAt} onChange={(e) => setCertificateForm((prev) => ({ ...prev, expiresAt: e.target.value }))} />
        </div>
        <div className={styles.formRow}>
          <label className={styles.label}>Certificate Error</label>
          <input className={styles.input} value={certificateForm.errorMessage} onChange={(e) => setCertificateForm((prev) => ({ ...prev, errorMessage: e.target.value }))} />
        </div>
        <div className={styles.formActions}>
          <button type="submit" className={styles.primaryBtn} disabled={busy || !hospital.customDomain}>Save Certificate State</button>
        </div>
      </form>
    </>
  )
}

function AddUserModal({ hospitalId, onClose, onCreated }: {
  hospitalId: number
  onClose: () => void
  onCreated: () => void
}) {
  const [form, setForm] = useState<CreateHospitalUserInput>({
    username: '', fullName: '', password: '', role: 'ADMIN', email: '', phone: '',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      await createHospitalUser(hospitalId, {
        ...form,
        email: form.email || undefined,
        phone: form.phone || undefined,
      })
      onCreated()
      onClose()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to create user')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h2 className={styles.modalTitle}>Add Hospital User</h2>
        <form className={styles.form} onSubmit={handleSubmit}>
          {error && <div className={styles.errorBanner}>{error}</div>}
          <div className={styles.formRow}>
            <label className={styles.label}>Username *</label>
            <input className={styles.input} required value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Full Name *</label>
            <input className={styles.input} required value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Password *</label>
            <input className={styles.input} type="password" required minLength={8} value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Role *</label>
            <select className={styles.select} required value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}>
              {HOSPITAL_ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Email</label>
            <input className={styles.input} type="email" value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className={styles.formRow}>
            <label className={styles.label}>Phone</label>
            <input className={styles.input} value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          </div>
          <div className={styles.formActions}>
            <button type="button" className={styles.secondaryBtn} onClick={onClose}>Cancel</button>
            <button type="submit" className={styles.primaryBtn} disabled={saving}>
              {saving ? 'Creating…' : 'Create User'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
