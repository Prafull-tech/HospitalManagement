package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicineMasterAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineMasterAuditLogRepository extends JpaRepository<MedicineMasterAuditLog, Long> {
}

