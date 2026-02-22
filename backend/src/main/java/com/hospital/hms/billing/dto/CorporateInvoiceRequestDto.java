package com.hospital.hms.billing.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request for POST /api/billing/corporate/invoice.
 */
public class CorporateInvoiceRequestDto {

    @NotNull
    private Long ipdAdmissionId;

    @NotNull
    private Long corporateAccountId;

    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public Long getCorporateAccountId() { return corporateAccountId; }
    public void setCorporateAccountId(Long corporateAccountId) { this.corporateAccountId = corporateAccountId; }
}
