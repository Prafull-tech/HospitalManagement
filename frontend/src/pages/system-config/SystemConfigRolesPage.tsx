/**
 * System Configuration — Role Management.
 * List, create, update, and deactivate roles. Admin/IT only.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { systemRolesApi } from '../../api/system'
import type { RoleResponse, RoleRequest } from '../../types/system'
import styles from './SystemConfigShared.module.css'
import shared from '../../styles/Dashboard.module.css'

export function SystemConfigRolesPage() {
  const [roles, setRoles] = useState<RoleResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showAll, setShowAll] = useState(false)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<RoleResponse | null>(null)
  const [form, setForm] = useState<RoleRequest>({
    code: '',
    name: '',
    description: '',
    systemRole: false,
    active: true,
    sortOrder: 0,
  })
  const [saveError, setSaveError] = useState('')
  const [saving, setSaving] = useState(false)

  const load = () => {
    setLoading(true)
    setError('')
    systemRolesApi
      .list(!showAll)
      .then(setRoles)
      .catch(() => setError('Failed to load roles.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [showAll])

  const openCreate = () => {
    setEditing(null)
    setForm({
      code: '',
      name: '',
      description: '',
      systemRole: false,
      active: true,
      sortOrder: roles.length + 1,
    })
    setSaveError('')
    setFormOpen(true)
  }

  const openEdit = (r: RoleResponse) => {
    setEditing(r)
    setForm({
      code: r.code,
      name: r.name,
      description: r.description ?? '',
      systemRole: r.systemRole,
      active: r.active,
      sortOrder: r.sortOrder ?? 0,
    })
    setSaveError('')
    setFormOpen(true)
  }

  const closeForm = () => {
    setFormOpen(false)
    setEditing(null)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setSaveError('')
    setSaving(true)
    const payload: RoleRequest = { ...form, description: form.description || undefined, sortOrder: form.sortOrder || undefined }
    const promise = editing
      ? systemRolesApi.update(editing.id, payload)
      : systemRolesApi.create(payload)
    promise
      .then(() => {
        load()
        closeForm()
      })
      .catch((err) => {
        setSaveError(err.response?.data?.message || 'Save failed.')
      })
      .finally(() => setSaving(false))
  }

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h1 className={shared.pageTitle}>Role Management</h1>
        <p className={shared.pageSubtitle}>
          Create and manage system roles. System roles cannot be deleted; deactivate to hide.
        </p>
      </div>

      <nav className={styles.tabs}>
        <Link to="/admin/config/roles" className={styles.tabActive}>Roles</Link>
        <Link to="/admin/config/modules" className={styles.tab}>Modules</Link>
        <Link to="/admin/config/permissions" className={styles.tab}>Permissions</Link>
        <Link to="/admin/config/features" className={styles.tab}>Feature Toggles</Link>
      </nav>

      <div className={styles.toolbar}>
        <button type="button" className={styles.primaryBtn} onClick={openCreate}>
          Add Role
        </button>
        <label className={styles.checkLabel}>
          <input
            type="checkbox"
            checked={showAll}
            onChange={(e) => setShowAll(e.target.checked)}
          />
          Show inactive
        </label>
      </div>

      {error && (
        <div className={styles.errorBanner}>
          {error}
          <button type="button" className={styles.retryBtn} onClick={load}>Retry</button>
        </div>
      )}

      {loading && <div className={styles.loading}>Loading…</div>}

      {!loading && !error && (
        <div className={styles.card}>
          <table className={`table table-striped ${styles.table}`}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>System</th>
                <th>Status</th>
                <th>Sort</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {roles.map((r) => (
                <tr key={r.id}>
                  <td><code className={styles.code}>{r.code}</code></td>
                  <td>{r.name}</td>
                  <td>{r.systemRole ? 'Yes' : 'No'}</td>
                  <td>
                    <span className={r.active ? styles.badgeSuccess : styles.badgeMuted}>
                      {r.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>{r.sortOrder ?? '—'}</td>
                  <td>
                    <button
                      type="button"
                      className={styles.textBtn}
                      onClick={() => openEdit(r)}
                      disabled={r.systemRole}
                      title={r.systemRole ? 'System roles are read-only for code' : 'Edit'}
                    >
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {roles.length === 0 && <p className={styles.empty}>No roles found.</p>}
        </div>
      )}

      {formOpen && (
        <div className={styles.modalOverlay} role="dialog" aria-modal="true" aria-labelledby="role-form-title">
          <div className={styles.modal}>
            <h2 id="role-form-title" className={styles.modalTitle}>
              {editing ? 'Edit Role' : 'Add Role'}
            </h2>
            <form onSubmit={handleSubmit} className={styles.form}>
              <div className={styles.formRow}>
                <label className={styles.label}>Code <span className={styles.required}>*</span></label>
                <input
                  type="text"
                  className={styles.input}
                  value={form.code}
                  onChange={(e) => setForm((f) => ({ ...f, code: e.target.value.toUpperCase().replace(/\s/g, '_') }))}
                  required
                  disabled={!!editing?.systemRole}
                  placeholder="e.g. CUSTOM_ROLE"
                />
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Name <span className={styles.required}>*</span></label>
                <input
                  type="text"
                  className={styles.input}
                  value={form.name}
                  onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                  required
                  placeholder="Display name"
                />
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Description</label>
                <input
                  type="text"
                  className={styles.input}
                  value={form.description}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                  placeholder="Optional"
                />
              </div>
              <div className={styles.formRow}>
                <label className={styles.checkLabel}>
                  <input
                    type="checkbox"
                    checked={form.systemRole}
                    onChange={(e) => setForm((f) => ({ ...f, systemRole: e.target.checked }))}
                    disabled={!!editing}
                  />
                  System role (built-in)
                </label>
              </div>
              <div className={styles.formRow}>
                <label className={styles.checkLabel}>
                  <input
                    type="checkbox"
                    checked={form.active}
                    onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
                  />
                  Active
                </label>
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Sort order</label>
                <input
                  type="number"
                  className={styles.input}
                  value={form.sortOrder}
                  onChange={(e) => setForm((f) => ({ ...f, sortOrder: Number(e.target.value) || 0 }))}
                  min={0}
                />
              </div>
              {saveError && <p className={styles.inlineError}>{saveError}</p>}
              <div className={styles.modalActions}>
                <button type="button" className={styles.secondaryBtn} onClick={closeForm}>
                  Cancel
                </button>
                <button type="submit" className={styles.primaryBtn} disabled={saving}>
                  {saving ? 'Saving…' : editing ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
