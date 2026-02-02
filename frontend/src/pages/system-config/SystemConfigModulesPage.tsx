/**
 * System Configuration — Module Management.
 * List, create, update, enable/disable modules.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { systemModulesApi } from '../../api/system'
import type { ModuleResponse, ModuleRequest } from '../../types/system'
import { MODULE_CATEGORY_LABELS } from './moduleCategoryLabels'
import styles from './SystemConfigShared.module.css'
import shared from '../../styles/Dashboard.module.css'

export function SystemConfigModulesPage() {
  const [modules, setModules] = useState<ModuleResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showDisabled, setShowDisabled] = useState(false)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<ModuleResponse | null>(null)
  const [form, setForm] = useState<ModuleRequest>({
    code: '',
    name: '',
    description: '',
    moduleCategory: 'CLINICAL',
    routePath: '',
    enabled: true,
    sortOrder: 0,
  })
  const [saveError, setSaveError] = useState('')
  const [saving, setSaving] = useState(false)

  const load = () => {
    setLoading(true)
    setError('')
    systemModulesApi
      .list(!showDisabled)
      .then(setModules)
      .catch(() => setError('Failed to load modules.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [showDisabled])

  const openCreate = () => {
    setEditing(null)
    setForm({
      code: '',
      name: '',
      description: '',
      moduleCategory: 'CLINICAL',
      routePath: '',
      enabled: true,
      sortOrder: 0,
    })
    setSaveError('')
    setFormOpen(true)
  }

  const openEdit = (m: ModuleResponse) => {
    setEditing(m)
    setForm({
      code: m.code,
      name: m.name,
      description: m.description ?? '',
      moduleCategory: m.moduleCategory,
      routePath: m.routePath ?? '',
      enabled: m.enabled,
      sortOrder: m.sortOrder ?? 0,
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
    const payload: ModuleRequest = {
      ...form,
      description: form.description || undefined,
      routePath: form.routePath || undefined,
      sortOrder: form.sortOrder || undefined,
    }
    const promise = editing
      ? systemModulesApi.update(editing.id, payload)
      : systemModulesApi.create(payload)
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
        <h1 className={shared.pageTitle}>Module Management</h1>
        <p className={shared.pageSubtitle}>
          Define system modules and their route paths. Disabled modules are hidden from the sidebar.
        </p>
      </div>

      <nav className={styles.tabs}>
        <Link to="/admin/config/roles" className={styles.tab}>Roles</Link>
        <Link to="/admin/config/modules" className={styles.tabActive}>Modules</Link>
        <Link to="/admin/config/permissions" className={styles.tab}>Permissions</Link>
        <Link to="/admin/config/features" className={styles.tab}>Feature Toggles</Link>
      </nav>

      <div className={styles.toolbar}>
        <button type="button" className={styles.primaryBtn} onClick={openCreate}>
          Add Module
        </button>
        <label className={styles.checkLabel}>
          <input
            type="checkbox"
            checked={showDisabled}
            onChange={(e) => setShowDisabled(e.target.checked)}
          />
          Show disabled
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
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Category</th>
                <th>Route</th>
                <th>Status</th>
                <th>Sort</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {modules.map((m) => (
                <tr key={m.id}>
                  <td><code className={styles.code}>{m.code}</code></td>
                  <td>{m.name}</td>
                  <td>{MODULE_CATEGORY_LABELS[m.moduleCategory] ?? m.moduleCategory}</td>
                  <td><code className={styles.code}>{m.routePath || '—'}</code></td>
                  <td>
                    <span className={m.enabled ? styles.badgeSuccess : styles.badgeMuted}>
                      {m.enabled ? 'Enabled' : 'Disabled'}
                    </span>
                  </td>
                  <td>{m.sortOrder ?? '—'}</td>
                  <td>
                    <button type="button" className={styles.textBtn} onClick={() => openEdit(m)}>
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {modules.length === 0 && <p className={styles.empty}>No modules found.</p>}
        </div>
      )}

      {formOpen && (
        <div className={styles.modalOverlay} role="dialog" aria-modal="true" aria-labelledby="module-form-title">
          <div className={styles.modal}>
            <h2 id="module-form-title" className={styles.modalTitle}>
              {editing ? 'Edit Module' : 'Add Module'}
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
                  disabled={!!editing}
                  placeholder="e.g. OPD"
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
                <label className={styles.label}>Category</label>
                <select
                  className={styles.input}
                  value={form.moduleCategory}
                  onChange={(e) => setForm((f) => ({ ...f, moduleCategory: e.target.value as ModuleRequest['moduleCategory'] }))}
                >
                  {(Object.keys(MODULE_CATEGORY_LABELS) as Array<keyof typeof MODULE_CATEGORY_LABELS>).map((k) => (
                    <option key={k} value={k}>{MODULE_CATEGORY_LABELS[k]}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formRow}>
                <label className={styles.label}>Route path</label>
                <input
                  type="text"
                  className={styles.input}
                  value={form.routePath}
                  onChange={(e) => setForm((f) => ({ ...f, routePath: e.target.value }))}
                  placeholder="/opd"
                />
              </div>
              <div className={styles.formRow}>
                <label className={styles.checkLabel}>
                  <input
                    type="checkbox"
                    checked={form.enabled}
                    onChange={(e) => setForm((f) => ({ ...f, enabled: e.target.checked }))}
                  />
                  Enabled
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
                <button type="button" className={styles.secondaryBtn} onClick={closeForm}>Cancel</button>
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
