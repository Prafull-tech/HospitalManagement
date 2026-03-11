package com.hospital.hms.lab.dto;

/**
 * Unified lab dashboard API response.
 * GET /api/lab/dashboard
 */
public class LabDashboardResponseDto {

    private long pendingCollection;
    private long pendingProcessing;
    private long pendingVerification;
    private long tatBreaches;
    private long emergencySamples;
    private long todayOrdered;
    private long todayCollected;
    private long todayCompleted;
    private long todayVerified;
    private double tatCompliancePercent;

    public LabDashboardResponseDto() {
    }

    public long getPendingCollection() {
        return pendingCollection;
    }

    public void setPendingCollection(long pendingCollection) {
        this.pendingCollection = pendingCollection;
    }

    public long getPendingProcessing() {
        return pendingProcessing;
    }

    public void setPendingProcessing(long pendingProcessing) {
        this.pendingProcessing = pendingProcessing;
    }

    public long getPendingVerification() {
        return pendingVerification;
    }

    public void setPendingVerification(long pendingVerification) {
        this.pendingVerification = pendingVerification;
    }

    public long getTatBreaches() {
        return tatBreaches;
    }

    public void setTatBreaches(long tatBreaches) {
        this.tatBreaches = tatBreaches;
    }

    public long getEmergencySamples() {
        return emergencySamples;
    }

    public void setEmergencySamples(long emergencySamples) {
        this.emergencySamples = emergencySamples;
    }

    public long getTodayOrdered() {
        return todayOrdered;
    }

    public void setTodayOrdered(long todayOrdered) {
        this.todayOrdered = todayOrdered;
    }

    public long getTodayCollected() {
        return todayCollected;
    }

    public void setTodayCollected(long todayCollected) {
        this.todayCollected = todayCollected;
    }

    public long getTodayCompleted() {
        return todayCompleted;
    }

    public void setTodayCompleted(long todayCompleted) {
        this.todayCompleted = todayCompleted;
    }

    public long getTodayVerified() {
        return todayVerified;
    }

    public void setTodayVerified(long todayVerified) {
        this.todayVerified = todayVerified;
    }

    public double getTatCompliancePercent() {
        return tatCompliancePercent;
    }

    public void setTatCompliancePercent(double tatCompliancePercent) {
        this.tatCompliancePercent = tatCompliancePercent;
    }
}
