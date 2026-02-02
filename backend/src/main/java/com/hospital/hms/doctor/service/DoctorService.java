package com.hospital.hms.doctor.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.dto.*;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.DoctorAvailability;
import com.hospital.hms.doctor.entity.DoctorStatus;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.DoctorAvailabilityRepository;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.doctor.repository.MedicalDepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public DoctorService(DoctorRepository doctorRepository,
                         MedicalDepartmentRepository departmentRepository,
                         DoctorAvailabilityRepository availabilityRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional
    public DoctorResponseDto create(DoctorRequestDto request) {
        if (doctorRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Doctor code already exists: " + request.getCode());
        }
        MedicalDepartment department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
        Doctor doctor = new Doctor();
        mapRequestToEntity(request, doctor, department);
        doctor = doctorRepository.save(doctor);
        return toResponse(doctor, true);
    }

    @Transactional
    public DoctorResponseDto update(Long id, DoctorRequestDto request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        MedicalDepartment department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
        mapRequestToEntity(request, doctor, department);
        doctor = doctorRepository.save(doctor);
        return toResponse(doctor, true);
    }

    public DoctorResponseDto getById(Long id) {
        Doctor doctor = doctorRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return toResponse(doctor, true);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDto> search(String code, Long departmentId, DoctorStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName"));
        Page<Doctor> result = doctorRepository.search(
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
        Doctor doctor = doctorRepository.findById(doctorId)
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
        return availabilityRepository.findByDoctorIdOrderByDayOfWeekAsc(doctorId)
                .stream()
                .map(this::toAvailabilityResponse)
                .collect(Collectors.toList());
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
}
