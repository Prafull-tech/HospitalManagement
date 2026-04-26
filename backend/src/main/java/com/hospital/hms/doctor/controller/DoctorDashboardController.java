package com.hospital.hms.doctor.controller;

import com.hospital.hms.doctor.dto.DoctorDashboardDto;
import com.hospital.hms.doctor.service.DoctorDashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/doctor/dashboard")
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
public class DoctorDashboardController {

    private final DoctorDashboardService doctorDashboardService;

    public DoctorDashboardController(DoctorDashboardService doctorDashboardService) {
        this.doctorDashboardService = doctorDashboardService;
    }

    @GetMapping
    public ResponseEntity<DoctorDashboardDto> getDashboard(
        @RequestParam Long doctorId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorDashboardService.getDashboard(doctorId, date));
    }
}