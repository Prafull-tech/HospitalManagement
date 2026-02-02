/**
 * Unified Ward page for all ward types. Single source of truth: GET /api/ipd/hospital-beds?hospitalId=1.
 * Handles general, private, and ICU (ICU/CCU/NICU/HDU). All data derived from hospital-beds API.
 */

import { useState, useEffect, useCallback, useMemo } from 'react'
import { Link, useParams, Navigate } from 'react-router-dom'
import { bedService } from '../../services/bedService'
import { ipdBedService } from '../../services/ipdBedService'
import { useAuth } from '../../contexts/AuthContext'
import type {
  HospitalBedItem,
  WardSummaryCounts,
  BedStatus,
  WardType,
} from '../../types/bed.types'
import { WardSummaryCards } from '../../components/wards/WardSummaryCards'
import { WardBedTable } from '../../components/wards/WardBedTable'
import shared from '../../styles/Dashboard.module.css'
import styles from './WardPage.module.css'

const DEFAULT_HOSPITAL_ID = 1
const AUTO_REFRESH_MS = 60_000

const WARD_SLUGS = ['general', 'private', 'icu'] as const
type WardSlug = (typeof WARD_SLUGS)[number]

const SLUG_CONFIG: Record<
  WardSlug,
  { title: string; wardTypes: WardType[]; searchWardType: string }
> = {
  general: {
    title: 'General Ward',
    wardTypes: ['GENERAL'],
    searchWardType: 'GENERAL',
  },
  private: {
    title: 'Private Ward',
    wardTypes: ['PRIVATE'],
    searchWardType: 'PRIVATE',
  },
  icu: {
    title: 'ICU / CCU / NICU / HDU',
    wardTypes: ['ICU', 'CCU', 'NICU', 'HDU'],
    searchWardType: 'ICU',
  },
}

function isWardSlug(s: string | undefined): s is WardSlug {
  return s != null && WARD_SLUGS.includes(s as WardSlug)
}

function deriveSummaryCounts(beds: HospitalBedItem[]): WardSummaryCounts {
  let occupied = 0
  let vacant = 0
  let reserved = 0
  let cleaning = 0
  let maintenance = 0
  let isolation = 0
  for (const b of beds) {
    switch (b.bedStatus) {
      case 'OCCUPIED':
        occupied++
        break
      case 'AVAILABLE':
        vacant++
        break
      case 'RESERVED':
        reserved++
        break
      case 'CLEANING':
        cleaning++
        break
      case 'MAINTENANCE':
        maintenance++
        break
      case 'ISOLATION':
        isolation++
        break
      default:
        break
    }
  }
  return {
    total: beds.length,
    occupied,
    vacant,
    reserved,
    cleaning,
    maintenance,
    isolation,
  }
}

function WardPageSkeleton({ label }: { label: string }) {
  return (
    <div
      className={styles.skeletonWrap}
      aria-busy="true"
      aria-label={`Loading ${label}`}
    >
      <div className={shared.statsRow}>
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className={shared.statCard}>
            <div className={`${shared.statIconWrap} ${shared.primary}`} />
            <div className={shared.statContent}>
              <span className={styles.skeletonValue} />
              <span className={styles.skeletonLabel} />
            </div>
          </div>
        ))}
      </div>
      <div className={styles.skeletonTable}>
        <div className={styles.skeletonRow} />
        <div className={styles.skeletonRow} />
        <div className={styles.skeletonRow} />
        <div className={styles.skeletonRow} />
        <div className={styles.skeletonRow} />
      </div>
    </div>
  )
}

