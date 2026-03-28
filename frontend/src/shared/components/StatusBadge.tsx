interface StatusBadgeProps {
  status: string
  variant?: 'success' | 'warning' | 'danger' | 'info' | 'secondary' | 'primary'
}

const STATUS_VARIANTS: Record<string, StatusBadgeProps['variant']> = {
  ACTIVE: 'success',
  COMPLETED: 'success',
  VERIFIED: 'success',
  ADMITTED: 'primary',
  PENDING: 'warning',
  IN_PROGRESS: 'info',
  CANCELLED: 'danger',
  CLOSED: 'secondary',
  DISCHARGED: 'secondary',
  EXPIRED: 'danger',
}

export function StatusBadge({ status, variant }: StatusBadgeProps) {
  const resolvedVariant = variant ?? STATUS_VARIANTS[status] ?? 'secondary'
  const label = status.replace(/_/g, ' ')
  return <span className={`badge bg-${resolvedVariant}`}>{label}</span>
}
