import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { changePassword, changeTemporaryPassword } from '../api/authApi'
import { useAuth } from '../contexts/AuthContext'
import styles from './ProfilePage.module.css'

type PasswordStrength = { score: 0 | 1 | 2 | 3 | 4; label: string; color: string }

function scorePassword(password: string): PasswordStrength {
  if (!password) return { score: 0, label: 'Start typing', color: 'var(--hms-text-muted)' }

  let score = 0
  if (password.length >= 8) score++
  if (password.length >= 12) score++
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++
  if (/[0-9]/.test(password) && /[^A-Za-z0-9]/.test(password)) score++

  const labels: [string, string][] = [
    ['Too short', '#dc2626'],
    ['Weak', '#f97316'],
    ['Fair', '#eab308'],
    ['Good', '#16a34a'],
    ['Strong', '#15803d'],
  ]

  return { score: score as 0 | 1 | 2 | 3 | 4, label: labels[score][0], color: labels[score][1] }
}

function PasswordHintItem({ label, met }: { label: string; met: boolean }) {
  return (
    <li className={`${styles.passwordRuleItem} ${met ? styles.passwordRuleMet : ''}`}>
      <span className={styles.passwordRuleDot} aria-hidden>
        {met ? 'OK' : '--'}
      </span>
      <span>{label}</span>
    </li>
  )
}

