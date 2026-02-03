/** Ward type (matches backend enum). */
export type WardType =
  | 'GENERAL'
  | 'SEMI_PRIVATE'
  | 'PRIVATE'
  | 'ICU'
  | 'CCU'
  | 'NICU'
  | 'HDU'
  | 'EMERGENCY'

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
  floor?: string
  capacity?: number
  chargeCategory?: string
  remarks?: string
  isActive: boolean
  createdAt?: string
  updatedAt?: string
}

export interface WardRequest {
  code: string
  name: string
  wardType: WardType
  floor?: string
  capacity?: number
  chargeCategory?: string
  remarks?: string
  isActive?: boolean
}

export type RoomType = 'SHARED' | 'PRIVATE'
export type RoomStatus = 'ACTIVE' | 'CLEANING' | 'MAINTENANCE' | 'ISOLATION'

export interface RoomResponse {
  id: number
  wardId: number
  wardName: string
  roomNumber: string
  capacity?: number
  roomType?: RoomType
  status: RoomStatus
  isActive: boolean
  createdAt?: string
  updatedAt?: string
}

export interface RoomRequest {
  roomNumber: string
  capacity?: number
  roomType?: RoomType
  status?: RoomStatus
  isActive?: boolean
}

export interface WardRoomAuditLog {
  id: number
  entityType: 'WARD' | 'ROOM'
  entityId: number
  action: 'CREATE' | 'UPDATE' | 'DISABLE'
  oldValue?: string
  newValue?: string
  performedBy: string
  performedAt: string
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
