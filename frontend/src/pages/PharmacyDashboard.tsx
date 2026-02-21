import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { IpdIssueQueueCard } from '../components/pharmacy/IpdIssueQueueCard'
import { FefoStockViewCard } from '../components/pharmacy/FefoStockViewCard'
import { ExpiryAlertsCard } from '../components/pharmacy/ExpiryAlertsCard'
import { PharmacySummaryCard } from '../components/pharmacy/PharmacySummaryCard'
import { MedicineListCard } from '../components/pharmacy/MedicineListCard'
import { RackManagementCard } from '../components/pharmacy/RackManagementCard'
import { StockTransactionsCard } from '../components/pharmacy/StockTransactionsCard'
import { MultiModeAddMedicineModal } from '../components/pharmacy/MultiModeAddMedicineModal'
import { PurchaseModal } from '../components/pharmacy/PurchaseModal'
import { SellModal } from '../components/pharmacy/SellModal'
import { usePharmacyPermissions } from '../hooks/usePharmacyPermissions'

type TabId = 'ISSUE_QUEUE' | 'FEFO_STOCK' | 'ALERTS' | 'SUMMARY' | 'MEDICINE_LIST' | 'RACK_MANAGEMENT' | 'STOCK_TRANSACTIONS'

const VALID_TABS: TabId[] = ['ISSUE_QUEUE', 'FEFO_STOCK', 'ALERTS', 'SUMMARY', 'MEDICINE_LIST', 'RACK_MANAGEMENT', 'STOCK_TRANSACTIONS']

