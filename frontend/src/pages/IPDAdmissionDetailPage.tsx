import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ipdApi } from '../api/ipd'
import type {
  IPDAdmissionResponse,
  AdmissionStatus,
  IPDTransferRequest,
  BedAvailabilityResponse,
} from '../types/ipd'
import styles from './IPDAdmissionDetailPage.module.css'

const ACTIVE_STATUSES: AdmissionStatus[] = ['ADMITTED', 'TRANSFERRED', 'DISCHARGE_INITIATED']

function statusClass(s: AdmissionStatus): string {
  switch (s) {
    case 'ADMITTED':
      return styles.statusAdmitted
    case 'TRANSFERRED':
      return styles.statusTransferred
    case 'DISCHARGE_INITIATED':
      return styles.statusDischargeInitiated
    case 'DISCHARGED':
      return styles.statusDischarged
    case 'CANCELLED':
      return styles.statusCancelled
    default:
      return ''
  }
}

export function IPDAdmissionDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [admission, setAdmission] = useState<IPDAdmissionResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [transferForm, setTransferForm] = useState<IPDTransferRequest>({ bedId: 0, remarks: '' })
  const [availableBeds, setAvailableBeds] = useState<BedAvailabilityResponse[]>([])
  const [transferSubmitting, setTransferSubmitting] = useState(false)

  const [dischargeRemarks, setDischargeRemarks] = useState('')
  const [dischargeSubmitting, setDischargeSubmitting] = useState(false)

  const admissionId = id ? Number(id) : null

  useEffect(() => {
    if (!admissionId) return
    setLoading(true)
    setError('')
    ipdApi
      .getById(admissionId)
      .then(setAdmission)
      .catch((err) => setError(err.response?.data?.message || 'Failed to load admission'))
      .finally(() => setLoading(false))
  }, [admissionId])

  useEffect(() => {
    ipdApi.getBedAvailability().then((list) => setAvailableBeds(list.filter((b) => b.available))).catch(() => [])
  }, [])

  const isActive = admission && ACTIVE_STATUSES.includes(admission.admissionStatus)
  const canTransfer = isActive && admission.admissionStatus !== 'DISCHARGE_INITIATED'
  const canDischarge = isActive

  const handleTransfer = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!admissionId || !transferForm.bedId) return
    setTransferSubmitting(true)
    setSuccess('')
    setError('')
    try {
      const updated = await ipdApi.transfer(admissionId, transferForm)
      setAdmission(updated)
      setSuccess('Transfer completed.')
      setTransferForm({ bedId: 0, remarks: '' })
      ipdApi.getBedAvailability().then((list) => setAvailableBeds(list.filter((b) => b.available))).catch(() => [])
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to transfer.')
    } finally {
      setTransferSubmitting(false)
    }
  }

  const handleDischarge = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!admissionId) return
    setDischargeSubmitting(true)
    setSuccess('')
    setError('')
    try {
      const updated = await ipdApi.discharge(admissionId, { dischargeRemarks: dischargeRemarks.trim() || undefined })
      setAdmission(updated)
      if (updated.admissionStatus === 'DISCHARGED') {
        setSuccess('Patient discharged.')
      } else {
        setSuccess('Discharge initiated. Complete discharge when ready.')
      }
      setDischargeRemarks('')
    } catch (err: unknown) {
      const ax = err as { response?: { data?: { message?: string } } }
      setError(ax.response?.data?.message || 'Failed to discharge.')
    } finally {
      setDischargeSubmitting(false)
    }
  }

  const handlePrint = () => {
    if (!admission) return
    const win = window.open('', '_blank')
    if (!win) return
    win.document.write(`
      <!DOCTYPE html><html><head><title>IPD Admission ${admission.admissionNumber}</title>
      <style>body{font-family:sans-serif;padding:1.5rem;} table{border-collapse:collapse;width:100%;max-width:600px;} th,td{border:1px solid #ccc;padding:0.5rem 1rem;text-align:left;} th{background:#f5f5f5;width:180px;}</style>
      </head><body>
      <h1>IPD Admission Details</h1>
      <table>
        <tr><th>Admission #</th><td>${admission.admissionNumber}</td></tr>
        <tr><th>Status</th><td>${admission.admissionStatus}</td></tr>
        <tr><th>Patient</th><td>${admission.patientName} (${admission.patientUhid})</td></tr>
        <tr><th>Primary doctor</th><td>${admission.primaryDoctorName} (${admission.primaryDoctorCode})</td></tr>
        <tr><th>Admission type</th><td>${admission.admissionType}</td></tr>
        <tr><th>Admitted at</th><td>${admission.admissionDateTime.replace('T', ' ').slice(0, 16)}</td></tr>
        <tr><th>Ward / Bed</th><td>${admission.currentWardName && admission.currentBedNumber ? `${admission.currentWardName} — ${admission.currentBedNumber}` : '—'}</td></tr>
        ${admission.dischargeDateTime ? `<tr><th>Discharged at</th><td>${admission.dischargeDateTime.replace('T', ' ').slice(0, 16)}</td></tr>` : ''}
        ${admission.remarks ? `<tr><th>Remarks</th><td>${admission.remarks}</td></tr>` : ''}
        ${admission.dischargeRemarks ? `<tr><th>Discharge remarks</th><td>${admission.dischargeRemarks}</td></tr>` : ''}
      </table>
      </body></html>
    `)
    win.document.close()
    win.focus()
    setTimeout(() => { win.print(); win.close(); }, 250)
  }

  if (loading || !admission) {
    return (
      <div className={styles.page}>
        <Link to="/ipd/admissions" className={styles.backLink}>← IPD Admissions</Link>
        {loading && <div className={styles.loading}>Loading…</div>}
        {error && <div className={styles.error}>{error}</div>}
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <Link to="/ipd/admissions" className={styles.backLink}>← IPD Admissions</Link>
      {error && <div className={styles.error}>{error}</div>}
      {success && <div className={styles.success}>{success}</div>}

      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h2 className={styles.cardTitle}>Admission details</h2>
          <button type="button" onClick={handlePrint} className={styles.printBtn}>Print</button>
        </div>
        <div className={styles.grid}>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Admission #</span>
            <span className={styles.fieldValue}>{admission.admissionNumber}</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Status</span>
            <span className={`${styles.statusBadge} ${statusClass(admission.admissionStatus)}`}>
              {admission.admissionStatus.replace(/_/g, ' ')}
            </span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Patient</span>
            <span className={styles.fieldValue}>{admission.patientName} ({admission.patientUhid})</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Primary doctor</span>
            <span className={styles.fieldValue}>{admission.primaryDoctorName} ({admission.primaryDoctorCode})</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Admission type</span>
            <span className={styles.fieldValue}>{admission.admissionType.replace(/_/g, ' ')}</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Admitted at</span>
            <span className={styles.fieldValue}>{admission.admissionDateTime.replace('T', ' ').slice(0, 16)}</span>
          </div>
          <div className={styles.field}>
            <span className={styles.fieldLabel}>Current ward / bed</span>
            <span className={styles.fieldValue}>
              {admission.currentWardName && admission.currentBedNumber
                ? `${admission.currentWardName} — ${admission.currentBedNumber}`
                : '—'}
            </span>
          </div>
          {admission.dischargeDateTime && (
            <div className={styles.field}>
              <span className={styles.fieldLabel}>Discharged at</span>
              <span className={styles.fieldValue}>{admission.dischargeDateTime.replace('T', ' ').slice(0, 16)}</span>
            </div>
          )}
          {admission.remarks && (
            <div className={styles.fieldFull}>
              <span className={styles.fieldLabel}>Remarks</span>
              <span className={styles.fieldValue}>{admission.remarks}</span>
            </div>
          )}
          {admission.dischargeRemarks && (
            <div className={styles.fieldFull}>
              <span className={styles.fieldLabel}>Discharge remarks</span>
              <span className={styles.fieldValue}>{admission.dischargeRemarks}</span>
            </div>
          )}
        </div>
      </div>

      {canTransfer && (
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>Transfer to another bed</h2>
          <form onSubmit={handleTransfer}>
            <div className={styles.formRow}>
              <label>Target bed</label>
              <select
                className={styles.select}
                value={transferForm.bedId || ''}
                onChange={(e) => setTransferForm((p) => ({ ...p, bedId: Number(e.target.value) || 0 }))}
              >
                <option value="">Select bed</option>
                {availableBeds.map((b) => (
                  <option key={b.bedId} value={b.bedId}>
                    {b.wardName} — {b.bedNumber}
                  </option>
                ))}
              </select>
            </div>
            <div className={styles.formRow}>
              <label>Remarks</label>
              <textarea
                className={styles.textarea}
                value={transferForm.remarks ?? ''}
                onChange={(e) => setTransferForm((p) => ({ ...p, remarks: e.target.value }))}
                placeholder="Optional"
                rows={2}
              />
            </div>
            <button
              type="submit"
              className={styles.btnPrimary}
              disabled={!transferForm.bedId || transferSubmitting}
            >
              {transferSubmitting ? 'Transferring…' : 'Transfer'}
            </button>
          </form>
        </div>
      )}

      {canDischarge && (
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>
            {admission.admissionStatus === 'DISCHARGE_INITIATED' ? 'Complete discharge' : 'Initiate / Complete discharge'}
          </h2>
          <form onSubmit={handleDischarge}>
            <div className={styles.formRow}>
              <label>Discharge remarks (optional)</label>
              <textarea
                className={styles.textarea}
                value={dischargeRemarks}
                onChange={(e) => setDischargeRemarks(e.target.value)}
                placeholder="Optional remarks"
                rows={2}
              />
            </div>
            <button type="submit" className={styles.btnPrimary} disabled={dischargeSubmitting}>
              {dischargeSubmitting
                ? 'Processing…'
                : admission.admissionStatus === 'DISCHARGE_INITIATED'
                  ? 'Complete discharge'
                  : 'Initiate discharge'}
            </button>
          </form>
        </div>
      )}
    </div>
  )
}
