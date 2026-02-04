import { useState, useEffect, useRef } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getAdmissionView } from '../../services/ipdAdmissionService'
import type { ViewAdmissionResponse } from '../../types/ipdAdmission.types'
import { AdmissionSummary } from '../../components/ipd/AdmissionSummary'
import { PatientInfoCard } from '../../components/ipd/PatientInfoCard'
import { ClinicalInfoCard } from '../../components/ipd/ClinicalInfoCard'
import { WardBedCard } from '../../components/ipd/WardBedCard'
import { BillingSummaryCard } from '../../components/ipd/BillingSummaryCard'
import { AdmissionTimeline } from '../../components/ipd/AdmissionTimeline'
import styles from './ViewAdmission.module.css'

function Skeleton() {
  return (
    <div className={styles.skeleton}>
      <div className={styles.skeletonCard}>
        <div className={`${styles.skeletonLine} ${styles.skeletonLineShort}`} />
        <div className={styles.skeletonLine} />
        <div className={styles.skeletonLine} />
        <div className={styles.skeletonLine} />
      </div>
      <div className={styles.skeletonCard}>
        <div className={styles.skeletonLine} />
        <div className={styles.skeletonLine} />
        <div className={styles.skeletonLine} />
      </div>
    </div>
  )
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function ViewAdmission() {
  const { id: admissionIdParam } = useParams<{ id: string }>()
  const printRef = useRef<HTMLDivElement>(null)
  const [data, setData] = useState<ViewAdmissionResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const id = admissionIdParam ? Number(admissionIdParam) : null

  useEffect(() => {
    if (!id || Number.isNaN(id)) {
      setError('Invalid admission ID')
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    getAdmissionView(id)
      .then(setData)
      .catch((err) => {
        const status = err.response?.status
        const message = err.response?.data?.message || err.message
        setError(status === 404 ? 'Admission not found.' : message || 'Failed to load admission.')
      })
      .finally(() => setLoading(false))
  }, [id])

  const handlePrint = () => {
    if (!data) return
    const win = window.open('', '_blank')
    if (!win) return
    const admission = data.admission
    const patient = data.patient
    win.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>IPD Admission — ${admission.admissionNumber}</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; padding: 24px; max-width: 800px; margin: 0 auto; font-size: 14px; color: #1a1a2e; }
          h1 { font-size: 1.25rem; margin: 0 0 8px 0; border-bottom: 2px solid #0d9488; padding-bottom: 8px; }
          h2 { font-size: 1rem; margin: 20px 0 8px 0; color: #374151; }
          table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
          th, td { border: 1px solid #e5e7eb; padding: 8px 12px; text-align: left; }
          th { background: #f9fafb; font-weight: 600; width: 180px; }
          .meta { font-size: 12px; color: #6b7280; margin-top: 24px; }
          .signature { margin-top: 32px; }
          .signature-line { border-top: 1px solid #1a1a2e; width: 200px; margin-top: 40px; padding-top: 4px; font-size: 12px; }
        </style>
      </head>
      <body>
        <h1>Hospital — IPD Admission Details</h1>
        <p class="meta">Printed on ${formatDateTime(new Date().toISOString())}</p>
        <h2>Admission Summary</h2>
        <table>
          <tr><th>IPD Admission No</th><td>${admission.admissionNumber}</td></tr>
          <tr><th>Status</th><td>${admission.admissionStatus.replace(/_/g, ' ')}</td></tr>
          <tr><th>Admission Date & Time</th><td>${formatDateTime(admission.admissionDateTime)}</td></tr>
          <tr><th>Ward / Room / Bed</th><td>${admission.currentWardName ?? '—'} / ${admission.currentRoomNumber ?? '—'} / ${admission.currentBedNumber ?? '—'}</td></tr>
          <tr><th>Treating Doctor</th><td>${admission.primaryDoctorName} (${admission.primaryDoctorCode})</td></tr>
        </table>
        <h2>Patient Information</h2>
        <table>
          <tr><th>UHID</th><td>${patient.uhid}</td></tr>
          <tr><th>Patient Name</th><td>${patient.fullName}</td></tr>
          <tr><th>Age / Gender</th><td>${patient.age} yrs / ${patient.gender}</td></tr>
          <tr><th>Contact</th><td>${patient.phone ?? '—'}</td></tr>
          <tr><th>Address</th><td>${patient.address ?? ''} ${patient.city ?? ''} ${patient.district ?? ''} ${patient.state ?? ''}</td></tr>
        </table>
        <h2>Clinical Information</h2>
        <table>
          <tr><th>Diagnosis</th><td>${admission.diagnosis ?? '—'}</td></tr>
          <tr><th>Admission Notes</th><td>${admission.remarks ?? '—'}</td></tr>
          <tr><th>Admission Source</th><td>${admission.admissionType.replace(/_/g, ' ')}</td></tr>
        </table>
        <h2>Ward & Bed Details</h2>
        <table>
          <tr><th>Ward Name</th><td>${admission.currentWardName ?? '—'}</td></tr>
          <tr><th>Room Number</th><td>${admission.currentRoomNumber ?? '—'}</td></tr>
          <tr><th>Bed Number</th><td>${admission.currentBedNumber ?? '—'}</td></tr>
        </table>
        <div class="signature">
          <div class="signature-line">Authorized Signature</div>
          <div class="meta">Date & time: ${formatDateTime(new Date().toISOString())}</div>
        </div>
      </body>
      </html>
    `)
    win.document.close()
    win.focus()
    setTimeout(() => {
      win.print()
      win.close()
    }, 300)
  }

  const handleDownloadPdf = () => {
    handlePrint()
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <header className={styles.header}>
          <div className={styles.headerLeft}>
            <h1 className={styles.title}>IPD Admission Details</h1>
            <p className={styles.breadcrumb}>
              <Link to="/">Home</Link>
              <span>→</span>
              <Link to="/ipd">IPD</Link>
              <span>→</span>
              <Link to="/ipd/admissions">Admissions</Link>
              <span>→</span>
              View
            </p>
          </div>
        </header>
        <Skeleton />
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className={styles.page}>
        <header className={styles.header}>
          <div className={styles.headerLeft}>
            <h1 className={styles.title}>IPD Admission Details</h1>
            <p className={styles.breadcrumb}>
              <Link to="/">Home</Link>
              <span>→</span>
              <Link to="/ipd">IPD</Link>
              <span>→</span>
              <Link to="/ipd/admissions">Admissions</Link>
              <span>→</span>
              View
            </p>
          </div>
        </header>
        <div className={styles.error} role="alert">
          {error || 'Admission not found.'}
        </div>
        <Link to="/ipd/admissions" className={styles.actionBtn}>
          Back to List
        </Link>
      </div>
    )
  }

  const { admission, patient, timeline, billingSummary } = data

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1 className={styles.title}>IPD Admission Details</h1>
          <p className={styles.breadcrumb}>
            <Link to="/">Home</Link>
            <span>→</span>
            <Link to="/ipd">IPD</Link>
            <span>→</span>
            <Link to="/ipd/admissions">Admissions</Link>
            <span>→</span>
            View
          </p>
        </div>
        <div className={styles.actions}>
          <button type="button" onClick={handlePrint} className={`${styles.actionBtn} ${styles.actionBtnPrimary}`}>
            Print Admission
          </button>
          <button type="button" onClick={handleDownloadPdf} className={styles.actionBtn}>
            Download PDF
          </button>
          <Link to="/ipd/admissions" className={styles.actionBtn}>
            Back to List
          </Link>
        </div>
      </header>

      <div ref={printRef}>
        <AdmissionSummary admission={admission} />
        <PatientInfoCard patient={patient} />
        <ClinicalInfoCard admission={admission} />
        <WardBedCard admission={admission} />
        <BillingSummaryCard billingSummary={billingSummary} admission={admission} />
        <AdmissionTimeline events={timeline} />
      </div>
    </div>
  )
}
