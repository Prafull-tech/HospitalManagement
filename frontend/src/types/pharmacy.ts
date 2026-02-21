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
  rackCode?: string
  shelfCode?: string
  binNumber?: string
}

/** FEFO batch suggestion for a medicine. */
export interface BatchSuggestion {
  batchNo?: string
  expiryDate?: string
  availableQty: number
  rackLocation?: string
}

/** Medicine line in the medication issue queue. */
export interface IssueQueueMedicine {
  orderId: number
  medicineId: number
  medicineName: string
  quantity: number
  dosage?: string
  route?: string
  lasa: boolean
  fefoSuggestion?: BatchSuggestion | null
}

/** Patient-level item in the medication issue queue. */
export interface IssueQueuePatient {
  patientName: string
  uhid?: string
  ipdNo?: string
  opdVisitNo?: string
  wardType: string
  bed?: string
  priority: string
  medicines: IssueQueueMedicine[]
  orderIds: number[]
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

export interface ManualEntryRequest {
  medicine: MedicineRequest
  batchNumber?: string
  expiryDate?: string
  quantity?: number
  externalLookupBarcode?: string
}

export interface BarcodeEntryRequest {
  barcode: string
  batchNumber?: string
  expiryDate?: string
  quantity: number
  rackId?: number
  shelfId?: number
  createNewMedicine?: MedicineRequest
  fromExternalLookup?: boolean
}

export interface ExistingBatchRequest {
  medicineId: number
  batchNumber?: string
  expiryDate?: string
  quantity: number
  rackId?: number
  shelfId?: number
}

export interface MedicineRequest {
  medicineCode: string
  medicineName: string
  category: string
  strength: string
  form: string
  minStock: number
  quantity?: number
  lasaFlag: boolean
  storageType: string
  active: boolean
  manufacturer?: string
  notes?: string
  rackId?: number
  shelfId?: number
  binNumber?: string
  barcode?: string
}

export interface MedicineResponse extends MedicineRequest {
  id: number
  createdAt: string
  createdBy?: string
  createdByUser?: string
  updatedAt: string
  rackCode?: string
  shelfCode?: string
}

export type MedicineLookupSource = 'LOCAL' | 'EXTERNAL'

export interface MedicineLookupResponse {
  source: MedicineLookupSource
  data: MedicineResponse
}

export type LocationArea = 'MAIN_STORE' | 'ICU_STORE' | 'COLD_ROOM'

export interface RackRequest {
  rackCode: string
  rackName: string
  locationArea: LocationArea
  storageType: string
  categoryType?: string
  lasaSafe?: boolean
  maxCapacity?: number
  active?: boolean
}

export interface ShelfRequest {
  shelfCode: string
  shelfLevel: number
  active?: boolean
  binNumber?: string
}

export interface ShelfResponse {
  id: number
  rackId: number
  shelfCode: string
  shelfLevel: number
  active: boolean
  binNumber?: string
}

export interface RackResponse {
  id: number
  rackCode: string
  rackName: string
  locationArea: LocationArea
  storageType: string
  categoryType?: string
  lasaSafe?: boolean
  maxCapacity?: number
  active: boolean
  createdAt: string
  shelves?: ShelfResponse[]
}

export interface RackSuggestion {
  rackId: number
  rackCode: string
  rackName?: string
  shelfId: number
  shelfCode: string
  reason: string
}

export interface RackInventoryItem {
  medicineId: number
  medicineCode: string
  medicineName: string
  storageType: string
  lasa: boolean
  shelfCode?: string
  shelfLevel?: number
  binNumber?: string
  batchCount: number
  nearestExpiry?: string
  expiryRiskClass: string
}

export interface RackInventory {
  rackId: number
  rackCode: string
  rackName: string
  locationArea: string
  storageType: string
  items: RackInventoryItem[]
}

export interface MedicineImportError {
  row: number
  error: string
}

export interface MedicineImportResult {
  totalRows: number
  successCount: number
  failedCount: number
  errors: MedicineImportError[]
}

// Purchase & Sell (Stock In/Out)
export interface PurchaseRequest {
  medicineId: number
  quantity: number
  transactionDate: string // YYYY-MM-DD
  batchNumber?: string
  expiryDate?: string
  supplier?: string
  costPerUnit?: number
  notes?: string
}

export type SaleType = 'PATIENT' | 'MANUAL'

export interface PatientIpdStatus {
  ipdAdmissionId: number | null
  admissionNumber: string | null
  wardName: string | null
  bedNumber: string | null
  ipdLinked: boolean
}

export interface SellLineItem {
  medicineId: number
  quantity: number
}

export interface SellRequest {
  transactionDate: string // YYYY-MM-DD
  saleType?: SaleType
  patientId?: number
  manualPatientName?: string
  manualPhone?: string
  manualEmail?: string
  manualAddress?: string
  reference?: string
  notes?: string
  /** Multi-item mode. When present, used instead of single medicineId/quantity. */
  lineItems?: SellLineItem[]
  /** Single-item mode (backward compat) */
  medicineId?: number
  quantity?: number
}

export type StockTransactionType = 'PURCHASE' | 'SELL'

export interface PharmacySellResponse {
  success: boolean
  invoiceNumber?: string
  pdfUrl?: string
  transaction?: StockTransactionResponse
}

export interface StockTransactionResponse {
  id: number
  medicineId: number
  medicineCode: string
  medicineName: string
  transactionType: StockTransactionType
  quantity: number
  transactionDate: string
  batchNumber?: string
  expiryDate?: string
  supplier?: string
  reference?: string
  saleType?: SaleType
  patientId?: number
  manualPatientName?: string
  manualPhone?: string
  manualEmail?: string
  manualAddress?: string
  costPerUnit?: number
  notes?: string
  performedBy: string
  performedAt: string
}


