import { useState, useEffect } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { BrandIdentity } from '../components/BrandIdentity'
import styles from './LoginPage.module.css'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/reception'

  // Redirect to app if already logged in
  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true })
    }
  }, [isAuthenticated, navigate, from])

  // Don't render login form if already authenticated (redirect in progress)
  if (isAuthenticated) {
    return (
      <div className={styles.redirectState}>
        <p className={styles.redirectText}>Redirecting…</p>
      </div>
    )
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!username.trim() || !password) {
      setError('Username and password are required.')
      return
    }
    try {
      setLoading(true)
      await login(username.trim(), password)
      navigate(from, { replace: true })
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'Invalid username or password.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.shell}>
        <section className={styles.introPanel}>
          <BrandIdentity to="" />
          <span className={styles.kicker}>Secure hospital workspace access</span>
          <h1 className={styles.title}>Sign in to continue the operational day.</h1>
          <p className={styles.subtitle}>
            Move back into reception, nursing, pharmacy, lab, and billing workflows with the same shared patient context.
          </p>
          <div className={styles.bulletList}>
            <div>
              <strong>Role-based access</strong>
              <span>Department teams land directly in the part of the system they work in most.</span>
            </div>
            <div>
              <strong>Continuous workflow</strong>
              <span>Admissions, care updates, and financial closure stay connected after sign-in.</span>
            </div>
            <div>
              <strong>Public to private path</strong>
              <span>Use the public site for discovery, then step into the live operational workspace.</span>
            </div>
          </div>
          <p className={styles.devHint}>
            Dev users: admin/admin123, superadmin/super123, pharm/pharm123, pharmacist/pharm123, store/store123, ipdph/ipdph123, labtech/lab123, labsup/lab123, radtech/rad123, bloodtech/blood123, doctor/doctor123, nurse/nurse123, reception/rec123
          </p>
        </section>

        <section className={styles.formPanel}>
          <div className={styles.formCard}>
            <div className={styles.formHeader}>
              <h2>Login</h2>
              <p>Have an account? Sign in to continue.</p>
            </div>

            <form onSubmit={handleSubmit} className={styles.form}>
              {error ? <div className={styles.error}>{error}</div> : null}

              <label className={styles.field}>
                <span>Username</span>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="username"
                  placeholder="e.g. pharm, nurse"
                />
              </label>

              <label className={styles.field}>
                <span>Password</span>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                  placeholder="••••••••"
                />
              </label>

              <div className={styles.formMeta}>
                <label className={styles.checkboxRow}>
                  <input id="rememberMe" type="checkbox" defaultChecked />
                  <span>Remember me</span>
                </label>
                <button className={styles.inlineButton} type="button">Forgot password</button>
              </div>

              <button type="submit" className={styles.submitButton} disabled={loading}>
                {loading ? 'Signing in…' : 'Sign in'}
              </button>
            </form>

            <div className={styles.footerNote}>
              New to HMS? <Link to="/signup">Request an account</Link>
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
