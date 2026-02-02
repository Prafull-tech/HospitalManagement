/**
 * IPD Beds – filter panel. Updates via onChange; no page reload.
 */

import type { BedFiltersParams, BedStatus, WardType } from '../../types/ipdBed.types'

const WARD_TYPES: { value: WardType; label: string }[] = [
  { value: 'GENERAL', label: 'General' },
  { value: 'SEMI_PRIVATE', label: 'Semi Pvt' },
  { value: 'PRIVATE', label: 'Private' },
  { value: 'ICU', label: 'ICU' },
  { value: 'CCU', label: 'CCU' },
  { value: 'NICU', label: 'NICU' },
  { value: 'HDU', label: 'HDU' },
  { value: 'EMERGENCY', label: 'Emergency' },
]

const BED_STATUSES: { value: BedStatus; label: string }[] = [
  { value: 'AVAILABLE', label: 'Vacant' },
  { value: 'OCCUPIED', label: 'Occupied' },
  { value: 'RESERVED', label: 'Reserved' },
  { value: 'CLEANING', label: 'Cleaning' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'ISOLATION', label: 'Isolation' },
]

export interface BedFiltersProps {
  filters: BedFiltersParams
  onChange: (f: BedFiltersParams) => void
  wardNames?: string[]
}

export function BedFilters({ filters, onChange, wardNames = [] }: BedFiltersProps) {
  const set = (key: keyof BedFiltersParams, value: string | number | undefined) => {
    onChange({ ...filters, [key]: value })
  }

  return (
    <div className="card border shadow-sm mb-3">
      <div className="card-body">
        <h6 className="card-title text-muted mb-3">Filters</h6>
        <div className="row g-2 g-md-3">
          <div className="col-12 col-md-6 col-lg-2">
            <label className="form-label small">Ward Type</label>
            <select
              className="form-select form-select-sm"
              value={filters.wardType ?? ''}
              onChange={(e) => set('wardType', e.target.value ? (e.target.value as WardType) : undefined)}
            >
              <option value="">All</option>
              {WARD_TYPES.map((t) => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </div>
          <div className="col-12 col-md-6 col-lg-2">
            <label className="form-label small">Ward Name</label>
            <select
              className="form-select form-select-sm"
              value={filters.wardName ?? ''}
              onChange={(e) => set('wardName', e.target.value || undefined)}
            >
              <option value="">All</option>
              {wardNames.map((name) => (
                <option key={name} value={name}>{name}</option>
              ))}
            </select>
          </div>
          <div className="col-12 col-md-6 col-lg-2">
            <label className="form-label small">Bed Status</label>
            <select
              className="form-select form-select-sm"
              value={filters.bedStatus ?? ''}
              onChange={(e) => set('bedStatus', e.target.value ? (e.target.value as BedStatus) : undefined)}
            >
              <option value="">All</option>
              {BED_STATUSES.map((s) => (
                <option key={s.value} value={s.value}>{s.label}</option>
              ))}
            </select>
          </div>
          <div className="col-12 col-md-6 col-lg-2">
            <label className="form-label small">Floor</label>
            <input
              type="text"
              className="form-control form-control-sm"
              placeholder="Optional"
              value={filters.floor ?? ''}
              onChange={(e) => set('floor', e.target.value || undefined)}
            />
          </div>
          <div className="col-12 col-md-6 col-lg-3">
            <label className="form-label small">Search by Bed No</label>
            <input
              type="text"
              className="form-control form-control-sm"
              placeholder="Bed number"
              value={filters.search ?? ''}
              onChange={(e) => set('search', e.target.value || undefined)}
            />
          </div>
        </div>
      </div>
    </div>
  )
}
