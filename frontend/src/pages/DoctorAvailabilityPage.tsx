import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import type { DoctorResponse, DoctorAvailabilityResponse, DoctorAvailabilityRequest } from '../types/doctor'
import styles from './DoctorAvailabilityPage.module.css'

const DAY_NAMES: Record<number, string> = {
  1: 'Monday',
  2: 'Tuesday',
  3: 'Wednesday',
  4: 'Thursday',
  5: 'Friday',
  6: 'Saturday',
  7: 'Sunday',
}

export function DoctorAvailabilityPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [doctor, setDoctor] = useState<DoctorResponse | null>(null)
  const [slots, setSlots] = useState<DoctorAvailabilityResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState<DoctorAvailabilityRequest>({
    dayOfWeek: 1,
    startTime: '09:00',
    endTime: '17:00',
    onCall: false,
  })

  useEffect(() => {
    if (!id) return
    const doctorId = Number(id)
    doctorsApi
      .getById(doctorId)
      .then((d) => {
        setDoctor(d)
        return doctorsApi.getAvailability(doctorId)
      })
      .then(setSlots)
      .catch(() => setError('Failed to load doctor or availability'))
      .finally(() => setLoading(false))
  }, [id])

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!id) return
    setError('')
    setSaving(true)
    try {
      const created = await doctorsApi.addAvailability(Number(id), form)
      setSlots((prev) => [...prev.filter((s) => s.dayOfWeek !== form.dayOfWeek), created].sort((a, b) => a.dayOfWeek - b.dayOfWeek))
      setForm({ dayOfWeek: 1, startTime: '09:00', endTime: '17:00', onCall: false })
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to add availability')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className={styles.loading}>Loading…</div>
  if (!doctor) return <div className={styles.error}>Doctor not found</div>

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h2 className={styles.doctorName}>
          {doctor.fullName} ({doctor.code})
        </h2>
        <p className={styles.dept}>{doctor.departmentName}</p>
        <button type="button" onClick={() => navigate('/doctors')} className={styles.backBtn}>
          Back to Doctors
        </button>
      </div>

      {error && <div className={styles.error}>{error}</div>}

      <div className={styles.card}>
        <h3 className={styles.cardTitle}>Add OPD availability slot</h3>
        <form onSubmit={handleAdd} className={styles.form}>
          <div className={styles.row}>
            <label>
              Day
              <select
                value={form.dayOfWeek}
                onChange={(e) => setForm((p) => ({ ...p, dayOfWeek: Number(e.target.value) }))}
                className={styles.select}
              >
                {[1, 2, 3, 4, 5, 6, 7].map((d) => (
                  <option key={d} value={d}>
                    {DAY_NAMES[d]}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Start time
              <input
                type="time"
                value={form.startTime}
                onChange={(e) => setForm((p) => ({ ...p, startTime: e.target.value }))}
                className={styles.input}
              />
            </label>
            <label>
              End time
              <input
                type="time"
                value={form.endTime}
                onChange={(e) => setForm((p) => ({ ...p, endTime: e.target.value }))}
                className={styles.input}
              />
            </label>
            <label className={styles.checkWrap}>
              <input
                type="checkbox"
                checked={form.onCall}
                onChange={(e) => setForm((p) => ({ ...p, onCall: e.target.checked }))}
              />
              <span>On-call</span>
            </label>
          </div>
          <button type="submit" disabled={saving} className={styles.submit}>
            {saving ? 'Adding…' : 'Add slot'}
          </button>
        </form>
      </div>

      <div className={styles.card}>
        <h3 className={styles.cardTitle}>Current availability</h3>
        {slots.length === 0 ? (
          <p className={styles.empty}>No slots configured. Add one above.</p>
        ) : (
          <ul className={styles.list}>
            {slots.map((s) => (
              <li key={s.id} className={styles.slot}>
                <span className={styles.day}>{DAY_NAMES[s.dayOfWeek]}</span>
                <span className={styles.time}>
                  {s.startTime} – {s.endTime}
                </span>
                {s.onCall && <span className={styles.onCall}>On-call</span>}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
