package com.hospital.hms.ipd.dto;

import java.time.Instant;

/**
 * Response DTO for confirm-bed API.
 */
public class ConfirmBedResponseDto {

    private Long id;
    private Long transferRecommendationId;
    private Long newBedId;
    private Instant reservedAt;
    private String reservationStatus;

    public ConfirmBedResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(Instant reservedAt) {
        this.reservedAt = reservedAt;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
