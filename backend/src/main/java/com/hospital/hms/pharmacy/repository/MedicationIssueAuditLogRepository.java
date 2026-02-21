package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicationIssueAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationIssueAuditLogRepository extends JpaRepository<MedicationIssueAuditLog, Long> {
}
