package com.hospital.hms.billing.dto;

/**
 * Response for POST /api/billing/tpa/preauth.
 */
public class TpaPreauthResponseDto {

    private Long ipdAdmissionId;
    private String approvalNumber;
    private String status; // PENDING, APPROVED, REJECTED
    private String message;

    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }

    public String getApprovalNumber() { return approvalNumber; }
    public void setApprovalNumber(String approvalNumber) { this.approvalNumber = approvalNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
