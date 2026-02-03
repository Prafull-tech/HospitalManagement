package com.hospital.hms.ward.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating a room.
 */
public class RoomRequestDto {

    @NotBlank(message = "Room number is required")
    @Size(max = 30)
    private String roomNumber;

    private Integer capacity;

    private com.hospital.hms.ward.entity.RoomType roomType;

    private com.hospital.hms.ward.entity.RoomStatus status;

    private Boolean isActive = true;

    public RoomRequestDto() {
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public com.hospital.hms.ward.entity.RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(com.hospital.hms.ward.entity.RoomType roomType) {
        this.roomType = roomType;
    }

    public com.hospital.hms.ward.entity.RoomStatus getStatus() {
        return status;
    }

    public void setStatus(com.hospital.hms.ward.entity.RoomStatus status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
