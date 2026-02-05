package com.hospital.hms.pharmacy.dto;

public class PharmacySummaryDto {

    private String date;
    private int totalIndentsReceived;
    private int totalIndentsIssued;
    private int pendingIndents;
    private int medicinesIssuedCount;
    private int stockAdjustmentsCount;
    private int overridesCount;
    private int highRiskAlerts;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTotalIndentsReceived() {
        return totalIndentsReceived;
    }

    public void setTotalIndentsReceived(int totalIndentsReceived) {
        this.totalIndentsReceived = totalIndentsReceived;
    }

    public int getTotalIndentsIssued() {
        return totalIndentsIssued;
    }

    public void setTotalIndentsIssued(int totalIndentsIssued) {
        this.totalIndentsIssued = totalIndentsIssued;
    }

    public int getPendingIndents() {
        return pendingIndents;
    }

    public void setPendingIndents(int pendingIndents) {
        this.pendingIndents = pendingIndents;
    }

    public int getMedicinesIssuedCount() {
        return medicinesIssuedCount;
    }

    public void setMedicinesIssuedCount(int medicinesIssuedCount) {
        this.medicinesIssuedCount = medicinesIssuedCount;
    }

    public int getStockAdjustmentsCount() {
        return stockAdjustmentsCount;
    }

    public void setStockAdjustmentsCount(int stockAdjustmentsCount) {
        this.stockAdjustmentsCount = stockAdjustmentsCount;
    }

    public int getOverridesCount() {
        return overridesCount;
    }

    public void setOverridesCount(int overridesCount) {
        this.overridesCount = overridesCount;
    }

    public int getHighRiskAlerts() {
        return highRiskAlerts;
    }

    public void setHighRiskAlerts(int highRiskAlerts) {
        this.highRiskAlerts = highRiskAlerts;
    }
}

