import { useState, useEffect } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import { PharmacyCardLayout } from './shared/PharmacyCardLayout'
import { usePharmacyPermissions } from '../../hooks/usePharmacyPermissions'
import type { RackResponse, RackRequest, ShelfRequest, RackInventory } from '../../types/pharmacy'

const LOCATION_AREAS = [
  { value: 'MAIN_STORE', label: 'Main Store' },
  { value: 'ICU_STORE', label: 'ICU Store' },
  { value: 'COLD_ROOM', label: 'Cold Room' },
]

const STORAGE_TYPES = [
  { value: 'ROOM_TEMP', label: 'Room Temperature' },
  { value: 'COLD_CHAIN', label: 'Cold Chain' },
]

export function RackManagementCard() {
  const [racks, setRacks] = useState<RackResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showAddRack, setShowAddRack] = useState(false)
  const [showAddShelfFor, setShowAddShelfFor] = useState<RackResponse | null>(null)
  const [inventory, setInventory] = useState<RackInventory | null>(null)
  const { canManageMedicineMaster } = usePharmacyPermissions()

  const loadRacks = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await pharmacyApi.listRacks(true)
      setRacks(data)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load racks.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadRacks()
  }, [])

  const handleViewInventory = async (rack: RackResponse) => {
    setError('')
    try {
      const data = await pharmacyApi.getRackInventory(rack.id)
      setInventory(data)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load inventory.'))
    }
  }

  const handleCloseInventory = () => setInventory(null)

  const headerAction = canManageMedicineMaster ? (
    <button type="button" className="btn btn-primary btn-sm" onClick={() => setShowAddRack(true)}>
      Add Rack
    </button>
  ) : undefined

  return (
    <>
      <PharmacyCardLayout
        title="Rack Management"
        description="Organize medicines by rack, shelf, and storage type. Cold chain validation enforced."
        headerAction={headerAction}
        error={error || undefined}
        loading={loading}
        empty={!loading && racks.length === 0}
        emptyMessage="No racks defined. Add a rack to organize medicines."
      >
        {inventory ? (
          <RackInventoryView inventory={inventory} onClose={handleCloseInventory} />
        ) : (
          <div className="table-responsive" style={{ maxHeight: 420, overflowY: 'auto' }}>
            <table className="table table-bordered table-hover table-sm align-middle">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Name</th>
                  <th>Location</th>
                  <th>Storage</th>
                  <th>Shelves</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {racks.map((r) => (
                  <tr key={r.id}>
                    <td><code>{r.rackCode}</code></td>
                    <td>{r.rackName}</td>
                    <td>{r.locationArea?.replace(/_/g, ' ')}</td>
                    <td>
                      <span className={r.storageType === 'COLD_CHAIN' ? 'text-primary' : ''}>
                        {r.storageType === 'COLD_CHAIN' ? 'Cold' : 'Room Temp'}
                      </span>
                    </td>
                    <td>{r.shelves?.length ?? 0}</td>
                    <td>
                      {canManageMedicineMaster && (
                        <button type="button" className="btn btn-outline-secondary btn-sm me-1" onClick={() => setShowAddShelfFor(r)}>
                          Add Shelf
                        </button>
                      )}
                      <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => handleViewInventory(r)}>
                        View Inventory
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </PharmacyCardLayout>

      {showAddRack && canManageMedicineMaster && (
        <AddRackModal
          onClose={() => setShowAddRack(false)}
          onSuccess={() => { setShowAddRack(false); void loadRacks() }}
        />
      )}
      {showAddShelfFor && canManageMedicineMaster && (
        <AddShelfModal
          rack={showAddShelfFor}
          onClose={() => setShowAddShelfFor(null)}
          onSuccess={() => { setShowAddShelfFor(null); void loadRacks() }}
        />
      )}
    </>
  )
}

function RackInventoryView({ inventory, onClose }: { inventory: RackInventory; onClose: () => void }) {
  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h6 className="mb-0">
          {inventory.rackName} ({inventory.rackCode}) — {inventory.storageType.replace(/_/g, ' ')}
        </h6>
        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onClose}>
          Back
        </button>
      </div>
      <div className="table-responsive" style={{ maxHeight: 360, overflowY: 'auto' }}>
        <table className="table table-bordered table-hover table-sm align-middle">
          <thead>
            <tr>
              <th>Medicine</th>
              <th>Shelf</th>
              <th>Bin</th>
              <th>LASA</th>
              <th>Storage</th>
              <th>Batches</th>
            </tr>
          </thead>
          <tbody>
            {inventory.items.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-muted text-center py-3">
                  No medicines mapped to this rack.
                </td>
              </tr>
            ) : (
              inventory.items.map((item) => (
                <tr key={item.medicineId}>
                  <td>
                    <span className={item.lasa ? 'text-danger fw-bold' : ''}>{item.medicineName}</span>
                  </td>
                  <td>{item.shelfCode ?? '—'}</td>
                  <td>{item.binNumber ?? '—'}</td>
                  <td>{item.lasa ? 'Yes' : 'No'}</td>
                  <td>
                    <span className={item.storageType === 'COLD_CHAIN' ? 'text-primary' : ''}>
                      {item.storageType === 'COLD_CHAIN' ? 'Cold' : 'Room'}
                    </span>
                  </td>
                  <td>{item.batchCount}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function AddShelfModal({ rack, onClose, onSuccess }: { rack: RackResponse; onClose: () => void; onSuccess: () => void }) {
  const [form, setForm] = useState<ShelfRequest>({ shelfCode: '', shelfLevel: 1, active: true })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.shelfCode.trim()) { setError('Shelf code is required.'); return }
    setSaving(true)
    setError('')
    try {
      await pharmacyApi.addShelf(rack.id, form)
      onSuccess()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to add shelf.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Add Shelf to {rack.rackName}</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2 mb-3">{error}</div>}
              <div className="mb-3">
                <label className="form-label">Shelf Code *</label>
                <input type="text" className="form-control" value={form.shelfCode} onChange={(e) => setForm({ ...form, shelfCode: e.target.value })} placeholder="e.g. R-01-S1" required />
              </div>
              <div className="mb-3">
                <label className="form-label">Shelf Level</label>
                <input type="number" min={1} className="form-control" value={form.shelfLevel} onChange={(e) => setForm({ ...form, shelfLevel: Number(e.target.value) || 1 })} />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={onClose} disabled={saving}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Add Shelf'}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

function AddRackModal({ onClose, onSuccess }: { onClose: () => void; onSuccess: () => void }) {
  const [form, setForm] = useState<RackRequest>({ rackCode: '', rackName: '', locationArea: 'MAIN_STORE', storageType: 'ROOM_TEMP', active: true })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.rackCode.trim() || !form.rackName.trim()) { setError('Rack code and name are required.'); return }
    setSaving(true)
    setError('')
    try {
      await pharmacyApi.createRack(form)
      onSuccess()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to add rack.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Add Rack</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2 mb-3">{error}</div>}
              <div className="mb-3">
                <label className="form-label">Rack Code *</label>
                <input type="text" className="form-control" value={form.rackCode} onChange={(e) => setForm({ ...form, rackCode: e.target.value })} placeholder="e.g. R-01" required />
              </div>
              <div className="mb-3">
                <label className="form-label">Rack Name *</label>
                <input type="text" className="form-control" value={form.rackName} onChange={(e) => setForm({ ...form, rackName: e.target.value })} placeholder="e.g. Antibiotic Rack" required />
              </div>
              <div className="mb-3">
                <label className="form-label">Location Area</label>
                <select className="form-select" value={form.locationArea} onChange={(e) => setForm({ ...form, locationArea: e.target.value as RackRequest['locationArea'] })}>
                  {LOCATION_AREAS.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                </select>
              </div>
              <div className="mb-3">
                <label className="form-label">Storage Type</label>
                <select className="form-select" value={form.storageType} onChange={(e) => setForm({ ...form, storageType: e.target.value })}>
                  {STORAGE_TYPES.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={onClose} disabled={saving}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Add Rack'}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
