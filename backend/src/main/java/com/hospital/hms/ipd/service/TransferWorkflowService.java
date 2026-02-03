package com.hospital.hms.ipd.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.ipd.dto.*;
import com.hospital.hms.ipd.entity.*;
import com.hospital.hms.ipd.repository.*;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.WardType;
import com.hospital.hms.ward.repository.BedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transfer workflow: recommend → consent → confirm-bed → execute.
 * DB-agnostic. Emergency bypass of consent enforced via EmergencyTransferService in caller/controller.
 */
@Service
public class TransferWorkflowService {

    private final TransferRecommendationRepository recommendationRepository;
    private final TransferConsentRepository consentRepository;
    private final TransferBedReservationRepository bedReservationRepository;
    private final PatientTransferRepository patientTransferRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final IPDAdmissionService admissionService;
    private final EmergencyTransferService emergencyTransferService;

    public TransferWorkflowService(TransferRecommendationRepository recommendationRepository,
                                   TransferConsentRepository consentRepository,
                                   TransferBedReservationRepository bedReservationRepository,
                                   PatientTransferRepository patientTransferRepository,
                                   IPDAdmissionRepository admissionRepository,
                                   DoctorRepository doctorRepository,
                                   BedRepository bedRepository,
                                   IPDAdmissionService admissionService,
                                   EmergencyTransferService emergencyTransferService) {
        this.recommendationRepository = recommendationRepository;
        this.consentRepository = consentRepository;
        this.bedReservationRepository = bedReservationRepository;
        this.patientTransferRepository = patientTransferRepository;
        this.admissionRepository = admissionRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.admissionService = admissionService;
        this.emergencyTransferService = emergencyTransferService;
    }

