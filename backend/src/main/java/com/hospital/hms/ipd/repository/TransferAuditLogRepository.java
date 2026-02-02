package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.TransferAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for transfer audit log. Read-only queries for compliance API. DB-agnostic.
 */
public interface TransferAuditLogRepository extends JpaRepository<TransferAuditLog, Long> {

    List<TransferAuditLog> findByIpdAdmissionIdOrderByCreatedAtDesc(Long ipdAdmissionId);

    List<TransferAuditLog> findByTransferRecommendationIdOrderByCreatedAtDesc(Long transferRecommendationId);

    Page<TransferAuditLog> findByIpdAdmissionIdOrderByCreatedAtDesc(Long ipdAdmissionId, Pageable pageable);

    Page<TransferAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to, Pageable pageable);
}
