interface FormFieldProps {
  label: string
  error?: string
  required?: boolean
  htmlFor?: string
  children: React.ReactNode
}

export function FormField({ label, error, required, htmlFor, children }: FormFieldProps) {
  return (
    <div className="mb-3">
      <label htmlFor={htmlFor} className="form-label">
        {label}
        {required && <span className="text-danger ms-1">*</span>}
      </label>
      {children}
      {error && <div className="invalid-feedback d-block">{error}</div>}
    </div>
  )
}
