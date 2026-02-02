package com.hospital.hms.hospital.repository;

import com.hospital.hms.hospital.entity.BedAvailabilityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Read-only audit trail for bed availability. Ordered by changedAt descending (newest first).
 */
public interface BedAvailabilityAuditLogRepository extends JpaRepository<BedAvailabilityAuditLog, Long> {

    List<BedAvailabilityAuditLog> findByBedAvailabilityIdOrderByChangedAtDesc(Long bedAvailabilityId);
}
