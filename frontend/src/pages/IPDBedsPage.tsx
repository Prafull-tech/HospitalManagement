import { useState, useEffect } from 'react'
import { ipdApi } from '../api/ipd'
import type { BedAvailabilityResponse } from '../types/ipd'
import styles from './IPDBedsPage.module.css'

export function IPDBedsPage() {
  const [beds, setBeds] = useState<BedAvailabilityResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    ipdApi
      .getBedAvailability()
      .then(setBeds)
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load bed availability')
        setBeds([])
      })
      .finally(() => setLoading(false))
  }, [])

  const byWard = beds.reduce<Record<string, BedAvailabilityResponse[]>>((acc, b) => {
    const key = `${b.wardId}-${b.wardName}`
    if (!acc[key]) acc[key] = []
    acc[key].push(b)
    return acc
  }, {})
  const wardGroups = Object.entries(byWard)

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h2 className={styles.cardTitle}>Bed availability</h2>
        {error && <div className={styles.error}>{error}</div>}
        {loading && <div className={styles.loading}>Loading…</div>}
        {!loading && wardGroups.length === 0 && (
          <p className={styles.empty}>No wards or beds configured. Run backend with IPD data loader.</p>
        )}
        {!loading && wardGroups.length > 0 && (
          <div className={styles.wardList}>
            {wardGroups.map(([key, wardBeds]) => {
              const w = wardBeds[0]
              return (
                <div key={key} className={styles.wardBlock}>
                  <h3 className={styles.wardName}>
                    {w.wardName} <span className={styles.wardCode}>({w.wardCode})</span>
                  </h3>
                  <div className={styles.bedGrid}>
                    {wardBeds.map((b) => (
                      <div
                        key={b.bedId}
                        className={`${styles.bedItem} ${b.available ? styles.bedAvailable : styles.bedOccupied}`}
                      >
                        <span className={styles.bedNumber}>{b.bedNumber}</span>
                        <span className={styles.bedStatus}>{b.available ? 'Available' : 'Occupied'}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
