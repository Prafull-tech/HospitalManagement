/** Ward type (matches backend enum). */
export type WardType =
  | 'GENERAL'
  | 'PRIVATE'
  | 'ICU'
  | 'CCU'
  | 'NICU'
  | 'HDU'

/** Bed status (matches backend enum). */
export type BedStatus =
  | 'AVAILABLE'
  | 'OCCUPIED'
  | 'RESERVED'
  | 'MAINTENANCE'

export interface WardResponse {
  id: number
  code: string
  name: string
  wardType: WardType
  capacity?: number
  chargeCategory?: string
  isActive: boolean
  createdAt?: string
  updatedAt?: string
}

export interface WardRequest {
  code: string
  name: string
  wardType: WardType
  capacity?: number
  chargeCategory?: string
  isActive?: boolean
}

export interface RoomResponse {
  id: number
  wardId: number
  roomNumber: string
  isActive: boolean
  createdAt?: string
  updatedAt?: string
}

export interface RoomRequest {
  roomNumber: string
  isActive?: boolean
}

export interface BedResponse {
  id: number
  wardId: number
  wardName: string
  wardCode: string
  roomId?: number
  roomNumber?: string
  bedNumber: string
  bedStatus: BedStatus
  isIsolation: boolean
  equipmentReady: boolean
  isActive: boolean
  available?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface BedRequest {
  bedNumber: string
  roomId?: number
  bedStatus?: BedStatus
  isIsolation?: boolean
  equipmentReady?: boolean
  isActive?: boolean
}

export interface BedStatusRequest {
  bedStatus: BedStatus
}
