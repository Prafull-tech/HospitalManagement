import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useCompanyProfile } from '../hooks/useCompanyProfile'
import { normalizeLogoSrc } from '../lib/logoImage'
import styles from './BrandIdentity.module.css'

interface BrandIdentityProps {
  to?: string
  compact?: boolean
  className?: string
}

export function BrandIdentity({ to = '/home', compact = false, className = '' }: BrandIdentityProps) {
  const { profile } = useCompanyProfile()
  const wrapperClass = compact ? `${styles.staticBrand} ${styles.compact}` : styles.staticBrand
  const logoSrc = normalizeLogoSrc(profile.logoUrl)
  const [logoLoadFailed, setLogoLoadFailed] = useState(false)

  useEffect(() => {
    setLogoLoadFailed(false)
  }, [logoSrc])

  const initials = (profile.logoText || profile.brandName || profile.companyName)
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')

  const content = (
    <>
      <span className={styles.mark} aria-hidden>
        <span className={styles.markGlow} />
        {logoSrc && !logoLoadFailed ? (
          <img src={logoSrc} alt="" className={styles.markImage} onError={() => setLogoLoadFailed(true)} />
        ) : (
          <span className={styles.markText}>{initials}</span>
        )}
      </span>
      <span className={styles.textWrap}>
        <span className={styles.kicker}>Care operations platform</span>
        <span className={styles.brandName}>{profile.brandName}</span>
        <span className={styles.companyName}>{profile.companyName}</span>
      </span>
    </>
  )

  if (!to) {
    return <span className={`${wrapperClass} ${className}`.trim()}>{content}</span>
  }

  return (
    <Link to={to} className={`${styles.brandLink} ${compact ? styles.compact : ''} ${className}`.trim()}>
      {content}
    </Link>
  )
}