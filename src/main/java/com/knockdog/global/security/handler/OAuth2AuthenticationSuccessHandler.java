package com.knockdog.global.security.handler;

import com.knockdog.domain.auth.service.AuthService;
import com.knockdog.domain.auth.service.KakaoOAuth2User;
import com.knockdog.domain.auth.service.RefreshTokenCookieService;
import com.knockdog.domain.auth.service.TokenPair;
import com.knockdog.global.config.FrontendProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/** OAuth 성공 시 refresh JWT 쿠키를 설정하고 프론트엔드가 access JWT를 재발급하도록 복귀시킨다. */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final FrontendProperties frontendProperties;

    /** 로그인 principal의 사용자 ID로 JWT를 발급하고 로그인 완료 표시와 함께 고정된 프론트 URL로 이동시킨다. */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        KakaoOAuth2User user = (KakaoOAuth2User) authentication.getPrincipal();
        TokenPair tokenPair = authService.issueTokens(user.getUserId());
        response.addHeader(HttpHeaders.SET_COOKIE,
                refreshTokenCookieService.createRefreshTokenCookie(tokenPair.refreshToken()).toString());
        String successUrl = UriComponentsBuilder.fromUriString(frontendProperties.oauthSuccessUrl())
                .queryParam("login", "success")
                .build()
                .toUriString();
        response.sendRedirect(successUrl);
    }
}
