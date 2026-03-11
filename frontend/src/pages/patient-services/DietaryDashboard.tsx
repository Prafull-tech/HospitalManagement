/**
 * Dietary / Kitchen Dashboard – Active diet plans, meal preparation queue.
 */

import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { dietaryApi } from '../../api/patientServices'
import type { DietPlan, DietType } from '../../api/patientServices'

function formatDateTime(iso: string | undefined): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' })
}

const DIET_TYPE_LABELS: Record<DietType, string> = {
  NORMAL: 'Normal',
  SOFT: 'Soft',
  LIQUID: 'Liquid',
  DIABETIC: 'Diabetic',
}

export function DietaryDashboard() {
  const [plans, setPlans] = useState<DietPlan[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activeOnly, setActiveOnly] = useState(true)

  const fetchPlans = useCallback(() => {
    setLoading(true)
    setError('')
    dietaryApi
      .listPlans(activeOnly)
      .then(setPlans)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load diet plans')
        setPlans([])
      })
      .finally(() => setLoading(false))
  }, [activeOnly])

  useEffect(() => {
    fetchPlans()
  }, [fetchPlans])

  const activePlans = plans.filter((p) => p.active)
  const inactivePlans = plans.filter((p) => !p.active)

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/">Home</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Dietary / Kitchen
          </li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Dietary / Kitchen</h1>
        <div className="d-flex gap-2">
          <div className="form-check form-switch">
            <input
              className="form-check-input"
              type="checkbox"
              id="activeOnly"
              checked={activeOnly}
              onChange={(e) => setActiveOnly(e.target.checked)}
            />
            <label className="form-check-label small" htmlFor="activeOnly">
              Active only
            </label>
          </div>
          <button type="button" className="btn btn-outline-primary btn-sm" onClick={fetchPlans} disabled={loading}>
            Refresh
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {/* Summary */}
      <div className="row g-2">
        <div className="col-6 col-md-4">
          <div className="card border border-success shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-success small mb-0">Active Diet Plans</p>
              <p className="fw-bold mb-0">{activePlans.length}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-4">
          <div className="card border border-secondary shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-secondary small mb-0">Inactive</p>
              <p className="fw-bold mb-0">{inactivePlans.length}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Diet plans table */}
      <div className="card border shadow-sm">
        <div className="card-header bg-light py-2">
          <h6 className="mb-0">Diet Plans & Restrictions</h6>
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="p-4 text-center text-muted">Loading…</div>
          ) : plans.length === 0 ? (
            <div className="p-4 text-center text-muted">No diet plans found.</div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Patient ID</th>
                    <th>IPD Admission</th>
                    <th>Diet Type</th>
                    <th>Meal Schedule</th>
                    <th>Doctor</th>
                    <th>Status</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {plans.map((p) => (
                    <tr key={p.id}>
                      <td>
                        <Link to={`/reception/search?patientId=${p.patientId}`}>{p.patientId}</Link>
                      </td>
                      <td>
                        <Link to={`/ipd/admissions/${p.ipdAdmissionId}`}>{p.ipdAdmissionId}</Link>
                      </td>
                      <td>
                        <span className="badge bg-info">{DIET_TYPE_LABELS[p.dietType] ?? p.dietType}</span>
                      </td>
                      <td className="small">{p.mealSchedule ?? '—'}</td>
                      <td className="small">{p.createdByDoctor ?? '—'}</td>
                      <td>
                        <span className={`badge ${p.active ? 'bg-success' : 'bg-secondary'}`}>
                          {p.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="small">{formatDateTime(p.createdAt)}</td>
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
