import type {
  DoctorFormValues,
  DoctorFormErrors,
  DepartmentResponse,
  DoctorType,
  DoctorStatus,
  Gender,
} from '../../types/doctor'
import styles from './DoctorForm.module.css'

const GENDERS: { value: Gender; label: string }[] = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
]

const DOCTOR_TYPES: { value: DoctorType; label: string }[] = [
  { value: 'CONSULTANT', label: 'Consultant' },
  { value: 'RMO', label: 'RMO' },
  { value: 'RESIDENT', label: 'Resident' },
  { value: 'DUTY_DOCTOR', label: 'Duty Doctor' },
]

const STATUSES: { value: DoctorStatus; label: string }[] = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'ON_LEAVE', label: 'On Leave' },
]

const SPECIALIZATIONS = [
  'Cardiology',
  'Dermatology',
  'Emergency Medicine',
  'General Medicine',
  'Neurology',
  'Orthopedics',
  'Pediatrics',
  'Psychiatry',
  'Radiology',
  'Surgery',
  'Other',
]

export interface DoctorFormProps {
  values: DoctorFormValues
  errors: DoctorFormErrors
  departments: DepartmentResponse[]
  isEdit: boolean
  onChange: (values: DoctorFormValues) => void
}

function getError(errors: DoctorFormErrors, key: string): string | undefined {
  return errors[key]
}

