package com.hospital.hms.ward.repository;

import com.hospital.hms.ward.entity.Bed;
import com.hospital.hms.ward.entity.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Bed repository. DB-agnostic.
 */
public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByWardIdOrderByBedNumberAsc(Long wardId);

    Optional<Bed> findByIdAndWard_Hospital_Id(Long bedId, Long hospitalId);

    List<Bed> findByWard_IsActiveTrueOrderByWard_NameAscBedNumberAsc();

    /** Load beds with ward and room eagerly to avoid LazyInitializationException when mapping to DTOs. */
    @Query("SELECT b FROM Bed b JOIN FETCH b.ward LEFT JOIN FETCH b.room WHERE b.ward.id = :wardId AND b.ward.hospital.id = :hospitalId ORDER BY b.bedNumber")
    List<Bed> findByWardIdWithWardAndRoom(@Param("wardId") Long wardId, @Param("hospitalId") Long hospitalId);

    /** Load beds with ward and room eagerly for all active wards. */
    @Query("SELECT b FROM Bed b JOIN FETCH b.ward LEFT JOIN FETCH b.room WHERE b.ward.isActive = true AND b.ward.hospital.id = :hospitalId ORDER BY b.ward.name, b.bedNumber")
    List<Bed> findAllWithActiveWardAndRoomOrderByWardNameAndBedNumber(@Param("hospitalId") Long hospitalId);

    List<Bed> findByWardIdAndWard_Hospital_IdAndBedStatusAndIsActiveTrue(Long wardId, Long hospitalId, BedStatus bedStatus);

    List<Bed> findByWardIdAndBedStatusAndIsActiveTrue(Long wardId, BedStatus bedStatus);

    Optional<Bed> findByWardIdAndWard_Hospital_IdAndBedNumber(Long wardId, Long hospitalId, String bedNumber);

    /** Business rule: prevent disabling ward when active beds exist. */
    boolean existsByWardIdAndIsActiveTrue(Long wardId);

    /** Business rule: prevent disabling room while occupied beds exist. */
    boolean existsByRoom_IdAndBedStatusAndIsActiveTrue(Long roomId, BedStatus bedStatus);
}
