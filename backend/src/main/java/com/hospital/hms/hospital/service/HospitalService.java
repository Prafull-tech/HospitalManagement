package com.hospital.hms.hospital.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.hospital.dto.CustomDomainCertificateRequestDto;
import com.hospital.hms.hospital.dto.HospitalRequestDto;
import com.hospital.hms.hospital.dto.HospitalResponseDto;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Hospital service. Full CRUD, multi-hospital ready. DB-agnostic.
 */
@Service
public class HospitalService {

    private static final String DOMAIN_STATUS_NOT_CONFIGURED = "NOT_CONFIGURED";
    private static final String DOMAIN_STATUS_PENDING = "PENDING";
    private static final String DOMAIN_STATUS_VERIFIED = "VERIFIED";
    private static final String DOMAIN_STATUS_FAILED = "FAILED";
    private static final String CERT_STATUS_NOT_REQUESTED = "NOT_REQUESTED";
    private static final String CERT_STATUS_REQUESTED = "REQUESTED";
    private static final String CERT_STATUS_ISSUED = "ISSUED";
    private static final String CERT_STATUS_EXPIRING_SOON = "EXPIRING_SOON";
    private static final String CERT_STATUS_EXPIRED = "EXPIRED";
    private static final String CERT_STATUS_FAILED = "FAILED";
    private static final Duration DOMAIN_VERIFY_TIMEOUT = Duration.ofSeconds(5);

    private final HospitalRepository hospitalRepository;
    private final HttpClient httpClient;
    private final String baseDomain;
    private final String platformSubdomain;

    public HospitalService(HospitalRepository hospitalRepository,
                           @Value("${hms.tenant.base-domain:hms.com}") String baseDomain,
                           @Value("${hms.tenant.platform-subdomain:admin}") String platformSubdomain) {
        this.hospitalRepository = hospitalRepository;
        this.baseDomain = baseDomain.toLowerCase(Locale.ROOT);
        this.platformSubdomain = platformSubdomain.toLowerCase(Locale.ROOT);
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(DOMAIN_VERIFY_TIMEOUT)
                .build();
    }

