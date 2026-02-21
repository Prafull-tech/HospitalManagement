package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicineEntryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineEntryAuditLogRepository extends JpaRepository<MedicineEntryAuditLog, Long> {
}
