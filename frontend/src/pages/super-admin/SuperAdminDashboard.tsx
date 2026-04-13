import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getDashboardSummary, type DashboardSummary } from '../../api/superAdmin'
import styles from './SuperAdmin.module.css'

const inrFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

export function SuperAdminDashboard() {
  const [data, setData] = useState<DashboardSummary | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getDashboardSummary()
      .then(setData)
      .catch((e) => setError(e?.response?.data?.message || 'Failed to load dashboard'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className={styles.loading}>Loading dashboard…</div>
  if (error) return <div className={styles.errorBanner}>{error}</div>
  if (!data) return null

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>Platform Dashboard</h1>
      </div>

      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Total Hospitals</p>
          <p className={styles.statValue}>{data.totalHospitals}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Active Hospitals</p>
          <p className={styles.statValue}>{data.activeHospitals}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Total Users</p>
          <p className={styles.statValue}>{data.totalUsers}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Active Subscriptions</p>
          <p className={styles.statValue}>{data.activeSubscriptions}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Trial Subscriptions</p>
          <p className={styles.statValue}>{data.trialSubscriptions}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Expired Subscriptions</p>
          <p className={styles.statValue}>{data.expiredSubscriptions}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Total Subscriptions</p>
          <p className={styles.statValue}>{data.totalSubscriptions}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Estimated Monthly Earnings</p>
          <p className={styles.statValue}>{inrFormatter.format(data.estimatedMonthlyEarnings || 0)}</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statLabel}>Current Month Earnings</p>
          <p className={styles.statValue}>{inrFormatter.format(data.estimatedCurrentMonthEarnings || 0)}</p>
        </div>
      </div>

      <div className={styles.sectionCard}>
        <h2 className={styles.detailTitle}>Subscription Earnings Breakdown</h2>
        {data.planBreakdown?.length ? (
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Plan</th>
                <th className={styles.tableNumber}>Active Subscriptions</th>
                <th className={styles.tableNumber}>Trial Subscriptions</th>
                <th className={styles.tableNumber}>Monthly Equivalent</th>
              </tr>
            </thead>
            <tbody>
              {data.planBreakdown.map((row) => (
                <tr key={row.planId || row.planCode}>
                  <td>
                    <div>{row.planName || row.planCode}</div>
                    <small className={styles.fieldHelp}>{row.planCode}</small>
                  </td>
                  <td className={styles.tableNumber}>{row.activeSubscriptions}</td>
                  <td className={styles.tableNumber}>{row.trialSubscriptions}</td>
                  <td className={styles.tableNumber}>{inrFormatter.format(row.estimatedMonthlyEarnings || 0)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className={styles.empty}>No subscription earnings data available.</div>
        )}
      </div>

      <div className={styles.toolbar}>
        <Link to="/super-admin/hospitals" className={styles.primaryBtn}>Manage Hospitals</Link>
        <Link to="/super-admin/subscriptions" className={styles.secondaryBtn}>View Subscriptions</Link>
        <Link to="/super-admin/subscriptions/plans" className={styles.secondaryBtn}>Subscription Plans</Link>
      </div>
    </div>
  )
}
