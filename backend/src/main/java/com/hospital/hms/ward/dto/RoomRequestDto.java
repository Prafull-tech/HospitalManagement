package com.hospital.hms.ward.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a room.
 */
public class RoomRequestDto {

    @NotBlank(message = "Room number is required")
    @Size(max = 30)
    private String roomNumber;

    private Boolean isActive = true;

    public RoomRequestDto() {
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
