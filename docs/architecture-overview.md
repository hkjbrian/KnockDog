> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 16:26

# 아키텍처 개요 (Architecture Overview)

인증 흐름과 API의 참조 문서. 각 결정의 **근거**는 [ADR](ADR/README.md)에 있다.

## 인증 → JWT → 역할 분기 흐름 (8단계)

1. 사용자가 카카오 로그인 클릭 → 브라우저가 카카오 인증 페이지로 이동
2. 카카오에서 로그인·동의
3. 카카오가 `redirect_uri`로 `code` 전달
4. 백엔드가 `code`로 토큰 교환 (client_secret 포함, 서버 대 서버)
5. 카카오가 access token 응답 (조회용, 이후 버림)
6. 백엔드가 사용자 정보 조회 (`/v2/user/me` → kakaoId)
7. 가입 처리 + **자체 JWT(Access/Refresh) 발급** → Refresh JWT만 HttpOnly 쿠키로 설정
8. `?login=success`와 함께 프론트로 리다이렉트 → 프론트가 refresh API로 Access JWT를 받아 메모리에 보관하고 Bearer 헤더로 API를 호출

1~6은 `oauth2Login`이 자동 처리(→ [ADR-0001](ADR/0001-oauth2login.md)), 7은 `AuthenticationSuccessHandler`, 8은 Resource Server.
한 앱에 **OAuth2 Client(카카오 로그인) + Resource Server(JWT 검증)** 가 함께 있다.
JWT 전달·CSRF 정책은 [ADR-0009](ADR/0009-bearer-access-refresh-cookie.md)를 따른다.
로컬 수동 검증은 Spring Boot가 제공하는 `http://localhost:8080/` 정적 화면에서 수행할 수 있다.

## SecurityFilterChain 구성 (2체인)

두 인증 방식이 서로 다른 세션·CSRF·응답 정책을 요구하므로 `SecurityFilterChain`을 두 개로 분리한다 (→ [ADR-0008](ADR/0008-security-filter-chain-split.md)).

- **API 체인 `@Order(1)`**: `securityMatcher("/api/**")` · STATELESS · CORS 적용 · Resource Server가 `Authorization: Bearer <JWT>` 헤더를 검증한다. `CookieCsrfTokenRepository`로 unsafe 요청의 CSRF 토큰을 검증한다. 401/403은 `ApiResponse` JSON 포맷으로 응답한다.
- **기본 체인 `@Order(2)`**: 카카오 OAuth 로그인 브라우저 흐름. 세션·CSRF 기본 보호를 유지하며 `oauth2Login()`으로 사용자 조회와 성공 handler를 연결한다.

공통 응답 DTO는 `com.knockdog.global.common.dto.ApiResponse<T>`이며, 모든 성공·실패 응답은 `{code, message, data}` 형식을 따른다(→ [ADR-0010](ADR/0010-api-response-envelope.md)). `RestAuthenticationEntryPoint`(401)/`RestAccessDeniedHandler`(403)도 이를 직렬화한다.

## API 명세 요약

직접 구현하는 API:

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/auth/signup/role` | role 확정 후 access JWT 응답·refresh 쿠키 회전 | Bearer access JWT + CSRF (role null 허용) |
| GET | `/api/auth/csrf` | CSRF 토큰 발급 | 쿠키 불필요 |
| POST | `/api/auth/refresh` | refresh로 access JWT 응답·refresh 쿠키 회전 | `KD_REFRESH` 쿠키 + CSRF |
| POST | `/api/auth/logout` | refresh 무효화(DB 삭제)·refresh 쿠키 삭제 | `KD_REFRESH` 쿠키 + CSRF |
| GET | `/api/home` | **role 기반 분기 응답 (과제 핵심)** | Bearer access JWT (role 필수) |

Spring 자동 생성: `GET /oauth2/authorization/kakao`, `GET /login/oauth2/code/kakao`.

### `GET /api/home` 응답 예시

응답의 역할별 데이터는 `ApiResponse.data`에 담긴다.

견주:
```json
{ "role": "OWNER", "myDogs": [...], "nearbyKindergartens": [...] }
```
원장:
```json
{ "role": "DIRECTOR", "kindergartenName": "...", "enrolledDogs": [...], "todayAttendance": 5 }
```

## 데이터 모델 요약

- **users**: `id`(PK) / `kakao_id`(UNIQUE) / `nickname` / `role`(ENUM `OWNER`|`DIRECTOR`, NULL 허용) / `created_at` / `updated_at` — 이메일은 저장하지 않는다.
- **refresh_tokens**: `id`(PK) / `user_id`(FK) / `token` / `expires_at`

홈 응답 데이터(myDogs 등)는 서비스 코드 내 더미. 상세 근거는 [ADR-0004](ADR/0004-minimal-data-model.md).
