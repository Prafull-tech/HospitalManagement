package com.hospital.hms.dashboard.dto;

import java.time.LocalDate;

/**
 * Hospital statistics for a date range (for Reception/Dashboard and print).
 */
public class DashboardStatsDto {

    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalPatientsRegistered;
    private long totalOPDVisits;
    private long totalAdmitted;
    private long totalDischarged;
    private long totalCurrentlyAdmitted; // census: active IPD
    private double totalCollection;     // placeholder for billing integration

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public long getTotalPatientsRegistered() {
        return totalPatientsRegistered;
    }

    public void setTotalPatientsRegistered(long totalPatientsRegistered) {
        this.totalPatientsRegistered = totalPatientsRegistered;
    }

    public long getTotalOPDVisits() {
        return totalOPDVisits;
    }

    public void setTotalOPDVisits(long totalOPDVisits) {
        this.totalOPDVisits = totalOPDVisits;
    }

    public long getTotalAdmitted() {
        return totalAdmitted;
    }

    public void setTotalAdmitted(long totalAdmitted) {
        this.totalAdmitted = totalAdmitted;
    }

    public long getTotalDischarged() {
        return totalDischarged;
    }

    public void setTotalDischarged(long totalDischarged) {
        this.totalDischarged = totalDischarged;
    }

    public long getTotalCurrentlyAdmitted() {
        return totalCurrentlyAdmitted;
    }

    public void setTotalCurrentlyAdmitted(long totalCurrentlyAdmitted) {
        this.totalCurrentlyAdmitted = totalCurrentlyAdmitted;
    }

    public double getTotalCollection() {
        return totalCollection;
    }

    public void setTotalCollection(double totalCollection) {
        this.totalCollection = totalCollection;
    }
}
