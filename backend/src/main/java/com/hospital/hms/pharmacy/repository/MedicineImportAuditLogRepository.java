package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicineImportAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineImportAuditLogRepository extends JpaRepository<MedicineImportAuditLog, Long> {
}
