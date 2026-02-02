/**
 * Doctor registration service: API integration and form-to-request mapping.
 */

import { doctorsApi, departmentsApi } from '../api/doctors'
import type {
  DoctorRequest,
  DoctorResponse,
  DoctorFormValues,
  DoctorFormErrors,
  DepartmentResponse,
  PageResponse,
  DoctorStatus,
} from '../types/doctor'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MOBILE_REGEX = /^[0-9]{10}$/

/** Validates doctor form; returns keyed errors. Empty object means valid. */
export function validateDoctorForm(values: DoctorFormValues): DoctorFormErrors {
  const err: DoctorFormErrors = {}
  if (!values.firstName?.trim()) err.firstName = 'First name is required.'
  if (!values.lastName?.trim()) err.lastName = 'Last name is required.'
  if (!values.mobile?.trim()) err.mobile = 'Mobile number is required.'
  else if (!MOBILE_REGEX.test(values.mobile.trim().replace(/\s/g, ''))) err.mobile = 'Enter a valid 10-digit mobile number.'
  if (values.email?.trim() && !EMAIL_REGEX.test(values.email.trim())) err.email = 'Enter a valid email address.'
  if (!values.code?.trim()) err.code = 'Doctor code is required.'
  if (!values.departmentId) err.departmentId = 'Please select a department.'
  if (!values.medicalRegistrationNumber?.trim()) err.medicalRegistrationNumber = 'Medical registration number is required.'
  const yoe = values.yearsOfExperience?.trim()
  if (yoe && (Number(yoe) < 0 || Number(yoe) > 60)) err.yearsOfExperience = 'Enter 0–60.'
  return err
}

export const doctorService = {
  /** Fetch all departments for dropdown. */
  getDepartments(): Promise<DepartmentResponse[]> {
    return departmentsApi.list().catch(() => [])
  },

  /** Fetch a single doctor by id. */
  getById(id: number): Promise<DoctorResponse> {
    return doctorsApi.getById(id)
  },

  /** Create doctor (POST). */
  create(data: DoctorRequest): Promise<DoctorResponse> {
    return doctorsApi.create(data)
  },

  /** Update doctor (PUT). */
  update(id: number, data: DoctorRequest): Promise<DoctorResponse> {
    return doctorsApi.update(id, data)
  },

  /**
   * Generate a suggested doctor code for new registrations.
   * Format: DOC-YYYYMMDD-XXX (XXX = random 3 digits).
   */
  generateCode(): string {
    const now = new Date()
    const y = now.getFullYear()
    const m = String(now.getMonth() + 1).padStart(2, '0')
    const d = String(now.getDate()).padStart(2, '0')
    const r = Math.floor(100 + Math.random() * 900)
    return `DOC-${y}${m}${d}-${r}`
  },

  /**
   * Map form values to API request. Combines firstName + lastName, maps mobile to phone,
   * and appends medical reg no and years of experience into qualifications if present.
   */
  formToRequest(values: DoctorFormValues): DoctorRequest {
    const fullName = [values.firstName.trim(), values.lastName.trim()].filter(Boolean).join(' ')
    const qualParts = [values.qualification.trim()]
    if (values.medicalRegistrationNumber.trim()) {
      qualParts.push(`Reg No: ${values.medicalRegistrationNumber.trim()}`)
    }
    if (values.yearsOfExperience.trim()) {
      qualParts.push(`Experience: ${values.yearsOfExperience.trim()} years`)
    }
    const qualifications = qualParts.filter(Boolean).join(' | ') || undefined
    return {
      code: values.code.trim(),
      fullName: fullName || '',
      departmentId: values.departmentId,
      specialization: values.specialization.trim() || undefined,
      doctorType: values.doctorType,
      status: values.status,
      phone: values.mobile.trim() || undefined,
      email: values.email.trim() || undefined,
      qualifications,
      onCall: values.onCall,
    }
  },

  /**
   * Map API response to form values (for edit). Splits fullName into first/last.
   */
  responseToFormValues(d: DoctorResponse): DoctorFormValues {
    const nameParts = (d.fullName || '').trim().split(/\s+/)
    const firstName = nameParts[0] ?? ''
    const lastName = nameParts.slice(1).join(' ') ?? ''
    return {
      firstName,
      lastName,
      gender: '',
      dateOfBirth: '',
      mobile: d.phone ?? '',
      email: d.email ?? '',
      address: '',
      code: d.code,
      departmentId: d.departmentId,
      specialization: d.specialization ?? '',
      doctorType: d.doctorType,
      qualification: d.qualifications ?? '',
      yearsOfExperience: '',
      medicalRegistrationNumber: '',
      status: d.status,
      opdAvailable: true,
      onCall: d.onCall ?? false,
      joiningDate: '',
    }
  },
}

export type { DoctorRequest, DoctorResponse, DepartmentResponse, PageResponse, DoctorStatus }
