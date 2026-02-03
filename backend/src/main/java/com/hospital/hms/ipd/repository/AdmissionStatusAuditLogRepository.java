package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.AdmissionStatusAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for admission status audit log. Read for API; writes via service.
 * DB-agnostic (H2 & MySQL).
 */
public interface AdmissionStatusAuditLogRepository extends JpaRepository<AdmissionStatusAuditLog, Long> {

    List<AdmissionStatusAuditLog> findByAdmissionIdOrderByChangedAtDesc(Long admissionId);

    Page<AdmissionStatusAuditLog> findByAdmissionIdOrderByChangedAtDesc(Long admissionId, Pageable pageable);

    Page<AdmissionStatusAuditLog> findByToStatusOrderByChangedAtDesc(AdmissionStatus toStatus, Pageable pageable);

    Page<AdmissionStatusAuditLog> findByChangedAtBetweenOrderByChangedAtDesc(
            Instant from, Instant to, Pageable pageable);
}
