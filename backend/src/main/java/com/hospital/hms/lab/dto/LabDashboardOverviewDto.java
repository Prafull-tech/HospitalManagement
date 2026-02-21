package com.hospital.hms.lab.dto;

/**
 * Overview tab: quick snapshot of lab activity for the day.
 */
public class LabDashboardOverviewDto {

    private long totalOrderedToday;
    private long testsCollectedToday;
    private long testsCompletedToday;
    private long testsVerifiedToday;
    private double tatCompliancePercent;

    public LabDashboardOverviewDto() {
    }

    public long getTotalOrderedToday() {
        return totalOrderedToday;
    }

    public void setTotalOrderedToday(long totalOrderedToday) {
        this.totalOrderedToday = totalOrderedToday;
    }

    public long getTestsCollectedToday() {
        return testsCollectedToday;
    }

    public void setTestsCollectedToday(long testsCollectedToday) {
        this.testsCollectedToday = testsCollectedToday;
    }

    public long getTestsCompletedToday() {
        return testsCompletedToday;
    }

    public void setTestsCompletedToday(long testsCompletedToday) {
        this.testsCompletedToday = testsCompletedToday;
    }

    public long getTestsVerifiedToday() {
        return testsVerifiedToday;
    }

    public void setTestsVerifiedToday(long testsVerifiedToday) {
        this.testsVerifiedToday = testsVerifiedToday;
    }

    public double getTatCompliancePercent() {
        return tatCompliancePercent;
    }

    public void setTatCompliancePercent(double tatCompliancePercent) {
        this.tatCompliancePercent = tatCompliancePercent;
    }
}
