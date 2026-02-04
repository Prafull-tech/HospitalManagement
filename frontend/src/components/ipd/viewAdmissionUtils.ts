import styles from './ViewAdmissionCards.module.css'

export function statusToBadgeClass(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return styles.badgeActive
    case 'ADMITTED':
      return styles.badgeAdmitted
    case 'TRANSFERRED':
      return styles.badgeTransferred
    case 'DISCHARGE_INITIATED':
      return styles.badgeDischargeInitiated
    case 'DISCHARGED':
      return styles.badgeDischarged
    case 'CANCELLED':
    case 'EXPIRED':
    case 'REFERRED':
    case 'LAMA':
      return styles.badgeCancelled
    default:
      return styles.badgeActive
  }
}

export function formatAge(patient: { age: number; ageYears?: number; ageMonths?: number; ageDays?: number }): string {
  const y = patient.ageYears ?? patient.age
  const m = patient.ageMonths ?? 0
  const d = patient.ageDays ?? 0
  if (m || d) return `${y} yrs ${m}M ${d}D`
  return `${y} yrs`
}
