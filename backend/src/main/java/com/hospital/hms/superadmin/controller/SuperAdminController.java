package com.hospital.hms.superadmin.controller;

import com.hospital.hms.hospital.dto.CustomDomainCertificateRequestDto;
import com.hospital.hms.hospital.dto.HospitalRequestDto;
import com.hospital.hms.hospital.dto.HospitalResponseDto;
import com.hospital.hms.hospital.service.HospitalService;
import com.hospital.hms.superadmin.dto.*;
import com.hospital.hms.superadmin.service.SuperAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final HospitalService hospitalService;

    public SuperAdminController(SuperAdminService superAdminService, HospitalService hospitalService) {
        this.superAdminService = superAdminService;
        this.hospitalService = hospitalService;
    }

    // ── Dashboard ──

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDto> getDashboard() {
        return ResponseEntity.ok(superAdminService.getDashboardSummary());
    }

    // ── Hospitals (delegates to existing HospitalService) ──

    @GetMapping("/hospitals")
    public ResponseEntity<List<HospitalResponseDto>> listHospitals(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(hospitalService.list(activeOnly));
    }

    @PostMapping("/hospitals")
    public ResponseEntity<HospitalResponseDto> createHospital(@Valid @RequestBody HospitalRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hospitalService.create(request));
    }

    @GetMapping("/hospitals/{id}")
    public ResponseEntity<HospitalResponseDto> getHospital(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }

    @PutMapping("/hospitals/{id}")
    public ResponseEntity<HospitalResponseDto> updateHospital(@PathVariable Long id,
                                                               @Valid @RequestBody HospitalRequestDto request) {
        return ResponseEntity.ok(hospitalService.update(id, request));
    }

    @PatchMapping("/hospitals/{id}/status")
    public ResponseEntity<Void> toggleHospitalStatus(@PathVariable Long id,
                                                      @RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        hospitalService.updateStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/hospitals/{id}/custom-domain/regenerate-token")
    public ResponseEntity<HospitalResponseDto> regenerateCustomDomainToken(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.regenerateCustomDomainVerificationToken(id));
    }

    @PostMapping("/hospitals/{id}/custom-domain/verify")
    public ResponseEntity<HospitalResponseDto> verifyCustomDomain(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.verifyCustomDomain(id));
    }

    @PostMapping("/hospitals/{id}/custom-domain/certificate/request")
    public ResponseEntity<HospitalResponseDto> requestCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.requestCertificate(id));
    }

    @PatchMapping("/hospitals/{id}/custom-domain/certificate")
    public ResponseEntity<HospitalResponseDto> updateCertificate(
            @PathVariable Long id,
            @Valid @RequestBody CustomDomainCertificateRequestDto request) {
        return ResponseEntity.ok(hospitalService.updateCertificate(id, request));
    }

    // ── Hospital Users ──

    @GetMapping("/hospitals/{id}/users")
    public ResponseEntity<List<HospitalUserDto>> getHospitalUsers(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.getHospitalUsers(id));
    }

    @GetMapping("/hospitals/{id}/modules")
    public ResponseEntity<HospitalModuleConfigResponseDto> getHospitalModules(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.getHospitalModuleConfiguration(id));
    }

    @PutMapping("/hospitals/{id}/modules")
    public ResponseEntity<HospitalModuleConfigResponseDto> updateHospitalModules(
            @PathVariable Long id,
            @Valid @RequestBody HospitalModuleUpdateRequestDto request) {
        return ResponseEntity.ok(superAdminService.updateHospitalModules(id, request));
    }

    @PostMapping("/hospitals/{id}/users")
    public ResponseEntity<HospitalUserDto> createHospitalUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateHospitalUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(superAdminService.createHospitalUser(id, request));
    }

    @PatchMapping("/hospitals/{hospitalId}/users/{userId}/status")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable Long hospitalId,
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        if (active == null) return ResponseEntity.badRequest().build();
        superAdminService.toggleUserStatus(hospitalId, userId, active);
        return ResponseEntity.noContent().build();
    }

    // ── Subscription Plans ──

    @GetMapping("/subscriptions/plans")
    public ResponseEntity<List<SubscriptionPlanResponseDto>> listPlans(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(superAdminService.listPlans(activeOnly));
    }

    @PostMapping("/subscriptions/plans")
    public ResponseEntity<SubscriptionPlanResponseDto> createPlan(
            @Valid @RequestBody SubscriptionPlanRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.createPlan(request));
    }

    @PutMapping("/subscriptions/plans/{id}")
    public ResponseEntity<SubscriptionPlanResponseDto> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequestDto request) {
        return ResponseEntity.ok(superAdminService.updatePlan(id, request));
    }

    // ── Hospital Subscriptions ──

    @GetMapping("/subscriptions")
    public ResponseEntity<List<HospitalSubscriptionResponseDto>> listSubscriptions() {
        return ResponseEntity.ok(superAdminService.listSubscriptions());
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<HospitalSubscriptionResponseDto> createSubscription(
            @Valid @RequestBody HospitalSubscriptionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(superAdminService.createSubscription(request));
    }

    @PatchMapping("/subscriptions/{id}/status")
    public ResponseEntity<HospitalSubscriptionResponseDto> updateSubscriptionStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(superAdminService.updateSubscriptionStatus(id, status));
    }
}
