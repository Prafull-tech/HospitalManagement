import { useState, useEffect } from 'react'
import { ipdApi } from '../api/ipd'
import { nursingApi } from '../api/nursing'
import type { VitalSignRequest, VitalSignResponse } from '../types/nursing'
import type { IPDAdmissionResponse } from '../types/ipd'
import type { NursingStaffResponse } from '../types/nursing'
import styles from './NursingVitalsPage.module.css'

export function NursingVitalsPage() {
  const [admissions, setAdmissions] = useState<IPDAdmissionResponse[]>([])
  const [staff, setStaff] = useState<NursingStaffResponse[]>([])
  const [admissionId, setAdmissionId] = useState<number | ''>('')
  const [vitals, setVitals] = useState<VitalSignResponse[]>([])
  const [form, setForm] = useState<VitalSignRequest>({
    ipdAdmissionId: 0,
    bloodPressureSystolic: undefined,
    bloodPressureDiastolic: undefined,
    pulse: undefined,
    temperature: undefined,
    spo2: undefined,
    respiration: undefined,
    recordedById: undefined,
    remarks: '',
  })
  const [loadingVitals, setLoadingVitals] = useState(false)
  const [loadingList, setLoadingList] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    ipdApi
      .search({ page: 0, size: 200 })
      .then((r) =>
        setAdmissions(
          r.content.filter((a) =>
            ['ADMITTED', 'TRANSFERRED', 'DISCHARGE_INITIATED'].includes(a.admissionStatus)
          )
        )
      )
      .catch(() => setAdmissions([]))
    nursingApi.listStaff(true).then(setStaff).catch(() => setStaff([]))
  }, [])

  useEffect(() => {
    if (!admissionId) {
      setVitals([])
      return
    }
    setLoadingList(true)
    nursingApi
      .getVitalsByAdmission(Number(admissionId))
      .then(setVitals)
      .catch(() => setVitals([]))
      .finally(() => setLoadingList(false))
  }, [admissionId])

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      ipdAdmissionId: admissionId ? Number(admissionId) : 0,
    }))
  }, [admissionId])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    const num = value === '' ? undefined : Number(value)
    setForm((prev) => ({
      ...prev,
      [name]: name === 'recordedById' ? num : name === 'remarks' ? value : num,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.ipdAdmissionId) {
      setError('Select an IPD admission.')
      return
    }
    setLoadingVitals(true)
    setError('')
    setSuccess('')
    try {
      await nursingApi.recordVitals(form)
      setSuccess('Vitals recorded.')
      setForm((prev) => ({
        ...prev,
        bloodPressureSystolic: undefined,
        bloodPressureDiastolic: undefined,
        pulse: undefined,
        temperature: undefined,
        spo2: undefined,
        respiration: undefined,
        remarks: '',
      }))
      nursingApi.getVitalsByAdmission(form.ipdAdmissionId).then(setVitals).catch(() => {})
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to record vitals.')
    } finally {
      setLoadingVitals(false)
    }
  }

  const selectedAdmission = admissions.find((a) => a.id === Number(admissionId))
  const chartData = vitals.slice(0, 20).reverse()

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Record vital signs</h2>
        {error && <div className={styles.error}>{error}</div>}
        {success && <div className={styles.success}>{success}</div>}
        <div className={styles.row}>
          <label>IPD admission</label>
          <select value={admissionId || ''} onChange={(e) => setAdmissionId(e.target.value ? Number(e.target.value) : '')} className={styles.select}>
            <option value="">Select admission</option>
            {admissions.map((a) => (
              <option key={a.id} value={a.id}>
                {a.admissionNumber} — {a.patientName}
              </option>
            ))}
          </select>
        </div>
        {admissionId && (
          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.grid}>
              <div className={styles.field}>
                <label>BP Systolic</label>
                <input name="bloodPressureSystolic" type="number" min={60} max={250} value={form.bloodPressureSystolic ?? ''} onChange={handleChange} placeholder="mmHg" className={styles.input} />
              </div>
              <div className={styles.field}>
                <label>BP Diastolic</label>
                <input name="bloodPressureDiastolic" type="number" min={40} max={150} value={form.bloodPressureDiastolic ?? ''} onChange={handleChange} placeholder="mmHg" className={styles.input} />
              </div>
              <div className={styles.field}>
                <label>Pulse</label>
                <input name="pulse" type="number" min={30} max={200} value={form.pulse ?? ''} onChange={handleChange} placeholder="/min" className={styles.input} />
              </div>
              <div className={styles.field}>
                <label>Temp (°C)</label>
                <input name="temperature" type="number" step="0.1" min={35} max={42} value={form.temperature ?? ''} onChange={handleChange} placeholder="°C" className={styles.input} />
              </div>
              <div className={styles.field}>
                <label>SpO2 (%)</label>
                <input name="spo2" type="number" min={70} max={100} value={form.spo2 ?? ''} onChange={handleChange} placeholder="%" className={styles.input} />
              </div>
              <div className={styles.field}>
                <label>Respiration</label>
                <input name="respiration" type="number" min={8} max={40} value={form.respiration ?? ''} onChange={handleChange} placeholder="/min" className={styles.input} />
              </div>
            </div>
            <div className={styles.row}>
              <label>Recorded by</label>
              <select name="recordedById" value={form.recordedById ?? ''} onChange={handleChange} className={styles.select}>
                <option value="">—</option>
                {staff.map((s) => (
                  <option key={s.id} value={s.id}>{s.fullName}</option>
                ))}
              </select>
            </div>
            <div className={styles.row}>
              <label>Remarks</label>
              <textarea name="remarks" value={form.remarks ?? ''} onChange={handleChange} placeholder="Optional" className={styles.textarea} rows={1} />
            </div>
            <button type="submit" className={styles.submitBtn} disabled={loadingVitals}>
              {loadingVitals ? 'Recording…' : 'Record vitals'}
            </button>
          </form>
        )}
      </div>

      {admissionId && (
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>Vitals history — {selectedAdmission?.admissionNumber}</h2>
          {loadingList && <div className={styles.loading}>Loading…</div>}
          {!loadingList && vitals.length === 0 && <p className={styles.empty}>No vitals recorded yet.</p>}
          {!loadingList && vitals.length > 0 && (
            <>
              <div className={styles.chartWrap}>
                <div className={styles.chartRow}>
                  <span className={styles.chartLabel}>Pulse</span>
                  <div className={styles.chartBarContainer}>
                    {chartData.map((v) => (
                      <div
                        key={v.id}
                        className={styles.chartBar}
                        style={{
                          height: v.pulse != null ? `${Math.min(100, (v.pulse / 120) * 100)}%` : '0',
                          minHeight: v.pulse != null ? '4px' : '0',
                        }}
                        title={`${v.recordedAt.slice(11, 16)}: ${v.pulse ?? '—'}`}
                      />
                    ))}
                  </div>
                </div>
                <div className={styles.chartRow}>
                  <span className={styles.chartLabel}>SpO2</span>
                  <div className={styles.chartBarContainer}>
                    {chartData.map((v) => (
                      <div
                        key={v.id}
                        className={styles.chartBarSpo2}
                        style={{
                          height: v.spo2 != null ? `${v.spo2}%` : '0',
                          minHeight: v.spo2 != null ? '4px' : '0',
                        }}
                        title={`${v.recordedAt.slice(11, 16)}: ${v.spo2 ?? '—'}%`}
                      />
                    ))}
                  </div>
                </div>
              </div>
              <div className={styles.tableWrap}>
                <table className={`table table-striped ${styles.table}`}>
                  <thead>
                    <tr>
                      <th>Time</th>
                      <th>BP</th>
                      <th>Pulse</th>
                      <th>Temp</th>
                      <th>SpO2</th>
                      <th>Resp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vitals.map((v) => (
                      <tr key={v.id}>
                        <td>{v.recordedAt.replace('T', ' ').slice(0, 16)}</td>
                        <td>{v.bloodPressureSystolic != null && v.bloodPressureDiastolic != null ? `${v.bloodPressureSystolic}/${v.bloodPressureDiastolic}` : '—'}</td>
                        <td>{v.pulse ?? '—'}</td>
                        <td>{v.temperature ?? '—'}</td>
                        <td>{v.spo2 != null ? `${v.spo2}%` : '—'}</td>
                        <td>{v.respiration ?? '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  )
}
