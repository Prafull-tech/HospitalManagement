import { useState, useEffect } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { BrandIdentity } from '../components/BrandIdentity'
import { useAppBootstrap } from '../components/AppBootstrap'
import { buildTenantHostAlias, clearTenantHostAlias, setTenantHostAlias } from '../lib/tenantHostAlias'
import styles from './LoginPage.module.css'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [hospitalSlug, setHospitalSlug] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login, isAuthenticated } = useAuth()
  const bootstrap = useAppBootstrap()
  const tenant = bootstrap?.tenant
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/dashboard'
  const isTenantHost = !!tenant?.tenantResolved
  const hostModeLabel = isTenantHost
    ? tenant?.resolvedBy === 'CUSTOM_DOMAIN'
      ? 'Enterprise custom domain'
      : 'Hospital subdomain'
    : tenant?.platformHost
      ? 'Platform admin host'
      : 'Public host'
  const loginTitle = isTenantHost
    ? `Sign in to ${tenant?.hospitalName ?? 'your hospital workspace'}.`
    : tenant?.platformHost
      ? 'Sign in to manage rollout and platform operations.'
      : 'Sign in to continue the operational day.'
  const loginSubtitle = isTenantHost
    ? `You are on ${tenant?.host || 'this host'}. Only ${tenant?.hospitalName ?? 'hospital'} staff accounts can sign in here.`
    : tenant?.platformHost
      ? 'Use this host for super-admin setup, domain verification, certificates, and hospital onboarding. Hospital teams should sign in from their hospital URL.'
      : 'Move back into reception, nursing, pharmacy, lab, and billing workflows with the same shared patient context.'
  const formHeaderTitle = isTenantHost ? `${tenant?.hospitalName ?? 'Hospital'} login` : 'Platform login'
  const formHeaderSubtitle = isTenantHost
    ? `${hostModeLabel} detected${tenant?.customDomain ? `: ${tenant.customDomain}` : tenant?.tenantSlug ? `: ${tenant.tenantSlug}.hms.com` : ''}.`
    : 'Super admins and rollout operators sign in here. Hospital staff should use their hospital workspace URL.'

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
      setError('Username/email and password are required.')
      return
    }
    if (!isTenantHost && !hospitalSlug.trim() && !tenant?.platformHost) {
      // Platform users can sign in without a hospital slug.
      setError('Hospital slug is required for hospital login.')
      return
    }
    try {
      setLoading(true)
      const loggedInUser = await login(username.trim(), password, isTenantHost ? tenant?.tenantSlug ?? hospitalSlug.trim() : hospitalSlug.trim() || null)
      const primaryRole = loggedInUser.roles[0]
      if (primaryRole === 'SUPER_ADMIN') {
        clearTenantHostAlias()
      } else {
        const tenantHostAlias = buildTenantHostAlias(loggedInUser.tenantSlug)
        if (tenantHostAlias && (tenant?.platformHost || !tenant?.tenantResolved)) {
          setTenantHostAlias(tenantHostAlias)
        }
      }
      if (loggedInUser.mustChangePassword) {
        navigate('/profile/change-password', { replace: true, state: { from: { pathname: from } } })
        return
      }
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
          <div className={styles.hostBadge}>{hostModeLabel}</div>
          <h1 className={styles.title}>{loginTitle}</h1>
          <p className={styles.subtitle}>{loginSubtitle}</p>
          <div className={styles.bulletList}>
            <div>
              <strong>Role-based access</strong>
              <span>{isTenantHost ? 'Department teams enter only their hospital workspace from this host.' : 'Platform operators can onboard hospitals and guide staff to the correct domain.'}</span>
            </div>
            <div>
              <strong>Continuous workflow</strong>
              <span>Admissions, care updates, and financial closure stay connected after sign-in.</span>
            </div>
            <div>
              <strong>Host-aware routing</strong>
              <span>{isTenantHost ? 'Custom-domain and subdomain hosts stay tenant-specific, while the platform host remains separate for super-admin operations.' : 'Use the platform host for onboarding and direct hospital teams to their subdomain or custom domain for day-to-day work.'}</span>
            </div>
          </div>
          <p className={styles.devHint}>
            Dev users: admin/admin123, superadmin/super123, pharm/pharm123, pharmacist/pharm123, store/store123, ipdph/ipdph123, labtech/lab123, labsup/lab123, radtech/rad123, bloodtech/blood123, doctor/doctor123, nurse/nurse123, reception/rec123
          </p>
        </section>

        <section className={styles.formPanel}>
          <div className={styles.formCard}>
            <div className={styles.formHeader}>
              <h2>{formHeaderTitle}</h2>
              <p>{formHeaderSubtitle}</p>
            </div>

            <form onSubmit={handleSubmit} className={styles.form}>
              {error ? <div className={styles.error}>{error}</div> : null}

              <label className={styles.field}>
                <span>{isTenantHost ? 'Email' : 'Username / Email'}</span>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete={isTenantHost ? 'email' : 'username'}
                  placeholder={isTenantHost ? 'e.g. admin@citygeneral.com' : 'e.g. superadmin'}
                />
              </label>

              {!isTenantHost && !tenant?.platformHost ? (
                <label className={styles.field}>
                  <span>Hospital slug</span>
                  <input
                    type="text"
                    value={hospitalSlug}
                    onChange={(e) => setHospitalSlug(e.target.value)}
                    autoComplete="organization"
                    placeholder="e.g. city-general"
                  />
                </label>
              ) : null}

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
              {isTenantHost ? 'Need a new hospital account? Contact your hospital administrator or platform support.' : <>New to HMS? <Link to="/signup">Request an account</Link></>}
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
