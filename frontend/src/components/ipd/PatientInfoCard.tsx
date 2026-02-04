import type { ViewAdmissionPatient } from '../../types/ipdAdmission.types'
import { formatAge } from './viewAdmissionUtils'
import styles from './ViewAdmissionCards.module.css'

export interface PatientInfoCardProps {
  patient: ViewAdmissionPatient
}

export function PatientInfoCard({ patient }: PatientInfoCardProps) {
  return (
    <section className={styles.card} aria-label="Patient information">
      <h2 className={styles.cardTitle}>Patient Information</h2>
      <div className={styles.grid}>
        <div>
          <span className={styles.label}>UHID</span>
          <div className={styles.value}>{patient.uhid}</div>
        </div>
        <div>
          <span className={styles.label}>Patient Name</span>
          <div className={styles.value}>{patient.fullName}</div>
        </div>
        <div>
          <span className={styles.label}>Age / Gender</span>
          <div className={styles.value}>
            {formatAge(patient)} / {patient.gender}
          </div>
        </div>
        <div>
          <span className={styles.label}>Contact Number</span>
          <div className={styles.value}>{patient.phone ?? '—'}</div>
        </div>
        <div className={styles.gridFull}>
          <span className={styles.label}>Address</span>
          <div className={styles.value}>
            {[patient.address, patient.city, patient.district, patient.state]
              .filter(Boolean)
              .join(', ') || '—'}
          </div>
        </div>
      </div>
    </section>
  )
}
