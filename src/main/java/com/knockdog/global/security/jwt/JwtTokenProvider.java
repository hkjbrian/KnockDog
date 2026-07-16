package com.knockdog.global.security.jwt;

import com.knockdog.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * HS256 대칭키로 access/refresh JWT를 발급·검증한다 (jjwt 0.12.x, ADR-0003).
 *
 * <ul>
 *   <li>access: sub=userId, role 클레임(온보딩 후), 짧은 만료
 *   <li>refresh: sub=userId 만, 긴 만료 (role은 재발급 시 DB에서 최신값을 읽는다)
 * </ul>
 *
 * <p>현재시각은 주입된 {@link Clock}에서 얻는다 (ADR-0007).
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Duration accessExp;
    private final Duration refreshExp;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessExp = properties.accessExp();
        this.refreshExp = properties.refreshExp();
        this.clock = clock;
    }

    public String createAccessToken(Long userId, Role role) {
        var builder = baseToken(userId, accessExp);
        if (role != null) {
            builder.claim("role", role.name());
        }
        return builder.compact();
    }

    public String createRefreshToken(Long userId) {
        return baseToken(userId, refreshExp).compact();
    }

    /** 서명·만료를 검증하고 클레임을 반환한다. 유효하지 않으면 {@link JwtException}. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 토큰의 sub(userId)를 반환한다. */
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /** 토큰의 role 클레임을 {@link Role}로 반환한다. 온보딩 전(클레임 없음)이면 null. */
    public Role getRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        return role == null ? null : Role.valueOf(role);
    }

    /** 서명·만료가 유효하면 true. 만료·위조·형식오류는 예외 없이 false로 반환한다. */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private JwtBuilder baseToken(Long userId, Duration ttl) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256);
    }
}