export function DoctorForm({ values, errors, departments, isEdit, onChange }: DoctorFormProps) {
  const update = (field: keyof DoctorFormValues, value: DoctorFormValues[keyof DoctorFormValues]) => {
    onChange({ ...values, [field]: value })
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    const el = e.target as HTMLInputElement
    if (name === 'departmentId') {
      update('departmentId', Number(value) || 0)
      return
    }
    if (name === 'gender') {
      update('gender', value as Gender | '')
      return
    }
    if (name === 'doctorType') {
      update('doctorType', value as DoctorType)
      return
    }
    if (name === 'status') {
      update('status', value as DoctorStatus)
      return
    }
    if (name === 'opdAvailable' || name === 'onCall') {
      update(name, el.checked)
      return
    }
    update(name as keyof DoctorFormValues, value)
  }

  return (
    <div className={styles.form}>
      {/* A. Personal Information */}
      <section className={styles.section} aria-labelledby="section-personal">
        <h3 id="section-personal" className={styles.sectionTitle}>
          Personal Information
        </h3>
        <div className={styles.grid}>
          <label className={styles.label}>
            <span><span>First Name</span> <span className={styles.required}>*</span></span>
            <input
              type="text"
              name="firstName"
              className={styles.input}
              value={values.firstName}
              onChange={handleChange}
              placeholder="First name"
              autoComplete="given-name"
              aria-invalid={Boolean(getError(errors, 'firstName'))}
            />
            {getError(errors, 'firstName') && <span className={styles.fieldError}>{getError(errors, 'firstName')}</span>}
          </label>
          <label className={styles.label}>
            <span><span>Last Name</span> <span className={styles.required}>*</span></span>
            <input
              type="text"
              name="lastName"
              className={styles.input}
              value={values.lastName}
              onChange={handleChange}
              placeholder="Last name"
              autoComplete="family-name"
              aria-invalid={Boolean(getError(errors, 'lastName'))}
            />
            {getError(errors, 'lastName') && <span className={styles.fieldError}>{getError(errors, 'lastName')}</span>}
          </label>
          <label className={styles.label}>
            <span>Gender</span>
            <select name="gender" className={styles.select} value={values.gender} onChange={handleChange}>
              <option value="">Select</option>
              {GENDERS.map((g) => (
                <option key={g.value} value={g.value}>{g.label}</option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            <span>Date of Birth</span>
            <input
              type="date"
              name="dateOfBirth"
              className={styles.input}
              value={values.dateOfBirth}
              onChange={handleChange}
              aria-label="Date of birth"
            />
          </label>
          <label className={styles.label}>
            <span><span>Mobile Number</span> <span className={styles.required}>*</span></span>
            <input
              type="tel"
              name="mobile"
              className={styles.input}
              value={values.mobile}
              onChange={handleChange}
              placeholder="e.g. 9876543210"
              autoComplete="tel"
              aria-invalid={Boolean(getError(errors, 'mobile'))}
            />
            {getError(errors, 'mobile') && <span className={styles.fieldError}>{getError(errors, 'mobile')}</span>}
          </label>
          <label className={styles.label}>
            <span>Email Address</span>
            <input
              type="email"
              name="email"
              className={styles.input}
              value={values.email}
              onChange={handleChange}
              placeholder="email@example.com"
              autoComplete="email"
              aria-invalid={Boolean(getError(errors, 'email'))}
            />
            {getError(errors, 'email') && <span className={styles.fieldError}>{getError(errors, 'email')}</span>}
          </label>
          <label className={[styles.label, styles.gridFull].join(' ')}>
            <span>Address</span>
            <textarea
              name="address"
              className={styles.textarea}
              value={values.address}
              onChange={handleChange}
              placeholder="Full address"
              rows={3}
            />
          </label>
        </div>
      </section>

      {/* B. Professional Details */}
      <section className={styles.section} aria-labelledby="section-professional">
        <h3 id="section-professional" className={styles.sectionTitle}>
          Professional Details
        </h3>
        <div className={styles.grid}>
          <label className={styles.label}>
            <span>Doctor Code</span>
            <input
              type="text"
              name="code"
              className={styles.input}
              value={values.code}
              readOnly
              disabled
              aria-describedby="code-helper"
            />
            <span id="code-helper" className={styles.helper}>{isEdit ? 'Read-only' : 'Auto-generated for new doctors'}</span>
          </label>
          <label className={styles.label}>
            <span><span>Department</span> <span className={styles.required}>*</span></span>
            <select
              name="departmentId"
              className={styles.select}
              value={values.departmentId}
              onChange={handleChange}
              required
              aria-invalid={Boolean(getError(errors, 'departmentId'))}
            >
              <option value={0}>Select department</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name} ({d.code})</option>
              ))}
            </select>
            {getError(errors, 'departmentId') && <span className={styles.fieldError}>{getError(errors, 'departmentId')}</span>}
          </label>
          <label className={styles.label}>
            <span>Specialization</span>
            <select
              name="specialization"
              className={styles.select}
              value={values.specialization}
              onChange={handleChange}
            >
              <option value="">Select</option>
              {SPECIALIZATIONS.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            <span>Doctor Type</span>
            <select name="doctorType" className={styles.select} value={values.doctorType} onChange={handleChange}>
              {DOCTOR_TYPES.map((t) => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            <span>Qualification</span>
            <input
              type="text"
              name="qualification"
              className={styles.input}
              value={values.qualification}
              onChange={handleChange}
              placeholder="e.g. MBBS, MD"
            />
          </label>
          <label className={styles.label}>
            <span>Years of Experience</span>
            <input
              type="number"
              name="yearsOfExperience"
              className={styles.input}
              value={values.yearsOfExperience}
              onChange={handleChange}
              placeholder="0"
              min={0}
              max={60}
              aria-label="Years of experience"
              aria-invalid={Boolean(getError(errors, 'yearsOfExperience'))}
            />
            {getError(errors, 'yearsOfExperience') && <span className={styles.fieldError}>{getError(errors, 'yearsOfExperience')}</span>}
          </label>
          <label className={styles.label}>
            <span><span>Medical Registration Number</span> <span className={styles.required}>*</span></span>
            <input
              type="text"
              name="medicalRegistrationNumber"
              className={styles.input}
              value={values.medicalRegistrationNumber}
              onChange={handleChange}
              placeholder="State/National registration number"
              aria-invalid={Boolean(getError(errors, 'medicalRegistrationNumber'))}
            />
            {getError(errors, 'medicalRegistrationNumber') && (
              <span className={styles.fieldError}>{getError(errors, 'medicalRegistrationNumber')}</span>
            )}
          </label>
        </div>
      </section>

      {/* C. Availability & Status */}
      <section className={styles.section} aria-labelledby="section-availability">
        <h3 id="section-availability" className={styles.sectionTitle}>
          Availability & Status
        </h3>
        <div className={styles.grid}>
          <label className={styles.label}>
            <span>Employment Status</span>
            <select name="status" className={styles.select} value={values.status} onChange={handleChange}>
              {STATUSES.map((s) => (
                <option key={s.value} value={s.value}>{s.label}</option>
              ))}
            </select>
          </label>
          <label className={styles.label}>
            <span>Joining Date</span>
            <input
              type="date"
              name="joiningDate"
              className={styles.input}
              value={values.joiningDate}
              onChange={handleChange}
              aria-label="Joining date"
            />
          </label>
          <div className={[styles.gridFull, styles.toggleRow].join(' ')}>
            <label className={styles.label}>
              <input
                type="checkbox"
                name="opdAvailable"
                checked={values.opdAvailable}
                onChange={handleChange}
              />
              <span>OPD Available</span>
            </label>
            <label className={styles.label}>
              <input
                type="checkbox"
                name="onCall"
                checked={values.onCall}
                onChange={handleChange}
              />
              <span>Emergency On-Call</span>
            </label>
          </div>
        </div>
      </section>
    </div>
  )
}
