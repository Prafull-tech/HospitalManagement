/**
 * Summary cards for ward bed counts. All values derived from hospital-beds API (no local state).
 */

import type { WardSummaryCounts } from '../../types/bed.types'
import shared from '../../styles/Dashboard.module.css'
import styles from './WardSummaryCards.module.css'

export interface WardSummaryCardsProps {
  counts: WardSummaryCounts
}

export function WardSummaryCards({ counts }: WardSummaryCardsProps) {
  return (
    <div className={shared.statsRow} role="region" aria-label="Bed summary">
      <div className={shared.statCard}>
        <div className={`${shared.statIconWrap} ${shared.primary}`} aria-hidden>
          <span className={styles.statIcon}>🛏</span>
        </div>
        <div className={shared.statContent}>
          <span className={shared.statValue}>{counts.total}</span>
          <span className={shared.statLabel}>Total Beds</span>
        </div>
      </div>
      <div className={shared.statCard}>
        <div className={`${shared.statIconWrap} ${styles.danger}`} aria-hidden>
          <span className={styles.statIcon}>👤</span>
        </div>
        <div className={shared.statContent}>
          <span className={shared.statValue}>{counts.occupied}</span>
          <span className={shared.statLabel}>Occupied</span>
        </div>
      </div>
      <div className={shared.statCard}>
        <div className={`${shared.statIconWrap} ${shared.success}`} aria-hidden>
          <span className={styles.statIcon}>✓</span>
        </div>
        <div className={shared.statContent}>
          <span className={shared.statValue}>{counts.vacant}</span>
          <span className={shared.statLabel}>Vacant</span>
        </div>
      </div>
      <div className={shared.statCard}>
        <div className={`${shared.statIconWrap} ${shared.warning}`} aria-hidden>
          <span className={styles.statIcon}>📌</span>
        </div>
        <div className={shared.statContent}>
          <span className={shared.statValue}>{counts.reserved}</span>
          <span className={shared.statLabel}>Reserved</span>
        </div>
      </div>
      <div className={shared.statCard}>
        <div className={`${shared.statIconWrap} ${styles.muted}`} aria-hidden>
          <span className={styles.statIcon}>🧹</span>
        </div>
        <div className={shared.statContent}>
          <span className={shared.statValue}>{counts.cleaning}</span>
          <span className={shared.statLabel}>Cleaning</span>
        </div>
      </div>
    </div>
  )
}
