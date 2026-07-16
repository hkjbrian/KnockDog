package com.knockdog.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** HttpOnly refresh JWT 쿠키의 이름과 브라우저 전달 정책. */
@ConfigurationProperties(prefix = "auth.refresh-cookie")
public record RefreshCookieProperties(
        String name,
        boolean secure,
        String sameSite) {
}