    @Transactional
    public TransferRecommendResponseDto recommend(TransferRecommendRequestDto request) {
        var admission = admissionRepository.findById(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + request.getIpdAdmissionId()));
        var doctor = doctorRepository.findById(request.getRecommendedByDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getRecommendedByDoctorId()));
        WardType from = parseWardType(request.getFromWardType());
        WardType to = parseWardType(request.getToWardType());

        TransferRecommendation rec = new TransferRecommendation();
        rec.setIpdAdmission(admission);
        rec.setRecommendedByDoctor(doctor);
        rec.setFromWardType(from);
        rec.setToWardType(to);
        rec.setIndicationId(request.getIndicationId());
        rec.setRecommendationNotes(request.getRecommendationNotes());
        rec.setEmergencyFlag(Boolean.TRUE.equals(request.getEmergencyFlag()));
        rec.setRecommendationTime(Instant.now());
        rec = recommendationRepository.save(rec);
        return toRecommendResponse(rec);
    }

    @Transactional
    public TransferConsentResponseDto recordConsent(TransferConsentRequestDto request) {
        var rec = recommendationRepository.findById(request.getTransferRecommendationId())
                .orElseThrow(() -> new ResourceNotFoundException("Transfer recommendation not found: " + request.getTransferRecommendationId()));

        TransferConsent consent = new TransferConsent();
        consent.setTransferRecommendation(rec);
        consent.setConsentGiven(request.getConsentGiven());
        consent.setConsentByName(request.getConsentByName());
        consent.setRelationToPatient(request.getRelationToPatient());
        consent.setConsentMode(request.getConsentMode());
        consent.setConsentTime(Instant.now());
        consent = consentRepository.save(consent);
        return toConsentResponse(consent);
    }

    @Transactional
    public ConfirmBedResponseDto confirmBed(ConfirmBedRequestDto request) {
        var rec = recommendationRepository.findById(request.getTransferRecommendationId())
                .orElseThrow(() -> new ResourceNotFoundException("Transfer recommendation not found: " + request.getTransferRecommendationId()));
        var bed = bedRepository.findById(request.getNewBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found: " + request.getNewBedId()));

        if (bed.getBedStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException("Bed must be AVAILABLE to reserve. Current status: " + bed.getBedStatus());
        }
        if (!Boolean.TRUE.equals(bed.getIsActive())) {
            throw new IllegalArgumentException("Bed is not active.");
        }
        bedReservationRepository.findByNewBedIdAndReservationStatus(bed.getId(), ReservationStatus.RESERVED)
                .ifPresent(r -> {
                    throw new IllegalArgumentException("Bed is already reserved for another transfer. Prevent double allocation.");
                });

        if (!emergencyTransferService.canBypassConsent(rec)) {
            boolean hasConsent = consentRepository.findFirstByTransferRecommendationIdOrderByCreatedAtDesc(rec.getId())
                    .map(c -> Boolean.TRUE.equals(c.getConsentGiven()))
                    .orElse(false);
            if (!hasConsent) {
                throw new IllegalArgumentException("Consent is required before reserving bed for non-emergency transfer.");
            }
        }

        TransferBedReservation res = new TransferBedReservation();
        res.setTransferRecommendation(rec);
        res.setNewBed(bed);
        res.setReservedAt(Instant.now());
        res.setReservationStatus(ReservationStatus.RESERVED);
        res = bedReservationRepository.save(res);
        return toConfirmBedResponse(res);
    }

    @Transactional
    public ExecuteTransferResponseDto execute(ExecuteTransferRequestDto request) {
        var rec = recommendationRepository.findById(request.getTransferRecommendationId())
                .orElseThrow(() -> new ResourceNotFoundException("Transfer recommendation not found: " + request.getTransferRecommendationId()));

        var reservation = bedReservationRepository.findFirstByTransferRecommendationIdAndReservationStatusOrderByReservedAtDesc(
                rec.getId(), ReservationStatus.RESERVED)
                .orElseThrow(() -> new ResourceNotFoundException("No RESERVED bed reservation found for this recommendation. Confirm bed first."));

        PatientTransfer pt = patientTransferRepository.findByIpdAdmissionIdOrderByCreatedAtDesc(rec.getIpdAdmission().getId())
                .stream()
                .filter(p -> p.getFromWardType().equals(rec.getFromWardType()) && p.getToWardType().equals(rec.getToWardType()))
                .findFirst()
                .orElseGet(() -> {
                    PatientTransfer newPt = new PatientTransfer();
                    newPt.setIpdAdmission(rec.getIpdAdmission());
                    newPt.setFromWardType(rec.getFromWardType());
                    newPt.setToWardType(rec.getToWardType());
                    newPt.setTransferType(TransferType.INTERNAL);
                    newPt.setTransferStatus(TransferStatus.BED_RESERVED);
                    return patientTransferRepository.save(newPt);
                });

        pt.setTransferStatus(parseTransferStatus(request.getTransferStatus() != null ? request.getTransferStatus() : "COMPLETED"));
        pt.setTransferTime(Instant.now());
        pt.setNurseId(request.getNurseId());
        pt.setAttendantId(request.getAttendantId());
        pt.setEquipmentUsed(request.getEquipmentUsed());
        pt = patientTransferRepository.save(pt);

        IPDTransferRequestDto transferReq = new IPDTransferRequestDto();
        transferReq.setBedId(reservation.getNewBed().getId());
        transferReq.setRemarks("Transfer executed from recommendation " + rec.getId());
        admissionService.transfer(rec.getIpdAdmission().getId(), transferReq);

        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        bedReservationRepository.save(reservation);

        return toExecuteResponse(pt);
    }

    @Transactional(readOnly = true)
    public TransferSummaryResponseDto getTransfersByAdmissionId(Long ipdAdmissionId) {
        if (!admissionRepository.existsById(ipdAdmissionId)) {
            throw new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId);
        }
        TransferSummaryResponseDto summary = new TransferSummaryResponseDto();
        summary.setIpdAdmissionId(ipdAdmissionId);

        List<TransferRecommendation> recs = recommendationRepository.findByIpdAdmissionIdOrderByRecommendationTimeDesc(ipdAdmissionId);
        summary.setRecommendations(recs.stream().map(TransferWorkflowService::toRecommendResponse).collect(Collectors.toList()));

        List<TransferConsentResponseDto> consents = new ArrayList<>();
        for (TransferRecommendation r : recs) {
            consentRepository.findByTransferRecommendationIdOrderByCreatedAtDesc(r.getId()).stream()
                    .map(TransferWorkflowService::toConsentResponse)
                    .forEach(consents::add);
        }
        summary.setConsents(consents);

        List<ConfirmBedResponseDto> beds = new ArrayList<>();
        for (TransferRecommendation r : recs) {
            bedReservationRepository.findByTransferRecommendationIdOrderByReservedAtDesc(r.getId()).stream()
                    .map(TransferWorkflowService::toConfirmBedResponse)
                    .forEach(beds::add);
        }
        summary.setBedReservations(beds);

        summary.setExecutions(patientTransferRepository.findByIpdAdmissionIdOrderByCreatedAtDesc(ipdAdmissionId)
                .stream()
                .map(TransferWorkflowService::toExecuteResponse)
                .collect(Collectors.toList()));

        return summary;
    }

    private static WardType parseWardType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Ward type is required.");
        }
        try {
            return WardType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ward type: " + value + ". Valid: " + java.util.Arrays.toString(WardType.values()));
        }
    }

    /**
     * Full transfer workflow in one call: recommend → consent → confirm-bed → execute.
     * System updates: old bed → VACANT (AVAILABLE), new bed → OCCUPIED, admission status → SHIFTED (TRANSFERRED).
     */
    @Transactional
    public IPDTransferFullResponseDto executeFullTransfer(IPDTransferFullRequestDto request) {
        TransferRecommendRequestDto recReq = new TransferRecommendRequestDto();
        recReq.setIpdAdmissionId(request.getIpdAdmissionId());
        recReq.setRecommendedByDoctorId(request.getRecommendedByDoctorId());
        recReq.setFromWardType(request.getFromWardType());
        recReq.setToWardType(request.getToWardType());
        recReq.setRecommendationNotes(request.getRecommendationNotes());
        recReq.setEmergencyFlag(request.getEmergencyFlag());
        TransferRecommendResponseDto recResp = recommend(recReq);

        TransferConsentRequestDto consentReq = new TransferConsentRequestDto();
        consentReq.setTransferRecommendationId(recResp.getId());
        consentReq.setConsentGiven(request.getConsentGiven());
        consentReq.setConsentByName(request.getConsentByName());
        consentReq.setRelationToPatient(request.getRelationToPatient());
        consentReq.setConsentMode(request.getConsentMode());
        TransferConsentResponseDto consentResp = recordConsent(consentReq);

        ConfirmBedRequestDto bedReq = new ConfirmBedRequestDto();
        bedReq.setTransferRecommendationId(recResp.getId());
        bedReq.setNewBedId(request.getNewBedId());
        ConfirmBedResponseDto bedResp = confirmBed(bedReq);

        ExecuteTransferRequestDto execReq = new ExecuteTransferRequestDto();
        execReq.setTransferRecommendationId(recResp.getId());
        execReq.setNurseId(request.getNurseId());
        execReq.setAttendantId(request.getAttendantId());
        execReq.setEquipmentUsed(request.getEquipmentUsed());
        execReq.setTransferStatus(request.getTransferStatus());
        ExecuteTransferResponseDto execResp = execute(execReq);

        IPDAdmission admission = admissionRepository.findById(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found after transfer: " + request.getIpdAdmissionId()));

        IPDTransferFullResponseDto response = new IPDTransferFullResponseDto();
        response.setIpdAdmissionId(admission.getId());
        response.setAdmissionNumber(admission.getAdmissionNumber());
        response.setAdmissionStatus(admission.getAdmissionStatus());
        response.setRecommendationId(recResp.getId());
        response.setConsentId(consentResp.getId());
        response.setBedReservationId(bedResp.getId());
        response.setPatientTransferId(execResp.getId());
        response.setOldBedStatus("VACANT");
        response.setNewBedStatus("OCCUPIED");
        response.setSystemUpdateSummary("Old bed → VACANT; New bed → OCCUPIED; Admission status → SHIFTED (TRANSFERRED).");
        response.setTransferredAt(execResp.getTransferTime());

        IPDAdmissionResponseDto admissionDto = admissionService.getById(admission.getId());
        response.setCurrentBedId(admissionDto.getCurrentBedId());
        response.setCurrentBedNumber(admissionDto.getCurrentBedNumber());
        response.setCurrentWardId(admissionDto.getCurrentWardId());
        response.setCurrentWardName(admissionDto.getCurrentWardName());

        return response;
    }

    private static TransferStatus parseTransferStatus(String value) {
        if (value == null || value.isBlank()) {
            return TransferStatus.COMPLETED;
        }
        try {
            return TransferStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TransferStatus.COMPLETED;
        }
    }

    private static TransferRecommendResponseDto toRecommendResponse(TransferRecommendation r) {
        TransferRecommendResponseDto dto = new TransferRecommendResponseDto();
        dto.setId(r.getId());
        dto.setIpdAdmissionId(r.getIpdAdmission().getId());
        dto.setRecommendedByDoctorId(r.getRecommendedByDoctor().getId());
        dto.setFromWardType(r.getFromWardType().name());
        dto.setToWardType(r.getToWardType().name());
        dto.setIndicationId(r.getIndicationId());
        dto.setRecommendationNotes(r.getRecommendationNotes());
        dto.setEmergencyFlag(r.getEmergencyFlag());
        dto.setRecommendationTime(r.getRecommendationTime());
        return dto;
    }

    private static TransferConsentResponseDto toConsentResponse(TransferConsent c) {
        TransferConsentResponseDto dto = new TransferConsentResponseDto();
        dto.setId(c.getId());
        dto.setTransferRecommendationId(c.getTransferRecommendation().getId());
        dto.setConsentGiven(c.getConsentGiven());
        dto.setConsentByName(c.getConsentByName());
        dto.setRelationToPatient(c.getRelationToPatient());
        dto.setConsentTime(c.getConsentTime());
        dto.setConsentMode(c.getConsentMode());
        return dto;
    }

    private static ConfirmBedResponseDto toConfirmBedResponse(TransferBedReservation r) {
        ConfirmBedResponseDto dto = new ConfirmBedResponseDto();
        dto.setId(r.getId());
        dto.setTransferRecommendationId(r.getTransferRecommendation().getId());
        dto.setNewBedId(r.getNewBed().getId());
        dto.setReservedAt(r.getReservedAt());
        dto.setReservationStatus(r.getReservationStatus().name());
        return dto;
    }

    private static ExecuteTransferResponseDto toExecuteResponse(PatientTransfer p) {
        ExecuteTransferResponseDto dto = new ExecuteTransferResponseDto();
        dto.setId(p.getId());
        dto.setIpdAdmissionId(p.getIpdAdmission().getId());
        dto.setFromWardType(p.getFromWardType().name());
        dto.setToWardType(p.getToWardType().name());
        dto.setTransferType(p.getTransferType().name());
        dto.setTransferStatus(p.getTransferStatus().name());
        dto.setNurseId(p.getNurseId());
        dto.setAttendantId(p.getAttendantId());
        dto.setEquipmentUsed(p.getEquipmentUsed() != null ? p.getEquipmentUsed().name() : null);
        dto.setTransferTime(p.getTransferTime());
        return dto;
    }
}
