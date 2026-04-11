package com.hospital.hms.enquiry.controller;

import com.hospital.hms.enquiry.dto.EnquiryAssignRequestDto;
import com.hospital.hms.enquiry.dto.EnquiryDashboardDto;
import com.hospital.hms.enquiry.dto.EnquiryNoteRequestDto;
import com.hospital.hms.enquiry.dto.EnquiryRequestDto;
import com.hospital.hms.enquiry.dto.EnquiryResponseDto;
import com.hospital.hms.enquiry.dto.EnquiryStatusUpdateRequestDto;
import com.hospital.hms.enquiry.entity.EnquiryCategory;
import com.hospital.hms.enquiry.entity.EnquiryPriority;
import com.hospital.hms.enquiry.entity.EnquiryStatus;
import com.hospital.hms.enquiry.service.EnquiryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/enquiries")
public class EnquiryController {

    private final EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @PostMapping
    public ResponseEntity<EnquiryResponseDto> create(@Valid @RequestBody EnquiryRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enquiryService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnquiryResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(enquiryService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EnquiryResponseDto>> search(
            @RequestParam(required = false) EnquiryStatus status,
            @RequestParam(required = false) EnquiryCategory category,
            @RequestParam(required = false) EnquiryPriority priority,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String assignedToUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) String patientUhid,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(enquiryService.search(
                status,
                category,
                priority,
                departmentId,
                assignedToUser,
                createdFrom,
                createdTo,
                patientUhid,
                query,
                page,
                size
        ));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<EnquiryDashboardDto> getDashboard() {
        return ResponseEntity.ok(enquiryService.getDashboard());
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<EnquiryResponseDto> assign(@PathVariable Long id,
                                                     @Valid @RequestBody EnquiryAssignRequestDto request) {
        return ResponseEntity.ok(enquiryService.assign(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EnquiryResponseDto> updateStatus(@PathVariable Long id,
                                                           @Valid @RequestBody EnquiryStatusUpdateRequestDto request) {
        return ResponseEntity.ok(enquiryService.updateStatus(id, request));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<EnquiryResponseDto> addNote(@PathVariable Long id,
                                                      @Valid @RequestBody EnquiryNoteRequestDto request) {
        return ResponseEntity.ok(enquiryService.addNote(id, request));
    }
}
