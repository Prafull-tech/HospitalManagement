package com.hospital.hms.housekeeping.dto;

import com.hospital.hms.housekeeping.entity.HousekeepingTaskType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a housekeeping task (POST /api/housekeeping/tasks).
 */
public class HousekeepingTaskRequestDto {

    private Long bedId;

    @Size(max = 30)
    private String roomNo;

    @Size(max = 100)
    private String wardName;

    @NotNull(message = "Task type is required")
    private HousekeepingTaskType taskType;

    @Size(max = 100)
    private String assignedStaff;

    private Long ipdAdmissionId;

    public HousekeepingTaskRequestDto() {
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

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }
}
