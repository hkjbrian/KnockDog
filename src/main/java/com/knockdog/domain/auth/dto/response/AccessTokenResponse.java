package com.knockdog.domain.auth.dto.response;

/** Authorization Bearer 헤더에 넣을 짧은 수명의 access JWT 응답. */
public record AccessTokenResponse(String accessToken) {
}
