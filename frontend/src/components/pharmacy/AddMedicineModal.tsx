import { useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'

interface AddMedicineModalProps {
  open: boolean
  onClose: () => void
}

const CATEGORIES = [
  'ANTIBIOTIC',
  'ANALGESIC',
  'CARDIAC',
  'DIABETIC',
  'IV_FLUID',
  'ICU_EMERGENCY',
  'OTHER',
]

const FORMS = ['TABLET', 'CAPSULE', 'INJECTION', 'IV', 'SYRUP', 'OINTMENT', 'OTHER']

const STORAGE_TYPES = ['ROOM_TEMP', 'COLD_CHAIN']

export function AddMedicineModal({ open, onClose }: AddMedicineModalProps) {
  const [form, setForm] = useState({
    medicineCode: '',
    medicineName: '',
    category: 'ANTIBIOTIC',
    strength: '',
    form: 'TABLET',
    minStock: 0,
    lasaFlag: false,
    storageType: 'ROOM_TEMP',
    active: true,
    manufacturer: '',
    notes: '',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  if (!open) return null

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({
      ...prev,
      [name]:
        type === 'checkbox'
          ? checked
          : name === 'minStock'
          ? Number.isNaN(Number(value))
            ? 0
            : Number(value)
          : value,
    }))
    setError('')
    setSuccess('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.medicineCode.trim() || !form.medicineName.trim()) {
      setError('Medicine code and name are required.')
      return
    }
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      await pharmacyApi.createMedicine({
        medicineCode: form.medicineCode.trim(),
        medicineName: form.medicineName.trim(),
        category: form.category,
        strength: form.strength.trim(),
        form: form.form,
        minStock: form.minStock,
        lasaFlag: form.lasaFlag,
        storageType: form.storageType,
        active: form.active,
        manufacturer: form.manufacturer.trim() || undefined,
        notes: form.notes.trim() || undefined,
      })
      setSuccess('Medicine added.')
      // keep modal open but clear key fields for next entry
      setForm((prev) => ({
        ...prev,
        medicineCode: '',
        medicineName: '',
        strength: '',
        minStock: 0,
        lasaFlag: false,
        manufacturer: '',
        notes: '',
      }))
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.errors?.medicineCode ||
        'Failed to add medicine.'
      setError(msg)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div
      className="modal d-block"
      role="dialog"
      aria-modal="true"
      style={{ background: 'rgba(15,23,42,0.45)' }}
    >
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Add Medicine</h5>
            <button
              type="button"
              className="btn-close"
              aria-label="Close"
              onClick={onClose}
            />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2">{error}</div>}
              {success && <div className="alert alert-success py-2">{success}</div>}

              <div className="row g-3">
                <div className="col-12 col-md-4">
                  <label className="form-label form-label-sm">
                    Medicine Code <span className="text-danger">*</span>
                  </label>
                  <input
                    name="medicineCode"
                    value={form.medicineCode}
                    onChange={handleChange}
                    className="form-control form-control-sm"
                    placeholder="e.g. CEFTRIAXONE_1G_IV"
                    required
                  />
                </div>
                <div className="col-12 col-md-8">
                  <label className="form-label form-label-sm">
                    Medicine Name <span className="text-danger">*</span>
                  </label>
                  <input
                    name="medicineName"
                    value={form.medicineName}
                    onChange={handleChange}
                    className="form-control form-control-sm"
                    placeholder="e.g. Ceftriaxone 1g IV"
                    required
                  />
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label form-label-sm">Category</label>
                  <select
                    name="category"
                    value={form.category}
                    onChange={handleChange}
                    className="form-select form-select-sm"
                  >
                    {CATEGORIES.map((c) => (
                      <option key={c} value={c}>
                        {c.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-12 col-md-2">
                  <label className="form-label form-label-sm">Strength</label>
                  <input
                    name="strength"
                    value={form.strength}
                    onChange={handleChange}
                    className="form-control form-control-sm"
                    placeholder="e.g. 500 mg"
                  />
                </div>
                <div className="col-12 col-md-3">
                  <label className="form-label form-label-sm">Form</label>
                  <select
                    name="form"
                    value={form.form}
                    onChange={handleChange}
                    className="form-select form-select-sm"
                  >
                    {FORMS.map((f) => (
                      <option key={f} value={f}>
                        {f.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-12 col-md-3">
                  <label className="form-label form-label-sm">Min stock level</label>
                  <input
                    name="minStock"
                    type="number"
                    min={0}
                    value={form.minStock}
                    onChange={handleChange}
                    className="form-control form-control-sm"
                  />
                </div>
                <div className="col-12 col-md-3">
                  <label className="form-label form-label-sm">Storage type</label>
                  <select
                    name="storageType"
                    value={form.storageType}
                    onChange={handleChange}
                    className="form-select form-select-sm"
                  >
                    {STORAGE_TYPES.map((s) => (
                      <option key={s} value={s}>
                        {s === 'ROOM_TEMP' ? 'Room temperature' : 'Cold chain'}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-12 col-md-3 d-flex align-items-end">
                  <div className="form-check">
                    <input
                      id="lasaFlag"
                      className="form-check-input"
                      type="checkbox"
                      name="lasaFlag"
                      checked={form.lasaFlag}
                      onChange={handleChange}
                    />
                    <label className="form-check-label small" htmlFor="lasaFlag">
                      LASA (Look-alike / Sound-alike)
                    </label>
                  </div>
                </div>
                <div className="col-12 col-md-3 d-flex align-items-end">
                  <div className="form-check">
                    <input
                      id="activeFlag"
                      className="form-check-input"
                      type="checkbox"
                      name="active"
                      checked={form.active}
                      onChange={handleChange}
                    />
                    <label className="form-check-label small" htmlFor="activeFlag">
                      Active
                    </label>
                  </div>
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label form-label-sm">Manufacturer (optional)</label>
                  <input
                    name="manufacturer"
                    value={form.manufacturer}
                    onChange={handleChange}
                    className="form-control form-control-sm"
                  />
                </div>
                <div className="col-12">
                  <label className="form-label form-label-sm">Notes (optional)</label>
                  <textarea
                    name="notes"
                    value={form.notes}
                    onChange={handleChange}
                    className="form-control form-control-sm"
                    rows={2}
                    placeholder="Any special handling instructions or internal notes"
                  />
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={onClose}
                disabled={saving}
              >
                Cancel
              </button>
              <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>
                {saving ? 'Saving…' : 'Save medicine'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

