package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.LabAuditEventType;
import com.hospital.hms.lab.entity.LabAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LabAuditLogRepository extends JpaRepository<LabAuditLog, Long> {

    List<LabAuditLog> findByTestOrderIdOrderByEventAtDesc(Long testOrderId);

    List<LabAuditLog> findByEventTypeOrderByEventAtDesc(LabAuditEventType eventType, Pageable pageable);

    @Query("SELECT l FROM LabAuditLog l WHERE l.eventAt BETWEEN :from AND :to ORDER BY l.eventAt DESC")
    List<LabAuditLog> findByEventAtBetweenOrderByEventAtDesc(
            @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    @Query("SELECT l FROM LabAuditLog l WHERE (:testOrderId IS NULL OR l.testOrderId = :testOrderId) " +
            "AND (:eventType IS NULL OR l.eventType = :eventType) " +
            "AND l.eventAt BETWEEN :from AND :to ORDER BY l.eventAt DESC")
    Page<LabAuditLog> search(
            @Param("testOrderId") Long testOrderId,
            @Param("eventType") LabAuditEventType eventType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
