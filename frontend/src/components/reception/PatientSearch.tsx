/**
 * Reusable patient search: by UHID, ID, mobile/phone, or name.
 * Used on OPD Register, IPD Admit, and anywhere patient selection is needed.
 */

import { useState, useEffect, useCallback, useRef } from 'react'
import { receptionApi } from '../../api/reception'
import type { PatientResponse } from '../../types/patient'
import styles from './PatientSearch.module.css'

const DEBOUNCE_MS = 300

export interface PatientSearchProps {
  /** Current UHID (controlled). When set, shows selected patient card. */
  value?: string
  /** Display name when patient is selected (e.g. from parent state). */
  displayName?: string | null
  /** Called when user selects a patient. Pass null when "Change" clears selection. */
  onSelect: (patient: PatientResponse | null) => void
  /** Placeholder for the search input. */
  placeholder?: string
  /** Optional label. */
  label?: string
  /** Whether the field is required (shows asterisk). */
  required?: boolean
  /** Input id for accessibility. */
  id?: string
}

export function PatientSearch({
  value,
  displayName,
  onSelect,
  placeholder = 'Search by UHID, ID, mobile or name',
  label = 'Patient',
  required,
  id = 'patient-search',
}: PatientSearchProps) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<PatientResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const wrapperRef = useRef<HTMLDivElement>(null)

  const runSearch = useCallback((q: string) => {
    if (!q.trim()) {
      setResults([])
      setLoading(false)
      return
    }
    setLoading(true)
    receptionApi
      .searchQuery(q.trim())
      .then((list) => {
        setResults(list)
        setOpen(true)
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current)
    if (!query.trim()) {
      setResults([])
      setOpen(false)
      setLoading(false)
      return
    }
    debounceRef.current = setTimeout(() => runSearch(query), DEBOUNCE_MS)
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current)
    }
  }, [query, runSearch])

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleSelect = (patient: PatientResponse) => {
    onSelect(patient)
    setQuery('')
    setResults([])
    setOpen(false)
  }

  const handleClear = () => {
    onSelect(null)
    setQuery('')
    setResults([])
    setOpen(false)
  }

  const selected = value?.trim()
  const showSearch = !selected

  return (
    <div className={styles.wrapper} ref={wrapperRef}>
      {label && (
        <label htmlFor={showSearch ? id : undefined} className={styles.label}>
          {label} {required && <span className={styles.required}>*</span>}
        </label>
      )}
      {showSearch ? (
        <>
          <input
            id={id}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onFocus={() => results.length > 0 && setOpen(true)}
            placeholder={placeholder}
            className={styles.input}
            autoComplete="off"
            aria-autocomplete="list"
            aria-expanded={open}
          />
          {loading && <span className={styles.loading}>Searching…</span>}
          {open && results.length > 0 && (
            <ul className={styles.results} role="listbox">
              {results.map((p) => (
                <li
                  key={p.id}
                  role="option"
                  className={styles.resultItem}
                  onClick={() => handleSelect(p)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault()
                      handleSelect(p)
                    }
                  }}
                  tabIndex={0}
                >
                  <span className={styles.resultName}>{p.fullName}</span>
                  <span className={styles.resultMeta}>
                    {p.uhid} {p.phone ? ` · ${p.phone}` : ''}
                  </span>
                </li>
              ))}
            </ul>
          )}
          {open && !loading && query.trim() && results.length === 0 && (
            <div className={styles.noResults}>No patient found. Try UHID, ID, mobile or name.</div>
          )}
        </>
      ) : (
        <div className={styles.selectedCard}>
          <span className={styles.selectedName}>{displayName ?? value}</span>
          <span className={styles.selectedUhid}>{value}</span>
          <button type="button" className={styles.changeBtn} onClick={handleClear}>
            Change
          </button>
        </div>
      )}
    </div>
  )
}
