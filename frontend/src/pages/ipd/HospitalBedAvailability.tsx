/**
 * Hospital-wise Bed Availability – live census from actual beds.
 * Uses same data source as /ipd/beds so General, Private, ICU/CCU/NICU/HDU show consistent numbers.
 */

import { useState, useEffect, useCallback } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { bedAvailabilityService } from '../../services/bedAvailabilityService'
import { ipdBedService } from '../../services/ipdBedService'
import type { HospitalItem } from '../../types/bedAvailability.types'
import type { BedAvailabilityItem } from '../../types/ipdBed.types'
import { computeWardCensus } from '../../utils/bedCensus'

export default function HospitalBedAvailability() {
  const [searchParams, setSearchParams] = useSearchParams()
  const hospitalIdParam = searchParams.get('hospitalId')

  const [hospitals, setHospitals] = useState<HospitalItem[]>([])
  const [selectedHospitalId, setSelectedHospitalId] = useState<number | null>(null)
  const [liveBeds, setLiveBeds] = useState<BedAvailabilityItem[]>([])
  const [loadingHospitals, setLoadingHospitals] = useState(true)
  const [loadingBeds, setLoadingBeds] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchHospitals = useCallback(async () => {
    setLoadingHospitals(true)
    setError(null)
    try {
      const list = await bedAvailabilityService.listHospitals(true)
      setHospitals(list)
      if (list.length > 0 && selectedHospitalId == null) {
        const idFromUrl = hospitalIdParam ? parseInt(hospitalIdParam, 10) : null
        const validId = idFromUrl && list.some((h) => h.id === idFromUrl) ? idFromUrl : list[0].id
        setSelectedHospitalId(validId)
        if (!hospitalIdParam || parseInt(hospitalIdParam, 10) !== validId) {
          setSearchParams({ hospitalId: String(validId) }, { replace: true })
        }
      }
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to load hospitals')
    } finally {
      setLoadingHospitals(false)
    }
  }, [hospitalIdParam, selectedHospitalId, setSearchParams])

  /** Same API as /ipd/beds – single source of truth for bed availability. */
  const fetchLiveBeds = useCallback(async () => {
    setLoadingBeds(true)
    setError(null)
    try {
      const list = await ipdBedService.getAvailability()
      setLiveBeds(list)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to load bed availability')
      setLiveBeds([])
    } finally {
      setLoadingBeds(false)
    }
  }, [])

  useEffect(() => {
    fetchHospitals()
  }, [fetchHospitals])

  useEffect(() => {
    if (selectedHospitalId != null) {
      setSearchParams({ hospitalId: String(selectedHospitalId) }, { replace: true })
      fetchLiveBeds()
    } else {
      setLiveBeds([])
    }
  }, [selectedHospitalId, fetchLiveBeds, setSearchParams])

  const handleHospitalChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const id = e.target.value ? parseInt(e.target.value, 10) : null
    setSelectedHospitalId(id)
  }

  const wardCensus = computeWardCensus(liveBeds)
  const selectedHospital = hospitals.find((h) => h.id === selectedHospitalId)

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/ipd">IPD</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Hospital Bed Availability
          </li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Hospital-wise Bed Availability</h1>
        <Link to="/ipd/beds" className="btn btn-outline-primary btn-sm">
          View all beds
        </Link>
      </div>

      {loadingHospitals ? (
        <div className="card border shadow-sm">
          <div className="card-body">
            <div className="placeholder-glow">
              <span className="placeholder col-6" /> Loading hospitals…
            </div>
          </div>
        </div>
      ) : hospitals.length === 0 ? (
        <div className="alert alert-info">No hospitals found.</div>
      ) : (
        <>
          <div className="card border shadow-sm">
            <div className="card-body">
              <label htmlFor="hospital-select" className="form-label mb-2">
                Select Hospital
              </label>
              <select
                id="hospital-select"
                className="form-select"
                value={selectedHospitalId ?? ''}
                onChange={handleHospitalChange}
                aria-label="Select hospital"
              >
                {hospitals.map((h) => (
                  <option key={h.id} value={h.id}>
                    {h.hospitalName}
                    {h.location ? ` – ${h.location}` : ''}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {error && (
            <div className="alert alert-danger d-flex align-items-center justify-content-between">
              <span>{error}</span>
              <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => setError(null)}>
                Dismiss
              </button>
            </div>
          )}

          {selectedHospital && (
            <p className="text-muted small mb-0">
              Live bed census for: <strong>{selectedHospital.hospitalName}</strong> (same data as{' '}
              <Link to="/ipd/beds">Bed Availability</Link>)
            </p>
          )}

          <div className="card border shadow-sm">
            <div className="card-header bg-light py-2 d-flex align-items-center justify-content-between">
              <h6 className="mb-0">Daily Bed Census by Ward Type</h6>
              <button
                type="button"
                className="btn btn-outline-primary btn-sm"
                onClick={fetchLiveBeds}
                disabled={loadingBeds}
              >
                {loadingBeds ? 'Loading…' : 'Refresh'}
              </button>
            </div>
            {loadingBeds ? (
              <div className="card-body">
                <div className="placeholder-glow">
                  <span className="placeholder col-12" />
                  <span className="placeholder col-12" />
                  <span className="placeholder col-12" />
                </div>
              </div>
            ) : (
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
                    {wardCensus.length === 0 ? (
                      <tr>
                        <td colSpan={8} className="text-center text-muted py-4">
                          No beds found. Data is shared with <Link to="/ipd/beds">Bed Availability</Link>.
                        </td>
                      </tr>
                    ) : (
                      wardCensus.map((row) => (
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
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  )
}
