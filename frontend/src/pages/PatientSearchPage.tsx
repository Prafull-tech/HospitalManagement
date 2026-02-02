import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { receptionApi } from '../api/reception'
import type { PatientResponse } from '../types/patient'
import type { ApiError } from '../types/patient'
import styles from './PatientSearchPage.module.css'

function SearchIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35" />
    </svg>
  )
}

export function PatientSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const initialUhid = searchParams.get('uhid') ?? ''
  const [uhid, setUhid] = useState(initialUhid)
  const [phone, setPhone] = useState('')
  const [name, setName] = useState('')
  const [results, setResults] = useState<PatientResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searched, setSearched] = useState(false)

  useEffect(() => {
    setUhid(initialUhid)
  }, [initialUhid])

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setResults([])
    setSearched(true)
    if (!uhid.trim() && !phone.trim() && !name.trim()) {
      setError('Enter UHID, phone, or name to search.')
      return
    }
    setLoading(true)
    try {
      const data = await receptionApi.search({
        uhid: uhid.trim() || undefined,
        phone: phone.trim() || undefined,
        name: name.trim() || undefined,
      })
      setResults(data)
    } catch (err: unknown) {
      const ax = err as { response?: { data?: ApiError } }
      setError(ax.response?.data?.message || 'Search failed.')
    } finally {
      setLoading(false)
    }
  }

  const clearSearch = () => {
    setUhid('')
    setPhone('')
    setName('')
    setResults([])
    setError('')
    setSearched(false)
    setSearchParams({})
  }

  return (
    <div className={styles.page}>
      <form onSubmit={handleSearch} className={styles.form}>
        {error && <div className={styles.error}>{error}</div>}
        <div className={styles.row}>
          <label>
            UHID
            <input
              type="text"
              value={uhid}
              onChange={(e) => setUhid(e.target.value)}
              placeholder="e.g. HMS-2025-000001"
            />
          </label>
          <label>
            Phone
            <input
              type="text"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="Phone number"
            />
          </label>
          <label>
            Name (partial)
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Full or partial name"
            />
          </label>
        </div>
        <div className={styles.actions}>
          <button type="submit" disabled={loading} className={styles.submit}>
            <span className={styles.submitIcon}><SearchIcon /></span>
            {loading ? 'Searching…' : 'Search'}
          </button>
          <button type="button" onClick={clearSearch} className={styles.cancel}>
            Clear
          </button>
        </div>
      </form>

      {searched && !loading && (
        <div className={styles.results}>
          <h2 className={styles.resultsTitle}>
            {results.length === 0 ? 'No patients found' : `${results.length} patient(s) found`}
          </h2>
          {results.length > 0 && (
            <div className={styles.resultsListWrap}>
            <ul className={styles.list}>
              {results.map((p) => (
                <li key={p.id} className={styles.card}>
                  <div className={styles.cardRow}>
                    <span className={styles.uhid}>{p.uhid}</span>
                    <span className={styles.fullName}>{p.fullName}</span>
                  </div>
                  <div className={styles.cardMeta}>
                    {p.age} yrs · {p.gender}
                    {p.phone && ` · ${p.phone}`}
                  </div>
                  {p.address && <div className={styles.address}>{p.address}</div>}
                </li>
              ))}
            </ul>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
