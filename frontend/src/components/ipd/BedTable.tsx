/**
 * IPD Beds – table with status badges and role-based actions.
 */

import { Link } from 'react-router-dom'
import type { BedAvailabilityItem } from '../../types/ipdBed.types'
import { canPerformBedAction, BED_STATUS_LABELS } from '../../types/ipdBed.types'

const STATUS_CLASS: Record<string, string> = {
  AVAILABLE: 'bg-success',
  OCCUPIED: 'bg-danger',
  RESERVED: 'bg-warning text-dark',
  CLEANING: 'bg-info',
  MAINTENANCE: 'bg-secondary',
  ISOLATION: 'bg-dark',
}

function formatDate(iso?: string): string {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' })
  } catch {
    return iso
  }
}

export interface BedTableProps {
  beds: BedAvailabilityItem[]
  userRoles: string[]
  onViewDetails: (bed: BedAvailabilityItem) => void
  onChangeStatus: (bed: BedAvailabilityItem) => void
  onMarkMaintenance: (bed: BedAvailabilityItem) => void
  onAllocate: (bed: BedAvailabilityItem) => void
  onTransfer: (bed: BedAvailabilityItem) => void
  loading?: boolean
}

export function BedTable({
  beds,
  userRoles,
  onViewDetails,
  onChangeStatus,
  onMarkMaintenance,
  onAllocate,
  onTransfer,
  loading,
}: BedTableProps) {
  const can = (action: Parameters<typeof canPerformBedAction>[1]) => canPerformBedAction(userRoles, action)

  if (loading) {
    return (
      <div className="card border shadow-sm">
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead>
                <tr>
                  <th>Bed No</th>
                  <th>Ward Name</th>
                  <th>Ward Type</th>
                  <th>Room No</th>
                  <th>Status</th>
                  <th>Patient Name</th>
                  <th>UHID</th>
                  <th>Admission No</th>
                  <th>Last Updated</th>
                  <th aria-label="Actions" />
                </tr>
              </thead>
              <tbody>
                {[1, 2, 3, 4, 5].map((i) => (
                  <tr key={i}>
                    <td colSpan={10}>
                      <div className="placeholder-glow">
                        <span className="placeholder col-12" />
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    )
  }

  if (beds.length === 0) {
    return (
      <div className="card border shadow-sm">
        <div className="card-body text-center py-5 text-muted">
          <p className="mb-0">No beds found. Adjust filters or ensure wards are configured.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="card border shadow-sm">
      <div className="table-responsive">
        <table className="table table-hover align-middle mb-0">
          <thead className="table-light">
            <tr>
              <th>Bed No</th>
              <th>Ward Name</th>
              <th>Ward Type</th>
              <th>Room No</th>
              <th>Status</th>
              <th>Patient Name</th>
              <th>UHID</th>
              <th>Admission No</th>
              <th>Last Updated</th>
              <th aria-label="Actions" />
            </tr>
          </thead>
          <tbody>
            {beds.map((bed) => (
              <tr key={bed.bedId}>
                <td className="fw-medium">{bed.bedNumber}</td>
                <td>{bed.wardName}</td>
                <td>{bed.wardType ?? '—'}</td>
                <td>{bed.roomNumber ?? '—'}</td>
                <td>
                  <span className={`badge ${STATUS_CLASS[bed.bedStatus] ?? 'bg-secondary'}`}>
                    {BED_STATUS_LABELS[bed.bedStatus] ?? bed.bedStatus}
                  </span>
                </td>
                <td>{bed.patientName ?? '—'}</td>
                <td>{bed.patientUhid ?? '—'}</td>
                <td>
                  {bed.admissionNumber ? (
                    <Link to={`/ipd/admissions/${bed.admissionId}`} className="text-decoration-none">
                      {bed.admissionNumber}
                    </Link>
                  ) : (
                    '—'
                  )}
                </td>
                <td className="small text-muted">{formatDate(bed.updatedAt)}</td>
                <td>
                  <div className="btn-group btn-group-sm">
                    {can('viewDetails') && (
                      <button
                        type="button"
                        className="btn btn-outline-primary"
                        onClick={() => onViewDetails(bed)}
                        title="View details"
                      >
                        View
                      </button>
                    )}
                    {can('allocateBed') && bed.available && (
                      <button
                        type="button"
                        className="btn btn-outline-success"
                        onClick={() => onAllocate(bed)}
                        title="Allocate bed"
                      >
                        Allocate
                      </button>
                    )}
                    {can('changeStatus') && (
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => onChangeStatus(bed)}
                        title="Change status"
                      >
                        Status
                      </button>
                    )}
                    {can('markMaintenance') && (
                      <button
                        type="button"
                        className="btn btn-outline-warning"
                        onClick={() => onMarkMaintenance(bed)}
                        title="Mark maintenance"
                      >
                        Maintenance
                      </button>
                    )}
                    {can('transferPatient') && !bed.available && bed.admissionId && (
                      <button
                        type="button"
                        className="btn btn-outline-info"
                        onClick={() => onTransfer(bed)}
                        title="Transfer patient"
                      >
                        Transfer
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
