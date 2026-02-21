package com.hospital.hms.lab.dto;

/**
 * Today's summary section: completed tests, pending samples, TAT compliance, emergency handled.
 */
public class LabTodaySummaryDto {

    private String date;
    private long completedTestsToday;
    private long pendingSamplesToday;
    private double tatCompliancePercent;
    private long emergencyTestsHandledToday;

    public LabTodaySummaryDto() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCompletedTestsToday() {
        return completedTestsToday;
    }

    public void setCompletedTestsToday(long completedTestsToday) {
        this.completedTestsToday = completedTestsToday;
    }

    public long getPendingSamplesToday() {
        return pendingSamplesToday;
    }

    public void setPendingSamplesToday(long pendingSamplesToday) {
        this.pendingSamplesToday = pendingSamplesToday;
    }

    public double getTatCompliancePercent() {
        return tatCompliancePercent;
    }

    public void setTatCompliancePercent(double tatCompliancePercent) {
        this.tatCompliancePercent = tatCompliancePercent;
    }

    public long getEmergencyTestsHandledToday() {
        return emergencyTestsHandledToday;
    }

    public void setEmergencyTestsHandledToday(long emergencyTestsHandledToday) {
        this.emergencyTestsHandledToday = emergencyTestsHandledToday;
    }
}
