package com.hospital.hms.auth;

import com.hospital.hms.auth.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${hms.security.jwt.secret}") String secret,
            @Value("${jwt.expiration:28800000}") long expirationMs
    ) {
        byte[] bytes = resolveSecretBytes(secret);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    private byte[] resolveSecretBytes(String secret) {
        String normalized = secret == null ? "" : secret.trim();
        byte[] bytes = tryDecodeBase64(normalized);
        if (bytes == null) {
            bytes = normalized.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length >= 32) {
            return bytes;
        }
        return sha256(normalized.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] tryDecodeBase64(String value) {
        if (value.isBlank()) {
            return null;
        }
        // Base64 secrets should not contain JWT-url-safe separators like '-' unless explicitly URL-base64 encoded.
        // We treat such values as raw strings to support plain-text dev secrets.
        if (!value.matches("^[A-Za-z0-9+/=]+$") || value.length() % 4 != 0) {
            return null;
        }
        try {
            return Decoders.BASE64.decode(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public String generateSuperAdminToken(AppUser admin, Instant issuedAt) {
        return buildToken(admin.getUsername(), Map.of(
                "role", admin.getRole().name(),
                "userId", admin.getId(),
                "isSuperAdmin", true
        ), issuedAt);
    }

    public String generateHospitalUserToken(String userId,
                                           String email,
                                           String role,
                                           Long hospitalId,
                                           String schemaName,
                                           Instant issuedAt) {
        return buildToken(userId, Map.of(
                "email", email,
                "role", role,
                "hospitalId", hospitalId,
                "schemaName", schemaName
        ), issuedAt);
    }

    private String buildToken(String subject, Map<String, Object> claims, Instant issuedAt) {
        Instant exp = issuedAt.plusMillis(expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}

