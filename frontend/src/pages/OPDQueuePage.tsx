/**
 * OPD Queue – all visits listed below with optional filters (doctor, date, status, patient, visit number).
 * UI aligned with /ipd/hospital-beds: breadcrumb, cards, Bootstrap table, form controls.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { doctorsApi } from '../api/doctors'
import { opdApi } from '../api/opd'
import type { OPDVisitResponse, VisitStatus } from '../types/opd'
import type { DoctorResponse } from '../types/doctor'
import styles from './OPDQueuePage.module.css'

const PAGE_SIZE = 500

const STATUS_OPTIONS: { value: VisitStatus | ''; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'REGISTERED', label: 'Registered' },
  { value: 'IN_CONSULTATION', label: 'In consultation' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'REFERRED', label: 'Referred' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

function statusBadgeClass(s: VisitStatus): string {
  switch (s) {
    case 'REGISTERED':
      return `${styles.statusBadge} ${styles.statusRegistered}`
    case 'IN_CONSULTATION':
      return `${styles.statusBadge} ${styles.statusInConsultation}`
    case 'COMPLETED':
      return `${styles.statusBadge} ${styles.statusCompleted}`
    case 'REFERRED':
      return `${styles.statusBadge} ${styles.statusReferred}`
    case 'CANCELLED':
      return `${styles.statusBadge} ${styles.statusCancelled}`
    default:
      return styles.statusBadge
  }
}

function formatVisitDate(value?: string) {
  if (!value) return '—'
  return new Date(`${value}T00:00:00`).toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

export function OPDQueuePage() {
  const [doctors, setDoctors] = useState<DoctorResponse[]>([])
  const [visits, setVisits] = useState<OPDVisitResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [filterDoctorId, setFilterDoctorId] = useState<number | ''>('')
  const [filterDate, setFilterDate] = useState('')
  const [filterStatus, setFilterStatus] = useState<VisitStatus | ''>('')
  const [filterPatientName, setFilterPatientName] = useState('')
  const [filterVisitNumber, setFilterVisitNumber] = useState('')

  const hasFilters =
    !!filterDoctorId ||
    !!filterDate ||
    !!filterStatus ||
    !!filterPatientName.trim() ||
    !!filterVisitNumber.trim()

  const loadVisits = (params: {
    doctorId?: number
    visitDate?: string
    status?: VisitStatus
    patientName?: string
    visitNumber?: string
  }) => {
    setLoading(true)
    setError(null)
    opdApi
      .search({
        page: 0,
        size: PAGE_SIZE,
        ...(params.doctorId && { doctorId: params.doctorId }),
        ...(params.visitDate && { visitDate: params.visitDate }),
        ...(params.status && { status: params.status }),
        ...(params.patientName?.trim() && { patientName: params.patientName.trim() }),
        ...(params.visitNumber?.trim() && { visitNumber: params.visitNumber.trim() }),
      })
      .then((data) => setVisits(data.content))
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load visits')
        setVisits([])
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    doctorsApi
      .list({ status: 'ACTIVE', page: 0, size: 200 })
      .then((data) => setDoctors(data.content))
      .catch(() => setDoctors([]))
  }, [])

  useEffect(() => {
    loadVisits({})
  }, [])

  const handleApplyFilters = (e: React.FormEvent) => {
    e.preventDefault()
    loadVisits({
      doctorId: filterDoctorId || undefined,
      visitDate: filterDate || undefined,
      status: filterStatus || undefined,
      patientName: filterPatientName || undefined,
      visitNumber: filterVisitNumber || undefined,
    })
  }

  const handleClearFilters = () => {
    setFilterDoctorId('')
    setFilterDate('')
    setFilterStatus('')
    setFilterPatientName('')
    setFilterVisitNumber('')
    loadVisits({})
  }

  const registeredCount = visits.filter((visit) => visit.visitStatus === 'REGISTERED').length
  const inConsultationCount = visits.filter((visit) => visit.visitStatus === 'IN_CONSULTATION').length
  const completedCount = visits.filter((visit) => visit.visitStatus === 'COMPLETED').length
  const filteredLabel = hasFilters ? `Filtered results (${visits.length})` : `All visits (${visits.length})`

  return (
    <div className={styles.page}>
      <nav aria-label="Breadcrumb" className={styles.breadcrumbWrap}>
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/opd">OPD</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Queue
          </li>
        </ol>
      </nav>

      <section className={styles.hero}>
        <div className={styles.heroCopy}>
          <span className={styles.heroEyebrow}>Outpatient Flow</span>
          <h1 className={styles.heroTitle}>OPD Queue</h1>
          <p className={styles.heroText}>
            Monitor the live visit stream, isolate bottlenecks fast, and jump directly into consultation from a cleaner queue view.
          </p>
          <div className={styles.heroActions}>
            <Link to="/opd/register" className="btn btn-light btn-sm">
              Register visit
            </Link>
            <button
              type="button"
              className="btn btn-outline-light btn-sm"
              onClick={() => loadVisits({
                doctorId: filterDoctorId || undefined,
                visitDate: filterDate || undefined,
                status: filterStatus || undefined,
                patientName: filterPatientName || undefined,
                visitNumber: filterVisitNumber || undefined,
              })}
              disabled={loading}
            >
              {loading ? 'Loading…' : 'Refresh queue'}
            </button>
          </div>
        </div>

        <div className={styles.heroMeta}>
          <div className={styles.heroPill}>
            <span className={styles.heroPillLabel}>Total visible visits</span>
            <strong className={styles.heroPillValue}>{visits.length}</strong>
          </div>
          <div className={styles.heroPill}>
            <span className={styles.heroPillLabel}>Doctors loaded</span>
            <strong className={styles.heroPillValue}>{doctors.length}</strong>
          </div>
          <div className={styles.heroPillMuted}>
            <span className={styles.heroPillLabel}>Queue mode</span>
            <strong className={styles.heroPillValue}>{hasFilters ? 'Filtered' : 'Live board'}</strong>
          </div>
        </div>
      </section>

      <section className={styles.statsGrid}>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>Registered</span>
          <strong className={styles.statValue}>{registeredCount}</strong>
          <span className={styles.statMeta}>Waiting to enter consultation</span>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>In Consultation</span>
          <strong className={styles.statValue}>{inConsultationCount}</strong>
          <span className={styles.statMeta}>Currently active doctor sessions</span>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>Completed</span>
          <strong className={styles.statValue}>{completedCount}</strong>
          <span className={styles.statMeta}>Visits already closed</span>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statLabel}>Filters</span>
          <strong className={styles.statValue}>{hasFilters ? 'On' : 'Off'}</strong>
          <span className={styles.statMeta}>{hasFilters ? 'Focused queue view enabled' : 'Showing the full OPD board'}</span>
        </article>
      </section>

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <div>
            <h2 className={styles.panelTitle}>Queue filters</h2>
            <p className={styles.panelText}>Narrow the queue by clinician, date, patient, status, or a specific visit number.</p>
          </div>
          {hasFilters ? <span className={styles.filterIndicator}>Custom filter view active</span> : null}
        </div>
        <form onSubmit={handleApplyFilters} className={styles.filterGrid}>
          <div className={styles.field}>
            <label htmlFor="opd-filter-doctor" className={styles.fieldLabel}>
                Doctor
            </label>
            <select
              id="opd-filter-doctor"
              className="form-select"
              value={filterDoctorId || ''}
              onChange={(e) => setFilterDoctorId(e.target.value ? Number(e.target.value) : '')}
            >
              <option value="">Any doctor</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.fullName} — {d.departmentName}
                </option>
              ))}
            </select>
          </div>
          <div className={styles.field}>
            <label htmlFor="opd-filter-date" className={styles.fieldLabel}>
                Visit date
            </label>
            <input
              id="opd-filter-date"
              type="date"
              className="form-control"
              value={filterDate}
              onChange={(e) => setFilterDate(e.target.value)}
            />
          </div>
          <div className={styles.field}>
            <label htmlFor="opd-filter-status" className={styles.fieldLabel}>
                Status
            </label>
            <select
              id="opd-filter-status"
              className="form-select"
              value={filterStatus}
              onChange={(e) => setFilterStatus((e.target.value || '') as VisitStatus | '')}
            >
              {STATUS_OPTIONS.map((o) => (
                <option key={o.value || 'any'} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>
          <div className={styles.field}>
            <label htmlFor="opd-filter-name" className={styles.fieldLabel}>
                Patient name (partial)
            </label>
            <input
              id="opd-filter-name"
              type="text"
              className="form-control"
              placeholder="Search patient name"
              value={filterPatientName}
              onChange={(e) => setFilterPatientName(e.target.value)}
            />
          </div>
          <div className={styles.field}>
            <label htmlFor="opd-filter-visitno" className={styles.fieldLabel}>
                Visit number
            </label>
            <input
              id="opd-filter-visitno"
              type="text"
              className="form-control"
              placeholder="e.g. OPD-2025-00001"
              value={filterVisitNumber}
              onChange={(e) => setFilterVisitNumber(e.target.value)}
            />
          </div>
          <div className={styles.actions}>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Loading…' : 'Apply filters'}
            </button>
            <button type="button" className="btn btn-outline-secondary" onClick={handleClearFilters}>
              Clear
            </button>
          </div>
        </form>
      </section>

      {error && (
        <div className={styles.errorBanner} role="alert">
          <span>{error}</span>
          <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => setError(null)}>
            Dismiss
          </button>
        </div>
      )}

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <div>
            <h2 className={styles.panelTitle}>{filteredLabel}</h2>
            <p className={styles.panelText}>Open any visit directly from the board and keep the queue moving without leaving the page.</p>
          </div>
          <button
            type="button"
            className="btn btn-outline-primary btn-sm"
            onClick={() => loadVisits({
              doctorId: filterDoctorId || undefined,
              visitDate: filterDate || undefined,
              status: filterStatus || undefined,
              patientName: filterPatientName || undefined,
              visitNumber: filterVisitNumber || undefined,
            })}
            disabled={loading}
          >
            {loading ? 'Loading…' : 'Refresh'}
          </button>
        </div>
        {loading ? (
          <div className={styles.loadingState}>
            <div className={styles.loadingLine} />
            <div className={styles.loadingLine} />
            <div className={styles.loadingLine} />
          </div>
        ) : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Token</th>
                  <th>Visit No</th>
                  <th>Patient</th>
                  <th>UHID</th>
                  <th>Visit Date</th>
                  <th>Doctor</th>
                  <th>Status</th>
                  <th className="text-end">Action</th>
                </tr>
              </thead>
              <tbody>
                {visits.length === 0 ? (
                  <tr>
                    <td colSpan={8} className={styles.emptyRow}>
                      {hasFilters
                        ? 'No visits match the current filters. Try clearing filters.'
                        : 'No OPD visits yet.'}
                    </td>
                  </tr>
                ) : (
                  visits.map((v) => (
                    <tr key={v.id} className={styles.tableRow}>
                      <td>
                        <span className={styles.tokenBadge}>{v.tokenNumber ?? '—'}</span>
                      </td>
                      <td>
                        <div className={styles.primaryCell}>{v.visitNumber}</div>
                      </td>
                      <td>
                        <div className={styles.primaryCell}>{v.patientName}</div>
                      </td>
                      <td>
                        <span className={styles.secondaryCell}>{v.patientUhid}</span>
                      </td>
                      <td>{formatVisitDate(v.visitDate)}</td>
                      <td>
                        <div className={styles.primaryCell}>{v.doctorName ?? '—'}</div>
                        <span className={styles.secondaryCell}>{v.departmentName ?? '—'}</span>
                      </td>
                      <td>
                        <span className={statusBadgeClass(v.visitStatus)}>
                          {v.visitStatus.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td className={styles.actionCell}>
                        <Link to={`/opd/visits/${v.id}`} className="btn btn-sm btn-outline-primary">
                          Open
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
