import { useState, useEffect, useCallback } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { searchNotes } from '../../services/nursingNotesService'
import type {
  NursingNoteSearchParams,
  NursingNoteSearchResponse,
  NursingNoteSearchPageResponse,
  WardTypeSearch,
} from '../../types/nursingNotes.types'
import { NursingNoteFilters } from '../../components/nursing/NursingNoteFilters'
import { NursingNotesTable } from '../../components/nursing/NursingNotesTable'
import { NursingNoteViewModal } from '../../components/nursing/NursingNoteViewModal'
import { NursingNotesTableSkeleton } from '../../components/nursing/NursingNotesTableSkeleton'
import styles from './SearchNursingNotes.module.css'

const PAGE_SIZE = 20

const VALID_WARD_TYPES: WardTypeSearch[] = [
  'GENERAL', 'PRIVATE', 'SEMI_PRIVATE', 'ICU', 'CCU', 'NICU', 'HDU', 'EMERGENCY',
]

function initialFiltersFromSearchParams(searchParams: URLSearchParams): NursingNoteSearchParams {
  const wardType = searchParams.get('wardType')
  const validWard = wardType && VALID_WARD_TYPES.includes(wardType as WardTypeSearch)
    ? (wardType as WardTypeSearch)
    : undefined
  return {
    page: 0,
    size: PAGE_SIZE,
    wardType: validWard,
    fromDate: searchParams.get('fromDate') ?? undefined,
    toDate: searchParams.get('toDate') ?? undefined,
  }
}

export function SearchNursingNotes() {
  const [searchParams] = useSearchParams()
  const [q, setQ] = useState('')
  const [filters, setFilters] = useState<NursingNoteSearchParams>(() =>
    initialFiltersFromSearchParams(searchParams)
  )
  const [filtersExpanded, setFiltersExpanded] = useState(!!searchParams.get('wardType'))
  const [result, setResult] = useState<NursingNoteSearchPageResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [viewNote, setViewNote] = useState<NursingNoteSearchResponse | null>(null)

  const runSearch = useCallback((pageNum: number = 0) => {
    const params: NursingNoteSearchParams = {
      ...filters,
      page: pageNum,
      size: PAGE_SIZE,
    }
    if (q.trim()) params.q = q.trim()
    setLoading(true)
    setError('')
    searchNotes(params)
      .then((data) =>
        setResult({
          content: data.content,
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          number: data.number,
          size: data.size,
        })
      )
      .catch((err) => {
        setError(err.response?.data?.message || 'Search failed.')
        setResult(null)
      })
      .finally(() => setLoading(false))
  }, [q, filters])

  useEffect(() => {
    runSearch(0)
    // Initial load only; Search / Apply filters / pagination call runSearch explicitly
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setFilters((prev) => ({ ...prev, page: 0 }))
    runSearch(0)
  }

  const handleFiltersSearch = () => {
    setFilters((prev) => ({ ...prev, page: 0 }))
    runSearch(0)
  }

  const handlePageChange = (nextPage: number) => {
    setFilters((prev) => ({ ...prev, page: nextPage }))
    runSearch(nextPage)
  }

  const openPrint = (note: NursingNoteSearchResponse) => {
    const params = new URLSearchParams()
    params.set('ipdAdmissionId', String(note.ipdAdmissionId))
    window.open(`/nursing/notes/print?${params.toString()}`, '_blank', 'noopener,noreferrer')
  }

  const openPrintAll = () => {
    const params = new URLSearchParams()
    if (filters.fromDate) params.set('recordedDateFrom', filters.fromDate)
    if (filters.toDate) params.set('recordedDateTo', filters.toDate)
    if (filters.shift) params.set('shiftType', filters.shift)
    const ids = [...new Set((result?.content ?? []).map((n) => n.ipdAdmissionId))]
    if (ids.length === 1) params.set('ipdAdmissionId', String(ids[0]))
    window.open(`/nursing/notes/print?${params.toString()}`, '_blank', 'noopener,noreferrer')
  }

  const page = result?.number ?? 0
  const totalPages = result?.totalPages ?? 0

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>Search Nursing Notes</h1>
        <Link to="/nursing/notes" className={styles.backLink}>
          ← Back to Notes
        </Link>
      </div>

      <form onSubmit={handleSearchSubmit} className={styles.searchCard}>
        <label htmlFor="search-q" className={styles.searchLabel}>
          Patient name or UHID (priority search)
        </label>
        <div className={styles.searchRow}>
          <input
            id="search-q"
            type="text"
            className={styles.searchInput}
            placeholder="Type patient name or UHID and press Enter"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            aria-label="Search by patient name or UHID"
            autoFocus
          />
          <button type="submit" className={styles.searchBtn} disabled={loading}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </div>
        <NursingNoteFilters
          filters={filters}
          onFiltersChange={setFilters}
          onSearch={handleFiltersSearch}
          loading={loading}
          expanded={filtersExpanded}
          onToggleExpand={() => setFiltersExpanded((e) => !e)}
        />
      </form>

      {error && <div className={styles.error} role="alert">{error}</div>}

      <div className={styles.resultsCard}>
        {loading && !result && <NursingNotesTableSkeleton />}
        {!loading && result && (
          <>
            <NursingNotesTable
              notes={result.content}
              onViewNote={setViewNote}
              onPrintNote={openPrint}
              onDownloadPdf={openPrint}
              onPrintAll={openPrintAll}
              totalElements={result.totalElements}
            />
            {totalPages > 1 && (
              <nav className={styles.pagination} aria-label="Pagination">
                <button
                  type="button"
                  className={styles.pageBtn}
                  disabled={page <= 0}
                  onClick={() => handlePageChange(page - 1)}
                  aria-label="Previous page"
                >
                  Previous
                </button>
                <span className={styles.pageInfo}>
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  type="button"
                  className={styles.pageBtn}
                  disabled={page >= totalPages - 1}
                  onClick={() => handlePageChange(page + 1)}
                  aria-label="Next page"
                >
                  Next
                </button>
              </nav>
            )}
          </>
        )}
        {!loading && result && result.content.length === 0 && (
          <p className={styles.empty}>No notes found. Try adjusting your search or filters.</p>
        )}
      </div>

      {viewNote && (
        <NursingNoteViewModal note={viewNote} onClose={() => setViewNote(null)} />
      )}
    </div>
  )
}
