import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { opdApi } from '../../api/opd'
import { useAuth } from '../../contexts/AuthContext'
import { formatDisplayDate, getTodayIsoDate, resolveCurrentDoctor } from '../../lib/doctorWorkspace'
import type { DoctorResponse } from '../../types/doctor'
import type { OPDVisitResponse } from '../../types/opd'
import { apiErrorWithNetworkHint } from '../../utils/apiNetworkError'
import styles from './DoctorWorkspace.module.css'

function getStatusClass(status: string): string {
  switch (status) {
    case 'REGISTERED':
      return `${styles.statusBadge} ${styles.statusRegistered}`
    case 'IN_CONSULTATION':
      return `${styles.statusBadge} ${styles.statusInConsultation}`
    case 'COMPLETED':
      return `${styles.statusBadge} ${styles.statusCompleted}`
    default:
      return `${styles.statusBadge} ${styles.statusMuted}`
  }
}

export function DoctorOPDQueuePage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [doctor, setDoctor] = useState<DoctorResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [queue, setQueue] = useState<OPDVisitResponse[]>([])
  const [selectedDate, setSelectedDate] = useState(getTodayIsoDate())
  const [workingVisitId, setWorkingVisitId] = useState<number | null>(null)

  const waitingVisit = useMemo(
    () => queue.find((entry) => entry.visitStatus === 'REGISTERED') || null,
    [queue]
  )

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError('')
      try {
        const resolvedDoctor = await resolveCurrentDoctor(user)
        if (cancelled) return
        setDoctor(resolvedDoctor)
        if (!resolvedDoctor) {
          setQueue([])
          return
        }

        const visits = await opdApi.getQueue(resolvedDoctor.id, selectedDate)
        if (cancelled) return
        setQueue(visits)
      } catch (err: unknown) {
        if (cancelled) return
        setError(apiErrorWithNetworkHint('Failed to load the OPD queue.', err))
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void load()
    return () => {
      cancelled = true
    }
  }, [selectedDate, user])

  const reloadQueue = async () => {
    if (!doctor) return
    setLoading(true)
    setError('')
    try {
      const visits = await opdApi.getQueue(doctor.id, selectedDate)
      setQueue(visits)
    } catch (err: unknown) {
      setError(apiErrorWithNetworkHint('Failed to refresh the OPD queue.', err))
    } finally {
      setLoading(false)
    }
  }

  const handleCallNext = async () => {
    if (!waitingVisit) return
    setWorkingVisitId(waitingVisit.id)
    setError('')
    try {
      await opdApi.updateStatus(waitingVisit.id, { status: 'IN_CONSULTATION' })
      await reloadQueue()
      navigate(`/opd/visits/${waitingVisit.id}`)
    } catch (err: unknown) {
      setError(apiErrorWithNetworkHint('Failed to move the next patient into consultation.', err))
    } finally {
      setWorkingVisitId(null)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <div>
          <h1 className={styles.panelTitle}>OPD Queue</h1>
          <p className={styles.panelText}>
            {doctor ? `Token-driven queue for ${doctor.fullName}.` : 'Token-driven queue for the active doctor workspace.'}
          </p>
        </div>
        <div className={styles.toolbarGroup}>
          <button type="button" className="btn btn-primary btn-sm" disabled={!waitingVisit || !!workingVisitId} onClick={handleCallNext}>
            {workingVisitId ? 'Opening...' : 'Call next patient'}
          </button>
          <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => void reloadQueue()} disabled={loading}>
            Refresh
          </button>
        </div>
      </div>

      <section className={styles.panel}>
        <div className={styles.toolbar}>
          <div>
            <label htmlFor="doctor-queue-date" className="form-label small mb-1">Queue date</label>
            <input id="doctor-queue-date" type="date" className="form-control form-control-sm" value={selectedDate} onChange={(event) => setSelectedDate(event.target.value)} />
          </div>
          <div className="text-muted small">Showing queue for {formatDisplayDate(selectedDate)}</div>
        </div>
      </section>

      {error ? <div className={styles.alert}>{error}</div> : null}

      {!loading && !doctor ? (
        <div className={styles.placeholderCard}>
          <h2 className={styles.panelTitle}>Doctor record required</h2>
          <p className={styles.placeholderText}>Link this login to a doctor record to activate the token queue and consultation handoff.</p>
        </div>
      ) : null}

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <div>
            <h2 className={styles.panelTitle}>Queue status</h2>
            <p className={styles.panelText}>Move the next waiting patient into consultation or open any active visit directly.</p>
          </div>
        </div>
        {loading ? (
          <div className={styles.emptyState}>Loading queue...</div>
        ) : queue.length === 0 ? (
          <div className={styles.emptyState}>No queue entries found for this date.</div>
        ) : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Token</th>
                  <th>Patient</th>
                  <th>Status</th>
                  <th>Visit</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {queue.map((visit) => (
                  <tr key={visit.id}>
                    <td>{visit.tokenNumber ?? '-'}</td>
                    <td>
                      <div>{visit.patientName}</div>
                      <div className="text-muted small">Department: {visit.departmentName}</div>
                    </td>
                    <td><span className={getStatusClass(visit.visitStatus)}>{visit.visitStatus.replace(/_/g, ' ')}</span></td>
                    <td>{visit.visitNumber}</td>
                    <td>
                      <div className="d-flex flex-wrap gap-2">
                        <Link to={`/opd/visits/${visit.id}`} className="btn btn-sm btn-outline-primary">Open consultation</Link>
                        {visit.visitStatus === 'REGISTERED' ? (
                          <button
                            type="button"
                            className="btn btn-sm btn-primary"
                            disabled={workingVisitId === visit.id}
                            onClick={async () => {
                              setWorkingVisitId(visit.id)
                              setError('')
                              try {
                                await opdApi.updateStatus(visit.id, { status: 'IN_CONSULTATION' })
                                await reloadQueue()
                              } catch (err: unknown) {
                                setError(apiErrorWithNetworkHint('Failed to update the queue status.', err))
                              } finally {
                                setWorkingVisitId(null)
                              }
                            }}
                          >
                            {workingVisitId === visit.id ? 'Updating...' : 'Mark in consultation'}
                          </button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}