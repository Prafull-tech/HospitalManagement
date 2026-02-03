/**
 * System Configuration — Permission Matrix.
 * Select a role and assign per-module actions and visibility.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { systemRolesApi, systemModulesApi, systemPermissionsApi } from '../../api/system'
import type {
  RoleResponse,
  ModuleResponse,
  PermissionMatrixItem,
  ActionType,
  ModuleVisibility,
} from '../../types/system'
import { ACTION_TYPES, VISIBILITY_OPTIONS } from './permissionConstants'
import styles from './SystemConfigShared.module.css'
import shared from '../../styles/Dashboard.module.css'

interface MatrixRow {
  moduleId: number
  moduleCode: string
  moduleName: string
  visibility: ModuleVisibility
  actions: Set<ActionType>
}

export function SystemConfigPermissionsPage() {
  const [roles, setRoles] = useState<RoleResponse[]>([])
  const [modules, setModules] = useState<ModuleResponse[]>([])
  const [selectedRoleId, setSelectedRoleId] = useState<number | ''>('')
  const [matrix, setMatrix] = useState<MatrixRow[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const [saveSuccess, setSaveSuccess] = useState(false)

  useEffect(() => {
    setLoading(true)
    setLoadError('')
    Promise.all([systemRolesApi.list(true), systemModulesApi.list(false)])
      .then(([r, m]) => {
        setRoles(r)
        setModules(m)
        if (r.length > 0 && !selectedRoleId) setSelectedRoleId(r[0].id)
      })
      .catch(() => setLoadError('Failed to load roles or modules.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (selectedRoleId === '' || modules.length === 0) {
      setMatrix([])
      return
    }
    setLoading(true)
    setLoadError('')
    systemPermissionsApi
      .getPermissionsForRole(selectedRoleId as number)
      .then((perms: PermissionMatrixItem[]) => {
        const byModule = new Map<number, MatrixRow>()
        for (const p of perms) {
          byModule.set(p.moduleId, {
            moduleId: p.moduleId,
            moduleCode: p.moduleCode,
            moduleName: p.moduleName,
            visibility: p.visibility ?? 'VISIBLE',
            actions: new Set(p.actions),
          })
        }
        const rows: MatrixRow[] = modules.map((m) => {
          const existing = byModule.get(m.id)
          return existing ?? {
            moduleId: m.id,
            moduleCode: m.code,
            moduleName: m.name,
            visibility: 'VISIBLE' as ModuleVisibility,
            actions: new Set<ActionType>(),
          }
        })
        setMatrix(rows)
      })
      .catch(() => setLoadError('Failed to load permissions for role.'))
      .finally(() => setLoading(false))
  }, [selectedRoleId, modules])

  const toggleAction = (moduleId: number, action: ActionType) => {
    setMatrix((prev) =>
      prev.map((row) => {
        if (row.moduleId !== moduleId) return row
        const next = new Set(row.actions)
        if (next.has(action)) next.delete(action)
        else next.add(action)
        return { ...row, actions: next }
      })
    )
  }

  const setVisibility = (moduleId: number, visibility: ModuleVisibility) => {
    setMatrix((prev) =>
      prev.map((row) => (row.moduleId === moduleId ? { ...row, visibility } : row))
    )
  }

  const handleSave = () => {
    if (selectedRoleId === '') return
    setSaving(true)
    setSaveError('')
    setSaveSuccess(false)
    const roleId = selectedRoleId as number
    const promises = matrix.map((row) =>
      systemPermissionsApi.assign({
        roleId,
        moduleId: row.moduleId,
        actions: Array.from(row.actions),
        visibility: row.visibility,
      })
    )
    Promise.all(promises)
      .then(() => {
        setSaveSuccess(true)
        setTimeout(() => setSaveSuccess(false), 3000)
      })
      .catch((err) => {
        setSaveError(err.response?.data?.message || 'Failed to save permissions.')
      })
      .finally(() => setSaving(false))
  }

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h1 className={shared.pageTitle}>Permission Matrix</h1>
        <p className={shared.pageSubtitle}>
          Assign actions and visibility per role and module. Changes take effect after save.
        </p>
      </div>

      <nav className={styles.tabs}>
        <Link to="/admin/config/roles" className={styles.tab}>Roles</Link>
        <Link to="/admin/config/modules" className={styles.tab}>Modules</Link>
        <Link to="/admin/config/permissions" className={styles.tabActive}>Permissions</Link>
        <Link to="/admin/config/features" className={styles.tab}>Feature Toggles</Link>
      </nav>

      <div className={styles.matrixToolbar}>
        <label className={styles.label}>
          Role
          <select
            className={styles.matrixSelect}
            value={selectedRoleId}
            onChange={(e) => setSelectedRoleId(e.target.value ? Number(e.target.value) : '')}
          >
            <option value="">Select role</option>
            {roles.map((r) => (
              <option key={r.id} value={r.id}>
                {r.name} ({r.code})
              </option>
            ))}
          </select>
        </label>
      </div>

      {loadError && (
        <div className={styles.errorBanner}>
          {loadError}
          <button
            type="button"
            className={styles.retryBtn}
            onClick={() => {
              if (selectedRoleId !== '' && modules.length > 0) {
                setLoading(true)
                setLoadError('')
                systemPermissionsApi
                  .getPermissionsForRole(selectedRoleId as number)
                  .then((perms: PermissionMatrixItem[]) => {
                    const byModule = new Map<number, MatrixRow>()
                    for (const p of perms) {
                      byModule.set(p.moduleId, {
                        moduleId: p.moduleId,
                        moduleCode: p.moduleCode,
                        moduleName: p.moduleName,
                        visibility: p.visibility ?? 'VISIBLE',
                        actions: new Set(p.actions),
                      })
                    }
                    const rows = modules.map((m) => {
                      const ex = byModule.get(m.id)
                      return ex ?? { moduleId: m.id, moduleCode: m.code, moduleName: m.name, visibility: 'VISIBLE' as ModuleVisibility, actions: new Set<ActionType>() }
                    })
                    setMatrix(rows)
                  })
                  .catch(() => setLoadError('Failed to load permissions for role.'))
                  .finally(() => setLoading(false))
              }
            }}
          >
            Retry
          </button>
        </div>
      )}

      {loading && <div className={styles.loading}>Loading…</div>}

      {!loading && !loadError && selectedRoleId !== '' && matrix.length > 0 && (
        <>
          <div className={styles.matrixScroll}>
            <table className={`table table-striped ${styles.matrixTable}`}>
              <thead>
                <tr>
                  <th>Module</th>
                  <th>Visibility</th>
                  {ACTION_TYPES.map((a) => (
                    <th key={a} className={styles.matrixCell}>
                      {a}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {matrix.map((row) => (
                  <tr key={row.moduleId}>
                    <td className={styles.moduleName}>
                      {row.moduleName} <code className={styles.code}>{row.moduleCode}</code>
                    </td>
                    <td>
                      <select
                        className={styles.visibilitySelect}
                        value={row.visibility}
                        onChange={(e) => setVisibility(row.moduleId, e.target.value as ModuleVisibility)}
                      >
                        {VISIBILITY_OPTIONS.map((v) => (
                          <option key={v.value} value={v.value}>
                            {v.label}
                          </option>
                        ))}
                      </select>
                    </td>
                    {ACTION_TYPES.map((action) => (
                      <td key={action} className={styles.matrixCell}>
                        <input
                          type="checkbox"
                          checked={row.actions.has(action)}
                          onChange={() => toggleAction(row.moduleId, action)}
                          title={`${action} for ${row.moduleName}`}
                        />
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className={styles.saveBar}>
            <div>
              {saveError && <span className={styles.inlineError}>{saveError}</span>}
              {saveSuccess && <span style={{ color: 'var(--hms-success)' }}>Permissions saved.</span>}
            </div>
            <button
              type="button"
              className={styles.primaryBtn}
              onClick={handleSave}
              disabled={saving}
            >
              {saving ? 'Saving…' : 'Save permissions'}
            </button>
          </div>
        </>
      )}

      {!loading && !loadError && selectedRoleId !== '' && matrix.length === 0 && modules.length > 0 && (
        <p className={styles.empty}>No permissions for this role yet. Use checkboxes above and click Save.</p>
      )}

      {!loading && !loadError && selectedRoleId !== '' && modules.length === 0 && (
        <p className={styles.empty}>No modules to display. Add modules first in Module Management.</p>
      )}
    </div>
  )
}
