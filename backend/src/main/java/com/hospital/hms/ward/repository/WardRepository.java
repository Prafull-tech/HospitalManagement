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

    List<Ward> findByIsActiveTrueOrderByNameAsc();

    List<Ward> findByIsActiveTrueAndWardTypeOrderByNameAsc(WardType wardType);
}
