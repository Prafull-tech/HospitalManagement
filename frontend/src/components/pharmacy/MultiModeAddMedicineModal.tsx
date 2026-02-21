/**
 * Multi-Mode Medicine Entry Modal
 * Entry modes: Manual, Barcode/GTIN, Excel Import, Existing Medicine (Add Batch)
 * NABH / LASA compliant, audit-ready
 */

import { useState, useEffect } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import type {
  MedicineResponse,
  MedicineRequest,
  RackResponse,
  RackSuggestion,
  ShelfResponse,
  MedicineImportResult,
  ManualEntryRequest,
  BarcodeEntryRequest,
  ExistingBatchRequest,
} from '../../types/pharmacy'

type EntryMode = 'MANUAL' | 'BARCODE' | 'EXCEL' | 'EXISTING'

const CATEGORIES = ['ANTIBIOTIC', 'ANALGESIC', 'CARDIAC', 'DIABETIC', 'IV_FLUID', 'ICU_EMERGENCY', 'OTHER']
const FORMS = ['TABLET', 'CAPSULE', 'INJECTION', 'IV', 'SYRUP', 'OINTMENT', 'OTHER']
const STORAGE_TYPES = ['ROOM_TEMP', 'COLD_CHAIN']
const ACCEPTED_TYPES = '.xlsx'
const MAX_SIZE_MB = 10

interface MultiModeAddMedicineModalProps {
  open: boolean
  onClose: () => void
  onSuccess?: () => void
  medicine?: MedicineResponse | null
  /** When set, opens directly in Excel import mode (e.g. from "Import Medicines" button). */
  initialMode?: EntryMode
}

