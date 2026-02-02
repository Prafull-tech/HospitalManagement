/**
 * System Configuration / Access Control types.
 * Align with backend DTOs and enums.
 */

export type ActionType = 'VIEW' | 'CREATE' | 'UPDATE' | 'DELETE' | 'APPROVE'

export type ModuleCategory =
  | 'CLINICAL'
  | 'ADMIN'
  | 'DIAGNOSTICS'
  | 'FRONT_OFFICE'
  | 'NURSING'
  | 'PHARMACY'
  | 'BILLING'
  | 'REPORTS'
  | 'SYSTEM'

export type ModuleVisibility = 'VISIBLE' | 'HIDDEN' | 'READ_ONLY'

export interface RoleResponse {
  id: number
  code: string
  name: string
  description?: string
  systemRole: boolean
  active: boolean
  sortOrder?: number
  createdAt: string
  updatedAt: string
}

export interface RoleRequest {
  code: string
  name: string
  description?: string
  systemRole: boolean
  active: boolean
  sortOrder?: number
}

export interface ModuleResponse {
  id: number
  code: string
  name: string
  description?: string
  moduleCategory: ModuleCategory
  routePath?: string
  enabled: boolean
  sortOrder?: number
  createdAt: string
  updatedAt: string
}

export interface ModuleRequest {
  code: string
  name: string
  description?: string
  moduleCategory: ModuleCategory
  routePath?: string
  enabled: boolean
  sortOrder?: number
}

export interface PermissionMatrixItem {
  moduleId: number
  moduleCode: string
  moduleName: string
  visibility?: ModuleVisibility
  actions: ActionType[]
}

export interface MyPermissionsResponse {
  roleCodes: string[]
  allowedModules: ModuleResponse[]
  permissions: PermissionMatrixItem[]
}

export interface PermissionAssignRequest {
  roleId: number
  moduleId: number
  actions: ActionType[]
  visibility?: ModuleVisibility
}

export interface FeatureToggleResponse {
  id: number
  featureKey: string
  name?: string
  description?: string
  enabled: boolean
  sortOrder?: number
  createdAt: string
  updatedAt: string
}
