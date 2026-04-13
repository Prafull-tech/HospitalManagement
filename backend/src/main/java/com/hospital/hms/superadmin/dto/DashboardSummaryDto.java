package com.hospital.hms.superadmin.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDto {
    private long totalHospitals;
    private long activeHospitals;
    private long totalUsers;
    private long activeSubscriptions;
    private long trialSubscriptions;
    private long expiredSubscriptions;
    private long totalSubscriptions;
    private BigDecimal estimatedMonthlyEarnings;
    private BigDecimal estimatedCurrentMonthEarnings;
    private List<DashboardPlanEarningDto> planBreakdown;

    public long getTotalHospitals() { return totalHospitals; }
    public void setTotalHospitals(long totalHospitals) { this.totalHospitals = totalHospitals; }

    public long getActiveHospitals() { return activeHospitals; }
    public void setActiveHospitals(long activeHospitals) { this.activeHospitals = activeHospitals; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveSubscriptions() { return activeSubscriptions; }
    public void setActiveSubscriptions(long activeSubscriptions) { this.activeSubscriptions = activeSubscriptions; }

    public long getTrialSubscriptions() { return trialSubscriptions; }
    public void setTrialSubscriptions(long trialSubscriptions) { this.trialSubscriptions = trialSubscriptions; }

    public long getExpiredSubscriptions() { return expiredSubscriptions; }
    public void setExpiredSubscriptions(long expiredSubscriptions) { this.expiredSubscriptions = expiredSubscriptions; }

    public long getTotalSubscriptions() { return totalSubscriptions; }
    public void setTotalSubscriptions(long totalSubscriptions) { this.totalSubscriptions = totalSubscriptions; }

    public BigDecimal getEstimatedMonthlyEarnings() { return estimatedMonthlyEarnings; }
    public void setEstimatedMonthlyEarnings(BigDecimal estimatedMonthlyEarnings) { this.estimatedMonthlyEarnings = estimatedMonthlyEarnings; }

    public BigDecimal getEstimatedCurrentMonthEarnings() { return estimatedCurrentMonthEarnings; }
    public void setEstimatedCurrentMonthEarnings(BigDecimal estimatedCurrentMonthEarnings) { this.estimatedCurrentMonthEarnings = estimatedCurrentMonthEarnings; }

    public List<DashboardPlanEarningDto> getPlanBreakdown() { return planBreakdown; }
    public void setPlanBreakdown(List<DashboardPlanEarningDto> planBreakdown) { this.planBreakdown = planBreakdown; }
}
