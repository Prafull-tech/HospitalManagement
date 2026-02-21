package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicineMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineMasterRepository extends JpaRepository<MedicineMaster, Long> {

    boolean existsByMedicineCodeIgnoreCase(String medicineCode);

    @Query("SELECT m FROM MedicineMaster m WHERE m.active = true AND " +
           "(LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(m.medicineCode) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY m.medicineName")
    List<MedicineMaster> searchActiveByNameOrCode(@Param("q") String q);

    java.util.Optional<MedicineMaster> findByMedicineCodeIgnoreCase(String medicineCode);

    @Query("SELECT m FROM MedicineMaster m WHERE m.rack.id = :rackId AND m.active = true ORDER BY m.medicineName")
    List<MedicineMaster> findActiveByRackId(Long rackId);

    @Query("SELECT COUNT(m) FROM MedicineMaster m WHERE m.rack.id = :rackId AND m.active = true")
    long countActiveByRackId(Long rackId);

    @Query("SELECT COUNT(m) FROM MedicineMaster m WHERE m.rack.id = :rackId AND m.active = true AND m.lasaFlag = true")
    long countLasaByRackId(Long rackId);

    java.util.Optional<MedicineMaster> findByBarcodeIgnoreCase(String barcode);

    boolean existsByBarcodeIgnoreCase(String barcode);
}

