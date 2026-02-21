import { Component, type ErrorInfo, type ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export class AppErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('AppErrorBoundary caught:', error, errorInfo)
  }

  render() {
    if (this.state.hasError && this.state.error) {
      return (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '100vh',
            padding: '1.5rem',
            background: 'var(--hms-bg, #f5f5f5)',
            color: 'var(--hms-text, #111)',
            textAlign: 'center',
          }}
        >
          <h2 style={{ marginBottom: '0.5rem' }}>Unable to initialize application</h2>
          <p style={{ color: 'var(--hms-text-secondary, #666)', marginBottom: '0.5rem' }}>
            An error occurred while loading the app.
          </p>
          <pre
            style={{
              fontSize: '0.75rem',
              textAlign: 'left',
              maxWidth: '100%',
              overflow: 'auto',
              padding: '1rem',
              background: '#eee',
              borderRadius: '4px',
              marginBottom: '1rem',
            }}
          >
            {this.state.error.message}
          </pre>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => this.setState({ hasError: false, error: null })}
          >
            Reload
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
