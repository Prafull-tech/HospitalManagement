package com.hospital.hms.system.controller;

import com.hospital.hms.system.dto.CompanyProfileDto;
import com.hospital.hms.system.service.CompanyProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/company-profile")
public class PublicCompanyProfileController {

    private final CompanyProfileService companyProfileService;

    public PublicCompanyProfileController(CompanyProfileService companyProfileService) {
        this.companyProfileService = companyProfileService;
    }

    @GetMapping
    public ResponseEntity<CompanyProfileDto> getProfile() {
        return ResponseEntity.ok(companyProfileService.getProfile());
    }
}