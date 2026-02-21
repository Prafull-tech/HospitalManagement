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
  normalTATMinutes: number
  price: number
  active: boolean
  priorityLevel: PriorityLevel
  isPanel: boolean
  panelTestCodes?: string
  description?: string
  normalRange?: string
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
  active?: boolean
  priorityLevel?: PriorityLevel
  isPanel?: boolean
  panelTestCodes?: string
  description?: string
  normalRange?: string
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

export interface LabDashboardSummary {
  pendingCollectionCount: number
  pendingVerificationCount: number
  completedTodayCount: number
  tatBreachCount: number
  emergencySamplesCount: number
  pendingCollection: TestOrder[]
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
