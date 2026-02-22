package com.hospital.hms.ipd.service;

import com.hospital.hms.billing.service.AdmissionChargeService;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.ipd.dto.DischargeStatusDto;
import com.hospital.hms.ipd.dto.DischargeSummaryRequestDto;
import com.hospital.hms.ipd.entity.*;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.ipd.repository.PatientDischargeRepository;
import com.hospital.hms.pharmacy.service.MedicationOrderService;
import com.hospital.hms.lab.service.TestOrderService;
import com.hospital.hms.billing.service.BillingAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * IPD Patient Discharge workflow with real-time clearance tracking.
 * NABH / medico-legal compliant. All clearances must be true before final discharge.
 */
@Service
public class DischargeService {

    private static final Logger log = LoggerFactory.getLogger(DischargeService.class);
    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED, AdmissionStatus.ACTIVE, AdmissionStatus.TRANSFERRED, AdmissionStatus.DISCHARGE_INITIATED);

    private final PatientDischargeRepository dischargeRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final AdmissionChargeService admissionChargeService;
    private final MedicationOrderService medicationOrderService;
    private final TestOrderService testOrderService;
    private final IPDAdmissionService admissionService;
    private final BillingAccountService billingAccountService;

    public DischargeService(PatientDischargeRepository dischargeRepository,
                            IPDAdmissionRepository admissionRepository,
                            BedAllocationRepository bedAllocationRepository,
                            AdmissionChargeService admissionChargeService,
                            MedicationOrderService medicationOrderService,
                            TestOrderService testOrderService,
                            IPDAdmissionService admissionService,
                            BillingAccountService billingAccountService) {
        this.dischargeRepository = dischargeRepository;
        this.admissionRepository = admissionRepository;
        this.bedAllocationRepository = bedAllocationRepository;
        this.admissionChargeService = admissionChargeService;
        this.medicationOrderService = medicationOrderService;
        this.testOrderService = testOrderService;
        this.admissionService = admissionService;
        this.billingAccountService = billingAccountService;
    }

    @Transactional(readOnly = true)
    public DischargeStatusDto getStatus(Long ipdAdmissionId) {
        IPDAdmission admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));

        PatientDischarge discharge = dischargeRepository.findByIpdAdmissionId(ipdAdmissionId).orElse(null);

        DischargeStatusDto dto = new DischargeStatusDto();
        dto.setIpdAdmissionId(admission.getId());
        dto.setAdmissionNumber(admission.getAdmissionNumber());
        dto.setPatientId(admission.getPatient().getId());
        dto.setUhid(admission.getPatient().getUhid());
        dto.setPatientName(admission.getPatient().getFullName());
        dto.setAdmittedDate(admission.getAdmissionDateTime());
        dto.setDischargeDate(admission.getDischargeDateTime());
        dto.setAdmissionStatus(admission.getAdmissionStatus().name());

        bedAllocationRepository.findActiveByAdmissionIdWithBedAndRoom(ipdAdmissionId).ifPresent(alloc -> {
            dto.setBedId(alloc.getBed().getId());
            dto.setBedNumber(alloc.getBed().getBedNumber());
            dto.setWardName(alloc.getBed().getWard() != null ? alloc.getBed().getWard().getName() : null);
        });

        if (discharge != null) {
            dto.setDischargeType(discharge.getDischargeType());
            dto.setDoctorClearance(Boolean.TRUE.equals(discharge.getDoctorClearance()));
            dto.setNursingClearance(Boolean.TRUE.equals(discharge.getNursingClearance()));
            dto.setPharmacyClearance(Boolean.TRUE.equals(discharge.getPharmacyClearance()));
            dto.setLabClearance(Boolean.TRUE.equals(discharge.getLabClearance()));
            dto.setBillingClearance(Boolean.TRUE.equals(discharge.getBillingClearance()));
            dto.setInsuranceClearance(Boolean.TRUE.equals(discharge.getInsuranceClearance()));
            dto.setHousekeepingClearance(Boolean.TRUE.equals(discharge.getHousekeepingClearance()));
            dto.setDiagnosisSummary(discharge.getDiagnosisSummary());
            dto.setTreatmentSummary(discharge.getTreatmentSummary());
            dto.setProcedures(discharge.getProcedures());
            dto.setAdvice(discharge.getAdvice());
            dto.setFollowUp(discharge.getFollowUp());
            dto.setMedicinesOnDischarge(discharge.getMedicinesOnDischarge());
        } else {
            dto.setDischargeType(DischargeType.NORMAL);
        }

        List<com.hospital.hms.ipd.dto.DischargePendingItemDto> pendingPharmacy = medicationOrderService.getPendingByIpdAdmissionId(ipdAdmissionId);
        List<com.hospital.hms.ipd.dto.DischargePendingItemDto> pendingLab = testOrderService.getPendingByIpdAdmissionId(ipdAdmissionId);
        dto.setPendingPharmacy(pendingPharmacy);
        dto.setPendingLab(pendingLab);
        dto.setPendingPharmacyCount(pendingPharmacy.size());
        dto.setPendingLabCount(pendingLab.size());

        // Use PatientBillingAccount as source of truth for billing totals (NABH compliant)
        var account = billingAccountService.getAccountViewByIpdAdmissionId(ipdAdmissionId);
        BigDecimal total = account.getTotalAmount();
        BigDecimal pending = account.getPendingAmount();
        boolean billingClear = billingAccountService.canDischarge(ipdAdmissionId);
        dto.setBillingTotal(total);
        dto.setBillingPendingAmount(pending);
        dto.setBillingPaid(billingClear);

        boolean insuranceApplicable = admission.getInsuranceTpa() != null && !admission.getInsuranceTpa().isBlank();
        if (!insuranceApplicable && discharge == null) {
            dto.setInsuranceClearance(true);
        }
        boolean allClear = dto.isDoctorClearance() && dto.isNursingClearance() && dto.isPharmacyClearance()
                && dto.isLabClearance() && dto.isBillingClearance() && dto.isInsuranceClearance() && dto.isHousekeepingClearance();
        dto.setAllClearancesComplete(allClear);
        dto.setCanFinalizeDischarge(allClear && billingClear && ACTIVE_STATUSES.contains(admission.getAdmissionStatus()));

        return dto;
    }

    private PatientDischarge getOrCreateDischarge(Long ipdAdmissionId) {
        return dischargeRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseGet(() -> createDischargeRecord(ipdAdmissionId));
    }

    private PatientDischarge createDischargeRecord(Long ipdAdmissionId) {
        IPDAdmission admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));

        PatientDischarge d = new PatientDischarge();
        d.setPatientId(admission.getPatient().getId());
        d.setUhid(admission.getPatient().getUhid());
        d.setIpdAdmissionId(ipdAdmissionId);
        d.setAdmittedDate(admission.getAdmissionDateTime());
        d.setDischargeType(DischargeType.NORMAL);

        bedAllocationRepository.findActiveByAdmissionIdWithBedAndRoom(ipdAdmissionId).ifPresent(alloc -> {
            d.setBedId(alloc.getBed().getId());
            d.setWardType(alloc.getBed().getWard() != null && alloc.getBed().getWard().getWardType() != null
                    ? alloc.getBed().getWard().getWardType().name() : null);
        });

        return dischargeRepository.save(d);
    }

    @Transactional
    public DischargeStatusDto recordDoctorClearance(Long ipdAdmissionId) {
        validateActiveAdmission(ipdAdmissionId);
        PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
        String user = SecurityContextUserResolver.resolveUserId();
        d.setDoctorClearance(true);
        d.setDoctorClearedBy(user);
        d.setDoctorClearedAt(Instant.now());
        dischargeRepository.save(d);
        log.info("Doctor clearance recorded for IPD {} by {}", ipdAdmissionId, user);
        return getStatus(ipdAdmissionId);
    }

    @Transactional
    public DischargeStatusDto recordNursingClearance(Long ipdAdmissionId) {
        validateActiveAdmission(ipdAdmissionId);
        PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
        String user = SecurityContextUserResolver.resolveUserId();
        d.setNursingClearance(true);
        d.setNursingClearedBy(user);
        d.setNursingClearedAt(Instant.now());
        dischargeRepository.save(d);
        log.info("Nursing clearance recorded for IPD {} by {}", ipdAdmissionId, user);
        return getStatus(ipdAdmissionId);
    }

    @Transactional
    public DischargeStatusDto recordPharmacyClearance(Long ipdAdmissionId) {
        validateActiveAdmission(ipdAdmissionId);
        List<com.hospital.hms.ipd.dto.DischargePendingItemDto> pending = medicationOrderService.getPendingByIpdAdmissionId(ipdAdmissionId);
        if (!pending.isEmpty()) {
            throw new IllegalArgumentException("Cannot clear pharmacy: " + pending.size() + " pending medication order(s).");
        }
        PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
        String user = SecurityContextUserResolver.resolveUserId();
        d.setPharmacyClearance(true);
        d.setPharmacyClearedBy(user);
        d.setPharmacyClearedAt(Instant.now());
        dischargeRepository.save(d);
        log.info("Pharmacy clearance recorded for IPD {} by {}", ipdAdmissionId, user);
        return getStatus(ipdAdmissionId);
    }

    @Transactional
    public DischargeStatusDto recordLabClearance(Long ipdAdmissionId) {
        validateActiveAdmission(ipdAdmissionId);
        List<com.hospital.hms.ipd.dto.DischargePendingItemDto> pending = testOrderService.getPendingByIpdAdmissionId(ipdAdmissionId);
        if (!pending.isEmpty()) {
            throw new IllegalArgumentException("Cannot clear lab: " + pending.size() + " pending test(s).");
        }
        PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
        String user = SecurityContextUserResolver.resolveUserId();
        d.setLabClearance(true);
        d.setLabClearedBy(user);
        d.setLabClearedAt(Instant.now());
        dischargeRepository.save(d);
        log.info("Lab clearance recorded for IPD {} by {}", ipdAdmissionId, user);
        return getStatus(ipdAdmissionId);
    }

    @Transactional
    public DischargeStatusDto recordBillingClearance(Long ipdAdmissionId) {
        validateActiveAdmission(ipdAdmissionId);
        if (!billingAccountService.canDischarge(ipdAdmissionId)) {
            BigDecimal pending = billingAccountService.getPendingAmount(ipdAdmissionId);
            String user = SecurityContextUserResolver.resolveUserId();
            String correlationId = MDC.get(MdcKeys.CORRELATION_ID) != null ? MDC.get(MdcKeys.CORRELATION_ID) : "DIS-" + UUID.randomUUID();
            log.warn("Billing clearance denied for IPD {} - pending amount {} - userId {} correlationId {}",
                    ipdAdmissionId, pending, user, correlationId);
            throw new IllegalArgumentException("Pending bill ₹" + pending + " must be cleared before billing clearance. Collect payment first.");
        }
        PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
        String user = SecurityContextUserResolver.resolveUserId();
        d.setBillingClearance(true);
        d.setBillingClearedBy(user);
        d.setBillingClearedAt(Instant.now());
        dischargeRepository.save(d);
        log.info("Billing clearance recorded for IPD {} by {}", ipdAdmissionId, user);
        return getStatus(ipdAdmissionId);
    }

    @Transactional
    public DischargeStatusDto recordInsuranceClearance(Long ipdAdmissionId, boolean adminOverride) {
        validateActiveAdmission(ipdAdmissionId);
        IPDAdmission admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        boolean hasInsurance = admission.getInsuranceTpa() != null && !admission.getInsuranceTpa().isBlank();
        if (!hasInsurance) {
            PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
            d.setInsuranceClearance(true);
            dischargeRepository.save(d);
        } else if (adminOverride) {
            PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
            d.setInsuranceClearance(true);
            dischargeRepository.save(d);
            log.info("Insurance clearance (admin override) for IPD {}", ipdAdmissionId);
        } else {
            throw new IllegalArgumentException("Insurance TPA present. Require approval or admin override.");
        }
        return getStatus(ipdAdmissionId);
    }

    @Transactional
    public DischargeStatusDto saveDischargeSummary(Long ipdAdmissionId, DischargeSummaryRequestDto request) {
        validateActiveAdmission(ipdAdmissionId);
        PatientDischarge d = getOrCreateDischarge(ipdAdmissionId);
        if (request.getDiagnosisSummary() != null) d.setDiagnosisSummary(request.getDiagnosisSummary());
        if (request.getTreatmentSummary() != null) d.setTreatmentSummary(request.getTreatmentSummary());
        if (request.getProcedures() != null) d.setProcedures(request.getProcedures());
        if (request.getAdvice() != null) d.setAdvice(request.getAdvice());
        if (request.getFollowUp() != null) d.setFollowUp(request.getFollowUp());
        if (request.getMedicinesOnDischarge() != null) d.setMedicinesOnDischarge(request.getMedicinesOnDischarge());
        dischargeRepository.save(d);
        return getStatus(ipdAdmissionId);
    }

    @Transactional
    public DischargeStatusDto finalizeDischarge(Long ipdAdmissionId, DischargeType dischargeType) {
        IPDAdmission admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));

        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            throw new IllegalArgumentException("Admission is not active or already discharged.");
        }

        PatientDischarge d = dischargeRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseThrow(() -> new IllegalArgumentException("Discharge record not found. Complete clearance workflow first."));

        if (!Boolean.TRUE.equals(d.getDoctorClearance()) || !Boolean.TRUE.equals(d.getNursingClearance())
                || !Boolean.TRUE.equals(d.getPharmacyClearance()) || !Boolean.TRUE.equals(d.getLabClearance())
                || !Boolean.TRUE.equals(d.getBillingClearance()) || !Boolean.TRUE.equals(d.getInsuranceClearance())) {
            throw new IllegalArgumentException("All clearances must be complete before final discharge.");
        }
        if (!billingAccountService.canDischarge(ipdAdmissionId)) {
            BigDecimal pending = billingAccountService.getPendingAmount(ipdAdmissionId);
            String user = SecurityContextUserResolver.resolveUserId();
            String correlationId = MDC.get(MdcKeys.CORRELATION_ID) != null ? MDC.get(MdcKeys.CORRELATION_ID) : "DIS-" + UUID.randomUUID();
            log.warn("Discharge attempt blocked - IPD {} billing pending {} (paid/corporate/EMI required) - userId {} correlationId {}",
                    ipdAdmissionId, pending, user, correlationId);
            throw new IllegalArgumentException("Billing clearance required before discharge. Pending bill ₹" + pending + " must be cleared.");
        }

        String user = SecurityContextUserResolver.resolveUserId();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID) != null ? MDC.get(MdcKeys.CORRELATION_ID) : "DIS-" + UUID.randomUUID();

        d.setDischargeType(dischargeType != null ? dischargeType : DischargeType.NORMAL);
        d.setDischargeDate(LocalDateTime.now());
        d.setDischargedBy(user);
        d.setDischargedAt(Instant.now());
        d.setCorrelationId(correlationId);
        d.setHousekeepingClearance(true);
        dischargeRepository.save(d);

        // IPD discharge is two-step: ACTIVE -> DISCHARGE_INITIATED -> DISCHARGED. Call twice if needed.
        admissionService.discharge(ipdAdmissionId, new com.hospital.hms.ipd.dto.IPDDischargeRequestDto());
        admissionService.discharge(ipdAdmissionId, new com.hospital.hms.ipd.dto.IPDDischargeRequestDto());

        log.info("Discharge finalized for IPD {} by {} type {} correlationId {}", ipdAdmissionId, user, d.getDischargeType(), correlationId);
        return getStatus(ipdAdmissionId);
    }

    private void validateActiveAdmission(Long ipdAdmissionId) {
        IPDAdmission admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            throw new IllegalArgumentException("Admission is not active. Current status: " + admission.getAdmissionStatus());
        }
    }
}
