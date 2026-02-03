/**
 * Hospital-wise Bed Availability – table with exact format:
 * Ward Type | Total Beds | Occupied | Vacant | Reserved | Under Cleaning
 */

import type { BedAvailabilityItem } from '../../types/bedAvailability.types'
import { WARD_TYPE_LABELS } from '../../types/bedAvailability.types'

export interface BedAvailabilityTableProps {
  items: BedAvailabilityItem[]
  canEdit: boolean
  canDelete: boolean
  onEdit: (item: BedAvailabilityItem) => void
  onDelete: (item: BedAvailabilityItem) => void
  loading?: boolean
}

export function BedAvailabilityTable({
  items,
  canEdit,
  canDelete,
  onEdit,
  onDelete,
  loading,
}: BedAvailabilityTableProps) {
  if (loading) {
    return (
      <div className="card border shadow-sm">
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-striped align-middle mb-0">
              <thead>
                <tr>
                  <th>Ward Type</th>
                  <th className="text-end">Total Beds</th>
                  <th className="text-end">Occupied</th>
                  <th className="text-end">Vacant</th>
                  <th className="text-end">Reserved</th>
                  <th className="text-end">Under Cleaning</th>
                  <th aria-label="Actions" />
                </tr>
              </thead>
              <tbody>
                {[1, 2, 3].map((i) => (
                  <tr key={i}>
                    <td colSpan={7}>
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

  if (items.length === 0) {
    return (
      <div className="card border shadow-sm">
        <div className="card-body text-center text-muted py-5">
          No bed availability records. Add one using the &quot;Add Record&quot; button.
        </div>
      </div>
    )
  }

  return (
    <div className="card border shadow-sm">
      <div className="table-responsive">
        <table className="table table-striped align-middle mb-0">
          <thead className="table-light">
            <tr>
              <th>Ward Type</th>
              <th className="text-end">Total Beds</th>
              <th className="text-end">Occupied</th>
              <th className="text-end">Vacant</th>
              <th className="text-end">Reserved</th>
              <th className="text-end">Under Cleaning</th>
              {(canEdit || canDelete) && <th className="text-end" style={{ width: 120 }}>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {items.map((row) => (
              <tr key={row.id}>
                <td>{WARD_TYPE_LABELS[row.wardType as keyof typeof WARD_TYPE_LABELS] ?? row.wardType}</td>
                <td className="text-end">{row.totalBeds}</td>
                <td className="text-end">{row.occupied}</td>
                <td className="text-end">{row.vacant}</td>
                <td className="text-end">{row.reserved}</td>
                <td className="text-end">{row.underCleaning}</td>
                {(canEdit || canDelete) && (
                  <td className="text-end">
                    {canEdit && (
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-primary me-1"
                        onClick={() => onEdit(row)}
                        aria-label={`Edit ${WARD_TYPE_LABELS[row.wardType as keyof typeof WARD_TYPE_LABELS] ?? row.wardType}`}
                      >
                        Edit
                      </button>
                    )}
                    {canDelete && (
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => onDelete(row)}
                        aria-label={`Delete ${WARD_TYPE_LABELS[row.wardType as keyof typeof WARD_TYPE_LABELS] ?? row.wardType}`}
                      >
                        Delete
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
