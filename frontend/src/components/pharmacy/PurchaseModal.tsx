import { useState, useEffect } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import type { MedicineResponse, PurchaseRequest } from '../../types/pharmacy'

interface PurchaseModalProps {
  open: boolean
  onClose: () => void
  onSuccess?: () => void
  /** Pre-select this medicine when opening. */
  preselectedMedicine?: MedicineResponse | null
}

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

export function PurchaseModal({
  open,
  onClose,
  onSuccess,
  preselectedMedicine,
}: PurchaseModalProps) {
  const [medicines, setMedicines] = useState<MedicineResponse[]>([])
  const [form, setForm] = useState<PurchaseRequest>({
    medicineId: 0,
    quantity: 1,
    transactionDate: todayStr(),
    batchNumber: '',
    expiryDate: '',
    supplier: '',
    costPerUnit: undefined,
    notes: '',
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (open) {
      pharmacyApi.listMedicines().then((data) => {
        setMedicines(data.filter((m) => m.active !== false))
      })
    }
  }, [open])

  useEffect(() => {
    if (open && preselectedMedicine) {
      setForm((f) => ({
        ...f,
        medicineId: preselectedMedicine.id,
        quantity: 1,
        transactionDate: todayStr(),
      }))
    } else if (open && !preselectedMedicine) {
      setForm({
        medicineId: medicines[0]?.id ?? 0,
        quantity: 1,
        transactionDate: todayStr(),
        batchNumber: '',
        expiryDate: '',
        supplier: '',
        costPerUnit: undefined,
        notes: '',
      })
    }
  }, [open, preselectedMedicine, medicines])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.medicineId || form.quantity < 1) return
    setSaving(true)
    setError('')
    try {
      const payload: PurchaseRequest = {
        medicineId: form.medicineId,
        quantity: form.quantity,
        transactionDate: form.transactionDate,
      }
      if (form.batchNumber?.trim()) payload.batchNumber = form.batchNumber.trim()
      if (form.expiryDate) payload.expiryDate = form.expiryDate
      if (form.supplier?.trim()) payload.supplier = form.supplier.trim()
      if (form.costPerUnit != null && form.costPerUnit > 0) payload.costPerUnit = form.costPerUnit
      if (form.notes?.trim()) payload.notes = form.notes.trim()

      await pharmacyApi.purchase(payload)
      onSuccess?.()
      onClose()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to record purchase.'))
    } finally {
      setSaving(false)
    }
  }

  if (!open) return null

  const selectedMedicine = medicines.find((m) => m.id === form.medicineId)

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} role="dialog" aria-modal="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Stock In (Purchase)</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2">{error}</div>}

              <div className="mb-3">
                <label className="form-label">Medicine</label>
                <select
                  className="form-select"
                  value={form.medicineId || ''}
                  onChange={(e) => setForm((f) => ({ ...f, medicineId: Number(e.target.value) }))}
                  required
                >
                  <option value="">Select medicine</option>
                  {medicines.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.medicineCode} – {m.medicineName} (qty: {m.quantity ?? 0})
                    </option>
                  ))}
                </select>
              </div>

              <div className="row g-2">
                <div className="col-6">
                  <label className="form-label">Quantity</label>
                  <input
                    type="number"
                    className="form-control"
                    min={1}
                    value={form.quantity}
                    onChange={(e) => setForm((f) => ({ ...f, quantity: Math.max(1, Number(e.target.value) || 0) }))}
                    required
                  />
                </div>
                <div className="col-6">
                  <label className="form-label">Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={form.transactionDate}
                    onChange={(e) => setForm((f) => ({ ...f, transactionDate: e.target.value }))}
                    required
                  />
                </div>
              </div>

              <div className="row g-2 mt-2">
                <div className="col-6">
                  <label className="form-label">Batch number</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Optional"
                    value={form.batchNumber ?? ''}
                    onChange={(e) => setForm((f) => ({ ...f, batchNumber: e.target.value }))}
                  />
                </div>
                <div className="col-6">
                  <label className="form-label">Expiry date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={form.expiryDate ?? ''}
                    onChange={(e) => setForm((f) => ({ ...f, expiryDate: e.target.value || undefined }))}
                  />
                </div>
              </div>

              <div className="mb-2 mt-2">
                <label className="form-label">Supplier</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Optional"
                  value={form.supplier ?? ''}
                  onChange={(e) => setForm((f) => ({ ...f, supplier: e.target.value }))}
                />
              </div>

              <div className="mb-2">
                <label className="form-label">Cost per unit</label>
                <input
                  type="number"
                  className="form-control"
                  min={0}
                  step="0.01"
                  placeholder="Optional"
                  value={form.costPerUnit ?? ''}
                  onChange={(e) => {
                    const v = e.target.value
                    setForm((f) => ({ ...f, costPerUnit: v === '' ? undefined : Number(v) }))
                  }}
                />
              </div>

              <div className="mb-2">
                <label className="form-label">Notes</label>
                <textarea
                  className="form-control"
                  rows={2}
                  placeholder="Optional"
                  value={form.notes ?? ''}
                  onChange={(e) => setForm((f) => ({ ...f, notes: e.target.value }))}
                />
              </div>

              {selectedMedicine && (
                <p className="small text-muted mb-0">
                  Current stock: {selectedMedicine.quantity ?? 0} → after purchase:{' '}
                  {(selectedMedicine.quantity ?? 0) + form.quantity}
                </p>
              )}
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={onClose}>
                Cancel
              </button>
              <button type="submit" className="btn btn-success" disabled={saving}>
                {saving ? 'Saving…' : 'Record Purchase'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
