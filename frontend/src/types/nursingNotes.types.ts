/** Nursing note search result (matches backend NursingNoteSearchResponse). */
export type NoteStatusSearch = 'DRAFT' | 'LOCKED'
export type ShiftTypeSearch = 'MORNING' | 'EVENING' | 'NIGHT'
export type WardTypeSearch =
  | 'GENERAL'
  | 'PRIVATE'
  | 'SEMI_PRIVATE'
  | 'ICU'
  | 'CCU'
  | 'NICU'
  | 'HDU'
  | 'EMERGENCY'

export interface NursingNoteSearchResponse {
  noteId: number
  ipdAdmissionId: number
  patientName: string | null
  uhid: string | null
  wardType: WardTypeSearch | null
  wardName: string | null
  bedNo: string | null
  shift: ShiftTypeSearch
  noteDateTime: string
  lastUpdated: string | null
  status: NoteStatusSearch
  noteType: string
  content: string
  recordedByName: string | null
}

export interface NursingNoteSearchParams {
  q?: string
  wardType?: WardTypeSearch
  bedNo?: string
  shift?: ShiftTypeSearch
  status?: NoteStatusSearch
  fromDate?: string
  toDate?: string
  page?: number
  size?: number
}

export interface NursingNoteSearchPageResponse {
  content: NursingNoteSearchResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
