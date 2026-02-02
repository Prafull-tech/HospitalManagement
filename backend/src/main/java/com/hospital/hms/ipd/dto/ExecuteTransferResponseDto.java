package com.hospital.hms.ipd.dto;

import java.time.Instant;

/**
 * Response DTO for execute transfer API.
 */
public class ExecuteTransferResponseDto {

    private Long id;
    private Long ipdAdmissionId;
    private String fromWardType;
    private String toWardType;
    private String transferType;
    private String transferStatus;
    private Long nurseId;
    private Long attendantId;
    private String equipmentUsed;
    private Instant transferTime;

    public ExecuteTransferResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public String getFromWardType() {
        return fromWardType;
    }

    public void setFromWardType(String fromWardType) {
        this.fromWardType = fromWardType;
    }

    public String getToWardType() {
        return toWardType;
    }

    public void setToWardType(String toWardType) {
        this.toWardType = toWardType;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }

    public Long getNurseId() {
        return nurseId;
    }

    public void setNurseId(Long nurseId) {
        this.nurseId = nurseId;
    }

    public Long getAttendantId() {
        return attendantId;
    }

    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }

    public String getEquipmentUsed() {
        return equipmentUsed;
    }

    public void setEquipmentUsed(String equipmentUsed) {
        this.equipmentUsed = equipmentUsed;
    }

    public Instant getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(Instant transferTime) {
        this.transferTime = transferTime;
    }
}
