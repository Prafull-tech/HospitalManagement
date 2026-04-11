import { useState } from 'react'
import { Link } from 'react-router-dom'
import { apiClient } from '../api/client'
import { BrandIdentity } from '../components/BrandIdentity'
import styles from './SignupPage.module.css'

export function SignupPage() {
  const [fullName, setFullName] = useState('')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!fullName.trim() || !username.trim() || !password) {
      setError('Full name, username, and password are required.')
      return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    try {
      setLoading(true)
      await apiClient.post('/auth/signup', {
        fullName: fullName.trim(),
        username: username.trim(),
        email: email.trim() || undefined,
        phone: phone.trim() || undefined,
        password,
      })
      setSuccess(true)
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'Registration failed. Please try again.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className={styles.page}>
        <div className={styles.successShell}>
          <BrandIdentity to="" compact />
          <h1 className={styles.heading}>Account Requested</h1>
          <p className={styles.success}>
            Your account request has been submitted. An administrator will review and activate your
            account shortly.
          </p>
          <div className={styles.footer}>
            <Link to="/login">Back to Login</Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <div className={styles.shell}>
        <section className={styles.introPanel}>
          <BrandIdentity to="" />
          <span className={styles.kicker}>Request access</span>
          <h1 className={styles.heading}>Create a workspace request for your team.</h1>
          <p className={styles.subheading}>
            Start the onboarding conversation with the details your hospital team needs to move into the platform confidently.
          </p>
          <div className={styles.callouts}>
            <div>
              <strong>Right-sized rollout</strong>
              <span>Begin with one department or prepare a phased setup across reception, billing, and inpatient care.</span>
            </div>
            <div>
              <strong>Administrator review</strong>
              <span>Requests are reviewed before activation so access can match your operating structure.</span>
            </div>
          </div>
        </section>

        <section className={styles.formCard}>
          <div className={styles.formHeader}>
            <h2>Create Account</h2>
            <p>Sign up to request access to HMS.</p>
          </div>

          <form onSubmit={handleSubmit} className={styles.form}>
            {error ? <div className={styles.error}>{error}</div> : null}

            <div className={styles.twoColumn}>
              <label className={styles.field}>
                <span>Full Name *</span>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="Dr. John Smith"
                  autoComplete="name"
                />
              </label>

              <label className={styles.field}>
                <span>Username *</span>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="jsmith"
                  autoComplete="username"
                />
              </label>
            </div>

            <div className={styles.twoColumn}>
              <label className={styles.field}>
                <span>Email</span>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="john@example.com"
                  autoComplete="email"
                />
              </label>

              <label className={styles.field}>
                <span>Phone</span>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+91 98765 43210"
                  autoComplete="tel"
                />
              </label>
            </div>

            <div className={styles.twoColumn}>
              <label className={styles.field}>
                <span>Password *</span>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Min. 8 characters"
                  autoComplete="new-password"
                />
              </label>

              <label className={styles.field}>
                <span>Confirm Password *</span>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter password"
                  autoComplete="new-password"
                />
              </label>
            </div>

            <button type="submit" className={styles.submitBtn} disabled={loading}>
              {loading ? 'Creating account…' : 'Create Account'}
            </button>
          </form>

          <div className={styles.footer}>
            Already have an account? <Link to="/login">Login</Link>
          </div>
        </section>
      </div>
    </div>
  )
}