export function WardPage() {
  const { wardSlug } = useParams<{ wardSlug: string }>()
  const { user } = useAuth()
  const userRoles = user?.roles ?? ['ADMIN']

  const slug = isWardSlug(wardSlug) ? wardSlug : null
  const config = slug ? SLUG_CONFIG[slug] : null

  const [beds, setBeds] = useState<HospitalBedItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchBeds = useCallback(async () => {
    if (!config) return
    setError(null)
    try {
      const wardTypes = config.wardTypes
      let list: HospitalBedItem[]
      if (wardTypes.length === 1) {
        list = await bedService.getHospitalBeds(DEFAULT_HOSPITAL_ID, {
          wardType: wardTypes[0],
        })
      } else {
        list = await bedService.getHospitalBeds(DEFAULT_HOSPITAL_ID)
      }
      const allowed = new Set(wardTypes.map((t) => t.toUpperCase()))
      const filtered = list.filter(
        (b) => allowed.has((b.wardType ?? '').toUpperCase())
      )
      setBeds(filtered)
    } catch (e) {
      const message =
        (e as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ??
        (e instanceof Error ? e.message : 'Failed to load bed data')
      setError(message)
      setBeds([])
    } finally {
      setLoading(false)
    }
  }, [config])

  useEffect(() => {
    if (!config) return
    setLoading(true)
    fetchBeds()
  }, [config, fetchBeds])

  useEffect(() => {
    if (!config || !fetchBeds) return
    const id = setInterval(fetchBeds, AUTO_REFRESH_MS)
    return () => clearInterval(id)
  }, [config, fetchBeds])

  const handleMarkStatus = useCallback(
    async (bedId: number, status: BedStatus) => {
      try {
        await ipdBedService.updateStatus(bedId, status)
        await fetchBeds()
      } catch (e) {
        const message =
          (e as { response?: { data?: { message?: string } } })?.response?.data
            ?.message ??
          (e instanceof Error ? e.message : 'Failed to update bed status')
        setError(message)
      }
    },
    [fetchBeds]
  )

  const summaryCounts = useMemo(() => deriveSummaryCounts(beds), [beds])
  const searchNotesUrl = config
    ? `/nursing/notes/search?wardType=${encodeURIComponent(config.searchWardType)}`
    : '/nursing/notes/search'

  if (wardSlug !== undefined && !slug) {
    return <Navigate to="/wards/general" replace />
  }

  if (!config) {
    return null
  }

  return (
    <div className={shared.dashboardPage}>
      <div className={styles.header}>
        <div>
          <h1 className={shared.pageTitle}>{config.title}</h1>
          <p className={shared.pageSubtitle}>
            Bed availability and patients from Hospital Beds API (single source
            of truth)
          </p>
        </div>
        <div className={styles.actions}>
          <Link to="/ipd/hospital-beds?hospitalId=1" className={styles.link}>
            Hospital Beds
          </Link>
          <Link to="/nursing/notes" className={styles.link}>
            Nursing Notes
          </Link>
          <Link to={searchNotesUrl} className={styles.link}>
            Search notes (this ward)
          </Link>
        </div>
      </div>

      {error && (
        <div className={styles.errorBanner} role="alert">
          <span>{error}</span>
          <div className={styles.errorActions}>
            <button
              type="button"
              className={styles.retryBtn}
              onClick={() => {
                setError(null)
                setLoading(true)
                fetchBeds()
              }}
            >
              Retry
            </button>
            <button
              type="button"
              className={styles.dismissBtn}
              onClick={() => setError(null)}
              aria-label="Dismiss"
            >
              Dismiss
            </button>
          </div>
        </div>
      )}

      {loading && !error && (
        <WardPageSkeleton label={config.title} />
      )}

      {!loading && !error && (
        <>
          <WardSummaryCards counts={summaryCounts} />

          <div className={styles.card}>
            <h2 className={styles.cardTitle}>Bed grid</h2>
            {beds.length === 0 ? (
              <p className={styles.empty}>
                No beds in {config.title}.
              </p>
            ) : (
              <WardBedTable
                beds={beds}
                userRoles={userRoles}
                onMarkStatus={handleMarkStatus}
                onRefetch={fetchBeds}
              />
            )}
          </div>
        </>
      )}
    </div>
  )
}
