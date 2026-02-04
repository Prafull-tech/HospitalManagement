import type { ViewAdmissionBillingSummary } from '../../types/ipdAdmission.types'
import type { ViewAdmissionAdmission } from '../../types/ipdAdmission.types'
import styles from './ViewAdmissionCards.module.css'

export interface BillingSummaryCardProps {
  billingSummary: ViewAdmissionBillingSummary
  admission: ViewAdmissionAdmission
}

function formatCurrency(amount: number | undefined): string {
  if (amount == null || Number.isNaN(amount)) return '₹0.00'
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
  }).format(amount)
}

export function BillingSummaryCard({ billingSummary, admission }: BillingSummaryCardProps) {
  return (
    <section className={styles.card} aria-label="Billing and insurance">
      <h2 className={styles.cardTitle}>Billing & Insurance (Read-Only)</h2>
      <div className={styles.grid}>
        <div>
          <span className={styles.label}>Deposit Amount</span>
          <div className={styles.value}>
            {formatCurrency(billingSummary.totalDeposit ?? admission.depositAmount)}
          </div>
        </div>
        <div>
          <span className={styles.label}>Package Name</span>
          <div className={styles.value}>—</div>
        </div>
        <div>
          <span className={styles.label}>Insurance / TPA</span>
          <div className={styles.value}>{admission.insuranceTpa ?? '—'}</div>
        </div>
        <div>
          <span className={styles.label}>Billing Status</span>
          <div className={styles.value}>{billingSummary.billingStatus ?? 'Pending'}</div>
        </div>
        <div>
          <span className={styles.label}>Total Charges</span>
          <div className={styles.value}>{formatCurrency(billingSummary.totalCharges)}</div>
        </div>
        <div>
          <span className={styles.label}>Charge Lines</span>
          <div className={styles.value}>{billingSummary.chargeCount ?? 0}</div>
        </div>
      </div>
    </section>
  )
}