export function PharmacyDashboard() {
  const [searchParams, setSearchParams] = useSearchParams()
  const tabFromUrl = searchParams.get('tab') as TabId | null
  const initialTab: TabId = tabFromUrl && VALID_TABS.includes(tabFromUrl) ? tabFromUrl : 'ISSUE_QUEUE'
  const [activeTab, setActiveTab] = useState<TabId>(initialTab)

  useEffect(() => {
    if (tabFromUrl && VALID_TABS.includes(tabFromUrl)) {
      setActiveTab(tabFromUrl)
    }
  }, [tabFromUrl])

  const handleTabChange = (tab: TabId) => {
    setActiveTab(tab)
    setSearchParams({ tab }, { replace: true })
  }
  const [showAddMedicine, setShowAddMedicine] = useState(false)
  const [showImportMedicine, setShowImportMedicine] = useState(false)
  const [editingMedicine, setEditingMedicine] = useState<import('../types/pharmacy').MedicineResponse | null>(null)
  const [showPurchase, setShowPurchase] = useState(false)
  const [showSell, setShowSell] = useState(false)
  const [purchasePreselect, setPurchasePreselect] = useState<import('../types/pharmacy').MedicineResponse | null>(null)
  const [sellPreselect, setSellPreselect] = useState<import('../types/pharmacy').MedicineResponse | null>(null)
  const [medicineListRefetchTrigger, setMedicineListRefetchTrigger] = useState(0)
  const [transactionsRefetchTrigger, setTransactionsRefetchTrigger] = useState(0)
  const { canAddMedicine, canImportMedicines, canEditMedicine, canDisableMedicine, canPurchase, canSell } =
    usePharmacyPermissions()

  const handleMedicineAdded = () => {
    setMedicineListRefetchTrigger((t) => t + 1)
    handleTabChange('MEDICINE_LIST')
  }

  const handleStockTransaction = () => {
    setMedicineListRefetchTrigger((t) => t + 1)
    setTransactionsRefetchTrigger((t) => t + 1)
    handleTabChange('STOCK_TRANSACTIONS')
  }

  return (
    <div className="d-flex flex-column gap-3">
      <div className="d-flex justify-content-between align-items-center">
        <div>
          <h2 className="h5 mb-1 fw-bold">Pharmacy / Medical Store</h2>
          <p className="text-muted small mb-0">
            IPD-focused pharmacy command center. System thinks first, pharmacist confirms.
          </p>
        </div>
        <div className="d-flex gap-2 flex-wrap">
          {canPurchase && (
            <button
              type="button"
              className="btn btn-success btn-sm"
              onClick={() => {
                setPurchasePreselect(null)
                setShowPurchase(true)
              }}
            >
              Purchase (Stock In)
            </button>
          )}
          {canSell && (
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() => {
                setSellPreselect(null)
                setShowSell(true)
              }}
            >
              Sell (Stock Out)
            </button>
          )}
          {canImportMedicines && (
            <button
              type="button"
              className="btn btn-outline-primary btn-sm"
              onClick={() => {
                setShowImportMedicine(true)
                setShowAddMedicine(true)
              }}
            >
              Import Medicines
            </button>
          )}
          {canAddMedicine && (
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() => setShowAddMedicine(true)}
            >
              Add Medicine
            </button>
          )}
        </div>
      </div>

      <ul className="nav nav-tabs">
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'ISSUE_QUEUE' ? 'active' : ''}`}
            onClick={() => handleTabChange('ISSUE_QUEUE')}
          >
            IPD Medicine Issue Queue
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'FEFO_STOCK' ? 'active' : ''}`}
            onClick={() => handleTabChange('FEFO_STOCK')}
          >
            FEFO Stock View
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'ALERTS' ? 'active' : ''}`}
            onClick={() => handleTabChange('ALERTS')}
          >
            Expiry &amp; Critical Alerts
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'SUMMARY' ? 'active' : ''}`}
            onClick={() => handleTabChange('SUMMARY')}
          >
            Today&apos;s Summary
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'MEDICINE_LIST' ? 'active' : ''}`}
            onClick={() => handleTabChange('MEDICINE_LIST')}
          >
            Medicine List
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'RACK_MANAGEMENT' ? 'active' : ''}`}
            onClick={() => handleTabChange('RACK_MANAGEMENT')}
          >
            Rack Management
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'STOCK_TRANSACTIONS' ? 'active' : ''}`}
            onClick={() => handleTabChange('STOCK_TRANSACTIONS')}
          >
            Stock Transactions
          </button>
        </li>
      </ul>

      <div className="mt-3">
        {activeTab === 'ISSUE_QUEUE' && <IpdIssueQueueCard />}
        {activeTab === 'FEFO_STOCK' && <FefoStockViewCard />}
        {activeTab === 'ALERTS' && <ExpiryAlertsCard />}
        {activeTab === 'SUMMARY' && <PharmacySummaryCard />}
        {activeTab === 'MEDICINE_LIST' && (
          <MedicineListCard
            refetchTrigger={medicineListRefetchTrigger}
            canEdit={canEditMedicine}
            canDisable={canDisableMedicine}
            canPurchase={canPurchase}
            canSell={canSell}
            onEditMedicine={setEditingMedicine}
            onPurchaseMedicine={(m) => {
              setPurchasePreselect(m)
              setShowPurchase(true)
            }}
            onSellMedicine={(m) => {
              setSellPreselect(m)
              setShowSell(true)
            }}
          />
        )}
        {activeTab === 'RACK_MANAGEMENT' && <RackManagementCard />}
        {activeTab === 'STOCK_TRANSACTIONS' && (
          <StockTransactionsCard refetchTrigger={transactionsRefetchTrigger} />
        )}
      </div>

      {(canAddMedicine || canEditMedicine || canImportMedicines) && (
        <MultiModeAddMedicineModal
          open={showAddMedicine || showImportMedicine || !!editingMedicine}
          onClose={() => {
            setShowAddMedicine(false)
            setShowImportMedicine(false)
            setEditingMedicine(null)
          }}
          onSuccess={handleMedicineAdded}
          medicine={editingMedicine}
          initialMode={showImportMedicine ? 'EXCEL' : 'MANUAL'}
        />
      )}
      {canPurchase && (
        <PurchaseModal
          open={showPurchase}
          onClose={() => {
            setShowPurchase(false)
            setPurchasePreselect(null)
          }}
          onSuccess={handleStockTransaction}
          preselectedMedicine={purchasePreselect}
        />
      )}
      {canSell && (
        <SellModal
          open={showSell}
          onClose={() => {
            setShowSell(false)
            setSellPreselect(null)
          }}
          onSuccess={handleStockTransaction}
          preselectedMedicine={sellPreselect}
        />
      )}
    </div>
  )
}
