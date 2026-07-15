# KnockDog — 반려견 유치원 인증/권한 API

견주 ↔ 원장을 연결하는 반려견 유치원 플랫폼의 인증/인가 서비스.
Kakao 소셜 로그인 → 자체 JWT 발급/검증 → 역할(원장/견주) 기반 홈 분기.

---

## 이 프로젝트에서 지켜야 할 핵심 규칙

1. **브랜치**: `main` ← `epic/*` ← `feature/*`. **`main`에 직접 작업/push 금지.** 1 PR = 1 기능. (상세: `docs/git-rules.md`, `docs/github-rules.md`)
2. **커밋**: Conventional Commits, 한글 OK. **`Co-Authored-By` 트레일러 절대 넣지 말 것.**
3. **시크릿**(카카오 키·JWT 서명키·DB 비번)은 **환경변수로만**. 하드코딩 금지.
4. **버전 임의 상향 금지**: Spring Boot 3.3.x / Gradle 8.x / Java 21 고정.
5. **Gradle 실행 시 Java 21 사용**: 시스템 기본이 JDK 25라 그대로 돌리면 실패. `JAVA_HOME=~/.sdkman/candidates/java/21.0.10-tem` 또는 `sdk env`. (상세: `docs/tech-stack.md`)
6. **결정하면 기록**: 논의로 무언가 확정되면 관련 문서/ADR을 **자동으로 작성·갱신하고 사용자에게 알린다.** 설계 결정은 `docs/ADR/`. (방법: `docs/documentation-guide.md`)
7. **생성 코드 맹신 금지**: 동작·보안 직접 검증. AI 작업 내역은 `docs/llm-usage.md`에 append.

> 모든 `docs/*.md`는 맨 앞에 시간정보 헤더를 둔다 (`docs/documentation-guide.md`).

## 문서 인덱스 (필요할 때 참조)

| 문서 | 내용 |
|------|------|
| [docs/documentation-guide.md](docs/documentation-guide.md) | 문서 작성 규칙 · 결정 기록 워크플로우 · ADR 형식 |
| [docs/assignment_information.md](docs/assignment_information.md) | 과제 원문 / 요구사항 |
| [docs/architecture-overview.md](docs/architecture-overview.md) | 인증 8단계 흐름 · API 명세 · 데이터 모델 요약 |
| [docs/ADR/](docs/ADR/README.md) | 설계 결정 기록 (oauth2Login, 역할 온보딩, JWT 전략, 데이터 모델) |
| [docs/tech-stack.md](docs/tech-stack.md) | 스택 버전 · 빌드/실행 · Java 버전 주의 · 환경변수 |
| [docs/git-rules.md](docs/git-rules.md) | 커밋 컨벤션·작성법 · 브랜치 전략 |
| [docs/github-rules.md](docs/github-rules.md) | PR 작성법 · 리뷰 · 머지 흐름 |
| [docs/llm-usage.md](docs/llm-usage.md) | LLM 활용 기록 (과제 필수, 작업마다 append) |

작업 시작 전, 관련 문서를 먼저 확인할 것.
