import { useEffect, useState } from 'react'
import { listAdminHospitalUsers, createAdminHospitalUser, updateAdminHospitalUserStatus, resetAdminHospitalUserPassword, type AdminHospitalUser, type CreateAdminHospitalUserInput } from '../api/adminUsers'
import { useAuth } from '../contexts/AuthContext'
import { apiErrorWithNetworkHint } from '../utils/apiNetworkError'
import shared from '../styles/Dashboard.module.css'
import styles from './system-config/SystemConfigShared.module.css'

const HOSPITAL_ROLES = [
  'ADMIN', 'RECEPTIONIST', 'FRONT_DESK', 'DOCTOR', 'NURSE', 'BILLING',
  'IPD_MANAGER', 'IPD_PHARMACIST', 'PHARMACIST', 'PHARMACY_MANAGER',
  'STORE_INCHARGE', 'QUALITY_MANAGER', 'LAB_TECH', 'LAB_TECHNICIAN',
  'LAB_SUPERVISOR', 'PATHOLOGIST', 'PHLEBOTOMIST', 'RADIOLOGY_TECH',
  'BLOOD_BANK_TECH', 'HOUSEKEEPING', 'HELP_DESK',
] as const

type FormErrors = Partial<Record<'username' | 'fullName' | 'password' | 'confirmPassword' | 'email' | 'phone', string>>

function validateUserForm(form: CreateAdminHospitalUserInput, confirmPassword: string): FormErrors {
  const errors: FormErrors = {}
  const username = form.username.trim()
  const fullName = form.fullName.trim()
  const email = form.email?.trim() ?? ''
  const phone = form.phone?.trim() ?? ''
  const digits = phone.replace(/\D/g, '')

  if (!username) errors.username = 'Username is required.'
  else if (username.length < 3) errors.username = 'Username must be at least 3 characters.'
  else if (!/^[A-Za-z0-9._-]+$/.test(username)) errors.username = 'Username can contain only letters, numbers, dots, hyphens, and underscores.'

  if (!fullName) errors.fullName = 'Full name is required.'
  else if (fullName.length < 3) errors.fullName = 'Full name must be at least 3 characters.'

  if (!form.password) errors.password = 'Temporary password is required.'
  else if (form.password.length < 8) errors.password = 'Temporary password must be at least 8 characters.'

  if (!confirmPassword) errors.confirmPassword = 'Please re-enter the temporary password.'
  else if (confirmPassword !== form.password) errors.confirmPassword = 'Passwords do not match.'

  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = 'Enter a valid email address.'
  if (phone && (digits.length < 7 || digits.length > 15)) errors.phone = 'Phone number must contain 7 to 15 digits.'

  return errors
}

const EMPTY_FORM: CreateAdminHospitalUserInput = {
  username: '',
  fullName: '',
  password: '',
  role: 'RECEPTIONIST',
  email: '',
  phone: '',
}

