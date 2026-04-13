import { useState } from 'react'
import { NavLink, Link, Outlet } from 'react-router-dom'
import { BrandIdentity } from './BrandIdentity'
import { useCompanyProfile } from '../hooks/useCompanyProfile'
import { useAppBootstrap } from './AppBootstrap'
import styles from './PublicLayout.module.css'

const primaryNavigation = [
  { to: '/home', label: 'Overview' },
  { to: '/blog', label: 'Insights' },
  { to: '/contact', label: 'Contact' },
  { to: '/login', label: 'Login' },
]

export function PublicLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { profile } = useCompanyProfile()
  const bootstrap = useAppBootstrap()
  const tenant = bootstrap?.tenant
  const supportLine = [profile.supportEmail, profile.supportPhone].filter(Boolean).join('  •  ')
  const hostMode = tenant?.tenantResolved
    ? tenant.resolvedBy === 'CUSTOM_DOMAIN'
      ? 'Custom domain workspace'
      : 'Hospital subdomain workspace'
    : tenant?.platformHost
      ? 'Platform host'
      : 'Public access live'
  const topbarMessage = tenant?.tenantResolved
    ? `${tenant.hospitalName} is active on ${tenant.resolvedBy === 'CUSTOM_DOMAIN' ? 'its enterprise custom domain' : 'its HMS subdomain'}.'`
    : tenant?.platformHost
      ? 'Platform host for super-admin setup, rollout operations, and hospital onboarding.'
      : 'Connected admissions, billing, nursing, ward, and discharge workflows in one operational layer.'
  const topbarRouteHint = tenant?.tenantResolved
    ? `Staff sign in on ${tenant.host || 'this host'}${tenant.customDomain ? ' while platform admins stay on the platform host.' : '.'}`
    : tenant?.platformHost
      ? 'Hospital staff should sign in from their hospital subdomain or custom domain.'
      : supportLine

  return (
    <div className={styles.layout}>
      <div className={styles.chromeGlow} aria-hidden />

      <div className={styles.navbarWrapper}>
        <div className={styles.topbar}>
          <div className={styles.topbarInner}>
            <p className={styles.topbarMessage}>{topbarMessage}</p>
            {topbarRouteHint ? <span className={styles.topbarMeta}>{topbarRouteHint}</span> : null}
          </div>
        </div>

        <nav className={styles.navbar}>
          <div className={styles.navbarLead}>
            <BrandIdentity />
            <div className={styles.statusBadge}>
              <span className={styles.statusDot} />
              {hostMode}
            </div>
          </div>

          <button
            className={styles.hamburger}
            onClick={() => setMenuOpen((value) => !value)}
            aria-label="Toggle menu"
            aria-expanded={menuOpen}
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              {menuOpen ? (
                <path d="M6 18L18 6M6 6l12 12" />
              ) : (
                <path d="M4 6h16M4 12h16M4 18h16" />
              )}
            </svg>
          </button>

          <div className={`${styles.navPanel} ${menuOpen ? styles.navPanelOpen : ''}`}>
            <ul className={styles.navLinks}>
              {primaryNavigation.map((item) => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    end={item.to === '/home'}
                    className={({ isActive }) => `${styles.navLink} ${isActive ? styles.navLinkActive : ''}`}
                    onClick={() => setMenuOpen(false)}
                  >
                    {item.label}
                  </NavLink>
                </li>
              ))}
            </ul>

            <div className={styles.navActions}>
              <Link to="/contact" className={styles.navSecondary} onClick={() => setMenuOpen(false)}>
                {tenant?.platformHost ? 'Book walkthrough' : 'Need platform help?'}
              </Link>
              <Link to="/signup" className={styles.navCta} onClick={() => setMenuOpen(false)}>
                {tenant?.tenantResolved ? 'Request access' : 'Launch workspace'}
              </Link>
            </div>
          </div>
        </nav>
      </div>

      <main className={styles.content}>
        <Outlet />
      </main>

      <footer className={styles.footer}>
        <div className={styles.footerInner}>
          <div className={styles.footerIntro}>
            <div className={styles.footerBrand}><BrandIdentity to="" compact /></div>
            <p className={styles.footerEyebrow}>Hospital operations, re-orchestrated</p>
            <p className={styles.footerAbout}>
              {profile.companyName} helps streamline operations, strengthen patient care, and keep
              every department aligned on one connected platform.
            </p>
            <div className={styles.footerHighlights}>
              <span className={styles.footerPill}>Admissions to discharge</span>
              <span className={styles.footerPill}>Live operational visibility</span>
              <span className={styles.footerPill}>Department-ready workflows</span>
            </div>
          </div>

          <div>
            <h4 className={styles.footerHeading}>Explore</h4>
            <ul className={styles.footerLinks}>
              <li><Link to="/home#features" className={styles.footerLink}>Platform capabilities</Link></li>
              <li><Link to="/home#challenge" className={styles.footerLink}>Operational challenges</Link></li>
              <li><Link to="/blog" className={styles.footerLink}>Clinical operations journal</Link></li>
            </ul>
          </div>

          <div>
            <h4 className={styles.footerHeading}>Reach us</h4>
            <ul className={styles.footerLinks}>
              <li><Link to="/contact" className={styles.footerLink}>Speak with our team</Link></li>
              <li><Link to="/home" className={styles.footerLink}>Why hospitals switch</Link></li>
              {profile.supportEmail && <li><a href={`mailto:${profile.supportEmail}`} className={styles.footerLink}>{profile.supportEmail}</a></li>}
              {profile.supportPhone && <li><a href={`tel:${profile.supportPhone}`} className={styles.footerLink}>{profile.supportPhone}</a></li>}
              {profile.addressText && <li><span className={styles.footerDetail}>{profile.addressText}</span></li>}
            </ul>
          </div>

          <aside className={styles.footerCard}>
            <span className={styles.footerCardLabel}>For growing care teams</span>
            <h4 className={styles.footerCardTitle}>{tenant?.tenantResolved ? `Route ${tenant.hospitalName} staff into the right workspace.` : 'Run a calmer front desk and a tighter inpatient floor.'}</h4>
            <p className={styles.footerCardText}>{tenant?.tenantResolved ? `This host is reserved for ${tenant.hospitalName}. Platform setup, domain verification, and certificate rollout stay on the admin host.` : 'Start with the public workspace now, then move your teams into a unified hospital operations flow.'}</p>
            <div className={styles.footerCardActions}>
              <Link to="/signup" className={styles.footerCardPrimary}>{tenant?.tenantResolved ? 'Request access' : 'Create account'}</Link>
              <Link to="/login" className={styles.footerCardSecondary}>{tenant?.tenantResolved ? 'Hospital login' : 'Existing login'}</Link>
            </div>
          </aside>
        </div>

        <div className={styles.footerBottom}>
          <span>© {new Date().getFullYear()} {profile.companyName}. All rights reserved.</span>
          <span className={styles.footerBottomDivider} aria-hidden>•</span>
          <span>Built for reliable hospital coordination.</span>
        </div>
      </footer>
    </div>
  )
}
