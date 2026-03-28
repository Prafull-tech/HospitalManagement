import { useState } from 'react'
import { Link } from 'react-router-dom'
import { changePassword } from '../api/authApi'
import styles from './ProfilePage.module.css'

export function ChangePasswordPage() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

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
    try {
      setLoading(true)
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
      <h1 className={styles.title}>Change Password</h1>
      <div className={styles.card}>
        <form onSubmit={handleSubmit} className={styles.form}>
          {error ? (
            <p role="alert" style={{ color: 'var(--hms-danger, #c00)', margin: '0 0 0.75rem' }}>
              {error}
            </p>
          ) : null}
          {success ? (
            <p role="status" style={{ color: 'var(--hms-success, #060)', margin: '0 0 0.75rem' }}>
              {success}
            </p>
          ) : null}
          <label className={styles.label}>
            Current password
            <input
              type="password"
              autoComplete="current-password"
              className={styles.input}
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              required
            />
          </label>
          <label className={styles.label}>
            New password (min 8 characters)
            <input
              type="password"
              autoComplete="new-password"
              className={styles.input}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={8}
            />
          </label>
          <label className={styles.label}>
            Confirm new password
            <input
              type="password"
              autoComplete="new-password"
              className={styles.input}
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              required
              minLength={8}
            />
          </label>
          <div className={styles.actions}>
            <button type="submit" className={styles.button} disabled={loading}>
              {loading ? 'Saving…' : 'Update password'}
            </button>
            <Link to="/profile" className={styles.link}>
              Back to Profile
            </Link>
          </div>
        </form>
      </div>
    </div>
  )
}
