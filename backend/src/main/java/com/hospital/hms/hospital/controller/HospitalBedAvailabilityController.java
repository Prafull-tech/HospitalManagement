package com.hospital.hms.hospital.controller;

import com.hospital.hms.hospital.config.BedAvailabilityRoles;
import com.hospital.hms.hospital.dto.BedAvailabilityAuditLogResponseDto;
import com.hospital.hms.hospital.dto.BedAvailabilityRequestDto;
import com.hospital.hms.hospital.dto.BedAvailabilityResponseDto;
import com.hospital.hms.hospital.service.BedAvailabilityCallerRoleResolver;
import com.hospital.hms.hospital.service.BedAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Hospital-wise Bed Availability.
 * <p>
 * Base path: /api (context) + /hospitals/{hospitalId}/bed-availability (mapping).
 * Full paths:
 * <ul>
 *   <li>POST   /api/hospitals/{hospitalId}/bed-availability</li>
 *   <li>GET    /api/hospitals/{hospitalId}/bed-availability</li>
 *   <li>GET    /api/hospitals/{hospitalId}/bed-availability/{id}</li>
 *   <li>GET    /api/hospitals/{hospitalId}/bed-availability/{id}/audit (read-only audit trail)</li>
 *   <li>PUT    /api/hospitals/{hospitalId}/bed-availability/{id}</li>
 *   <li>DELETE /api/hospitals/{hospitalId}/bed-availability/{id}</li>
 * </ul>
 * <p>
 * Role-based access (method-level security, no hardcoded roles in controller logic):
 * <ul>
 *   <li>ADMIN → POST, GET, PUT, DELETE</li>
 *   <li>IPD_MANAGER → GET, PUT</li>
 *   <li>DOCTOR → GET only</li>
 * </ul>
 * <p>
 * HTTP status codes: 201 Created (POST), 200 OK (GET, PUT), 204 No Content (DELETE).
 * Validation and business errors return 400, 404, 409 via global exception handler.
 * DTO-based request/response; DB-agnostic.
 * <p>
 * Sample request (POST / PUT):
 * <pre>
 * {
 *   "wardType": "GENERAL",
 *   "totalBeds": 20,
 *   "occupiedBeds": 12,
 *   "reservedBeds": 1,
 *   "underCleaningBeds": 2
 * }
 * </pre>
 * GET response format: Ward Type | Total Beds | Occupied | Vacant | Reserved | Under Cleaning.
 * Vacant is calculated dynamically. Sorted by ward type.
 * <p>
 * Full sample JSON (POST, PUT, GET, validation errors) for ward types General, Semi Private, Private, ICU, Emergency:
 * see {@code backend/docs/bed-availability-api-samples.md}.
 */
@RestController
@RequestMapping("/hospitals/{hospitalId}/bed-availability")
public class HospitalBedAvailabilityController {

    private final BedAvailabilityService bedAvailabilityService;
    private final BedAvailabilityCallerRoleResolver callerRoleResolver;

    public HospitalBedAvailabilityController(BedAvailabilityService bedAvailabilityService,
                                             BedAvailabilityCallerRoleResolver callerRoleResolver) {
        this.bedAvailabilityService = bedAvailabilityService;
        this.callerRoleResolver = callerRoleResolver;
    }

    /**
     * Create bed availability for the given hospital. ADMIN only.
     */
    @PostMapping
    @PreAuthorize(BedAvailabilityRoles.ADMIN_ONLY)
    public ResponseEntity<BedAvailabilityResponseDto> create(
            @PathVariable Long hospitalId,
            @Valid @RequestBody BedAvailabilityRequestDto request,
            Authentication authentication) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        BedAvailabilityResponseDto created = bedAvailabilityService.create(
                hospitalId, request, callerRoleResolver.resolve(authentication), updatedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * List all bed availability records for the given hospital. No auth required (permitAll).
     */
    @GetMapping
    public ResponseEntity<List<BedAvailabilityResponseDto>> list(@PathVariable Long hospitalId) {
        List<BedAvailabilityResponseDto> list = bedAvailabilityService.listByHospitalId(hospitalId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get a single bed availability by id within the hospital. No auth required (permitAll).
     */
    @GetMapping("/{id}")
    public ResponseEntity<BedAvailabilityResponseDto> getById(
            @PathVariable Long hospitalId,
            @PathVariable Long id) {
        BedAvailabilityResponseDto dto = bedAvailabilityService.getById(hospitalId, id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Read-only audit trail for a bed availability record. No auth required (permitAll).
     */
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<BedAvailabilityAuditLogResponseDto>> getAuditTrail(
            @PathVariable Long hospitalId,
            @PathVariable Long id) {
        List<BedAvailabilityAuditLogResponseDto> trail = bedAvailabilityService.getAuditTrail(hospitalId, id);
        return ResponseEntity.ok(trail);
    }

    /**
     * Update bed availability by id within the hospital. ADMIN, IPD_MANAGER.
     */
    @PutMapping("/{id}")
    @PreAuthorize(BedAvailabilityRoles.CAN_UPDATE)
    public ResponseEntity<BedAvailabilityResponseDto> update(
            @PathVariable Long hospitalId,
            @PathVariable Long id,
            @Valid @RequestBody BedAvailabilityRequestDto request,
            Authentication authentication) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        BedAvailabilityResponseDto updated = bedAvailabilityService.update(
                hospitalId, id, request, callerRoleResolver.resolve(authentication), updatedBy);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete bed availability by id within the hospital. ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(BedAvailabilityRoles.ADMIN_ONLY)
    public ResponseEntity<Void> delete(
            @PathVariable Long hospitalId,
            @PathVariable Long id,
            Authentication authentication) {
        bedAvailabilityService.delete(hospitalId, id, callerRoleResolver.resolve(authentication));
        return ResponseEntity.noContent().build();
    }
}