    @Transactional(readOnly = true)
    public List<HospitalResponseDto> list(Boolean activeOnly) {
        List<Hospital> list = Boolean.TRUE.equals(activeOnly)
                ? hospitalRepository.findByDeletedFalseAndIsActiveTrueOrderByHospitalNameAsc()
                : hospitalRepository.findByDeletedFalseOrderByHospitalNameAsc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HospitalResponseDto getById(Long id) {
        Hospital h = getEntityById(id);
        return toDto(h);
    }

    @Transactional(readOnly = true)
    public Hospital getEntityById(Long id) {
        Hospital h = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + id));
        if (Boolean.TRUE.equals(h.getDeleted())) {
            throw new ResourceNotFoundException("Hospital not found: " + id);
        }
        return h;
    }

    @Transactional
    public HospitalResponseDto create(HospitalRequestDto request) {
        String code = request.getHospitalCode() != null ? request.getHospitalCode().trim() : "";
        String subdomain = normalizeSubdomain(request.getSubdomain());
        String customDomain = normalizeCustomDomain(request.getCustomDomain());
        if (hospitalRepository.existsByHospitalCodeAndDeletedFalse(code)) {
            throw new IllegalArgumentException("Hospital code already exists. Please use a unique hospital code.");
        }
        if (subdomain != null && hospitalRepository.existsBySubdomainAndDeletedFalse(subdomain)) {
            throw new IllegalArgumentException("Hospital subdomain already exists. Please use a unique subdomain.");
        }
        if (customDomain != null && hospitalRepository.existsByCustomDomainAndDeletedFalse(customDomain)) {
            throw new IllegalArgumentException("Hospital custom domain already exists. Please use a unique custom domain.");
        }
        Hospital h = new Hospital();
        h.setHospitalCode(code);
        h.setHospitalName(request.getHospitalName() != null ? request.getHospitalName().trim() : "");
        applyFields(h, request, true);
        h.setIsActive(request.getActive() != null ? request.getActive() : true);
        h.setDeleted(false);
        h = hospitalRepository.save(h);
        return toDto(h);
    }

    @Transactional
    public HospitalResponseDto update(Long id, HospitalRequestDto request) {
        Hospital h = getEntityById(id);
        String code = request.getHospitalCode() != null ? request.getHospitalCode().trim() : "";
        String subdomain = normalizeSubdomain(request.getSubdomain());
        String customDomain = normalizeCustomDomain(request.getCustomDomain());
        if (hospitalRepository.existsByHospitalCodeAndDeletedFalseAndIdNot(code, id)) {
            throw new IllegalArgumentException("Hospital code already exists. Please use a unique hospital code.");
        }
        if (subdomain != null && hospitalRepository.existsBySubdomainAndDeletedFalseAndIdNot(subdomain, id)) {
            throw new IllegalArgumentException("Hospital subdomain already exists. Please use a unique subdomain.");
        }
        if (customDomain != null && hospitalRepository.existsByCustomDomainAndDeletedFalseAndIdNot(customDomain, id)) {
            throw new IllegalArgumentException("Hospital custom domain already exists. Please use a unique custom domain.");
        }
        h.setHospitalCode(code);
        applyFields(h, request, false);
        h.setIsActive(request.getActive() != null ? request.getActive() : true);
        h = hospitalRepository.save(h);
        return toDto(h);
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {
        Hospital hospital = getEntityById(id);
        hospital.setIsActive(active);
        hospitalRepository.save(hospital);
    }

    @Transactional
    public HospitalResponseDto regenerateCustomDomainVerificationToken(Long id) {
        Hospital hospital = getEntityById(id);
        if (normalizeCustomDomain(hospital.getCustomDomain()) == null) {
            throw new IllegalArgumentException("Custom domain is not configured for this hospital");
        }
        initializeCustomDomainState(hospital, true);
        return toDto(hospitalRepository.save(hospital));
    }

    @Transactional
    public HospitalResponseDto verifyCustomDomain(Long id) {
        Hospital hospital = getEntityById(id);
        String customDomain = normalizeCustomDomain(hospital.getCustomDomain());
        if (customDomain == null) {
            throw new IllegalArgumentException("Custom domain is not configured for this hospital");
        }
        String token = hospital.getDomainVerificationToken();
        if (token == null || token.isBlank()) {
            initializeCustomDomainState(hospital, true);
            token = hospital.getDomainVerificationToken();
        }

        String challengeBody;
        try {
            challengeBody = fetchVerificationChallenge(customDomain);
        } catch (RuntimeException ex) {
            hospital.setDomainVerificationStatus(DOMAIN_STATUS_FAILED);
            hospital.setLastDomainVerificationError(ex.getMessage());
            hospital.setDomainVerifiedAt(null);
            hospitalRepository.save(hospital);
            throw ex;
        }
        if (!token.equals(challengeBody)) {
            hospital.setDomainVerificationStatus(DOMAIN_STATUS_FAILED);
            hospital.setLastDomainVerificationError("Verification challenge response did not match the expected token");
            hospital.setDomainVerifiedAt(null);
            hospitalRepository.save(hospital);
            throw new IllegalArgumentException("Custom domain could not be verified. Ensure the domain points to HMS and exposes the verification challenge.");
        }

        hospital.setDomainVerificationStatus(DOMAIN_STATUS_VERIFIED);
        hospital.setDomainVerifiedAt(Instant.now());
        hospital.setLastDomainVerificationError(null);
        hospitalRepository.save(hospital);
        return toDto(hospital);
    }

    @Transactional
    public HospitalResponseDto requestCertificate(Long id) {
        Hospital hospital = getEntityById(id);
        if (normalizeCustomDomain(hospital.getCustomDomain()) == null) {
            throw new IllegalArgumentException("Custom domain is not configured for this hospital");
        }
        if (!DOMAIN_STATUS_VERIFIED.equals(hospital.getDomainVerificationStatus())) {
            throw new IllegalArgumentException("Verify the custom domain before requesting a certificate");
        }
        hospital.setCertificateStatus(CERT_STATUS_REQUESTED);
        hospital.setCertificateRequestedAt(Instant.now());
        hospital.setLastCertificateError(null);
        hospitalRepository.save(hospital);
        return toDto(hospital);
    }

    @Transactional
    public HospitalResponseDto updateCertificate(Long id, CustomDomainCertificateRequestDto request) {
        Hospital hospital = getEntityById(id);
        if (normalizeCustomDomain(hospital.getCustomDomain()) == null) {
            throw new IllegalArgumentException("Custom domain is not configured for this hospital");
        }
        String status = normalizeCertificateStatus(request.getStatus());
        hospital.setCertificateStatus(status);
        hospital.setCertificateIssuedAt(parseInstant(request.getIssuedAt()));
        hospital.setCertificateExpiresAt(parseInstant(request.getExpiresAt()));
        hospital.setLastCertificateError(normalize(request.getErrorMessage()));
        if (CERT_STATUS_REQUESTED.equals(status) && hospital.getCertificateRequestedAt() == null) {
            hospital.setCertificateRequestedAt(Instant.now());
        }
        if (!CERT_STATUS_REQUESTED.equals(status) && !CERT_STATUS_FAILED.equals(status) && hospital.getCertificateIssuedAt() == null) {
            hospital.setCertificateIssuedAt(Instant.now());
        }
        hospitalRepository.save(hospital);
        return toDto(hospital);
    }

    @Transactional
    public void delete(Long id) {
        Hospital h = getEntityById(id);
        h.setDeleted(true);
        h.setIsActive(false);
        hospitalRepository.save(h);
    }

    private HospitalResponseDto toDto(Hospital h) {
        HospitalResponseDto dto = new HospitalResponseDto();
        dto.setId(h.getId());
        dto.setHospitalCode(h.getHospitalCode());
        dto.setHospitalName(h.getHospitalName());
        dto.setLocation(h.getLocation());
        dto.setSubdomain(h.getSubdomain());
        dto.setCustomDomain(h.getCustomDomain());
        dto.setDomainVerificationToken(h.getDomainVerificationToken());
        dto.setDomainVerificationStatus(h.getDomainVerificationStatus());
        dto.setDomainVerifiedAt(h.getDomainVerifiedAt() != null ? h.getDomainVerifiedAt().toString() : null);
        dto.setCertificateStatus(h.getCertificateStatus());
        dto.setCertificateRequestedAt(h.getCertificateRequestedAt() != null ? h.getCertificateRequestedAt().toString() : null);
        dto.setCertificateIssuedAt(h.getCertificateIssuedAt() != null ? h.getCertificateIssuedAt().toString() : null);
        dto.setCertificateExpiresAt(h.getCertificateExpiresAt() != null ? h.getCertificateExpiresAt().toString() : null);
        dto.setLastDomainVerificationError(h.getLastDomainVerificationError());
        dto.setLastCertificateError(h.getLastCertificateError());
        dto.setLogoUrl(h.getLogoUrl());
        dto.setWebsiteUrl(h.getWebsiteUrl());
        dto.setFacebookUrl(h.getFacebookUrl());
        dto.setTwitterUrl(h.getTwitterUrl());
        dto.setInstagramUrl(h.getInstagramUrl());
        dto.setLinkedinUrl(h.getLinkedinUrl());
        dto.setContactEmail(h.getContactEmail());
        dto.setBillingEmail(h.getBillingEmail());
        dto.setContactPhone(h.getContactPhone());
        dto.setOnboardingStatus(h.getOnboardingStatus());
        dto.setActive(h.getIsActive());
        return dto;
    }

    private void applyFields(Hospital hospital, HospitalRequestDto request, boolean creating) {
        String previousCustomDomain = normalizeStoredCustomDomain(hospital.getCustomDomain());
        String incomingCustomDomain = normalizeCustomDomain(request.getCustomDomain());
        hospital.setHospitalName(normalize(request.getHospitalName()));
        hospital.setLocation(normalize(request.getLocation()));
        hospital.setSubdomain(normalizeSubdomain(request.getSubdomain()));
        hospital.setCustomDomain(incomingCustomDomain);
        hospital.setLogoUrl(normalize(request.getLogoUrl()));
        hospital.setWebsiteUrl(normalize(request.getWebsiteUrl()));
        hospital.setFacebookUrl(normalize(request.getFacebookUrl()));
        hospital.setTwitterUrl(normalize(request.getTwitterUrl()));
        hospital.setInstagramUrl(normalize(request.getInstagramUrl()));
        hospital.setLinkedinUrl(normalize(request.getLinkedinUrl()));
        hospital.setContactEmail(normalize(request.getContactEmail()));
        hospital.setBillingEmail(normalize(request.getBillingEmail()));
        hospital.setContactPhone(normalize(request.getContactPhone()));
        boolean customDomainChanged = creating
                ? incomingCustomDomain != null
                : (previousCustomDomain == null ? incomingCustomDomain != null : !previousCustomDomain.equals(incomingCustomDomain));
        if (incomingCustomDomain == null) {
            clearCustomDomainState(hospital);
        } else if (customDomainChanged || hospital.getDomainVerificationToken() == null || hospital.getDomainVerificationToken().isBlank()) {
            initializeCustomDomainState(hospital, true);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeSubdomain(String value) {
        String normalized = normalize(value);
        return normalized != null ? normalized.toLowerCase() : null;
    }

    private String normalizeCustomDomain(String value) {
        String lowered = normalizeDomainHost(value);
        if (lowered == null) {
            return null;
        }
        validateCustomDomainHost(lowered);
        return lowered;
    }

    private String normalizeStoredCustomDomain(String value) {
        return normalizeDomainHost(value);
    }

    private String normalizeDomainHost(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String lowered = normalized.toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://", "")
                .replaceAll("/$", "");
        int slashIndex = lowered.indexOf('/');
        if (slashIndex >= 0) {
            lowered = lowered.substring(0, slashIndex);
        }
        if (!lowered.matches("^[a-z0-9.-]+$")) {
            throw new IllegalArgumentException("Custom domain must be a valid host name without protocol or path");
        }
        return lowered;
    }

    private void validateCustomDomainHost(String host) {
        if (host.equals(baseDomain) || host.endsWith("." + baseDomain)) {
            throw new IllegalArgumentException("Domains under " + baseDomain
                    + " are platform-managed HMS subdomains. Use the subdomain field instead of enterprise custom domain.");
        }
        if (host.equals(platformSubdomain)) {
            throw new IllegalArgumentException("Custom domain cannot use the reserved platform subdomain: " + platformSubdomain);
        }
    }

    private void initializeCustomDomainState(Hospital hospital, boolean resetCertificate) {
        hospital.setDomainVerificationToken(generateVerificationToken());
        hospital.setDomainVerificationStatus(DOMAIN_STATUS_PENDING);
        hospital.setDomainVerifiedAt(null);
        hospital.setLastDomainVerificationError(null);
        if (resetCertificate) {
            hospital.setCertificateStatus(CERT_STATUS_NOT_REQUESTED);
            hospital.setCertificateRequestedAt(null);
            hospital.setCertificateIssuedAt(null);
            hospital.setCertificateExpiresAt(null);
            hospital.setLastCertificateError(null);
        }
    }

    private void clearCustomDomainState(Hospital hospital) {
        hospital.setCustomDomain(null);
        hospital.setDomainVerificationToken(null);
        hospital.setDomainVerificationStatus(DOMAIN_STATUS_NOT_CONFIGURED);
        hospital.setDomainVerifiedAt(null);
        hospital.setLastDomainVerificationError(null);
        hospital.setCertificateStatus(CERT_STATUS_NOT_REQUESTED);
        hospital.setCertificateRequestedAt(null);
        hospital.setCertificateIssuedAt(null);
        hospital.setCertificateExpiresAt(null);
        hospital.setLastCertificateError(null);
    }

    private String generateVerificationToken() {
        return "hms-verify-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String fetchVerificationChallenge(String customDomain) {
        IOException lastIoException = null;
        InterruptedException lastInterruptedException = null;
        Integer lastStatusCode = null;
        List<String> attemptErrors = new ArrayList<>();
        for (String scheme : List.of("https", "http")) {
            String verificationUrl = scheme + "://" + customDomain + "/.well-known/hms-domain-verification";
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(verificationUrl))
                    .timeout(DOMAIN_VERIFY_TIMEOUT)
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body() != null ? response.body().trim() : "";
                }
                lastStatusCode = response.statusCode();
                attemptErrors.add(verificationUrl + " -> HTTP " + response.statusCode());
            } catch (IOException ex) {
                lastIoException = ex;
                attemptErrors.add(verificationUrl + " -> " + describeNetworkError(ex));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                lastInterruptedException = ex;
                break;
            }
        }
        if (lastInterruptedException != null) {
            throw new IllegalStateException("Domain verification was interrupted", lastInterruptedException);
        }
        if (lastIoException != null) {
            throw new IllegalArgumentException("Unable to reach the custom domain verification endpoint. "
                    + String.join("; ", attemptErrors)
                    + ". Ensure the custom domain resolves to HMS and serves /.well-known/hms-domain-verification.", lastIoException);
        }
        if (lastStatusCode != null) {
            throw new IllegalArgumentException("Custom domain verification endpoint returned HTTP " + lastStatusCode
                    + ". Ensure /.well-known/hms-domain-verification on the custom domain routes to /api/.well-known/hms-domain-verification.");
        }
        throw new IllegalArgumentException("Unable to verify the custom domain challenge response");
    }

    private String describeNetworkError(IOException ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        String message = rootCause.getMessage();
        if (message == null || message.isBlank()) {
            message = ex.getMessage();
        }
        if (message == null || message.isBlank()) {
            return rootCause.getClass().getSimpleName();
        }
        return rootCause.getClass().getSimpleName() + ": " + message;
    }

    private String normalizeCertificateStatus(String value) {
        String status = normalize(value);
        if (status == null) {
            throw new IllegalArgumentException("Certificate status is required");
        }
        String normalized = status.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case CERT_STATUS_NOT_REQUESTED, CERT_STATUS_REQUESTED, CERT_STATUS_ISSUED,
                    CERT_STATUS_EXPIRING_SOON, CERT_STATUS_EXPIRED, CERT_STATUS_FAILED -> normalized;
            default -> throw new IllegalArgumentException("Invalid certificate status: " + value);
        };
    }

    private Instant parseInstant(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Instant.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid ISO-8601 timestamp: " + value);
        }
    }
}
