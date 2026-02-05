package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicineMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineMasterRepository extends JpaRepository<MedicineMaster, Long> {

    boolean existsByMedicineCodeIgnoreCase(String medicineCode);
}

