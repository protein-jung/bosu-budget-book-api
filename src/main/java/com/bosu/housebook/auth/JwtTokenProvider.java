package com.bosu.housebook.auth;

import com.bosu.housebook.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String ADMIN_SUBJECT = "admin";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = jwtProperties.expirationMs();
    }

    public String generateToken(Long userId, boolean isAdminUser) {
        return buildToken(String.valueOf(userId), ROLE_USER, isAdminUser);
    }

    public String generateAdminToken() {
        return buildToken(ADMIN_SUBJECT, ROLE_ADMIN, false);
    }

    private String buildToken(String subject, String role, boolean isAdminUser) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .claim("admin", isAdminUser)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token) {
        String subject = parseClaims(token).getSubject();
        return Long.valueOf(subject);
    }

    public boolean isAdmin(String token) {
        return ROLE_ADMIN.equals(parseClaims(token).get("role", String.class));
    }

    /** 일반 사용자 토큰(role=USER)인데, 그 계정이 관리자로 지정된 이메일이라 admin.user-email과
     * 일치해서 발급 시점에 admin 클레임이 붙은 경우. */
    public boolean isUserAdmin(String token) {
        return Boolean.TRUE.equals(parseClaims(token).get("admin", Boolean.class));
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
