import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import { labApi } from '../../api/lab'
import { opdApi } from '../../api/opd'
import { useAuth } from '../../contexts/AuthContext'
import { formatDisplayDate, formatDisplayDateTime, getTodayIsoDate, resolveCurrentDoctor } from '../../lib/doctorWorkspace'
import type { DoctorResponse } from '../../types/doctor'
import type { LabOrder } from '../../types/lab'
import type { OPDVisitResponse } from '../../types/opd'
import { apiErrorWithNetworkHint } from '../../utils/apiNetworkError'
import styles from './DoctorWorkspace.module.css'

type DashboardState = {
  todayAppointments: number
  waitingPatients: number
  completedConsultations: number
  pendingLabReports: number
  recentPatients: OPDVisitResponse[]
  todayQueue: OPDVisitResponse[]
  upcomingAppointments: {
    id: number
    patientName: string
    appointmentDate: string
    slotTime: string
    status: string
    opdVisitId: number | null
    patientId: number
  }[]
}

const EMPTY_STATE: DashboardState = {
  todayAppointments: 0,
  waitingPatients: 0,
  completedConsultations: 0,
  pendingLabReports: 0,
  recentPatients: [],
  todayQueue: [],
  upcomingAppointments: [],
}

function getStatusClass(status: string): string {
  switch (status) {
    case 'REGISTERED':
    case 'BOOKED':
    case 'CONFIRMED':
      return `${styles.statusBadge} ${styles.statusRegistered}`
    case 'IN_CONSULTATION':
      return `${styles.statusBadge} ${styles.statusInConsultation}`
    case 'COMPLETED':
      return `${styles.statusBadge} ${styles.statusCompleted}`
    default:
      return `${styles.statusBadge} ${styles.statusMuted}`
  }
}

