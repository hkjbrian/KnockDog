> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 16:26

# 기술 스택 & 실행 방법 (Tech Stack)

## 버전 (준수 필수 — 임의 상향 금지)

| 항목 | 버전 | 비고 |
|------|------|------|
| 언어 | Java **21** | 과제 요구 "17 이상, 21 허용" |
| 프레임워크 | Spring Boot **3.3.13** | 3.3.x 최신 |
| 보안 | Spring Security 6.3.x | Boot 3.3이 관리 (OAuth2 Client + Resource Server) |
| 빌드 | Gradle **8.10.2** (wrapper) | 요구 8.x |
| DB | MySQL 8.0 (Docker) + Spring Data JPA | |
| JWT | jjwt **0.12.7** | api / impl / jackson |
| 보조 | Lombok, configuration-processor | |
| 테스트 DB | H2 (in-memory, `testRuntimeOnly`) | 테스트는 DB 없이 통과 |

## ⚠️ Java 버전 주의 (빌드 시 필독)

시스템 기본 JDK는 **25**지만 이 프로젝트는 **Java 21**을 써야 한다.
**Gradle 8.10.2는 Java 25에서 실행되지 않으므로**, Gradle 명령 실행 시 반드시 Java 21로 실행할 것:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.10-tem
./gradlew build
```

- 프로젝트 루트 `.sdkmanrc`가 `java=21.0.10-tem`을 지정 → `sdk env`로 셸 전환 가능
  (`~/.sdkman/etc/config`에 `sdkman_auto_env=true` 설정 시 폴더 진입 자동 전환)
- `build.gradle`의 Gradle toolchain이 컴파일을 Java 21로 강제하지만, **Gradle 런처를 띄우는 JDK 자체가 21**이어야 함.

## 실행 방법

```bash
# MySQL 기동 (루트 docker-compose.yml, MySQL 8.0)
docker compose up -d

export JAVA_HOME=~/.sdkman/candidates/java/21.0.10-tem

./gradlew build            # 빌드 (테스트 포함)
./gradlew test             # 테스트 — H2 인메모리로 실행, DB 불필요
./gradlew bootRun          # 앱 실행 (docker MySQL 필요)
```

- 테스트는 `src/test/resources/application.yml`의 H2 인메모리 DB로 실행되어 **별도 DB 없이 통과**한다.
- 앱 실행(`bootRun`)은 docker MySQL이 떠 있어야 한다. MySQL은 루트 `docker-compose.yml`로 구동한다.

## 환경변수 (시크릿 — 하드코딩 금지)

시크릿은 환경변수로만 주입한다. (예정 목록, 구현하며 갱신)

| 변수 | 용도 |
|------|------|
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 |
| `KAKAO_CLIENT_SECRET` | 카카오 client secret |
| `KAKAO_REDIRECT_URI` | 카카오 redirect URI |
| `JWT_SECRET` | JWT HS256 서명키 |
| `AUTH_COOKIE_SECURE` | Refresh JWT 쿠키 Secure 속성 (운영 `true`, 로컬 HTTP만 `false`) |
| `FRONTEND_OAUTH_SUCCESS_URL` | OAuth 성공 뒤 refresh API로 access JWT를 복원할 프론트 URL |
| `DB_PORT` | docker MySQL host 포트 |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | MySQL 접속 정보 (URL 포트는 `DB_PORT`와 일치) |

로컬은 프로젝트 루트 `.env`(gitignore)로 관리한다. 템플릿 `.env.example`(커밋)을 복사(`cp .env.example .env`)해 채운다.
- **docker compose**: 같은 폴더의 `.env`를 자동으로 읽는다.
- **Spring 앱**: `.env`를 자동으로 읽지 않는다. IntelliJ는 `EnvFile` 플러그인으로 `.env`를 주입하고(README 참고), 셸 실행은 `set -a; source .env; set +a`로 export한다.

## 프로젝트 구조

```
knockdog/                 ← 저장소 루트 = Spring 프로젝트 루트
├── README.md             ← 사람용 빠른 실행 가이드
├── CLAUDE.md             ← 핵심 규칙 + 문서 인덱스 (AI용)
├── docs/                 ← 참조 문서 (본 파일 등)
├── docker-compose.yml    ← MySQL 8.0 로컬 인프라
├── build.gradle
├── settings.gradle
├── .sdkmanrc             ← Java 21 고정
├── gradle/ gradlew ...
└── src/
    ├── main/java/com/knockdog/auth/   ← base package
    ├── main/resources/application.yml ← 앱 설정 (env 주입)
    └── test/resources/application.yml ← 테스트용 H2 설정
```
