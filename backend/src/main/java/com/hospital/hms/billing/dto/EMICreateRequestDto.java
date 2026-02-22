package com.hospital.hms.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request for POST /api/billing/emi/create.
 */
public class EMICreateRequestDto {

    @NotNull
    private Long billingAccountId;

    private Long ipdAdmissionId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal totalAmount;

    @NotNull
    @DecimalMin("0")
    private BigDecimal downPayment;

    @NotNull
    @Min(1)
    private Integer tenureMonths;

    public Long getBillingAccountId() { return billingAccountId; }
    public void setBillingAccountId(Long billingAccountId) { this.billingAccountId = billingAccountId; }
    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDownPayment() { return downPayment; }
    public void setDownPayment(BigDecimal downPayment) { this.downPayment = downPayment; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
}
