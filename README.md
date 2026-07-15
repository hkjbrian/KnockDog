# KnockDog — 반려견 유치원 인증/권한 API

견주 ↔ 원장을 연결하는 반려견 유치원 플랫폼의 인증/인가 서비스.
Kakao 소셜 로그인 → 자체 JWT 발급/검증 → 역할(원장/견주) 기반 홈 분기.

## 기술 스택
- Java 21 · Spring Boot 3.3.13 · Spring Security 6.3 (OAuth2 Client + Resource Server)
- Gradle 8.10.2 · MySQL 8.0 (Docker) · Spring Data JPA · jjwt 0.12.7

## 빠른 실행

> ⚠️ 시스템 기본 JDK가 25면 Gradle 실행이 실패한다. **Java 21**로 실행할 것.

```bash
# 1) MySQL 기동 (Docker)
docker compose up -d

# 2) Java 21 지정 (프로젝트 .sdkmanrc가 21 지정 → sdk env 도 가능)
export JAVA_HOME=~/.sdkman/candidates/java/21.0.10-tem

# 3) 빌드 · 테스트 · 실행
./gradlew build      # 테스트는 H2 인메모리로 실행 (DB 불필요)
./gradlew bootRun
```

## 환경변수

시크릿은 **환경변수로만** 주입한다. 하드코딩 금지.

| 변수 | 용도 | 로컬 기본값 |
|------|------|------------|
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/knockdog` |
| `DB_USERNAME` / `DB_PASSWORD` | DB 계정 | `knockdog` / `knockdog` |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` / `KAKAO_REDIRECT_URI` | 카카오 OAuth (인증 기능 구현 시) | — |
| `JWT_SECRET` | JWT HS256 서명키 (인증 기능 구현 시) | — |

## 문서

| 문서 | 내용 |
|------|------|
| [CLAUDE.md](CLAUDE.md) | 핵심 규칙 + 문서 인덱스 |
| [docs/assignment_information.md](docs/assignment_information.md) | 과제 요구사항 |
| [docs/architecture-overview.md](docs/architecture-overview.md) | 인증 흐름 · API 명세 · 데이터 모델 |
| [docs/ADR/](docs/ADR/README.md) | 설계 결정 기록 |
| [docs/tech-stack.md](docs/tech-stack.md) | 스택 · 빌드/실행 · 환경변수 |

## 브랜치 전략

`main` ← `epic/*` ← `feature/*`. 상세: [docs/git-rules.md](docs/git-rules.md) · [docs/github-rules.md](docs/github-rules.md).
