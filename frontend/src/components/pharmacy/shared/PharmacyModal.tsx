import type { ReactNode } from 'react'

export interface PharmacyModalProps {
  open: boolean
  title: string
  onClose: () => void
  primaryLabel: string
  primaryVariant?: 'primary' | 'success' | 'danger'
  onPrimary: (e: React.FormEvent) => void | Promise<void>
  loading?: boolean
  error?: string
  children: ReactNode
}

/**
 * Unified modal layout for Pharmacy: title, body, footer (Cancel + Primary).
 * Bootstrap 5 modal classes. No inconsistent spacing.
 */
export function PharmacyModal({
  open,
  title,
  onClose,
  primaryLabel,
  primaryVariant = 'primary',
  onPrimary,
  loading = false,
  error,
  children,
}: PharmacyModalProps) {
  if (!open) return null

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    void onPrimary(e)
  }

  return (
    <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{title}</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {error && <div className="alert alert-danger py-2 mb-3">{error}</div>}
              {children}
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={onClose} disabled={loading}>
                Cancel
              </button>
              <button
                type="submit"
                className={`btn btn-${primaryVariant}`}
                disabled={loading}
              >
                {loading ? 'Saving…' : primaryLabel}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
