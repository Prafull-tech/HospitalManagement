/**
 * Hospital-wise Bed Availability Management.
 * Hospital selector (for SUPER_ADMIN), table, Add/Edit modal, role-based actions, confirmation dialogs.
 */

import { useState, useEffect, useCallback } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { bedAvailabilityService } from '../../services/bedAvailabilityService'
import { useAuth } from '../../contexts/AuthContext'
import type {
  HospitalItem,
  BedAvailabilityItem,
  BedAvailabilityFormValues,
} from '../../types/bedAvailability.types'
import { canEdit, canDelete, WARD_TYPE_LABELS } from '../../types/bedAvailability.types'
import { BedAvailabilityTable } from '../../components/ipd/BedAvailabilityTable'
import { BedAvailabilityForm } from '../../components/ipd/BedAvailabilityForm'

export default function HospitalBedAvailability() {
  const { user } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const hospitalIdParam = searchParams.get('hospitalId')

  const [hospitals, setHospitals] = useState<HospitalItem[]>([])
  const [selectedHospitalId, setSelectedHospitalId] = useState<number | null>(null)
  const [items, setItems] = useState<BedAvailabilityItem[]>([])
  const [loadingHospitals, setLoadingHospitals] = useState(true)
  const [loadingBeds, setLoadingBeds] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<BedAvailabilityItem | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [deleteTarget, setDeleteTarget] = useState<BedAvailabilityItem | null>(null)
  const [deleting, setDeleting] = useState(false)

  const primaryRole = user?.roles?.[0] ?? 'ADMIN'
  const userCanEdit = canEdit(primaryRole)
  const userCanDelete = canDelete(primaryRole)

  const fetchHospitals = useCallback(async () => {
    setLoadingHospitals(true)
    setError(null)
    try {
      const list = await bedAvailabilityService.listHospitals(true)
      setHospitals(list)
      if (list.length > 0 && !selectedHospitalId) {
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

  const fetchBeds = useCallback(async () => {
    if (selectedHospitalId == null) {
      setItems([])
      return
    }
    setLoadingBeds(true)
    setError(null)
    try {
      const list = await bedAvailabilityService.listBeds(selectedHospitalId)
      setItems(list)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to load bed availability')
    } finally {
      setLoadingBeds(false)
    }
  }, [selectedHospitalId])

  useEffect(() => {
    fetchHospitals()
  }, [fetchHospitals])

  useEffect(() => {
    if (selectedHospitalId != null) {
      setSearchParams({ hospitalId: String(selectedHospitalId) }, { replace: true })
      fetchBeds()
    } else {
      setItems([])
    }
  }, [selectedHospitalId, fetchBeds, setSearchParams])

  const handleHospitalChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const id = e.target.value ? parseInt(e.target.value, 10) : null
    setSelectedHospitalId(id)
  }

  const handleAdd = () => {
    setEditingItem(null)
    setFormOpen(true)
  }

  const handleEdit = (item: BedAvailabilityItem) => {
    setEditingItem(item)
    setFormOpen(true)
  }

  const handleFormSubmit = async (values: BedAvailabilityFormValues) => {
    if (selectedHospitalId == null) return
    setSubmitting(true)
    setError(null)
    try {
      if (editingItem) {
        const updated = await bedAvailabilityService.update(selectedHospitalId, editingItem.id, values)
        setItems((prev) => prev.map((r) => (r.id === updated.id ? updated : r)))
      } else {
        const created = await bedAvailabilityService.create(selectedHospitalId, values)
        setItems((prev) => [...prev, created].sort((a, b) => a.wardType.localeCompare(b.wardType)))
      }
      setFormOpen(false)
      setEditingItem(null)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to save')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeleteClick = (item: BedAvailabilityItem) => {
    setDeleteTarget(item)
  }

  const handleDeleteConfirm = async () => {
    if (selectedHospitalId == null || !deleteTarget) return
    setDeleting(true)
    setError(null)
    try {
      await bedAvailabilityService.delete(selectedHospitalId, deleteTarget.id)
      setItems((prev) => prev.filter((r) => r.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to delete')
    } finally {
      setDeleting(false)
    }
  }

  const selectedHospital = hospitals.find((h) => h.id === selectedHospitalId)

  return (
    <>
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
          {userCanEdit && selectedHospitalId != null && (
            <button type="button" className="btn btn-primary" onClick={handleAdd}>
              Add Record
            </button>
          )}
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
                Showing bed availability for: <strong>{selectedHospital.hospitalName}</strong>
              </p>
            )}

            <BedAvailabilityTable
              items={items}
              canEdit={userCanEdit}
              canDelete={userCanDelete}
              onEdit={handleEdit}
              onDelete={handleDeleteClick}
              loading={loadingBeds}
            />
          </>
        )}
      </div>

      <BedAvailabilityForm
        open={formOpen}
        initial={editingItem}
        onClose={() => {
          setFormOpen(false)
          setEditingItem(null)
        }}
        onSubmit={handleFormSubmit}
        submitting={submitting}
      />

      {deleteTarget && (
        <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Confirm Delete</h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setDeleteTarget(null)}
                  aria-label="Close"
                  disabled={deleting}
                />
              </div>
              <div className="modal-body">
                Delete bed availability record for <strong>{WARD_TYPE_LABELS[deleteTarget.wardType as keyof typeof WARD_TYPE_LABELS] ?? deleteTarget.wardType}</strong>? This action cannot be
                undone.
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setDeleteTarget(null)}
                  disabled={deleting}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={handleDeleteConfirm}
                  disabled={deleting}
                >
                  {deleting ? 'Deleting…' : 'Delete'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