export function ChangePasswordPage() {
  const { user, updateUser } = useAuth()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const location = useLocation()
  const navigate = useNavigate()
  const mustChangePassword = !!user?.mustChangePassword
  const redirectTarget = (location.state as { from?: { pathname?: string } })?.from?.pathname || '/dashboard'
  const strength = scorePassword(newPassword)
  const passwordChecks = [
    { label: 'At least 8 characters', met: newPassword.length >= 8 },
    { label: 'Uppercase and lowercase letters', met: /[A-Z]/.test(newPassword) && /[a-z]/.test(newPassword) },
    { label: 'A number and a special character', met: /[0-9]/.test(newPassword) && /[^A-Za-z0-9]/.test(newPassword) },
    { label: 'Confirmation matches', met: !!confirm && newPassword === confirm },
  ]

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters.')
      return
    }
    if (newPassword !== confirm) {
      setError('New password and confirmation do not match.')
      return
    }
    if (!mustChangePassword && !currentPassword) {
      setError('Current password is required.')
      return
    }
    try {
      setLoading(true)
      if (mustChangePassword) {
        await changeTemporaryPassword(newPassword)
        updateUser({ mustChangePassword: false })
        setSuccess('Password updated. You can continue into the hospital workspace now.')
        setCurrentPassword('')
        setNewPassword('')
        setConfirm('')
        navigate(redirectTarget, { replace: true })
        return
      }

      await changePassword(currentPassword, newPassword)
      setSuccess('Password updated. Use your new password next time you sign in.')
      setCurrentPassword('')
      setNewPassword('')
      setConfirm('')
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        (err as Error)?.message ||
        'Could not change password.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.passwordHero}>
        <div className={styles.passwordHeroCopy}>
          <span className={styles.passwordEyebrow}>Account Security</span>
          <h1 className={styles.passwordHeroTitle}>{mustChangePassword ? 'Set Your Password' : 'Change Password'}</h1>
          <p className={styles.passwordHeroText}>
            {mustChangePassword
              ? 'This account was created with a temporary password. Set a secure password to continue into the hospital workspace.'
              : 'Update your password with something unique and hard to guess. A stronger password reduces the risk of unauthorized access to patient and hospital data.'}
          </p>
          <div className={styles.passwordHeroMeta}>
            <div className={styles.passwordHeroPill}>
              <span className={styles.passwordHeroLabel}>User</span>
              <strong>{user?.username || 'Current user'}</strong>
            </div>
            {user?.hospitalName ? (
              <div className={styles.passwordHeroPill}>
                <span className={styles.passwordHeroLabel}>Workspace</span>
                <strong>{user.hospitalName}</strong>
              </div>
            ) : null}
            <div className={`${styles.passwordHeroPill} ${mustChangePassword ? styles.passwordHeroPillWarning : ''}`}>
              <span className={styles.passwordHeroLabel}>Status</span>
              <strong>{mustChangePassword ? 'First login reset required' : 'Protected account'}</strong>
            </div>
          </div>
        </div>
        <div className={styles.passwordHeroPanel}>
          <div className={styles.passwordHeroPanelTop}>
            <span className={styles.passwordHeroPanelIcon}>LOCK</span>
            <div>
              <h2 className={styles.passwordHeroPanelTitle}>Security checklist</h2>
              <p className={styles.passwordHeroPanelText}>Aim for a password that is longer, mixed, and not reused anywhere else.</p>
            </div>
          </div>
          <ul className={styles.passwordRuleList}>
            <PasswordHintItem label="Use 12 or more characters when possible" met={newPassword.length >= 12} />
            <PasswordHintItem label="Avoid hospital name, username, or simple sequences" met={newPassword.length > 0} />
            <PasswordHintItem label="Do not reuse your previous temporary password" met={!mustChangePassword || (newPassword.length > 0 && newPassword !== currentPassword)} />
          </ul>
        </div>
      </div>

      <div className={styles.passwordLayout}>
        <div className={styles.passwordFormCard}>
          <div className={styles.passwordCardHeader}>
            <h2 className={styles.passwordCardTitle}>{mustChangePassword ? 'Create a new sign-in password' : 'Update your sign-in password'}</h2>
            <p className={styles.passwordCardText}>
              {mustChangePassword
                ? 'You will use this password for all future logins to this hospital workspace.'
                : 'Your current session stays active here, but use the new password the next time you sign in.'}
            </p>
          </div>

          <form onSubmit={handleSubmit} className={styles.passwordForm}>
            {error ? <div role="alert" className={styles.alertError}>{error}</div> : null}
            {success ? <div role="status" className={styles.alertSuccess}>{success}</div> : null}

            {!mustChangePassword ? (
              <label className={styles.formLabel}>
                Current password
                <input
                  type="password"
                  autoComplete="current-password"
                  className={styles.formInput}
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  required
                />
              </label>
            ) : (
              <div className={styles.passwordInlineNotice}>
                Your administrator created this account with a temporary password. Replace it now before you continue.
              </div>
            )}

            <div className={styles.passwordFieldGroup}>
              <label className={styles.formLabel}>
                New password
                <input
                  type="password"
                  autoComplete="new-password"
                  className={styles.formInput}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  minLength={8}
                />
              </label>

              <div className={styles.strengthWrap}>
                <div className={styles.strengthBar} aria-hidden>
                  {[0, 1, 2, 3].map((index) => (
                    <span
                      key={index}
                      className={styles.strengthSegment}
                      style={{ background: strength.score > index ? strength.color : 'var(--hms-border)' }}
                    />
                  ))}
                </div>
                <span className={styles.strengthLabel} style={{ color: strength.color }}>{strength.label}</span>
              </div>
            </div>

            <label className={styles.formLabel}>
              Confirm new password
              <input
                type="password"
                autoComplete="new-password"
                className={styles.formInput}
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                required
                minLength={8}
              />
            </label>

            <ul className={styles.passwordRuleList}>
              {passwordChecks.map((rule) => (
                <PasswordHintItem key={rule.label} label={rule.label} met={rule.met} />
              ))}
            </ul>

            <div className={styles.formActions}>
              <button type="submit" className={styles.btnPrimary} disabled={loading}>
                {loading ? 'Saving…' : mustChangePassword ? 'Set password' : 'Update password'}
              </button>
              {!mustChangePassword ? (
                <Link to="/profile" className={styles.btnGhost}>
                  Back to Profile
                </Link>
              ) : null}
            </div>
          </form>
        </div>

        <aside className={styles.passwordSideCard}>
          <h2 className={styles.passwordSideTitle}>Security tips</h2>
          <div className={styles.passwordTipList}>
            <div className={styles.passwordTipItem}>
              <strong>Prefer a passphrase</strong>
              <p>Multiple unrelated words are easier to remember and harder to crack than short complex strings.</p>
            </div>
            <div className={styles.passwordTipItem}>
              <strong>Keep work credentials unique</strong>
              <p>Do not reuse the same password from email, social apps, or any other hospital systems.</p>
            </div>
            <div className={styles.passwordTipItem}>
              <strong>Store it safely</strong>
              <p>Use a password manager instead of writing credentials on paper or sharing them over chat.</p>
            </div>
          </div>
        </aside>
      </div>
    </div>
  )
}
