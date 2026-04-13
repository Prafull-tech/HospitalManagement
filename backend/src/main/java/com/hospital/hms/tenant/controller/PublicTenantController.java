package com.hospital.hms.tenant.controller;

import com.hospital.hms.tenant.dto.TenantContextResponseDto;
import com.hospital.hms.tenant.service.TenantResolutionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicTenantController {

    private final TenantResolutionService tenantResolutionService;

    public PublicTenantController(TenantResolutionService tenantResolutionService) {
        this.tenantResolutionService = tenantResolutionService;
    }

    @GetMapping("/tenant-context")
    public ResponseEntity<TenantContextResponseDto> getTenantContext(HttpServletRequest request) {
        return ResponseEntity.ok(tenantResolutionService.buildTenantContext(request));
    }
}