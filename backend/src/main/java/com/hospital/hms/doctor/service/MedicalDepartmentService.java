package com.hospital.hms.doctor.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.dto.DepartmentRequestDto;
import com.hospital.hms.doctor.dto.DepartmentResponseDto;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.doctor.repository.MedicalDepartmentRepository;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Department master service. DB-agnostic.
 */
@Service
public class MedicalDepartmentService {

    private final MedicalDepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final TenantContextService tenantContextService;

    public MedicalDepartmentService(MedicalDepartmentRepository departmentRepository,
                                    DoctorRepository doctorRepository,
                                    TenantContextService tenantContextService) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.tenantContextService = tenantContextService;
    }

    public List<DepartmentResponseDto> listAll() {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        return departmentRepository.findAllByHospitalIdOrderByNameAsc(hospitalId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponseDto getById(Long id) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        MedicalDepartment dept = departmentRepository.findByIdAndHospitalId(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return toResponse(dept);
    }

    @Transactional
    public DepartmentResponseDto create(DepartmentRequestDto request) {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        if (departmentRepository.findByCodeAndHospitalId(request.getCode().trim(), hospital.getId()).isPresent()) {
            throw new IllegalArgumentException("Department code already exists: " + request.getCode());
        }
        MedicalDepartment dept = new MedicalDepartment();
        dept.setHospital(hospital);
        mapRequestToEntity(request, dept);
        dept = departmentRepository.save(dept);
        if (request.getHodDoctorId() != null) {
            assignHod(dept.getId(), request.getHodDoctorId());
            dept = departmentRepository.findById(dept.getId()).orElse(dept);
        }
        return toResponse(dept);
    }

    @Transactional
    public DepartmentResponseDto update(Long id, DepartmentRequestDto request) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        MedicalDepartment dept = departmentRepository.findByIdAndHospitalId(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        mapRequestToEntity(request, dept);
        dept = departmentRepository.save(dept);
        if (request.getHodDoctorId() != null) {
            assignHod(id, request.getHodDoctorId());
            dept = departmentRepository.findById(id).orElse(dept);
        } else {
            dept.setHod(null);
            departmentRepository.save(dept);
        }
        return toResponse(dept);
    }

    @Transactional
    public void assignHod(Long departmentId, Long doctorId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        MedicalDepartment dept = departmentRepository.findByIdAndHospitalId(departmentId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
        Doctor doctor = doctorRepository.findByIdAndHospitalId(doctorId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        dept.setHod(doctor);
        departmentRepository.save(dept);
    }

    private void mapRequestToEntity(DepartmentRequestDto request, MedicalDepartment dept) {
        dept.setCode(request.getCode().trim());
        dept.setName(request.getName().trim());
        dept.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
    }

    private DepartmentResponseDto toResponse(MedicalDepartment d) {
        DepartmentResponseDto resp = new DepartmentResponseDto();
        resp.setId(d.getId());
        resp.setCode(d.getCode());
        resp.setName(d.getName());
        resp.setDescription(d.getDescription());
        if (d.getHod() != null) {
            resp.setHodDoctorId(d.getHod().getId());
            resp.setHodDoctorName(d.getHod().getFullName());
        }
        return resp;
    }
}