export function MultiModeAddMedicineModal({
  open,
  onClose,
  onSuccess,
  medicine,
  initialMode = 'MANUAL',
}: MultiModeAddMedicineModalProps) {
  const isEdit = !!medicine
  const [entryMode, setEntryMode] = useState<EntryMode>(initialMode)
  const [racks, setRacks] = useState<RackResponse[]>([])
  const [shelves] = useState<ShelfResponse[]>([])
  const [medicines, setMedicines] = useState<MedicineResponse[]>([])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (open) {
      setEntryMode(initialMode)
      pharmacyApi.listRacks(true).then(setRacks).catch(() => setRacks([]))
      pharmacyApi.listMedicines().then(setMedicines).catch(() => setMedicines([]))
    }
  }, [open, initialMode])

  const resetState = () => {
    setError('')
    setSuccess('')
    setSaving(false)
  }

  if (!open) return null

  if (isEdit) {
    return (
      <ManualEntryForm
        racks={racks}
        shelves={shelves}
        medicine={medicine}
        onClose={onClose}
        onSuccess={() => {
          onSuccess?.()
          onClose()
        }}
        setError={setError}
        setSuccess={setSuccess}
        setSaving={setSaving}
        error={error}
        success={success}
        saving={saving}
      />
    )
  }

  return (
    <div
      className="modal d-block"
      role="dialog"
      aria-modal="true"
      style={{ background: 'rgba(15,23,42,0.45)' }}
    >
      <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Add Medicine</h5>
            <button type="button" className="btn-close" aria-label="Close" onClick={onClose} />
          </div>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label fw-semibold">Entry Mode</label>
              <div className="d-flex flex-wrap gap-3">
                {(['MANUAL', 'BARCODE', 'EXCEL', 'EXISTING'] as const).map((mode) => (
                  <div key={mode} className="form-check">
                    <input
                      id={`mode-${mode}`}
                      type="radio"
                      className="form-check-input"
                      name="entryMode"
                      checked={entryMode === mode}
                      onChange={() => {
                        setEntryMode(mode)
                        resetState()
                      }}
                    />
                    <label className="form-check-label" htmlFor={`mode-${mode}`}>
                      {mode === 'MANUAL' && 'Manual Entry'}
                      {mode === 'BARCODE' && 'Barcode Scan / GTIN'}
                      {mode === 'EXCEL' && 'Import from Excel'}
                      {mode === 'EXISTING' && 'Existing Medicine (Add Batch)'}
                    </label>
                  </div>
                ))}
              </div>
            </div>

            {error && <div className="alert alert-danger py-2">{error}</div>}
            {success && <div className="alert alert-success py-2">{success}</div>}

            {entryMode === 'MANUAL' && (
              <ManualEntryForm
                racks={racks}
                shelves={shelves}
                onClose={onClose}
                onSuccess={() => {
                  onSuccess?.()
                  onClose()
                }}
                setError={setError}
                setSuccess={setSuccess}
                setSaving={setSaving}
                error={error}
                success={success}
                saving={saving}
              />
            )}
            {entryMode === 'BARCODE' && (
              <BarcodeEntryForm
                racks={racks}
                shelves={shelves}
                onClose={onClose}
                onSuccess={() => {
                  onSuccess?.()
                  onClose()
                }}
                setError={setError}
                setSuccess={setSuccess}
                setSaving={setSaving}
                error={error}
                success={success}
                saving={saving}
              />
            )}
            {entryMode === 'EXCEL' && (
              <ExcelImportForm
                onClose={onClose}
                onSuccess={() => {
                  onSuccess?.()
                  onClose()
                }}
                setError={setError}
                error={error}
              />
            )}
            {entryMode === 'EXISTING' && (
              <ExistingMedicineForm
                medicines={medicines}
                racks={racks}
                shelves={shelves}
                onClose={onClose}
                onSuccess={() => {
                  onSuccess?.()
                  onClose()
                }}
                setError={setError}
                setSuccess={setSuccess}
                setSaving={setSaving}
                error={error}
                success={success}
                saving={saving}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

// ----- Manual Entry Form -----
function ManualEntryForm({
  racks,
  shelves: shelvesProp,
  medicine,
  onClose,
  onSuccess,
  setError,
  setSuccess,
  setSaving,
  error: _error,
  success: _success,
  saving,
}: {
  racks: RackResponse[]
  shelves: ShelfResponse[]
  medicine?: MedicineResponse | null
  onClose: () => void
  onSuccess: () => void
  setError: (s: string) => void
  setSuccess: (s: string) => void
  setSaving: (b: boolean) => void
  error: string
  success: string
  saving: boolean
}) {
  const [form, setForm] = useState({
    medicineCode: '',
    medicineName: '',
    category: 'ANTIBIOTIC' as string,
    strength: '',
    form: 'TABLET' as string,
    minStock: 0,
    quantity: 0,
    lasaFlag: false,
    storageType: 'ROOM_TEMP' as string,
    active: true,
    manufacturer: '',
    notes: '',
    rackId: undefined as number | undefined,
    shelfId: undefined as number | undefined,
    binNumber: '',
    batchNumber: '',
    expiryDate: '',
  })
  const [shelves, setShelves] = useState<ShelfResponse[]>(shelvesProp)
  const [suggestion, setSuggestion] = useState<RackSuggestion | null>(null)
  const [suggestionLoading, setSuggestionLoading] = useState(false)

  useEffect(() => {
    if (medicine) {
      setForm({
        medicineCode: medicine.medicineCode,
        medicineName: medicine.medicineName,
        category: medicine.category,
        strength: medicine.strength ?? '',
        form: medicine.form,
        minStock: medicine.minStock ?? 0,
        quantity: medicine.quantity ?? 0,
        lasaFlag: medicine.lasaFlag ?? false,
        storageType: medicine.storageType,
        active: medicine.active !== false,
        manufacturer: medicine.manufacturer ?? '',
        notes: medicine.notes ?? '',
        rackId: medicine.rackId,
        shelfId: medicine.shelfId,
        binNumber: medicine.binNumber ?? '',
        batchNumber: '',
        expiryDate: '',
      })
    }
  }, [medicine])

  useEffect(() => {
    if (form.rackId) {
      pharmacyApi.listShelves(form.rackId).then(setShelves).catch(() => setShelves([]))
    } else {
      setShelves([])
    }
  }, [form.rackId])

  useEffect(() => {
    setSuggestion(null)
    setSuggestionLoading(true)
    pharmacyApi
      .suggestRack({
        category: form.category,
        storageType: form.storageType,
        lasaFlag: form.lasaFlag,
      })
      .then(setSuggestion)
      .catch(() => setSuggestion(null))
      .finally(() => setSuggestionLoading(false))
  }, [form.category, form.storageType, form.lasaFlag])

  const racksForStorage = racks.filter((r) => r.storageType === form.storageType)
  const selectedRack = form.rackId ? racks.find((r) => r.id === form.rackId) : null
  const storageMismatch = selectedRack && selectedRack.storageType !== form.storageType

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const t = e.target
    const { name, value, type } = t
    const checked = t instanceof HTMLInputElement ? t.checked : false
    setForm((prev) => ({
      ...prev,
      [name]:
        type === 'checkbox'
          ? checked
          : ['minStock', 'quantity'].includes(name)
            ? (Number.isNaN(Number(value)) ? 0 : Number(value))
            : value,
      ...(name === 'storageType' && { rackId: undefined, shelfId: undefined }),
      ...(name === 'rackId' && { shelfId: undefined }),
    }))
    setError('')
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
      const req: ManualEntryRequest = {
        medicine: {
          medicineCode: form.medicineCode.trim(),
          medicineName: form.medicineName.trim(),
          category: form.category,
          strength: form.strength.trim(),
          form: form.form,
          minStock: form.minStock,
          quantity: form.quantity ?? 0,
          lasaFlag: form.lasaFlag,
          storageType: form.storageType,
          active: form.active,
          manufacturer: form.manufacturer.trim() || undefined,
          notes: form.notes.trim() || undefined,
          rackId: form.rackId,
          shelfId: form.shelfId,
          binNumber: form.binNumber.trim() || undefined,
        },
        batchNumber: form.batchNumber.trim() || undefined,
        expiryDate: form.expiryDate || undefined,
        quantity: form.quantity,
      }
      await pharmacyApi.createManualEntry(req)
      setSuccess('Medicine added.')
      onSuccess()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to add medicine.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="row g-3">
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Medicine Code *</label>
          <input
            name="medicineCode"
            value={form.medicineCode}
            onChange={handleChange}
            className="form-control form-control-sm"
            required
          />
        </div>
        <div className="col-12 col-md-8">
          <label className="form-label form-label-sm">Medicine Name *</label>
          <input
            name="medicineName"
            value={form.medicineName}
            onChange={handleChange}
            className="form-control form-control-sm"
            required
          />
        </div>
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Category</label>
          <select name="category" value={form.category} onChange={handleChange} className="form-select form-select-sm">
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>
        <div className="col-12 col-md-2">
          <label className="form-label form-label-sm">Strength</label>
          <input name="strength" value={form.strength} onChange={handleChange} className="form-control form-control-sm" />
        </div>
        <div className="col-12 col-md-3">
          <label className="form-label form-label-sm">Form</label>
          <select name="form" value={form.form} onChange={handleChange} className="form-select form-select-sm">
            {FORMS.map((f) => (
              <option key={f} value={f}>
                {f.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>
        <div className="col-12 col-md-2">
          <label className="form-label form-label-sm">Min Stock</label>
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
          <label className="form-label form-label-sm">Storage Type</label>
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
        {suggestion && !form.rackId && (
          <div className="col-12">
            <div className="alert alert-info py-2 small mb-0">
              <strong>Suggested Rack:</strong> {suggestion.rackCode} / Shelf {suggestion.shelfCode}
              <br />
              <span className="text-muted">{suggestion.reason}</span>
              <div className="mt-2 d-flex gap-2">
                <button
                  type="button"
                  className="btn btn-sm btn-outline-primary"
                  onClick={() => {
                    setForm((p) => ({
                      ...p,
                      rackId: suggestion.rackId,
                      shelfId: suggestion.shelfId,
                    }))
                    setSuggestion(null)
                  }}
                >
                  Accept Suggestion
                </button>
                <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => setSuggestion(null)}>
                  Override manually
                </button>
              </div>
            </div>
          </div>
        )}
        {suggestionLoading && !suggestion && (
          <div className="col-12">
            <small className="text-muted">Loading rack suggestion…</small>
          </div>
        )}
        {storageMismatch && (
          <div className="col-12">
            <div className="alert alert-warning py-2 small mb-0">
              This medicine requires {form.storageType === 'COLD_CHAIN' ? 'cold chain' : 'room temperature'} storage.
              The selected rack may not match.
            </div>
          </div>
        )}
        <div className="col-12 col-md-3">
          <label className="form-label form-label-sm">Rack</label>
          <select
            name="rackId"
            value={form.rackId ?? ''}
            onChange={(e) => {
              const val = e.target.value ? Number(e.target.value) : undefined
              setForm((p) => ({
                ...p,
                rackId: val,
                shelfId: undefined,
              }))
            }}
            className="form-select form-select-sm"
          >
            <option value="">—</option>
            {racksForStorage.map((r) => (
              <option key={r.id} value={r.id}>
                {r.rackCode}
              </option>
            ))}
          </select>
        </div>
        <div className="col-12 col-md-3">
          <label className="form-label form-label-sm">Shelf</label>
          <select
            name="shelfId"
            value={form.shelfId ?? ''}
            onChange={(e) =>
              setForm((p) => ({
                ...p,
                shelfId: e.target.value ? Number(e.target.value) : undefined,
              }))
            }
            className="form-select form-select-sm"
            disabled={!form.rackId}
          >
            <option value="">—</option>
            {shelves.map((s) => (
              <option key={s.id} value={s.id}>
                {s.shelfCode}
              </option>
            ))}
          </select>
        </div>
        <div className="col-12">
          <hr className="my-2" />
          <small className="text-muted fw-semibold">Batch Section</small>
        </div>
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Batch No</label>
          <input
            name="batchNumber"
            value={form.batchNumber}
            onChange={handleChange}
            className="form-control form-control-sm"
          />
        </div>
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Expiry</label>
          <input
            name="expiryDate"
            type="date"
            value={form.expiryDate}
            onChange={handleChange}
            className="form-control form-control-sm"
          />
        </div>
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Quantity</label>
          <input
            name="quantity"
            type="number"
            min={0}
            value={form.quantity}
            onChange={handleChange}
            className="form-control form-control-sm"
          />
        </div>
        <div className="col-12 col-md-6">
          <div className="form-check">
            <input
              id="lasaFlag"
              type="checkbox"
              name="lasaFlag"
              checked={form.lasaFlag}
              onChange={handleChange}
              className="form-check-input"
            />
            <label className="form-check-label small" htmlFor="lasaFlag">
              LASA (Look-alike / Sound-alike)
            </label>
          </div>
        </div>
        <div className="col-12 col-md-6">
          <label className="form-label form-label-sm">Manufacturer</label>
          <input
            name="manufacturer"
            value={form.manufacturer}
            onChange={handleChange}
            className="form-control form-control-sm"
          />
        </div>
      </div>
      <div className="modal-footer mt-3">
        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>
          {saving ? 'Saving…' : 'Save medicine'}
        </button>
      </div>
    </form>
  )
}

// ----- Barcode Entry Form -----
function BarcodeEntryForm({
  racks,
  shelves: shelvesProp,
  onClose,
  onSuccess,
  setError,
  setSuccess,
  setSaving,
  error: _error,
  success: _success,
  saving,
}: {
  racks: RackResponse[]
  shelves: ShelfResponse[]
  onClose: () => void
  onSuccess: () => void
  setError: (s: string) => void
  setSuccess: (s: string) => void
  setSaving: (b: boolean) => void
  error: string
  success: string
  saving: boolean
}) {
  const [barcode, setBarcode] = useState('')
  const [lookedUp, setLookedUp] = useState<MedicineResponse | null>(null)
  const [lookupSource, setLookupSource] = useState<'LOCAL' | 'EXTERNAL' | null>(null)
  const [lookupLoading, setLookupLoading] = useState(false)
  const [batchNumber, setBatchNumber] = useState('')
  const [expiryDate, setExpiryDate] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [rackId, setRackId] = useState<number | undefined>()
  const [shelfId, setShelfId] = useState<number | undefined>()
  const [createNew, setCreateNew] = useState<MedicineRequest | null>(null)
  const [suggestion, setSuggestion] = useState<RackSuggestion | null>(null)
  const [shelves, setShelves] = useState<ShelfResponse[]>(shelvesProp)

  const medicineForSuggestion = lookedUp ?? createNew

  useEffect(() => {
    if (rackId) {
      pharmacyApi.listShelves(rackId).then(setShelves).catch(() => setShelves([]))
    } else {
      setShelves([])
    }
  }, [rackId])

  useEffect(() => {
    if (!medicineForSuggestion) {
      setSuggestion(null)
      return
    }
    pharmacyApi
      .suggestRack({
        category: medicineForSuggestion.category,
        storageType: medicineForSuggestion.storageType,
        lasaFlag: medicineForSuggestion.lasaFlag ?? false,
      })
      .then(setSuggestion)
      .catch(() => setSuggestion(null))
  }, [medicineForSuggestion?.category, medicineForSuggestion?.storageType, medicineForSuggestion?.lasaFlag])

  const handleLookup = async () => {
    if (!barcode.trim()) {
      setError('Enter or scan barcode.')
      return
    }
    setLookupLoading(true)
    setError('')
    setLookedUp(null)
    setCreateNew(null)
    setLookupSource(null)
    try {
      const result = await pharmacyApi.lookupMedicine(barcode.trim())
      if (result) {
        setLookupSource(result.source)
        if (result.source === 'LOCAL') {
          setLookedUp(result.data)
        } else {
          const d = result.data
          setCreateNew({
            medicineCode: d.medicineCode ?? barcode.trim().replace(/\s/g, '_').substring(0, 50),
            medicineName: d.medicineName ?? '',
            category: d.category ?? 'OTHER',
            strength: d.strength ?? '',
            form: d.form ?? 'TABLET',
            minStock: d.minStock ?? 0,
            quantity: 0,
            lasaFlag: d.lasaFlag ?? false,
            storageType: d.storageType ?? 'ROOM_TEMP',
            active: true,
            manufacturer: d.manufacturer,
            barcode: barcode.trim(),
          })
        }
      } else {
        setError('Medicine not found. Create new?')
      }
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Barcode lookup failed.'))
    } finally {
      setLookupLoading(false)
    }
  }

  const handleCreateNew = () => {
    setLookupSource(null)
    setCreateNew({
      medicineCode: barcode.trim().replace(/\s/g, '_').substring(0, 50),
      medicineName: '',
      category: 'OTHER',
      strength: '',
      form: 'TABLET',
      minStock: 0,
      quantity: 0,
      lasaFlag: false,
      storageType: 'ROOM_TEMP',
      active: true,
      barcode: barcode.trim(),
    })
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      if (lookedUp) {
        if (quantity < 1) {
          setError('Quantity must be at least 1 when adding batch.')
          setSaving(false)
          return
        }
        const req: BarcodeEntryRequest = {
          barcode: barcode.trim(),
          batchNumber: batchNumber.trim() || undefined,
          expiryDate: expiryDate || undefined,
          quantity,
          rackId,
          shelfId,
          createNewMedicine: undefined,
        }
        await pharmacyApi.createFromBarcode(req)
      } else if (createNew) {
        if (!createNew.medicineName?.trim()) {
          setError('Medicine name is required when creating new.')
          setSaving(false)
          return
        }
        const req: BarcodeEntryRequest = {
          barcode: barcode.trim(),
          batchNumber: batchNumber.trim() || undefined,
          expiryDate: expiryDate || undefined,
          quantity: quantity,
          rackId,
          shelfId,
          createNewMedicine: { ...createNew, barcode: barcode.trim(), rackId, shelfId },
          fromExternalLookup: lookupSource === 'EXTERNAL',
        }
        await pharmacyApi.createFromBarcode(req)
      } else {
        setError('Look up barcode first or choose "Create new".')
        setSaving(false)
        return
      }
      setSuccess('Medicine added.')
      onSuccess()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to add medicine.'))
    } finally {
      setSaving(false)
    }
  }

  const showBatchForm = lookedUp || createNew

  return (
    <form onSubmit={handleSubmit}>
      <div className="mb-3">
        <label className="form-label form-label-sm">Scan Barcode / Enter GTIN</label>
        <div className="d-flex gap-2">
          <input
            type="text"
            className="form-control form-control-sm"
            value={barcode}
            onChange={(e) => setBarcode(e.target.value)}
            placeholder="Scan or type barcode"
            onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleLookup())}
          />
          <button type="button" className="btn btn-outline-primary btn-sm" onClick={handleLookup} disabled={lookupLoading}>
            {lookupLoading ? 'Looking up…' : 'Look up'}
          </button>
        </div>
      </div>

      {lookedUp && (
        <div className="alert alert-info py-2 small mb-3">
          <strong>Found (local):</strong> {lookedUp.medicineName} ({lookedUp.medicineCode})
        </div>
      )}

      {createNew && lookupSource === 'EXTERNAL' && (
        <div className="alert alert-success py-2 small mb-3">
          <strong>Fetched from external database.</strong> Review and edit before saving.
        </div>
      )}

      {!lookedUp && !createNew && barcode && (
        <div className="mb-3">
          <button type="button" className="btn btn-outline-success btn-sm" onClick={handleCreateNew}>
            Medicine not found. Create new?
          </button>
        </div>
      )}

      {createNew && (
        <div className="mb-3">
          <label className="form-label form-label-sm">Medicine Name *</label>
          <input
            type="text"
            className="form-control form-control-sm"
            value={createNew.medicineName}
            onChange={(e) => setCreateNew((p) => (p ? { ...p, medicineName: e.target.value } : null))}
            required
          />
          {lookupSource === 'EXTERNAL' && (
            <div className="row g-2 mt-2">
              <div className="col-12 col-md-4">
                <label className="form-label form-label-sm">Strength</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={createNew.strength}
                  onChange={(e) => setCreateNew((p) => (p ? { ...p, strength: e.target.value } : null))}
                />
              </div>
              <div className="col-12 col-md-4">
                <label className="form-label form-label-sm">Form</label>
                <select
                  className="form-select form-select-sm"
                  value={createNew.form}
                  onChange={(e) => setCreateNew((p) => (p ? { ...p, form: e.target.value } : null))}
                >
                  {FORMS.map((f) => (
                    <option key={f} value={f}>
                      {f.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-12 col-md-4">
                <label className="form-label form-label-sm">Manufacturer</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={createNew.manufacturer ?? ''}
                  onChange={(e) => setCreateNew((p) => (p ? { ...p, manufacturer: e.target.value || undefined } : null))}
                />
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label form-label-sm">Category</label>
                <select
                  className="form-select form-select-sm"
                  value={createNew.category}
                  onChange={(e) => setCreateNew((p) => (p ? { ...p, category: e.target.value } : null))}
                >
                  {CATEGORIES.map((c) => (
                    <option key={c} value={c}>
                      {c.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label form-label-sm">Storage Type</label>
                <select
                  className="form-select form-select-sm"
                  value={createNew.storageType}
                  onChange={(e) => setCreateNew((p) => (p ? { ...p, storageType: e.target.value } : null))}
                >
                  {STORAGE_TYPES.map((s) => (
                    <option key={s} value={s}>
                      {s === 'ROOM_TEMP' ? 'Room temperature' : 'Cold chain'}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )}
        </div>
      )}

      {showBatchForm && (
        <>
          {suggestion && !rackId && (
            <div className="alert alert-info py-2 small mb-3">
              <strong>Suggested Rack:</strong> {suggestion.rackCode} / Shelf {suggestion.shelfCode}
              <br />
              <span className="text-muted">{suggestion.reason}</span>
              <div className="mt-2 d-flex gap-2">
                <button
                  type="button"
                  className="btn btn-sm btn-outline-primary"
                  onClick={() => {
                    setRackId(suggestion.rackId)
                    setShelfId(suggestion.shelfId)
                    setSuggestion(null)
                  }}
                >
                  Accept Suggestion
                </button>
                <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => setSuggestion(null)}>
                  Override manually
                </button>
              </div>
            </div>
          )}
          <hr className="my-2" />
          <div className="row g-3">
            <div className="col-12 col-md-4">
              <label className="form-label form-label-sm">Batch No</label>
              <input
                className="form-control form-control-sm"
                value={batchNumber}
                onChange={(e) => setBatchNumber(e.target.value)}
              />
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label form-label-sm">Expiry</label>
              <input
                type="date"
                className="form-control form-control-sm"
                value={expiryDate}
                onChange={(e) => setExpiryDate(e.target.value)}
              />
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label form-label-sm">Quantity *</label>
              <input
                type="number"
                min={0}
                className="form-control form-control-sm"
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value) || 0)}
              />
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label form-label-sm">Rack</label>
              <select
                className="form-select form-select-sm"
                value={rackId ?? ''}
                onChange={(e) => {
                  const val = e.target.value ? Number(e.target.value) : undefined
                  setRackId(val)
                  setShelfId(undefined)
                }}
              >
                <option value="">—</option>
                {(medicineForSuggestion?.storageType
                  ? racks.filter((r) => r.storageType === medicineForSuggestion.storageType)
                  : racks
                ).map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.rackCode}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label form-label-sm">Shelf</label>
              <select
                className="form-select form-select-sm"
                value={shelfId ?? ''}
                onChange={(e) => setShelfId(e.target.value ? Number(e.target.value) : undefined)}
                disabled={!rackId}
              >
                <option value="">—</option>
                {shelves.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.shelfCode}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </>
      )}

      <div className="modal-footer mt-3">
        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary btn-sm" disabled={saving || !showBatchForm}>
          {saving ? 'Saving…' : 'Save'}
        </button>
      </div>
    </form>
  )
}

type ExcelImportType = 'master' | 'stock'

// ----- Excel Import Form -----
function ExcelImportForm({
  onClose,
  onSuccess,
  setError,
  error: _error,
}: {
  onClose: () => void
  onSuccess: () => void
  setError: (s: string) => void
  error: string
}) {
  const [importType, setImportType] = useState<ExcelImportType>('master')
  const [file, setFile] = useState<File | null>(null)
  const [importing, setImporting] = useState(false)
  const [downloadingTemplate, setDownloadingTemplate] = useState(false)
  const [result, setResult] = useState<MedicineImportResult | null>(null)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0]
    setError('')
    setResult(null)
    if (!f) {
      setFile(null)
      return
    }
    if (!f.name.toLowerCase().endsWith('.xlsx')) {
      setError('Only .xlsx files are allowed.')
      setFile(null)
      return
    }
    if (f.size > MAX_SIZE_MB * 1024 * 1024) {
      setError(`File size must not exceed ${MAX_SIZE_MB} MB.`)
      setFile(null)
      return
    }
    setFile(f)
  }

  const handleDownloadTemplate = async () => {
    setDownloadingTemplate(true)
    setError('')
    try {
      const blob = await pharmacyApi.downloadMedicineTemplate(importType)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = importType === 'stock' ? 'stock-import-template.xlsx' : 'medicine-master-import-template.xlsx'
      a.click()
      URL.revokeObjectURL(url)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to download template.'))
    } finally {
      setDownloadingTemplate(false)
    }
  }

  const handleImport = async () => {
    if (!file) {
      setError('Please select an Excel file.')
      return
    }
    setImporting(true)
    setError('')
    setResult(null)
    try {
      const res =
        importType === 'stock'
          ? await pharmacyApi.importStock(file)
          : await pharmacyApi.importMedicineMaster(file)
      setResult(res)
      if (res.successCount > 0) onSuccess()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Import failed.'))
    } finally {
      setImporting(false)
    }
  }

  const handleDownloadErrorReport = async () => {
    if (!result?.errors?.length) return
    try {
      const blob = await pharmacyApi.downloadImportErrorReport(result)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'medicine-import-errors.xlsx'
      a.click()
      URL.revokeObjectURL(url)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to download error report.'))
    }
  }

  const showResult = result !== null

  return (
    <>
      {!showResult ? (
        <>
          <div className="mb-3">
            <label className="form-label fw-semibold">Import Type</label>
            <div className="d-flex flex-wrap gap-3">
              <div className="form-check">
                <input
                  id="excel-import-master"
                  type="radio"
                  className="form-check-input"
                  name="excelImportType"
                  checked={importType === 'master'}
                  onChange={() => {
                    setImportType('master')
                    setFile(null)
                    setResult(null)
                  }}
                />
                <label className="form-check-label" htmlFor="excel-import-master">
                  Medicine Master Import
                </label>
              </div>
              <div className="form-check">
                <input
                  id="excel-import-stock"
                  type="radio"
                  className="form-check-input"
                  name="excelImportType"
                  checked={importType === 'stock'}
                  onChange={() => {
                    setImportType('stock')
                    setFile(null)
                    setResult(null)
                  }}
                />
                <label className="form-check-label" htmlFor="excel-import-stock">
                  Stock Import
                </label>
              </div>
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label form-label-sm">Excel file (.xlsx)</label>
            <input
              type="file"
              accept={ACCEPTED_TYPES}
              onChange={handleFileChange}
              className="form-control form-control-sm"
            />
            {file && (
              <small className="text-muted d-block mt-1">
                {file.name} ({(file.size / 1024).toFixed(1)} KB)
              </small>
            )}
          </div>
          <div className="mb-3">
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm"
              onClick={handleDownloadTemplate}
              disabled={downloadingTemplate}
            >
              {downloadingTemplate ? 'Downloading…' : `Download ${importType === 'stock' ? 'Stock' : 'Master'} Template`}
            </button>
          </div>
        </>
      ) : (
        <>
          <div className="alert alert-info py-2 mb-3">
            <strong>Import complete.</strong> Total: {result.totalRows} | Success: {result.successCount} | Failed:{' '}
            {result.failedCount}
          </div>
          {result.errors?.length ? (
            <>
              <div className="table-responsive" style={{ maxHeight: 200 }}>
                <table className="table table-sm table-bordered">
                  <thead className="table-light">
                    <tr>
                      <th>Row</th>
                      <th>Error</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.errors.map((e, i) => (
                      <tr key={i}>
                        <td>{e.row}</td>
                        <td>{e.error}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <button type="button" className="btn btn-outline-primary btn-sm mt-2" onClick={handleDownloadErrorReport}>
                Download Error Report
              </button>
            </>
          ) : null}
        </>
      )}
      <div className="modal-footer mt-3">
        {showResult ? (
          <button type="button" className="btn btn-primary btn-sm" onClick={onClose}>
            Close
          </button>
        ) : (
          <>
            <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={importing}>
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={handleImport}
              disabled={!file || importing}
            >
              {importing ? 'Importing…' : 'Import'}
            </button>
          </>
        )}
      </div>
    </>
  )
}

// ----- Existing Medicine Form -----
function ExistingMedicineForm({
  medicines,
  racks,
  shelves: shelvesProp,
  onClose,
  onSuccess,
  setError,
  setSuccess,
  setSaving,
  error: _error,
  success: _success,
  saving,
}: {
  medicines: MedicineResponse[]
  racks: RackResponse[]
  shelves: ShelfResponse[]
  onClose: () => void
  onSuccess: () => void
  setError: (s: string) => void
  setSuccess: (s: string) => void
  setSaving: (b: boolean) => void
  error: string
  success: string
  saving: boolean
}) {
  const [search, setSearch] = useState('')
  const [selectedId, setSelectedId] = useState<number | undefined>()
  const [batchNumber, setBatchNumber] = useState('')
  const [expiryDate, setExpiryDate] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [rackId, setRackId] = useState<number | undefined>()
  const [shelfId, setShelfId] = useState<number | undefined>()
  const [suggestion, setSuggestion] = useState<RackSuggestion | null>(null)
  const [shelves, setShelves] = useState<ShelfResponse[]>(shelvesProp)

  useEffect(() => {
    if (rackId) {
      pharmacyApi.listShelves(rackId).then(setShelves).catch(() => setShelves([]))
    } else {
      setShelves([])
    }
  }, [rackId])

  const filtered = medicines.filter(
    (m) =>
      !search.trim() ||
      m.medicineName.toLowerCase().includes(search.toLowerCase()) ||
      m.medicineCode.toLowerCase().includes(search.toLowerCase())
  )

  const selected = selectedId ? medicines.find((m) => m.id === selectedId) : null

  useEffect(() => {
    if (!selected) {
      setSuggestion(null)
      return
    }
    pharmacyApi
      .suggestRack({
        category: selected.category,
        storageType: selected.storageType,
        lasaFlag: selected.lasaFlag ?? false,
      })
      .then(setSuggestion)
      .catch(() => setSuggestion(null))
  }, [selected?.id, selected?.category, selected?.storageType, selected?.lasaFlag])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedId) {
      setError('Select an existing medicine.')
      return
    }
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const req: ExistingBatchRequest = {
        medicineId: selectedId,
        batchNumber: batchNumber.trim() || undefined,
        expiryDate: expiryDate || undefined,
        quantity,
        rackId,
        shelfId,
      }
      await pharmacyApi.addBatchToExisting(req)
      setSuccess('Batch added.')
      onSuccess()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to add batch.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="mb-3">
        <label className="form-label form-label-sm">Search Existing Medicine</label>
        <input
          type="text"
          className="form-control form-control-sm"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Type medicine name or code"
        />
      </div>
      <div className="mb-3">
        <label className="form-label form-label-sm">Select Medicine</label>
        <select
          className="form-select form-select-sm"
          value={selectedId ?? ''}
          onChange={(e) => setSelectedId(e.target.value ? Number(e.target.value) : undefined)}
        >
          <option value="">— Select —</option>
          {filtered.slice(0, 50).map((m) => (
            <option key={m.id} value={m.id}>
              {m.medicineCode} — {m.medicineName} (Stock: {m.quantity ?? 0})
            </option>
          ))}
        </select>
      </div>
      {selected && (
        <div className="alert alert-light py-2 small mb-3">
          <strong>Current stock:</strong> {selected.quantity ?? 0}
        </div>
      )}
      {selected && suggestion && !rackId && (
        <div className="alert alert-info py-2 small mb-3">
          <strong>Suggested Rack:</strong> {suggestion.rackCode} / Shelf {suggestion.shelfCode}
          <br />
          <span className="text-muted">{suggestion.reason}</span>
          <div className="mt-2 d-flex gap-2">
            <button
              type="button"
              className="btn btn-sm btn-outline-primary"
              onClick={() => {
                setRackId(suggestion.rackId)
                setShelfId(suggestion.shelfId)
                setSuggestion(null)
              }}
            >
              Accept Suggestion
            </button>
            <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => setSuggestion(null)}>
              Override manually
            </button>
          </div>
        </div>
      )}
      <hr className="my-2" />
      <div className="row g-3">
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Batch No</label>
          <input
            className="form-control form-control-sm"
            value={batchNumber}
            onChange={(e) => setBatchNumber(e.target.value)}
          />
        </div>
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Expiry</label>
          <input
            type="date"
            className="form-control form-control-sm"
            value={expiryDate}
            onChange={(e) => setExpiryDate(e.target.value)}
          />
        </div>
        <div className="col-12 col-md-4">
          <label className="form-label form-label-sm">Quantity *</label>
          <input
            type="number"
            min={1}
            className="form-control form-control-sm"
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value) || 1)}
          />
        </div>
        {selected && (
          <>
            <div className="col-12 col-md-6">
              <label className="form-label form-label-sm">Rack</label>
              <select
                className="form-select form-select-sm"
                value={rackId ?? ''}
                onChange={(e) => {
                  const val = e.target.value ? Number(e.target.value) : undefined
                  setRackId(val)
                  setShelfId(undefined)
                }}
              >
                <option value="">—</option>
                {racks
                  .filter((r) => r.storageType === selected.storageType)
                  .map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.rackCode}
                    </option>
                  ))}
              </select>
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label form-label-sm">Shelf</label>
              <select
                className="form-select form-select-sm"
                value={shelfId ?? ''}
                onChange={(e) => setShelfId(e.target.value ? Number(e.target.value) : undefined)}
                disabled={!rackId}
              >
                <option value="">—</option>
                {shelves.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.shelfCode}
                  </option>
                ))}
              </select>
            </div>
          </>
        )}
      </div>
      <div className="modal-footer mt-3">
        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary btn-sm" disabled={saving || !selectedId}>
          {saving ? 'Saving…' : 'Add Batch'}
        </button>
      </div>
    </form>
  )
}
