package com.hospital.hms.hospital.repository;

import com.hospital.hms.hospital.entity.BedAvailability;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.ward.entity.WardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for BedAvailability. One record per hospital + ward type. DB-agnostic, no native queries.
 */
public interface BedAvailabilityRepository extends JpaRepository<BedAvailability, Long> {

    /** Fetch all bed availability records for a hospital, ordered by ward type. */
    List<BedAvailability> findByHospitalIdOrderByWardTypeAsc(Long hospitalId);

    Optional<BedAvailability> findByHospitalIdAndWardType(Long hospitalId, WardType wardType);

    /** Check if a record exists for the given hospital and ward type. */
    boolean existsByHospitalAndWardType(Hospital hospital, WardType wardType);

    boolean existsByHospitalIdAndWardType(Long hospitalId, WardType wardType);

    boolean existsByHospitalIdAndWardTypeAndIdNot(Long hospitalId, WardType wardType, Long excludeId);
}
