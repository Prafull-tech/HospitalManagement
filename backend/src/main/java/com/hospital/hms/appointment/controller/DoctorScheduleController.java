package com.hospital.hms.appointment.controller;

import com.hospital.hms.appointment.dto.DoctorScheduleRequestDto;
import com.hospital.hms.appointment.dto.DoctorScheduleResponseDto;
import com.hospital.hms.appointment.service.DoctorScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors/schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    public DoctorScheduleController(DoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DoctorScheduleResponseDto> create(@Valid @RequestBody DoctorScheduleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.create(request));
    }

    @GetMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<DoctorScheduleResponseDto>> getByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getByDoctorId(doctorId));
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteByDoctorId(@PathVariable Long doctorId) {
        scheduleService.deleteByDoctorId(doctorId);
        return ResponseEntity.noContent().build();
    }
}
