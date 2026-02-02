import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { opdApi } from '../api/opd'
import type { OPDVisitResponse, VisitStatus } from '../types/opd'
import type { DoctorResponse } from '../types/doctor'
import styles from './OPDQueuePage.module.css'

const today = new Date().toISOString().slice(0, 10)

function statusClass(s: VisitStatus): string {
  switch (s) {
    case 'REGISTERED':
      return styles.statusRegistered
    case 'IN_CONSULTATION':
      return styles.statusInConsultation
    case 'COMPLETED':
      return styles.statusCompleted
    case 'REFERRED':
      return styles.statusReferred
    case 'CANCELLED':
      return styles.statusCancelled
    default:
      return ''
  }
}

export function OPDQueuePage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [doctorId, setDoctorId] = useState<number | ''>('')
  const [visitDate, setVisitDate] = useState(today)
  const [queue, setQueue] = useState<OPDVisitResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    doctorsApi
      .list({ status: 'ACTIVE', page: 0, size: 200 })
      .then((data) => setDoctors(data.content))
      .catch(() => setDoctors([]))
  }, [])

  useEffect(() => {
    if (!doctorId) {
      setQueue([])
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    opdApi
      .getQueue(Number(doctorId), visitDate || undefined)
      .then(setQueue)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load queue')
        setQueue([])
      })
      .finally(() => setLoading(false))
  }, [doctorId, visitDate])

  return (
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <span className={styles.label}>Doctor</span>
        <select
          value={doctorId || ''}
          onChange={(e) => setDoctorId(e.target.value ? Number(e.target.value) : '')}
          className={styles.select}
        >
          <option value="">Select doctor</option>
          {doctors.map((d) => (
            <option key={d.id} value={d.id}>
              {d.fullName} — {d.departmentName}
            </option>
          ))}
        </select>
        <span className={styles.label}>Date</span>
        <input
          type="date"
          value={visitDate}
          onChange={(e) => setVisitDate(e.target.value)}
          className={styles.input}
        />
      </div>

      {error && <div className={styles.error}>{error}</div>}
      {loading && <div className={styles.loading}>Loading queue…</div>}

      {!loading && doctorId && (
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>Consultation queue</h2>
          {queue.length === 0 ? (
            <p className={styles.empty}>No visits in queue for this doctor and date.</p>
          ) : (
            <div className={styles.queueList}>
              {queue.map((v) => (
                <div key={v.id} className={styles.queueItem}>
                  <span className={styles.tokenBadge}>{v.tokenNumber ?? '—'}</span>
                  <div className={styles.patientInfo}>
                    <span className={styles.patientName}>{v.patientName}</span>
                    <div className={styles.uhid}>{v.patientUhid}</div>
                    <div className={styles.visitNumber}>{v.visitNumber}</div>
                  </div>
                  <span className={`${styles.statusBadge} ${statusClass(v.visitStatus)}`}>
                    {v.visitStatus.replace(/_/g, ' ')}
                  </span>
                  <Link to={`/opd/visits/${v.id}`} className={styles.link}>
                    Open
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
