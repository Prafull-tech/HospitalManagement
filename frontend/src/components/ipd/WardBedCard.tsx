import type { ViewAdmissionAdmission } from '../../types/ipdAdmission.types'
import styles from './ViewAdmissionCards.module.css'

export interface WardBedCardProps {
  admission: ViewAdmissionAdmission
}

export function WardBedCard({ admission }: WardBedCardProps) {
  return (
    <section className={styles.card} aria-label="Ward and bed details">
      <h2 className={styles.cardTitle}>Ward & Bed Details</h2>
      <div className={styles.grid}>
        <div>
          <span className={styles.label}>Ward Name</span>
          <div className={styles.value}>{admission.currentWardName ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Ward Type</span>
          <div className={styles.value}>{admission.currentWardName ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Room Number</span>
          <div className={styles.value}>{admission.currentRoomNumber ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Bed Number</span>
          <div className={styles.value}>{admission.currentBedNumber ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Bed Status</span>
          <div className={styles.value}>
            {admission.admissionStatus === 'DISCHARGED' ? 'Released' : 'Allocated'}
          </div>
        </div>
      </div>
    </section>
  )
}
