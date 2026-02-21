package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.PharmacyRack;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.hms.pharmacy.entity.StorageType;

import java.util.List;

public interface PharmacyRackRepository extends JpaRepository<PharmacyRack, Long> {

    boolean existsByRackCodeIgnoreCase(String rackCode);

    List<PharmacyRack> findAllByActiveTrueOrderByRackCodeAsc();

    List<PharmacyRack> findByActiveTrueAndStorageTypeOrderByRackCodeAsc(StorageType storageType);
}
