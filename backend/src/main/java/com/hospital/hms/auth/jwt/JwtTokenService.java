package com.hospital.hms.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.entity.RefreshToken;
import com.hospital.hms.auth.entity.UserRole;
import com.hospital.hms.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenService {

    private final Algorithm algorithm;
    private final long expiryMinutes;
    private final long refreshExpiryDays;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenService(
            @Value("${hms.security.jwt.secret}") String secret,
            @Value("${hms.security.jwt.expiry-minutes:30}") long expiryMinutes,
            @Value("${hms.security.jwt.refresh-expiry-days:7}") long refreshExpiryDays,
            RefreshTokenRepository refreshTokenRepository) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expiryMinutes = expiryMinutes;
        this.refreshExpiryDays = refreshExpiryDays;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        var builder = JWT.create()
                .withSubject(user.getUsername())
                .withClaim("role", user.getRole().name())
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(expiryMinutes, ChronoUnit.MINUTES)));

        if (user.getHospital() != null) {
            builder.withClaim("hospitalId", user.getHospital().getId())
                    .withClaim("hospitalCode", user.getHospital().getHospitalCode())
                    .withClaim("tenantSlug", user.getHospital().getSubdomain());
        }

        return builder.sign(algorithm);
    }

    public DecodedJWT verify(String token) throws JWTVerificationException {
        return JWT.require(algorithm).build().verify(token);
    }

    @Transactional
    public String createRefreshToken(String username) {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUsername(username);
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(RefreshToken::isUsable)
                .orElse(null);
    }

    @Transactional
    public String rotateRefreshToken(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        return createRefreshToken(oldToken.getUsername());
    }

    @Transactional
    public void revokeAllTokensForUser(String username) {
        refreshTokenRepository.revokeAllByUsername(username);
    }
}
