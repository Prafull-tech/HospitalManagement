/**
 * Shared ward census computation – single source of truth for bed availability by ward type.
 * Used by /ipd/beds and /ipd/hospital-beds so both show the same numbers.
 */

import type { WardCensusRow } from '../types/ipdBed.types'

export const WARD_TYPE_LABELS: Record<string, string> = {
  GENERAL: 'General',
  SEMI_PRIVATE: 'Semi Pvt',
  PRIVATE: 'Private',
  ICU: 'ICU',
  CCU: 'CCU',
  NICU: 'NICU',
  HDU: 'HDU',
  EMERGENCY: 'Emergency',
}

export interface BedForCensus {
  wardType?: string | null
  bedStatus: string
}

/**
 * Compute ward-wise census from a list of beds (same logic for /ipd/beds and /ipd/hospital-beds).
 */
export function computeWardCensus(beds: BedForCensus[]): WardCensusRow[] {
  const byWardType = new Map<string, BedForCensus[]>()
  beds.forEach((b) => {
    const key = (b.wardType ?? 'GENERAL').toUpperCase()
    if (!byWardType.has(key)) byWardType.set(key, [])
    byWardType.get(key)!.push(b)
  })
  return Array.from(byWardType.entries())
    .map(([wardType, list]) => {
      const row: WardCensusRow = {
        wardType: wardType as WardCensusRow['wardType'],
        wardTypeLabel: WARD_TYPE_LABELS[wardType] ?? wardType,
        total: list.length,
        occupied: 0,
        vacant: 0,
        reserved: 0,
        cleaning: 0,
        maintenance: 0,
        isolation: 0,
      }
      list.forEach((b) => {
        const s = (b.bedStatus ?? '').toUpperCase()
        if (s === 'AVAILABLE') row.vacant++
        else if (s === 'OCCUPIED') row.occupied++
        else if (s === 'RESERVED') row.reserved++
        else if (s === 'CLEANING') row.cleaning++
        else if (s === 'MAINTENANCE') row.maintenance++
        else if (s === 'ISOLATION') row.isolation++
      })
      return row
    })
    .sort((a, b) => a.wardTypeLabel.localeCompare(b.wardTypeLabel))
}
