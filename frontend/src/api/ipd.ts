import { apiClient } from './client'
import type {
  IPDAdmissionResponse,
  IPDAdmissionRequest,
  IPDTransferRequest,
  IPDDischargeRequest,
  WardResponse,
  BedAvailabilityResponse,
  IPDPageResponse,
  AdmissionStatus,
  WardType,
} from '../types/ipd'

const ADMISSIONS = '/ipd/admissions'
const ADMIT = '/ipd/admit'
const WARDS = '/ipd/wards'
const BEDS_AVAILABILITY = '/ipd/beds/availability'
const HOSPITAL_BEDS = '/ipd/hospital-beds'

export const ipdApi = {
  /** POST /api/ipd/admit — IPD Admit Patient (mandatory: UHID, doctor, ward type, bed, admission date/time, diagnosis). Bed set to RESERVED on submit. */
  admit(data: IPDAdmissionRequest): Promise<IPDAdmissionResponse> {
    return apiClient.post(ADMIT, data).then((res) => res.data)
  },

  /** Legacy: POST /api/ipd/admissions (same service, accepts extended body). */
  admitLegacy(data: IPDAdmissionRequest): Promise<IPDAdmissionResponse> {
    return apiClient.post(ADMISSIONS, data).then((res) => res.data)
  },

  getById(id: number): Promise<IPDAdmissionResponse> {
    return apiClient.get(`${ADMISSIONS}/${id}`).then((res) => res.data)
  },

  search(params: {
    admissionNumber?: string
    patientUhid?: string
    patientName?: string
    status?: AdmissionStatus
    fromDate?: string
    toDate?: string
    page?: number
    size?: number
  }): Promise<IPDPageResponse<IPDAdmissionResponse>> {
    return apiClient.get(ADMISSIONS, { params }).then((res) => res.data)
  },

  /** GET /api/ipd/admissions/search — search by admission #, UHID, patient name, status. Paginated. */
  searchAdmissions(params: {
    admissionNumber?: string
    patientUhid?: string
    patientName?: string
    status?: AdmissionStatus
    fromDate?: string
    toDate?: string
    page?: number
    size?: number
  }): Promise<IPDPageResponse<IPDAdmissionResponse>> {
    return apiClient.get(`${ADMISSIONS}/search`, { params }).then((res) => res.data)
  },

  transfer(id: number, data: IPDTransferRequest): Promise<IPDAdmissionResponse> {
    return apiClient.post(`${ADMISSIONS}/${id}/transfer`, data).then((res) => res.data)
  },

  discharge(id: number, data?: IPDDischargeRequest): Promise<IPDAdmissionResponse> {
    return apiClient.post(`${ADMISSIONS}/${id}/discharge`, data ?? {}).then((res) => res.data)
  },

  listWards(wardType?: WardType): Promise<WardResponse[]> {
    return apiClient.get(WARDS, { params: wardType != null ? { wardType } : {} }).then((res) => res.data)
  },

  getBedAvailability(params?: { wardType?: WardType; vacantOnly?: boolean }): Promise<BedAvailabilityResponse[]> {
    return apiClient.get(BEDS_AVAILABILITY, { params: params ?? {} }).then((res) => res.data)
  },

  /** GET /api/ipd/hospital-beds — bed availability by ward type; vacantOnly returns only VACANT (selectable) beds. */
  getHospitalBeds(params?: { hospitalId?: number; wardType?: WardType; vacantOnly?: boolean }): Promise<BedAvailabilityResponse[]> {
    return apiClient.get(HOSPITAL_BEDS, { params: params ?? {} }).then((res) => res.data)
  },
}
