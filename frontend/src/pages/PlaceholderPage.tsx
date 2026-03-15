/**
 * Placeholder page for features under development.
 * Displays a consistent "Coming soon" message with optional title.
 */

import { Link } from 'react-router-dom'

interface PlaceholderPageProps {
  title?: string
  description?: string
}

export function PlaceholderPage({ title = 'Coming Soon', description }: PlaceholderPageProps) {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center py-5 px-3">
      <div className="card border shadow-sm" style={{ maxWidth: 420 }}>
        <div className="card-body text-center py-5">
          <div className="mb-3">
            <span className="badge bg-secondary fs-6">Under Development</span>
          </div>
          <h2 className="h4 mb-3">{title}</h2>
          <p className="text-muted mb-4">
            {description ?? 'This module is planned for a future release. Please use the legacy navigation for now.'}
          </p>
          <Link to="/" className="btn btn-primary">
            Back to Dashboard
          </Link>
        </div>
      </div>
    </div>
  )
}
