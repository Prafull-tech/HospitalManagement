import { Link } from 'react-router-dom'
import { useCompanyProfile } from '../hooks/useCompanyProfile'
import styles from './BrandIdentity.module.css'

interface BrandIdentityProps {
  to?: string
  compact?: boolean
  className?: string
}

export function BrandIdentity({ to = '/home', compact = false, className = '' }: BrandIdentityProps) {
  const { profile } = useCompanyProfile()
  const wrapperClass = compact ? `${styles.staticBrand} ${styles.compact}` : styles.staticBrand
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
        {profile.logoUrl ? (
          <img src={profile.logoUrl} alt="" className={styles.markImage} />
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