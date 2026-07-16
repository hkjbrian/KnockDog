package com.knockdog.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knockdog.global.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * 인가 실패(403) 발생 시 JSON 형식의 공통 에러 응답을 반환한다.
 *
 * <p>기본 {@code AccessDeniedHandler}는 HTML 에러 페이지로 응답하므로, API 규격을 지키기 위해
 * 커스텀 구현을 사용한다. 예외 원문은 노출하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final String CODE = "FORBIDDEN";
    private static final String MESSAGE = "접근 권한이 없습니다.";

    private final ObjectMapper objectMapper;

    /** Spring Security 필터 단계의 인가 실패를 공통 JSON 응답으로 쓴다. */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(CODE, MESSAGE));
    }
}
