import type { ReactNode } from 'react'

export interface PharmacyTableProps {
  headers: ReactNode[]
  empty?: boolean
  emptyColSpan?: number
  emptyMessage?: string
  children: ReactNode
  maxHeight?: number
}

/**
 * Unified table wrapper: table table-bordered table-hover table-sm.
 * Consistent table-responsive and maxHeight.
 */
export function PharmacyTable({
  headers,
  empty = false,
  emptyColSpan,
  emptyMessage = 'No data available.',
  children,
  maxHeight = 420,
}: PharmacyTableProps) {
  const colSpan = emptyColSpan ?? headers.length
  return (
    <div className="table-responsive" style={{ maxHeight, overflowY: 'auto' }}>
      <table className="table table-hover table-sm align-middle">
        <thead>
          <tr>{headers.map((h, i) => (typeof h === 'string' ? <th key={i}>{h}</th> : <th key={i}>{h}</th>))}</tr>
        </thead>
        <tbody>
          {empty && (
            <tr>
              <td colSpan={colSpan} className="small text-muted text-center py-4">
                {emptyMessage}
              </td>
            </tr>
          )}
          {!empty && children}
        </tbody>
      </table>
    </div>
  )
}
