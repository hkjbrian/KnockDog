package com.knockdog.global.common.dto;

/**
 * API 공통 에러 응답 DTO.
 *
 * <p>Spring Security 예외 핸들러 및 향후 컨트롤러 예외 처리에서 공통으로 사용한다.
 * 필드는 최소한(code, message)만 유지한다. 서버 시각 등 부가 정보가 필요하면
 * 상위 응답 래퍼에서 확장한다.
 */
public record ErrorResponse(String code, String message) {
}
