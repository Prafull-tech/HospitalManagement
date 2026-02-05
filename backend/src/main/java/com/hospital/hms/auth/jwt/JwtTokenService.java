package com.hospital.hms.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hospital.hms.auth.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenService {

    private final Algorithm algorithm;
    private final long expiryMinutes;

    public JwtTokenService(
            @Value("${hms.security.jwt.secret:dev-jwt-secret-change-me}") String secret,
            @Value("${hms.security.jwt.expiry-minutes:480}") long expiryMinutes) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expiryMinutes = expiryMinutes;
    }

    public String generateToken(String username, UserRole role) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(username)
                .withClaim("role", role.name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(expiryMinutes, ChronoUnit.MINUTES)))
                .sign(algorithm);
    }

    public DecodedJWT verify(String token) throws JWTVerificationException {
        return JWT.require(algorithm).build().verify(token);
    }
}

