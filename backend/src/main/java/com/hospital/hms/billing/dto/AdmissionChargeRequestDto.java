package com.hospital.hms.billing.dto;

import com.hospital.hms.billing.entity.ChargeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request to add a charge to an IPD admission. Used by Pharmacy, Lab, Doctor Orders, etc.
 */
public class AdmissionChargeRequestDto {

    @NotNull(message = "Charge type is required")
    private ChargeType chargeType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal amount;

    @Size(max = 500)
    private String description;

    @Size(max = 50)
    private String referenceType;

    private Long referenceId;

    public AdmissionChargeRequestDto() {
    }

    public ChargeType getChargeType() {
        return chargeType;
    }

    public void setChargeType(ChargeType chargeType) {
        this.chargeType = chargeType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}
