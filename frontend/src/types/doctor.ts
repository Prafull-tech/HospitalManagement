export type DoctorType = 'CONSULTANT' | 'RMO' | 'RESIDENT' | 'DUTY_DOCTOR'
export type DoctorStatus = 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE'

export interface DepartmentResponse {
  id: number
  code: string
  name: string
  description?: string
  hodDoctorId?: number
  hodDoctorName?: string
}

export interface DoctorAvailabilityResponse {
  id: number
  dayOfWeek: number
  startTime: string
  endTime: string
  onCall: boolean
}

export interface DoctorResponse {
  id: number
  code: string
  fullName: string
  departmentId: number
  departmentName: string
  departmentCode: string
  specialization?: string
  doctorType: DoctorType
  status: DoctorStatus
  phone?: string
  email?: string
  qualifications?: string
  onCall: boolean
  createdAt: string
  updatedAt: string
  availability?: DoctorAvailabilityResponse[]
}

export interface DoctorRequest {
  code: string
  fullName: string
  departmentId: number
  specialization?: string
  doctorType: DoctorType
  status?: DoctorStatus
  phone?: string
  email?: string
  qualifications?: string
  onCall?: boolean
}

export interface DoctorAvailabilityRequest {
  dayOfWeek: number
  startTime: string
  endTime: string
  onCall?: boolean
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

/** Extended form values for Doctor Registration (UI-only fields mapped to API on submit). */
export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

export interface DoctorFormValues {
  /* Personal */
  firstName: string
  lastName: string
  gender: Gender | ''
  dateOfBirth: string
  mobile: string
  email: string
  address: string
  /* Professional */
  code: string
  departmentId: number
  specialization: string
  doctorType: DoctorType
  qualification: string
  yearsOfExperience: string
  medicalRegistrationNumber: string
  /* Availability & Status */
  status: DoctorStatus
  opdAvailable: boolean
  onCall: boolean
  joiningDate: string
}

export const EMPTY_DOCTOR_FORM: DoctorFormValues = {
  firstName: '',
  lastName: '',
  gender: '',
  dateOfBirth: '',
  mobile: '',
  email: '',
  address: '',
  code: '',
  departmentId: 0,
  specialization: '',
  doctorType: 'CONSULTANT',
  qualification: '',
  yearsOfExperience: '',
  medicalRegistrationNumber: '',
  status: 'ACTIVE',
  opdAvailable: true,
  onCall: false,
  joiningDate: '',
}

/** Validation errors keyed by field name. */
export interface DoctorFormErrors {
  [key: string]: string
}
