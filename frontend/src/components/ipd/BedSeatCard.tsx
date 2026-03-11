/**
 * IPD Bed Management – Bed seat card (movie-style square button).
 * Displays bed number with status color; tooltip shows patient info when occupied.
 */

import type { BedAvailabilityItem } from '../../types/ipdBed.types'
import { BED_STATUS_COLORS } from './BedLegend'
import styles from './BedSeatCard.module.css'

export interface BedSeatCardProps {
  bed: BedAvailabilityItem
  onClick: (bed: BedAvailabilityItem) => void
  disabled?: boolean
}

function getStatusColor(status: BedAvailabilityItem['bedStatus']): string {
  return BED_STATUS_COLORS[status] ?? BED_STATUS_COLORS.UNKNOWN
}

function getTooltipText(bed: BedAvailabilityItem): string | undefined {
  if (bed.patientName || bed.admissionNumber) {
    const parts = [bed.patientName, bed.admissionNumber].filter(Boolean)
    return parts.join('\n')
  }
  return undefined
}

export function BedSeatCard({ bed, onClick, disabled }: BedSeatCardProps) {
  const color = getStatusColor(bed.bedStatus)
  const title = getTooltipText(bed)

  return (
    <button
      type="button"
      className={styles.bedSeatCard}
      title={title}
      style={{
        backgroundColor: color,
        color: ['OCCUPIED', 'RESERVED', 'MAINTENANCE'].includes(bed.bedStatus) ? '#fff' : '#1a1a1a',
        borderColor: color,
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.7 : 1,
      }}
      onClick={() => !disabled && onClick(bed)}
      disabled={disabled}
      aria-label={`Bed ${bed.bedNumber}${bed.patientName ? ` - ${bed.patientName}` : ''}`}
    >
      <span className={styles.bedSeatCardLabel}>{bed.bedNumber}</span>
    </button>
  )
}
