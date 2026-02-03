import type { NursingNoteSearchResponse, WardTypeSearch } from '../../types/nursingNotes.types'
import styles from './NursingNotesTable.module.css'

const ICU_WARD_TYPES: WardTypeSearch[] = ['ICU', 'CCU', 'NICU', 'HDU']

function isIcuWard(wardType: WardTypeSearch | string | null | undefined): boolean {
  return wardType != null && ICU_WARD_TYPES.includes(wardType as WardTypeSearch)
}

export interface NursingNotesTableProps {
  notes: NursingNoteSearchResponse[]
  onViewNote?: (note: NursingNoteSearchResponse) => void
  onPrintNote?: (note: NursingNoteSearchResponse) => void
  onDownloadPdf?: (note: NursingNoteSearchResponse) => void
  onPrintAll?: () => void
  totalElements?: number
}

export function NursingNotesTable({
  notes,
  onViewNote,
  onPrintNote,
  onDownloadPdf,
  onPrintAll,
  totalElements = 0,
}: NursingNotesTableProps) {
  const formatDateTime = (s: string | null | undefined) =>
    s ? s.replace('T', ' ').slice(0, 16) : '—'

  return (
    <div className={styles.wrap}>
      <div className={styles.toolbar}>
        <span className={styles.count}>
          {totalElements} note{totalElements !== 1 ? 's' : ''}
        </span>
        {onPrintAll && notes.length > 0 && (
          <button type="button" className={styles.printAllBtn} onClick={onPrintAll}>
            Print / PDF (current results)
          </button>
        )}
      </div>
      <div className={styles.tableWrap}>
        <table className={`table table-striped ${styles.table}`} role="grid" aria-label="Nursing notes search results">
          <thead>
            <tr>
              <th scope="col">Patient name</th>
              <th scope="col">UHID</th>
              <th scope="col">Ward / Bed</th>
              <th scope="col">Shift</th>
              <th scope="col">Last updated</th>
              <th scope="col">Status</th>
              <th scope="col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {notes.length === 0 && (
              <tr>
                <td colSpan={7} className={styles.emptyCell}>
                  No notes found.
                </td>
              </tr>
            )}
            {notes.map((n) => (
              <tr
                key={n.noteId}
                className={isIcuWard(n.wardType) ? styles.rowIcu : undefined}
              >
                <td className={styles.cellName}>{n.patientName ?? '—'}</td>
                <td className={styles.cellUhid}>{n.uhid ?? '—'}</td>
                <td>
                  {n.wardName ?? '—'} / {n.bedNo ?? '—'}
                </td>
                <td>
                  <span className={styles.shiftBadge}>{n.shift}</span>
                </td>
                <td className={styles.cellDate}>{formatDateTime(n.lastUpdated ?? n.noteDateTime)}</td>
                <td>
                  <span
                    className={
                      n.status === 'LOCKED' ? styles.statusLocked : styles.statusDraft
                    }
                  >
                    {n.status}
                  </span>
                </td>
                <td className={styles.actions}>
                  {onViewNote && (
                    <button
                      type="button"
                      className={styles.actionBtn}
                      onClick={() => onViewNote(n)}
                      aria-label={`View note ${n.noteId}`}
                    >
                      View
                    </button>
                  )}
                  {onPrintNote && (
                    <button
                      type="button"
                      className={styles.actionBtn}
                      onClick={() => onPrintNote(n)}
                      aria-label={`Print note ${n.noteId}`}
                    >
                      Print
                    </button>
                  )}
                  {onDownloadPdf && (
                    <button
                      type="button"
                      className={styles.actionBtn}
                      onClick={() => onDownloadPdf(n)}
                      aria-label={`Download PDF note ${n.noteId}`}
                    >
                      PDF
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
