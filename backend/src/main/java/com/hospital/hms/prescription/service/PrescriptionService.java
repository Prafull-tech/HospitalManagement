package com.hospital.hms.prescription.service;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.repository.AppUserRepository;
import com.hospital.hms.common.exception.BadRequestException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.prescription.dto.PrescriptionItemRequestDto;
import com.hospital.hms.prescription.dto.PrescriptionItemResponseDto;
import com.hospital.hms.prescription.dto.PrescriptionRequestDto;
import com.hospital.hms.prescription.dto.PrescriptionResponseDto;
import com.hospital.hms.prescription.entity.Prescription;
import com.hospital.hms.prescription.entity.PrescriptionItem;
import com.hospital.hms.prescription.repository.PrescriptionRepository;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OPDVisitRepository opdVisitRepository;
    private final IPDAdmissionRepository ipdAdmissionRepository;
    private final TenantContextService tenantContextService;
    private final PrescriptionNumberGenerator prescriptionNumberGenerator;
    private final AppUserRepository appUserRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository,
                               OPDVisitRepository opdVisitRepository,
                               IPDAdmissionRepository ipdAdmissionRepository,
                               TenantContextService tenantContextService,
                               PrescriptionNumberGenerator prescriptionNumberGenerator,
                               AppUserRepository appUserRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.opdVisitRepository = opdVisitRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.tenantContextService = tenantContextService;
        this.prescriptionNumberGenerator = prescriptionNumberGenerator;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public PrescriptionResponseDto create(PrescriptionRequestDto request) {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        validateContext(request);

        Patient patient = patientRepository.findByUhidAndHospitalIdAndActiveTrue(request.getPatientUhid().trim(), hospital.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found for UHID: " + request.getPatientUhid()));

        Doctor doctor = resolveDoctor(request.getDoctorId(), hospital.getId());
        OPDVisit opdVisit = resolveOpdVisit(request.getOpdVisitId(), hospital.getId(), patient.getId());
        IPDAdmission ipdAdmission = resolveIpdAdmission(request.getIpdAdmissionId(), hospital.getId(), patient.getId());

        Prescription prescription = new Prescription();
        prescription.setHospital(hospital);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setOpdVisit(opdVisit);
        prescription.setIpdAdmission(ipdAdmission);
        prescription.setPrescriptionDate(LocalDate.now());
        prescription.setPrescriptionNumber(prescriptionNumberGenerator.generate());
        prescription.setNotes(trimToNull(request.getNotes()));
        prescription.setFollowUpDate(request.getFollowUpDate());
        prescription.setItems(new ArrayList<>());

        for (PrescriptionItemRequestDto itemRequest : request.getItems()) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setMedicineName(itemRequest.getMedicineName().trim());
            item.setDosage(trimToNull(itemRequest.getDosage()));
            item.setFrequency(trimToNull(itemRequest.getFrequency()));
            item.setDuration(trimToNull(itemRequest.getDuration()));
            item.setRoute(trimToNull(itemRequest.getRoute()));
            item.setInstructions(trimToNull(itemRequest.getInstructions()));
            item.setQuantity(itemRequest.getQuantity());
            prescription.getItems().add(item);
        }

        return toResponse(prescriptionRepository.save(prescription));
    }

    @Transactional(readOnly = true)
    public PrescriptionResponseDto getById(Long id) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Prescription prescription = prescriptionRepository.findByIdAndHospitalId(id, hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
        return toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> listByPatient(Long patientId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        return prescriptionRepository.findByPatientIdAndHospitalIdOrderByPrescriptionDateDescIdDesc(patientId, hospitalId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> listByDoctor(Long doctorId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        return prescriptionRepository.findByDoctorIdAndHospitalIdOrderByPrescriptionDateDescIdDesc(doctorId, hospitalId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> listByOpdVisit(Long opdVisitId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        return prescriptionRepository.findByOpdVisitIdAndHospitalIdOrderByPrescriptionDateDescIdDesc(opdVisitId, hospitalId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponseDto> search(Long patientId,
                                                Long doctorId,
                                                Long opdVisitId,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                int page,
                                                int size) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();

        if (patientId == null && doctorId == null && opdVisitId == null && fromDate == null && toDate == null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "prescriptionDate", "id"));
            Page<Prescription> result = prescriptionRepository.search(hospitalId, null, null, null, null, null, pageable);
            return result.map(this::toResponse);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "prescriptionDate", "id"));
        Page<Prescription> result = prescriptionRepository.search(hospitalId, patientId, doctorId, opdVisitId, fromDate, toDate, pageable);
        List<PrescriptionResponseDto> content = result.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return new PageImpl<>(content, result.getPageable(), result.getTotalElements());
    }

    private void validateContext(PrescriptionRequestDto request) {
        if (request.getOpdVisitId() == null && request.getIpdAdmissionId() == null) {
            throw new BadRequestException("Either opdVisitId or ipdAdmissionId is required");
        }
        if (request.getOpdVisitId() != null && request.getIpdAdmissionId() != null) {
            throw new BadRequestException("Prescription cannot be linked to both OPD visit and IPD admission at the same time");
        }
    }

    private Doctor resolveDoctor(Long requestedDoctorId, Long hospitalId) {
        if (requestedDoctorId != null) {
            return doctorRepository.findByIdAndHospitalId(requestedDoctorId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + requestedDoctorId));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(String.valueOf(authentication.getPrincipal()))) {
            throw new BadRequestException("Doctor context is required to create a prescription");
        }

        AppUser currentUser = appUserRepository.findByUsernameIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + authentication.getName()));

        return doctorRepository.findByAppUserIdAndHospitalId(currentUser.getId(), hospitalId)
            .or(() -> currentUser.getEmail() == null ? java.util.Optional.empty() : doctorRepository.findByEmailIgnoreCaseAndHospitalId(currentUser.getEmail(), hospitalId))
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile is not linked to the current user"));
    }

    private OPDVisit resolveOpdVisit(Long opdVisitId, Long hospitalId, Long patientId) {
        if (opdVisitId == null) {
            return null;
        }
        OPDVisit visit = opdVisitRepository.findByIdAndPatientHospitalId(opdVisitId, hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + opdVisitId));
        if (!visit.getPatient().getId().equals(patientId)) {
            throw new BadRequestException("OPD visit does not belong to the selected patient");
        }
        return visit;
    }

    private IPDAdmission resolveIpdAdmission(Long ipdAdmissionId, Long hospitalId, Long patientId) {
        if (ipdAdmissionId == null) {
            return null;
        }
        IPDAdmission admission = ipdAdmissionRepository.findByIdAndHospitalId(ipdAdmissionId, hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        if (!admission.getPatient().getId().equals(patientId)) {
            throw new BadRequestException("IPD admission does not belong to the selected patient");
        }
        return admission;
    }

    private PrescriptionResponseDto toResponse(Prescription prescription) {
        PrescriptionResponseDto dto = new PrescriptionResponseDto();
        dto.setId(prescription.getId());
        dto.setPrescriptionNumber(prescription.getPrescriptionNumber());
        dto.setOpdVisitId(prescription.getOpdVisit() != null ? prescription.getOpdVisit().getId() : null);
        dto.setIpdAdmissionId(prescription.getIpdAdmission() != null ? prescription.getIpdAdmission().getId() : null);
        dto.setPatientId(prescription.getPatient().getId());
        dto.setPatientUhid(prescription.getPatient().getUhid());
        dto.setPatientName(prescription.getPatient().getFullName());
        dto.setDoctorId(prescription.getDoctor().getId());
        dto.setDoctorName(prescription.getDoctor().getFullName());
        dto.setHospitalId(prescription.getHospital().getId());
        dto.setPrescriptionDate(prescription.getPrescriptionDate());
        dto.setNotes(prescription.getNotes());
        dto.setFollowUpDate(prescription.getFollowUpDate());
        dto.setItems(prescription.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()));
        dto.setCreatedAt(prescription.getCreatedAt());
        dto.setUpdatedAt(prescription.getUpdatedAt());
        return dto;
    }

    private PrescriptionItemResponseDto toItemResponse(PrescriptionItem item) {
        PrescriptionItemResponseDto dto = new PrescriptionItemResponseDto();
        dto.setId(item.getId());
        dto.setMedicineName(item.getMedicineName());
        dto.setDosage(item.getDosage());
        dto.setFrequency(item.getFrequency());
        dto.setDuration(item.getDuration());
        dto.setRoute(item.getRoute());
        dto.setInstructions(item.getInstructions());
        dto.setQuantity(item.getQuantity());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}