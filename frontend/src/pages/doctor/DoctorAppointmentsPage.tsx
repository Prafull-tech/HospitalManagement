import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { appointmentApi } from '../../api/appointment'
import { useAuth } from '../../contexts/AuthContext'
import { formatDisplayDate, formatDisplayDateTime, getTodayIsoDate, getWeekDates, resolveCurrentDoctor } from '../../lib/doctorWorkspace'
import type { AppointmentResponse, AppointmentStatus } from '../../types/appointment.types'
import type { DoctorResponse } from '../../types/doctor'
import { apiErrorWithNetworkHint } from '../../utils/apiNetworkError'
import styles from './DoctorWorkspace.module.css'

function getStatusClass(status: AppointmentStatus): string {
  switch (status) {
    case 'BOOKED':
    case 'CONFIRMED':
    case 'PENDING_CONFIRMATION':
      return `${styles.statusBadge} ${styles.statusRegistered}`
    case 'COMPLETED':
      return `${styles.statusBadge} ${styles.statusCompleted}`
    default:
      return `${styles.statusBadge} ${styles.statusMuted}`
  }
}

export function DoctorAppointmentsPage() {
  const { user } = useAuth()
  const [doctor, setDoctor] = useState<DoctorResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [mode, setMode] = useState<'day' | 'week'>('day')
  const [selectedDate, setSelectedDate] = useState(getTodayIsoDate())
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([])
  const [statusFilter, setStatusFilter] = useState<AppointmentStatus | ''>('')

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
          setAppointments([])
          return
        }

        const dates = mode === 'week' ? getWeekDates(selectedDate) : [selectedDate]
        const responses = await Promise.all(
          dates.map((date) => appointmentApi.search({ doctorId: resolvedDoctor.id, date, page: 0, size: 50 }))
        )

        if (cancelled) return
        const merged = responses
          .flatMap((entry) => entry.content)
          .sort((left, right) => `${left.appointmentDate}${left.slotTime}`.localeCompare(`${right.appointmentDate}${right.slotTime}`))
        setAppointments(merged)
      } catch (err: unknown) {
        if (cancelled) return
        setError(apiErrorWithNetworkHint('Failed to load doctor appointments.', err))
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
  }, [mode, selectedDate, user])

  const filteredAppointments = useMemo(
    () => appointments.filter((entry) => !statusFilter || entry.status === statusFilter),
    [appointments, statusFilter]
  )

  return (
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <div>
          <h1 className={styles.panelTitle}>My Appointments</h1>
          <p className={styles.panelText}>
            {doctor ? `Viewing the ${mode === 'day' ? 'daily' : 'weekly'} appointment schedule for ${doctor.fullName}.` : 'Doctor-linked appointment view.'}
          </p>
        </div>
        <div className={styles.toolbarGroup}>
          <button type="button" className={`btn btn-sm ${mode === 'day' ? 'btn-primary' : 'btn-outline-primary'}`} onClick={() => setMode('day')}>
            Daily
          </button>
          <button type="button" className={`btn btn-sm ${mode === 'week' ? 'btn-primary' : 'btn-outline-primary'}`} onClick={() => setMode('week')}>
            Weekly
          </button>
        </div>
      </div>

      <section className={styles.panel}>
        <div className={styles.toolbar}>
          <div className={styles.toolbarGroup}>
            <div>
              <label htmlFor="doctor-appt-date" className="form-label small mb-1">Anchor date</label>
              <input id="doctor-appt-date" type="date" className="form-control form-control-sm" value={selectedDate} onChange={(event) => setSelectedDate(event.target.value)} />
            </div>
            <div>
              <label htmlFor="doctor-appt-status" className="form-label small mb-1">Status</label>
              <select id="doctor-appt-status" className="form-select form-select-sm" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as AppointmentStatus | '')}>
                <option value="">All statuses</option>
                <option value="BOOKED">Booked</option>
                <option value="CONFIRMED">Confirmed</option>
                <option value="PENDING_CONFIRMATION">Pending confirmation</option>
                <option value="COMPLETED">Completed</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="NO_SHOW">No-show</option>
              </select>
            </div>
          </div>
          <div className="text-muted small">{mode === 'day' ? formatDisplayDate(selectedDate) : `Week of ${formatDisplayDate(getWeekDates(selectedDate)[0])}`}</div>
        </div>
      </section>

      {error ? <div className={styles.alert}>{error}</div> : null}

      {!loading && !doctor ? (
        <div className={styles.placeholderCard}>
          <h2 className={styles.panelTitle}>Doctor record required</h2>
          <p className={styles.placeholderText}>This page will become live after the logged-in user is linked to an active doctor master record.</p>
        </div>
      ) : null}

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <div>
            <h2 className={styles.panelTitle}>Schedule</h2>
            <p className={styles.panelText}>Track appointment status and jump into consultation once an OPD visit exists.</p>
          </div>
        </div>
        {loading ? (
          <div className={styles.emptyState}>Loading appointments...</div>
        ) : filteredAppointments.length === 0 ? (
          <div className={styles.emptyState}>No appointments found for the selected period.</div>
        ) : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Patient</th>
                  <th>Slot</th>
                  <th>Status</th>
                  <th>Visit</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredAppointments.map((entry) => (
                  <tr key={entry.id}>
                    <td>
                      <div>{entry.patientName}</div>
                      <div className="text-muted small">UHID: {entry.patientUhid}</div>
                    </td>
                    <td>{formatDisplayDateTime(entry.appointmentDate, entry.slotTime)}</td>
                    <td><span className={getStatusClass(entry.status)}>{entry.status.replace(/_/g, ' ')}</span></td>
                    <td>{entry.opdVisitId ? `OPD #${entry.opdVisitId}` : 'Not yet created'}</td>
                    <td>
                      <div className="d-flex flex-wrap gap-2">
                        {entry.opdVisitId ? (
                          <Link to={`/opd/visits/${entry.opdVisitId}`} className="btn btn-sm btn-primary">Start consultation</Link>
                        ) : (
                          <span className="text-muted small">Await OPD conversion</span>
                        )}
                        <Link to={`/reception/patient/${entry.patientId}`} className="btn btn-sm btn-outline-secondary">View history</Link>
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