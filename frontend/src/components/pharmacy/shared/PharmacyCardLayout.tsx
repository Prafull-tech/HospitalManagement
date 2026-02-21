import type { ReactNode } from 'react'

export interface PharmacyCardLayoutProps {
  title: string
  description?: string
  headerAction?: ReactNode
  error?: string
  loading?: boolean
  empty?: boolean
  emptyMessage?: string
  children: ReactNode
}

/**
 * Unified card layout for all Pharmacy tab content.
 * Ensures consistent structure: header, description, error, loading, empty state, body.
 */
export function PharmacyCardLayout({
  title,
  description,
  headerAction,
  error,
  loading,
  empty,
  emptyMessage = 'No data available.',
  children,
}: PharmacyCardLayoutProps) {
  return (
    <div className="card shadow-sm mb-3">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div>
          <h3 className="h6 mb-0 fw-bold">{title}</h3>
          {description && <p className="small text-muted mb-0">{description}</p>}
        </div>
        {headerAction}
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger py-2 mb-3">{error}</div>}
        {loading && (
          <div className="placeholder-glow">
            <span className="placeholder col-8" />
            <span className="placeholder col-6 mt-2" />
            <span className="placeholder col-4 mt-2" />
          </div>
        )}
        {!loading && empty && (
          <p className="small text-muted mb-0 py-3">{emptyMessage}</p>
        )}
        {!loading && !empty && children}
      </div>
    </div>
  )
}
