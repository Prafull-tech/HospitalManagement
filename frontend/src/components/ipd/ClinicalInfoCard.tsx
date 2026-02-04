import type { ViewAdmissionAdmission } from '../../types/ipdAdmission.types'
import styles from './ViewAdmissionCards.module.css'

export interface ClinicalInfoCardProps {
  admission: ViewAdmissionAdmission
}

function admissionSourceLabel(type: string): string {
  switch (type) {
    case 'OPD_REFERRAL':
      return 'OPD Referral'
    case 'EMERGENCY':
      return 'Emergency'
    case 'DIRECT':
      return 'Direct'
    default:
      return type.replace(/_/g, ' ')
  }
}

export function ClinicalInfoCard({ admission }: ClinicalInfoCardProps) {
  return (
    <section className={styles.card} aria-label="Clinical information">
      <h2 className={styles.cardTitle}>Clinical Information</h2>
      <div className={styles.grid}>
        <div className={styles.gridFull}>
          <span className={styles.label}>Diagnosis</span>
          <div className={styles.value}>{admission.diagnosis ?? '—'}</div>
        </div>
        <div className={styles.gridFull}>
          <span className={styles.label}>Admission Notes</span>
          <div className={styles.value}>{admission.remarks ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Admission Source</span>
          <div className={styles.value}>{admissionSourceLabel(admission.admissionType)}</div>
        </div>
        <div>
          <span className={styles.label}>Doctor Recommendation</span>
          <div className={styles.value}>
            {admission.primaryDoctorName} ({admission.primaryDoctorCode})
          </div>
        </div>
        {admission.dischargeRemarks && (
          <div className={styles.gridFull}>
            <span className={styles.label}>Discharge Remarks</span>
            <div className={styles.value}>{admission.dischargeRemarks}</div>
          </div>
        )}
      </div>
    </section>
  )
}
