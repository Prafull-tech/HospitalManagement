package com.hospital.hms.pharmacy.dto;

/**
 * Active IPD admission info for pharmacy sell (patient-linked mode).
 */
public class PatientIpdStatusDto {

    private Long ipdAdmissionId;
    private String admissionNumber;
    private String wardName;
    private String bedNumber;
    private boolean ipdLinked;

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public void setAdmissionNumber(String admissionNumber) {
        this.admissionNumber = admissionNumber;
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

    public boolean isIpdLinked() {
        return ipdLinked;
    }

    public void setIpdLinked(boolean ipdLinked) {
        this.ipdLinked = ipdLinked;
    }
}
