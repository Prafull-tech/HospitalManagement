import { useState, useMemo } from 'react'

interface UsePaginationOptions {
  totalItems: number
  pageSize?: number
  initialPage?: number
}

export function usePagination({ totalItems, pageSize = 10, initialPage = 0 }: UsePaginationOptions) {
  const [page, setPage] = useState(initialPage)

  const totalPages = useMemo(() => Math.max(1, Math.ceil(totalItems / pageSize)), [totalItems, pageSize])

  const safeSetPage = (p: number) => {
    setPage(Math.max(0, Math.min(p, totalPages - 1)))
  }

  return {
    page,
    pageSize,
    totalPages,
    setPage: safeSetPage,
    nextPage: () => safeSetPage(page + 1),
    prevPage: () => safeSetPage(page - 1),
    isFirstPage: page === 0,
    isLastPage: page === totalPages - 1,
    startIndex: page * pageSize,
    endIndex: Math.min((page + 1) * pageSize, totalItems),
  }
}
