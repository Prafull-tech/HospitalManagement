package com.hospital.hms.tenant.service;

import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import com.hospital.hms.tenant.dto.TenantContextResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TenantResolutionService {

    private final HospitalRepository hospitalRepository;
    private final String baseDomain;
    private final List<String> tenantBaseDomains;
    private final String platformSubdomain;

    public TenantResolutionService(HospitalRepository hospitalRepository,
                                   @Value("${hms.tenant.base-domain:hms.com}") String baseDomain,
                                   @Value("${hms.tenant.alias-base-domains:}") String aliasBaseDomains,
                                   @Value("${hms.tenant.platform-subdomain:admin}") String platformSubdomain) {
        this.hospitalRepository = hospitalRepository;
        this.baseDomain = baseDomain.toLowerCase();
        this.tenantBaseDomains = buildTenantBaseDomains(this.baseDomain, aliasBaseDomains);
        this.platformSubdomain = platformSubdomain.toLowerCase();
    }

    public Optional<Hospital> resolveTenantHospital(HttpServletRequest request) {
        return resolveTenantHospital(getRequestHost(request));
    }

    public Optional<Hospital> resolveTenantHospital(String host) {
        String normalizedHost = normalizeHost(host);
        if (normalizedHost == null || isPlatformHost(normalizedHost)) {
            return Optional.empty();
        }
        Optional<Hospital> customDomainHospital = hospitalRepository.findByCustomDomainAndDeletedFalse(normalizedHost);
        if (customDomainHospital.isPresent()) {
            return customDomainHospital;
        }
        String matchingBaseDomain = findMatchingBaseDomain(normalizedHost);
        if (matchingBaseDomain == null) {
            return Optional.empty();
        }
        String suffix = "." + matchingBaseDomain;
        if (!normalizedHost.endsWith(suffix)) {
            return Optional.empty();
        }
        String subdomain = normalizedHost.substring(0, normalizedHost.length() - suffix.length());
        if (subdomain.isBlank() || subdomain.contains(".")) {
            return Optional.empty();
        }
        return hospitalRepository.findBySubdomainAndDeletedFalse(subdomain);
    }

    public boolean isPlatformHost(HttpServletRequest request) {
        return isPlatformHost(getRequestHost(request));
    }

    public boolean isPlatformHost(String host) {
        String normalizedHost = normalizeHost(host);
        if (normalizedHost == null) {
            return true;
        }
        if (isLocalHost(normalizedHost)) {
            return true;
        }
        return tenantBaseDomains.stream().anyMatch(baseDomain ->
            normalizedHost.equals(baseDomain) || normalizedHost.equals(platformSubdomain + "." + baseDomain)
        );
    }

    public TenantContextResponseDto buildTenantContext(HttpServletRequest request) {
        String host = getRequestHost(request);
        TenantContextResponseDto dto = new TenantContextResponseDto();
        dto.setHost(normalizeHost(host));
        dto.setPlatformHost(isPlatformHost(host));
        resolveTenantHospital(host).ifPresent(hospital -> {
            dto.setTenantResolved(true);
            dto.setHospitalId(hospital.getId());
            dto.setHospitalCode(hospital.getHospitalCode());
            dto.setHospitalName(hospital.getHospitalName());
            dto.setTenantSlug(hospital.getSubdomain());
            dto.setCustomDomain(hospital.getCustomDomain());
            dto.setResolvedBy(isResolvedByCustomDomain(dto.getHost(), hospital) ? "CUSTOM_DOMAIN" : "SUBDOMAIN");
            dto.setDomainVerificationStatus(hospital.getDomainVerificationStatus());
            dto.setCertificateStatus(hospital.getCertificateStatus());
            dto.setCertificateExpiresAt(hospital.getCertificateExpiresAt() != null ? hospital.getCertificateExpiresAt().toString() : null);
            dto.setLogoUrl(hospital.getLogoUrl());
            dto.setContactEmail(hospital.getContactEmail());
            dto.setContactPhone(hospital.getContactPhone());
            dto.setActive(hospital.getIsActive());
        });
        return dto;
    }

    public String normalizeRequestHost(HttpServletRequest request) {
        return normalizeHost(getRequestHost(request));
    }

    public String getRequestHost(HttpServletRequest request) {
        String tenantHost = request.getHeader("X-HMS-Tenant-Host");
        if (tenantHost != null && !tenantHost.isBlank()) {
            return tenantHost;
        }
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            return forwardedHost;
        }
        return request.getServerName();
    }

    private String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        String normalized = host.trim().toLowerCase();
        int commaIndex = normalized.indexOf(',');
        if (commaIndex >= 0) {
            normalized = normalized.substring(0, commaIndex).trim();
        }
        int portIndex = normalized.indexOf(':');
        if (portIndex >= 0) {
            normalized = normalized.substring(0, portIndex);
        }
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private boolean isResolvedByCustomDomain(String normalizedHost, Hospital hospital) {
        return normalizedHost != null
                && hospital.getCustomDomain() != null
                && normalizedHost.equalsIgnoreCase(hospital.getCustomDomain());
    }

    private List<String> buildTenantBaseDomains(String primaryBaseDomain, String aliasBaseDomains) {
        return Arrays.stream((primaryBaseDomain + "," + aliasBaseDomains).split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private String findMatchingBaseDomain(String normalizedHost) {
        return tenantBaseDomains.stream()
                .filter(baseDomain -> normalizedHost.endsWith("." + baseDomain))
                .findFirst()
                .orElse(null);
    }

    private boolean isLocalHost(String host) {
        return "localhost".equals(host)
                || "127.0.0.1".equals(host)
                || "0.0.0.0".equals(host)
                || "backend".equals(host)
                || "frontend".equals(host)
                || host.endsWith(".localhost");
    }
}