package com.scheduler.distributed_job_scheduler_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMillis) {
        if (!StringUtils.hasText(base64Secret)) {
            throw new IllegalArgumentException("app.jwt.secret must be configured");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("app.jwt.expiration-ms must be greater than zero");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(base64Secret.trim());
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "app.jwt.secret must be valid Base64 containing at least 32 random bytes", exception);
        }
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserDetails userDetails) {
        if (userDetails == null || !StringUtils.hasText(userDetails.getUsername())) {
            throw new IllegalArgumentException("A user with a non-blank username is required to generate a JWT");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expirationMillis);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (!StringUtils.hasText(token) || userDetails == null) {
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);
            return StringUtils.hasText(claims.getSubject())
                    && claims.getSubject().equals(userDetails.getUsername())
                    && userDetails.isEnabled()
                    && userDetails.isAccountNonExpired()
                    && userDetails.isAccountNonLocked()
                    && userDetails.isCredentialsNonExpired();
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("JWT must not be blank");
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }
}
