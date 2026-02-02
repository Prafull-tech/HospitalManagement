package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.AdmissionPriorityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for admission priority audit log. Read-only queries for API; writes via service.
 * DB-agnostic (H2 & MySQL).
 */
public interface AdmissionPriorityAuditLogRepository extends JpaRepository<AdmissionPriorityAuditLog, Long> {

    List<AdmissionPriorityAuditLog> findByAdmissionIdOrderByCreatedAtDesc(Long admissionId);

    Page<AdmissionPriorityAuditLog> findByAdmissionIdOrderByCreatedAtDesc(Long admissionId, Pageable pageable);

    Page<AdmissionPriorityAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AdmissionPriorityAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            Instant from, Instant to, Pageable pageable);
}
