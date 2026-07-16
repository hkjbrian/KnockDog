> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 00:50

# ADR-0004: 데이터 모델 최소화 (인증 전용 테이블만)

- **상태**: Accepted
- **결정일**: 2026-07-15

## 맥락 (Context)
홈 API는 견주/원장에게 서로 다른 데이터(반려견 목록, 주변 유치원, 등원 현황 등)를 보여준다. 이 데이터를 실제 엔티티로 모델링할지, 더미로 처리할지 결정이 필요했다. 과제는 "데이터는 더미 수준이어도 OK, 핵심은 role 기반 분기"라고 명시한다.

## 검토한 후보 (Candidates)
- **후보 A — 인증 전용 테이블만 (users, refresh_tokens)**: 홈 데이터는 서비스 코드 내 더미로 제공. 스키마가 단순하고 과제 핵심(role 분기)에 집중.
- **후보 B — dogs / kindergartens 엔티티까지 실제 구현**: JPA 연관관계(FK)를 더 보여줄 수 있으나, 과제 핵심과 무관한 범위 확장.

## 결정 (Decision)
**후보 A(인증 전용 테이블만)를 채택.**

- **users**: `id`(PK) / `kakao_id`(UNIQUE) / `nickname` / `role`(ENUM, NULL 허용) / `created_at` / `updated_at`
- **refresh_tokens**: `id`(PK) / `user_id`(FK) / `token` / `expires_at`

홈 응답 데이터(`myDogs`, `nearbyKindergartens`, `enrolledDogs`, `todayAttendance`)는 **서비스 코드 내 더미**로 제공하고, role로 분기한다.

## 결과 (Consequences)
- 스키마가 단순, 인증/인가에 집중.
- 홈 분기는 DB 조회가 아니라 서비스 계층의 role 분기 + 더미 데이터로 구현.
- 추후 실제 데이터가 필요하면 별도 ADR로 dogs/kindergartens 도입을 재검토.
