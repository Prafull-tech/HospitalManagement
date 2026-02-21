import type { BedAvailabilityResponse } from '../../types/ipd'
import styles from './BedSeatGrid.module.css'

function sortBedsByNumber(beds: BedAvailabilityResponse[]): BedAvailabilityResponse[] {
  return [...beds].sort((a, b) => {
    const numA = parseInt(a.bedNumber.replace(/\D/g, ''), 10) || 0
    const numB = parseInt(b.bedNumber.replace(/\D/g, ''), 10) || 0
    if (numA !== numB) return numA - numB
    return a.bedNumber.localeCompare(b.bedNumber)
  })
}

export interface BedSeatGridProps {
  beds: BedAvailabilityResponse[]
  selectedBedId: number
  onSelectBed: (bedId: number) => void
  seatsPerRow?: number
}

export function BedSeatGrid({
  beds,
  selectedBedId,
  onSelectBed,
  seatsPerRow = 5,
}: BedSeatGridProps) {
  const selectableBeds = beds.filter((b) => b.selectableForAdmission ?? b.available)
  const sortedBeds = sortBedsByNumber(selectableBeds)

  if (sortedBeds.length === 0) {
    return (
      <div className={styles.empty}>
        No VACANT beds in this ward type.
      </div>
    )
  }

  const rows: BedAvailabilityResponse[][] = []
  for (let i = 0; i < sortedBeds.length; i += seatsPerRow) {
    rows.push(sortedBeds.slice(i, i + seatsPerRow))
  }

  return (
    <div className={styles.container}>
      <div className={styles.screen}>Ward — Select a bed</div>
      <div className={styles.grid}>
        {rows.map((row, rowIdx) => (
          <div key={rowIdx} className={styles.row}>
            {row.map((bed) => {
              const isSelected = selectedBedId === bed.bedId
              const isAvailable = bed.selectableForAdmission ?? bed.available
              return (
                <button
                  key={bed.bedId}
                  type="button"
                  className={`${styles.seat} ${isSelected ? styles.selected : ''} ${!isAvailable ? styles.occupied : ''}`}
                  onClick={() => isAvailable && onSelectBed(bed.bedId)}
                  disabled={!isAvailable}
                  title={`${bed.wardName} — ${bed.bedNumber}${bed.bedStatusDisplay ? ` (${bed.bedStatusDisplay})` : ''}`}
                >
                  <span className={styles.seatLabel}>{bed.bedNumber}</span>
                </button>
              )
            })}
          </div>
        ))}
      </div>
      <div className={styles.legend}>
        <span className={styles.legendItem}>
          <span className={`${styles.seat} ${styles.available}`} /> Available
        </span>
        <span className={styles.legendItem}>
          <span className={`${styles.seat} ${styles.selected}`} /> Selected
        </span>
      </div>
    </div>
  )
}
