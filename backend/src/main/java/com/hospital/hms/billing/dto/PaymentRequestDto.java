package com.hospital.hms.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Collect payment against a billing account. Provide exactly one of:
 * {@code ipdId}, {@code opdVisitId}, or {@code billingAccountId}.
 */
public class PaymentRequestDto {

    private Long ipdId;
    private Long opdVisitId;
    private Long billingAccountId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "mode is required")
    @Size(max = 30)
    private String mode;

    @Size(max = 100)
    private String referenceNo;

    public Long getIpdId() {
        return ipdId;
    }

    public void setIpdId(Long ipdId) {
        this.ipdId = ipdId;
    }

    public Long getOpdVisitId() {
        return opdVisitId;
    }

    public void setOpdVisitId(Long opdVisitId) {
        this.opdVisitId = opdVisitId;
    }

    public Long getBillingAccountId() {
        return billingAccountId;
    }

    public void setBillingAccountId(Long billingAccountId) {
        this.billingAccountId = billingAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
}
