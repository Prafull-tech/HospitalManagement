package com.hospital.hms.ipd.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.config.AdmissionStatusTransitionRules;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.AdmissionStatusAuditLog;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.AdmissionStatusAuditLogRepository;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.ward.service.BedService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * IPD Admission Status Master: controlled status transitions and audit of all status changes.
 */
@Service
public class AdmissionStatusMasterService {

    private final IPDAdmissionRepository admissionRepository;
    private final AdmissionStatusAuditLogRepository auditLogRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final BedService bedService;

    public AdmissionStatusMasterService(IPDAdmissionRepository admissionRepository,
                                       AdmissionStatusAuditLogRepository auditLogRepository,
                                       BedAllocationRepository bedAllocationRepository,
                                       BedService bedService) {
        this.admissionRepository = admissionRepository;
        this.auditLogRepository = auditLogRepository;
        this.bedAllocationRepository = bedAllocationRepository;
        this.bedService = bedService;
    }

    /**
     * Returns all statuses in the master (for dropdown/API).
     */
    public Set<AdmissionStatus> getAllStatuses() {
        return AdmissionStatusTransitionRules.getAllStatuses();
    }

    /**
     * Returns only the 6 primary master statuses: ACTIVE, DISCHARGED, SHIFTED/TRANSFERRED, REFERRED, LAMA, EXPIRED.
     */
    public Set<AdmissionStatus> getMasterStatusesOnly() {
        return AdmissionStatusTransitionRules.getMasterStatusesOnly();
    }

    /**
     * Returns allowed target statuses from the given source status.
     * Use null for "initial" (allowed: ADMITTED only).
     */
    public Set<AdmissionStatus> getAllowedTransitions(AdmissionStatus fromStatus) {
        return AdmissionStatusTransitionRules.getAllowedTargets(fromStatus);
    }

    /**
     * Validates that the transition from -> to is allowed. Throws if not.
     */
    public void validateTransition(AdmissionStatus fromStatus, AdmissionStatus toStatus) {
        if (!AdmissionStatusTransitionRules.isAllowed(fromStatus, toStatus)) {
            String from = fromStatus == null ? "null" : fromStatus.name();
            throw new IllegalArgumentException(
                    "Status transition not allowed: " + from + " → " + toStatus.name() + ". " +
                            "Allowed from " + from + ": " + AdmissionStatusTransitionRules.getAllowedTargets(fromStatus));
        }
    }

    /**
     * Records a status change in the audit log. Call this after every status change (from service or API).
     */
    @Transactional
    public void recordStatusChange(Long admissionId, AdmissionStatus fromStatus, AdmissionStatus toStatus,
                                   String changedBy, String reason) {
        AdmissionStatusAuditLog log = new AdmissionStatusAuditLog();
        log.setAdmissionId(admissionId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setChangedAt(Instant.now());
        log.setChangedBy(changedBy);
        log.setReason(reason != null && reason.length() > 500 ? reason.substring(0, 500) : reason);
        auditLogRepository.save(log);
    }

    /**
     * Changes admission status with validation and audit. Use for direct status-change API (e.g. ACTIVE → REFERRED).
     * For workflow-driven changes (admit, shift-to-ward, transfer, discharge) the respective services update status
     * and call recordStatusChange.
     */
    @Transactional
    public IPDAdmission changeStatus(Long admissionId, AdmissionStatus toStatus, String reason, Authentication authentication) {
        IPDAdmission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + admissionId));
        AdmissionStatus fromStatus = admission.getAdmissionStatus();

        validateTransition(fromStatus, toStatus);

        admission.setAdmissionStatus(toStatus);
        if (toStatus == AdmissionStatus.DISCHARGED) {
            admission.setDischargeDateTime(LocalDateTime.now());
            bedAllocationRepository.findActiveByAdmissionId(admissionId).ifPresent(alloc -> {
                bedService.setBedStatusAvailable(alloc.getBed().getId());
                alloc.setReleasedAt(Instant.now());
                bedAllocationRepository.save(alloc);
            });
        }
        admission = admissionRepository.save(admission);

        String changedBy = authentication != null ? authentication.getName() : null;
        recordStatusChange(admissionId, fromStatus, toStatus, changedBy, reason);
        return admission;
    }

    /**
     * Returns audit log entries for an admission, newest first.
     */
    public List<AdmissionStatusAuditLog> getAuditLogByAdmissionId(Long admissionId) {
        if (!admissionRepository.existsById(admissionId)) {
            throw new ResourceNotFoundException("IPD admission not found: " + admissionId);
        }
        return auditLogRepository.findByAdmissionIdOrderByChangedAtDesc(admissionId);
    }
}
