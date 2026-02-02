package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.BedAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Bed allocation repository. DB-agnostic.
 */
public interface BedAllocationRepository extends JpaRepository<BedAllocation, Long> {

    @Query("SELECT ba FROM BedAllocation ba WHERE ba.bed.id = :bedId AND ba.releasedAt IS NULL")
    Optional<BedAllocation> findActiveByBedId(@Param("bedId") Long bedId);

    @Query("SELECT ba FROM BedAllocation ba JOIN FETCH ba.admission a JOIN FETCH a.patient WHERE ba.bed.id = :bedId AND ba.releasedAt IS NULL")
    Optional<BedAllocation> findActiveByBedIdWithAdmissionAndPatient(@Param("bedId") Long bedId);

    @Query("SELECT ba FROM BedAllocation ba WHERE ba.admission.id = :admissionId AND ba.releasedAt IS NULL")
    Optional<BedAllocation> findActiveByAdmissionId(@Param("admissionId") Long admissionId);

    @Query("SELECT ba FROM BedAllocation ba WHERE ba.admission.id = :admissionId ORDER BY ba.allocatedAt DESC")
    List<BedAllocation> findByAdmissionIdOrderByAllocatedAtDesc(@Param("admissionId") Long admissionId);
}
