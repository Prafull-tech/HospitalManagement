import { apiClient } from './client'
import type {
  WardResponse,
  WardRequest,
  WardType,
  RoomResponse,
  RoomRequest,
  BedResponse,
  BedRequest,
  BedStatusRequest,
} from '../types/ward'

const WARDS = '/wards'
const BEDS = '/beds'

export const wardApi = {
  // Wards
  createWard(data: WardRequest): Promise<WardResponse> {
    return apiClient.post(WARDS, data).then((res) => res.data)
  },

  listWards(activeOnly = true, wardType?: WardType): Promise<WardResponse[]> {
    return apiClient
      .get(WARDS, { params: { activeOnly, wardType } })
      .then((res) => res.data)
  },

  getWardById(id: number): Promise<WardResponse> {
    return apiClient.get(`${WARDS}/${id}`).then((res) => res.data)
  },

  // Rooms
  createRoom(wardId: number, data: RoomRequest): Promise<RoomResponse> {
    return apiClient.post(`${WARDS}/${wardId}/rooms`, data).then((res) => res.data)
  },

  listRoomsByWard(wardId: number): Promise<RoomResponse[]> {
    return apiClient.get(`${WARDS}/${wardId}/rooms`).then((res) => res.data)
  },

  // Beds
  createBed(wardId: number, data: BedRequest): Promise<BedResponse> {
    return apiClient.post(`${WARDS}/${wardId}/beds`, data).then((res) => res.data)
  },

  listBedsByWard(wardId: number): Promise<BedResponse[]> {
    return apiClient.get(`${WARDS}/${wardId}/beds`).then((res) => res.data)
  },

  getBedAvailability(wardId?: number): Promise<BedResponse[]> {
    return apiClient
      .get(`${BEDS}/availability`, { params: wardId != null ? { wardId } : {} })
      .then((res) => res.data)
  },

  updateBedStatus(bedId: number, data: BedStatusRequest): Promise<BedResponse> {
    return apiClient.put(`${BEDS}/${bedId}/status`, data).then((res) => res.data)
  },
}
