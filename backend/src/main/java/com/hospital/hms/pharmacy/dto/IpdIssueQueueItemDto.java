package com.hospital.hms.pharmacy.dto;

import java.util.List;

public class IpdIssueQueueItemDto {

    private Long indentId;
    private Long ipdAdmissionId;
    private String ipdAdmissionNumber;
    private String patientName;
    private String wardName;
    private String bedNumber;
    private String priority; // ICU, EMERGENCY, HIGH, ROUTINE
    private int medicineCount;
    private String orderedAtDisplay;
    private int waitingMinutes;
    private String status; // PENDING, DELAYED
    private List<IpdIssueQueueLineDto> lines;

    public Long getIndentId() {
        return indentId;
    }

    public void setIndentId(Long indentId) {
        this.indentId = indentId;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public String getIpdAdmissionNumber() {
        return ipdAdmissionNumber;
    }

    public void setIpdAdmissionNumber(String ipdAdmissionNumber) {
        this.ipdAdmissionNumber = ipdAdmissionNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getMedicineCount() {
        return medicineCount;
    }

    public void setMedicineCount(int medicineCount) {
        this.medicineCount = medicineCount;
    }

    public String getOrderedAtDisplay() {
        return orderedAtDisplay;
    }

    public void setOrderedAtDisplay(String orderedAtDisplay) {
        this.orderedAtDisplay = orderedAtDisplay;
    }

    public int getWaitingMinutes() {
        return waitingMinutes;
    }

    public void setWaitingMinutes(int waitingMinutes) {
        this.waitingMinutes = waitingMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<IpdIssueQueueLineDto> getLines() {
        return lines;
    }

    public void setLines(List<IpdIssueQueueLineDto> lines) {
        this.lines = lines;
    }
}

