import { useState } from 'react'
import { IpdIssueQueueCard } from '../components/pharmacy/IpdIssueQueueCard'
import { FefoStockViewCard } from '../components/pharmacy/FefoStockViewCard'
import { ExpiryAlertsCard } from '../components/pharmacy/ExpiryAlertsCard'
import { PharmacySummaryCard } from '../components/pharmacy/PharmacySummaryCard'
import { AddMedicineModal } from '../components/pharmacy/AddMedicineModal'
import { usePharmacyPermissions } from '../hooks/usePharmacyPermissions'

type TabId = 'ISSUE_QUEUE' | 'FEFO_STOCK' | 'ALERTS' | 'SUMMARY'

export function PharmacyDashboard() {
  const [activeTab, setActiveTab] = useState<TabId>('ISSUE_QUEUE')
  const [showAddMedicine, setShowAddMedicine] = useState(false)
  const { canAddMedicine } = usePharmacyPermissions()

  return (
    <div className="d-flex flex-column gap-3">
      <div className="d-flex justify-content-between align-items-center">
        <div>
          <h2 className="h5 mb-1 fw-bold">Pharmacy / Medical Store</h2>
          <p className="text-muted small mb-0">
            IPD-focused pharmacy command center. System thinks first, pharmacist confirms.
          </p>
        </div>
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

      <ul className="nav nav-tabs">
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'ISSUE_QUEUE' ? 'active' : ''}`}
            onClick={() => setActiveTab('ISSUE_QUEUE')}
          >
            IPD Medicine Issue Queue
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'FEFO_STOCK' ? 'active' : ''}`}
            onClick={() => setActiveTab('FEFO_STOCK')}
          >
            FEFO Stock View
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'ALERTS' ? 'active' : ''}`}
            onClick={() => setActiveTab('ALERTS')}
          >
            Expiry &amp; Critical Alerts
          </button>
        </li>
        <li className="nav-item">
          <button
            type="button"
            className={`nav-link ${activeTab === 'SUMMARY' ? 'active' : ''}`}
            onClick={() => setActiveTab('SUMMARY')}
          >
            Today&apos;s Summary
          </button>
        </li>
      </ul>

      <div className="mt-3">
        {activeTab === 'ISSUE_QUEUE' && <IpdIssueQueueCard />}
        {activeTab === 'FEFO_STOCK' && <FefoStockViewCard />}
        {activeTab === 'ALERTS' && <ExpiryAlertsCard />}
        {activeTab === 'SUMMARY' && <PharmacySummaryCard />}
      </div>

      {canAddMedicine && (
        <AddMedicineModal open={showAddMedicine} onClose={() => setShowAddMedicine(false)} />
      )}
    </div>
  )
}
