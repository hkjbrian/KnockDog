package com.knockdog.domain.auth.service;

/** 새로 발급한 access/refresh JWT 쌍. access는 응답 body, refresh는 HttpOnly 쿠키로 전달한다. */
public record TokenPair(String accessToken, String refreshToken) {
}
