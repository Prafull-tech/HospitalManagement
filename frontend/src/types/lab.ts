/**
 * Laboratory Information System (LIS) TypeScript types
 */

export type TestCategory =
  | 'HEMATOLOGY'
  | 'BIOCHEMISTRY'
  | 'SEROLOGY_IMMUNOLOGY'
  | 'MICROBIOLOGY'
  | 'HISTOPATHOLOGY_CYTOLOGY'
  | 'EMERGENCY_ICU_PANEL'

export type SampleType =
  | 'BLOOD'
  | 'URINE'
  | 'STOOL'
  | 'SPUTUM'
  | 'CSF'
  | 'PLEURAL_FLUID'
  | 'PERITONEAL_FLUID'
  | 'SWAB'
  | 'TISSUE'
  | 'OTHER'

export type TestStatus =
  | 'ORDERED'
  | 'COLLECTED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'VERIFIED'
  | 'RELEASED'
  | 'REJECTED'
  | 'CANCELLED'

export type TATStatus = 'WITHIN_TAT' | 'BREACH'

export type PriorityLevel = 'ROUTINE' | 'PRIORITY' | 'STAT'

export type ReportStatus = 'DRAFT' | 'VERIFIED' | 'RELEASED'

export interface TestMaster {
  id: number
  testCode: string
  testName: string
  category: TestCategory
  sampleType: SampleType
  unit?: string
  normalRange?: string
  normalTATMinutes: number
  price: number
  active: boolean
  priorityLevel: PriorityLevel
  isPanel: boolean
  panelTestCodes?: string
  description?: string
  instructions?: string
  createdByUser?: string
  createdAt: string
  updatedAt: string
}

export interface TestMasterRequest {
  testCode: string
  testName: string
  category: TestCategory
  sampleType: SampleType
  normalTATMinutes: number
  price: number
  unit?: string
  normalRange?: string
  active?: boolean
  priorityLevel?: PriorityLevel
  isPanel?: boolean
  panelTestCodes?: string
  description?: string
  instructions?: string
}

export interface TestOrder {
  id: number
  orderNumber: string
  patientId: number
  patientUhid: string
  patientName: string
  testMasterId: number
  testCode: string
  testName: string
  sampleType?: SampleType
  doctorId: number
  doctorName: string
  ipdAdmissionId?: number
  ipdAdmissionNumber?: string
  opdVisitId?: number
  opdVisitNumber?: string
  status: TestStatus
  orderedAt: string
  sampleCollectedAt?: string
  collectedBy?: string
  wardName?: string
  bedNumber?: string
  resultEnteredAt?: string
  resultEnteredBy?: string
  verifiedAt?: string
  verifiedBy?: string
  releasedAt?: string
  releasedBy?: string
  tatStartTime?: string
  tatEndTime?: string
  tatStatus?: TATStatus
  tatBreachReason?: string
  rejectionReason?: string
  cancellationReason?: string
  clinicalNotes?: string
  isPriority: boolean
  billingChargePosted: boolean
  billingChargeId?: number
}

export interface TestOrderRequest {
  testMasterId: number
  doctorId: number
  ipdAdmissionId?: number
  opdVisitId?: number
  clinicalNotes?: string
  isPriority?: boolean
}

export type LabOrderPriority = 'NORMAL' | 'EMERGENCY'

export type LabOrderStatus = 'ORDERED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type LabOrderItemStatus = 'ORDERED' | 'COLLECTED' | 'IN_PROGRESS' | 'COMPLETED' | 'VERIFIED' | 'RELEASED' | 'REJECTED' | 'CANCELLED'

export type LabOrderItemSampleStatus = 'PENDING' | 'COLLECTED' | 'REJECTED'

export interface LabOrderItem {
  id: number
  orderId: number
  testId: number
  testCode: string
  testName: string
  status: LabOrderItemStatus
  sampleStatus: LabOrderItemSampleStatus
  testOrderId?: number
  /** Display fields for sample processing list */
  orderNumber?: string
  patientUhid?: string
  patientName?: string
  sampleCollectedAt?: string
  isPriority?: boolean
  /** COLLECTED -> Start Processing, IN_PROGRESS -> Mark Processed */
  testOrderStatus?: TestStatus
  resultEnteredAt?: string
  resultEnteredBy?: string
}

export interface LabOrder {
  id: number
  patientId: number
  uhid: string
  patientName: string
  ipdAdmissionId?: number
  ipdAdmissionNumber?: string
  opdVisitId?: number
  opdVisitNumber?: string
  orderedByDoctorId: number
  orderedByDoctorName: string
  priority: LabOrderPriority
  status: LabOrderStatus
  orderedAt: string
  items: LabOrderItem[]
}

export interface LabOrderRequest {
  orderedByDoctorId: number
  doctorId?: number
  patientId?: number
  ipdAdmissionId?: number
  opdVisitId?: number
  priority?: LabOrderPriority
  isPriority?: boolean
  testIds?: number[]
  testMasterId?: number
}

export interface SampleCollectionRequest {
  testOrderId: number
  wardName?: string
  bedNumber?: string
  remarks?: string
}

export interface LabResult {
  id: number
  testOrderId: number
  parameterName?: string
  resultValue?: string
  unit?: string
  normalRange?: string
  flag?: string
  enteredAt: string
  enteredBy?: string
  remarks?: string
  isCritical: boolean
}

/** Request for POST /api/lab/result (single result by orderItemId) */
export interface LabResultEntryRequest {
  orderItemId: number
  testValue: string
  unit?: string
  referenceRange?: string
  remarks?: string
}

export interface LabResultRequest {
  testOrderId: number
  parameters: ResultParameter[]
  remarks?: string
}

export interface ResultParameter {
  parameterName?: string
  resultValue?: string
  unit?: string
  normalRange?: string
  flag?: string
  isCritical?: boolean
}

export interface LabReport {
  id: number
  reportNumber: string
  testOrderId: number
  orderNumber: string
  patientName?: string
  patientUhid?: string
  testName?: string
  status: ReportStatus
  generatedAt?: string
  generatedBy?: string
  verifiedAt?: string
  verifiedBy?: string
  releasedAt?: string
  releasedBy?: string
  reportContent?: string
  interpretation?: string
  supervisorSignature?: string
  correctionLog?: string
  isReadOnly: boolean
  pdfPath?: string
}

/** GET /api/lab/dashboard response */
export interface LabDashboardResponse {
  pendingCollection: number
  pendingProcessing: number
  pendingVerification: number
  tatBreaches: number
  emergencySamples: number
  todayOrdered: number
  todayCollected: number
  todayCompleted: number
  todayVerified: number
  tatCompliancePercent: number
}

export interface LabDashboardSummary {
  pendingCollectionCount: number
  pendingProcessingCount: number
  pendingVerificationCount: number
  completedTodayCount: number
  tatBreachCount: number
  emergencySamplesCount: number
  pendingCollection: TestOrder[]
  pendingProcessing: TestOrder[]
  pendingVerification: TestOrder[]
  tatBreaches: TestOrder[]
  emergencySamples: TestOrder[]
}

export interface LabDashboardMetrics {
  pendingCollection: number
  pendingVerification: number
  tatBreaches: number
  emergencySamples: number
}

export interface LabDashboardOverview {
  totalOrderedToday: number
  testsCollectedToday: number
  testsCompletedToday: number
  testsVerifiedToday: number
  tatCompliancePercent: number
}

export interface LabTodaySummary {
  date: string
  completedTestsToday: number
  pendingSamplesToday: number
  tatCompliancePercent: number
  emergencyTestsHandledToday: number
}
