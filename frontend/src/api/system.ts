/**
 * System Configuration / Access Control API.
 * Base path: /api/system (via apiClient baseURL /api).
 */

import { apiClient } from './client'
import type {
  RoleResponse,
  RoleRequest,
  ModuleResponse,
  ModuleRequest,
  MyPermissionsResponse,
  PermissionAssignRequest,
  PermissionMatrixItem,
  FeatureToggleResponse,
} from '../types/system'

const BASE = '/system'

export const systemRolesApi = {
  list(activeOnly = false): Promise<RoleResponse[]> {
    return apiClient.get(`${BASE}/roles`, { params: { activeOnly } }).then((r) => r.data)
  },
  getById(id: number): Promise<RoleResponse> {
    return apiClient.get(`${BASE}/roles/${id}`).then((r) => r.data)
  },
  create(data: RoleRequest): Promise<RoleResponse> {
    return apiClient.post(`${BASE}/roles`, data).then((r) => r.data)
  },
  update(id: number, data: RoleRequest): Promise<RoleResponse> {
    return apiClient.put(`${BASE}/roles/${id}`, data).then((r) => r.data)
  },
}

export const systemModulesApi = {
  list(enabledOnly = false): Promise<ModuleResponse[]> {
    return apiClient.get(`${BASE}/modules`, { params: { enabledOnly } }).then((r) => r.data)
  },
  getById(id: number): Promise<ModuleResponse> {
    return apiClient.get(`${BASE}/modules/${id}`).then((r) => r.data)
  },
  create(data: ModuleRequest): Promise<ModuleResponse> {
    return apiClient.post(`${BASE}/modules`, data).then((r) => r.data)
  },
  update(id: number, data: ModuleRequest): Promise<ModuleResponse> {
    return apiClient.put(`${BASE}/modules/${id}`, data).then((r) => r.data)
  },
}

export const systemPermissionsApi = {
  getMyPermissions(roleCodes: string[]): Promise<MyPermissionsResponse> {
    const headers = roleCodes.length > 0 ? { 'X-Roles': roleCodes.join(',') } : {}
    return apiClient.get<MyPermissionsResponse>(`${BASE}/permissions/me`, { headers }).then((r) => r.data)
  },
  getPermissionsForRole(roleId: number): Promise<PermissionMatrixItem[]> {
    return apiClient.get(`${BASE}/permissions/role/${roleId}`).then((r) => r.data)
  },
  assign(data: PermissionAssignRequest): Promise<void> {
    return apiClient.post(`${BASE}/permissions/assign`, data).then(() => undefined)
  },
}

export const systemFeaturesApi = {
  list(): Promise<FeatureToggleResponse[]> {
    return apiClient.get(`${BASE}/features`).then((r) => r.data)
  },
  setEnabled(id: number, enabled: boolean): Promise<FeatureToggleResponse> {
    return apiClient.patch(`${BASE}/features/${id}`, null, { params: { enabled } }).then((r) => r.data)
  },
}
