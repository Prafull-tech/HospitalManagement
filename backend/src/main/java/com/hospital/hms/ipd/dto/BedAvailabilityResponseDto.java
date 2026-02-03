package com.hospital.hms.ipd.dto;

import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.WardType;

import java.time.Instant;

/**
 * Response DTO for bed availability (ward + bed + status + optional allocation info).
 * Read-only for IPD admission bed selection. Only beds with status VACANT (AVAILABLE) are selectable.
 */
public class BedAvailabilityResponseDto {

    private Long bedId;
    private String bedNumber;
    private Long wardId;
    private String wardName;
    private String wardCode;
    private WardType wardType;
    private Long roomId;
    private String roomNumber;
    private BedStatus bedStatus;
    /** Display label for UI: VACANT, OCCUPIED, RESERVED, CLEANING, etc. (VACANT = AVAILABLE). */
    private String bedStatusDisplay;
    private Boolean available;
    /** True when bed is VACANT (AVAILABLE) and active; only such beds may be selected for IPD admission. */
    private Boolean selectableForAdmission;
    private Instant updatedAt;
    private Long patientId;
    private String patientName;
    private String patientUhid;
    private String admissionNumber;
    private Long admissionId;

    public BedAvailabilityResponseDto() {
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
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

    public BedStatus getBedStatus() {
        return bedStatus;
    }

    public void setBedStatus(BedStatus bedStatus) {
        this.bedStatus = bedStatus;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getBedStatusDisplay() {
        return bedStatusDisplay;
    }

    public void setBedStatusDisplay(String bedStatusDisplay) {
        this.bedStatusDisplay = bedStatusDisplay;
    }

    public Boolean getSelectableForAdmission() {
        return selectableForAdmission;
    }

    public void setSelectableForAdmission(Boolean selectableForAdmission) {
        this.selectableForAdmission = selectableForAdmission;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public void setAdmissionNumber(String admissionNumber) {
        this.admissionNumber = admissionNumber;
    }

    public Long getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(Long admissionId) {
        this.admissionId = admissionId;
    }
}
