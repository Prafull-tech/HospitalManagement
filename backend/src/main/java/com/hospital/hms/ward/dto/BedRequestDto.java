package com.hospital.hms.ward.dto;

import com.hospital.hms.ward.entity.BedStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a bed.
 */
public class BedRequestDto {

    @NotBlank(message = "Bed number is required")
    @Size(max = 30)
    private String bedNumber;

    private Long roomId;

    private BedStatus bedStatus = BedStatus.AVAILABLE;

    private Boolean isIsolation = false;

    private Boolean equipmentReady = true;

    private Boolean isActive = true;

    public BedRequestDto() {
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public BedStatus getBedStatus() {
        return bedStatus;
    }

    public void setBedStatus(BedStatus bedStatus) {
        this.bedStatus = bedStatus;
    }

    public Boolean getIsIsolation() {
        return isIsolation;
    }

    public void setIsIsolation(Boolean isIsolation) {
        this.isIsolation = isIsolation;
    }

    public Boolean getEquipmentReady() {
        return equipmentReady;
    }

    public void setEquipmentReady(Boolean equipmentReady) {
        this.equipmentReady = equipmentReady;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
