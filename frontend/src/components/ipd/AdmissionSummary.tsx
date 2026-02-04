import type { ViewAdmissionAdmission } from '../../types/ipdAdmission.types'
import { statusToBadgeClass } from './viewAdmissionUtils'
import styles from './ViewAdmissionCards.module.css'

export interface AdmissionSummaryProps {
  admission: ViewAdmissionAdmission
}

function formatDateTime(iso: string | undefined): string {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleString(undefined, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function AdmissionSummary({ admission }: AdmissionSummaryProps) {
  const statusClass = statusToBadgeClass(admission.admissionStatus)

  return (
    <section className={styles.card} aria-label="Admission summary">
      <h2 className={styles.cardTitle}>Admission Summary</h2>
      <div className={styles.grid}>
        <div>
          <span className={styles.label}>IPD Admission No</span>
          <div className={styles.value}>{admission.admissionNumber}</div>
        </div>
        <div>
          <span className={styles.label}>Status</span>
          <div>
            <span className={`${styles.badge} ${statusClass}`}>
              {admission.admissionStatus.replace(/_/g, ' ')}
            </span>
          </div>
        </div>
        <div>
          <span className={styles.label}>Admission Date & Time</span>
          <div className={styles.value}>{formatDateTime(admission.admissionDateTime)}</div>
        </div>
        <div>
          <span className={styles.label}>Ward Type</span>
          <div className={styles.value}>
            {admission.currentWardName ?? '—'}
          </div>
        </div>
        <div>
          <span className={styles.label}>Room No</span>
          <div className={styles.value}>{admission.currentRoomNumber ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Bed No</span>
          <div className={styles.value}>{admission.currentBedNumber ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Treating Doctor</span>
          <div className={styles.value}>
            {admission.primaryDoctorName} ({admission.primaryDoctorCode})
          </div>
        </div>
      </div>
    </section>
  )
}
