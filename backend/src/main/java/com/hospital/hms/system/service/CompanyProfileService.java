package com.hospital.hms.system.service;

import com.hospital.hms.system.dto.CompanyProfileDto;
import com.hospital.hms.system.entity.CompanyProfile;
import com.hospital.hms.system.repository.CompanyProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyProfileService {

    private static final long SINGLETON_ID = 1L;

    private final CompanyProfileRepository repository;

    public CompanyProfileService(CompanyProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CompanyProfileDto getProfile() {
        return toDto(getOrCreate());
    }

    public CompanyProfileDto updateProfile(CompanyProfileDto request) {
        CompanyProfile profile = getOrCreate();
        profile.setCompanyName(request.getCompanyName().trim());
        profile.setBrandName(request.getBrandName().trim());
        profile.setLogoText(trimToNull(request.getLogoText()));
        profile.setLogoUrl(trimToNull(request.getLogoUrl()));
        profile.setSupportEmail(trimToNull(request.getSupportEmail()));
        profile.setSupportPhone(trimToNull(request.getSupportPhone()));
        profile.setAddressText(trimToNull(request.getAddressText()));
        return toDto(repository.save(profile));
    }

    private CompanyProfile getOrCreate() {
        return repository.findById(SINGLETON_ID).orElseGet(() -> {
            CompanyProfile profile = new CompanyProfile();
            profile.setId(SINGLETON_ID);
            profile.setCompanyName("HMS Hospital Management System");
            profile.setBrandName("HMS");
            profile.setLogoText("HMS");
            profile.setSupportEmail("support@hms-hospital.com");
            profile.setSupportPhone("+91 22 1234 5678");
            profile.setAddressText("HMS Office, Health Tech Park, Mumbai, Maharashtra, India");
            return repository.save(profile);
        });
    }

    private CompanyProfileDto toDto(CompanyProfile profile) {
        CompanyProfileDto dto = new CompanyProfileDto();
        dto.setId(profile.getId());
        dto.setCompanyName(profile.getCompanyName());
        dto.setBrandName(profile.getBrandName());
        dto.setLogoText(profile.getLogoText());
        dto.setLogoUrl(profile.getLogoUrl());
        dto.setSupportEmail(profile.getSupportEmail());
        dto.setSupportPhone(profile.getSupportPhone());
        dto.setAddressText(profile.getAddressText());
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}