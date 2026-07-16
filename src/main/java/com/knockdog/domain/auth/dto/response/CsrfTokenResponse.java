package com.knockdog.domain.auth.dto.response;

/** 프론트엔드가 안전하지 않은 요청에 넣을 CSRF 토큰과 헤더 이름. */
public record CsrfTokenResponse(String headerName, String token) {
}
