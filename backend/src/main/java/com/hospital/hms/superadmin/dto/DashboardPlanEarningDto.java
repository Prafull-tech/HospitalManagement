package com.hospital.hms.superadmin.dto;

import java.math.BigDecimal;

public class DashboardPlanEarningDto {
    private Long planId;
    private String planCode;
    private String planName;
    private long activeSubscriptions;
    private long trialSubscriptions;
    private BigDecimal estimatedMonthlyEarnings;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public long getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(long activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public long getTrialSubscriptions() {
        return trialSubscriptions;
    }

    public void setTrialSubscriptions(long trialSubscriptions) {
        this.trialSubscriptions = trialSubscriptions;
    }

    public BigDecimal getEstimatedMonthlyEarnings() {
        return estimatedMonthlyEarnings;
    }

    public void setEstimatedMonthlyEarnings(BigDecimal estimatedMonthlyEarnings) {
        this.estimatedMonthlyEarnings = estimatedMonthlyEarnings;
    }
}