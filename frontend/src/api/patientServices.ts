/**
 * Patient Services API: Housekeeping, Laundry, Dietary, Meals.
 */

import { apiClient } from './client'

// ============ Housekeeping ============
export type HousekeepingTaskType = 'BED_CLEANING' | 'ROOM_CLEANING' | 'DISINFECTION'
export type HousekeepingTaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'

export interface HousekeepingTask {
  id: number
  bedId?: number
  roomNo?: string
  wardName?: string
  taskType: HousekeepingTaskType
  assignedStaff?: string
  status: HousekeepingTaskStatus
  ipdAdmissionId?: number
  createdAt: string
  completedAt?: string
}

export interface HousekeepingTaskRequest {
  bedId?: number
  roomNo?: string
  wardName?: string
  taskType: HousekeepingTaskType
  assignedStaff?: string
  ipdAdmissionId?: number
}

export const housekeepingApi = {
  listTasks(status?: HousekeepingTaskStatus): Promise<HousekeepingTask[]> {
    return apiClient
      .get<HousekeepingTask[]>('/housekeeping/tasks', { params: status ? { status } : {} })
      .then((r) => r.data)
  },
  createTask(body: HousekeepingTaskRequest): Promise<HousekeepingTask> {
    return apiClient.post<HousekeepingTask>('/housekeeping/tasks', body).then((r) => r.data)
  },
  completeTask(id: number): Promise<HousekeepingTask> {
    return apiClient.put<HousekeepingTask>(`/housekeeping/tasks/${id}/complete`).then((r) => r.data)
  },
}

// ============ Laundry ============
export type LinenType = 'BEDSHEET' | 'PILLOW_COVER' | 'BLANKET'
export type LaundryStatus = 'DIRTY' | 'WASHING' | 'READY'

export interface LinenInventory {
  id: number
  linenType: LinenType
  wardName: string
  quantityIssued: number
  quantityReturned: number
  laundryStatus: LaundryStatus
  ipdAdmissionId?: number
  createdAt: string
  updatedAt: string
}

export interface LaundryIssueRequest {
  wardName: string
  linenType: LinenType
  quantity: number
  ipdAdmissionId?: number
}

export interface LaundryReturnRequest {
  wardName: string
  linenType: LinenType
  quantity: number
  ipdAdmissionId?: number
}

export const laundryApi = {
  issue(body: LaundryIssueRequest): Promise<LinenInventory> {
    return apiClient.post<LinenInventory>('/laundry/issue', body).then((r) => r.data)
  },
  returnLinen(body: LaundryReturnRequest): Promise<LinenInventory> {
    return apiClient.post<LinenInventory>('/laundry/return', body).then((r) => r.data)
  },
  getStatus(wardName?: string): Promise<LinenInventory[]> {
    return apiClient
      .get<LinenInventory[]>('/laundry/status', { params: wardName ? { wardName } : {} })
      .then((r) => r.data)
  },
}

// ============ Dietary ============
export type DietType = 'NORMAL' | 'SOFT' | 'LIQUID' | 'DIABETIC'

export interface DietPlan {
  id: number
  patientId: number
  ipdAdmissionId: number
  dietType: DietType
  mealSchedule?: string
  createdByDoctor?: string
  active: boolean
  createdAt: string
}

export interface DietPlanRequest {
  patientId: number
  ipdAdmissionId: number
  dietType: DietType
  mealSchedule?: string
  createdByDoctor?: string
  active?: boolean
}

export const dietaryApi = {
  listPlans(active?: boolean): Promise<DietPlan[]> {
    return apiClient
      .get<DietPlan[]>('/dietary/plans', { params: active !== undefined ? { active } : {} })
      .then((r) => r.data)
  },
  createPlan(body: DietPlanRequest): Promise<DietPlan> {
    return apiClient.post<DietPlan>('/dietary/plans', body).then((r) => r.data)
  },
}

// ============ Meals ============
export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER'
export type PatientMealStatus = 'PENDING' | 'SERVED'

export interface PatientMeal {
  id: number
  patientId: number
  ipdAdmissionId: number
  mealType: MealType
  dietType: string
  deliveredBy?: string
  deliveredAt?: string
  status: PatientMealStatus
  mealDate: string
}

export const mealsApi = {
  listToday(): Promise<PatientMeal[]> {
    return apiClient.get<PatientMeal[]>('/meals/today').then((r) => r.data)
  },
  serveMeal(mealId: number): Promise<PatientMeal> {
    return apiClient.post<PatientMeal>('/meals/serve', { mealId }).then((r) => r.data)
  },
}

// ============ Patient Clearance ============
export interface PatientClearance {
  housekeeping: boolean
  linen: boolean
  dietary: boolean
}

export const patientClearanceApi = {
  getClearance(ipdId: number): Promise<PatientClearance> {
    return apiClient.get<PatientClearance>(`/patient/clearance/${ipdId}`).then((r) => r.data)
  },
}
