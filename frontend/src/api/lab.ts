import { apiClient } from './client'
import type {
  TestMaster,
  TestMasterRequest,
  TestOrder,
  TestOrderRequest,
  SampleCollectionRequest,
  LabResult,
  LabResultRequest,
  LabReport,
  LabDashboardSummary,
  LabDashboardMetrics,
  LabDashboardOverview,
  LabTodaySummary,
  TestStatus,
} from '../types/lab'

const BASE = '/lab'

export const labApi = {
  // Test Master Management
  createTestMaster: (request: TestMasterRequest): Promise<TestMaster> =>
    apiClient.post(`${BASE}/test-masters`, request),

  listTestMasters: (active?: boolean): Promise<TestMaster[]> => {
    const params = active !== undefined ? `?active=${active}` : ''
    return apiClient.get(`${BASE}/test-masters${params}`)
  },

  getTestMaster: (id: number): Promise<TestMaster> =>
    apiClient.get(`${BASE}/test-masters/${id}`),

  updateTestMaster: (id: number, request: TestMasterRequest): Promise<TestMaster> =>
    apiClient.put(`${BASE}/test-masters/${id}`, request),

  deleteTestMaster: (id: number): Promise<void> =>
    apiClient.delete(`${BASE}/test-masters/${id}`),

  expandPanel: (panelCode: string): Promise<string[]> =>
    apiClient.get(`${BASE}/test-masters/panels/${panelCode}/expand`),

  // Test Ordering
  createOrder: (request: TestOrderRequest): Promise<TestOrder[]> =>
    apiClient.post(`${BASE}/orders`, request),

  getOrder: (id: number): Promise<TestOrder> =>
    apiClient.get(`${BASE}/orders/${id}`),

  getOrdersByIpdAdmission: (ipdAdmissionId: number): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/ipd/${ipdAdmissionId}`),

  getOrdersByOpdVisit: (opdVisitId: number): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/opd/${opdVisitId}`),

  getOrdersByPatient: (patientId: number): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/patient/${patientId}`),

  getOrdersByStatus: (status: TestStatus): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/orders/status/${status}`),

  // Sample Collection
  collectSample: (testOrderId: number, request: SampleCollectionRequest): Promise<TestOrder> =>
    apiClient.post(`${BASE}/samples/collect?testOrderId=${testOrderId}`, request),

  rejectSample: (testOrderId: number, rejectionReason: string): Promise<TestOrder> =>
    apiClient.post(`${BASE}/samples/reject?testOrderId=${testOrderId}&rejectionReason=${encodeURIComponent(rejectionReason)}`),

  // Lab Processing
  enterResults: (request: LabResultRequest): Promise<LabResult[]> =>
    apiClient.post(`${BASE}/results`, request),

  getResultsByOrder: (testOrderId: number): Promise<LabResult[]> =>
    apiClient.get(`${BASE}/results/order/${testOrderId}`),

  // Report Verification & Release
  generateReport: (testOrderId: number): Promise<LabReport> =>
    apiClient.post(`${BASE}/reports/generate?testOrderId=${testOrderId}`),

  verifyReport: (reportId: number, supervisorSignature: string): Promise<LabReport> =>
    apiClient.post(`${BASE}/reports/${reportId}/verify?supervisorSignature=${encodeURIComponent(supervisorSignature)}`),

  releaseReport: (reportId: number): Promise<LabReport> =>
    apiClient.post(`${BASE}/reports/${reportId}/release`),

  getReportByOrder: (testOrderId: number): Promise<LabReport> =>
    apiClient.get(`${BASE}/reports/order/${testOrderId}`),

  // Dashboard
  getDashboardSummary: (): Promise<LabDashboardSummary> =>
    apiClient.get(`${BASE}/dashboard/summary`),

  getDashboardMetrics: (): Promise<LabDashboardMetrics> =>
    apiClient.get(`${BASE}/dashboard/metrics`),

  getDashboardOverview: (): Promise<LabDashboardOverview> =>
    apiClient.get(`${BASE}/dashboard/overview`),

  getTodaySummary: (): Promise<LabTodaySummary> =>
    apiClient.get(`${BASE}/dashboard/today-summary`),

  getPendingCollection: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/samples/pending-collection`),

  getEmergencySamples: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/samples/emergency`),

  getPendingVerification: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/reports/pending-verification`),

  getTatBreaches: (): Promise<TestOrder[]> =>
    apiClient.get(`${BASE}/tat/breaches`),
}
