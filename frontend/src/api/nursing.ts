import { apiClient } from './client'
import type {
  NursingStaffResponse,
  NursingStaffRequest,
  NurseAssignmentResponse,
  NurseAssignmentRequest,
  VitalSignResponse,
  VitalSignRequest,
  NursingNoteResponse,
  NursingNoteRequest,
  NursingNoteSearchParams,
  NursingNotePageResponse,
  MedicationAdministrationResponse,
  MedicationAdministrationRequest,
  NurseRole,
} from '../types/nursing'

const STAFF = '/nursing/staff'
const ASSIGNMENTS = '/nursing/assignments'
const VITALS = '/nursing/vitals'
const NOTES = '/nursing/notes'
const MEDICATIONS = '/nursing/medications'

export const nursingApi = {
  createStaff(data: NursingStaffRequest): Promise<NursingStaffResponse> {
    return apiClient.post(STAFF, data).then((res) => res.data)
  },

  listStaff(activeOnly?: boolean, nurseRole?: NurseRole): Promise<NursingStaffResponse[]> {
    return apiClient
      .get(STAFF, { params: { activeOnly: activeOnly ?? true, nurseRole } })
      .then((res) => res.data)
  },

  getStaffById(id: number): Promise<NursingStaffResponse> {
    return apiClient.get(`${STAFF}/${id}`).then((res) => res.data)
  },

  createAssignment(data: NurseAssignmentRequest): Promise<NurseAssignmentResponse> {
    return apiClient.post(ASSIGNMENTS, data).then((res) => res.data)
  },

  getAssignmentsByAdmission(ipdAdmissionId: number): Promise<NurseAssignmentResponse[]> {
    return apiClient
      .get(`${ASSIGNMENTS}/by-admission/${ipdAdmissionId}`)
      .then((res) => res.data)
  },

  recordVitals(data: VitalSignRequest): Promise<VitalSignResponse> {
    return apiClient.post(VITALS, data).then((res) => res.data)
  },

  getVitalsByAdmission(ipdAdmissionId: number): Promise<VitalSignResponse[]> {
    return apiClient.get(`${VITALS}/${ipdAdmissionId}`).then((res) => res.data)
  },

  createNote(data: NursingNoteRequest): Promise<NursingNoteResponse> {
    return apiClient.post(NOTES, data).then((res) => res.data)
  },

  updateNote(id: number, data: NursingNoteRequest): Promise<NursingNoteResponse> {
    return apiClient.put(`${NOTES}/${id}`, data).then((res) => res.data)
  },

  getNoteById(id: number): Promise<NursingNoteResponse> {
    return apiClient.get(`${NOTES}/${id}`).then((res) => res.data)
  },

  searchNotes(params: NursingNoteSearchParams): Promise<NursingNotePageResponse> {
    return apiClient.get(`${NOTES}/search`, { params }).then((res) => res.data)
  },

  getNotesByAdmission(ipdAdmissionId: number): Promise<NursingNoteResponse[]> {
    return apiClient.get(`${NOTES}/admission/${ipdAdmissionId}`).then((res) => res.data)
  },

  lockNote(id: number, lockedById?: number): Promise<NursingNoteResponse> {
    return apiClient.post(`${NOTES}/${id}/lock`, null, { params: lockedById != null ? { lockedById } : {} }).then((res) => res.data)
  },

  getNotesPrint(params: {
    ipdAdmissionId?: number
    recordedDateFrom?: string
    recordedDateTo?: string
    shiftType?: string
  }): Promise<NursingNoteResponse[]> {
    return apiClient.get(`${NOTES}/print`, { params }).then((res) => res.data)
  },

  recordMedication(data: MedicationAdministrationRequest): Promise<MedicationAdministrationResponse> {
    return apiClient.post(MEDICATIONS, data).then((res) => res.data)
  },

  getMedicationsByAdmission(ipdAdmissionId: number): Promise<MedicationAdministrationResponse[]> {
    return apiClient.get(`${MEDICATIONS}/${ipdAdmissionId}`).then((res) => res.data)
  },
}
