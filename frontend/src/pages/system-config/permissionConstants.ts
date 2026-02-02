import type { ActionType, ModuleVisibility } from '../../types/system'

export const ACTION_TYPES: ActionType[] = ['VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE']

export const VISIBILITY_OPTIONS: { value: ModuleVisibility; label: string }[] = [
  { value: 'VISIBLE', label: 'Visible' },
  { value: 'READ_ONLY', label: 'Read-only' },
  { value: 'HIDDEN', label: 'Hidden' },
]
