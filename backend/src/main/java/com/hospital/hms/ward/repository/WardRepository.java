package com.hospital.hms.ward.repository;

import com.hospital.hms.ward.entity.Ward;
import com.hospital.hms.ward.entity.WardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Ward repository. DB-agnostic.
 */
public interface WardRepository extends JpaRepository<Ward, Long> {

    Optional<Ward> findByCode(String code);

    Optional<Ward> findByIdAndHospitalId(Long id, Long hospitalId);

    boolean existsByIdAndHospitalId(Long id, Long hospitalId);

    Optional<Ward> findByCodeAndHospitalId(String code, Long hospitalId);

    List<Ward> findByIsActiveTrueOrderByNameAsc();

    List<Ward> findByHospitalIdOrderByNameAsc(Long hospitalId);

    List<Ward> findByHospitalIdAndIsActiveTrueOrderByNameAsc(Long hospitalId);

    List<Ward> findByHospitalIdAndIsActiveTrueAndWardTypeOrderByNameAsc(Long hospitalId, WardType wardType);

    List<Ward> findByIsActiveTrueAndWardTypeOrderByNameAsc(WardType wardType);
}
