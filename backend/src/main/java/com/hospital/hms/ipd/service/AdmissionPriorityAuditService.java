package com.hospital.hms.ipd.service;

import com.hospital.hms.ipd.dto.AdmissionPriorityAuditResponseDto;
import com.hospital.hms.ipd.entity.AdmissionPriorityAuditLog;
import com.hospital.hms.ipd.entity.PriorityCode;
import com.hospital.hms.ipd.repository.AdmissionPriorityAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Audit logging for admission priority decisions. Writes only; read API uses repository directly.
 * Tracks: priority assigned, rule applied, special consideration, override details, approved by, timestamp.
 * DB-agnostic.
 */
@Service
public class AdmissionPriorityAuditService {

    private final AdmissionPriorityAuditLogRepository auditLogRepository;

    public AdmissionPriorityAuditService(AdmissionPriorityAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Log a system-assigned priority (e.g. at admission). Not an override.
     */
    @Transactional
    public void logPriorityAssigned(Long admissionId, PriorityCode priorityAssigned,
                                     String ruleApplied, List<String> specialConsiderationApplied) {
        AdmissionPriorityAuditLog log = new AdmissionPriorityAuditLog();
        log.setAdmissionId(admissionId);
        log.setPriorityAssigned(priorityAssigned);
        log.setRuleApplied(ruleApplied);
        log.setSpecialConsiderationApplied(specialConsiderationApplied != null && !specialConsiderationApplied.isEmpty()
                ? String.join(", ", specialConsiderationApplied)
                : null);
        log.setIsOverride(false);
        log.setOverrideDetails(null);
        log.setApprovedBy(null);
        auditLogRepository.save(log);
    }

    /**
     * Log an override by authority. Override details and approved by are set.
     */
    @Transactional
    public void logPriorityOverride(Long admissionId, PriorityCode newPriority,
                                     String overrideDetails, String approvedBy) {
        AdmissionPriorityAuditLog log = new AdmissionPriorityAuditLog();
        log.setAdmissionId(admissionId);
        log.setPriorityAssigned(newPriority);
        log.setRuleApplied(null);
        log.setSpecialConsiderationApplied(null);
        log.setIsOverride(true);
        log.setOverrideDetails(overrideDetails);
        log.setApprovedBy(approvedBy);
        auditLogRepository.save(log);
    }

    /**
     * Read-only: list audit entries for an admission, newest first.
     */
    @Transactional(readOnly = true)
    public List<AdmissionPriorityAuditResponseDto> getByAdmissionId(Long admissionId) {
        return auditLogRepository.findByAdmissionIdOrderByCreatedAtDesc(admissionId)
                .stream()
                .map(AdmissionPriorityAuditService::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Read-only: paginated audit list. Optional admissionId; optional from/to date range.
     */
    @Transactional(readOnly = true)
    public Page<AdmissionPriorityAuditResponseDto> getPage(Long admissionId, Instant from, Instant to, Pageable pageable) {
        Page<AdmissionPriorityAuditLog> page;
        if (admissionId != null) {
            page = auditLogRepository.findByAdmissionIdOrderByCreatedAtDesc(admissionId, pageable);
        } else if (from != null && to != null) {
            page = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageable);
        } else {
            page = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return page.map(AdmissionPriorityAuditService::toDto);
    }

    private static AdmissionPriorityAuditResponseDto toDto(AdmissionPriorityAuditLog log) {
        AdmissionPriorityAuditResponseDto dto = new AdmissionPriorityAuditResponseDto();
        dto.setId(log.getId());
        dto.setAdmissionId(log.getAdmissionId());
        dto.setPriorityAssigned(log.getPriorityAssigned());
        dto.setRuleApplied(log.getRuleApplied());
        dto.setSpecialConsiderationApplied(log.getSpecialConsiderationApplied());
        dto.setIsOverride(log.getIsOverride());
        dto.setOverrideDetails(log.getOverrideDetails());
        dto.setApprovedBy(log.getApprovedBy());
        dto.setTimestamp(log.getCreatedAt());
        return dto;
    }
}
