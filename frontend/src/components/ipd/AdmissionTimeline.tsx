import type { ViewAdmissionTimelineEvent } from '../../types/ipdAdmission.types'
import styles from './ViewAdmissionCards.module.css'

export interface AdmissionTimelineProps {
  events: ViewAdmissionTimelineEvent[]
}

function formatTimestamp(iso: string): string {
  const d = new Date(iso)
  return d.toLocaleString(undefined, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function AdmissionTimeline({ events }: AdmissionTimelineProps) {
  const sorted = [...events].sort(
    (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
  )

  return (
    <section className={styles.card} aria-label="Timeline and activity log">
      <h2 className={styles.cardTitle}>Timeline / Activity Log</h2>
      {sorted.length === 0 ? (
        <p className={styles.noData}>No timeline events recorded.</p>
      ) : (
        <ul className={styles.timelineList}>
          {sorted.map((evt, i) => (
            <li key={`${evt.timestamp}-${evt.eventType}-${i}`} className={styles.timelineItem}>
              <span className={styles.timelineTime}>{formatTimestamp(evt.timestamp)}</span>
              <div>
                <div className={styles.timelineTitle}>{evt.title}</div>
                {evt.description && <div className={styles.timelineDesc}>{evt.description}</div>}
                {evt.sourceModule && (
                  <div className={styles.timelineSource}>{evt.sourceModule}</div>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
