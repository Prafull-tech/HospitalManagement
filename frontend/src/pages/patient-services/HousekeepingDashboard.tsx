/**
 * Housekeeping Dashboard – Pending, In Progress, Completed tasks.
 */

import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { housekeepingApi } from '../../api/patientServices'
import type { HousekeepingTask, HousekeepingTaskStatus } from '../../api/patientServices'

function formatDateTime(iso: string | undefined): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' })
}

const STATUS_LABELS: Record<HousekeepingTaskStatus, string> = {
  PENDING: 'Pending',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
}

const TASK_TYPE_LABELS: Record<string, string> = {
  BED_CLEANING: 'Bed Cleaning',
  ROOM_CLEANING: 'Room Cleaning',
  DISINFECTION: 'Disinfection',
}

export function HousekeepingDashboard() {
  const [tasks, setTasks] = useState<HousekeepingTask[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [statusFilter, setStatusFilter] = useState<HousekeepingTaskStatus | ''>('')
  const [actionLoading, setActionLoading] = useState<number | null>(null)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [createForm, setCreateForm] = useState({
    wardName: '',
    roomNo: '',
    taskType: 'BED_CLEANING' as HousekeepingTask['taskType'],
    assignedStaff: '',
  })
  const [createLoading, setCreateLoading] = useState(false)

  const fetchTasks = useCallback(() => {
    setLoading(true)
    setError('')
    housekeepingApi
      .listTasks(statusFilter || undefined)
      .then(setTasks)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load tasks')
        setTasks([])
      })
      .finally(() => setLoading(false))
  }, [statusFilter])

  useEffect(() => {
    fetchTasks()
  }, [fetchTasks])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!createForm.wardName.trim()) return
    setCreateLoading(true)
    setError('')
    try {
      await housekeepingApi.createTask({
        wardName: createForm.wardName.trim(),
        roomNo: createForm.roomNo.trim() || undefined,
        taskType: createForm.taskType,
        assignedStaff: createForm.assignedStaff.trim() || undefined,
      })
      setShowCreateForm(false)
      setCreateForm({ wardName: '', roomNo: '', taskType: 'BED_CLEANING', assignedStaff: '' })
      fetchTasks()
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } }
      setError(e.response?.data?.message || 'Failed to create task')
    } finally {
      setCreateLoading(false)
    }
  }

  const handleComplete = async (id: number) => {
    setActionLoading(id)
    setError('')
    try {
      await housekeepingApi.completeTask(id)
      fetchTasks()
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } }
      setError(e.response?.data?.message || 'Failed to complete task')
    } finally {
      setActionLoading(null)
    }
  }

  const pending = tasks.filter((t) => t.status === 'PENDING')
  const inProgress = tasks.filter((t) => t.status === 'IN_PROGRESS')
  const completed = tasks.filter((t) => t.status === 'COMPLETED')

  return (
    <div className="d-flex flex-column gap-3">
      <nav aria-label="Breadcrumb">
        <ol className="breadcrumb mb-0">
          <li className="breadcrumb-item">
            <Link to="/">Home</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            Housekeeping
          </li>
        </ol>
      </nav>

      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <h1 className="h4 mb-0">Housekeeping Tasks</h1>
        <div className="d-flex gap-2">
          <button type="button" className="btn btn-primary btn-sm" onClick={() => setShowCreateForm(true)}>
            Create Task
          </button>
          <select
            className="form-select form-select-sm"
            style={{ width: 'auto' }}
            value={statusFilter}
            onChange={(e) => setStatusFilter((e.target.value || '') as HousekeepingTaskStatus | '')}
          >
            <option value="">All</option>
            <option value="PENDING">Pending</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
          <button type="button" className="btn btn-outline-primary btn-sm" onClick={fetchTasks} disabled={loading}>
            Refresh
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {/* Create task form */}
      {showCreateForm && (
        <div className="card border shadow">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h6 className="mb-0">Create Housekeeping Task</h6>
            <button type="button" className="btn-close btn-sm" onClick={() => setShowCreateForm(false)} aria-label="Close" />
          </div>
          <form className="card-body" onSubmit={handleCreate}>
            <div className="row g-2">
              <div className="col-12 col-md-4">
                <label className="form-label small">Ward Name</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={createForm.wardName}
                  onChange={(e) => setCreateForm((f) => ({ ...f, wardName: e.target.value }))}
                  required
                />
              </div>
              <div className="col-12 col-md-4">
                <label className="form-label small">Room No</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={createForm.roomNo}
                  onChange={(e) => setCreateForm((f) => ({ ...f, roomNo: e.target.value }))}
                />
              </div>
              <div className="col-12 col-md-4">
                <label className="form-label small">Task Type</label>
                <select
                  className="form-select form-select-sm"
                  value={createForm.taskType}
                  onChange={(e) => setCreateForm((f) => ({ ...f, taskType: e.target.value as HousekeepingTask['taskType'] }))}
                >
                  {Object.entries(TASK_TYPE_LABELS).map(([v, l]) => (
                    <option key={v} value={v}>
                      {l}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label small">Assigned Staff</label>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  value={createForm.assignedStaff}
                  onChange={(e) => setCreateForm((f) => ({ ...f, assignedStaff: e.target.value }))}
                />
              </div>
            </div>
            <div className="d-flex gap-2 mt-2">
              <button type="submit" className="btn btn-primary btn-sm" disabled={createLoading}>
                {createLoading ? '…' : 'Create'}
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowCreateForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Summary cards */}
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
          <div className="card border border-info shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-info small mb-0">In Progress</p>
              <p className="fw-bold mb-0">{inProgress.length}</p>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-4">
          <div className="card border border-success shadow-sm h-100">
            <div className="card-body py-2">
              <p className="text-success small mb-0">Completed</p>
              <p className="fw-bold mb-0">{completed.length}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Task list */}
      <div className="card border shadow-sm">
        <div className="card-header bg-light py-2">
          <h6 className="mb-0">Tasks</h6>
        </div>
        <div className="card-body p-0">
          {loading ? (
            <div className="p-4 text-center text-muted">Loading…</div>
          ) : tasks.length === 0 ? (
            <div className="p-4 text-center text-muted">No tasks found.</div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Ward / Room</th>
                    <th>Type</th>
                    <th>Assigned</th>
                    <th>Status</th>
                    <th>Created</th>
                    <th>Completed</th>
                    <th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {tasks.map((t) => (
                    <tr key={t.id}>
                      <td>
                        {t.wardName ?? '—'} {t.roomNo && `/ ${t.roomNo}`}
                      </td>
                      <td>{TASK_TYPE_LABELS[t.taskType] ?? t.taskType}</td>
                      <td>{t.assignedStaff ?? '—'}</td>
                      <td>
                        <span
                          className={`badge ${
                            t.status === 'COMPLETED'
                              ? 'bg-success'
                              : t.status === 'IN_PROGRESS'
                                ? 'bg-info'
                                : 'bg-warning text-dark'
                          }`}
                        >
                          {STATUS_LABELS[t.status]}
                        </span>
                      </td>
                      <td className="small">{formatDateTime(t.createdAt)}</td>
                      <td className="small">{formatDateTime(t.completedAt)}</td>
                      <td>
                        {t.status !== 'COMPLETED' && (
                          <button
                            type="button"
                            className="btn btn-sm btn-success"
                            onClick={() => handleComplete(t.id)}
                            disabled={!!actionLoading}
                          >
                            {actionLoading === t.id ? '…' : 'Complete'}
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
