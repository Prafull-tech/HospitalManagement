package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.PharmacyShelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PharmacyShelfRepository extends JpaRepository<PharmacyShelf, Long> {

    List<PharmacyShelf> findByRack_IdOrderByShelfLevelAsc(Long rackId);

    List<PharmacyShelf> findByRack_IdAndActiveTrueOrderByShelfLevelAsc(Long rackId);

    boolean existsByRack_IdAndShelfCodeIgnoreCase(Long rackId, String shelfCode);
}
