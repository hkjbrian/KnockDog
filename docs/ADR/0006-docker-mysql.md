> 생성: 2026-07-16 02:30 · 최종 수정: 2026-07-16 02:30

# ADR-0006: 로컬 MySQL을 Docker로 구동

- **상태**: Accepted
- **결정일**: 2026-07-16

## 맥락 (Context)
앱은 MySQL 8.0을 사용한다(과제 요구). 개발자·리뷰어마다 로컬 MySQL 설치 상태(버전·포트·계정)가 제각각이면 "내 환경에선 되는데" 문제가 생기고 재현성이 떨어진다. 동일한 DB 환경을 쉽게 띄울 방법이 필요하다.

## 검토한 후보 (Candidates)
- **후보 A — docker-compose로 MySQL 8.0 구동**: 루트 `docker-compose.yml` 한 개, `docker compose up -d` 한 명령으로 동일 버전·설정 재현. 로컬 설치 불필요, 정리도 쉬움.
- **후보 B — 로컬 네이티브 설치 (brew 등)**: 추가 도구가 필요 없으나 버전·설정이 사람마다 달라 재현성이 낮고 온보딩 비용이 큼.
- **후보 C — 클라우드 관리형 DB**: 운영 유사성은 높지만 과제 규모엔 과하고 비용·네트워크에 의존.

## 결정 (Decision)
**후보 A(docker-compose)를 채택.**
- 루트 `docker-compose.yml`, `mysql:8.0` 이미지, 이름 있는 볼륨으로 데이터 영속화.
- DB 이름·계정·비밀번호는 **환경변수로 주입**(로컬 편의를 위한 기본값 제공). 시크릿 하드코딩 금지 원칙 유지.
- 앱은 `application.yml`의 `DB_URL/DB_USERNAME/DB_PASSWORD`(env)로 이 MySQL에 접속.

## 결과 (Consequences)
- `docker compose up -d` 한 명령으로 누구나 동일한 MySQL 8.0을 즉시 사용.
- 테스트는 DB에 의존하지 않는다(H2, → [ADR-0005](0005-h2-test-database.md)). 앱 실행(`bootRun`)만 이 MySQL을 필요로 함.
- Docker 미설치 환경에서는 별도 설치가 필요(트레이드오프).
