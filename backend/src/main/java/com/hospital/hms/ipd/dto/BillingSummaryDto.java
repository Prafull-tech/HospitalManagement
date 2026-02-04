package com.hospital.hms.ipd.dto;

import java.math.BigDecimal;

/**
 * Read-only billing summary for an IPD admission (view).
 */
public class BillingSummaryDto {

    private BigDecimal totalCharges;
    private BigDecimal totalDeposit;
    private Integer chargeCount;
    private String billingStatus;

    public BillingSummaryDto() {
    }

    public BigDecimal getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }

    public BigDecimal getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(BigDecimal totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public Integer getChargeCount() {
        return chargeCount;
    }

    public void setChargeCount(Integer chargeCount) {
        this.chargeCount = chargeCount;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }
}
