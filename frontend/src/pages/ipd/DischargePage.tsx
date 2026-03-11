import { useState, useEffect, useCallback } from 'react'
import { Link, useParams } from 'react-router-dom'
import { dischargeApi } from '../../api/discharge'
import type { DischargeStatus, DischargeSummaryRequest } from '../../types/discharge'
import styles from './DischargePage.module.css'

const POLL_INTERVAL_MS = 5000

function formatDateTime(iso: string | undefined): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString(undefined, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatCurrency(n: number | undefined): string {
  if (n == null) return '₹0'
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(n)
}

function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/\n/g, '<br>')
}

function buildPrintDocument(status: DischargeStatus, formatDateTime: (iso?: string) => string, formatCurrency: (n?: number) => string): string {
  const clearances = [
    { label: 'Doctor', done: status.doctorClearance },
    { label: 'Nursing', done: status.nursingClearance },
    { label: 'Pharmacy', done: status.pharmacyClearance },
    { label: 'Lab', done: status.labClearance },
    { label: 'Billing', done: status.billingClearance },
    { label: 'Insurance', done: status.insuranceClearance },
    { label: 'Housekeeping', done: status.housekeepingClearance },
    { label: 'Linen', done: status.linenClearance },
    { label: 'Dietary', done: status.dietaryClearance },
  ]
  const summaryItems = [
    ['Diagnosis', status.diagnosisSummary ?? '—'],
    ['Treatment', status.treatmentSummary ?? '—'],
    ['Procedures', status.procedures ?? '—'],
    ['Advice', status.advice ?? '—'],
    ['Follow-up', status.followUp ?? '—'],
    ['Medicines on discharge', status.medicinesOnDischarge ?? '—'],
  ]
  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Discharge Summary - ${status.admissionNumber}</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: Arial, sans-serif; font-size: 12pt; line-height: 1.5; color: #000; background: #fff; margin: 0; padding: 20px; }
    h1 { font-size: 18pt; margin: 0 0 20px 0; border-bottom: 2px solid #333; padding-bottom: 8px; }
    h2 { font-size: 14pt; margin: 20px 0 10px 0; color: #000; }
    .section { margin-bottom: 20px; page-break-inside: avoid; }
    .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
    p { margin: 0 0 8px 0; }
    ul { margin: 0; padding-left: 20px; }
    li { margin-bottom: 4px; }
    .checklist { margin: 0; }
    .check-item { margin-bottom: 6px; }
    .check-done { color: #059669; }
    .check-pending { color: #666; }
    .summary-item { margin-bottom: 10px; }
    .summary-item strong { display: inline-block; min-width: 160px; }
    @media print { body { padding: 0; } }
  </style>
</head>
<body>
  <h1>IPD Discharge Summary — ${status.admissionNumber}</h1>

  <div class="section">
    <h2>Patient & Admission</h2>
    <div class="grid2">
      <div>
        <p><strong>Patient:</strong> ${escapeHtml(status.patientName)}</p>
        <p><strong>UHID:</strong> ${escapeHtml(status.uhid)}</p>
      </div>
      <div>
        <p><strong>Bed / Ward:</strong> ${escapeHtml(status.bedNumber ?? '—')} / ${escapeHtml(status.wardName ?? '—')}</p>
        <p><strong>Admitted:</strong> ${formatDateTime(status.admittedDate)}</p>
        ${status.dischargeDate ? `<p><strong>Discharge:</strong> ${formatDateTime(status.dischargeDate)}</p>` : ''}
      </div>
    </div>
  </div>

  <div class="section">
    <h2>Billing Status</h2>
    <p>${status.billingPaid ? '✔ Paid' : '❌ Pending ' + formatCurrency(status.billingPendingAmount ?? status.billingTotal ?? 0)}</p>
    <p>Total: ${formatCurrency(status.billingTotal)} — Paid: ${formatCurrency((status.billingTotal ?? 0) - (status.billingPendingAmount ?? 0))}</p>
  </div>

  <div class="section">
    <h2>Clearance Checklist</h2>
    <div class="checklist">
      ${clearances.map((c) => `<div class="check-item ${c.done ? 'check-done' : 'check-pending'}">${c.done ? '✔' : '○'} ${c.label}</div>`).join('')}
    </div>
  </div>

  <div class="section">
    <h2>Discharge Summary</h2>
    ${status.diagnosisSummary || status.treatmentSummary
      ? summaryItems.map(([label, val]) => `<div class="summary-item"><strong>${label}:</strong> ${escapeHtml(val || '—')}</div>`).join('')
      : '<p>Doctor to fill discharge summary.</p>'}
  </div>

  ${status.pendingPharmacy.length > 0 || status.pendingLab.length > 0 ? `
  <div class="section">
    <h2>Pending Items</h2>
    ${status.pendingPharmacy.length > 0 ? `<p><strong>Pharmacy:</strong></p><ul>${status.pendingPharmacy.map((p) => `<li>${escapeHtml(p.description)} — ${escapeHtml(p.status)}</li>`).join('')}</ul>` : ''}
    ${status.pendingLab.length > 0 ? `<p><strong>Lab:</strong></p><ul>${status.pendingLab.map((p) => `<li>${escapeHtml(p.description)} — ${escapeHtml(p.status)}</li>`).join('')}</ul>` : ''}
  </div>
  ` : ''}

  <p style="margin-top: 30px; font-size: 10pt; color: #666;">Printed on ${new Date().toLocaleString()}</p>
</body>
</html>`
}

function openPrintWindow(status: DischargeStatus) {
  const html = buildPrintDocument(status, formatDateTime, formatCurrency)
  const win = window.open('', '_blank')
  if (!win) {
    alert('Please allow pop-ups to print the discharge summary.')
    return
  }
  win.document.write(html)
  win.document.close()
  win.focus()
  setTimeout(() => {
    win.print()
    win.onafterprint = () => win.close()
  }, 250)
}

export function DischargePage() {
  const { id } = useParams<{ id: string }>()
  const ipdId = id ? Number(id) : null

  const [status, setStatus] = useState<DischargeStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState<string | null>(null)
  const [summaryForm, setSummaryForm] = useState<DischargeSummaryRequest>({})
  const [showSummaryForm, setShowSummaryForm] = useState(false)

  const openSummaryForm = () => {
    if (status) {
      setSummaryForm({
        diagnosisSummary: status.diagnosisSummary,
        treatmentSummary: status.treatmentSummary,
        procedures: status.procedures,
        advice: status.advice,
        followUp: status.followUp,
        medicinesOnDischarge: status.medicinesOnDischarge,
      })
    }
    setShowSummaryForm(true)
  }

  const fetchStatus = useCallback(() => {
    if (!ipdId || Number.isNaN(ipdId)) return Promise.resolve()
    return dischargeApi.getStatus(ipdId).then(setStatus).catch(() => {})
  }, [ipdId])

  useEffect(() => {
    if (!ipdId || Number.isNaN(ipdId)) {
      setError('Invalid admission ID')
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    dischargeApi
      .getStatus(ipdId)
      .then(setStatus)
      .catch((err) => {
        const msg = err.response?.data?.message || err.message
        setError(err.response?.status === 404 ? 'Admission not found.' : msg || 'Failed to load discharge status.')
      })
      .finally(() => setLoading(false))
  }, [ipdId])

  useEffect(() => {
    if (!status || status.admissionStatus === 'DISCHARGED') return
    const t = setInterval(fetchStatus, POLL_INTERVAL_MS)
    return () => clearInterval(t)
  }, [status?.admissionStatus, fetchStatus])

  const runAction = async (
    key: string,
    fn: () => Promise<DischargeStatus>
  ) => {
    setActionLoading(key)
    setError('')
    try {
      const updated = await fn()
      setStatus(updated)
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string }; status?: number }; message?: string }
      const msg = e.response?.data?.message || e.message || 'Action failed'
      setError(msg)
    } finally {
      setActionLoading(null)
    }
  }

  const handleDoctorClearance = () =>
    runAction('doctor', () => dischargeApi.recordDoctorClearance(ipdId!))
  const handleNursingClearance = () =>
    runAction('nursing', () => dischargeApi.recordNursingClearance(ipdId!))
  const handlePharmacyClearance = () =>
    runAction('pharmacy', () => dischargeApi.recordPharmacyClearance(ipdId!))
  const handleLabClearance = () =>
    runAction('lab', () => dischargeApi.recordLabClearance(ipdId!))
  const handleFinalizeBill = () =>
    runAction('billing', () => dischargeApi.finalizeBill(ipdId!))
  const handleInsuranceOverride = () =>
    runAction('insurance', () => dischargeApi.recordInsuranceClearance(ipdId!, true))
  const handleHousekeepingClearance = () =>
    runAction('housekeeping', () => dischargeApi.recordHousekeepingClearance(ipdId!))
  const handleLinenClearance = () =>
    runAction('linen', () => dischargeApi.recordLinenClearance(ipdId!))
  const handleDietaryClearance = () =>
    runAction('dietary', () => dischargeApi.recordDietaryClearance(ipdId!))
  const handleSaveSummary = () =>
    runAction('summary', () => dischargeApi.saveSummary(ipdId!, summaryForm))
  const handleFinalizeDischarge = () =>
    runAction('finalize', () => dischargeApi.finalizeDischarge(ipdId!))

  const isDischarged = status?.admissionStatus === 'DISCHARGED'

  if (loading) {
    return (
      <div className={styles.page}>
        <header className={styles.header}>
          <h1 className={styles.title}>IPD Discharge</h1>
          <p className={styles.breadcrumb}>
            <Link to="/ipd">IPD</Link>
            <span>→</span>
            <Link to="/ipd/admissions">Admissions</Link>
            <span>→</span>
            Discharge
          </p>
        </header>
        <div className={styles.skeleton}>Loading discharge status…</div>
      </div>
    )
  }

  if (error && !status) {
    return (
      <div className={styles.page}>
        <header className={styles.header}>
          <h1 className={styles.title}>IPD Discharge</h1>
        </header>
        <div className={styles.error} role="alert">
          {error}
        </div>
        <Link to="/ipd/admissions" className={styles.actionBtn}>
          Back to Admissions
        </Link>
      </div>
    )
  }

  if (!status) return null

  const clearances = [
    { key: 'doctor', label: 'Doctor', done: status.doctorClearance, action: handleDoctorClearance },
    { key: 'nursing', label: 'Nursing', done: status.nursingClearance, action: handleNursingClearance },
    { key: 'pharmacy', label: 'Pharmacy', done: status.pharmacyClearance, action: handlePharmacyClearance },
    { key: 'lab', label: 'Lab', done: status.labClearance, action: handleLabClearance },
    { key: 'billing', label: 'Billing', done: status.billingClearance, action: handleFinalizeBill },
    { key: 'insurance', label: 'Insurance (TPA)', done: status.insuranceClearance, action: handleInsuranceOverride },
    { key: 'housekeeping', label: 'Housekeeping', done: status.housekeepingClearance, action: handleHousekeepingClearance },
    { key: 'linen', label: 'Linen Returned', done: status.linenClearance, action: handleLinenClearance },
    { key: 'dietary', label: 'Dietary / Meals Closed', done: status.dietaryClearance, action: handleDietaryClearance },
  ]

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1 className={styles.title}>IPD Discharge — {status.admissionNumber}</h1>
          <p className={styles.breadcrumb}>
            <Link to="/ipd">IPD</Link>
            <span>→</span>
            <Link to="/ipd/admissions">Admissions</Link>
            <span>→</span>
            <Link to={`/ipd/admissions/${ipdId}`}>View</Link>
            <span>→</span>
            Discharge
          </p>
        </div>
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.actionBtn}
            onClick={() => openPrintWindow(status)}
            aria-label="Print discharge summary"
          >
            Print
          </button>
          <Link to={`/ipd/admissions/${ipdId}`} className={styles.actionBtn}>
            Back to Admission
          </Link>
        </div>
      </header>

      {error && (
        <div className={styles.error} role="alert">
          {error}
        </div>
      )}

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Patient & Admission</h2>
        <div className={styles.grid2}>
          <div>
            <p><strong>Patient</strong> {status.patientName}</p>
            <p><strong>UHID</strong> {status.uhid}</p>
          </div>
          <div>
            <p><strong>Bed / Ward</strong> {status.bedNumber ?? '—'} / {status.wardName ?? '—'}</p>
            <p><strong>Admitted</strong> {formatDateTime(status.admittedDate)}</p>
          </div>
        </div>
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Pending Items</h2>
        <div className={styles.grid2}>
          <div>
            <h3>Pharmacy ({status.pendingPharmacyCount})</h3>
            {status.pendingPharmacy.length === 0 ? (
              <p className={styles.muted}>No pending medication orders</p>
            ) : (
              <ul className={styles.list}>
                {status.pendingPharmacy.map((p) => (
                  <li key={p.id}>{p.description} — {p.status}</li>
                ))}
              </ul>
            )}
          </div>
          <div>
            <h3>Lab ({status.pendingLabCount})</h3>
            {status.pendingLab.length === 0 ? (
              <p className={styles.muted}>No pending tests</p>
            ) : (
              <ul className={styles.list}>
                {status.pendingLab.map((p) => (
                  <li key={p.id}>{p.description} — {p.status}</li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Billing Status</h2>
        <div className={styles.billingStatus}>
          {status.billingPaid ? (
            <p className={styles.billingPaid}>✔ Paid</p>
          ) : (
            <p className={styles.billingPending}>
              ❌ Pending {formatCurrency(status.billingPendingAmount ?? status.billingTotal ?? 0)}
            </p>
          )}
        </div>
        <p className={styles.muted}>
          Total: {formatCurrency(status.billingTotal)} — Paid: {formatCurrency(
            (status.billingTotal ?? 0) - (status.billingPendingAmount ?? 0)
          )}
        </p>
        {!status.billingPaid && (
          <p className={styles.billingAlert}>
            Pending bill must be cleared before discharge.{' '}
            <Link to={`/billing/account/${ipdId}`}>Go to Billing</Link>
          </p>
        )}
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Clearance Checklist</h2>
        <div className={styles.checklist}>
          {clearances.map((c) => (
            <div key={c.key} className={styles.checkItem}>
              <span className={c.done ? styles.checkDone : styles.checkPending}>
                {c.done ? '✔' : '○'} {c.label}
              </span>
              {!c.done && c.action && !isDischarged && (
                <button
                  type="button"
                  className={styles.btnSmall}
                  onClick={c.action}
                  disabled={!!actionLoading}
                >
                  {actionLoading === c.key ? '…' : c.key === 'billing' ? 'Finalize Bill' : 'Approve'}
                </button>
              )}
            </div>
          ))}
        </div>
      </section>

      <section className={styles.card}>
        <h2 className={styles.cardTitle}>Discharge Summary</h2>
        {!showSummaryForm ? (
          <div>
            {status.diagnosisSummary || status.treatmentSummary ? (
              <div className={styles.summaryPreview}>
                <p><strong>Diagnosis</strong> {status.diagnosisSummary ?? '—'}</p>
                <p><strong>Treatment</strong> {status.treatmentSummary ?? '—'}</p>
                <p><strong>Procedures</strong> {status.procedures ?? '—'}</p>
                <p><strong>Advice</strong> {status.advice ?? '—'}</p>
                <p><strong>Follow-up</strong> {status.followUp ?? '—'}</p>
                <p><strong>Medicines on discharge</strong> {status.medicinesOnDischarge ?? '—'}</p>
              </div>
            ) : (
              <p className={styles.muted}>Doctor to fill discharge summary.</p>
            )}
            {!isDischarged && (
              <button
                type="button"
                className={styles.actionBtn}
                onClick={openSummaryForm}
              >
                {status.diagnosisSummary ? 'Edit Summary' : 'Fill Summary'}
              </button>
            )}
          </div>
        ) : (
          <form
            className={styles.summaryForm}
            onSubmit={(e) => {
              e.preventDefault()
              handleSaveSummary()
              setShowSummaryForm(false)
            }}
          >
            <label>Diagnosis</label>
            <textarea
              value={summaryForm.diagnosisSummary ?? ''}
              onChange={(e) => setSummaryForm((s) => ({ ...s, diagnosisSummary: e.target.value }))}
              rows={2}
            />
            <label>Treatment Given</label>
            <textarea
              value={summaryForm.treatmentSummary ?? ''}
              onChange={(e) => setSummaryForm((s) => ({ ...s, treatmentSummary: e.target.value }))}
              rows={2}
            />
            <label>Procedures</label>
            <input
              type="text"
              value={summaryForm.procedures ?? ''}
              onChange={(e) => setSummaryForm((s) => ({ ...s, procedures: e.target.value }))}
            />
            <label>Advice</label>
            <textarea
              value={summaryForm.advice ?? ''}
              onChange={(e) => setSummaryForm((s) => ({ ...s, advice: e.target.value }))}
              rows={2}
            />
            <label>Follow-up</label>
            <input
              type="text"
              value={summaryForm.followUp ?? ''}
              onChange={(e) => setSummaryForm((s) => ({ ...s, followUp: e.target.value }))}
            />
            <label>Medicines on Discharge</label>
            <textarea
              value={summaryForm.medicinesOnDischarge ?? ''}
              onChange={(e) => setSummaryForm((s) => ({ ...s, medicinesOnDischarge: e.target.value }))}
              rows={2}
            />
            <div className={styles.formActions}>
              <button type="submit" className={styles.actionBtnPrimary} disabled={!!actionLoading}>
                {actionLoading === 'summary' ? 'Saving…' : 'Save Summary'}
              </button>
              <button type="button" className={styles.actionBtn} onClick={() => setShowSummaryForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        )}
      </section>

      {!isDischarged && (
        <section className={styles.card}>
          <h2 className={styles.cardTitle}>Actions</h2>
          <div className={styles.actions}>
            <button
              type="button"
              className={styles.actionBtnPrimary}
              onClick={handleFinalizeDischarge}
              disabled={!status.canFinalizeDischarge || !!actionLoading}
              title={!status.canFinalizeDischarge ? 'Complete all clearances first' : ''}
            >
              {actionLoading === 'finalize' ? 'Processing…' : 'Complete Discharge'}
            </button>
          </div>
          {!status.canFinalizeDischarge && (
            <p className={styles.muted}>
              {!status.billingPaid
                ? `Pending bill ₹${status.billingPendingAmount ?? 0} must be cleared before discharge.`
                : 'All clearances must be complete before final discharge.'}
            </p>
          )}
        </section>
      )}

      {isDischarged && (
        <section className={styles.card}>
          <p className={styles.success}>Patient discharged successfully.</p>
          <p>Discharge date: {formatDateTime(status.dischargeDate)}</p>
        </section>
      )}
    </div>
  )
}
