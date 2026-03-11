package com.hospital.hms.lab.service;

import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.lab.entity.LabAuditEventType;
import com.hospital.hms.lab.entity.LabAuditLog;
import com.hospital.hms.lab.repository.LabAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Centralized lab audit logging (Section 14).
 * Tracks: Sample collected, Result entered, Result verified, Report printed.
 */
@Service
public class LabAuditService {

    private final LabAuditLogRepository repository;

    public LabAuditService(LabAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(LabAuditEventType eventType, Long testOrderId, String performedBy) {
        log(eventType, testOrderId, null, null, performedBy, null);
    }

    @Transactional
    public void log(LabAuditEventType eventType, Long testOrderId, Long labOrderItemId, Long labReportId,
                    String performedBy, String orderNumber) {
        LabAuditLog log = new LabAuditLog();
        log.setEventType(eventType);
        log.setTestOrderId(testOrderId);
        log.setLabOrderItemId(labOrderItemId);
        log.setLabReportId(labReportId);
        log.setOrderNumber(orderNumber);
        log.setPerformedBy(performedBy);
        log.setEventAt(Instant.now());
        log.setCorrelationId(Optional.ofNullable(org.slf4j.MDC.get(MdcKeys.CORRELATION_ID))
                .orElse("LAB-" + UUID.randomUUID()));
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<LabAuditLog> findByTestOrderId(Long testOrderId) {
        return repository.findByTestOrderIdOrderByEventAtDesc(testOrderId);
    }
}
