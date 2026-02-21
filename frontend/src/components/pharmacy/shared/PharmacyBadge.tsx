/**
 * Centralized badge styling for Pharmacy module.
 * Ensures consistent badge colors across all tabs.
 */

export type PharmacyBadgeType =
  | 'PURCHASE'
  | 'SELL'
  | 'LASA'
  | 'SAFE'
  | 'NEAR_EXPIRY'
  | 'CRITICAL'
  | 'EXPIRED'
  | 'INFO'
  | 'WARNING'
  | 'DANGER'
  | 'ICU'
  | 'EMERGENCY'
  | 'PRIORITY'

export function PharmacyBadge({ type, label }: { type: PharmacyBadgeType; label?: string }) {
  const displayLabel = label ?? type.replace(/_/g, ' ')
  const className = getBadgeClass(type)
  return <span className={className}>{displayLabel}</span>
}

function getBadgeClass(type: PharmacyBadgeType): string {
  switch (type) {
    case 'PURCHASE':
      return 'badge bg-success'
    case 'SELL':
      return 'badge bg-primary'
    case 'LASA':
      return 'badge bg-warning text-dark'
    case 'SAFE':
      return 'badge bg-success'
    case 'NEAR_EXPIRY':
      return 'badge bg-warning text-dark'
    case 'CRITICAL':
      return 'badge bg-danger'
    case 'EXPIRED':
      return 'badge bg-secondary'
    case 'INFO':
      return 'badge bg-info'
    case 'WARNING':
      return 'badge bg-warning text-dark'
    case 'DANGER':
      return 'badge bg-danger'
    case 'ICU':
    case 'EMERGENCY':
      return 'badge bg-danger'
    case 'PRIORITY':
      return 'badge bg-secondary'
    default:
      return 'badge bg-secondary'
  }
}
