package com.hospital.hms.ipd.service;

import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.ipd.dto.*;
import com.hospital.hms.ipd.entity.*;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.ward.entity.Bed;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.WardType;
import com.hospital.hms.ward.repository.BedRepository;
import com.hospital.hms.ward.service.BedService;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * IPD admission service. Enforces: one active admission per patient; one active allocation per bed. DB-agnostic.
 */
@Service
public class IPDAdmissionService {

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.ACTIVE,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    private final IPDAdmissionRepository admissionRepository;
    private final BedRepository bedRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final IPDAdmissionNumberGenerator admissionNumberGenerator;
    private final BedService bedService;
    private final AdmissionPriorityEvaluationService priorityEvaluationService;
    private final AdmissionPriorityOverrideAuthorityResolver overrideAuthorityResolver;
    private final AdmissionPriorityAuditService priorityAuditService;
    private final AdmissionStatusMasterService admissionStatusMasterService;

    public IPDAdmissionService(IPDAdmissionRepository admissionRepository,
                               BedRepository bedRepository,
                               BedAllocationRepository bedAllocationRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository,
                               IPDAdmissionNumberGenerator admissionNumberGenerator,
                               BedService bedService,
                               AdmissionPriorityEvaluationService priorityEvaluationService,
                               AdmissionPriorityOverrideAuthorityResolver overrideAuthorityResolver,
                               AdmissionPriorityAuditService priorityAuditService,
                               AdmissionStatusMasterService admissionStatusMasterService) {
        this.admissionRepository = admissionRepository;
        this.bedRepository = bedRepository;
        this.bedAllocationRepository = bedAllocationRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.admissionNumberGenerator = admissionNumberGenerator;
        this.bedService = bedService;
        this.priorityEvaluationService = priorityEvaluationService;
        this.overrideAuthorityResolver = overrideAuthorityResolver;
        this.priorityAuditService = priorityAuditService;
        this.admissionStatusMasterService = admissionStatusMasterService;
    }

    @Transactional
    public IPDAdmissionResponseDto admit(IPDAdmissionRequestDto request) {
        Patient patient = patientRepository.findByUhid(request.getPatientUhid().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UHID: " + request.getPatientUhid()));
        Doctor doctor = doctorRepository.findById(request.getPrimaryDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getPrimaryDoctorId()));
        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found: " + request.getBedId()));

        List<IPDAdmission> activeAdmissions = admissionRepository.findByPatientIdAndAdmissionStatusIn(
                patient.getId(), ACTIVE_STATUSES);
        if (!activeAdmissions.isEmpty()) {
            throw new IllegalArgumentException("Patient already has an active IPD admission. Discharge or cancel first.");
        }

        if (bedAllocationRepository.findActiveByBedId(bed.getId()).isPresent()) {
            throw new IllegalArgumentException("Bed is already occupied. Choose another bed.");
        }
        if (bed.getBedStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException("Bed must be VACANT at submit time. Current status: " + bed.getBedStatus());
        }
        if (!Boolean.TRUE.equals(bed.getIsActive())) {
            throw new IllegalArgumentException("Bed is not active.");
        }

        String admissionNumber = admissionNumberGenerator.generate();
        LocalDateTime admissionDateTime = request.getAdmissionDateTime() != null
                ? request.getAdmissionDateTime() : LocalDateTime.now();
        String diagnosis = (request.getDiagnosis() != null && !request.getDiagnosis().isBlank())
                ? request.getDiagnosis().trim() : "To be documented";

        AdmissionPriorityRequest priorityInput = buildPriorityRequest(request, bed);
        AdmissionPriorityResult priorityResult = priorityEvaluationService.evaluateWithReason(priorityInput);

        IPDAdmission adm = new IPDAdmission();
        adm.setAdmissionNumber(admissionNumber);
        adm.setPatient(patient);
        adm.setPrimaryDoctor(doctor);
        adm.setAdmissionType(request.getAdmissionType());
        adm.setAdmissionStatus(AdmissionStatus.ADMITTED);
        adm.setAdmissionDateTime(admissionDateTime);
        adm.setDiagnosis(diagnosis);
        adm.setDepositAmount(request.getDepositAmount());
        adm.setInsuranceTpa(request.getInsuranceTpa() != null ? request.getInsuranceTpa().trim() : null);
        adm.setAdmissionFormDocumentRef(request.getAdmissionFormDocumentRef() != null ? request.getAdmissionFormDocumentRef().trim() : null);
        adm.setConsentFormDocumentRef(request.getConsentFormDocumentRef() != null ? request.getConsentFormDocumentRef().trim() : null);
        adm.setIdProofDocumentRef(request.getIdProofDocumentRef() != null ? request.getIdProofDocumentRef().trim() : null);
        adm.setOpdVisitId(request.getOpdVisitId());
        adm.setRemarks(request.getRemarks());
        adm.setAdmissionPriority(priorityResult.getPriority());
        adm.setPriorityAssignmentReason(priorityResult.getAssignmentReason());
        adm.setPriorityOverridden(false);
        IPDAdmission admission = admissionRepository.save(adm);

        priorityAuditService.logPriorityAssigned(
                admission.getId(),
                priorityResult.getPriority(),
                priorityResult.getRuleApplied() != null ? priorityResult.getRuleApplied().name() : null,
                priorityResult.getSpecialConsiderationApplied());

        BedAllocation allocation = new BedAllocation();
        allocation.setBed(bed);
        allocation.setAdmission(admission);
        allocation.setAllocatedAt(Instant.now());
        bedAllocationRepository.save(allocation);
        bedService.setBedStatusReserved(bed.getId());

        admissionStatusMasterService.recordStatusChange(
                admission.getId(), null, AdmissionStatus.ADMITTED, null, "Admission created");

        return toResponse(admission, true);
    }

