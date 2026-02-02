/**
 * Hospital-wise Bed Availability – Add/Edit modal form.
 * Total Beds, Occupied, Reserved, Under Cleaning; Vacant auto-calculated (read-only).
 */

import { useState, useEffect } from 'react'
import type { BedAvailabilityItem, BedAvailabilityFormValues } from '../../types/bedAvailability.types'
import {
  computeVacant,
  validateCounts,
  WARD_TYPE_LABELS,
  type WardType,
} from '../../types/bedAvailability.types'

const WARD_TYPE_OPTIONS = (Object.entries(WARD_TYPE_LABELS) as [WardType, string][]).map(
  ([value, label]) => ({ value, label })
)

export interface BedAvailabilityFormProps {
  open: boolean
  initial?: BedAvailabilityItem | null
  onClose: () => void
  onSubmit: (values: BedAvailabilityFormValues) => void
  submitting?: boolean
}

const defaultValues: BedAvailabilityFormValues = {
  wardType: 'GENERAL',
  totalBeds: 0,
  occupiedBeds: 0,
  reservedBeds: 0,
  underCleaningBeds: 0,
}

export function BedAvailabilityForm({
  open,
  initial,
  onClose,
  onSubmit,
  submitting,
}: BedAvailabilityFormProps) {
  const [values, setValues] = useState<BedAvailabilityFormValues>({ ...defaultValues })
  const [touched, setTouched] = useState<Record<string, boolean>>({})
  const [submitError, setSubmitError] = useState<string | null>(null)

  const isEdit = Boolean(initial?.id)
  const vacant = computeVacant(values)
  const validationError = validateCounts(values)

  useEffect(() => {
    if (open) {
      setSubmitError(null)
      setTouched({})
      if (initial) {
        setValues({
          wardType: initial.wardType,
          totalBeds: initial.totalBeds,
          occupiedBeds: initial.occupied,
          reservedBeds: initial.reserved,
          underCleaningBeds: initial.underCleaning,
        })
      } else {
        setValues({ ...defaultValues })
      }
    }
  }, [open, initial])

  const handleChange = (field: keyof BedAvailabilityFormValues, value: number | string) => {
    setValues((prev) => ({ ...prev, [field]: value }))
    setTouched((prev) => ({ ...prev, [field]: true }))
    setSubmitError(null)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitError(null)
    if (validationError) {
      setSubmitError(validationError)
      return
    }
    onSubmit(values)
  }

  if (!open) return null

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{isEdit ? 'Edit Bed Availability' : 'Add Bed Availability'}</h5>
            <button
              type="button"
              className="btn-close"
              onClick={onClose}
              aria-label="Close"
              disabled={submitting}
            />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {submitError && (
                <div className="alert alert-danger py-2" role="alert">
                  {submitError}
                </div>
              )}

              <div className="mb-3">
                <label htmlFor="wardType" className="form-label">
                  Ward Type <span className="text-danger">*</span>
                </label>
                <select
                  id="wardType"
                  className="form-select"
                  value={values.wardType}
                  onChange={(e) => handleChange('wardType', e.target.value)}
                  disabled={isEdit}
                  required
                >
                  {WARD_TYPE_OPTIONS.map(({ value, label }) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
                {isEdit && (
                  <small className="text-muted">Ward type cannot be changed when editing.</small>
                )}
              </div>

              <div className="mb-3">
                <label htmlFor="totalBeds" className="form-label">
                  Total Beds <span className="text-danger">*</span>
                </label>
                <input
                  id="totalBeds"
                  type="number"
                  min={0}
                  className="form-control"
                  value={values.totalBeds}
                  onChange={(e) => handleChange('totalBeds', parseInt(e.target.value, 10) || 0)}
                  required
                />
                {touched.totalBeds && values.totalBeds < 0 && (
                  <div className="form-text text-danger">Must be &gt;= 0</div>
                )}
              </div>

              <div className="mb-3">
                <label htmlFor="occupiedBeds" className="form-label">
                  Occupied <span className="text-danger">*</span>
                </label>
                <input
                  id="occupiedBeds"
                  type="number"
                  min={0}
                  className="form-control"
                  value={values.occupiedBeds}
                  onChange={(e) => handleChange('occupiedBeds', parseInt(e.target.value, 10) || 0)}
                  required
                />
                {touched.occupiedBeds && values.occupiedBeds < 0 && (
                  <div className="form-text text-danger">Must be &gt;= 0</div>
                )}
              </div>

              <div className="mb-3">
                <label htmlFor="reservedBeds" className="form-label">
                  Reserved <span className="text-danger">*</span>
                </label>
                <input
                  id="reservedBeds"
                  type="number"
                  min={0}
                  className="form-control"
                  value={values.reservedBeds}
                  onChange={(e) => handleChange('reservedBeds', parseInt(e.target.value, 10) || 0)}
                  required
                />
                {touched.reservedBeds && values.reservedBeds < 0 && (
                  <div className="form-text text-danger">Must be &gt;= 0</div>
                )}
              </div>

              <div className="mb-3">
                <label htmlFor="underCleaningBeds" className="form-label">
                  Under Cleaning <span className="text-danger">*</span>
                </label>
                <input
                  id="underCleaningBeds"
                  type="number"
                  min={0}
                  className="form-control"
                  value={values.underCleaningBeds}
                  onChange={(e) => handleChange('underCleaningBeds', parseInt(e.target.value, 10) || 0)}
                  required
                />
                {touched.underCleaningBeds && values.underCleaningBeds < 0 && (
                  <div className="form-text text-danger">Must be &gt;= 0</div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label">Vacant (auto-calculated)</label>
                <input
                  type="text"
                  className="form-control bg-light"
                  value={vacant}
                  readOnly
                  disabled
                  aria-readonly="true"
                />
                <small className="text-muted">Vacant = Total Beds - (Occupied + Reserved + Under Cleaning)</small>
              </div>

              {validationError && (
                <div className="alert alert-warning py-2 mb-0" role="alert">
                  {validationError}
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onClose} disabled={submitting}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={submitting || Boolean(validationError)}>
                {submitting ? 'Saving…' : isEdit ? 'Update' : 'Add'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
