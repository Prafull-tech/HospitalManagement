export type PharmacyPriority = 'ICU' | 'EMERGENCY' | 'HIGH' | 'ROUTINE'

export interface IpdIssueQueueLine {
  medicineCode: string
  medicineName: string
  requestedQty: number
  availableQty: number
  nextBatchNumber?: string
  nextBatchExpiryDisplay?: string
  expiryRiskClass: 'text-success' | 'text-warning' | 'text-danger' | 'text-muted'
  lasa: boolean
}

export interface IpdIssueQueueItem {
  indentId: number
  ipdAdmissionId: number
  ipdAdmissionNumber: string
  patientName: string
  wardName: string
  bedNumber: string
  priority: PharmacyPriority
  medicineCount: number
  orderedAtDisplay: string
  waitingMinutes: number
  status: 'PENDING' | 'DELAYED'
  lines: IpdIssueQueueLine[]
}

export type ExpiryRiskLevel = 'SAFE' | 'NEAR_EXPIRY' | 'CRITICAL' | 'EXPIRED'

export interface FefoStockRow {
  medicineCode: string
  medicineName: string
  batchNumber: string
  expiryDate: string
  quantityAvailable: number
  fefoRank: number
  riskLevel: ExpiryRiskLevel
  riskColorClass: 'text-success' | 'text-warning' | 'text-danger' | 'text-muted'
  lasa: boolean
  storageLocation?: string
}

export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL'

export interface ExpiryAlert {
  id: number
  medicineCode: string
  medicineName: string
  batchNumber: string
  expiryDate: string
  quantityRemaining: number
  riskLevel: ExpiryRiskLevel
  severity: AlertSeverity
  storageLocation?: string
  acknowledged: boolean
  createdAt: string
}

export interface PharmacySummary {
  date: string
  totalIndentsReceived: number
  totalIndentsIssued: number
  pendingIndents: number
  medicinesIssuedCount: number
  stockAdjustmentsCount: number
  overridesCount: number
  highRiskAlerts: number
}

export interface MedicineRequest {
  medicineCode: string
  medicineName: string
  category: string
  strength: string
  form: string
  minStock: number
  lasaFlag: boolean
  storageType: string
  active: boolean
  manufacturer?: string
  notes?: string
}

export interface MedicineResponse extends MedicineRequest {
  id: number
  createdAt: string
  createdBy?: string
  updatedAt: string
}


