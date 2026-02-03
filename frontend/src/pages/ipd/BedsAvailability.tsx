/**
 * IPD Beds – Availability & Management.
 * Summary cards, filters, table, role-based actions, details modal, status confirmation.
 */

import { useState, useEffect, useCallback } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { ipdBedService } from '../../services/ipdBedService'
import { useAuth } from '../../contexts/AuthContext'
import type { BedAvailabilityItem, BedFiltersParams, BedStatus, SummaryCounts, WardCensusRow } from '../../types/ipdBed.types'
import { BED_STATUS_LABELS } from '../../types/ipdBed.types'
import { computeWardCensus as computeWardCensusUtil } from '../../utils/bedCensus'
import { BedFilters } from '../../components/ipd/BedFilters'
import { BedTable } from '../../components/ipd/BedTable'
import { BedDetailsModal } from '../../components/ipd/BedDetailsModal'

function defaultFiltersFromSearchParams(searchParams: URLSearchParams): BedFiltersParams {
  return {
    wardType: (searchParams.get('wardType') as BedFiltersParams['wardType']) ?? undefined,
    wardName: searchParams.get('wardName') ?? undefined,
    bedStatus: (searchParams.get('bedStatus') as BedFiltersParams['bedStatus']) ?? undefined,
    search: searchParams.get('search') ?? undefined,
    floor: searchParams.get('floor') ?? undefined,
  }
}

function filtersToSearchParams(f: BedFiltersParams): URLSearchParams {
  const p = new URLSearchParams()
  if (f.wardType) p.set('wardType', f.wardType)
  if (f.wardName) p.set('wardName', f.wardName)
  if (f.bedStatus) p.set('bedStatus', f.bedStatus)
  if (f.search) p.set('search', f.search)
  if (f.floor) p.set('floor', f.floor)
  return p
}

function computeSummary(beds: BedAvailabilityItem[]): SummaryCounts {
  const s: SummaryCounts = {
    total: beds.length,
    available: 0,
    occupied: 0,
    reserved: 0,
    cleaning: 0,
    maintenance: 0,
    isolation: 0,
  }
  beds.forEach((b) => {
    if (b.bedStatus === 'AVAILABLE') s.available++
    else if (b.bedStatus === 'OCCUPIED') s.occupied++
    else if (b.bedStatus === 'RESERVED') s.reserved++
    else if (b.bedStatus === 'CLEANING') s.cleaning++
    else if (b.bedStatus === 'MAINTENANCE') s.maintenance++
    else if (b.bedStatus === 'ISOLATION') s.isolation++
  })
  return s
}

function computeWardCensus(beds: BedAvailabilityItem[]): WardCensusRow[] {
  return computeWardCensusUtil(beds)
}

function filterBedsClient(beds: BedAvailabilityItem[], filters: BedFiltersParams): BedAvailabilityItem[] {
  let list = beds
  if (filters.wardName) {
    list = list.filter((b) => b.wardName === filters.wardName)
  }
  if (filters.floor) {
    // Optional: if backend adds floor to DTO, filter here
  }
  return list
}

