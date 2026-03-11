/**
 * Laundry & Linen Dashboard – Issue, return, status.
 */

import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { laundryApi } from '../../api/patientServices'
import type { LinenInventory, LinenType } from '../../api/patientServices'

function formatDateTime(iso: string | undefined): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' })
}

const LINEN_TYPE_LABELS: Record<LinenType, string> = {
  BEDSHEET: 'Bedsheet',
  PILLOW_COVER: 'Pillow Cover',
  BLANKET: 'Blanket',
}

const STATUS_LABELS: Record<string, string> = {
  DIRTY: 'Dirty',
  WASHING: 'Washing',
  READY: 'Ready',
}

export function LaundryDashboard() {
  const [inventory, setInventory] = useState<LinenInventory[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [wardFilter, setWardFilter] = useState('')
  const [showIssueForm, setShowIssueForm] = useState(false)
  const [showReturnForm, setShowReturnForm] = useState(false)
  const [formData, setFormData] = useState({
    wardName: '',
    linenType: 'BEDSHEET' as LinenType,
    quantity: 1,
  })
  const [submitting, setSubmitting] = useState(false)

  const fetchStatus = useCallback(() => {
    setLoading(true)
    setError('')
    laundryApi
      .getStatus(wardFilter || undefined)
      .then(setInventory)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load status')
        setInventory([])
      })
      .finally(() => setLoading(false))
  }, [wardFilter])

  useEffect(() => {
    fetchStatus()
  }, [fetchStatus])

  const wardNames = [...new Set(inventory.map((i) => i.wardName))].sort()

  const handleIssue = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.wardName.trim()) return
    setSubmitting(true)
    setError('')
    try {
      await laundryApi.issue({
        wardName: formData.wardName.trim(),
        linenType: formData.linenType,
        quantity: formData.quantity,
      })
      setShowIssueForm(false)
      setFormData({ wardName: '', linenType: 'BEDSHEET', quantity: 1 })
      fetchStatus()
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } }
      setError(e.response?.data?.message || 'Failed to issue linen')
    } finally {
      setSubmitting(false)
    }
  }

  const handleReturn = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.wardName.trim()) return
    setSubmitting(true)
    setError('')
    try {
      await laundryApi.returnLinen({
        wardName: formData.wardName.trim(),
        linenType: formData.linenType,
        quantity: formData.quantity,
      })
      setShowReturnForm(false)
      setFormData({ wardName: '', linenType: 'BEDSHEET', quantity: 1 })
      fetchStatus()
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } }
      setError(e.response?.data?.message || 'Failed to return linen')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/">Home</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Laundry & Linen
          </li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Laundry & Linen</h1>
        <div className="d-flex gap-2">
          <select
            className="form-select form-select-sm"
            style={{ width: 'auto' }}
            value={wardFilter}
            onChange={(e) => setWardFilter(e.target.value)}
          >
            <option value="">All Wards</option>
            {wardNames.map((w) => (
              <option key={w} value={w}>
                {w}
              </option>
            ))}
          </select>
          <button type="button" className="btn btn-success btn-sm" onClick={() => setShowIssueForm(true)}>
            Issue Linen
          </button>
          <button type="button" className="btn btn-warning btn-sm" onClick={() => setShowReturnForm(true)}>
            Return Linen
          </button>
          <button type="button" className="btn btn-outline-primary btn-sm" onClick={fetchStatus} disabled={loading}>
            Refresh
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {/* Issue form modal */}
      {showIssueForm && (
        <div className="card border shadow">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h6 className="mb-0">Issue Linen to Ward</h6>
            <button type="button" className="btn-close btn-sm" onClick={() => setShowIssueForm(false)} aria-label="Close" />
          </div>
          <form className="card-body" onSubmit={handleIssue}>
            <div className="mb-2">
              <label className="form-label small">Ward Name</label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={formData.wardName}
                onChange={(e) => setFormData((f) => ({ ...f, wardName: e.target.value }))}
                required
              />
            </div>
            <div className="mb-2">
              <label className="form-label small">Linen Type</label>
              <select
                className="form-select form-select-sm"
                value={formData.linenType}
                onChange={(e) => setFormData((f) => ({ ...f, linenType: e.target.value as LinenType }))}
              >
                {Object.entries(LINEN_TYPE_LABELS).map(([v, l]) => (
                  <option key={v} value={v}>
                    {l}
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-2">
              <label className="form-label small">Quantity</label>
              <input
                type="number"
                min={1}
                className="form-control form-control-sm"
                value={formData.quantity}
                onChange={(e) => setFormData((f) => ({ ...f, quantity: parseInt(e.target.value, 10) || 1 }))}
              />
            </div>
            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-success btn-sm" disabled={submitting}>
                {submitting ? '…' : 'Issue'}
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowIssueForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Return form modal */}
      {showReturnForm && (
        <div className="card border shadow">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h6 className="mb-0">Return Linen from Ward</h6>
            <button type="button" className="btn-close btn-sm" onClick={() => setShowReturnForm(false)} aria-label="Close" />
          </div>
          <form className="card-body" onSubmit={handleReturn}>
            <div className="mb-2">
              <label className="form-label small">Ward Name</label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={formData.wardName}
                onChange={(e) => setFormData((f) => ({ ...f, wardName: e.target.value }))}
                required
              />
            </div>
            <div className="mb-2">
              <label className="form-label small">Linen Type</label>
              <select
                className="form-select form-select-sm"
                value={formData.linenType}
                onChange={(e) => setFormData((f) => ({ ...f, linenType: e.target.value as LinenType }))}
              >
                {Object.entries(LINEN_TYPE_LABELS).map(([v, l]) => (
                  <option key={v} value={v}>
                    {l}
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-2">
              <label className="form-label small">Quantity</label>
              <input
                type="number"
                min={1}
                className="form-control form-control-sm"
                value={formData.quantity}
                onChange={(e) => setFormData((f) => ({ ...f, quantity: parseInt(e.target.value, 10) || 1 }))}
              />
            </div>
            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-warning btn-sm" disabled={submitting}>
                {submitting ? '…' : 'Return'}
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowReturnForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Status table */}
      <div className="card border shadow-sm">
        <div className="card-header bg-light py-2">
          <h6 className="mb-0">Linen Status</h6>
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="p-4 text-center text-muted">Loading…</div>
          ) : inventory.length === 0 ? (
            <div className="p-4 text-center text-muted">No linen records.</div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Ward</th>
                    <th>Type</th>
                    <th className="text-end">Issued</th>
                    <th className="text-end">Returned</th>
                    <th>Status</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {inventory.map((i) => (
                    <tr key={i.id}>
                      <td>{i.wardName}</td>
                      <td>{LINEN_TYPE_LABELS[i.linenType]}</td>
                      <td className="text-end">{i.quantityIssued}</td>
                      <td className="text-end">{i.quantityReturned}</td>
                      <td>
                        <span
                          className={`badge ${
                            i.laundryStatus === 'READY'
                              ? 'bg-success'
                              : i.laundryStatus === 'WASHING'
                                ? 'bg-info'
                                : 'bg-secondary'
                          }`}
                        >
                          {STATUS_LABELS[i.laundryStatus] ?? i.laundryStatus}
                        </span>
                      </td>
                      <td className="small">{formatDateTime(i.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
