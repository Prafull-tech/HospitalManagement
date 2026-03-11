/**
 * IPD Bed Management – Ward type filter for bed layout view.
 * Spec: All, General, Private, ICU, CCU, NICU.
 */

import type { WardType } from '../../types/ipdBed.types'

const WARD_TYPE_OPTIONS: { value: WardType | ''; label: string }[] = [
  { value: '', label: 'All' },
  { value: 'GENERAL', label: 'General' },
  { value: 'PRIVATE', label: 'Private' },
  { value: 'ICU', label: 'ICU' },
  { value: 'CCU', label: 'CCU' },
  { value: 'NICU', label: 'NICU' },
]

export interface WardFilterProps {
  value: WardType | undefined
  onChange: (wardType: WardType | undefined) => void
  className?: string
}

export function WardFilter({ value, onChange, className = '' }: WardFilterProps) {
  return (
    <div className={`ward-filter d-flex align-items-center gap-2 ${className}`}>
      <label htmlFor="ward-type-filter" className="form-label mb-0 small fw-medium">
        Ward Type:
      </label>
      <select
        id="ward-type-filter"
        className="form-select form-select-sm"
        style={{ width: 'auto', minWidth: 140 }}
        value={value ?? ''}
        onChange={(e) => onChange((e.target.value || undefined) as WardType | undefined)}
      >
        {WARD_TYPE_OPTIONS.map((opt) => (
          <option key={opt.value || 'all'} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  )
}
