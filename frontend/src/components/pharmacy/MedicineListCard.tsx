import { useCallback, useEffect, useMemo, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import { PharmacyCardLayout } from './shared/PharmacyCardLayout'
import { PharmacyBadge } from './shared/PharmacyBadge'
import type { MedicineResponse } from '../../types/pharmacy'

interface MedicineListCardProps {
  refetchTrigger?: number
  canEdit?: boolean
  canDisable?: boolean
  canPurchase?: boolean
  canSell?: boolean
  onEditMedicine?: (medicine: MedicineResponse) => void
  onPurchaseMedicine?: (medicine: MedicineResponse) => void
  onSellMedicine?: (medicine: MedicineResponse) => void
}

const CATEGORIES = ['', 'ANTIBIOTIC', 'ANALGESIC', 'CARDIAC', 'DIABETIC', 'IV_FLUID', 'ICU_EMERGENCY', 'OTHER']
const STORAGE_TYPES = ['', 'ROOM_TEMP', 'COLD_CHAIN']
const ACTIVE_FILTERS = [
  { value: '', label: 'All' },
  { value: 'true', label: 'Active only' },
  { value: 'false', label: 'Inactive only' },
]
const STOCK_FILTERS = [
  { value: '', label: 'All stock' },
  { value: 'out', label: 'Out of stock' },
  { value: 'low', label: 'Low stock' },
  { value: 'ok', label: 'In stock' },
]

export function MedicineListCard({
  refetchTrigger = 0,
  canEdit = false,
  canDisable = false,
  canPurchase = false,
  canSell = false,
  onEditMedicine,
  onPurchaseMedicine,
  onSellMedicine,
}: MedicineListCardProps) {
  const [list, setList] = useState<MedicineResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filterSearch, setFilterSearch] = useState('')
  const [filterCategory, setFilterCategory] = useState('')
  const [filterStorage, setFilterStorage] = useState('')
  const [filterActive, setFilterActive] = useState('')
  const [filterStock, setFilterStock] = useState('')
  const [disablingId, setDisablingId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await pharmacyApi.listMedicines()
      setList(data)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load medicine list.'))
      setList([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load, refetchTrigger])

  const filteredList = useMemo(() => {
    return list.filter((m) => {
      if (filterSearch) {
        const q = filterSearch.toLowerCase()
        if (!m.medicineCode?.toLowerCase().includes(q) && !m.medicineName?.toLowerCase().includes(q)) return false
      }
      if (filterCategory && m.category !== filterCategory) return false
      if (filterStorage && m.storageType !== filterStorage) return false
      if (filterActive === 'true' && m.active === false) return false
      if (filterActive === 'false' && m.active !== false) return false
      if (filterStock) {
        const qty = m.quantity ?? 0
        const min = m.minStock ?? 0
        if (filterStock === 'out' && qty > 0) return false
        if (filterStock === 'low' && (qty >= min || qty === 0)) return false
        if (filterStock === 'ok' && qty < min) return false
      }
      return true
    })
  }, [list, filterSearch, filterCategory, filterStorage, filterActive, filterStock])

  const handleDisable = async (m: MedicineResponse) => {
    if (!canDisable || !window.confirm(`Disable medicine "${m.medicineName}" (${m.medicineCode})? It will no longer appear in active lists.`)) {
      return
    }
    setDisablingId(m.id)
    setError('')
    try {
      await pharmacyApi.disableMedicine(m.id)
      await load()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to disable medicine.'))
    } finally {
      setDisablingId(null)
    }
  }

  const qtyClass = (qty: number, min: number) => {
    if (qty === 0) return 'text-danger'
    if (qty < min) return 'text-warning'
    return ''
  }

  const locationDisplay = (m: MedicineResponse) => {
    const parts = [m.rackCode, m.shelfCode, m.binNumber ? `Bin ${m.binNumber}` : null].filter(Boolean)
    return parts.length ? parts.join(' | ') : '—'
  }

  return (
    <PharmacyCardLayout
      title="Medicine List"
      description="All medicines in the master. New entries appear here after you add them."
      error={error || undefined}
      loading={loading}
      empty={!loading && filteredList.length === 0}
      emptyMessage={list.length === 0 ? 'No medicines yet. Use "Add Medicine" to add one.' : 'No medicines match the current filters.'}
    >
      <div className="row g-2 mb-3">
        <div className="col-12 col-md-3">
          <input
            type="text"
            className="form-control form-control-sm"
            placeholder="Search code or name…"
            value={filterSearch}
            onChange={(e) => setFilterSearch(e.target.value)}
          />
        </div>
        <div className="col-6 col-md-2">
          <select className="form-select form-select-sm" value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)}>
            <option value="">All categories</option>
            {CATEGORIES.filter(Boolean).map((c) => (
              <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>
            ))}
          </select>
        </div>
        <div className="col-6 col-md-2">
          <select className="form-select form-select-sm" value={filterStorage} onChange={(e) => setFilterStorage(e.target.value)}>
            <option value="">All storage</option>
            {STORAGE_TYPES.filter(Boolean).map((s) => (
              <option key={s} value={s}>{s === 'ROOM_TEMP' ? 'Room temp' : 'Cold chain'}</option>
            ))}
          </select>
        </div>
        <div className="col-6 col-md-2">
          <select className="form-select form-select-sm" value={filterActive} onChange={(e) => setFilterActive(e.target.value)}>
            {ACTIVE_FILTERS.map((f) => (
              <option key={f.value} value={f.value}>{f.label}</option>
            ))}
          </select>
        </div>
        <div className="col-6 col-md-2">
          <select className="form-select form-select-sm" value={filterStock} onChange={(e) => setFilterStock(e.target.value)}>
            {STOCK_FILTERS.map((f) => (
              <option key={f.value} value={f.value}>{f.label}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="table-responsive" style={{ maxHeight: 420, overflowY: 'auto' }}>
        <table className="table table-bordered table-hover table-sm align-middle">
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Category</th>
              <th>Strength</th>
              <th>Form</th>
              <th className="text-end">Min stock</th>
              <th className="text-end">Quantity</th>
              <th>Storage</th>
              <th>Rack / Shelf</th>
              <th>LASA</th>
              <th>Active</th>
              {(canEdit || canDisable || canPurchase || canSell) && <th className="text-end">Actions</th>}
            </tr>
          </thead>
          <tbody>
            {filteredList.map((m) => (
              <tr key={m.id}>
                <td className="fw-semibold">{m.medicineCode}</td>
                <td>{m.medicineName}</td>
                <td className="small">{m.category?.replace(/_/g, ' ') ?? '—'}</td>
                <td className="small">{m.strength || '—'}</td>
                <td className="small">{m.form?.replace(/_/g, ' ') ?? '—'}</td>
                <td className="text-end">{m.minStock ?? 0}</td>
                <td className={`text-end ${qtyClass(m.quantity ?? 0, m.minStock ?? 0)}`}>
                  {m.quantity ?? 0}
                </td>
                <td className="small">{m.storageType?.replace(/_/g, ' ') ?? '—'}</td>
                <td className="small text-muted">{locationDisplay(m)}</td>
                <td>{m.lasaFlag ? <PharmacyBadge type="LASA" /> : '—'}</td>
                <td>{m.active !== false ? 'Yes' : 'No'}</td>
                {(canEdit || canDisable || canPurchase || canSell) && (
                  <td className="text-end">
                    <div className="btn-group btn-group-sm">
                      {canPurchase && (
                        <button type="button" className="btn btn-outline-success btn-sm" onClick={() => onPurchaseMedicine?.(m)} title="Purchase (Stock In)">
                          Purchase
                        </button>
                      )}
                      {canSell && (m.quantity ?? 0) > 0 && (
                        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => onSellMedicine?.(m)} title="Sell (Stock Out)">
                          Sell
                        </button>
                      )}
                      {canEdit && (
                        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => onEditMedicine?.(m)} title="Edit">
                          Edit
                        </button>
                      )}
                      {canDisable && m.active !== false && (
                        <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => handleDisable(m)} disabled={disablingId === m.id} title="Disable">
                          {disablingId === m.id ? '…' : 'Disable'}
                        </button>
                      )}
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </PharmacyCardLayout>
  )
}
