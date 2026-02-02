/**
 * Bed grid/table for General Ward. Data from hospital-beds API only.
 * Columns: Bed No, Status (badge), Patient Name, UHID, Actions (role-based).
 */

import { Link } from 'react-router-dom'
import type {
  HospitalBedItem,
  BedStatus,
  GeneralWardAction,
} from '../../types/bed.types'
import {
  BED_STATUS_LABELS,
  canPerformGeneralWardAction,
} from '../../types/bed.types'
import styles from './WardBedTable.module.css'

export interface WardBedTableProps {
  beds: HospitalBedItem[]
  userRoles: string[]
  onMarkStatus?: (bedId: number, status: BedStatus) => Promise<void>
  onRefetch?: () => void
}

function StatusBadge({ status }: { status: BedStatus }) {
  const label = BED_STATUS_LABELS[status] ?? status
  const variant = statusToVariant(status)
  return (
    <span className={`${styles.badge} ${styles[variant]}`} title={label}>
      {label}
    </span>
  )
}

function statusToVariant(status: BedStatus): string {
  switch (status) {
    case 'AVAILABLE':
      return 'badgeVacant'
    case 'OCCUPIED':
      return 'badgeOccupied'
    case 'RESERVED':
      return 'badgeReserved'
    case 'CLEANING':
      return 'badgeCleaning'
    case 'MAINTENANCE':
      return 'badgeMaintenance'
    case 'ISOLATION':
      return 'badgeIsolation'
    default:
      return 'badgeDefault'
  }
}

export function WardBedTable({
  beds,
  userRoles,
  onMarkStatus,
  onRefetch,
}: WardBedTableProps) {
  const canView = canPerformGeneralWardAction(userRoles, 'viewPatient')
  const canAddNote = canPerformGeneralWardAction(userRoles, 'addNursingNote')
  const canTransfer = canPerformGeneralWardAction(userRoles, 'transferPatient')
  const canMarkCleaningOrVacant = canPerformGeneralWardAction(
    userRoles,
    'markCleaningOrVacant'
  )

  const handleMarkStatus = async (bedId: number, status: BedStatus) => {
    if (!onMarkStatus) return
    await onMarkStatus(bedId, status)
    onRefetch?.()
  }

  if (beds.length === 0) {
    return (
      <div className={styles.empty} role="status">
        No beds in this ward.
      </div>
    )
  }

  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Bed No</th>
            <th>Status</th>
            <th>Patient Name</th>
            <th>UHID</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {beds.map((bed) => (
            <tr key={bed.bedId}>
              <td className={styles.cellNum}>{bed.bedNumber}</td>
              <td>
                <StatusBadge status={bed.bedStatus} />
              </td>
              <td className={styles.cellName}>
                {bed.bedStatus === 'OCCUPIED'
                  ? bed.patientName ?? '—'
                  : '—'}
              </td>
              <td className={styles.cellUhid}>
                {bed.bedStatus === 'OCCUPIED'
                  ? bed.patientUhid ?? bed.uhid ?? '—'
                  : '—'}
              </td>
              <td className={styles.actions}>
                {bed.bedStatus === 'OCCUPIED' && bed.admissionId != null && (
                  <>
                    {canView && (
                      <Link
                        to={`/ipd/admissions/${bed.admissionId}`}
                        className={styles.link}
                      >
                        View Patient
                      </Link>
                    )}
                    {canAddNote && (
                      <Link
                        to="/nursing/notes"
                        className={styles.link}
                        state={{ wardType: 'GENERAL', admissionId: bed.admissionId }}
                      >
                        Add Note
                      </Link>
                    )}
                    {canTransfer && (
                      <Link
                        to={`/ipd/admissions/${bed.admissionId}`}
                        className={styles.link}
                        state={{ tab: 'transfer' }}
                      >
                        Transfer
                      </Link>
                    )}
                  </>
                )}
                {canMarkCleaningOrVacant && onMarkStatus && (
                  <>
                    {bed.bedStatus !== 'CLEANING' &&
                      bed.bedStatus !== 'MAINTENANCE' &&
                      bed.bedStatus !== 'ISOLATION' && (
                        <button
                          type="button"
                          className={styles.btnSecondary}
                          onClick={() =>
                            handleMarkStatus(bed.bedId, 'CLEANING')
                          }
                        >
                          Mark Cleaning
                        </button>
                      )}
                    {(bed.bedStatus === 'CLEANING' ||
                      bed.bedStatus === 'AVAILABLE') && (
                      <button
                        type="button"
                        className={styles.btnSecondary}
                        onClick={() =>
                          handleMarkStatus(bed.bedId, 'AVAILABLE')
                        }
                      >
                        Mark Vacant
                      </button>
                    )}
                  </>
                )}
                {!(bed.bedStatus === 'OCCUPIED' && bed.admissionId != null && (canView || canAddNote || canTransfer)) &&
                  !(canMarkCleaningOrVacant && onMarkStatus && (bed.bedStatus !== 'CLEANING' && bed.bedStatus !== 'MAINTENANCE' && bed.bedStatus !== 'ISOLATION' || bed.bedStatus === 'CLEANING' || bed.bedStatus === 'AVAILABLE')) &&
                  '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
