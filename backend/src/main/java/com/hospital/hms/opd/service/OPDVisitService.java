package com.hospital.hms.opd.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.opd.dto.*;
import com.hospital.hms.opd.entity.OPDClinicalNote;
import com.hospital.hms.opd.entity.OPDToken;
import com.hospital.hms.opd.entity.ConsultationOutcome;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.entity.VisitType;
import com.hospital.hms.opd.entity.VisitStatus;
import com.hospital.hms.opd.repository.OPDClinicalNoteRepository;
import com.hospital.hms.opd.repository.OPDTokenRepository;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OPD visit service. Enforces business rules; DB-agnostic.
 */
@Service
public class OPDVisitService {

    private final OPDVisitRepository visitRepository;
    private final OPDTokenRepository tokenRepository;
    private final OPDClinicalNoteRepository clinicalNoteRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OPDVisitNumberGenerator visitNumberGenerator;

    public OPDVisitService(OPDVisitRepository visitRepository,
                          OPDTokenRepository tokenRepository,
                          OPDClinicalNoteRepository clinicalNoteRepository,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          OPDVisitNumberGenerator visitNumberGenerator) {
        this.visitRepository = visitRepository;
        this.tokenRepository = tokenRepository;
        this.clinicalNoteRepository = clinicalNoteRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.visitNumberGenerator = visitNumberGenerator;
    }

    @Transactional
    public OPDVisitResponseDto registerVisit(OPDVisitRequestDto request) {
        return registerVisit(request, VisitType.OPD);
    }

