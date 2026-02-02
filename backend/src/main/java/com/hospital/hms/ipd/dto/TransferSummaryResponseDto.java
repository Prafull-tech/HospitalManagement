package com.hospital.hms.ipd.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for GET /api/ipd/transfers/{ipdAdmissionId}. Aggregates recommendations, consents, bed reservations, executions.
 */
public class TransferSummaryResponseDto {

    private Long ipdAdmissionId;
    private List<TransferRecommendResponseDto> recommendations = new ArrayList<>();
    private List<TransferConsentResponseDto> consents = new ArrayList<>();
    private List<ConfirmBedResponseDto> bedReservations = new ArrayList<>();
    private List<ExecuteTransferResponseDto> executions = new ArrayList<>();

    public TransferSummaryResponseDto() {
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public List<TransferRecommendResponseDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<TransferRecommendResponseDto> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    public List<TransferConsentResponseDto> getConsents() {
        return consents;
    }

    public void setConsents(List<TransferConsentResponseDto> consents) {
        this.consents = consents != null ? consents : new ArrayList<>();
    }

    public List<ConfirmBedResponseDto> getBedReservations() {
        return bedReservations;
    }

    public void setBedReservations(List<ConfirmBedResponseDto> bedReservations) {
        this.bedReservations = bedReservations != null ? bedReservations : new ArrayList<>();
    }

    public List<ExecuteTransferResponseDto> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecuteTransferResponseDto> executions) {
        this.executions = executions != null ? executions : new ArrayList<>();
    }
}
