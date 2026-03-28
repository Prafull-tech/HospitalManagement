import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getProfile, updateProfile, changePassword } from '../api/authApi'
import type { UserProfile } from '../api/authApi'
import styles from './ProfilePage.module.css'

// ---- helpers ----

function getInitials(name: string): string {
  return name.split(/[\s_@.]+/).filter(Boolean).slice(0, 2).map((w) => w[0]).join('').toUpperCase()
}

function formatDate(iso: string | undefined): string {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })
  } catch { return iso }
}

function roleDisplay(role: string): string {
  return role.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())
}

type PasswordStrength = { score: 0 | 1 | 2 | 3 | 4; label: string; color: string }

function scorePassword(pw: string): PasswordStrength {
  if (!pw) return { score: 0, label: '', color: 'transparent' }
  let score = 0
  if (pw.length >= 8) score++
  if (pw.length >= 12) score++
  if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++
  if (/[0-9]/.test(pw) && /[^A-Za-z0-9]/.test(pw)) score++
  const labels: [string, string][] = [
    ['Too short', '#ef4444'],
    ['Weak', '#f97316'],
    ['Fair', '#eab308'],
    ['Good', '#22c55e'],
    ['Strong', '#16a34a'],
  ]
  return { score: score as 0 | 1 | 2 | 3 | 4, label: labels[score][0], color: labels[score][1] }
}

// ---- main component ----

