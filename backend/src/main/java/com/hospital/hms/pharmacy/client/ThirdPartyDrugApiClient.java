package com.hospital.hms.pharmacy.client;

import com.hospital.hms.pharmacy.dto.ExternalDrugApiResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Client for 3rd party drug lookup API by barcode/GTIN.
 * Placeholder URL can be updated via thirdparty.api.url.
 * When URL is empty or not configured, lookup returns empty (fallback to manual).
 */
@Component
public class ThirdPartyDrugApiClient {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyDrugApiClient.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;
    private final boolean enabled;

    public ThirdPartyDrugApiClient(
            @Value("${thirdparty.api.url:}") String baseUrl,
            @Value("${thirdparty.api.key:}") String apiKey) {
        this.baseUrl = baseUrl != null ? baseUrl.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.enabled = !this.baseUrl.isEmpty();

        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        if (!enabled) {
            log.info("Third-party drug API disabled (thirdparty.api.url not configured). Placeholder: https://placeholder-drug-api.example.com");
        } else {
            log.info("Third-party drug API enabled: {} (key configured: {})", maskUrl(this.baseUrl), !this.apiKey.isEmpty());
        }
    }

    /**
     * Look up drug by barcode/GTIN.
     * Returns empty if disabled, timeout, or API error (fallback to manual).
     */
    public Optional<ExternalDrugApiResponseDto> lookupByBarcode(String barcode) {
        if (!enabled) {
            return Optional.empty();
        }
        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }

        String url = buildLookupUrl(barcode);
        try {
            var spec = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON);

            if (!apiKey.isEmpty()) {
                spec = spec.header("X-Api-Key", apiKey);
            }

            ExternalDrugApiResponseDto response = spec.retrieve()
                    .body(ExternalDrugApiResponseDto.class);

            if (response != null && response.resolveMedicineName() != null) {
                return Optional.of(response);
            }
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("External drug API lookup failed for barcode {}: {} (fallback to manual)", barcode, e.getMessage());
            return Optional.empty();
        }
    }

    private String buildLookupUrl(String barcode) {
        String base = baseUrl.replaceAll("/+$", "");
        if (base.endsWith("/gtin") || base.endsWith("/gtin/")) {
            return base + barcode.trim();
        }
        return base + "/gtin/" + barcode.trim();
    }

    private static String maskUrl(String url) {
        if (url == null || url.length() < 20) return "***";
        return url.substring(0, Math.min(30, url.length())) + "...";
    }
}
