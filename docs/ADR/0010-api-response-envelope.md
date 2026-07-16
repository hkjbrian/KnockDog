> 생성: 2026-07-16 15:50 · 최종 수정: 2026-07-16 16:24

# ADR-0010: 성공·실패 API 응답을 ApiResponse로 통일

- **상태**: Accepted
- **결정일**: 2026-07-16

## 맥락 (Context)
인증 API의 성공 응답은 204 또는 개별 DTO이고, 검증·도메인·Spring Security 예외는 각각 다른 형식으로 응답할 여지가 있었다. 프론트엔드가 상태 코드뿐 아니라 응답 body를 일관되게 처리할 수 있도록 공통 형식이 필요하다.

## 검토한 후보 (Candidates)
- **후보 A — 성공은 endpoint별 DTO·204, 실패만 ErrorResponse**: REST 관례상 간결하지만 프론트엔드는 성공/실패마다 body 유무와 구조를 분기해야 한다.
- **후보 B — 모든 API에 `ApiResponse<T>` envelope 적용**: 성공·실패의 최상위 구조를 고정할 수 있다. 데이터가 없는 성공도 `data: null`을 명시하므로 body가 조금 커진다.

## 결정 (Decision)
**후보 B를 채택한다.** 모든 직접 구현 API와 필터 단계 401/403은 `{code, message, data}` 형식을 사용한다.

- 성공: `code`는 `SUCCESS`, `data`에는 endpoint별 DTO 또는 `null`을 둔다.
- 실패: 도메인·검증·필터 예외 모두 오류 `code`, 사용자용 `message`, `data: null`을 둔다.
- 기존 `ErrorResponse`는 `ApiResponse<Void>`로 대체한다.
- `AuthController`는 `ResponseEntity<ApiResponse<T>>`로 상태·헤더·body를 명시한다.

## 결과 (Consequences)
- 컨트롤러, `GlobalExceptionHandler`, `AuthenticationEntryPoint`, `AccessDeniedHandler`가 같은 DTO를 사용한다.
- refresh 쿠키 정책은 `RefreshTokenCookieService`가 `ResponseCookie`로 생성하고, controller와 OAuth success handler가 이를 응답 헤더에 넣는다. access JWT는 endpoint별 `data`에 담는다.
- #12의 홈 API도 역할별 데이터만 `data`에 넣고 동일한 envelope를 유지한다.
