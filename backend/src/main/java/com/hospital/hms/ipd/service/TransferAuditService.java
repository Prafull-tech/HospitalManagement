package com.hospital.hms.ipd.service;

import com.hospital.hms.ipd.dto.TransferAuditResponseDto;
import com.hospital.hms.ipd.entity.*;
import com.hospital.hms.ipd.repository.TransferAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Audit logging for transfers. Tracks who recommended, who approved, consent details,
 * emergency flag, old ward → new ward, timestamp. Read-only API for compliance.
 * DB-agnostic.
 */
@Service
public class TransferAuditService {

    private final TransferAuditLogRepository auditLogRepository;

    public TransferAuditService(TransferAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logRecommended(TransferRecommendation rec) {
        TransferAuditLog log = new TransferAuditLog();
        log.setTransferRecommendationId(rec.getId());
        log.setIpdAdmissionId(rec.getIpdAdmission().getId());
        log.setAction(TransferAuditAction.RECOMMENDED);
        log.setRecommendedByDoctorId(rec.getRecommendedByDoctor().getId());
        log.setEmergencyFlag(rec.getEmergencyFlag());
        log.setFromWardType(rec.getFromWardType().name());
        log.setToWardType(rec.getToWardType().name());
        log.setDetails(rec.getRecommendationNotes());
        auditLogRepository.save(log);
    }

    @Transactional
    public void logConsent(TransferRecommendation rec, TransferConsent consent, String performedBy, String performedByRole) {
        TransferAuditLog log = new TransferAuditLog();
        log.setTransferRecommendationId(rec.getId());
        log.setIpdAdmissionId(rec.getIpdAdmission().getId());
        log.setAction(TransferAuditAction.CONSENT_RECORDED);
        log.setPerformedBy(performedBy);
        log.setPerformedByRole(performedByRole);
        log.setConsentGiven(consent.getConsentGiven());
        log.setConsentByName(consent.getConsentByName());
        log.setConsentMode(consent.getConsentMode() != null ? consent.getConsentMode().name() : null);
        log.setEmergencyFlag(rec.getEmergencyFlag());
        log.setFromWardType(rec.getFromWardType().name());
        log.setToWardType(rec.getToWardType().name());
        log.setDetails("Consent " + (Boolean.TRUE.equals(consent.getConsentGiven()) ? "given" : "recorded"));
        auditLogRepository.save(log);
    }

    @Transactional
    public void logBedConfirmed(TransferRecommendation rec, Long newBedId, String performedBy, String performedByRole) {
        TransferAuditLog log = new TransferAuditLog();
        log.setTransferRecommendationId(rec.getId());
        log.setIpdAdmissionId(rec.getIpdAdmission().getId());
        log.setAction(TransferAuditAction.BED_CONFIRMED);
        log.setPerformedBy(performedBy);
        log.setPerformedByRole(performedByRole);
        log.setEmergencyFlag(rec.getEmergencyFlag());
        log.setFromWardType(rec.getFromWardType().name());
        log.setToWardType(rec.getToWardType().name());
        log.setNewBedId(newBedId);
        log.setDetails("Bed reserved for transfer");
        auditLogRepository.save(log);
    }

    @Transactional
    public void logExecuted(TransferRecommendation rec, String performedBy, String performedByRole) {
        TransferAuditLog log = new TransferAuditLog();
        log.setTransferRecommendationId(rec.getId());
        log.setIpdAdmissionId(rec.getIpdAdmission().getId());
        log.setAction(TransferAuditAction.EXECUTED);
        log.setPerformedBy(performedBy);
        log.setPerformedByRole(performedByRole);
        log.setEmergencyFlag(rec.getEmergencyFlag());
        log.setFromWardType(rec.getFromWardType().name());
        log.setToWardType(rec.getToWardType().name());
        log.setDetails("Transfer executed");
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<TransferAuditResponseDto> getByAdmissionId(Long ipdAdmissionId) {
        return auditLogRepository.findByIpdAdmissionIdOrderByCreatedAtDesc(ipdAdmissionId)
                .stream()
                .map(TransferAuditService::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferAuditResponseDto> getByRecommendationId(Long transferRecommendationId) {
        return auditLogRepository.findByTransferRecommendationIdOrderByCreatedAtDesc(transferRecommendationId)
                .stream()
                .map(TransferAuditService::toDto)
                .collect(Collectors.toList());
    }

    private static TransferAuditResponseDto toDto(TransferAuditLog log) {
        TransferAuditResponseDto dto = new TransferAuditResponseDto();
        dto.setId(log.getId());
        dto.setTransferRecommendationId(log.getTransferRecommendationId());
        dto.setIpdAdmissionId(log.getIpdAdmissionId());
        dto.setAction(log.getAction().name());
        dto.setPerformedBy(log.getPerformedBy());
        dto.setPerformedByRole(log.getPerformedByRole());
        dto.setRecommendedByDoctorId(log.getRecommendedByDoctorId());
        dto.setConsentGiven(log.getConsentGiven());
        dto.setConsentByName(log.getConsentByName());
        dto.setConsentMode(log.getConsentMode());
        dto.setEmergencyFlag(log.getEmergencyFlag());
        dto.setFromWardType(log.getFromWardType());
        dto.setToWardType(log.getToWardType());
        dto.setNewBedId(log.getNewBedId());
        dto.setDetails(log.getDetails());
        dto.setTimestamp(log.getCreatedAt());
        return dto;
    }
}
