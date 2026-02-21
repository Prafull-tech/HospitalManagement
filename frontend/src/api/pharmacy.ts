import { apiClient } from './client'
import type {
  ExpiryAlert,
  FefoStockRow,
  IpdIssueQueueItem,
  IssueQueuePatient,
  BatchSuggestion,
  PharmacySummary,
  MedicineRequest,
  MedicineResponse,
  MedicineLookupResponse,
  ManualEntryRequest,
  BarcodeEntryRequest,
  ExistingBatchRequest,
  MedicineImportResult,
  RackRequest,
  RackResponse,
  RackSuggestion,
  ShelfRequest,
  ShelfResponse,
  RackInventory,
  PurchaseRequest,
  SellRequest,
  StockTransactionResponse,
  PharmacySellResponse,
  PatientIpdStatus,
} from '../types/pharmacy'

// Base path is relative to axios baseURL '/api' from apiClient.
// Do NOT prefix with '/api' again here.
const BASE = '/pharmacy'

export const pharmacyApi = {
  getIpdIssueQueue(params?: { q?: string }): Promise<IpdIssueQueueItem[]> {
    return apiClient.get(`${BASE}/ipd/issue-queue`, { params }).then((r) => r.data)
  },

  getMedicationIssueQueue(params?: { q?: string }): Promise<IssueQueuePatient[]> {
    return apiClient.get(`${BASE}/issue-queue`, { params }).then((r) => r.data)
  },

  getBatchSuggestion(medicineId: number): Promise<BatchSuggestion | null> {
    return apiClient
      .get(`${BASE}/batch/suggest/${medicineId}`, { validateStatus: (s) => s === 200 || s === 204 })
      .then((r) => (r.status === 204 ? null : r.data))
  },

  issueMedications(orderIds: number[]): Promise<void> {
    return apiClient.post(`${BASE}/issue`, { orderIds }).then(() => {})
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

  listMedicines(params?: { q?: string }): Promise<MedicineResponse[]> {
    return apiClient.get(`${BASE}/medicines`, { params }).then((r) => r.data)
  },

  searchMedicines(q: string): Promise<MedicineResponse[]> {
    return apiClient.get(`${BASE}/medicines`, { params: { q } }).then((r) => r.data)
  },

  createMedicine(data: MedicineRequest): Promise<MedicineResponse> {
    return apiClient.post(`${BASE}/medicines`, data).then((r) => r.data)
  },

  getMedicineByBarcode(barcode: string): Promise<MedicineResponse | null> {
    return apiClient
      .get(`${BASE}/medicines/barcode/${encodeURIComponent(barcode)}`)
      .then((r) => r.data)
      .catch((e) => (e?.response?.status === 404 ? null : Promise.reject(e)))
  },

  lookupMedicine(barcode: string): Promise<MedicineLookupResponse | null> {
    return apiClient
      .get(`${BASE}/medicines/lookup/${encodeURIComponent(barcode)}`)
      .then((r) => r.data)
      .catch((e) => (e?.response?.status === 404 ? null : Promise.reject(e)))
  },

  createManualEntry(data: ManualEntryRequest): Promise<MedicineResponse> {
    return apiClient.post(`${BASE}/medicines/manual`, data).then((r) => r.data)
  },

  createFromBarcode(data: BarcodeEntryRequest): Promise<MedicineResponse> {
    return apiClient.post(`${BASE}/medicines/barcode`, data).then((r) => r.data)
  },

  addBatchToExisting(data: ExistingBatchRequest): Promise<MedicineResponse> {
    return apiClient.post(`${BASE}/medicines/existing`, data).then((r) => r.data)
  },

  updateMedicine(id: number, data: MedicineRequest): Promise<MedicineResponse> {
    return apiClient.put(`${BASE}/medicines/${id}`, data).then((r) => r.data)
  },

  disableMedicine(id: number): Promise<void> {
    return apiClient.delete(`${BASE}/medicines/${id}`).then(() => {})
  },

  downloadMedicineTemplate(type?: 'master' | 'stock'): Promise<Blob> {
    return apiClient
      .get(`${BASE}/medicines/template`, { params: { type: type ?? 'master' }, responseType: 'blob' })
      .then((r) => r.data)
  },

  importMedicineMaster(file: File): Promise<MedicineImportResult> {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post(`${BASE}/medicines/import/master`, formData, { timeout: 60000 }).then((r) => r.data)
  },

  importStock(file: File): Promise<MedicineImportResult> {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post(`${BASE}/medicines/import/stock`, formData, { timeout: 60000 }).then((r) => r.data)
  },

  importMedicines(file: File): Promise<MedicineImportResult> {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post(`${BASE}/medicines/import`, formData, { timeout: 60000 }).then((r) => r.data)
  },

  downloadImportErrorReport(result: MedicineImportResult): Promise<Blob> {
    return apiClient.post(`${BASE}/medicines/import/error-report`, result, {
      responseType: 'blob',
    }).then((r) => r.data)
  },

  // ---------- Rack Management ----------

  suggestRack(params: {
    category?: string
    storageType?: string
    lasaFlag?: boolean
  }): Promise<RackSuggestion | null> {
    return apiClient
      .get(`${BASE}/racks/suggest`, {
        params: {
          category: params.category,
          storageType: params.storageType ?? 'ROOM_TEMP',
          lasaFlag: params.lasaFlag ?? false,
        },
        validateStatus: (status) => status === 200 || status === 204,
      })
      .then((r) => (r.status === 204 ? null : r.data))
  },

  listRacks(includeInactive?: boolean): Promise<RackResponse[]> {
    return apiClient.get(`${BASE}/racks`, { params: { includeInactive: !!includeInactive } }).then((r) => r.data)
  },

  createRack(data: RackRequest): Promise<RackResponse> {
    return apiClient.post(`${BASE}/racks`, data).then((r) => r.data)
  },

  updateRack(id: number, data: RackRequest): Promise<RackResponse> {
    return apiClient.put(`${BASE}/racks/${id}`, data).then((r) => r.data)
  },

  deleteRack(id: number): Promise<void> {
    return apiClient.delete(`${BASE}/racks/${id}`).then(() => {})
  },

  listShelves(rackId: number): Promise<ShelfResponse[]> {
    return apiClient.get(`${BASE}/racks/${rackId}/shelves`).then((r) => r.data)
  },

  addShelf(rackId: number, data: ShelfRequest): Promise<ShelfResponse> {
    return apiClient.post(`${BASE}/racks/${rackId}/shelves`, data).then((r) => r.data)
  },

  getRackInventory(rackId: number): Promise<RackInventory> {
    return apiClient.get(`${BASE}/racks/${rackId}/inventory`).then((r) => r.data)
  },

  // ---------- Purchase & Sell ----------

  purchase(data: PurchaseRequest): Promise<StockTransactionResponse> {
    return apiClient.post(`${BASE}/stock/purchase`, data).then((r) => r.data)
  },

  sell(data: SellRequest): Promise<PharmacySellResponse> {
    return apiClient.post(`${BASE}/stock/sell`, data).then((r) => r.data)
  },

  async downloadInvoice(invoiceNumber: string): Promise<void> {
    const res = await apiClient.get(`${BASE}/invoice/${encodeURIComponent(invoiceNumber)}`, {
      responseType: 'blob',
    })
    const blob = res.data as Blob
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${invoiceNumber}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  },

  regenerateInvoice(saleId: number): Promise<PharmacySellResponse> {
    return apiClient.post(`${BASE}/invoice/${saleId}/regenerate`).then((r) => r.data)
  },

  listStockTransactions(params?: { medicineId?: number; limit?: number }): Promise<StockTransactionResponse[]> {
    return apiClient.get(`${BASE}/stock/transactions`, { params }).then((r) => r.data)
  },

  getPatientIpdStatus(patientId: number): Promise<PatientIpdStatus> {
    return apiClient.get(`${BASE}/patients/${patientId}/ipd-status`).then((r) => r.data)
  },
}

