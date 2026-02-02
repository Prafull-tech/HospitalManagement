package com.hospital.hms.ipd.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for POST /api/ipd/transfers/confirm-bed.
 */
public class ConfirmBedRequestDto {

    @NotNull(message = "Transfer recommendation ID is required")
    private Long transferRecommendationId;

    @NotNull(message = "New bed ID is required")
    private Long newBedId;

    public ConfirmBedRequestDto() {
    }

    public Long getTransferRecommendationId() {
        return transferRecommendationId;
    }

    public void setTransferRecommendationId(Long transferRecommendationId) {
        this.transferRecommendationId = transferRecommendationId;
    }

    public Long getNewBedId() {
        return newBedId;
    }

    public void setNewBedId(Long newBedId) {
        this.newBedId = newBedId;
    }
}
