import { useEffect, useState } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import { PharmacyCardLayout } from './shared/PharmacyCardLayout'
import { PharmacyBadge } from './shared/PharmacyBadge'
import type { IpdIssueQueueItem, IssueQueuePatient } from '../../types/pharmacy'

type QueueMode = 'medication' | 'legacy'

export function IpdIssueQueueCard() {
  const [mode, setMode] = useState<QueueMode>('medication')
  const [medicationItems, setMedicationItems] = useState<IssueQueuePatient[]>([])
  const [legacyItems, setLegacyItems] = useState<IpdIssueQueueItem[]>([])
  const [selectedPatientIdx, setSelectedPatientIdx] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const load = async (q?: string) => {
    setLoading(true)
    setError('')
    try {
      const [medData, legacyData] = await Promise.all([
        pharmacyApi.getMedicationIssueQueue(q ? { q } : undefined),
        pharmacyApi.getIpdIssueQueue(q ? { q } : undefined),
      ])
      setMedicationItems(medData)
      setLegacyItems(legacyData)
      if (medData.length > 0) {
        setMode('medication')
        if (selectedPatientIdx === null || selectedPatientIdx >= medData.length) {
          setSelectedPatientIdx(0)
        }
      } else if (legacyData.length > 0) {
        setMode('legacy')
        setSelectedPatientIdx(0)
      } else {
        setSelectedPatientIdx(null)
      }
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to load issue queue.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const selectedMedication = medicationItems[selectedPatientIdx ?? -1] ?? null
  const selectedLegacy = legacyItems[selectedPatientIdx ?? -1] ?? null
  const hasItems = medicationItems.length > 0 || legacyItems.length > 0

  const handleIssueMedication = async () => {
    if (!selectedMedication || selectedMedication.orderIds.length === 0) return
    setError('')
    setMessage('')
    try {
      await pharmacyApi.issueMedications(selectedMedication.orderIds)
      setMessage('Medicines issued.')
      await load()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Issue failed.'))
    }
  }

  const handleIssueLegacy = async (type: 'FULL' | 'PARTIAL') => {
    if (!selectedLegacy) return
    setError('')
    setMessage('')
    try {
      if (type === 'FULL') {
        await pharmacyApi.issueIndent(selectedLegacy.indentId)
      } else {
        await pharmacyApi.issueIndentPartial(selectedLegacy.indentId)
      }
      setMessage(type === 'FULL' ? 'Medicines issued.' : 'Partial issue recorded.')
      await load()
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Issue failed.'))
    }
  }

  return (
    <PharmacyCardLayout
      title="Pharmacy Medicine Issue Queue"
      description="ICU / Emergency first. FEFO batch suggestion; pharmacist confirms. IPD, OPD, Emergency."
      error={error || undefined}
      loading={loading}
      empty={!loading && !hasItems}
      emptyMessage="No pending medication orders."
    >
      {message && <div className="alert alert-success py-2 mb-3">{message}</div>}
      <div className="row g-3">
        <div className="col-12 col-md-4">
          <input
            type="text"
            className="form-control form-control-sm mb-2"
            placeholder="Search patient, UHID, IPD #, OPD…"
            onChange={(e) => {
              const q = e.target.value.trim()
              void load(q || undefined)
            }}
          />
          <div
            className="border rounded"
            style={{ maxHeight: 360, overflowY: 'auto' }}
          >
            {mode === 'medication' &&
              medicationItems.map((i, idx) => (
                <button
                  key={`${i.patientName}-${i.ipdNo ?? i.opdVisitNo ?? idx}`}
                  type="button"
                  className={`w-100 text-start px-3 py-2 border-0 ${
                    selectedPatientIdx === idx ? 'bg-primary bg-opacity-10' : 'bg-white'
                  }`}
                  onClick={() => setSelectedPatientIdx(idx)}
                >
                  <div className="d-flex justify-content-between">
                    <span className="fw-semibold small">
                      {i.ipdNo ?? i.opdVisitNo ?? '—'} · {i.patientName}
                    </span>
                    <PharmacyBadge
                      type={i.priority === 'HIGH' ? 'EMERGENCY' : 'PRIORITY'}
                      label={i.priority}
                    />
                  </div>
                  <div className="small text-muted">
                    {i.wardType} / {i.bed ?? '—'} · {i.medicines.length} medicines
                  </div>
                </button>
              ))}
            {mode === 'legacy' &&
              legacyItems.map((i, idx) => (
                <button
                  key={i.indentId}
                  type="button"
                  className={`w-100 text-start px-3 py-2 border-0 ${
                    selectedPatientIdx === idx ? 'bg-primary bg-opacity-10' : 'bg-white'
                  }`}
                  onClick={() => setSelectedPatientIdx(idx)}
                >
                  <div className="d-flex justify-content-between">
                    <span className="fw-semibold small">
                      {i.ipdAdmissionNumber} · {i.patientName}
                    </span>
                    <PharmacyBadge
                      type={i.priority === 'ICU' || i.priority === 'EMERGENCY' ? 'EMERGENCY' : 'PRIORITY'}
                      label={i.priority}
                    />
                  </div>
                  <div className="small text-muted">
                    {i.wardName} / {i.bedNumber} · {i.medicineCount} medicines · {i.waitingMinutes} min
                  </div>
                </button>
              ))}
          </div>
        </div>

        <div className="col-12 col-md-8">
          {!hasItems && (
            <p className="small text-muted mb-0">No pending orders. Select from queue when available.</p>
          )}
          {selectedMedication && (
            <>
              <div className="mb-2">
                <div className="fw-semibold">
                  {selectedMedication.patientName}
                  {selectedMedication.uhid && <span className="text-muted ms-2">({selectedMedication.uhid})</span>}
                </div>
                <div className="small text-muted">
                  {selectedMedication.ipdNo ? `IPD: ${selectedMedication.ipdNo}` : ''}
                  {selectedMedication.opdVisitNo ? `OPD: ${selectedMedication.opdVisitNo}` : ''}
                  {' · '}
                  {selectedMedication.wardType} / {selectedMedication.bed ?? '—'}
                  {' · '}
                  <PharmacyBadge
                    type={selectedMedication.priority === 'HIGH' ? 'EMERGENCY' : 'PRIORITY'}
                    label={selectedMedication.priority}
                  />
                </div>
              </div>
              <div className="table-responsive" style={{ maxHeight: 360, overflowY: 'auto' }}>
                <table className="table table-bordered table-hover table-sm align-middle">
                  <thead>
                    <tr>
                      <th>Medicine</th>
                      <th>Dosage</th>
                      <th>Route</th>
                      <th className="text-end">Qty</th>
                      <th>FEFO Suggestion</th>
                      <th>LASA</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedMedication.medicines.map((m) => (
                      <tr key={m.orderId}>
                        <td>{m.medicineName}</td>
                        <td>{m.dosage ?? '—'}</td>
                        <td>{m.route ?? '—'}</td>
                        <td className="text-end">{m.quantity}</td>
                        <td className="small">
                          {m.fefoSuggestion?.batchNo ? (
                            <span>
                              {m.fefoSuggestion.batchNo}
                              {m.fefoSuggestion.expiryDate && (
                                <span className="text-muted"> (Exp: {m.fefoSuggestion.expiryDate})</span>
                              )}
                              {m.fefoSuggestion.rackLocation && (
                                <span className="text-muted"> · {m.fefoSuggestion.rackLocation}</span>
                              )}
                            </span>
                          ) : (
                            '—'
                          )}
                        </td>
                        <td>{m.lasa && <PharmacyBadge type="LASA" />}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="d-flex gap-2">
                <button
                  type="button"
                  className="btn btn-success btn-sm"
                  onClick={() => void handleIssueMedication()}
                >
                  Issue
                </button>
              </div>
            </>
          )}
          {selectedLegacy && !selectedMedication && (
            <>
              <div className="mb-2">
                <div className="fw-semibold">
                  {selectedLegacy.patientName} · {selectedLegacy.ipdAdmissionNumber}
                </div>
                <div className="small text-muted">
                  {selectedLegacy.wardName} / {selectedLegacy.bedNumber} · Ordered at {selectedLegacy.orderedAtDisplay}
                </div>
              </div>
              <div className="table-responsive" style={{ maxHeight: 360, overflowY: 'auto' }}>
                <table className="table table-bordered table-hover table-sm align-middle">
                  <thead>
                    <tr>
                      <th>Medicine</th>
                      <th>Location</th>
                      <th className="text-end">Requested</th>
                      <th className="text-end">Available</th>
                      <th>FEFO batch</th>
                      <th>Expiry</th>
                      <th>LASA</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedLegacy.lines.map((l) => (
                      <tr key={l.medicineCode}>
                        <td>{l.medicineName}</td>
                        <td className="small">
                          {(l.rackCode || l.shelfCode || l.binNumber) ? (
                            <span className="text-muted">
                              {l.rackCode && <>Rack: {l.rackCode}</>}
                              {l.shelfCode && <> | Shelf: {l.shelfCode}</>}
                              {l.binNumber && <> | Bin: {l.binNumber}</>}
                            </span>
                          ) : (
                            '—'
                          )}
                        </td>
                        <td className="text-end">{l.requestedQty}</td>
                        <td className="text-end">{l.availableQty}</td>
                        <td>{l.nextBatchNumber ?? '—'}</td>
                        <td className={l.expiryRiskClass}>{l.nextBatchExpiryDisplay ?? '—'}</td>
                        <td>{l.lasa && <PharmacyBadge type="LASA" />}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="d-flex gap-2">
                <button
                  type="button"
                  className="btn btn-success btn-sm"
                  onClick={() => void handleIssueLegacy('FULL')}
                >
                  One-click issue (FEFO)
                </button>
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() => void handleIssueLegacy('PARTIAL')}
                >
                  Mark partial issue
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </PharmacyCardLayout>
  )
}
