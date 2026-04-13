package com.hospital.hms.tenant.controller;

import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.tenant.service.TenantResolutionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
public class PublicDomainVerificationController {

    private final TenantResolutionService tenantResolutionService;

    public PublicDomainVerificationController(TenantResolutionService tenantResolutionService) {
        this.tenantResolutionService = tenantResolutionService;
    }

    @GetMapping(value = "/hms-domain-verification", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getVerificationToken(HttpServletRequest request) {
        String normalizedHost = tenantResolutionService.normalizeRequestHost(request);
        return tenantResolutionService.resolveTenantHospital(request)
                .filter(hospital -> isMatchingCustomDomain(normalizedHost, hospital))
                .filter(hospital -> hospital.getDomainVerificationToken() != null && !hospital.getDomainVerificationToken().isBlank())
                .map(hospital -> ResponseEntity.ok(hospital.getDomainVerificationToken()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean isMatchingCustomDomain(String normalizedHost, Hospital hospital) {
        return normalizedHost != null
                && hospital.getCustomDomain() != null
                && normalizedHost.equalsIgnoreCase(hospital.getCustomDomain());
    }
}