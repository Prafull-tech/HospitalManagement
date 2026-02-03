package com.hospital.hms.ward.repository;

import com.hospital.hms.ward.entity.WardRoomAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Audit repository for Ward & Room changes. DB-agnostic.
 */
public interface WardRoomAuditLogRepository extends JpaRepository<WardRoomAuditLog, Long> {

    List<WardRoomAuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Long entityId);

    List<WardRoomAuditLog> findAllByOrderByPerformedAtDesc();
}