export function DoctorDashboardPage() {
  const { user } = useAuth()
  const [doctor, setDoctor] = useState<DoctorResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [state, setState] = useState<DashboardState>(EMPTY_STATE)
  const today = useMemo(() => getTodayIsoDate(), [])

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
          setState(EMPTY_STATE)
          return
        }

        const [appointmentsResult, queueResult, completedResult, recentVisitsResult, labOrdersResult] = await Promise.allSettled([
          appointmentApi.search({ doctorId: resolvedDoctor.id, date: today, page: 0, size: 20 }),
          opdApi.getQueue(resolvedDoctor.id, today),
          opdApi.search({ doctorId: resolvedDoctor.id, visitDate: today, status: 'COMPLETED', page: 0, size: 100 }),
          opdApi.search({ doctorId: resolvedDoctor.id, page: 0, size: 5 }),
          labApi.listOrders(),
        ])

        if (cancelled) return

        const appointments = appointmentsResult.status === 'fulfilled' ? appointmentsResult.value.content : []
        const queue = queueResult.status === 'fulfilled' ? queueResult.value : []
        const completed = completedResult.status === 'fulfilled' ? completedResult.value.content : []
        const recentPatients = recentVisitsResult.status === 'fulfilled' ? recentVisitsResult.value.content : []
        const allLabOrders = labOrdersResult.status === 'fulfilled' ? labOrdersResult.value : []

        const pendingLabReports = (allLabOrders as LabOrder[])
          .filter((order) => order.orderedByDoctorId === resolvedDoctor.id)
          .filter((order) => order.status !== 'COMPLETED' && order.status !== 'CANCELLED')
          .length

        setState({
          todayAppointments: appointments.length,
          waitingPatients: queue.filter((entry) => entry.visitStatus === 'REGISTERED').length,
          completedConsultations: completed.length,
          pendingLabReports,
          recentPatients,
          todayQueue: queue,
          upcomingAppointments: appointments.map((entry) => ({
            id: entry.id,
            patientName: entry.patientName,
            appointmentDate: entry.appointmentDate,
            slotTime: entry.slotTime,
            status: entry.status,
            opdVisitId: entry.opdVisitId,
            patientId: entry.patientId,
          })),
        })
      } catch (err: unknown) {
        if (cancelled) return
        setError(apiErrorWithNetworkHint('Failed to load the doctor workspace.', err))
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
  }, [today, user])

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div>
          <span className={styles.heroEyebrow}>Doctor Workspace</span>
          <h1 className={styles.heroTitle}>Clinical workflow first, admin noise out of the way.</h1>
          <p className={styles.heroText}>
            Review your appointments, open the OPD queue, continue consultations, and keep a tight handle on pending lab work from one place.
          </p>
        </div>
        <div className={styles.heroMeta}>
          <div className={styles.heroPill}>
            <span className={styles.heroPillLabel}>Doctor</span>
            <div className={styles.heroPillValue}>{doctor?.fullName || user?.fullName || 'Doctor profile pending'}</div>
            <p className={styles.heroPillText}>{doctor ? `${doctor.departmentName} · ${doctor.specialization || doctor.doctorType}` : 'Match your login to a doctor master record to unlock doctor-specific data.'}</p>
          </div>
          <div className={styles.heroPill}>
            <span className={styles.heroPillLabel}>Workspace</span>
            <div className={styles.heroPillValue}>{user?.hospitalName || 'Hospital'}</div>
            <p className={styles.heroPillText}>Date: {formatDisplayDate(today)}</p>
          </div>
        </div>
      </section>

      {error ? <div className={styles.alert}>{error}</div> : null}

      {!loading && !doctor ? (
        <div className={styles.placeholderCard}>
          <h2 className={styles.panelTitle}>Doctor identity needs to be linked</h2>
          <p className={styles.placeholderText}>
            Your login is working, but this account could not be matched to an active doctor record yet. The new doctor workspace is live; once the doctor master record matches your profile, the dashboard will auto-populate with appointments, queue data, and lab activity.
          </p>
        </div>
      ) : null}

      <section className={styles.statsGrid}>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>Today's Appointments</span>
          <div className={styles.statValue}>{loading ? '...' : state.todayAppointments}</div>
          <div className={styles.statMeta}>Booked and confirmed appointments on your schedule.</div>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>Waiting Patients</span>
          <div className={styles.statValue}>{loading ? '...' : state.waitingPatients}</div>
          <div className={styles.statMeta}>Registered OPD patients still waiting to be seen.</div>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>Completed Consultations</span>
          <div className={styles.statValue}>{loading ? '...' : state.completedConsultations}</div>
          <div className={styles.statMeta}>Visits marked completed today.</div>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>Pending Lab Reports</span>
          <div className={styles.statValue}>{loading ? '...' : state.pendingLabReports}</div>
          <div className={styles.statMeta}>Lab orders raised by you that are still in progress.</div>
        </article>
      </section>

      <section className={styles.contentGrid}>
        <article className={styles.panel}>
          <div className={styles.panelHeader}>
            <div>
              <h2 className={styles.panelTitle}>Today's schedule</h2>
              <p className={styles.panelText}>The nearest appointments and direct jump-off points into consultation.</p>
            </div>
            <Link to="/doctor/appointments" className="btn btn-outline-primary btn-sm">View all</Link>
          </div>
          {state.upcomingAppointments.length === 0 ? (
            <div className={styles.emptyState}>No appointments were found for today.</div>
          ) : (
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Patient</th>
                    <th>Slot</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {state.upcomingAppointments.slice(0, 5).map((entry) => (
                    <tr key={entry.id}>
                      <td>{entry.patientName}</td>
                      <td>{formatDisplayDateTime(entry.appointmentDate, entry.slotTime)}</td>
                      <td><span className={getStatusClass(entry.status)}>{entry.status.replace(/_/g, ' ')}</span></td>
                      <td>
                        {entry.opdVisitId ? (
                          <Link to={`/opd/visits/${entry.opdVisitId}`} className="btn btn-sm btn-primary">Start consultation</Link>
                        ) : (
                          <span className="text-muted small">Await OPD visit</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </article>

        <article className={styles.panel}>
          <div className={styles.panelHeader}>
            <div>
              <h2 className={styles.panelTitle}>Quick actions</h2>
              <p className={styles.panelText}>Open the daily doctor workflow without leaving this workspace.</p>
            </div>
          </div>
          <div className={styles.linkGrid}>
            <Link to="/doctor/opd-queue" className={styles.linkCard}>
              <span className={styles.linkTitle}>Open OPD queue</span>
              <span className={styles.linkHint}>See waiting patients and move the next token into consultation.</span>
            </Link>
            <Link to="/doctor/appointments" className={styles.linkCard}>
              <span className={styles.linkTitle}>Review appointments</span>
              <span className={styles.linkHint}>Switch between daily and weekly schedule views.</span>
            </Link>
            <Link to="/doctor/lab" className={styles.linkCard}>
              <span className={styles.linkTitle}>Open lab results</span>
              <span className={styles.linkHint}>Review results while dedicated doctor lab ordering is being wired.</span>
            </Link>
            <Link to="/doctor/prescriptions" className={styles.linkCard}>
              <span className={styles.linkTitle}>Prescription workspace</span>
              <span className={styles.linkHint}>Reserved for the upcoming prescription authoring flow.</span>
            </Link>
          </div>
        </article>
      </section>

      <section className={styles.contentGrid}>
        <article className={styles.panel}>
          <div className={styles.panelHeader}>
            <div>
              <h2 className={styles.panelTitle}>Recent patients</h2>
              <p className={styles.panelText}>Your latest OPD visits with direct access to the consultation record.</p>
            </div>
          </div>
          {state.recentPatients.length === 0 ? (
            <div className={styles.emptyState}>No recent visits were found for this doctor.</div>
          ) : (
            <ul className={styles.list}>
              {state.recentPatients.map((visit) => (
                <li key={visit.id} className={styles.listItem}>
                  <p className={styles.listTitle}>{visit.patientName}</p>
                  <p className={styles.listMeta}>Visit #{visit.visitNumber} · Token {visit.tokenNumber ?? '-'} · {visit.departmentName}</p>
                  <div className="d-flex flex-wrap align-items-center gap-2 mt-3">
                    <span className={getStatusClass(visit.visitStatus)}>{visit.visitStatus.replace(/_/g, ' ')}</span>
                    <Link to={`/opd/visits/${visit.id}`} className="btn btn-sm btn-outline-primary">Open visit</Link>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </article>

        <article className={styles.panel}>
          <div className={styles.panelHeader}>
            <div>
              <h2 className={styles.panelTitle}>Today's queue</h2>
              <p className={styles.panelText}>Live queue state from the OPD visit register.</p>
            </div>
            <Link to="/doctor/opd-queue" className="btn btn-outline-primary btn-sm">Manage queue</Link>
          </div>
          {state.todayQueue.length === 0 ? (
            <div className={styles.emptyState}>No OPD queue entries found for today.</div>
          ) : (
            <ul className={styles.list}>
              {state.todayQueue.slice(0, 6).map((visit) => (
                <li key={visit.id} className={styles.listItem}>
                  <div className="d-flex align-items-start justify-content-between gap-3">
                    <div>
                      <p className={styles.listTitle}>Token {visit.tokenNumber ?? '-'} · {visit.patientName}</p>
                      <p className={styles.listMeta}>Visit #{visit.visitNumber} · {visit.departmentName}</p>
                    </div>
                    <span className={getStatusClass(visit.visitStatus)}>{visit.visitStatus.replace(/_/g, ' ')}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </article>
      </section>
    </div>
  )
}