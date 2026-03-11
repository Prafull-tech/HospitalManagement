/**
 * IPD Bed Management – Color legend for bed statuses.
 * Movie-style seat map legend: Vacant, Occupied, Reserved, Cleaning, Maintenance.
 */

import type { BedStatus } from '../../types/ipdBed.types'

/** Color codes per spec: VACANT→Green, OCCUPIED→Red, RESERVED→Yellow, CLEANING→Gray, MAINTENANCE→Orange */
export const BED_STATUS_COLORS: Record<BedStatus | 'UNKNOWN', string> = {
  AVAILABLE: '#22c55e',    // Green – Vacant
  OCCUPIED: '#ef4444',    // Red
  RESERVED: '#eab308',    // Yellow
  CLEANING: '#94a3b8',    // Gray
  MAINTENANCE: '#f97316', // Orange
  ISOLATION: '#64748b',   // Slate gray
  UNKNOWN: '#94a3b8',
}

const LEGEND_ITEMS: { status: BedStatus; label: string; color: string }[] = [
  { status: 'AVAILABLE', label: 'Vacant', color: BED_STATUS_COLORS.AVAILABLE },
  { status: 'OCCUPIED', label: 'Occupied', color: BED_STATUS_COLORS.OCCUPIED },
  { status: 'RESERVED', label: 'Reserved', color: BED_STATUS_COLORS.RESERVED },
  { status: 'CLEANING', label: 'Cleaning', color: BED_STATUS_COLORS.CLEANING },
  { status: 'MAINTENANCE', label: 'Maintenance', color: BED_STATUS_COLORS.MAINTENANCE },
]

export interface BedLegendProps {
  className?: string
}

export function BedLegend({ className = '' }: BedLegendProps) {
  return (
    <div className={`bed-legend d-flex flex-wrap gap-3 align-items-center ${className}`}>
      {LEGEND_ITEMS.map(({ status, label, color }) => (
        <div key={status} className="bed-legend-item d-flex align-items-center gap-2">
          <span
            className="bed-legend-dot rounded"
            style={{ backgroundColor: color, width: 14, height: 14 }}
            aria-hidden
          />
          <span className="small">{label}</span>
        </div>
      ))}
    </div>
  )
}
