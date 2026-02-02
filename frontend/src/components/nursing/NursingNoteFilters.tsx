import type {
  WardTypeSearch,
  ShiftTypeSearch,
  NoteStatusSearch,
  NursingNoteSearchParams,
} from '../../types/nursingNotes.types'
import styles from './NursingNoteFilters.module.css'

const WARD_OPTIONS: { value: '' | WardTypeSearch; label: string }[] = [
  { value: '', label: 'Any ward' },
  { value: 'GENERAL', label: 'General Ward' },
  { value: 'PRIVATE', label: 'Private Ward' },
  { value: 'SEMI_PRIVATE', label: 'Semi-private' },
  { value: 'ICU', label: 'ICU' },
  { value: 'CCU', label: 'CCU' },
  { value: 'NICU', label: 'NICU' },
  { value: 'HDU', label: 'HDU' },
  { value: 'EMERGENCY', label: 'Emergency' },
]

const SHIFT_OPTIONS: { value: '' | ShiftTypeSearch; label: string }[] = [
  { value: '', label: 'Any shift' },
  { value: 'MORNING', label: 'Morning' },
  { value: 'EVENING', label: 'Evening' },
  { value: 'NIGHT', label: 'Night' },
]

const STATUS_OPTIONS: { value: '' | NoteStatusSearch; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'DRAFT', label: 'Draft' },
  { value: 'LOCKED', label: 'Locked' },
]

export interface NursingNoteFiltersProps {
  filters: NursingNoteSearchParams
  onFiltersChange: (f: NursingNoteSearchParams) => void
  onSearch: () => void
  loading?: boolean
  expanded?: boolean
  onToggleExpand?: () => void
}

export function NursingNoteFilters({
  filters,
  onFiltersChange,
  onSearch,
  loading = false,
  expanded = true,
  onToggleExpand,
}: NursingNoteFiltersProps) {
  const set = (key: keyof NursingNoteSearchParams, value: string | number | undefined) => {
    onFiltersChange({ ...filters, [key]: value })
  }

  return (
    <div className={styles.wrap}>
      <button
        type="button"
        className={styles.toggle}
        onClick={onToggleExpand}
        aria-expanded={expanded}
        aria-controls="nursing-note-filters-panel"
      >
        {expanded ? '▼' : '▶'} Advanced filters
      </button>
      <div id="nursing-note-filters-panel" className={styles.panel} data-expanded={expanded}>
        <div className={styles.grid}>
          <label className={styles.label}>
            Ward type
            <select
              value={filters.wardType ?? ''}
              onChange={(e) => set('wardType', (e.target.value || undefined) as WardTypeSearch | undefined)}
              className={styles.select}
              aria-label="Ward type"
            >
              {WARD_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            Bed no
            <input
              type="text"
              value={filters.bedNo ?? ''}
              onChange={(e) => set('bedNo', e.target.value.trim() || undefined)}
              placeholder="Bed number"
              className={styles.input}
              aria-label="Bed number"
            />
          </label>
          <label className={styles.label}>
            Shift
            <select
              value={filters.shift ?? ''}
              onChange={(e) => set('shift', (e.target.value || undefined) as ShiftTypeSearch | undefined)}
              className={styles.select}
              aria-label="Shift"
            >
              {SHIFT_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            Status
            <select
              value={filters.status ?? ''}
              onChange={(e) => set('status', (e.target.value || undefined) as NoteStatusSearch | undefined)}
              className={styles.select}
              aria-label="Note status"
            >
              {STATUS_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            From date
            <input
              type="date"
              value={filters.fromDate ?? ''}
              onChange={(e) => set('fromDate', e.target.value || undefined)}
              className={styles.input}
              aria-label="Date from"
            />
          </label>
          <label className={styles.label}>
            To date
            <input
              type="date"
              value={filters.toDate ?? ''}
              onChange={(e) => set('toDate', e.target.value || undefined)}
              className={styles.input}
              aria-label="Date to"
            />
          </label>
        </div>
        <button
          type="button"
          className={styles.searchBtn}
          onClick={onSearch}
          disabled={loading}
          aria-label="Apply filters"
        >
          {loading ? 'Searching…' : 'Apply filters'}
        </button>
      </div>
    </div>
  )
}
