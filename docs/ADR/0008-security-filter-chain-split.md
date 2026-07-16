> 생성: 2026-07-16 14:27 · 최종 수정: 2026-07-16 14:27

# ADR-0008: SecurityFilterChain을 API/기본 두 개로 분리

- **상태**: Accepted
- **결정일**: 2026-07-16

## 맥락 (Context)
한 애플리케이션이 두 종류의 인증을 동시에 처리한다.

- **브라우저 흐름**: 카카오 `oauth2Login` → 세션 기반. 리다이렉트·CSRF·세션 쿠키가 필요.
- **API 흐름**: `Bearer <JWT>` → Resource Server. STATELESS·CSRF 비활성·JSON 예외 응답이 필요.

두 흐름은 세션 정책, 인증 방식, 예외 응답 포맷이 서로 반대다. 이후 이슈(#11 oauth2Login, #12 resource server jwt)가 이 공통 설정을 각각 확장해야 하므로, 골격 단계에서 어떻게 SecurityFilterChain을 구성할지 결정이 필요했다.

## 검토한 후보 (Candidates)
- **후보 A — 단일 체인 + `RequestMatcher` 분기**: 하나의 `SecurityFilterChain` 안에서 `authorizeHttpRequests`, `sessionManagement`, `exceptionHandling` 등을 조건 분기. 설정 파일이 하나로 모여 겉보기엔 단순하지만, 세션 정책이나 CSRF 같은 체인 전역 설정을 request별로 분기할 수 없어 실질적으로 두 정책이 섞인다. 이후 #11이 `oauth2Login`을, #12가 `oauth2ResourceServer`를 같은 체인에 붙이면 필터 순서·엔드포인트 충돌이 발생하기 쉽다.
- **후보 B — 체인 2개(@Order 분리)**: `securityMatcher("/api/**")` 로 매칭되는 API 체인(Order 1)과 그 외 요청을 처리하는 기본 체인(Order 2)을 분리. 각 체인이 자기 정책(STATELESS vs 세션, csrf off vs 후일 csrf 옵션, 예외 응답 포맷 등)을 독립적으로 소유한다. 이후 feature 이슈는 자기 체인에만 hook을 꽂으면 되므로 충돌이 줄어든다. 반면 CORS나 예외 핸들러 같은 공통 정책은 두 체인에서 명시적으로 참조해야 한다.

## 결정 (Decision)
**후보 B(체인 2개, `@Order`로 우선순위 지정)를 채택.** 근거:

1. Spring Security의 세션 정책·CSRF·예외 처리는 **체인 단위** 설정이라 request별 분기와 상성이 나쁘다.
2. #11(oauth2Login)과 #12(resource server jwt)가 각각 다른 체인에 "꽂기만" 하면 되어 이후 이슈의 변경 범위가 자연스레 격리된다.
3. 공통 정책(CORS, 401/403 JSON 응답, `ErrorResponse` DTO)은 별도 빈으로 추출해 두 체인이 참조한다.

구성:

- **API 체인 `@Order(1)`**: `securityMatcher("/api/**")` · STATELESS · csrf 비활성 · CORS · `anyRequest().authenticated()` · `RestAuthenticationEntryPoint`/`RestAccessDeniedHandler` 연결. TODO: #12에서 `oauth2ResourceServer().jwt()` 추가.
- **기본 체인 `@Order(2)`**: 그 외 요청. 현재는 `permitAll()`. TODO: #11에서 `oauth2Login()` 추가.
- **공통 빈**: `CorsConfigurationSource`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `ErrorResponse`(dto).

## 결과 (Consequences)
- #11은 기본 체인에 `oauth2Login()` 만 추가하면 되고, #12는 API 체인에 `oauth2ResourceServer(rs -> rs.jwt(...))` 만 추가하면 된다. 두 이슈가 서로의 설정을 건드리지 않는다.
- 401/403 응답 포맷이 프로젝트 전체에서 일관된다 (`ErrorResponse{code, message}` 최소 스키마).
- 공통 정책을 두 체인이 각자 명시하는 비용이 생긴다(주로 CORS). 지금은 감내할 만한 수준.
- API용 라우트가 새로 늘어도 `/api/**` 매칭 하에 자동으로 STATELESS 정책을 상속받는다.
