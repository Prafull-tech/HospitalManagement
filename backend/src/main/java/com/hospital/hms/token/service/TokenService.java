package com.hospital.hms.token.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.DoctorStatus;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.opd.dto.OPDVisitRequestDto;
import com.hospital.hms.opd.dto.OPDVisitResponseDto;
import com.hospital.hms.opd.entity.VisitType;
import com.hospital.hms.opd.service.OPDVisitService;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import com.hospital.hms.token.dto.*;
import com.hospital.hms.token.entity.Token;
import com.hospital.hms.token.entity.TokenAuditEventType;
import com.hospital.hms.token.entity.TokenAuditLog;
import com.hospital.hms.token.entity.TokenPriority;
import com.hospital.hms.token.entity.TokenStatus;
import com.hospital.hms.token.repository.TokenAuditLogRepository;
import com.hospital.hms.token.repository.TokenRepository;
import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final TokenAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OPDVisitService opdVisitService;
    private final TenantContextService tenantContextService;

    public TokenService(TokenRepository tokenRepository,
                        TokenAuditLogRepository auditLogRepository,
                        PatientRepository patientRepository,
                        DoctorRepository doctorRepository,
                        OPDVisitService opdVisitService,
                        TenantContextService tenantContextService) {
        this.tokenRepository = tokenRepository;
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.opdVisitService = opdVisitService;
        this.tenantContextService = tenantContextService;
    }

    @Transactional
    public TokenResponseDto generate(TokenGenerateRequestDto request) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));
        validatePatientBelongsToCurrentHospital(patient);
        Doctor doctor = doctorRepository.findByIdAndHospitalId(request.getDoctorId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));
        MedicalDepartment department = doctor.getDepartment();
        if (request.getDepartmentId() != null && !request.getDepartmentId().equals(department.getId())) {
            // Allow override if needed; for now use doctor's department
        }

        LocalDate today = LocalDate.now();
        Integer maxNum = tokenRepository.findMaxTokenNumberForDoctorAndDate(doctor.getId(), today);
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        String tokenNo = String.format("OPD-%03d", nextNum);

        Token token = new Token();
        token.setTokenNo(tokenNo);
        token.setTokenNumber(nextNum);
        token.setPatient(patient);
        token.setDoctor(doctor);
        token.setDepartment(department);
        token.setTokenDate(today);
        token.setAppointmentId(request.getAppointmentId());
        token.setPriority(request.getPriority() != null ? request.getPriority() : TokenPriority.NORMAL);
        token.setStatus(TokenStatus.WAITING);
        token = tokenRepository.save(token);

        audit(token.getId(), TokenAuditEventType.GENERATED);
        return toDto(token);
    }

    @Transactional(readOnly = true)
    public List<TokenResponseDto> getQueue(Long doctorId, LocalDate date) {
        findDoctorInCurrentHospital(doctorId);
        LocalDate d = date != null ? date : LocalDate.now();
        List<Token> tokens = tokenRepository.findQueueByDoctorAndDate(doctorId, d);
        return tokens.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TokenDisplayDto getCurrentDisplay(Long doctorId, LocalDate date) {
        Doctor doctor = findDoctorInCurrentHospital(doctorId);
        LocalDate d = date != null ? date : LocalDate.now();
        List<Token> queue = tokenRepository.findQueueByDoctorAndDate(doctorId, d);

        TokenDisplayDto dto = new TokenDisplayDto();
        dto.setDoctorName(doctor.getFullName());
        dto.setRoomNo(doctor.getCode() != null ? "Room " + doctor.getCode() : "—");

        Token current = queue.stream()
                .filter(t -> t.getStatus() == TokenStatus.CALLED || t.getStatus() == TokenStatus.IN_CONSULTATION)
                .findFirst()
                .orElse(null);
        if (current != null) {
            dto.setCurrentToken(current.getTokenNo());
        }

        Token next = queue.stream()
                .filter(t -> t.getStatus() == TokenStatus.WAITING)
                .findFirst()
                .orElse(null);
        if (next != null) {
            dto.setNextToken(next.getTokenNo());
        }
        return dto;
    }

    @Transactional
    public TokenResponseDto callNext(Long doctorId, LocalDate date) {
        findDoctorInCurrentHospital(doctorId);
        LocalDate d = date != null ? date : LocalDate.now();
        List<Token> waiting = tokenRepository.findWaitingByDoctorAndDate(doctorId, d, TokenStatus.WAITING);
        if (waiting.isEmpty()) {
            throw new ResourceNotFoundException("No waiting tokens for doctor " + doctorId);
        }
        Token token = waiting.get(0);

        Instant now = Instant.now();
        token.setStatus(TokenStatus.CALLED);
        token.setCalledAt(now);
        token = tokenRepository.save(token);

        // Create OPD visit when token is called
        OPDVisitRequestDto visitReq = new OPDVisitRequestDto();
        visitReq.setPatientUhid(token.getPatient().getUhid());
        visitReq.setDoctorId(token.getDoctor().getId());
        visitReq.setVisitDate(token.getTokenDate());
        OPDVisitResponseDto visit = opdVisitService.registerVisit(visitReq, VisitType.OPD);
        token.setOpdVisitId(visit.getId());
        tokenRepository.save(token);

        audit(token.getId(), TokenAuditEventType.CALLED);
        return toDto(token);
    }

    @Transactional
    public TokenResponseDto startConsultation(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found: " + tokenId));
        validateTokenBelongsToCurrentHospital(token);
        if (token.getStatus() != TokenStatus.CALLED) {
            throw new IllegalStateException("Token must be in CALLED status to start consultation");
        }
        token.setStatus(TokenStatus.IN_CONSULTATION);
        token = tokenRepository.save(token);
        audit(token.getId(), TokenAuditEventType.STARTED_CONSULTATION);
        return toDto(token);
    }

    @Transactional
    public TokenResponseDto complete(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found: " + tokenId));
        validateTokenBelongsToCurrentHospital(token);
        if (token.getStatus() != TokenStatus.IN_CONSULTATION && token.getStatus() != TokenStatus.CALLED) {
            throw new IllegalStateException("Token must be in consultation to complete");
        }
        Instant now = Instant.now();
        token.setStatus(TokenStatus.COMPLETED);
        token.setCompletedAt(now);
        token = tokenRepository.save(token);
        audit(token.getId(), TokenAuditEventType.COMPLETED);
        return toDto(token);
    }

    @Transactional
    public TokenResponseDto skip(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found: " + tokenId));
        validateTokenBelongsToCurrentHospital(token);
        if (token.getStatus() != TokenStatus.WAITING && token.getStatus() != TokenStatus.CALLED) {
            throw new IllegalStateException("Token cannot be skipped in current status");
        }

        Integer maxSkip = tokenRepository.findQueueByDoctorAndDate(token.getDoctor().getId(), token.getTokenDate())
                .stream()
                .map(Token::getSkipSequence)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        token.setSkipSequence(maxSkip + 1);
        token.setStatus(TokenStatus.SKIPPED);
        token = tokenRepository.save(token);

        // Re-add to waiting queue at end
        token.setStatus(TokenStatus.WAITING);
        token = tokenRepository.save(token);

        audit(token.getId(), TokenAuditEventType.SKIPPED);
        return toDto(token);
    }

    @Transactional(readOnly = true)
    public TokenDashboardDto getDashboard(Long doctorId, LocalDate date) {
        findDoctorInCurrentHospital(doctorId);
        LocalDate d = date != null ? date : LocalDate.now();
        List<Token> all = tokenRepository.findQueueByDoctorAndDate(doctorId, d);

        TokenDashboardDto dto = new TokenDashboardDto();
        dto.setWaiting(all.stream().filter(t -> t.getStatus() == TokenStatus.WAITING).map(this::toDto).collect(Collectors.toList()));
        dto.setInConsultation(all.stream().filter(t -> t.getStatus() == TokenStatus.IN_CONSULTATION || t.getStatus() == TokenStatus.CALLED).map(this::toDto).collect(Collectors.toList()));
        dto.setCompleted(all.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).map(this::toDto).collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getWalkInStats(LocalDate date) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LocalDate d = date != null ? date : LocalDate.now();
        List<Token> all = tokenRepository.findByHospitalIdAndTokenDate(hospitalId, d);
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("walkInsToday", (long) all.size());
        stats.put("patientsWaiting", all.stream().filter(t -> t.getStatus() == TokenStatus.WAITING).count());
        stats.put("patientsConsulted", all.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).count());
        stats.put("inConsultation", all.stream().filter(t -> t.getStatus() == TokenStatus.IN_CONSULTATION || t.getStatus() == TokenStatus.CALLED).count());
        stats.put("emergencyWalkIns", all.stream().filter(t -> t.getPriority() == TokenPriority.EMERGENCY).count());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<TokenDisplayDto> getAllCurrentDisplays(LocalDate date) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LocalDate d = date != null ? date : LocalDate.now();
        List<Token> hospitalTokens = tokenRepository.findByHospitalIdAndTokenDate(hospitalId, d);
        List<Long> doctorIds = hospitalTokens.stream()
                .map(t -> t.getDoctor().getId())
                .distinct()
                .collect(Collectors.toList());

        List<TokenDisplayDto> result = new ArrayList<>();
        for (Long docId : doctorIds) {
            result.add(getCurrentDisplay(docId, d));
        }
        return result;
    }

    private void validatePatientBelongsToCurrentHospital(Patient patient) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new OperationNotAllowedException("Patient does not belong to current hospital");
        }
    }

    private void validateTokenBelongsToCurrentHospital(Token token) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        if (!token.getPatient().getHospital().getId().equals(hospitalId)) {
            throw new OperationNotAllowedException("Token does not belong to current hospital");
        }
    }

    private Doctor findDoctorInCurrentHospital(Long doctorId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        return doctorRepository.findByIdAndHospitalId(doctorId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
    }

    private void audit(Long tokenId, TokenAuditEventType eventType) {
        TokenAuditLog log = new TokenAuditLog();
        log.setTokenId(tokenId);
        log.setEventType(eventType);
        log.setEventAt(Instant.now());
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .ifPresent(log::setPerformedBy);
        auditLogRepository.save(log);
    }

    private TokenResponseDto toDto(Token t) {
        TokenResponseDto dto = new TokenResponseDto();
        dto.setId(t.getId());
        dto.setTokenNo(t.getTokenNo());
        dto.setPatientId(t.getPatient().getId());
        dto.setPatientName(t.getPatient().getFullName());
        dto.setUhid(t.getPatient().getUhid());
        dto.setDoctorId(t.getDoctor().getId());
        dto.setDoctorName(t.getDoctor().getFullName());
        dto.setDoctorCode(t.getDoctor().getCode());
        dto.setDepartmentId(t.getDepartment().getId());
        dto.setDepartmentName(t.getDepartment().getName());
        dto.setTokenDate(t.getTokenDate());
        dto.setAppointmentId(t.getAppointmentId());
        dto.setPriority(t.getPriority());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setCalledAt(t.getCalledAt());
        dto.setCompletedAt(t.getCompletedAt());
        dto.setOpdVisitId(t.getOpdVisitId());
        return dto;
    }
}