export function ProfilePage() {
  const { user, logout, updateUser } = useAuth()
  const navigate = useNavigate()

  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [loadingProfile, setLoadingProfile] = useState(true)

  // edit form state
  const [editing, setEditing] = useState(false)
  const [editFullName, setEditFullName] = useState('')
  const [editEmail, setEditEmail] = useState('')
  const [editPhone, setEditPhone] = useState('')
  const [saving, setSaving] = useState(false)
  const [editError, setEditError] = useState('')
  const [editSuccess, setEditSuccess] = useState('')

  // password form state
  const [currentPw, setCurrentPw] = useState('')
  const [newPw, setNewPw] = useState('')
  const [confirmPw, setConfirmPw] = useState('')
  const [pwLoading, setPwLoading] = useState(false)
  const [pwError, setPwError] = useState('')
  const [pwSuccess, setPwSuccess] = useState('')
  const strength = scorePassword(newPw)

  useEffect(() => {
    setLoadingProfile(true)
    getProfile()
      .then((data) => {
        setProfile(data)
        setEditFullName(data.fullName)
        setEditEmail(data.email ?? '')
        setEditPhone(data.phone ?? '')
      })
      .catch(() => {
        // Fall back to auth context data
        if (user) {
          setProfile({
            username: user.username,
            fullName: user.fullName ?? '',
            role: user.roles?.[0] ?? '',
            email: user.email ?? '',
            phone: user.phone ?? '',
            active: user.active ?? true,
            createdAt: user.createdAt ?? '',
          })
          setEditFullName(user.fullName ?? '')
          setEditEmail(user.email ?? '')
          setEditPhone(user.phone ?? '')
        }
      })
      .finally(() => setLoadingProfile(false))
  }, [user])

  const startEdit = () => {
    setEditing(true)
    setEditError('')
    setEditSuccess('')
  }

  const cancelEdit = () => {
    setEditing(false)
    setEditFullName(profile?.fullName ?? '')
    setEditEmail(profile?.email ?? '')
    setEditPhone(profile?.phone ?? '')
    setEditError('')
  }

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!editFullName.trim()) { setEditError('Full name is required.'); return }
    setSaving(true); setEditError(''); setEditSuccess('')
    try {
      const updated = await updateProfile({ fullName: editFullName.trim(), email: editEmail.trim(), phone: editPhone.trim() })
      setProfile(updated)
      updateUser({ fullName: updated.fullName, email: updated.email, phone: updated.phone })
      setEditSuccess('Profile updated successfully.')
      setEditing(false)
    } catch (err: unknown) {
      setEditError((err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Could not update profile.')
    } finally {
      setSaving(false)
    }
  }

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    setPwError(''); setPwSuccess('')
    if (newPw.length < 8) { setPwError('New password must be at least 8 characters.'); return }
    if (newPw !== confirmPw) { setPwError('Passwords do not match.'); return }
    setPwLoading(true)
    try {
      await changePassword(currentPw, newPw)
      setPwSuccess('Password updated. You will be signed out — please log in again.')
      setCurrentPw(''); setNewPw(''); setConfirmPw('')
      setTimeout(() => { logout(); navigate('/login', { replace: true }) }, 2500)
    } catch (err: unknown) {
      setPwError((err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Could not change password.')
    } finally {
      setPwLoading(false)
    }
  }

  if (loadingProfile && !profile) {
    return <div className={styles.profilePage}><p style={{ color: 'var(--hms-text-muted)' }}>Loading profile…</p></div>
  }

  const displayName = profile?.fullName || user?.username || 'User'
  const initials = getInitials(displayName)
  const roleName = profile?.role || user?.roles?.[0] || ''
  const isActive = profile?.active ?? true

  return (
    <div className={styles.profilePage}>

      {/* ====== SECTION A — Profile Header ====== */}
      <div className={styles.sectionCard}>
        <div className={styles.profileHeader}>
          <div className={styles.avatarWrap}>
            <div className={styles.avatar}>{initials}</div>
          </div>
          <div className={styles.profileMeta}>
            <div className={styles.profileNameRow}>
              <h1 className={styles.profileName}>{displayName}</h1>
              <span className={`${styles.statusBadge} ${isActive ? styles.statusActive : styles.statusInactive}`}>
                {isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
            <p className={styles.profileUsername}>@{profile?.username ?? user?.username}</p>
            <div className={styles.badgeRow}>
              <span className={styles.roleBadge}>{roleDisplay(roleName)}</span>
            </div>
            <div className={styles.metaRow}>
              {profile?.email && (
                <span className={styles.metaItem}>
                  <MetaIcon type="email" />{profile.email}
                </span>
              )}
              {profile?.phone && (
                <span className={styles.metaItem}>
                  <MetaIcon type="phone" />{profile.phone}
                </span>
              )}
              {profile?.createdAt && (
                <span className={styles.metaItem}>
                  <MetaIcon type="calendar" />Member since {formatDate(profile.createdAt)}
                </span>
              )}
            </div>
          </div>
          <div className={styles.headerActions}>
            {!editing && (
              <button type="button" className={styles.btnPrimary} onClick={startEdit}>
                Edit Profile
              </button>
            )}
          </div>
        </div>
      </div>

      {/* ====== SECTION B — Edit Profile ====== */}
      <div className={styles.sectionCard}>
        <div className={styles.cardHead}>
          <h2 className={styles.cardTitle}>Profile Information</h2>
          {!editing && (
            <button type="button" className={styles.btnOutline} onClick={startEdit}>Edit</button>
          )}
        </div>

        {editSuccess && <div className={styles.alertSuccess}>{editSuccess}</div>}
        {editError && !editing && <div className={styles.alertError}>{editError}</div>}

        {editing ? (
          <form onSubmit={handleSaveProfile} className={styles.editForm}>
            {editError && <div className={styles.alertError}>{editError}</div>}
            <div className={styles.formGrid}>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>Full Name *</label>
                <input
                  type="text" className={styles.formInput} value={editFullName}
                  onChange={(e) => setEditFullName(e.target.value)} required autoFocus
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>Email Address</label>
                <input
                  type="email" className={styles.formInput} value={editEmail}
                  onChange={(e) => setEditEmail(e.target.value)} placeholder="you@example.com"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>Phone Number</label>
                <input
                  type="tel" className={styles.formInput} value={editPhone}
                  onChange={(e) => setEditPhone(e.target.value)} placeholder="+91 98765 43210"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>Username</label>
                <input type="text" className={`${styles.formInput} ${styles.formInputReadonly}`} value={profile?.username ?? ''} readOnly />
                <p className={styles.formHint}>Username cannot be changed.</p>
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="submit" className={styles.btnPrimary} disabled={saving}>
                {saving ? 'Saving…' : 'Save Changes'}
              </button>
              <button type="button" className={styles.btnGhost} onClick={cancelEdit} disabled={saving}>Cancel</button>
            </div>
          </form>
        ) : (
          <dl className={styles.infoGrid}>
            <div className={styles.infoRow}><dt className={styles.infoLabel}>Full Name</dt><dd className={styles.infoValue}>{profile?.fullName || '—'}</dd></div>
            <div className={styles.infoRow}><dt className={styles.infoLabel}>Username</dt><dd className={styles.infoValue}>{profile?.username || '—'}</dd></div>
            <div className={styles.infoRow}><dt className={styles.infoLabel}>Email</dt><dd className={styles.infoValue}>{profile?.email || <span className={styles.dimText}>Not set</span>}</dd></div>
            <div className={styles.infoRow}><dt className={styles.infoLabel}>Phone</dt><dd className={styles.infoValue}>{profile?.phone || <span className={styles.dimText}>Not set</span>}</dd></div>
            <div className={styles.infoRow}><dt className={styles.infoLabel}>Role</dt><dd className={styles.infoValue}>{roleDisplay(roleName)}</dd></div>
            <div className={styles.infoRow}><dt className={styles.infoLabel}>Account Status</dt><dd className={styles.infoValue}><span className={`${styles.statusBadge} ${isActive ? styles.statusActive : styles.statusInactive}`}>{isActive ? 'Active' : 'Inactive'}</span></dd></div>
            <div className={styles.infoRow}><dt className={styles.infoLabel}>Member Since</dt><dd className={styles.infoValue}>{formatDate(profile?.createdAt)}</dd></div>
          </dl>
        )}
      </div>

      {/* ====== SECTION C — Security ====== */}
      <div className={styles.sectionCard}>
        <div className={styles.cardHead}>
          <h2 className={styles.cardTitle}>Security</h2>
        </div>

        {pwSuccess && <div className={styles.alertSuccess}>{pwSuccess}</div>}

        <form onSubmit={handleChangePassword} className={styles.editForm}>
          {pwError && <div className={styles.alertError}>{pwError}</div>}
          <div className={styles.formGrid}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Current Password</label>
              <input
                type="password" className={styles.formInput} autoComplete="current-password"
                value={currentPw} onChange={(e) => setCurrentPw(e.target.value)} required
              />
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>New Password</label>
              <input
                type="password" className={styles.formInput} autoComplete="new-password"
                value={newPw} onChange={(e) => setNewPw(e.target.value)} required minLength={8}
              />
              {newPw && (
                <div className={styles.strengthWrap}>
                  <div className={styles.strengthBar}>
                    {[1,2,3,4].map((i) => (
                      <div
                        key={i}
                        className={styles.strengthSegment}
                        style={{ background: strength.score >= i ? strength.color : undefined }}
                      />
                    ))}
                  </div>
                  <span className={styles.strengthLabel} style={{ color: strength.color }}>{strength.label}</span>
                </div>
              )}
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Confirm New Password</label>
              <input
                type="password" className={styles.formInput} autoComplete="new-password"
                value={confirmPw} onChange={(e) => setConfirmPw(e.target.value)} required minLength={8}
              />
              {confirmPw && newPw !== confirmPw && (
                <p className={styles.formHintError}>Passwords do not match.</p>
              )}
            </div>
          </div>
          <div className={styles.formActions}>
            <button type="submit" className={styles.btnDanger} disabled={pwLoading}>
              {pwLoading ? 'Updating…' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>

    </div>
  )
}

// ---- small icon helper ----
function MetaIcon({ type }: { type: 'email' | 'phone' | 'calendar' }) {
  if (type === 'email') return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}>
      <rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
    </svg>
  )
  if (type === 'phone') return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}>
      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.29h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8a16 16 0 0 0 6.29 6.29l.91-.91a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
    </svg>
  )
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}>
      <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
    </svg>
  )
}
