package com.hospital.hms.doctor.service;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.repository.AppUserRepository;
import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.appointment.dto.AppointmentResponseDto;
import com.hospital.hms.appointment.entity.Appointment;
import com.hospital.hms.appointment.repository.AppointmentRepository;
import com.hospital.hms.doctor.dto.*;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.DoctorAvailability;
import com.hospital.hms.doctor.entity.DoctorStatus;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.DoctorAvailabilityRepository;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.doctor.repository.MedicalDepartmentRepository;
import com.hospital.hms.hospital.entity.Hospital;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Doctor / medical staff master service. DB-agnostic.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final MedicalDepartmentRepository departmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppUserRepository appUserRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantContextService tenantContextService;

    public DoctorService(DoctorRepository doctorRepository,
                         MedicalDepartmentRepository departmentRepository,
                         DoctorAvailabilityRepository availabilityRepository,
                         AppUserRepository appUserRepository,
                         AppointmentRepository appointmentRepository,
                         TenantContextService tenantContextService) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.appUserRepository = appUserRepository;
        this.appointmentRepository = appointmentRepository;
        this.tenantContextService = tenantContextService;
    }

    @Transactional
    public DoctorResponseDto create(DoctorRequestDto request) {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        if (doctorRepository.findByCodeAndHospitalId(request.getCode().trim(), hospital.getId()).isPresent()) {
            throw new IllegalArgumentException("Doctor code already exists: " + request.getCode());
        }
        MedicalDepartment department = departmentRepository.findByIdAndHospitalId(request.getDepartmentId(), hospital.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
        Doctor doctor = new Doctor();
        doctor.setHospital(hospital);
        mapRequestToEntity(request, doctor, department);
        doctor = doctorRepository.save(doctor);
        return toResponse(doctor, true);
    }

    @Transactional
    public DoctorResponseDto update(Long id, DoctorRequestDto request) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Doctor doctor = doctorRepository.findByIdAndHospitalId(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        MedicalDepartment department = departmentRepository.findByIdAndHospitalId(request.getDepartmentId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
        mapRequestToEntity(request, doctor, department);
        doctor = doctorRepository.save(doctor);
        return toResponse(doctor, true);
    }

    public DoctorResponseDto getById(Long id) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Doctor doctor = doctorRepository.findByIdWithDepartmentAndHospitalId(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return toResponse(doctor, true);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDto> search(String code, Long departmentId, DoctorStatus status, String search, int page, int size) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName"));
        Page<Doctor> result = doctorRepository.search(
            hospitalId,
                code != null && !code.isBlank() ? code.trim() : null,
                departmentId,
                status,
                search != null && !search.isBlank() ? search.trim() : null,
                pageable
        );
        List<DoctorResponseDto> content = result.getContent().stream()
                .map(d -> toResponse(d, false))
                .collect(Collectors.toList());
        return new PageImpl<>(content, result.getPageable(), result.getTotalElements());
    }

    @Transactional
    public DoctorAvailabilityResponseDto addAvailability(Long doctorId, DoctorAvailabilityRequestDto request) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Doctor doctor = doctorRepository.findByIdAndHospitalId(doctorId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        DoctorAvailability existing = availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, request.getDayOfWeek()).orElse(null);
        DoctorAvailability slot;
        if (existing != null) {
            existing.setStartTime(request.getStartTime());
            existing.setEndTime(request.getEndTime());
            existing.setOnCall(request.getOnCall() != null ? request.getOnCall() : false);
            slot = availabilityRepository.save(existing);
        } else {
            slot = new DoctorAvailability();
            slot.setDoctor(doctor);
            slot.setDayOfWeek(request.getDayOfWeek());
            slot.setStartTime(request.getStartTime());
            slot.setEndTime(request.getEndTime());
            slot.setOnCall(request.getOnCall() != null ? request.getOnCall() : false);
            slot = availabilityRepository.save(slot);
        }
        return toAvailabilityResponse(slot);
    }

    public List<DoctorAvailabilityResponseDto> getAvailability(Long doctorId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        doctorRepository.findByIdAndHospitalId(doctorId, hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        return availabilityRepository.findByDoctorIdOrderByDayOfWeekAsc(doctorId)
                .stream()
                .map(this::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorResponseDto getCurrentDoctor() {
        return toResponse(resolveCurrentDoctorEntity(), true);
    }

    @Transactional
    public DoctorAvailabilityResponseDto updateMyAvailability(DoctorAvailabilityRequestDto request) {
        Doctor doctor = resolveCurrentDoctorEntity();
        return addAvailability(doctor.getId(), request);
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponseDto> getMyAvailability() {
        Doctor doctor = resolveCurrentDoctorEntity();
        return getAvailability(doctor.getId());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getMyAppointments(LocalDate date) {
        Doctor doctor = resolveCurrentDoctorEntity();
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return appointmentRepository.findQueueWithAssociations(hospitalId, targetDate, doctor.getId())
                .stream()
                .map(this::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    private Doctor resolveCurrentDoctorEntity() {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(String.valueOf(authentication.getPrincipal()))) {
            throw new OperationNotAllowedException("Authenticated user context is required");
        }

        AppUser currentUser = appUserRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));

        return doctorRepository.findByAppUserIdAndHospitalId(currentUser.getId(), hospitalId)
                .or(() -> currentUser.getEmail() == null ? java.util.Optional.empty() : doctorRepository.findByEmailIgnoreCaseAndHospitalId(currentUser.getEmail(), hospitalId))
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile is not linked to the current user"));
    }

    private void mapRequestToEntity(DoctorRequestDto request, Doctor doctor, MedicalDepartment department) {
        doctor.setCode(request.getCode().trim());
        doctor.setFullName(request.getFullName().trim());
        doctor.setDepartment(department);
        doctor.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        doctor.setDoctorType(request.getDoctorType());
        doctor.setStatus(request.getStatus() != null ? request.getStatus() : DoctorStatus.ACTIVE);
        doctor.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        doctor.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        doctor.setQualifications(request.getQualifications() != null ? request.getQualifications().trim() : null);
        doctor.setOnCall(request.getOnCall() != null ? request.getOnCall() : false);
    }

    private DoctorResponseDto toResponse(Doctor d, boolean loadAvailability) {
        DoctorResponseDto resp = new DoctorResponseDto();
        resp.setId(d.getId());
        resp.setCode(d.getCode());
        resp.setFullName(d.getFullName());
        resp.setDepartmentId(d.getDepartment().getId());
        resp.setDepartmentName(d.getDepartment().getName());
        resp.setDepartmentCode(d.getDepartment().getCode());
        resp.setSpecialization(d.getSpecialization());
        resp.setDoctorType(d.getDoctorType());
        resp.setStatus(d.getStatus());
        resp.setPhone(d.getPhone());
        resp.setEmail(d.getEmail());
        resp.setQualifications(d.getQualifications());
        resp.setOnCall(d.getOnCall());
        resp.setCreatedAt(d.getCreatedAt());
        resp.setUpdatedAt(d.getUpdatedAt());
        if (loadAvailability) {
            resp.setAvailability(availabilityRepository.findByDoctorIdOrderByDayOfWeekAsc(d.getId())
                    .stream()
                    .map(this::toAvailabilityResponse)
                    .collect(Collectors.toList()));
        }
        return resp;
    }

    private DoctorAvailabilityResponseDto toAvailabilityResponse(DoctorAvailability a) {
        DoctorAvailabilityResponseDto dto = new DoctorAvailabilityResponseDto();
        dto.setId(a.getId());
        dto.setDayOfWeek(a.getDayOfWeek());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setOnCall(a.getOnCall());
        return dto;
    }

    private AppointmentResponseDto toAppointmentResponse(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientUhid(appointment.getPatient().getUhid());
        dto.setPatientName(appointment.getPatient().getFullName());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getFullName());
        dto.setDoctorCode(appointment.getDoctor().getCode());
        dto.setDepartmentId(appointment.getDepartment().getId());
        dto.setDepartmentName(appointment.getDepartment().getName());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setSlotTime(appointment.getSlotTime());
        dto.setTokenNo(appointment.getTokenNo());
        dto.setStatus(appointment.getStatus());
        dto.setSource(appointment.getSource());
        dto.setVisitType(appointment.getVisitType());
        dto.setCreatedBy(appointment.getCreatedBy());
        dto.setCancelReason(appointment.getCancelReason());
        dto.setOpdVisitId(appointment.getOpdVisitId());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }
}
