import { apiClient } from './client'
import type {
  TestMaster,
  TestMasterRequest,
  TestOrder,
  TestOrderRequest,
  LabOrder,
  LabOrderRequest,
  LabOrderItem,
  SampleCollectionRequest,
  LabResult,
  LabResultEntryRequest,
  LabResultRequest,
  LabReport,
  LabDashboardResponse,
  LabDashboardSummary,
  LabDashboardMetrics,
  LabDashboardOverview,
  LabTodaySummary,
  TestStatus,
} from '../types/lab'

const BASE = '/lab'

export const labApi = {
  // Test Master / Lab Tests Management
  createTestMaster: (request: TestMasterRequest): Promise<TestMaster> =>
    apiClient.post(`${BASE}/tests`, request).then((r) => r.data),

  listTestMasters: (active?: boolean): Promise<TestMaster[]> => {
    const params = active !== undefined ? `?active=${active}` : ''
    return apiClient.get(`${BASE}/tests${params}`).then((r) => r.data)
  },

  getTestMaster: (id: number): Promise<TestMaster> =>
    apiClient.get(`${BASE}/tests/${id}`).then((r) => r.data),

  updateTestMaster: (id: number, request: TestMasterRequest): Promise<TestMaster> =>
    apiClient.put(`${BASE}/tests/${id}`, request).then((r) => r.data),

  deleteTestMaster: (id: number): Promise<void> =>
    apiClient.delete(`${BASE}/tests/${id}`),

  expandPanel: (panelCode: string): Promise<string[]> =>
    apiClient.get(`${BASE}/test-masters/panels/${panelCode}/expand`).then((r) => r.data),

  // Test Ordering (LabOrder + TestOrder)
  createOrder: (request: LabOrderRequest | TestOrderRequest): Promise<LabOrder> =>
    apiClient.post(`${BASE}/orders`, request).then((r) => r.data),

  getOrder: (id: number): Promise<TestOrder> =>
    apiClient.get(`${BASE}/orders/test-order/${id}`).then((r) => r.data),

  listOrders: (params?: { ipdAdmissionId?: number; opdVisitId?: number; patientId?: number }): Promise<LabOrder[]> => {
    const searchParams = new URLSearchParams()
    if (params?.ipdAdmissionId) searchParams.set('ipdAdmissionId', String(params.ipdAdmissionId))
    if (params?.opdVisitId) searchParams.set('opdVisitId', String(params.opdVisitId))
    if (params?.patientId) searchParams.set('patientId', String(params.patientId))
    const qs = searchParams.toString()
    return apiClient.get(`${BASE}/orders${qs ? `?${qs}` : ''}`).then((r) => r.data)
  },

  getLabOrder: (id: number): Promise<LabOrder> =>
    apiClient.get(`${BASE}/orders/${id}`).then((r) => r.data),

  getOrderItem: (orderItemId: number): Promise<LabOrderItem> =>
    apiClient.get(`${BASE}/orders/items/${orderItemId}`).then((r) => r.data),

  getOrdersByIpdAdmission: (ipdAdmissionId: number): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/ipd/${ipdAdmissionId}`).then((r) => r.data),

  getOrdersByOpdVisit: (opdVisitId: number): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/opd/${opdVisitId}`).then((r) => r.data),

  getOrdersByPatient: (patientId: number): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/patient/${patientId}`).then((r) => r.data),

  getOrdersByStatus: (status: TestStatus): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/status/${status}`).then((r) => r.data),

  // Sample Collection
  collectSample: (testOrderId: number, request: SampleCollectionRequest): Promise<TestOrder> =>
    apiClient.post(`${BASE}/samples/collect?testOrderId=${testOrderId}`, request).then((r) => r.data),

  rejectSample: (testOrderId: number, rejectionReason: string): Promise<TestOrder> =>
    apiClient.post(`${BASE}/samples/reject?testOrderId=${testOrderId}&rejectionReason=${encodeURIComponent(rejectionReason)}`).then((r) => r.data),

  // Lab Processing
  enterResult: (request: LabResultEntryRequest): Promise<LabResult> =>
    apiClient.post(`${BASE}/result`, request).then((r) => r.data),

  getResultsByOrderItem: (orderItemId: number): Promise<LabResult[]> =>
    apiClient.get(`${BASE}/result/order-item/${orderItemId}`).then((r) => r.data),

  getPendingVerificationItems: (): Promise<LabOrderItem[]> =>
    apiClient.get(`${BASE}/result-verification/pending`).then((r) => r.data),

  verifyResult: (orderItemId: number, action: 'VERIFY' | 'REJECT'): Promise<LabOrderItem> =>
    apiClient.put(`${BASE}/result/verify/${orderItemId}?action=${action}`).then((r) => r.data),

  enterResults: (request: LabResultRequest): Promise<LabResult[]> =>
    apiClient.post(`${BASE}/results`, request).then((r) => r.data),

  getResultsByOrder: (testOrderId: number): Promise<LabResult[]> =>
    apiClient.get(`${BASE}/results/order/${testOrderId}`).then((r) => r.data),

  // Report Verification & Release
  generateReport: (testOrderId: number): Promise<LabReport> =>
    apiClient.post(`${BASE}/reports/generate?testOrderId=${testOrderId}`).then((r) => r.data),

  verifyReport: (reportId: number, supervisorSignature: string): Promise<LabReport> =>
    apiClient.post(`${BASE}/reports/${reportId}/verify?supervisorSignature=${encodeURIComponent(supervisorSignature)}`).then((r) => r.data),

  releaseReport: (reportId: number): Promise<LabReport> =>
    apiClient.post(`${BASE}/reports/${reportId}/release`).then((r) => r.data),

  getReportByOrder: (testOrderId: number): Promise<LabReport> =>
    apiClient.get(`${BASE}/reports/order/${testOrderId}`).then((r) => r.data),

  downloadReportPdf: async (orderId: number): Promise<Blob> => {
    const res = await apiClient.get(`${BASE}/report/${orderId}/pdf`, { responseType: 'blob' })
    return res.data
  },

  // Dashboard
  getDashboardSummary: (): Promise<LabDashboardSummary> =>
    apiClient.get(`${BASE}/dashboard/summary`).then((r) => r.data),

  getDashboardMetrics: (): Promise<LabDashboardMetrics> =>
    apiClient.get(`${BASE}/dashboard/metrics`).then((r) => r.data),

  getDashboardOverview: (): Promise<LabDashboardOverview> =>
    apiClient.get(`${BASE}/dashboard/overview`).then((r) => r.data),

  getTodaySummary: (): Promise<LabTodaySummary> =>
    apiClient.get(`${BASE}/dashboard/today-summary`).then((r) => r.data),

  getPendingCollection: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/samples/pending-collection`).then((r) => r.data),

  getEmergencySamples: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/samples/emergency`).then((r) => r.data),

  getPendingVerification: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/reports/pending-verification`).then((r) => r.data),

  getTatBreaches: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/tat/breaches`).then((r) => r.data),

  // Sample Processing (by orderItemId)
  getPendingProcessingItems: (): Promise<LabOrderItem[]> =>
    apiClient.get(`${BASE}/sample-processing/pending`).then((r) => r.data),

  processSampleItem: (orderItemId: number, action: 'START' | 'COMPLETE'): Promise<LabOrderItem> =>
    apiClient.put(`${BASE}/sample/process/${orderItemId}?action=${action}`).then((r) => r.data),

  // Legacy: by testOrderId (START only)
  startProcessing: (testOrderId: number): Promise<TestOrder> =>
    apiClient.put(`${BASE}/sample/process/test-order/${testOrderId}?action=START`).then((r) => r.data),

  // Unified Dashboard (cards + today's activity)
  getDashboard: (): Promise<LabDashboardResponse> =>
    apiClient.get(`${BASE}/dashboard`).then((r) => r.data),

  // Reports Search - GET /api/lab/reports
  searchReports: (params?: {
    uhid?: string
    patientName?: string
    testName?: string
    fromDate?: string
    toDate?: string
  }): Promise<LabReport[]> => {
    const searchParams = new URLSearchParams()
    if (params?.uhid) searchParams.set('uhid', params.uhid)
    if (params?.patientName) searchParams.set('patientName', params.patientName)
    if (params?.testName) searchParams.set('testName', params.testName)
    if (params?.fromDate) searchParams.set('fromDate', params.fromDate)
    if (params?.toDate) searchParams.set('toDate', params.toDate)
    const qs = searchParams.toString()
    return apiClient.get(`${BASE}/reports${qs ? `?${qs}` : ''}`).then((r) => r.data)
  },
}
