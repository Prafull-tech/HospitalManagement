package com.hospital.hms.ward.repository;

import com.hospital.hms.ward.entity.WardTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Ward Type Master repository. DB-agnostic.
 */
public interface WardTypeMasterRepository extends JpaRepository<WardTypeMaster, Long> {

    List<WardTypeMaster> findByIsActiveTrueOrderByNameAsc();

    List<WardTypeMaster> findAllByOrderByNameAsc();

    Optional<WardTypeMaster> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long excludeId);
}
