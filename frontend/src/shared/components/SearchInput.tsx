import { useState, useEffect, useRef } from 'react'

interface SearchInputProps {
  placeholder?: string
  onSearch: (query: string) => void
  debounceMs?: number
  initialValue?: string
  className?: string
}

export function SearchInput({
  placeholder = 'Search...',
  onSearch,
  debounceMs = 300,
  initialValue = '',
  className = '',
}: SearchInputProps) {
  const [value, setValue] = useState(initialValue)
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    if (timerRef.current) clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => {
      onSearch(value.trim())
    }, debounceMs)
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current)
    }
  }, [value, debounceMs, onSearch])

  return (
    <div className={`input-group ${className}`}>
      <span className="input-group-text">
        <svg width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
          <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85zm-5.242.156a5 5 0 1 1 0-10 5 5 0 0 1 0 10z" />
        </svg>
      </span>
      <input
        type="text"
        className="form-control"
        placeholder={placeholder}
        value={value}
        onChange={(e) => setValue(e.target.value)}
      />
      {value && (
        <button
          className="btn btn-outline-secondary"
          type="button"
          onClick={() => setValue('')}
        >
          Clear
        </button>
      )}
    </div>
  )
}
