import { useState, useRef } from 'react'
import { pharmacyApi } from '../../api/pharmacy'
import { getPharmacyErrorMessage } from '../../utils/pharmacyApiError'
import type { MedicineImportResult } from '../../types/pharmacy'

interface ImportMedicineModalProps {
  open: boolean
  onClose: () => void
  onSuccess?: () => void
}

const ACCEPTED_TYPES = '.xlsx'
const MAX_SIZE_MB = 10

type ImportType = 'master' | 'stock'

export function ImportMedicineModal({ open, onClose, onSuccess }: ImportMedicineModalProps) {
  const [importType, setImportType] = useState<ImportType>('master')
  const [file, setFile] = useState<File | null>(null)
  const [importing, setImporting] = useState(false)
  const [downloadingTemplate, setDownloadingTemplate] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState<MedicineImportResult | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  if (!open) return null

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
    setError('')
    setDownloadingTemplate(true)
    try {
      const blob = await pharmacyApi.downloadMedicineTemplate(importType)
      if (!blob || blob.size === 0) {
        setError('Template is empty. Please try again.')
        return
      }
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = importType === 'stock' ? 'stock-import-template.xlsx' : 'medicine-master-import-template.xlsx'
      a.style.display = 'none'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      setTimeout(() => URL.revokeObjectURL(url), 200)
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
      if (res.successCount > 0) {
        onSuccess?.()
      }
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Import failed. Please check the file format and try again.'))
    } finally {
      setImporting(false)
    }
  }

  const handleDownloadErrorReport = async () => {
    if (!result?.errors?.length) return
    try {
      const blob = await pharmacyApi.downloadImportErrorReport(result)
      if (!blob || blob.size === 0) return
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'medicine-import-errors.xlsx'
      a.style.display = 'none'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      setTimeout(() => URL.revokeObjectURL(url), 200)
    } catch (err: unknown) {
      setError(getPharmacyErrorMessage(err, 'Failed to download error report.'))
    }
  }

  const handleClose = () => {
    setFile(null)
    setResult(null)
    setError('')
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
    onClose()
  }

  const showResult = result !== null

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
            <h5 className="modal-title">Import Medicines (Excel)</h5>
            <button
              type="button"
              className="btn-close"
              aria-label="Close"
              onClick={handleClose}
              disabled={importing}
            />
          </div>
          <div className="modal-body">
            {error && <div className="alert alert-danger py-2">{error}</div>}

            {!showResult ? (
              <>
                <div className="mb-3">
                  <label className="form-label fw-semibold">Import Type</label>
                  <div className="d-flex flex-wrap gap-3">
                    <div className="form-check">
                      <input
                        id="import-master"
                        type="radio"
                        className="form-check-input"
                        name="importType"
                        checked={importType === 'master'}
                        onChange={() => {
                          setImportType('master')
                          setFile(null)
                          setResult(null)
                          if (fileInputRef.current) fileInputRef.current.value = ''
                        }}
                      />
                      <label className="form-check-label" htmlFor="import-master">
                        Medicine Master Import
                      </label>
                    </div>
                    <div className="form-check">
                      <input
                        id="import-stock"
                        type="radio"
                        className="form-check-input"
                        name="importType"
                        checked={importType === 'stock'}
                        onChange={() => {
                          setImportType('stock')
                          setFile(null)
                          setResult(null)
                          if (fileInputRef.current) fileInputRef.current.value = ''
                        }}
                      />
                      <label className="form-check-label" htmlFor="import-stock">
                        Stock Import
                      </label>
                    </div>
                  </div>
                </div>
                <div className="mb-3">
                  <label className="form-label form-label-sm">Excel file (.xlsx only)</label>
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept={ACCEPTED_TYPES}
                    onChange={handleFileChange}
                    className="form-control form-control-sm"
                  />
                  {file && (
                    <small className="text-muted d-block mt-1">
                      Selected: {file.name} ({(file.size / 1024).toFixed(1)} KB)
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
                <p className="small text-muted mb-0">
                  {importType === 'master' ? (
                    <>
                      <strong>Medicine Master:</strong> MedicineCode, MedicineName, Category, Strength, Form,
                      MinStock, LASA (Yes/No), StorageType (RoomTemp/ColdChain), Active (true/false). No Quantity.
                    </>
                  ) : (
                    <>
                      <strong>Stock:</strong> MedicineCode, BatchNo, ExpiryDate (YYYY-MM-DD), Quantity (mandatory),
                      CostPrice, Rack. MedicineCode must exist in master.
                    </>
                  )}
                </p>
              </>
            ) : (
              <>
                <div className="alert alert-info py-2 mb-3">
                  <strong>Import complete.</strong> Total rows: {result.totalRows} | Success:{' '}
                  {result.successCount} | Failed: {result.failedCount}
                </div>
                {result.errors && result.errors.length > 0 && (
                  <>
                    <h6 className="mb-2">Failed rows</h6>
                    <div className="table-responsive" style={{ maxHeight: 200 }}>
                      <table className="table table-sm">
                        <thead className="table-light sticky-top">
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
                    <button
                      type="button"
                      className="btn btn-outline-primary btn-sm mt-2"
                      onClick={handleDownloadErrorReport}
                    >
                      Download Error Report (Excel)
                    </button>
                  </>
                )}
              </>
            )}
          </div>
          <div className="modal-footer">
            {showResult ? (
              <button type="button" className="btn btn-primary btn-sm" onClick={handleClose}>
                Close
              </button>
            ) : (
              <>
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm"
                  onClick={handleClose}
                  disabled={importing}
                >
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
        </div>
      </div>
    </div>
  )
}
