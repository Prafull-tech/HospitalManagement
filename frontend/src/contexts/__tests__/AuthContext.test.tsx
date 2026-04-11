import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { AuthProvider, useAuth } from '../AuthContext'
import { BrowserRouter } from 'react-router-dom'

vi.mock('../../api/client', () => ({
  apiClient: {
    post: vi.fn(),
    interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
  },
}))
vi.mock('../../api/authRedirect', () => ({
  setAuthClearCallback: vi.fn(),
  getAuthRedirect: vi.fn(),
  getAuthClearCallback: vi.fn(),
}))

function TestConsumer() {
  const { isAuthenticated, user } = useAuth()
  return (
    <div>
      <span data-testid="auth">{isAuthenticated ? 'yes' : 'no'}</span>
      <span data-testid="user">{user?.username ?? 'none'}</span>
    </div>
  )
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('starts unauthenticated when no stored auth', () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      </BrowserRouter>
    )
    expect(screen.getByTestId('auth').textContent).toBe('no')
    expect(screen.getByTestId('user').textContent).toBe('none')
  })

  it('restores user from localStorage', () => {
    localStorage.setItem(
      'hms_auth',
      JSON.stringify({ username: 'admin', role: 'ADMIN', fullName: 'Admin', token: 'tok' })
    )
    render(
      <BrowserRouter>
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      </BrowserRouter>
    )
    expect(screen.getByTestId('auth').textContent).toBe('yes')
    expect(screen.getByTestId('user').textContent).toBe('admin')
  })
})
