package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.ReservationStatus;
import com.hospital.hms.ipd.entity.TransferBedReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for transfer bed reservations. DB-agnostic (H2 & MySQL).
 */
public interface TransferBedReservationRepository extends JpaRepository<TransferBedReservation, Long> {

    List<TransferBedReservation> findByTransferRecommendationIdOrderByReservedAtDesc(Long transferRecommendationId);

    Optional<TransferBedReservation> findFirstByTransferRecommendationIdAndReservationStatusOrderByReservedAtDesc(
            Long transferRecommendationId, ReservationStatus status);

    @Query("SELECT r FROM TransferBedReservation r WHERE r.newBed.id = :bedId AND r.reservationStatus = :status")
    Optional<TransferBedReservation> findByNewBedIdAndReservationStatus(
            @Param("bedId") Long bedId, @Param("status") ReservationStatus status);
}
