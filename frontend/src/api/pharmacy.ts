import { apiClient } from './client'
import type {
  ExpiryAlert,
  FefoStockRow,
  IpdIssueQueueItem,
  PharmacySummary,
  MedicineRequest,
  MedicineResponse,
} from '../types/pharmacy'

// Base path is relative to axios baseURL '/api' from apiClient.
// Do NOT prefix with '/api' again here.
const BASE = '/pharmacy'

export const pharmacyApi = {
  getIpdIssueQueue(params?: { q?: string }): Promise<IpdIssueQueueItem[]> {
    return apiClient.get(`${BASE}/ipd/issue-queue`, { params }).then((r) => r.data)
  },

  issueIndent(indentId: number): Promise<void> {
    return apiClient.post(`${BASE}/ipd/indents/${indentId}/issue`).then(() => {})
  },

  issueIndentPartial(indentId: number): Promise<void> {
    return apiClient.post(`${BASE}/ipd/indents/${indentId}/issue-partial`).then(() => {})
  },

  getFefoStock(params?: { q?: string; risk?: string }): Promise<FefoStockRow[]> {
    return apiClient.get(`${BASE}/stock/fefo`, { params }).then((r) => r.data)
  },

  getAlerts(params?: {
    severity?: string
    acknowledged?: boolean
  }): Promise<ExpiryAlert[]> {
    return apiClient.get(`${BASE}/alerts`, { params }).then((r) => r.data)
  },

  acknowledgeAlert(id: number): Promise<ExpiryAlert> {
    return apiClient.post(`${BASE}/alerts/${id}/ack`).then((r) => r.data)
  },

  getTodaySummary(): Promise<PharmacySummary> {
    return apiClient.get(`${BASE}/summary/today`).then((r) => r.data)
  },

  exportTodaySummary(format: 'PDF' | 'XLSX') {
    return apiClient.get(`${BASE}/summary/today/export`, {
      params: { format },
      responseType: 'blob',
    })
  },

  createMedicine(data: MedicineRequest): Promise<MedicineResponse> {
    return apiClient.post(`${BASE}/medicines`, data).then((r) => r.data)
  },
}