    @Transactional(readOnly = true)
    public IPDAdmissionResponseDto getById(Long id) {
        IPDAdmission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + id));
        return toResponse(admission, true);
    }

    /**
     * Nursing staff performs shift-to-ward. Bed status → OCCUPIED, admission status → ACTIVE.
     * Only admission in status ADMITTED can be shifted. Shift timestamp mandatory.
     */
    @Transactional
    public IPDAdmissionResponseDto shiftToWard(Long admissionId, ShiftToWardRequestDto request, Authentication authentication) {
        IPDAdmission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + admissionId));
        if (admission.getAdmissionStatus() != AdmissionStatus.ADMITTED) {
            throw new IllegalArgumentException("Only admission with status ADMITTED can be shifted to ward. Current: " + admission.getAdmissionStatus());
        }
        admission.setShiftedToWardAt(request.getShiftTimestamp());
        admission.setShiftedToWardBy(authentication != null ? authentication.getName() : null);
        admission.setAdmissionStatus(AdmissionStatus.ACTIVE);
        admission = admissionRepository.save(admission);

        bedAllocationRepository.findActiveByAdmissionId(admission.getId()).ifPresent(alloc -> {
            bedService.setBedStatusOccupied(alloc.getBed().getId());
        });

        admissionStatusMasterService.recordStatusChange(
                admission.getId(), AdmissionStatus.ADMITTED, AdmissionStatus.ACTIVE,
                authentication != null ? authentication.getName() : null, "Shifted to ward");

        return toResponse(admission, true);
    }

    @Transactional(readOnly = true)
    public Page<IPDAdmissionResponseDto> search(String admissionNumber, String patientUhid, String patientName,
                                                 AdmissionStatus status, LocalDateTime fromDate, LocalDateTime toDate,
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "admissionDateTime"));
        Page<IPDAdmission> result = admissionRepository.search(
                admissionNumber != null && !admissionNumber.isBlank() ? admissionNumber.trim() : null,
                patientUhid != null && !patientUhid.isBlank() ? patientUhid.trim() : null,
                patientName != null && !patientName.isBlank() ? patientName.trim() : null,
                status,
                fromDate,
                toDate,
                pageable
        );
        return result.map(a -> toResponse(a, true));
    }

    @Transactional
    public IPDAdmissionResponseDto transfer(Long id, IPDTransferRequestDto request) {
        IPDAdmission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + id));
        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            throw new IllegalArgumentException("Admission is not active. Cannot transfer.");
        }

        Bed targetBed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found: " + request.getBedId()));
        if (bedAllocationRepository.findActiveByBedId(targetBed.getId()).isPresent()) {
            throw new IllegalArgumentException("Target bed is already occupied.");
        }
        if (!Boolean.TRUE.equals(targetBed.getIsActive())) {
            throw new IllegalArgumentException("Target bed is not active.");
        }

        // Release current allocation: old bed → VACANT (AVAILABLE)
        bedAllocationRepository.findActiveByAdmissionId(admission.getId()).ifPresent(current -> {
            bedService.setBedStatusAvailable(current.getBed().getId());
            current.setReleasedAt(Instant.now());
            bedAllocationRepository.save(current);
        });

        // New allocation: new bed → OCCUPIED
        BedAllocation newAllocation = new BedAllocation();
        newAllocation.setBed(targetBed);
        newAllocation.setAdmission(admission);
        newAllocation.setAllocatedAt(Instant.now());
        bedAllocationRepository.save(newAllocation);
        bedService.setBedStatusOccupied(targetBed.getId());

        AdmissionStatus fromStatus = admission.getAdmissionStatus();
        // Admission status → SHIFTED (stored as TRANSFERRED)
        admission.setAdmissionStatus(AdmissionStatus.TRANSFERRED);
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            admission.setRemarks(
                    (admission.getRemarks() != null ? admission.getRemarks() + "\n" : "") + "Transfer: " + request.getRemarks());
        }
        admission = admissionRepository.save(admission);

        admissionStatusMasterService.recordStatusChange(
                admission.getId(), fromStatus, AdmissionStatus.TRANSFERRED, null,
                request.getRemarks() != null ? request.getRemarks() : "Transfer executed");

        return toResponse(admission, true);
    }

    /**
     * Initiate or complete discharge. First call → DISCHARGE_INITIATED; second call → DISCHARGED, bed released.
     * Hospital SOP: billing clearance should be verified before allowing final discharge (integrate with billing module when ready).
     */
    @Transactional
    public IPDAdmissionResponseDto discharge(Long id, IPDDischargeRequestDto request) {
        IPDAdmission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + id));
        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            throw new IllegalArgumentException("Admission is not active or already discharged.");
        }

        AdmissionStatus fromStatus = admission.getAdmissionStatus();
        if (admission.getAdmissionStatus() == AdmissionStatus.DISCHARGE_INITIATED) {
            admission.setAdmissionStatus(AdmissionStatus.DISCHARGED);
            admission.setDischargeDateTime(LocalDateTime.now());
            if (request.getDischargeRemarks() != null && !request.getDischargeRemarks().isBlank()) {
                admission.setDischargeRemarks(request.getDischargeRemarks());
            }
            admissionStatusMasterService.recordStatusChange(
                    admission.getId(), AdmissionStatus.DISCHARGE_INITIATED, AdmissionStatus.DISCHARGED, null,
                    request.getDischargeRemarks());
        } else {
            admission.setAdmissionStatus(AdmissionStatus.DISCHARGE_INITIATED);
            if (request.getDischargeRemarks() != null && !request.getDischargeRemarks().isBlank()) {
                admission.setDischargeRemarks(request.getDischargeRemarks());
            }
            admissionStatusMasterService.recordStatusChange(
                    admission.getId(), fromStatus, AdmissionStatus.DISCHARGE_INITIATED, null,
                    request.getDischargeRemarks());
        }
        admission = admissionRepository.save(admission);

        if (admission.getAdmissionStatus() == AdmissionStatus.DISCHARGED) {
            bedAllocationRepository.findActiveByAdmissionId(admission.getId()).ifPresent(alloc -> {
                bedService.setBedStatusAvailable(alloc.getBed().getId());
                alloc.setReleasedAt(Instant.now());
                bedAllocationRepository.save(alloc);
            });
        }

        return toResponse(admission, true);
    }

    /**
     * Override admission priority. Only authority roles (MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, IPD_MANAGER) may call.
     * Override requires a reason (validated by DTO); action is logged for audit on the entity.
     *
     * @throws OperationNotAllowedException if caller does not have an authority role
     * @throws IllegalArgumentException      if reason is missing (defensive after @Valid)
     */
    @Transactional
    public IPDAdmissionResponseDto overridePriority(Long admissionId,
                                                     AdmissionPriorityOverrideRequestDto request,
                                                     Authentication authentication) {
        if (request == null || request.getNewPriority() == null) {
            throw new IllegalArgumentException("Override request and new priority are required.");
        }
        String reason = request.getReason();
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Override reason is required.");
        }
        if (reason.length() < 10 || reason.length() > 500) {
            throw new IllegalArgumentException("Override reason must be between 10 and 500 characters.");
        }

        String authorityUsername = overrideAuthorityResolver.resolveAuthorityUsername(authentication)
                .orElseThrow(() -> new OperationNotAllowedException(
                        "Only MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, or IPD_MANAGER may override admission priority."));

        return doOverridePriority(admissionId, request.getNewPriority(), reason.trim(), authorityUsername);
    }

    /**
     * Performs the override and audit logging. Called after authority check.
     */
    private IPDAdmissionResponseDto doOverridePriority(Long admissionId, PriorityCode newPriority,
                                                        String reason, String authorityUsername) {
        IPDAdmission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + admissionId));
        admission.setAdmissionPriority(newPriority);
        admission.setPriorityAssignmentReason("Override by " + authorityUsername + ": " + reason);
        admission.setPriorityOverridden(true);
        admission.setPriorityOverrideBy(authorityUsername);
        admission.setPriorityOverrideAt(Instant.now());
        admission = admissionRepository.save(admission);

        priorityAuditService.logPriorityOverride(admission.getId(), newPriority, reason, authorityUsername);

        return toResponse(admission, true);
    }

    private static AdmissionPriorityRequest buildPriorityRequest(IPDAdmissionRequestDto request, Bed bed) {
        AdmissionPriorityRequest pr = new AdmissionPriorityRequest();
        pr.setAdmissionSource(request.getAdmissionSource());
        String wardType = request.getWardType();
        if (wardType == null || wardType.isBlank()) {
            WardType wt = bed.getWard().getWardType();
            wardType = wt != null ? wt.name() : null;
        }
        pr.setWardType(wardType);
        pr.setReferred(Boolean.TRUE.equals(request.getReferred()));
        pr.setSeniorCitizen(Boolean.TRUE.equals(request.getSeniorCitizen()));
        pr.setPregnantWoman(Boolean.TRUE.equals(request.getPregnantWoman()));
        pr.setChild(Boolean.TRUE.equals(request.getChild()));
        pr.setDisabledPatient(Boolean.TRUE.equals(request.getDisabledPatient()));
        return pr;
    }

    private IPDAdmissionResponseDto toResponse(IPDAdmission a, boolean loadCurrentBed) {
        IPDAdmissionResponseDto dto = new IPDAdmissionResponseDto();
        dto.setId(a.getId());
        dto.setAdmissionNumber(a.getAdmissionNumber());
        dto.setPatientUhid(a.getPatient().getUhid());
        dto.setPatientId(a.getPatient().getId());
        dto.setPatientName(a.getPatient().getFullName());
        dto.setPrimaryDoctorId(a.getPrimaryDoctor().getId());
        dto.setPrimaryDoctorName(a.getPrimaryDoctor().getFullName());
        dto.setPrimaryDoctorCode(a.getPrimaryDoctor().getCode());
        dto.setAdmissionType(a.getAdmissionType());
        dto.setAdmissionStatus(a.getAdmissionStatus());
        dto.setAdmissionDateTime(a.getAdmissionDateTime());
        dto.setDischargeDateTime(a.getDischargeDateTime());
        dto.setOpdVisitId(a.getOpdVisitId());
        dto.setRemarks(a.getRemarks());
        dto.setDiagnosis(a.getDiagnosis());
        dto.setDepositAmount(a.getDepositAmount());
        dto.setInsuranceTpa(a.getInsuranceTpa());
        dto.setAdmissionFormDocumentRef(a.getAdmissionFormDocumentRef());
        dto.setConsentFormDocumentRef(a.getConsentFormDocumentRef());
        dto.setIdProofDocumentRef(a.getIdProofDocumentRef());
        dto.setDischargeRemarks(a.getDischargeRemarks());
        dto.setAdmissionPriority(a.getAdmissionPriority());
        dto.setPriorityAssignmentReason(a.getPriorityAssignmentReason());
        dto.setPriorityOverridden(a.getPriorityOverridden());
        dto.setPriorityOverrideBy(a.getPriorityOverrideBy());
        dto.setPriorityOverrideAt(a.getPriorityOverrideAt());
        dto.setShiftedToWardAt(a.getShiftedToWardAt());
        dto.setShiftedToWardBy(a.getShiftedToWardBy());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());

        if (loadCurrentBed) {
            bedAllocationRepository.findActiveByAdmissionIdWithBedAndRoom(a.getId()).ifPresent(alloc -> {
                dto.setCurrentBedId(alloc.getBed().getId());
                dto.setCurrentBedNumber(alloc.getBed().getBedNumber());
                dto.setCurrentWardId(alloc.getBed().getWard().getId());
                dto.setCurrentWardName(alloc.getBed().getWard().getName());
                if (alloc.getBed().getRoom() != null) {
                    dto.setCurrentRoomNumber(alloc.getBed().getRoom().getRoomNumber());
                }
            });
        }
        return dto;
    }
}
