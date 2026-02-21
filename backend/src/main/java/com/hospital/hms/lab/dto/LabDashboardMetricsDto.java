package com.hospital.hms.lab.dto;

/**
 * Dashboard metric counts for lab cards (pending collection, verification, TAT breaches, emergency).
 */
public class LabDashboardMetricsDto {

    private long pendingCollection;
    private long pendingVerification;
    private long tatBreaches;
    private long emergencySamples;

    public LabDashboardMetricsDto() {
    }

    public long getPendingCollection() {
        return pendingCollection;
    }

    public void setPendingCollection(long pendingCollection) {
        this.pendingCollection = pendingCollection;
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
}
