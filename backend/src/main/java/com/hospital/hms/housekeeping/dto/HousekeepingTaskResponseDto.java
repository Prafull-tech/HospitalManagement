package com.hospital.hms.housekeeping.dto;

import com.hospital.hms.housekeeping.entity.HousekeepingTaskStatus;
import com.hospital.hms.housekeeping.entity.HousekeepingTaskType;

import java.time.Instant;

/**
 * Response DTO for housekeeping task (GET /api/housekeeping/tasks).
 */
public class HousekeepingTaskResponseDto {

    private Long id;
    private Long bedId;
    private String roomNo;
    private String wardName;
    private HousekeepingTaskType taskType;
    private String assignedStaff;
    private HousekeepingTaskStatus status;
    private Long ipdAdmissionId;
    private Instant createdAt;
    private Instant completedAt;

    public HousekeepingTaskResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public HousekeepingTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(HousekeepingTaskType taskType) {
        this.taskType = taskType;
    }

    public String getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public HousekeepingTaskStatus getStatus() {
        return status;
    }

    public void setStatus(HousekeepingTaskStatus status) {
        this.status = status;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
