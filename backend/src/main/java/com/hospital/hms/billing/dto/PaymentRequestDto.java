package com.hospital.hms.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for collecting payment against an IPD billing account.
 * NABH / medico-legal audit-ready.
 */
public class PaymentRequestDto {

    @NotNull(message = "ipdId is required")
    private Long ipdId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "mode is required")
    @Size(max = 30)
    private String mode; // Cash, Card, UPI

    @Size(max = 100)
    private String referenceNo;

    public Long getIpdId() { return ipdId; }
    public void setIpdId(Long ipdId) { this.ipdId = ipdId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
