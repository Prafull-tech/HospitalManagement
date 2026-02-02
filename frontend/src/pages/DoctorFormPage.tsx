import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DoctorForm } from '../components/doctors/DoctorForm'
import { doctorService, validateDoctorForm } from '../services/doctorService'
import { EMPTY_DOCTOR_FORM } from '../types/doctor'
import type { DoctorFormValues, DoctorFormErrors, DepartmentResponse } from '../types/doctor'
import type { ApiError } from '../types/patient'
import shared from '../styles/Dashboard.module.css'
import styles from './DoctorFormPage.module.css'

export function DoctorFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)

  const [formValues, setFormValues] = useState<DoctorFormValues>(EMPTY_DOCTOR_FORM)
  const [errors, setErrors] = useState<DoctorFormErrors>({})
  const [departments, setDepartments] = useState<DepartmentResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [loadDoctor, setLoadDoctor] = useState(isEdit)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  useEffect(() => {
    doctorService.getDepartments().then(setDepartments)
  }, [])

  useEffect(() => {
    if (!isEdit) {
      setFormValues((prev) => ({ ...prev, code: doctorService.generateCode() }))
      return
    }
    if (!id) return
    setLoadDoctor(true)
    doctorService
      .getById(Number(id))
      .then((d) => {
        setFormValues(doctorService.responseToFormValues(d))
      })
      .catch(() => setError('Failed to load doctor.'))
      .finally(() => setLoadDoctor(false))
  }, [id, isEdit])

  useEffect(() => {
    if (!successMessage) return
    const t = setTimeout(() => setSuccessMessage(''), 4000)
    return () => clearTimeout(t)
  }, [successMessage])

  const handleSave = async () => {
    setError('')
    setErrors({})
    const err = validateDoctorForm(formValues)
    if (Object.keys(err).length > 0) {
      setErrors(err)
      setError('Please correct the errors below.')
      return
    }
    setLoading(true)
    try {
      const payload = doctorService.formToRequest(formValues)
      if (isEdit && id) {
        await doctorService.update(Number(id), payload)
        setSuccessMessage('Doctor updated successfully.')
      } else {
        await doctorService.create(payload)
        setSuccessMessage('Doctor registered successfully.')
        setFormValues({ ...EMPTY_DOCTOR_FORM, code: doctorService.generateCode() })
      }
    } catch (err: unknown) {
      const ax = err as { response?: { data?: ApiError } }
      setError(ax.response?.data?.message || 'Save failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleReset = () => {
    setError('')
    setErrors({})
    if (isEdit && id) {
      doctorService.getById(Number(id)).then((d) => setFormValues(doctorService.responseToFormValues(d))).catch(() => {})
    } else {
      setFormValues({ ...EMPTY_DOCTOR_FORM, code: doctorService.generateCode() })
    }
  }

  if (loadDoctor) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading…</div>
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <div className={shared.pageHeader}>
        <nav className={styles.breadcrumb} aria-label="Breadcrumb">
          <Link to="/">Home</Link>
          <span className={styles.breadcrumbSep}>→</span>
          <span>Clinical Care</span>
          <span className={styles.breadcrumbSep}>→</span>
          <Link to="/doctors">Doctors</Link>
          <span className={styles.breadcrumbSep}>→</span>
          <span aria-current="page">{isEdit ? 'Edit' : 'Register'}</span>
        </nav>
        <div className={styles.headerRow}>
          <h1 className={shared.pageTitle}>Doctor Registration</h1>
          <div className={styles.headerActions}>
            <button type="button" className={styles.btnPrimary} onClick={handleSave} disabled={loading}>
              {loading ? 'Saving…' : 'Save'}
            </button>
            <button type="button" className={styles.btnSecondary} onClick={handleReset} disabled={loading}>
              Reset
            </button>
            <Link to="/doctors" className={styles.btnTertiary}>
              Back to List
            </Link>
          </div>
        </div>
      </div>

      {error && <div className={styles.error} role="alert">{error}</div>}
      {successMessage && (
        <div className={styles.success} role="status">
          {successMessage}
        </div>
      )}

      <DoctorForm
        values={formValues}
        errors={errors}
        departments={departments}
        isEdit={isEdit}
        onChange={setFormValues}
      />
    </div>
  )
}
