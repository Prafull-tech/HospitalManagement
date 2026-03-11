package com.hospital.hms.lab.dto;

import java.util.List;

/**
 * Dashboard summary DTO for lab operations.
 */
public class LabDashboardSummaryDto {

    private Long pendingCollectionCount;
    private Long pendingProcessingCount;
    private Long pendingVerificationCount;
    private Long completedTodayCount;
    private Long tatBreachCount;
    private Long emergencySamplesCount;
    private List<TestOrderResponseDto> pendingCollection;
    private List<TestOrderResponseDto> pendingProcessing;
    private List<TestOrderResponseDto> pendingVerification;
    private List<TestOrderResponseDto> tatBreaches;
    private List<TestOrderResponseDto> emergencySamples;

    public LabDashboardSummaryDto() {
    }

    // Getters and setters
    public Long getPendingCollectionCount() {
        return pendingCollectionCount;
    }

    public void setPendingCollectionCount(Long pendingCollectionCount) {
        this.pendingCollectionCount = pendingCollectionCount;
    }

    public Long getPendingVerificationCount() {
        return pendingVerificationCount;
    }

    public void setPendingVerificationCount(Long pendingVerificationCount) {
        this.pendingVerificationCount = pendingVerificationCount;
    }

    public Long getPendingProcessingCount() {
        return pendingProcessingCount;
    }

    public void setPendingProcessingCount(Long pendingProcessingCount) {
        this.pendingProcessingCount = pendingProcessingCount;
    }

    public Long getCompletedTodayCount() {
        return completedTodayCount;
    }

    public void setCompletedTodayCount(Long completedTodayCount) {
        this.completedTodayCount = completedTodayCount;
    }

    public Long getTatBreachCount() {
        return tatBreachCount;
    }

    public void setTatBreachCount(Long tatBreachCount) {
        this.tatBreachCount = tatBreachCount;
    }

    public Long getEmergencySamplesCount() {
        return emergencySamplesCount;
    }

    public void setEmergencySamplesCount(Long emergencySamplesCount) {
        this.emergencySamplesCount = emergencySamplesCount;
    }

    public List<TestOrderResponseDto> getPendingCollection() {
        return pendingCollection;
    }

    public void setPendingCollection(List<TestOrderResponseDto> pendingCollection) {
        this.pendingCollection = pendingCollection;
    }

    public List<TestOrderResponseDto> getPendingProcessing() {
        return pendingProcessing;
    }

    public void setPendingProcessing(List<TestOrderResponseDto> pendingProcessing) {
        this.pendingProcessing = pendingProcessing;
    }

    public List<TestOrderResponseDto> getPendingVerification() {
        return pendingVerification;
    }

    public void setPendingVerification(List<TestOrderResponseDto> pendingVerification) {
        this.pendingVerification = pendingVerification;
    }

    public List<TestOrderResponseDto> getTatBreaches() {
        return tatBreaches;
    }

    public void setTatBreaches(List<TestOrderResponseDto> tatBreaches) {
        this.tatBreaches = tatBreaches;
    }

    public List<TestOrderResponseDto> getEmergencySamples() {
        return emergencySamples;
    }

    public void setEmergencySamples(List<TestOrderResponseDto> emergencySamples) {
        this.emergencySamples = emergencySamples;
    }
}
