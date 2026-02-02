/**
 * Permissions context: fetches allowed modules and actions from /api/system/permissions/me.
 * Used for dynamic sidebar filtering and action-based UI (can create/edit/delete).
 * When API is unavailable or no roles, falls back to role-based sidebar filter only.
 */

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { systemPermissionsApi } from '../api/system'
import type { MyPermissionsResponse, ActionType, ModuleResponse } from '../types/system'

interface PermissionsState {
  loading: boolean
  error: string | null
  data: MyPermissionsResponse | null
  /** Route paths the user is allowed to see (for sidebar). Empty = use role filter only. */
  allowedRoutePaths: Set<string>
  /** Module code → allowed actions. */
  actionsByModule: Map<string, Set<ActionType>>
  /** Allowed modules by code. */
  allowedModulesByCode: Map<string, ModuleResponse>
}

interface PermissionsContextValue extends PermissionsState {
  /** True if we have permission data from API (use it for sidebar/actions). */
  hasPermissionData: boolean
  /** Check if user can perform action on module (by code). */
  can: (moduleCode: string, action: ActionType) => boolean
  /** Check if route is allowed (pathname starts with or equals one of allowedRoutePaths). */
  isRouteAllowed: (pathname: string) => boolean
  refetch: () => void
}

const defaultState: PermissionsState = {
  loading: false,
  error: null,
  data: null,
  allowedRoutePaths: new Set(),
  actionsByModule: new Map(),
  allowedModulesByCode: new Map(),
}

const PermissionsContext = createContext<PermissionsContextValue | null>(null)

function buildState(data: MyPermissionsResponse | null): Omit<PermissionsState, 'loading' | 'error'> {
  if (!data) {
    return {
      data: null,
      allowedRoutePaths: new Set(),
      actionsByModule: new Map(),
      allowedModulesByCode: new Map(),
    }
  }
  const allowedRoutePaths = new Set<string>()
  const actionsByModule = new Map<string, Set<ActionType>>()
  const allowedModulesByCode = new Map<string, ModuleResponse>()
  for (const m of data.allowedModules) {
    if (m.routePath) {
      const path = m.routePath.startsWith('/') ? m.routePath : `/${m.routePath}`
      allowedRoutePaths.add(path)
    }
    allowedModulesByCode.set(m.code, m)
  }
  for (const p of data.permissions) {
    const set = new Set<ActionType>(p.actions)
    actionsByModule.set(p.moduleCode, set)
  }
  return {
    data,
    allowedRoutePaths,
    actionsByModule,
    allowedModulesByCode,
  }
}

export function PermissionsProvider({
  children,
  roleCodes,
}: {
  children: React.ReactNode
  /** Role codes for X-Roles header (e.g. from Auth). When empty, no request is sent. */
  roleCodes: string[]
}) {
  const [state, setState] = useState<PermissionsState>({ ...defaultState, loading: false })

  const fetchPermissions = useCallback(() => {
    if (roleCodes.length === 0) {
      setState((s) => ({ ...s, loading: false, error: null, ...buildState(null) }))
      return
    }
    setState((s) => ({ ...s, loading: true, error: null }))
    systemPermissionsApi
      .getMyPermissions(roleCodes)
      .then((data) => {
        setState((s) => ({ ...s, loading: false, error: null, ...buildState(data) }))
      })
      .catch(() => {
        setState((s) => ({
          ...s,
          loading: false,
          error: 'Failed to load permissions',
          ...buildState(null),
        }))
      })
  }, [roleCodes.join(',')])

  useEffect(() => {
    fetchPermissions()
  }, [fetchPermissions])

  const can = useCallback(
    (moduleCode: string, action: ActionType): boolean => {
      const set = state.actionsByModule.get(moduleCode)
      return set ? set.has(action) : false
    },
    [state.actionsByModule]
  )

  const isRouteAllowed = useCallback(
    (pathname: string): boolean => {
      if (state.allowedRoutePaths.size === 0) return true
      const normalized = pathname.replace(/\/$/, '') || '/'
      for (const path of state.allowedRoutePaths) {
        const p = path.replace(/\/$/, '') || '/'
        if (normalized === p || normalized.startsWith(p + '/')) return true
      }
      return false
    },
    [state.allowedRoutePaths]
  )

  const value = useMemo<PermissionsContextValue>(
    () => ({
      ...state,
      hasPermissionData: state.data != null && state.allowedRoutePaths.size > 0,
      can,
      isRouteAllowed,
      refetch: fetchPermissions,
    }),
    [state, can, isRouteAllowed, fetchPermissions]
  )

  return <PermissionsContext.Provider value={value}>{children}</PermissionsContext.Provider>
}

export function usePermissions(): PermissionsContextValue {
  const ctx = useContext(PermissionsContext)
  if (!ctx) {
    throw new Error('usePermissions must be used within PermissionsProvider')
  }
  return ctx
}

export function usePermissionsOptional(): PermissionsContextValue | null {
  return useContext(PermissionsContext)
}
