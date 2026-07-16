> 생성: 2026-07-15 23:55 · 최종 수정: 2026-07-16 16:26

# LLM 활용 기록

과제 요구사항에 따라, AI(Claude / Claude Code)를 어떤 작업에 어떻게 활용했고 결과를 어떻게 검증했는지 기록한다.
생성 코드는 맹신하지 않고 동작·보안을 직접 검증한다.

| 날짜 | 작업 | AI 활용 내용 | 검증 방법 |
|------|------|-------------|-----------|
| 2026-07-15 | 설계 | 카카오 OAuth2 Authorization Code 흐름, Resource Server / oauth2Login 개념, 역할 결정 방식(온보딩), JWT/Refresh 전략을 AI와 논의하며 설계 확정 | 흐름을 다이어그램으로 재확인, 요구사항과 대조 |
| 2026-07-15 | 스캐폴딩 | Spring Initializr 산출물이 Boot 4.x로 생성되어, AI가 Boot 3.3.13 / Gradle 8.10.2로 의존성 좌표·버전 수정 (webmvc→web, 모듈 test starter→starter-test 등) | `./gradlew build` 성공 확인, 버전 요구사항(3.3.x/8.x/Java 21) 충족 확인 |
| 2026-07-15 | 개발환경 | CLAUDE.md·.claude/settings.json·본 문서 등 AI 활용 환경 구성 | — |
| 2026-07-16 | 문서체계 | CLAUDE.md를 핵심 규칙+인덱스로 슬림화, docs를 카테고리(assignment/tech-stack/git-rules/github-rules/architecture-overview)로 분리, 설계 결정을 docs/ADR로 주제별 기록, 문서 작성 가이드 정립 | 구조·링크 일관성 확인 |
| 2026-07-16 | 로컬 인프라 | docker-compose(MySQL 8.0)·application.yml(env 주입)·테스트용 H2 설정·README 작성. | `./gradlew clean build`(테스트 포함) 성공 확인 — H2로 contextLoads 통과 |
| 2026-07-16 | 개발 프로세스 | 이슈 기반 워크플로우(Milestone↔epic, 이슈→`feature/<#>-<slug>`→PR) 설계를 AI와 문답으로 확정, 이슈 템플릿·규칙 문서(issue-driven-workflow.md) 작성 | GitHub `Closes` 자동 close가 default 브랜치 머지 시에만 동작함을 확인해 수동 close 규칙으로 반영, 프로세스를 이슈 #5로 직접 dogfooding |
| 2026-07-16 | JWT 토큰 기반(#10) | AI와 TDD(RED→GREEN)로 `JwtTokenProvider`(jjwt 0.12.x·HS256, access/refresh 생성·검증·`validate`)·`RefreshToken` 엔티티/repo·`Role` enum 구현. 시각 취급 방식을 문답으로 검토해 ADR-0007(Instant/UTC + 주입 Clock) 도출, `Date`→`Instant`+`Clock` 리팩터 | `./gradlew build` 그린(단위 10개: 라운드트립·만료·서명위조·role 유무). 만료는 `Clock.fixed`로 결정론적 검증, 서명 위조는 다른 키로 거부 확인 |
| 2026-07-16 | Spring Security 공통 골격(#14) | AI가 `SecurityConfig`를 2개 `SecurityFilterChain`(@Order)로 분리(API `/api/**` STATELESS + Bearer 훅, 기본 체인 permitAll)·`RestAuthenticationEntryPoint`/`RestAccessDeniedHandler`로 401/403 JSON 응답·CORS·`ErrorResponse` DTO 구현. 필터 단계 예외는 `@RestControllerAdvice`로 잡히지 않는다는 점을 문답으로 검토해 `ObjectMapper` 직접 write 패턴 유지 결정(스코프상 `GlobalExceptionHandler`는 컨트롤러가 생기는 #11에서 도입), 근거는 ADR-0008 | `./gradlew build` 그린. MockMvc 통합 테스트 3개: 미인증 `/api/foo` → 401 JSON, USER role로 `@PreAuthorize("hasRole('ADMIN')")` → 403 JSON, `/` permitAll 검증. 테스트 컨트롤러는 `@TestConfiguration`으로 test 스코프에만 노출해 main 오염 없음 |
| 2026-07-16 | Spring Security 보안 보완(#14) | 리뷰에서 기본(브라우저) 체인의 CSRF 비활성화가 OAuth 세션의 교차 출처 로그아웃을 허용할 수 있음을 확인해, API 체인만 CSRF 비활성화를 유지하고 기본 체인은 Spring Security 기본 CSRF 보호를 사용하도록 수정 | `POST /logout`이 CSRF 토큰 없이 403, 토큰 포함 시 리다이렉트되는 MockMvc 회귀 테스트 추가. Java 21 `./gradlew test` 성공 |
| 2026-07-16 | 카카오 로그인·Bearer JWT(#11) | AI와 과제의 Bearer 예시를 대조해 access JWT는 응답 body→페이지 메모리→Authorization 헤더로 전달하고, refresh JWT만 HttpOnly 쿠키로 유지하는 ADR-0009를 확정. 카카오 가입·oauth2Login·refresh 회전·역할 온보딩·로그아웃 API·`GlobalExceptionHandler`를 구현 | Java 21 `./gradlew test` 성공. Bearer 헤더 API 인증, access 쿠키 거부, API CSRF 거부, refresh 회전·재사용 거부, 역할 확정 뒤 role 클레임을 테스트 |
| 2026-07-16 | 응답·트랜잭션 리팩터링(#11) | 의존성 주입 생성자를 Lombok `@RequiredArgsConstructor`로 통일하고, 서비스 기본 트랜잭션을 readOnly로 두고 쓰기 메서드에만 명시적 트랜잭션을 적용. 성공·실패 응답을 `ApiResponse<T>{code,message,data}`로 통일하는 ADR-0010과 예외/필터 처리를 반영 | Java 21 `./gradlew test` 성공. 401/403·CSRF 거부·역할 확정 성공 응답의 공통 envelope와 refresh 회귀 테스트 확인 |
| 2026-07-16 | 카카오 로그인 수동 검증 화면(#11) | AI가 Spring Boot 정적 HTML 화면을 추가해 카카오 로그인 시작·CSRF 토큰 발급·Bearer 헤더 역할 확정·refresh·logout을 브라우저에서 직접 확인하도록 구성 | 정적 리소스 응답 테스트와 Java 21 전체 빌드로 확인. 실제 카카오 로그인은 앱 키·redirect URI 설정이 필요 |
| 2026-07-16 | 인증 확인 화면 보완(#11) | AI가 인라인 `output` 태그로 인한 긴 CSRF 토큰의 깨진 렌더링을 블록형 `pre` 태그와 줄바꿈 CSS로 수정 | Java 21 `SecurityConfigTest` 성공, 정적 HTML에 상태 표시 영역이 포함되는지 확인 |

<!-- 이후 작업(kakao-login, role-home 등)마다 행 추가 -->
