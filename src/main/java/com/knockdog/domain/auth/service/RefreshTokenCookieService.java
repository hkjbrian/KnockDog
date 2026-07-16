package com.knockdog.domain.auth.service;

import com.knockdog.global.config.RefreshCookieProperties;
import com.knockdog.global.security.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** HttpOnly refresh JWT 쿠키의 생성·조회 정책을 한 곳에서 관리한다. */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private static final String REFRESH_PATH = "/api/auth";

    private final RefreshCookieProperties properties;
    private final JwtProperties jwtProperties;

    /** 새 refresh JWT를 브라우저에 설정하는 HttpOnly 쿠키를 만든다. */
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(refreshToken, jwtProperties.refreshExp());
    }

    /** 브라우저의 refresh JWT 쿠키를 만료시키는 응답 쿠키를 만든다. */
    public ResponseCookie createExpiredRefreshTokenCookie() {
        return createCookie("", Duration.ZERO);
    }

    /** 요청 쿠키에서 refresh JWT를 찾는다. */
    public Optional<String> findRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> properties.name().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private ResponseCookie createCookie(String value, Duration maxAge) {
        return ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(REFRESH_PATH)
                .maxAge(maxAge)
                .build();
    }
}
