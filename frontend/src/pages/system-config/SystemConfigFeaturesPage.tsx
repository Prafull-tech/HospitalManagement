/**
 * System Configuration — Feature Toggles.
 * Enable/disable features without redeploy. Admin/IT only.
 */

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { systemFeaturesApi } from '../../api/system'
import type { FeatureToggleResponse } from '../../types/system'
import styles from './SystemConfigShared.module.css'
import shared from '../../styles/Dashboard.module.css'

export function SystemConfigFeaturesPage() {
  const [features, setFeatures] = useState<FeatureToggleResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [togglingId, setTogglingId] = useState<number | null>(null)
  const [confirmToggle, setConfirmToggle] = useState<{ id: number; enabled: boolean } | null>(null)

  const load = () => {
    setLoading(true)
    setError('')
    systemFeaturesApi
      .list()
      .then(setFeatures)
      .catch(() => setError('Failed to load feature toggles.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const handleSwitch = (f: FeatureToggleResponse, nextEnabled: boolean) => {
    setConfirmToggle({ id: f.id, enabled: nextEnabled })
  }

  const confirm = () => {
    if (!confirmToggle) return
    setTogglingId(confirmToggle.id)
    setConfirmToggle(null)
    systemFeaturesApi
      .setEnabled(confirmToggle.id, confirmToggle.enabled)
      .then((updated) => {
        setFeatures((prev) => prev.map((x) => (x.id === updated.id ? updated : x)))
      })
      .catch(() => setError('Failed to update feature toggle.'))
      .finally(() => setTogglingId(null))
  }

  const cancelConfirm = () => {
    setConfirmToggle(null)
  }

  return (
    <div className={shared.dashboardPage}>
      <div className={shared.pageHeader}>
        <h1 className={shared.pageTitle}>Feature Toggles</h1>
        <p className={shared.pageSubtitle}>
          Enable or disable features without redeploying. Changes take effect immediately.
        </p>
      </div>

      <nav className={styles.tabs}>
        <Link to="/admin/config/roles" className={styles.tab}>Roles</Link>
        <Link to="/admin/config/modules" className={styles.tab}>Modules</Link>
        <Link to="/admin/config/permissions" className={styles.tab}>Permissions</Link>
        <Link to="/admin/config/features" className={styles.tabActive}>Feature Toggles</Link>
      </nav>

      {error && (
        <div className={styles.errorBanner}>
          {error}
          <button type="button" className={styles.retryBtn} onClick={load}>Retry</button>
        </div>
      )}

      {loading && <div className={styles.loading}>Loading…</div>}

      {!loading && !error && (
        <div className={styles.card}>
          {features.map((f) => (
            <div key={f.id} className={styles.switchRow}>
              <div className={styles.switchLabel}>
                <span className={styles.switchTitle}>{f.name || f.featureKey}</span>
                {f.description && (
                  <span className={styles.switchDesc}>{f.description}</span>
                )}
              </div>
              <button
                type="button"
                className={`${styles.switch} ${f.enabled ? styles.switchOn : ''}`}
                onClick={() => handleSwitch(f, !f.enabled)}
                disabled={togglingId === f.id}
                role="switch"
                aria-checked={f.enabled}
                aria-label={`${f.enabled ? 'Disable' : 'Enable'} ${f.name || f.featureKey}`}
                title={f.enabled ? 'Turn off' : 'Turn on'}
              >
                <span className={styles.switchThumb} />
              </button>
            </div>
          ))}
          {features.length === 0 && <p className={styles.empty}>No feature toggles defined.</p>}
        </div>
      )}

      {confirmToggle && (
        <div className={styles.modalOverlay} role="dialog" aria-modal="true" aria-labelledby="confirm-title">
          <div className={styles.modal}>
            <h2 id="confirm-title" className={styles.modalTitle}>Confirm change</h2>
            <p className={styles.modalText}>
              {confirmToggle.enabled
                ? 'Enable this feature? It will be available to users immediately.'
                : 'Disable this feature? It will be hidden from users immediately.'}
            </p>
            <div className={styles.modalActions}>
              <button type="button" className={styles.secondaryBtn} onClick={cancelConfirm}>
                Cancel
              </button>
              <button type="button" className={styles.primaryBtn} onClick={confirm}>
                {confirmToggle.enabled ? 'Enable' : 'Disable'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