export function AdminUsersPage() {
  const { user } = useAuth()
  const [users, setUsers] = useState<AdminHospitalUser[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [showResetModal, setShowResetModal] = useState(false)
  const [resetTarget, setResetTarget] = useState<AdminHospitalUser | null>(null)
  const [resetPassword, setResetPassword] = useState('')
  const [resetConfirmPassword, setResetConfirmPassword] = useState('')
  const [form, setForm] = useState<CreateAdminHospitalUserInput>(EMPTY_FORM)
  const [confirmPassword, setConfirmPassword] = useState('')
  const [validationErrors, setValidationErrors] = useState<FormErrors>({})

  const hospitalUsers = users.filter((entry) => entry.role !== 'SUPER_ADMIN')
  const activeUsers = hospitalUsers.filter((entry) => entry.active).length
  const pendingPasswordReset = hospitalUsers.filter((entry) => entry.mustChangePassword).length

  const load = () => {
    setLoading(true)
    setError('')
    listAdminHospitalUsers()
      .then((data) => setUsers(data))
      .catch((err) => setError(apiErrorWithNetworkHint('Failed to load hospital users.', err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const updateForm = <K extends keyof CreateAdminHospitalUserInput>(key: K, value: CreateAdminHospitalUserInput[K]) => {
    const nextForm = { ...form, [key]: value }
    setForm(nextForm)
    setError('')
    if (Object.keys(validationErrors).length > 0) {
      setValidationErrors(validateUserForm(nextForm, confirmPassword))
    }
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError('')
    setSuccess('')

    const trimmedForm: CreateAdminHospitalUserInput = {
      ...form,
      username: form.username.trim(),
      fullName: form.fullName.trim(),
      email: form.email?.trim() || '',
      phone: form.phone?.trim() || '',
    }

    const nextErrors = validateUserForm(trimmedForm, confirmPassword)
    setValidationErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) {
      return
    }

    try {
      setSaving(true)
      await createAdminHospitalUser({
        ...trimmedForm,
        email: trimmedForm.email || undefined,
        phone: trimmedForm.phone || undefined,
      })
      setSuccess('User created with a temporary password. They will be forced to change it on first login.')
      setShowModal(false)
      setForm(EMPTY_FORM)
      setConfirmPassword('')
      setValidationErrors({})
      load()
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(message || apiErrorWithNetworkHint('Failed to create hospital user.', err))
    } finally {
      setSaving(false)
    }
  }

  const handleToggleStatus = async (targetUser: AdminHospitalUser) => {
    setError('')
    setSuccess('')
    try {
      await updateAdminHospitalUserStatus(targetUser.id, !targetUser.active)
      setSuccess(`${targetUser.username} has been ${targetUser.active ? 'deactivated' : 'activated'}.`)
      load()
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(message || apiErrorWithNetworkHint('Failed to update user status.', err))
    }
  }

  const openResetPassword = (targetUser: AdminHospitalUser) => {
    setError('')
    setSuccess('')
    setResetTarget(targetUser)
    setResetPassword('')
    setResetConfirmPassword('')
    setShowResetModal(true)
  }

  const handleResetPassword = async (event: React.FormEvent) => {
    event.preventDefault()
    setError('')
    setSuccess('')
    if (!resetTarget) return
    const password = resetPassword.trim()
    if (!password || password.length < 8) {
      setError('Temporary password must be at least 8 characters.')
      return
    }
    if (password !== resetConfirmPassword) {
      setError('Passwords do not match.')
      return
    }
    try {
      setSaving(true)
      await resetAdminHospitalUserPassword(resetTarget.id, password)
      setSuccess(`Temporary password reset for ${resetTarget.username}. They will be forced to change it on first login.`)
      setShowResetModal(false)
      setResetTarget(null)
      setResetPassword('')
      setResetConfirmPassword('')
      load()
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(message || apiErrorWithNetworkHint('Failed to reset password.', err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h1 className={shared.pageTitle}>User Management</h1>
        <p className={shared.pageSubtitle}>
          Create and manage users for {user?.hospitalName || 'this hospital'} only. New users start with a temporary password and must change it on first login.
        </p>
      </div>

      <div className={shared.statsRow}>
        <div className={shared.statCard}>
          <div className={shared.statContent}>
            <div className={shared.statValue}>{hospitalUsers.length}</div>
            <div className={shared.statLabel}>Total hospital users</div>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={shared.statContent}>
            <div className={shared.statValue}>{activeUsers}</div>
            <div className={shared.statLabel}>Active users</div>
          </div>
        </div>
        <div className={shared.statCard}>
          <div className={shared.statContent}>
            <div className={shared.statValue}>{pendingPasswordReset}</div>
            <div className={shared.statLabel}>Pending first login reset</div>
          </div>
        </div>
      </div>

      <div className={styles.toolbar}>
        <button type="button" className={styles.primaryBtn} onClick={() => setShowModal(true)}>
          + Add User
        </button>
      </div>

      {error ? <div className={styles.errorBanner}>{error}</div> : null}
      {success ? <div className={styles.card} style={{ padding: '0.85rem 1rem', color: 'var(--hms-success)' }}>{success}</div> : null}

      {loading ? (
        <div className={styles.loading}>Loading hospital users…</div>
      ) : hospitalUsers.length === 0 ? (
        <div className={styles.empty}>No users exist for this hospital yet.</div>
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
                <th>First Login</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {hospitalUsers.map((entry) => (
                <tr key={entry.id}>
                  <td>{entry.username}</td>
                  <td>{entry.fullName}</td>
                  <td>{entry.role}</td>
                  <td>{entry.email || '—'}</td>
                  <td>
                    <span className={entry.active ? styles.badgeSuccess : styles.badgeMuted}>
                      {entry.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <span className={entry.mustChangePassword ? styles.badgeMuted : styles.badgeSuccess}>
                      {entry.mustChangePassword ? 'Temporary password' : 'Completed'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap' }}>
                      <button type="button" className={styles.textBtn} onClick={() => openResetPassword(entry)}>
                        Reset password
                      </button>
                      <button type="button" className={styles.textBtn} onClick={() => handleToggleStatus(entry)}>
                        {entry.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showResetModal && resetTarget ? (
        <div className={styles.modalOverlay} role="dialog" aria-modal="true" aria-labelledby="hospital-admin-reset-pw-title">
          <div className={styles.modal} style={{ maxWidth: '520px' }}>
            <h2 id="hospital-admin-reset-pw-title" className={styles.modalTitle}>Reset Temporary Password</h2>
            <form className={styles.form} onSubmit={handleResetPassword} noValidate>
              <p className={styles.modalText} style={{ padding: 0, marginBottom: '1rem' }}>
                Set a new temporary password for <strong>{resetTarget.username}</strong>. They will be forced to change it on first login.
              </p>
              <div className={styles.formRow}>
                <label className={styles.label}>Temporary Password</label>
                <input className={styles.input} type="password" value={resetPassword} onChange={(e) => setResetPassword(e.target.value)} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Confirm Temporary Password</label>
                <input className={styles.input} type="password" value={resetConfirmPassword} onChange={(e) => setResetConfirmPassword(e.target.value)} />
              </div>
              <div className={styles.modalActions}>
                <button
                  type="button"
                  className={styles.secondaryBtn}
                  onClick={() => { setShowResetModal(false); setResetTarget(null); setResetPassword(''); setResetConfirmPassword('') }}
                  disabled={saving}
                >
                  Cancel
                </button>
                <button type="submit" className={styles.primaryBtn} disabled={saving}>
                  {saving ? 'Saving…' : 'Reset Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {showModal ? (
        <div className={styles.modalOverlay} role="dialog" aria-modal="true" aria-labelledby="hospital-admin-add-user-title">
          <div className={styles.modal} style={{ maxWidth: '520px' }}>
            <h2 id="hospital-admin-add-user-title" className={styles.modalTitle}>Add Hospital User</h2>
            <form className={styles.form} onSubmit={handleSubmit} noValidate>
              <p className={styles.modalText} style={{ padding: 0, marginBottom: '1rem' }}>
                Set a temporary password now. The user will be forced to change it on first login.
              </p>
              <div className={styles.formRow}>
                <label className={styles.label}>Username</label>
                <input className={styles.input} value={form.username} onChange={(e) => updateForm('username', e.target.value)} />
                {validationErrors.username ? <p className={styles.inlineError}>{validationErrors.username}</p> : null}
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Full Name</label>
                <input className={styles.input} value={form.fullName} onChange={(e) => updateForm('fullName', e.target.value)} />
                {validationErrors.fullName ? <p className={styles.inlineError}>{validationErrors.fullName}</p> : null}
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Temporary Password</label>
                <input className={styles.input} type="password" value={form.password} onChange={(e) => updateForm('password', e.target.value)} />
                {validationErrors.password ? <p className={styles.inlineError}>{validationErrors.password}</p> : null}
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Confirm Temporary Password</label>
                <input className={styles.input} type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
                {validationErrors.confirmPassword ? <p className={styles.inlineError}>{validationErrors.confirmPassword}</p> : null}
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Role</label>
                <select className={styles.input} value={form.role} onChange={(e) => updateForm('role', e.target.value)}>
                  {HOSPITAL_ROLES.map((role) => <option key={role} value={role}>{role}</option>)}
                </select>
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Email</label>
                <input className={styles.input} type="email" value={form.email} onChange={(e) => updateForm('email', e.target.value)} />
                {validationErrors.email ? <p className={styles.inlineError}>{validationErrors.email}</p> : null}
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Phone</label>
                <input className={styles.input} value={form.phone} onChange={(e) => updateForm('phone', e.target.value)} />
                {validationErrors.phone ? <p className={styles.inlineError}>{validationErrors.phone}</p> : null}
              </div>
              <div className={styles.modalActions}>
                <button type="button" className={styles.secondaryBtn} onClick={() => { setShowModal(false); setValidationErrors({}); setConfirmPassword(''); setForm(EMPTY_FORM) }} disabled={saving}>
                  Cancel
                </button>
                <button type="submit" className={styles.primaryBtn} disabled={saving}>
                  {saving ? 'Creating…' : 'Create User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  )
}