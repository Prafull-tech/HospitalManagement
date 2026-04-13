package com.hospital.hms.appointment.service;

import com.hospital.hms.appointment.dto.DoctorScheduleRequestDto;
import com.hospital.hms.appointment.dto.DoctorScheduleResponseDto;
import com.hospital.hms.appointment.entity.DoctorSchedule;
import com.hospital.hms.appointment.repository.DoctorScheduleRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final TenantContextService tenantContextService;

    public DoctorScheduleService(DoctorScheduleRepository scheduleRepository,
                                 DoctorRepository doctorRepository,
                                 TenantContextService tenantContextService) {
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.tenantContextService = tenantContextService;
    }

    @Transactional
    public DoctorScheduleResponseDto create(DoctorScheduleRequestDto request) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Doctor doctor = doctorRepository.findByIdAndHospitalId(request.getDoctorId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        DoctorSchedule s = new DoctorSchedule();
        s.setDoctor(doctor);
        s.setDayOfWeek(request.getDayOfWeek());
        s.setStartTime(request.getStartTime());
        s.setEndTime(request.getEndTime());
        s.setSlotDurationMinutes(request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 10);
        s.setMaxPatients(request.getMaxPatients());
        s = scheduleRepository.save(s);
        return toResponse(s);
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDto> getByDoctorId(Long doctorId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        doctorRepository.findByIdAndHospitalId(doctorId, hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        return scheduleRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteByDoctorId(Long doctorId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        doctorRepository.findByIdAndHospitalId(doctorId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        scheduleRepository.deleteByDoctorId(doctorId);
    }

    private DoctorScheduleResponseDto toResponse(DoctorSchedule s) {
        DoctorScheduleResponseDto dto = new DoctorScheduleResponseDto();
        dto.setId(s.getId());
        dto.setDoctorId(s.getDoctor().getId());
        dto.setDoctorName(s.getDoctor().getFullName());
        dto.setDayOfWeek(s.getDayOfWeek());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setSlotDurationMinutes(s.getSlotDurationMinutes());
        dto.setMaxPatients(s.getMaxPatients());
        return dto;
    }
}
