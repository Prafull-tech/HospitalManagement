package com.hospital.hms.walkin.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.opd.dto.OPDVisitRequestDto;
import com.hospital.hms.opd.dto.OPDVisitResponseDto;
import com.hospital.hms.opd.entity.VisitType;
import com.hospital.hms.opd.service.OPDVisitService;
import com.hospital.hms.reception.dto.PatientRequestDto;
import com.hospital.hms.reception.dto.PatientResponseDto;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.service.PatientService;
import com.hospital.hms.token.dto.TokenGenerateRequestDto;
import com.hospital.hms.token.dto.TokenResponseDto;
import com.hospital.hms.token.entity.TokenPriority;
import com.hospital.hms.token.service.TokenService;
import com.hospital.hms.walkin.dto.WalkInRegisterRequestDto;
import com.hospital.hms.walkin.dto.WalkInRegisterResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class WalkInService {

    private final PatientService patientService;
    private final OPDVisitService opdVisitService;
    private final TokenService tokenService;

    public WalkInService(PatientService patientService,
                         OPDVisitService opdVisitService,
                         TokenService tokenService) {
        this.patientService = patientService;
        this.opdVisitService = opdVisitService;
        this.tokenService = tokenService;
    }

    @Transactional
    public WalkInRegisterResponseDto register(WalkInRegisterRequestDto request) {
        PatientResponseDto patient;

        if (request.getPatientId() != null && request.getPatientId() > 0) {
            patient = patientService.getById(request.getPatientId());
        } else {
            if (request.getFullName() == null || request.getFullName().isBlank()) {
                throw new IllegalArgumentException("Patient name is required for new patient");
            }
            if (request.getGender() == null || request.getGender().isBlank()) {
                throw new IllegalArgumentException("Gender is required for new patient");
            }
            if (request.getAge() == null || request.getAge() < 0) {
                throw new IllegalArgumentException("Valid age is required for new patient");
            }
            PatientRequestDto patientReq = new PatientRequestDto();
            patientReq.setFullName(request.getFullName().trim());
            patientReq.setGender(request.getGender().trim());
            patientReq.setAge(request.getAge());
            patientReq.setPhone(request.getMobile() != null ? request.getMobile().trim() : null);
            patientReq.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
            patientReq.setCity(request.getCity() != null ? request.getCity().trim() : null);
            patientReq.setState(request.getState() != null ? request.getState().trim() : null);
            patientReq.setIdProofType(request.getIdProofType() != null ? request.getIdProofType().trim() : null);
            patientReq.setIdProofNumber(request.getIdProofNumber() != null ? request.getIdProofNumber().trim() : null);
            patientReq.setDateOfBirth(null);
            patient = patientService.register(patientReq);
        }

        OPDVisitRequestDto visitReq = new OPDVisitRequestDto();
        visitReq.setPatientUhid(patient.getUhid());
        visitReq.setDoctorId(request.getDoctorId());
        visitReq.setVisitDate(LocalDate.now());
        OPDVisitResponseDto visit = opdVisitService.registerVisit(visitReq, VisitType.OPD);

        TokenGenerateRequestDto tokenReq = new TokenGenerateRequestDto();
        tokenReq.setPatientId(patient.getId());
        tokenReq.setDoctorId(request.getDoctorId());
        tokenReq.setDepartmentId(request.getDepartmentId());
        tokenReq.setPriority(request.getPriority() != null ? request.getPriority() : TokenPriority.NORMAL);
        TokenResponseDto token = tokenService.generate(tokenReq);

        WalkInRegisterResponseDto response = new WalkInRegisterResponseDto();
        response.setPatientUhid(patient.getUhid());
        response.setPatientName(patient.getFullName());
        response.setOpdVisitId(visit.getId());
        response.setVisitNumber(visit.getVisitNumber());
        response.setToken(token);
        return response;
    }
}
