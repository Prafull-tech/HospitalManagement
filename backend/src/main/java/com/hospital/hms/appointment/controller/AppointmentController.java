package com.hospital.hms.appointment.controller;

import com.hospital.hms.appointment.dto.*;
import com.hospital.hms.appointment.entity.AppointmentStatus;
import com.hospital.hms.appointment.service.AppointmentService;
import com.hospital.hms.opd.dto.OPDVisitResponseDto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<AppointmentDashboardDto> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getDashboard(date));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> create(@Valid @RequestBody AppointmentRequestDto request,
                                                         Authentication auth) {
        String user = auth != null ? auth.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request, user));
    }

    @PostMapping("/walkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> createWalkIn(@Valid @RequestBody WalkInAppointmentRequestDto request,
                                                              Authentication auth) {
        String user = auth != null ? auth.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createWalkIn(request, user));
    }

    @PostMapping("/online")
    public ResponseEntity<AppointmentResponseDto> createOnline(@Valid @RequestBody OnlineAppointmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createOnline(request));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> reschedule(@PathVariable Long id,
                                                             @Valid @RequestBody RescheduleRequestDto request,
                                                             Authentication auth) {
        String user = auth != null ? auth.getName() : "system";
        return ResponseEntity.ok(appointmentService.reschedule(id, request, user));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable Long id,
                                                         @RequestBody(required = false) CancelRequestDto request,
                                                         Authentication auth) {
        String user = auth != null ? auth.getName() : "system";
        return ResponseEntity.ok(appointmentService.cancel(id, request != null ? request : new CancelRequestDto(), user));
    }

    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> markNoShow(@PathVariable Long id, Authentication auth) {
        String user = auth != null ? auth.getName() : "system";
        return ResponseEntity.ok(appointmentService.markNoShow(id, user));
    }

    @PostMapping("/{id}/convert-to-opd")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST')")
    public ResponseEntity<OPDVisitResponseDto> convertToOpd(@PathVariable Long id, Authentication auth) {
        String user = auth != null ? auth.getName() : "system";
        return ResponseEntity.ok(appointmentService.convertToOpdVisit(id, user));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> confirmOnline(@PathVariable Long id, Authentication auth) {
        String user = auth != null ? auth.getName() : "system";
        return ResponseEntity.ok(appointmentService.confirmOnline(id, user));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<org.springframework.data.domain.Page<AppointmentResponseDto>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) String patientUhid,
            @RequestParam(required = false) String patientName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(appointmentService.search(date, doctorId, status, patientUhid, patientName, page, size));
    }

    @GetMapping("/queue/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponseDto>> getQueue(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getQueue(doctorId, date));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<AppointmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }
}
