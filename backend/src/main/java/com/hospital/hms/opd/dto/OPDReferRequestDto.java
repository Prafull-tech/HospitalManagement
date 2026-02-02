package com.hospital.hms.opd.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for referring OPD visit.
 */
public class OPDReferRequestDto {

    private Long referredToDepartmentId;
    private Long referredToDoctorId;
    private Boolean referToIpd = false;

    @Size(max = 500)
    private String referralRemarks;

    public OPDReferRequestDto() {
    }

    public Long getReferredToDepartmentId() {
        return referredToDepartmentId;
    }

    public void setReferredToDepartmentId(Long referredToDepartmentId) {
        this.referredToDepartmentId = referredToDepartmentId;
    }

    public Long getReferredToDoctorId() {
        return referredToDoctorId;
    }

    public void setReferredToDoctorId(Long referredToDoctorId) {
        this.referredToDoctorId = referredToDoctorId;
    }

    public Boolean getReferToIpd() {
        return referToIpd;
    }

    public void setReferToIpd(Boolean referToIpd) {
        this.referToIpd = referToIpd;
    }

    public String getReferralRemarks() {
        return referralRemarks;
    }

    public void setReferralRemarks(String referralRemarks) {
        this.referralRemarks = referralRemarks;
    }
}
