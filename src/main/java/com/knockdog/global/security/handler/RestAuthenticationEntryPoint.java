package com.knockdog.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knockdog.global.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증 실패(401) 발생 시 JSON 형식의 공통 에러 응답을 반환한다.
 *
 * <p>Spring Security의 기본 EntryPoint는 브라우저 로그인 리다이렉트를 목적으로 하므로,
 * API 응답 규격(application/json)을 강제하기 위해 커스텀 구현을 사용한다.
 * 예외 메시지 원문은 노출하지 않고 고정 메시지를 반환한다(정보 누출 방지).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String CODE = "UNAUTHORIZED";
    private static final String MESSAGE = "인증이 필요합니다.";

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(CODE, MESSAGE));
    }
}
