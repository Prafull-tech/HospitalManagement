/**
 * IPD Beds – bed details drawer/modal (bed, ward, room, patient, admission, status history).
 */

import type { BedAvailabilityItem } from '../../types/ipdBed.types'
import { BED_STATUS_LABELS } from '../../types/ipdBed.types'

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
    return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
  } catch {
    return iso
  }
}

export interface BedDetailsModalProps {
  bed: BedAvailabilityItem | null
  onClose: () => void
}

export function BedDetailsModal({ bed, onClose }: BedDetailsModalProps) {
  if (!bed) return null

  return (
    <div className="modal fade show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} aria-modal="true" role="dialog">
      <div className="modal-dialog modal-dialog-scrollable modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Bed Details – {bed.bedNumber}</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <div className="modal-body">
            <div className="row mb-3">
              <div className="col-12">
                <h6 className="text-muted border-bottom pb-1">Bed & Ward</h6>
              </div>
              <div className="col-md-6">
                <p className="mb-1"><strong>Bed No</strong> {bed.bedNumber}</p>
                <p className="mb-1"><strong>Ward</strong> {bed.wardName} ({bed.wardCode})</p>
                <p className="mb-1"><strong>Ward Type</strong> {bed.wardType ?? '—'}</p>
              </div>
              <div className="col-md-6">
                <p className="mb-1"><strong>Room No</strong> {bed.roomNumber ?? '—'}</p>
                <p className="mb-1">
                  <strong>Status</strong>{' '}
                  <span className={`badge ${STATUS_CLASS[bed.bedStatus] ?? 'bg-secondary'}`}>
                    {BED_STATUS_LABELS[bed.bedStatus] ?? bed.bedStatus}
                  </span>
                </p>
                <p className="mb-1"><strong>Last Updated</strong> {formatDate(bed.updatedAt)}</p>
              </div>
            </div>

            {bed.patientName && (
              <div className="row mb-3">
                <div className="col-12">
                  <h6 className="text-muted border-bottom pb-1">Current Patient (if occupied)</h6>
                </div>
                <div className="col-md-6">
                  <p className="mb-1"><strong>Patient Name</strong> {bed.patientName}</p>
                  <p className="mb-1"><strong>UHID</strong> {bed.patientUhid ?? '—'}</p>
                </div>
                <div className="col-md-6">
                  <p className="mb-1"><strong>Admission No</strong> {bed.admissionNumber ?? '—'}</p>
                </div>
              </div>
            )}

            <div className="row">
              <div className="col-12">
                <h6 className="text-muted border-bottom pb-1">Status history</h6>
                <p className="small text-muted mb-0">Status history is read-only. Current status: {BED_STATUS_LABELS[bed.bedStatus] ?? bed.bedStatus}. Last updated: {formatDate(bed.updatedAt)}.</p>
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