    @Transactional
    public OPDVisitResponseDto registerVisit(OPDVisitRequestDto request, VisitType visitType) {
        Patient patient = patientRepository.findByUhid(request.getPatientUhid().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UHID: " + request.getPatientUhid()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));
        LocalDate visitDate = request.getVisitDate();
        if (visitDate == null) {
            visitDate = LocalDate.now();
        }

        String visitNumber = visitNumberGenerator.generate();
        Integer maxToken = tokenRepository.findMaxTokenNumberForDoctorAndDate(doctor.getId(), visitDate);
        int nextToken = (maxToken != null ? maxToken : 0) + 1;

        OPDVisit visit = new OPDVisit();
        visit.setVisitNumber(visitNumber);
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setDepartment(doctor.getDepartment());
        visit.setVisitDate(visitDate);
        visit.setVisitType(visitType != null ? visitType : VisitType.OPD);
        visit.setVisitStatus(VisitStatus.REGISTERED);
        visit.setTokenNumber(nextToken);
        visit = visitRepository.save(visit);

        OPDToken token = new OPDToken();
        token.setVisit(visit);
        token.setDoctor(doctor);
        token.setTokenDate(visitDate);
        token.setTokenNumber(nextToken);
        tokenRepository.save(token);

        return toResponse(visit, true);
    }

    public OPDVisitResponseDto getById(Long id) {
        OPDVisit visit = visitRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + id));
        return toResponse(visit, true);
    }

    @Transactional(readOnly = true)
    public Page<OPDVisitResponseDto> search(LocalDate visitDate, Long doctorId, VisitStatus status,
                                            String patientUhid, String patientName, String visitNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "visitDate", "tokenNumber"));
        Page<OPDVisit> result = visitRepository.search(
                visitDate,
                doctorId,
                status,
                patientUhid != null && !patientUhid.isBlank() ? patientUhid.trim() : null,
                patientName != null && !patientName.isBlank() ? patientName.trim() : null,
                visitNumber != null && !visitNumber.isBlank() ? visitNumber.trim() : null,
                pageable
        );
        List<OPDVisitResponseDto> content = result.getContent().stream()
                .map(v -> toResponse(v, false))
                .collect(Collectors.toList());
        return new PageImpl<>(content, result.getPageable(), result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<OPDVisitResponseDto> getQueue(Long doctorId, LocalDate visitDate) {
        if (visitDate == null) visitDate = LocalDate.now();
        return visitRepository.findByVisitDateAndDoctorIdOrderByTokenNumberAsc(visitDate, doctorId)
                .stream()
                .map(v -> toResponse(v, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public OPDVisitResponseDto updateStatus(Long id, OPDStatusRequestDto request) {
        OPDVisit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + id));
        visit.setVisitStatus(request.getStatus());
        if (request.getConsultationOutcome() != null) {
            visit.setConsultationOutcome(request.getConsultationOutcome());
        }
        visit = visitRepository.save(visit);
        return toResponse(visit, true);
    }

    /**
     * Doctor explicitly marks "Admission Recommended" for the visit. Only DOCTOR role may call.
     * Admission recommendation is stored with the visit for IPD admission integration.
     */
    @Transactional
    public OPDVisitResponseDto recommendAdmission(Long visitId, RecommendAdmissionRequestDto request, Authentication authentication) {
        OPDVisit visit = visitRepository.findByIdWithAssociations(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
        visit.setAdmissionRecommended(true);
        visit.setAdmissionRecommendedAt(Instant.now());
        visit.setAdmissionRecommendedBy(authentication != null ? authentication.getName() : null);
        if (request != null && request.getConsultationOutcome() != null) {
            visit.setConsultationOutcome(request.getConsultationOutcome());
        } else {
            visit.setConsultationOutcome(ConsultationOutcome.IPD_ADMISSION_ADVISED);
        }
        visit = visitRepository.save(visit);
        return toResponse(visit, true);
    }

    @Transactional
    public OPDClinicalNoteResponseDto addOrUpdateNotes(Long visitId, OPDClinicalNoteRequestDto request) {
        OPDVisit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + visitId));
        OPDClinicalNote note = clinicalNoteRepository.findByVisitId(visitId).orElse(null);
        if (note == null) {
            note = new OPDClinicalNote();
            note.setVisit(visit);
        }
        if (request.getChiefComplaint() != null) note.setChiefComplaint(request.getChiefComplaint().trim());
        if (request.getProvisionalDiagnosis() != null) note.setProvisionalDiagnosis(request.getProvisionalDiagnosis().trim());
        if (request.getDoctorRemarks() != null) note.setDoctorRemarks(request.getDoctorRemarks().trim());
        note = clinicalNoteRepository.save(note);
        return toNoteResponse(note);
    }

    @Transactional
    public OPDVisitResponseDto refer(Long visitId, OPDReferRequestDto request) {
        OPDVisit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + visitId));
        visit.setReferredToDepartmentId(request.getReferredToDepartmentId());
        visit.setReferredToDoctorId(request.getReferredToDoctorId());
        visit.setReferToIpd(request.getReferToIpd() != null ? request.getReferToIpd() : false);
        visit.setReferralRemarks(request.getReferralRemarks() != null ? request.getReferralRemarks().trim() : null);
        visit.setVisitStatus(VisitStatus.REFERRED);
        visit = visitRepository.save(visit);
        return toResponse(visit, true);
    }

    private OPDVisitResponseDto toResponse(OPDVisit v, boolean loadNote) {
        OPDVisitResponseDto dto = new OPDVisitResponseDto();
        dto.setId(v.getId());
        dto.setVisitNumber(v.getVisitNumber());
        dto.setPatientUhid(v.getPatient().getUhid());
        dto.setPatientId(v.getPatient().getId());
        dto.setPatientName(v.getPatient().getFullName());
        dto.setDoctorId(v.getDoctor().getId());
        dto.setDoctorName(v.getDoctor().getFullName());
        dto.setDoctorCode(v.getDoctor().getCode());
        dto.setDepartmentId(v.getDepartment().getId());
        dto.setDepartmentName(v.getDepartment().getName());
        dto.setVisitDate(v.getVisitDate());
        dto.setVisitStatus(v.getVisitStatus());
        dto.setTokenNumber(v.getTokenNumber());
        dto.setReferredToDepartmentId(v.getReferredToDepartmentId());
        dto.setReferredToDoctorId(v.getReferredToDoctorId());
        dto.setReferToIpd(v.getReferToIpd());
        dto.setReferralRemarks(v.getReferralRemarks());
        dto.setVisitType(v.getVisitType() != null ? v.getVisitType() : VisitType.OPD);
        dto.setConsultationOutcome(v.getConsultationOutcome());
        dto.setAdmissionRecommended(Boolean.TRUE.equals(v.getAdmissionRecommended()));
        dto.setAdmissionRecommendedAt(v.getAdmissionRecommendedAt());
        dto.setAdmissionRecommendedBy(v.getAdmissionRecommendedBy());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setUpdatedAt(v.getUpdatedAt());
        if (loadNote) {
            clinicalNoteRepository.findByVisitId(v.getId())
                    .map(this::toNoteResponse)
                    .ifPresent(dto::setClinicalNote);
        }
        return dto;
    }

    private OPDClinicalNoteResponseDto toNoteResponse(OPDClinicalNote n) {
        OPDClinicalNoteResponseDto dto = new OPDClinicalNoteResponseDto();
        dto.setId(n.getId());
        dto.setChiefComplaint(n.getChiefComplaint());
        dto.setProvisionalDiagnosis(n.getProvisionalDiagnosis());
        dto.setDoctorRemarks(n.getDoctorRemarks());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setUpdatedAt(n.getUpdatedAt());
        return dto;
    }
}
