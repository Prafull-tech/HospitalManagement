package com.hospital.hms.ward.dto;

import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.WardType;

import java.time.Instant;

/**
 * Response DTO for bed.
 */
public class BedResponseDto {

    private Long id;
    private Long wardId;
    private String wardName;
    private String wardCode;
    private WardType wardType;
    private Long roomId;
    private String roomNumber;
    private String bedNumber;
    private BedStatus bedStatus;
    private Boolean isIsolation;
    private Boolean equipmentReady;
    private Boolean isActive;
    private Boolean available;
    private Instant createdAt;
    private Instant updatedAt;

    public BedResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }

    public WardType getWardType() {
        return wardType;
    }

    public void setWardType(WardType wardType) {
        this.wardType = wardType;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
