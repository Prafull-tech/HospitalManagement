import { useState, useEffect, useCallback } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { receptionApi } from '../../api/reception'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import type {
  MedicineResponse,
  SellRequest,
  SellLineItem,
  SaleType,
  PatientIpdStatus,
} from '../../types/pharmacy'
import type { PatientResponse } from '../../types/patient'

interface SellModalProps {
  open: boolean
  onClose: () => void
  onSuccess?: () => void
  preselectedMedicine?: MedicineResponse | null
}

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

export function SellModal({
  open,
  onClose,
  onSuccess,
  preselectedMedicine,
}: SellModalProps) {
  const [medicines, setMedicines] = useState<MedicineResponse[]>([])
  const [saleType, setSaleType] = useState<SaleType>('PATIENT')
  const [lineItems, setLineItems] = useState<SellLineItem[]>([{ medicineId: 0, quantity: 1 }])
  const [form, setForm] = useState<SellRequest>({
    transactionDate: todayStr(),
    saleType: 'PATIENT',
    reference: '',
    notes: '',
  })

  // Linked patient mode
  const [patientSearch, setPatientSearch] = useState('')
  const [patientResults, setPatientResults] = useState<PatientResponse[]>([])
  const [patientSearching, setPatientSearching] = useState(false)
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null)
  const [ipdStatus, setIpdStatus] = useState<PatientIpdStatus | null>(null)
  const [showPatientDropdown, setShowPatientDropdown] = useState(false)

  // Manual mode
  const [manualName, setManualName] = useState('')
  const [manualPhone, setManualPhone] = useState('')
  const [manualEmail, setManualEmail] = useState('')
  const [manualAddress, setManualAddress] = useState('')

  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')
  const [lastInvoice, setLastInvoice] = useState<string | null>(null)

  const loadMedicines = useCallback(() => {
    pharmacyApi.listMedicines().then((data) => {
      setMedicines(data.filter((m) => m.active !== false && (m.quantity ?? 0) > 0))
    })
  }, [])

  useEffect(() => {
    if (open) loadMedicines()
  }, [open, loadMedicines])

  useEffect(() => {
    if (open && preselectedMedicine) {
      const avail = preselectedMedicine.quantity ?? 0
      setLineItems([{ medicineId: preselectedMedicine.id, quantity: avail >= 1 ? 1 : 0 }])
      setForm((f) => ({ ...f, transactionDate: todayStr() }))
    } else if (open && !preselectedMedicine) {
      const first = medicines.find((m) => (m.quantity ?? 0) > 0)
      setLineItems([{ medicineId: first?.id ?? 0, quantity: 1 }])
      setForm((f) => ({ ...f, transactionDate: todayStr() }))
    }
  }, [open, preselectedMedicine, medicines])

  // Debounced patient search
  useEffect(() => {
    if (!open || saleType !== 'PATIENT') return
    const q = patientSearch.trim()
    if (q.length < 2) {
      setPatientResults([])
      setShowPatientDropdown(false)
      return
    }
    const t = setTimeout(() => {
      setPatientSearching(true)
      receptionApi
        .searchQuery(q)
        .then((data) => {
          setPatientResults(data ?? [])
          setShowPatientDropdown(true)
        })
        .catch(() => setPatientResults([]))
        .finally(() => setPatientSearching(false))
    }, 300)
    return () => clearTimeout(t)
  }, [open, saleType, patientSearch])

  // Fetch IPD status when patient selected
  useEffect(() => {
    if (!open || !selectedPatient) {
      setIpdStatus(null)
      return
    }
    pharmacyApi
      .getPatientIpdStatus(selectedPatient.id)
      .then(setIpdStatus)
      .catch(() => setIpdStatus({ ipdAdmissionId: null, admissionNumber: null, wardName: null, bedNumber: null, ipdLinked: false }))
  }, [open, selectedPatient?.id])

  const resetPatientSection = () => {
    setSelectedPatient(null)
    setPatientSearch('')
    setPatientResults([])
    setShowPatientDropdown(false)
    setIpdStatus(null)
    setManualName('')
    setManualPhone('')
    setManualEmail('')
    setManualAddress('')
  }

  const handleSaleTypeChange = (type: SaleType) => {
    setSaleType(type)
    setForm((f) => ({ ...f, saleType: type, patientId: undefined }))
    resetPatientSection()
  }

  const handleSelectPatient = (p: PatientResponse) => {
    setSelectedPatient(p)
    setPatientSearch('')
    setPatientResults([])
    setShowPatientDropdown(false)
    setForm((f) => ({ ...f, patientId: p.id }))
  }

  const handleClearPatient = () => {
    setSelectedPatient(null)
    setForm((f) => ({ ...f, patientId: undefined }))
    setIpdStatus(null)
  }

  const addLineItem = () => {
    const first = medicines.find((m) => (m.quantity ?? 0) > 0)
    setLineItems((prev) => [...prev, { medicineId: first?.id ?? 0, quantity: 1 }])
  }

  const removeLineItem = (index: number) => {
    if (lineItems.length <= 1) return
    setLineItems((prev) => prev.filter((_, i) => i !== index))
  }

  const updateLineItem = (index: number, field: 'medicineId' | 'quantity', value: number) => {
    setLineItems((prev) =>
      prev.map((item, i) =>
        i === index ? { ...item, [field]: value } : item
      )
    )
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const validItems = lineItems.filter((item) => item.medicineId > 0 && item.quantity >= 1)
    if (validItems.length === 0) {
      setError('Add at least one medicine with quantity.')
      return
    }
    for (const item of validItems) {
      const med = medicines.find((m) => m.id === item.medicineId)
      const available = med?.quantity ?? 0
      if (item.quantity > available) {
        setError(`${med?.medicineName ?? 'Medicine'}: quantity cannot exceed available (${available}).`)
        return
      }
    }
    if (saleType === 'PATIENT' && !form.patientId) {
      setError('Please select a patient for linked sale.')
      return
    }
    if (saleType === 'MANUAL' && !manualName.trim()) {
      setError('Patient name is required for walk-in sale.')
      return
    }

    setSaving(true)
    setError('')
    setSuccessMsg('')
    try {
      const payload: SellRequest = {
        lineItems: validItems,
        transactionDate: form.transactionDate,
        saleType,
      }
      if (saleType === 'PATIENT' && form.patientId) {
        payload.patientId = form.patientId
        if (ipdStatus?.admissionNumber) payload.reference = ipdStatus.admissionNumber
      }
      if (saleType === 'MANUAL') {
        payload.manualPatientName = manualName.trim()
        if (manualPhone.trim()) payload.manualPhone = manualPhone.trim()
        if (manualEmail.trim()) payload.manualEmail = manualEmail.trim()
        if (manualAddress.trim()) payload.manualAddress = manualAddress.trim()
      }
      if (form.reference?.trim()) payload.reference = form.reference.trim()
      if (form.notes?.trim()) payload.notes = form.notes.trim()

      const result = await pharmacyApi.sell(payload)
      setSuccessMsg('Sale recorded successfully.')
      onSuccess?.()
      if (result.invoiceNumber) {
        setLastInvoice(result.invoiceNumber)
      } else {
        setTimeout(() => onClose(), 1200)
      }
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to record sale.'))
    } finally {
      setSaving(false)
    }
  }

  const handleClose = () => {
    resetPatientSection()
    setError('')
    setSuccessMsg('')
    setLastInvoice(null)
    onClose()
  }

  const handleDownloadInvoice = () => {
    if (lastInvoice) {
      pharmacyApi.downloadInvoice(lastInvoice).catch(() => setError('Failed to download invoice.'))
    }
  }

  if (!open) return null

  const validItems = lineItems.filter((item) => item.medicineId > 0 && item.quantity >= 1)
  const allValid = validItems.length > 0 && validItems.every((item) => {
    const med = medicines.find((m) => m.id === item.medicineId)
    return (med?.quantity ?? 0) >= item.quantity
  })
  const canSubmit =
    !saving &&
    allValid &&
    (saleType === 'MANUAL' ? manualName.trim().length > 0 : !!form.patientId)

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Stock Out (Sell)</h5>
            <button type="button" className="btn-close" onClick={handleClose} aria-label="Close" />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2">{error}</div>}
              {successMsg && (
                <div className="alert alert-success py-2 d-flex align-items-center justify-content-between flex-wrap gap-2">
                  <span>{successMsg}</span>
                  {lastInvoice && (
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-success"
                      onClick={handleDownloadInvoice}
                    >
                      Download Invoice
                    </button>
                  )}
                </div>
              )}

              {/* SECTION 1: Sale Type Toggle */}
              <div className="mb-4">
                <label className="form-label fw-semibold">Sale Type</label>
                <div className="d-flex gap-3">
                  <label className="d-flex align-items-center gap-2">
                    <input
                      type="radio"
                      name="saleType"
                      checked={saleType === 'PATIENT'}
                      onChange={() => handleSaleTypeChange('PATIENT')}
                    />
                    Linked to Patient (OPD/IPD)
                  </label>
                  <label className="d-flex align-items-center gap-2">
                    <input
                      type="radio"
                      name="saleType"
                      checked={saleType === 'MANUAL'}
                      onChange={() => handleSaleTypeChange('MANUAL')}
                    />
                    Manual / Walk-in Sale
                  </label>
                </div>
              </div>

              {/* SECTION 2: Linked Patient Mode */}
              {saleType === 'PATIENT' && (
                <div className="mb-4">
                  <label className="form-label">Search Patient (UHID / IPD / Name / Phone)</label>
                  <div className="position-relative">
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Type to search..."
                      value={patientSearch}
                      onChange={(e) => setPatientSearch(e.target.value)}
                      onFocus={() => patientResults.length > 0 && setShowPatientDropdown(true)}
                      disabled={!!selectedPatient}
                    />
                    {patientSearching && (
                      <span className="position-absolute end-0 top-50 translate-middle-y me-2 small text-muted">
                        Searching...
                      </span>
                    )}
                    {selectedPatient && (
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-secondary position-absolute end-0 top-50 translate-middle-y me-2"
                        onClick={handleClearPatient}
                      >
                        Clear
                      </button>
                    )}
                    {showPatientDropdown && patientResults.length > 0 && !selectedPatient && (
                      <ul
                        className="list-group position-absolute w-100 mt-1 shadow"
                        style={{ zIndex: 1050, maxHeight: 200, overflowY: 'auto' }}
                      >
                        {patientResults.map((p) => (
                          <li
                            key={p.id}
                            className="list-group-item list-group-item-action"
                            style={{ cursor: 'pointer' }}
                            onClick={() => handleSelectPatient(p)}
                          >
                            <div className="d-flex justify-content-between">
                              <span>
                                {p.fullName} • UHID: {p.uhid}
                              </span>
                              {p.phone && <span className="text-muted">{p.phone}</span>}
                            </div>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>

                  {selectedPatient && (
                    <div className="card mt-2 border">
                      <div className="card-body py-2">
                        <div className="d-flex align-items-center gap-2 mb-2">
                          <span className="fw-semibold">{selectedPatient.fullName}</span>
                          {ipdStatus?.ipdLinked ? (
                            <span className="badge bg-info">IPD Linked</span>
                          ) : (
                            <span className="badge bg-secondary">OPD</span>
                          )}
                        </div>
                        <div className="row g-2 small">
                          <div className="col-md-6">
                            <span className="text-muted">Phone:</span>{' '}
                            {selectedPatient.phone || '—'}
                          </div>
                          <div className="col-md-6">
                            <span className="text-muted">Email:</span> — (not in system)
                          </div>
                          {ipdStatus?.ipdLinked && (
                            <>
                              <div className="col-md-6">
                                <span className="text-muted">IPD No:</span>{' '}
                                {ipdStatus.admissionNumber || '—'}
                              </div>
                              <div className="col-md-6">
                                <span className="text-muted">Ward / Bed:</span>{' '}
                                {[ipdStatus.wardName, ipdStatus.bedNumber].filter(Boolean).join(' / ') || '—'}
                              </div>
                            </>
                          )}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* SECTION 3: Manual Mode */}
              {saleType === 'MANUAL' && (
                <div className="mb-4">
                  <div className="row g-2">
                    <div className="col-md-6">
                      <label className="form-label">Patient Name <span className="text-danger">*</span></label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="Required"
                        value={manualName}
                        onChange={(e) => setManualName(e.target.value)}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label">Phone</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="Optional"
                        value={manualPhone}
                        onChange={(e) => setManualPhone(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label">Email</label>
                      <input
                        type="email"
                        className="form-control"
                        placeholder="Optional"
                        value={manualEmail}
                        onChange={(e) => setManualEmail(e.target.value)}
                      />
                    </div>
                    <div className="col-12">
                      <label className="form-label">Address</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="Optional"
                        value={manualAddress}
                        onChange={(e) => setManualAddress(e.target.value)}
                      />
                    </div>
                  </div>
                </div>
              )}

              {/* SECTION 4: Medicine - Multi-item */}
              <div className="mb-3">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <label className="form-label mb-0">Medicine</label>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-primary"
                    onClick={addLineItem}
                  >
                    + Add Medicine
                  </button>
                </div>
                {lineItems.map((item, idx) => {
                  const med = medicines.find((m) => m.id === item.medicineId)
                  const avail = med?.quantity ?? 0
                  const maxQ = Math.max(0, avail)
                  return (
                    <div key={idx} className="d-flex gap-2 align-items-end mb-2">
                      <div className="flex-grow-1">
                        <select
                          className="form-select form-select-sm"
                          value={item.medicineId || ''}
                          onChange={(e) => {
                            const id = Number(e.target.value)
                            const m = medicines.find((x) => x.id === id)
                            updateLineItem(idx, 'medicineId', id)
                            updateLineItem(idx, 'quantity', Math.min(item.quantity, m?.quantity ?? 0) || 1)
                          }}
                        >
                          <option value="">Select medicine</option>
                          {medicines.map((m) => (
                            <option key={m.id} value={m.id}>
                              {m.medicineName} – Available: {m.quantity ?? 0}
                            </option>
                          ))}
                        </select>
                        {med && (
                          <small className="text-muted">
                            Stock: {avail} → after: {Math.max(0, avail - item.quantity)}
                          </small>
                        )}
                      </div>
                      <div style={{ width: 90 }}>
                        <input
                          type="number"
                          className="form-control form-control-sm"
                          min={1}
                          max={maxQ}
                          value={item.quantity}
                          onChange={(e) =>
                            updateLineItem(idx, 'quantity', Math.min(maxQ, Math.max(1, Number(e.target.value) || 0)))
                          }
                          placeholder="Qty"
                        />
                      </div>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => removeLineItem(idx)}
                        disabled={lineItems.length <= 1}
                        title="Remove"
                        aria-label="Remove"
                      >
                        ×
                      </button>
                    </div>
                  )
                })}
              </div>

              <div className="row g-2 mb-3">
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

              <div className="mb-2">
                <label className="form-label">Reference (e.g. indent/IPD no)</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Optional"
                  value={form.reference ?? ''}
                  onChange={(e) => setForm((f) => ({ ...f, reference: e.target.value }))}
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
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={handleClose}>
                {lastInvoice ? 'Close' : 'Cancel'}
              </button>
              {!lastInvoice && (
                <button type="submit" className="btn btn-primary" disabled={!canSubmit}>
                  {saving ? 'Saving…' : 'Record Sale'}
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
