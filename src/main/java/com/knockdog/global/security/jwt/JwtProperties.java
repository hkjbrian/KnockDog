package com.knockdog.global.security.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 서명·만료 설정. 서명키는 환경변수(JWT_SECRET)로만 주입한다 (ADR-0003).
 *
 * @param secret HS256 대칭키 (>= 32 bytes)
 * @param accessExp access 토큰 만료 (예: 30m)
 * @param refreshExp refresh 토큰 만료 (예: 14d)
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Duration accessExp,
        Duration refreshExp)
{}
