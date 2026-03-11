/**
 * Patient Meals Dashboard – Today's meals, serve tracking.
 */

import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { mealsApi } from '../../api/patientServices'
import type { PatientMeal, MealType } from '../../api/patientServices'

function formatDateTime(iso: string | undefined): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' })
}

const MEAL_TYPE_LABELS: Record<MealType, string> = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
}

export function MealServiceDashboard() {
  const [meals, setMeals] = useState<PatientMeal[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  const fetchMeals = useCallback(() => {
    setLoading(true)
    setError('')
    mealsApi
      .listToday()
      .then(setMeals)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load meals')
        setMeals([])
      })
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    fetchMeals()
  }, [fetchMeals])

  const handleServe = async (id: number) => {
    setActionLoading(id)
    setError('')
    try {
      await mealsApi.serveMeal(id)
      fetchMeals()
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } }
      setError(e.response?.data?.message || 'Failed to mark meal served')
    } finally {
      setActionLoading(null)
    }
  }

  const pending = meals.filter((m) => m.status === 'PENDING')
  const served = meals.filter((m) => m.status === 'SERVED')

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/">Home</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Patient Meals
          </li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Patient Meals — Today</h1>
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={fetchMeals} disabled={loading}>
          Refresh
        </button>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {/* Summary */}
      <div className="row g-2">
        <div className="col-6 col-md-4">
          <div className="card border border-warning shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-warning small mb-0">Pending</p>
              <p className="fw-bold mb-0">{pending.length}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-4">
          <div className="card border border-success shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-success small mb-0">Served</p>
              <p className="fw-bold mb-0">{served.length}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Meals table */}
      <div className="card border shadow-sm">
        <div className="card-header bg-light py-2">
          <h6 className="mb-0">Today&apos;s Meal Queue</h6>
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="p-4 text-center text-muted">Loading…</div>
          ) : meals.length === 0 ? (
            <div className="p-4 text-center text-muted">No meals for today.</div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Patient ID</th>
                    <th>IPD Admission</th>
                    <th>Meal Type</th>
                    <th>Diet</th>
                    <th>Status</th>
                    <th>Delivered By</th>
                    <th>Delivered At</th>
                    <th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {meals.map((m) => (
                    <tr key={m.id}>
                      <td>
                        <Link to={`/reception/search?patientId=${m.patientId}`}>{m.patientId}</Link>
                      </td>
                      <td>
                        <Link to={`/ipd/admissions/${m.ipdAdmissionId}`}>{m.ipdAdmissionId}</Link>
                      </td>
                      <td>{MEAL_TYPE_LABELS[m.mealType] ?? m.mealType}</td>
                      <td>{m.dietType}</td>
                      <td>
                        <span className={`badge ${m.status === 'SERVED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                          {m.status}
                        </span>
                      </td>
                      <td className="small">{m.deliveredBy ?? '—'}</td>
                      <td className="small">{formatDateTime(m.deliveredAt)}</td>
                      <td>
                        {m.status === 'PENDING' && (
                          <button
                            type="button"
                            className="btn btn-sm btn-success"
                            onClick={() => handleServe(m.id)}
                            disabled={!!actionLoading}
                          >
                            {actionLoading === m.id ? '…' : 'Mark Served'}
                          </button>
                        )}
                      </td>
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
