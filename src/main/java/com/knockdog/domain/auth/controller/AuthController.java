package com.knockdog.domain.auth.controller;

import com.knockdog.domain.auth.dto.request.RoleSignupRequest;
import com.knockdog.domain.auth.dto.response.AccessTokenResponse;
import com.knockdog.domain.auth.dto.response.CsrfTokenResponse;
import com.knockdog.domain.auth.service.AuthService;
import com.knockdog.domain.auth.service.RefreshTokenCookieService;
import com.knockdog.domain.auth.service.TokenPair;
import com.knockdog.global.common.dto.ApiResponse;
import com.knockdog.global.exception.BusinessException;
import com.knockdog.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Bearer access JWT와 HttpOnly refresh 쿠키 기반 온보딩·재발급·로그아웃 API. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    /** unsafe 요청에 앞서 프론트엔드가 보낼 CSRF 토큰을 발급한다. */
    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<CsrfTokenResponse>> csrf(CsrfToken csrfToken) {
        CsrfTokenResponse csrfTokenResponse = new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
        return ResponseEntity.ok(ApiResponse.success(csrfTokenResponse));
    }

    /** 인증된 사용자의 역할을 최초 한 번 확정하고 새 access/refresh 토큰을 발급한다. */
    @PostMapping("/signup/role")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> signupRole(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RoleSignupRequest request) {
        TokenPair tokenPair = authService.assignRole(getUserId(jwt), request.role());
        return successWithRefreshCookie("역할이 확정되었습니다.", tokenPair);
    }

    /** refresh 쿠키를 검증·회전하고 access JWT 응답과 새 refresh 쿠키를 설정한다. */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refresh(HttpServletRequest request) {
        String refreshToken = requiredRefreshToken(request);
        TokenPair tokenPair = authService.refresh(refreshToken);
        return successWithRefreshCookie("토큰이 재발급되었습니다.", tokenPair);
    }

    /** refresh 토큰을 무효화하고 브라우저의 refresh 쿠키를 삭제한다. */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        refreshTokenCookieService.findRefreshToken(request).ifPresent(authService::logout);
        ResponseCookie refreshCookie = refreshTokenCookieService.createExpiredRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("로그아웃되었습니다.", null));
    }

    private Long getUserId(Jwt jwt) {
        if (jwt == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return Long.valueOf(jwt.getSubject());
    }

    private String requiredRefreshToken(HttpServletRequest request) {
        Optional<String> refreshToken = refreshTokenCookieService.findRefreshToken(request);
        return refreshToken.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    private ResponseEntity<ApiResponse<AccessTokenResponse>> successWithRefreshCookie(
            String message,
            TokenPair tokenPair) {
        ResponseCookie refreshCookie = refreshTokenCookieService.createRefreshTokenCookie(tokenPair.refreshToken());
        AccessTokenResponse response = new AccessTokenResponse(tokenPair.accessToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(message, response));
    }
}
