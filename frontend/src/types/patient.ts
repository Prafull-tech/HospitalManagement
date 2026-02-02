export interface PatientRequest {
  fullName: string
  idProofType?: string
  idProofNumber?: string
  dateOfBirth?: string
  age: number
  ageYears?: number
  ageMonths?: number
  ageDays?: number
  gender: string
  weightKg?: number
  heightCm?: number
  phone?: string
  address?: string
  state?: string
  city?: string
  district?: string
  fatherHusbandName?: string
  referredBy?: string
  referredName?: string
  referredPhone?: string
  consultantName?: string
  specialization?: string
  organisationType?: string
  organisationName?: string
  remarks?: string
}

export interface PatientResponse {
  id: number
  uhid: string
  registrationNumber: string
  registrationDate: string
  fullName: string
  idProofType?: string
  idProofNumber?: string
  dateOfBirth?: string
  age: number
  ageYears?: number
  ageMonths?: number
  ageDays?: number
  gender: string
  weightKg?: number
  heightCm?: number
  phone?: string
  address?: string
  state?: string
  city?: string
  district?: string
  fatherHusbandName?: string
  referredBy?: string
  referredName?: string
  referredPhone?: string
  consultantName?: string
  specialization?: string
  organisationType?: string
  organisationName?: string
  remarks?: string
  createdAt: string
  updatedAt: string
}

export interface ApiError {
  status: number
  message: string
  timestamp?: string
  errors?: Record<string, string>
}
