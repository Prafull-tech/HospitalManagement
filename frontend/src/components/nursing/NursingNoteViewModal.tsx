import type { NursingNoteSearchResponse } from '../../types/nursingNotes.types'
import styles from './NursingNoteViewModal.module.css'

export interface NursingNoteViewModalProps {
  note: NursingNoteSearchResponse | null
  onClose: () => void
}

export function NursingNoteViewModal({ note, onClose }: NursingNoteViewModalProps) {
  if (!note) return null

  const formatDateTime = (s: string | null | undefined) =>
    s ? s.replace('T', ' ').slice(0, 19) : '—'

  return (
    <div className={styles.overlay} role="dialog" aria-modal="true" aria-labelledby="view-note-title">
      <div className={styles.backdrop} onClick={onClose} aria-hidden />
      <div className={styles.modal}>
        <div className={styles.header}>
          <h2 id="view-note-title" className={styles.title}>
            Nursing note — {note.patientName ?? '—'}
          </h2>
          <button
            type="button"
            className={styles.closeBtn}
            onClick={onClose}
            aria-label="Close"
          >
            ×
          </button>
        </div>
        <div className={styles.meta}>
          <span>{note.uhid ?? '—'}</span>
          <span>{note.wardName ?? '—'} / Bed {note.bedNo ?? '—'}</span>
          <span className={styles.shiftBadge}>{note.shift}</span>
          <span>{formatDateTime(note.noteDateTime)}</span>
          {note.recordedByName && <span>Recorded by {note.recordedByName}</span>}
          <span className={note.status === 'LOCKED' ? styles.statusLocked : styles.statusDraft}>
            {note.status}
          </span>
        </div>
        <div className={styles.content}>{note.content}</div>
      </div>
    </div>
  )
}
