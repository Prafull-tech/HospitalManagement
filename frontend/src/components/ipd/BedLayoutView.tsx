/**
 * IPD Bed Management – Movie-style bed layout (Ward → Room → Bed grid).
 * Replaces table-based bed list with visual seat map.
 */

import { useMemo, useState } from 'react'
import type { BedAvailabilityItem, WardType } from '../../types/ipdBed.types'
import { BedSeatCard } from './BedSeatCard'
import { BedLegend } from './BedLegend'
import { WardFilter } from './WardFilter'
import { BedDetailsModal } from './BedDetailsModal'
import styles from './BedLayoutView.module.css'

export interface GroupedBeds {
  wardName: string
  wardType?: WardType
  rooms: {
    roomNo: string
    beds: BedAvailabilityItem[]
    total: number
    occupied: number
    vacant: number
  }[]
}

function groupBedsByWardAndRoom(beds: BedAvailabilityItem[]): GroupedBeds[] {
  const byWard = new Map<string, Map<string, BedAvailabilityItem[]>>()

  beds.forEach((bed) => {
    const wardKey = bed.wardName
    const roomKey = bed.roomNumber ?? '—'

    if (!byWard.has(wardKey)) {
      byWard.set(wardKey, new Map())
    }
    const roomMap = byWard.get(wardKey)!
    if (!roomMap.has(roomKey)) {
      roomMap.set(roomKey, [])
    }
    roomMap.get(roomKey)!.push(bed)
  })

  return Array.from(byWard.entries()).map(([wardName, roomMap]) => {
    const firstBed = Array.from(roomMap.values()).flat()[0]
    const rooms = Array.from(roomMap.entries())
      .map(([roomNo, roomBeds]) => {
        const occupied = roomBeds.filter((b) => b.bedStatus === 'OCCUPIED').length
        const vacant = roomBeds.filter((b) => b.bedStatus === 'AVAILABLE').length
        const sorted = [...roomBeds].sort((a, b) => {
          const numA = parseInt(a.bedNumber.replace(/\D/g, ''), 10) || 0
          const numB = parseInt(b.bedNumber.replace(/\D/g, ''), 10) || 0
          if (numA !== numB) return numA - numB
          return a.bedNumber.localeCompare(b.bedNumber)
        })
        return {
          roomNo,
          beds: sorted,
          total: roomBeds.length,
          occupied,
          vacant,
        }
      })
      .sort((a, b) => a.roomNo.localeCompare(b.roomNo))

    return {
      wardName,
      wardType: firstBed?.wardType,
      rooms,
    }
  })
}

export interface BedLayoutViewProps {
  beds: BedAvailabilityItem[]
  loading?: boolean
  wardTypeFilter?: WardType
  onWardTypeFilterChange: (wardType: WardType | undefined) => void
  onAllocate: (bed: BedAvailabilityItem) => void
  onViewDetails: (bed: BedAvailabilityItem) => void
  onTransfer: (bed: BedAvailabilityItem) => void
  onDischarge: (bed: BedAvailabilityItem) => void
  onChangeStatus?: (bed: BedAvailabilityItem) => void
  onMarkMaintenance?: (bed: BedAvailabilityItem) => void
}

export function BedLayoutView({
  beds,
  loading,
  wardTypeFilter,
  onWardTypeFilterChange,
  onAllocate,
  onViewDetails,
  onTransfer,
  onDischarge,
}: BedLayoutViewProps) {
  const [filteredBeds, grouped] = useMemo(() => {
    let list = beds
    if (wardTypeFilter) {
      list = list.filter((b) => (b.wardType ?? '').toUpperCase() === wardTypeFilter)
    }
    const groupedData = groupBedsByWardAndRoom(list)
    return [list, groupedData]
  }, [beds, wardTypeFilter])

  const [detailsBed, setDetailsBed] = useState<BedAvailabilityItem | null>(null)

  const handleBedClick = (bed: BedAvailabilityItem) => {
    if (bed.bedStatus === 'AVAILABLE') {
      onAllocate(bed)
    } else {
      setDetailsBed(bed)
    }
  }

  if (loading) {
    return (
      <div className={`card border shadow-sm ${styles.loadingState}`}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading beds…</span>
        </div>
      </div>
    )
  }

  if (filteredBeds.length === 0) {
    return (
      <div className="card border shadow-sm">
        <div className={`${styles.emptyState}`}>
          <p className="mb-0">No beds found. Adjust the ward filter or ensure wards are configured.</p>
        </div>
      </div>
    )
  }

  return (
    <>
      <div className="card border shadow-sm">
        <div className="card-body">
          <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-3">
            <WardFilter value={wardTypeFilter} onChange={onWardTypeFilterChange} />
            <BedLegend />
          </div>

          <div className={styles.bedLayout}>
            {grouped.map((ward) => (
              <section key={ward.wardName} className={styles.wardSection}>
                <h3 className={styles.wardTitle}>{ward.wardName}</h3>

                {ward.rooms.map((room) => (
                  <div key={`${ward.wardName}-${room.roomNo}`} className={styles.roomBlock}>
                    <div className={styles.roomHeader}>
                      <span className={styles.roomName}>Room {room.roomNo}</span>
                      <span className={styles.roomStats}>
                        <span>Total: {room.total}</span>
                        <span>Occupied: {room.occupied}</span>
                        <span>Vacant: {room.vacant}</span>
                      </span>
                    </div>

                    <div className={styles.bedGrid}>
                      {room.beds.map((bed) => (
                        <BedSeatCard
                          key={bed.bedId}
                          bed={bed}
                          onClick={handleBedClick}
                        />
                      ))}
                    </div>
                  </div>
                ))}
              </section>
            ))}
          </div>
        </div>
      </div>

      <BedDetailsModal
        bed={detailsBed}
        onClose={() => setDetailsBed(null)}
        onView={(bed) => {
          if (bed.admissionId) {
            setDetailsBed(null)
            onViewDetails(bed)
          }
        }}
        onTransfer={(bed) => {
          setDetailsBed(null)
          onTransfer(bed)
        }}
        onDischarge={(bed) => {
          setDetailsBed(null)
          onDischarge(bed)
        }}
        onChangeStatus={(bed) => {
          setDetailsBed(null)
          onChangeStatus?.(bed)
        }}
        onMarkMaintenance={(bed) => {
          setDetailsBed(null)
          onMarkMaintenance?.(bed)
        }}
      />
    </>
  )
}
