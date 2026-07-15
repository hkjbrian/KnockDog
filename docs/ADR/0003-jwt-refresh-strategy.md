> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 00:50

# ADR-0003: JWT Access/Refresh + Refresh DB 저장 전략

- **상태**: Accepted
- **결정일**: 2026-07-15

## 맥락 (Context)
자체 JWT로 인증 상태를 관리하고, 만료·재발급·**로그아웃**을 지원해야 한다. JWT는 본래 무상태(stateless)라 한 번 발급하면 만료 전까지 유효 → 로그아웃해도 서버가 강제로 무효화할 수 없는 문제가 있다.

## 검토한 후보 (Candidates)
- **후보 A — Refresh를 DB에 저장**: refresh 토큰을 DB에 저장하고, 로그아웃 시 삭제/재발급 시 존재 확인으로 무효화. 인프라 추가 없이 요구사항 충족.
- **후보 B — Redis에 refresh/블랙리스트 저장**: TTL·성능에 유리하나 과제 규모엔 인프라가 과함.
- **후보 C — 완전 무상태(refresh 미저장)**: 가장 단순하지만 로그아웃 강제 무효화가 불가 → 요구사항(로그아웃 처리) 미충족.

## 결정 (Decision)
**후보 A(Refresh DB 저장)를 채택.**

| 토큰 | 만료 | 클레임 | 저장 |
|------|------|--------|------|
| Access | ~30분 | `sub`=users.id, `role`(온보딩 후) | 무상태 |
| Refresh | ~14일 | `sub`=users.id | **DB 저장** |

- 서명: HS256 대칭키 (서명키는 환경변수 `JWT_SECRET`).
- **로그아웃**: refresh 토큰을 DB에서 삭제 → 재발급 요청 시 "DB에 존재하는지" 확인하여 무효화.
- 재발급: 유효하고 DB에 존재하는 refresh만 새 access 발급.

## 결과 (Consequences)
- `refresh_tokens` 테이블 필요(→ [ADR-0004](0004-minimal-data-model.md)).
- 로그아웃/재발급 로직이 DB 조회에 의존.