export function BedsAvailability() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const { user } = useAuth()
  const userRoles = user?.roles ?? ['ADMIN']

  const [filters, setFilters] = useState<BedFiltersParams>(() => defaultFiltersFromSearchParams(searchParams))
  const [allBeds, setAllBeds] = useState<BedAvailabilityItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [detailsBed, setDetailsBed] = useState<BedAvailabilityItem | null>(null)
  const [statusModal, setStatusModal] = useState<{ bed: BedAvailabilityItem } | null>(null)
  const [maintenanceModal, setMaintenanceModal] = useState<BedAvailabilityItem | null>(null)
  const [updating, setUpdating] = useState(false)

  const fetchBeds = useCallback(() => {
    setLoading(true)
    setError('')
    ipdBedService
      .getAvailability(filters)
      .then(setAllBeds)
      .catch((err) => {
        const msg = err.response?.data?.message
        setError(
          msg === 'An unexpected error occurred'
            ? 'Unable to load beds. Please ensure the server is running and try again.'
            : msg || 'Failed to load bed availability.'
        )
        setAllBeds([])
      })
      .finally(() => setLoading(false))
  }, [filters.wardId, filters.wardType, filters.bedStatus, filters.search])

  useEffect(() => {
    fetchBeds()
  }, [fetchBeds])

  useEffect(() => {
    setSearchParams(filtersToSearchParams(filters), { replace: true })
  }, [filters])

  const filteredBeds = filterBedsClient(allBeds, filters)
  const summary = computeSummary(filteredBeds)
  const wardCensus = computeWardCensus(filteredBeds)
  const wardNames = [...new Set(allBeds.map((b) => b.wardName))].sort()

  const handleRefresh = () => fetchBeds()

  const handleChangeStatus = (bed: BedAvailabilityItem, newStatus: BedStatus) => {
    setUpdating(true)
    ipdBedService
      .updateStatus(bed.bedId, newStatus)
      .then((updated) => {
        setAllBeds((prev) => prev.map((b) => (b.bedId === updated.bedId ? updated : b)))
        setStatusModal(null)
        if (detailsBed?.bedId === bed.bedId) setDetailsBed(updated)
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to update status')
      })
      .finally(() => setUpdating(false))
  }

  const handleMarkMaintenance = (bed: BedAvailabilityItem) => {
    setUpdating(true)
    ipdBedService
      .updateStatus(bed.bedId, 'MAINTENANCE')
      .then((updated) => {
        setAllBeds((prev) => prev.map((b) => (b.bedId === updated.bedId ? updated : b)))
        setMaintenanceModal(null)
        if (detailsBed?.bedId === bed.bedId) setDetailsBed(updated)
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to update status')
      })
      .finally(() => setUpdating(false))
  }

  const handleAllocate = (bed: BedAvailabilityItem) => {
    navigate(`/ipd/admit?bedId=${bed.bedId}`)
  }

  const handleTransfer = (bed: BedAvailabilityItem) => {
    if (bed.admissionId) navigate(`/ipd/admissions/${bed.admissionId}`)
  }

  const exportCsv = () => {
    const headers = ['Bed No', 'Ward Name', 'Ward Type', 'Room No', 'Status', 'Patient Name', 'UHID', 'Admission No', 'Last Updated']
    const rows = filteredBeds.map((b) =>
      [b.bedNumber, b.wardName, b.wardType ?? '', b.roomNumber ?? '', b.bedStatus, b.patientName ?? '', b.patientUhid ?? '', b.admissionNumber ?? '', b.updatedAt ?? ''].join(',')
    )
    const csv = [headers.join(','), ...rows].join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `ipd-beds-${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/ipd">IPD</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Beds
          </li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">IPD Beds Availability</h1>
        <div className="d-flex gap-2">
          <Link to="/ipd/hospital-beds" className="btn btn-outline-primary btn-sm">
            Hospital-wise view
          </Link>
          <button type="button" className="btn btn-outline-primary btn-sm" onClick={handleRefresh} disabled={loading}>
            Refresh
          </button>
          <button type="button" className="btn btn-outline-secondary btn-sm" onClick={exportCsv} disabled={loading || filteredBeds.length === 0}>
            Export CSV
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger d-flex align-items-center justify-content-between flex-wrap gap-2" role="alert">
          <span>{loading ? 'Retrying…' : error}</span>
          <div className="d-flex align-items-center gap-2">
            <button type="button" className="btn btn-sm btn-outline-danger" onClick={fetchBeds} disabled={loading}>
              {loading ? 'Retrying…' : 'Retry'}
            </button>
            <button type="button" className="btn-close" onClick={() => setError('')} aria-label="Close" />
          </div>
        </div>
      )}

      {/* Summary Cards */}
      <div className="row g-2 g-md-3">
        <div className="col-6 col-md">
          <div className="card border shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-muted small mb-0">Total Beds</p>
              <p className="fw-bold mb-0">{summary.total}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md">
          <div className="card border border-success shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-success small mb-0">Vacant</p>
              <p className="fw-bold text-success mb-0">{summary.available}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md">
          <div className="card border border-danger shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-danger small mb-0">Occupied</p>
              <p className="fw-bold text-danger mb-0">{summary.occupied}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md">
          <div className="card border border-warning shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-warning small mb-0">Reserved</p>
              <p className="fw-bold text-warning mb-0">{summary.reserved}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md">
          <div className="card border border-info shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-info small mb-0">Cleaning</p>
              <p className="fw-bold text-info mb-0">{summary.cleaning}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md">
          <div className="card border border-secondary shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-secondary small mb-0">Maintenance</p>
              <p className="fw-bold text-secondary mb-0">{summary.maintenance}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md">
          <div className="card border border-dark shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-dark small mb-0">Isolation</p>
              <p className="fw-bold text-dark mb-0">{summary.isolation}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Daily Bed Census by Ward Type (NABH: daily bed census) */}
      {wardCensus.length > 0 && (
        <div className="card border shadow-sm">
          <div className="card-header bg-light py-2 d-flex align-items-center justify-content-between">
            <h6 className="mb-0">Daily Bed Census by Ward Type</h6>
          </div>
          <div className="table-responsive">
            <table className="table table-striped mb-0">
              <thead className="table-light">
                <tr>
                  <th>Ward Type</th>
                  <th className="text-end">Total Beds</th>
                  <th className="text-end">Occupied</th>
                  <th className="text-end">Vacant</th>
                  <th className="text-end">Reserved</th>
                  <th className="text-end">Under Cleaning</th>
                  <th className="text-end">Maintenance</th>
                  <th className="text-end">Isolation</th>
                </tr>
              </thead>
              <tbody>
                {wardCensus.map((row) => (
                  <tr key={row.wardType}>
                    <td>{row.wardTypeLabel}</td>
                    <td className="text-end">{row.total}</td>
                    <td className="text-end">{row.occupied}</td>
                    <td className="text-end">{row.vacant}</td>
                    <td className="text-end">{row.reserved}</td>
                    <td className="text-end">{row.cleaning}</td>
                    <td className="text-end">{row.maintenance}</td>
                    <td className="text-end">{row.isolation}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Filters */}
      <BedFilters filters={filters} onChange={setFilters} wardNames={wardNames} />

      {/* Beds Table */}
      <BedTable
        beds={filteredBeds}
        userRoles={userRoles}
        loading={loading}
        onViewDetails={setDetailsBed}
        onChangeStatus={(bed) => setStatusModal({ bed })}
        onMarkMaintenance={setMaintenanceModal}
        onAllocate={handleAllocate}
        onTransfer={handleTransfer}
      />

      {/* Bed Details Modal */}
      <BedDetailsModal bed={detailsBed} onClose={() => setDetailsBed(null)} />

      {/* Change Status Confirmation Modal */}
      {statusModal && (
        <div className="modal fade show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} aria-modal="true" role="dialog">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Change Bed Status</h5>
                <button type="button" className="btn-close" onClick={() => setStatusModal(null)} aria-label="Close" />
              </div>
              <div className="modal-body">
                <p>Bed <strong>{statusModal.bed.bedNumber}</strong> – {statusModal.bed.wardName}. Select new status:</p>
                <div className="d-flex flex-wrap gap-2">
                  {(['AVAILABLE', 'OCCUPIED', 'RESERVED', 'CLEANING', 'MAINTENANCE', 'ISOLATION'] as const).map((status) => (
                    <button
                      key={status}
                      type="button"
                      className="btn btn-outline-primary"
                      disabled={updating}
                      onClick={() => handleChangeStatus(statusModal.bed, status)}
                    >
                      {BED_STATUS_LABELS[status]}
                    </button>
                  ))}
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setStatusModal(null)}>Cancel</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Mark Maintenance Confirmation */}
      {maintenanceModal && (
        <div className="modal fade show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} aria-modal="true" role="dialog">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Mark Bed Under Maintenance</h5>
                <button type="button" className="btn-close" onClick={() => setMaintenanceModal(null)} aria-label="Close" />
              </div>
              <div className="modal-body">
                <p>Set bed <strong>{maintenanceModal.bedNumber}</strong> ({maintenanceModal.wardName}) to Maintenance? The bed will be unavailable until status is changed.</p>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setMaintenanceModal(null)}>Cancel</button>
                <button type="button" className="btn btn-warning" disabled={updating} onClick={() => handleMarkMaintenance(maintenanceModal)}>
                  {updating ? 'Updating…' : 'Mark Maintenance'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
