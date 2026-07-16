> 생성: 2026-07-16 16:21 · 최종 수정: 2026-07-16 16:26

# ADR-0009: Access JWT는 Bearer 헤더, Refresh JWT는 HttpOnly 쿠키로 전달

- **상태**: Accepted
- **결정일**: 2026-07-16

## 맥락 (Context)

과제의 홈 API 예시는 `Authorization: Bearer <JWT>`를 명시한다. Access JWT를 이 표준 헤더로 검증하면서도, 긴 수명의 refresh JWT는 JavaScript에서 읽을 수 없도록 저장·전달 정책을 정해야 한다. 카카오 OAuth 성공 콜백은 브라우저 페이지 이동이므로 access JWT를 URL에 넣어 전달하면 기록·로그 노출 위험이 있다.

## 검토한 후보 (Candidates)

- **후보 A — access/refresh JWT를 모두 HttpOnly 쿠키**: 카카오 콜백 뒤 리다이렉트는 간단하지만 브라우저 JavaScript가 access JWT를 읽을 수 없어 `Authorization: Bearer` 헤더를 만들 수 없다.
- **후보 B — access JWT는 응답 body, refresh JWT만 HttpOnly 쿠키**: 짧은 access JWT는 프론트 페이지 메모리에만 두고 Bearer 헤더로 전송할 수 있다. refresh JWT는 XSS로부터 보호되고 서버가 회전·무효화한다.
- **후보 C — access JWT를 localStorage/sessionStorage에 저장**: 새로고침 복원은 쉽지만 XSS가 토큰을 읽을 수 있어 채택하지 않는다.

## 결정 (Decision)

**후보 B를 채택한다.**

- Access JWT는 `/api/auth/refresh`, `/api/auth/signup/role`의 response body로 전달한다. 프론트는 페이지 메모리에만 저장하고 보호 API마다 `Authorization: Bearer <access JWT>` 헤더를 보낸다.
- Resource Server는 Spring Security의 기본 Bearer resolver로 Authorization 헤더를 검증한다.
- Refresh JWT만 `KD_REFRESH` 쿠키(`HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/api/auth`)로 설정하고 DB 저장·회전·로그아웃 무효화 전략(ADR-0003)을 유지한다.
- OAuth 성공 handler는 refresh 쿠키를 설정하고 `?login=success`와 함께 고정된 프론트 URL로 리다이렉트한다. 프론트는 CSRF 토큰을 받은 뒤 `/api/auth/refresh`를 호출해 access JWT를 메모리에 복원한다.
- refresh 쿠키를 사용하는 `POST /api/auth/refresh`, `POST /api/auth/logout`은 CSRF 토큰을 검증한다. 현재 API 체인의 unsafe 요청도 동일하게 CSRF 보호한다.

## 결과 (Consequences)

- 과제의 `Authorization: Bearer <JWT>` 예시와 Spring Resource Server 기본 방식에 맞는다.
- Access JWT는 새로고침 시 사라지므로 프론트는 refresh API로 복원해야 한다. localStorage·sessionStorage에는 저장하지 않는다.
- Refresh JWT는 HttpOnly 쿠키이므로 XSS에서 읽을 수 없지만 자동 전송되므로 CSRF 방어가 필요하다.
