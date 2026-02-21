package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicineLookupAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineLookupAuditLogRepository extends JpaRepository<MedicineLookupAuditLog, Long> {
}
