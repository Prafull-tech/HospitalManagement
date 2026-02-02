package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ward.entity.Bed;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Bed reservation for transfer process. Integrates bed confirmation with transfer.
 * <ul>
 *   <li>Bed must be AVAILABLE before reservation</li>
 *   <li>Reservation required before shifting patient</li>
 *   <li>Prevent double allocation: one active (RESERVED) reservation per bed</li>
 * </ul>
 * DB-agnostic JPA design (H2 & MySQL).
 */
@Entity
@Table(
    name = "transfer_bed_reservation",
    indexes = {
        @Index(name = "idx_transfer_bed_res_rec", columnList = "transfer_recommendation_id"),
        @Index(name = "idx_transfer_bed_res_bed", columnList = "new_bed_id"),
        @Index(name = "idx_transfer_bed_res_status", columnList = "reservation_status"),
        @Index(name = "idx_transfer_bed_res_at", columnList = "reserved_at")
    }
)
public class TransferBedReservation extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_recommendation_id", nullable = false)
    private TransferRecommendation transferRecommendation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_bed_id", nullable = false)
    private Bed newBed;

    @NotNull
    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false, length = 20)
    private ReservationStatus reservationStatus;

    public TransferBedReservation() {
    }

    public TransferRecommendation getTransferRecommendation() {
        return transferRecommendation;
    }

    public void setTransferRecommendation(TransferRecommendation transferRecommendation) {
        this.transferRecommendation = transferRecommendation;
    }

    public Bed getNewBed() {
        return newBed;
    }

    public void setNewBed(Bed newBed) {
        this.newBed = newBed;
    }

    public Instant getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(Instant reservedAt) {
        this.reservedAt = reservedAt;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
