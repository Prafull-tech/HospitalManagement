import { useNavigate } from 'react-router-dom'
import styles from './BillingDashboard.module.css'

const BILLING_CARDS = [
  { path: '/billing/ipd', title: 'IPD Billing', desc: 'View and manage billing for IPD admissions. Access patient accounts, finalize bills, and record payments.' },
  { path: '/billing/corporate', title: 'Corporate Billing', desc: 'Corporate tie-up accounts, credit limits, and corporate invoice generation.' },
  { path: '/billing/emi', title: 'EMI Billing', desc: 'Create EMI plans for billing accounts. Allow discharge with down payment and tenure.' },
  { path: '/billing/payment/online', title: 'Online Payment', desc: 'Razorpay / UPI / Card integration for online payment collection.' },
  { path: '/billing/opd/group', title: 'OPD Group Bill', desc: 'Group multiple OPD visits into one consolidated bill.' },
  { path: '/billing/tpa', title: 'Insurance / TPA', desc: 'Insurance claims, TPA approvals, and pre-authorization tracking.' },
  { path: '/billing/payments', title: 'Payments', desc: 'Payment collection (Cash / Card / UPI), receipts, and payment history.' },
  { path: '/billing/refunds', title: 'Refunds', desc: 'Refund requests and processing.' },
] as const

export function BillingDashboard() {
  const navigate = useNavigate()

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Billing & Accounts</h1>
        <p className={styles.subtitle}>Centralized billing, insurance, and financials</p>
      </header>

      <div className={styles.cards}>
        {BILLING_CARDS.map(({ path, title, desc }) => (
          <button
            key={path}
            type="button"
            className={styles.card}
            onClick={() => navigate(path)}
          >
            <h2>{title}</h2>
            <p>{desc}</p>
          </button>
        ))}
      </div>
    </div>
  )
}
