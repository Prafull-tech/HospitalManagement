package com.hospital.hms.housekeeping.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Housekeeping task for bed/room cleaning or disinfection.
 * Links to bed, optionally to IPD admission (post-discharge cleanup).
 */
@Entity
@Table(
    name = "housekeeping_tasks",
    indexes = {
        @Index(name = "idx_housekeeping_status", columnList = "status"),
        @Index(name = "idx_housekeeping_bed", columnList = "bed_id"),
        @Index(name = "idx_housekeeping_created", columnList = "created_at")
    }
)
public class HousekeepingTask extends BaseIdEntity {

    @Column(name = "bed_id")
    private Long bedId;

    @Column(name = "room_no", length = 30)
    private String roomNo;

    @Column(name = "ward_name", length = 100)
    private String wardName;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private HousekeepingTaskType taskType;

    @Column(name = "assigned_staff", length = 100)
    private String assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HousekeepingTaskStatus status = HousekeepingTaskStatus.PENDING;

    @Column(name = "ipd_admission_id")
    private Long ipdAdmissionId;

    @Column(name = "completed_at")
    private Instant completedAt;

    public HousekeepingTask() {
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
