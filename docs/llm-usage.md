> 생성: 2026-07-15 23:55 · 최종 수정: 2026-07-16 11:55

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

<!-- 이후 작업(kakao-login, jwt, role-home 등)마다 행 추가 -->
