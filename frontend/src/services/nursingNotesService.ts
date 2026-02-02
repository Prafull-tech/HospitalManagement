import { apiClient } from '../api/client'
import type {
  NursingNoteSearchParams,
  NursingNoteSearchPageResponse,
  NursingNoteSearchResponse,
} from '../types/nursingNotes.types'

const NOTES = '/nursing/notes'

/** Backend search returns this shape (NursingNoteResponseDto page). */
interface BackendNoteItem {
  id: number
  ipdAdmissionId: number
  patientName?: string | null
  patientUhid?: string | null
  wardType?: string | null
  wardName?: string | null
  bedNumber?: string | null
  shiftType?: string
  recordedAt?: string
  updatedAt?: string
  noteStatus?: string
  noteType?: string
  content?: string
  recordedByName?: string | null
}

function toSearchResponse(item: BackendNoteItem): NursingNoteSearchResponse {
  return {
    noteId: item.id,
    ipdAdmissionId: item.ipdAdmissionId,
    patientName: item.patientName ?? null,
    uhid: item.patientUhid ?? null,
    wardType: (item.wardType as NursingNoteSearchResponse['wardType']) ?? null,
    wardName: item.wardName ?? null,
    bedNo: item.bedNumber ?? null,
    shift: (item.shiftType as NursingNoteSearchResponse['shift']) ?? 'MORNING',
    noteDateTime: item.recordedAt ?? '',
    lastUpdated: item.updatedAt ?? item.recordedAt ?? null,
    status: (item.noteStatus as NursingNoteSearchResponse['status']) ?? 'DRAFT',
    noteType: item.noteType ?? '',
    content: item.content ?? '',
    recordedByName: item.recordedByName ?? null,
  }
}

/**
 * Search nursing notes via GET /api/nursing/notes/search.
 * Params: q (→ patientName & patientUhid), wardType, bedNo (→ bedNumber), shift (→ shiftType),
 * status (→ noteStatus), fromDate (→ recordedDateFrom), toDate (→ recordedDateTo), page, size.
 */
export function searchNotes(params: NursingNoteSearchParams): Promise<NursingNoteSearchPageResponse> {
  const apiParams: Record<string, string | number | undefined> = {
    page: params.page ?? 0,
    size: params.size ?? 20,
  }
  if (params.q?.trim()) {
    apiParams.patientName = params.q.trim()
    apiParams.patientUhid = params.q.trim()
  }
  if (params.wardType) apiParams.wardType = params.wardType
  if (params.bedNo?.trim()) apiParams.bedNumber = params.bedNo.trim()
  if (params.shift) apiParams.shiftType = params.shift
  if (params.status) apiParams.noteStatus = params.status
  if (params.fromDate) apiParams.recordedDateFrom = params.fromDate
  if (params.toDate) apiParams.recordedDateTo = params.toDate

  return apiClient
    .get(`${NOTES}/search`, { params: apiParams })
    .then((res) => {
      const data = res.data as {
        content: BackendNoteItem[]
        totalElements: number
        totalPages: number
        number: number
        size: number
      }
      return {
        content: (data.content ?? []).map(toSearchResponse),
        totalElements: data.totalElements ?? 0,
        totalPages: data.totalPages ?? 0,
        number: data.number ?? 0,
        size: data.size ?? 20,
      }
    })
}

/**
 * Get full note by id (for View Full Note).
 */
export function getNoteById(id: number): Promise<{ content: string; [k: string]: unknown }> {
  return apiClient.get(`${NOTES}/${id}`).then((res) => res.data)
}

/** Print response: full note DTO (matches backend NursingNoteResponseDto). */
export interface NursingNotePrintItem {
  id: number
  content: string
  patientName?: string
  patientUhid?: string
  recordedAt?: string
  shiftType?: string
  noteType?: string
  wardName?: string
  bedNumber?: string
  [k: string]: unknown
}

/**
 * Get notes for print/PDF (by admission or date range + shift).
 * Backend params: ipdAdmissionId, recordedDateFrom, recordedDateTo, shiftType.
 */
export function getNotesPrint(params: {
  ipdAdmissionId?: number
  recordedDateFrom?: string
  recordedDateTo?: string
  shiftType?: string
}): Promise<NursingNotePrintItem[]> {
  return apiClient.get(`${NOTES}/print`, { params }).then((res) => res.data)
}
