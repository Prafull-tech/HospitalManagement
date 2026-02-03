package com.hospital.hms.ward.dto;

import java.time.Instant;

/**
 * Response DTO for room.
 */
public class RoomResponseDto {

    private Long id;
    private Long wardId;
    private String wardName;
    private String roomNumber;
    private Integer capacity;
    private com.hospital.hms.ward.entity.RoomType roomType;
    private com.hospital.hms.ward.entity.RoomStatus status;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public RoomResponseDto() {
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
