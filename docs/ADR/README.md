> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 13:28

# ADR 인덱스 (Architecture Decision Records)

프로젝트 전반에 영향을 주는 설계 결정을 주제별로 기록한다.

- 새 ADR은 **[adr-template.md](adr-template.md)를 복사**해 `NNNN-슬러그.md`로 작성한다.
- 섹션 순서는 **맥락 → 검토한 후보 → 결정 → 결과**를 지킨다. (형식 상세: [../documentation-guide.md](../documentation-guide.md))
- 아래 인덱스에 새 항목을 추가한다.

| # | 제목 | 상태 |
|---|------|------|
| [0001](0001-oauth2login.md) | 카카오 인증에 `oauth2Login` 채택 | Accepted |
| [0002](0002-role-onboarding.md) | 역할 결정 = 가입 후 온보딩 | Accepted |
| [0003](0003-jwt-refresh-strategy.md) | JWT Access/Refresh + Refresh DB 저장 전략 | Accepted |
| [0004](0004-minimal-data-model.md) | 데이터 모델 최소화 (인증 전용 테이블만) | Accepted |
| [0005](0005-h2-test-database.md) | 테스트 DB로 H2 인메모리 채택 | Accepted |
| [0006](0006-docker-mysql.md) | 로컬 MySQL을 Docker로 구동 | Accepted |
| [0007](0007-server-time-handling.md) | 서버 시각은 Instant(UTC) + 주입 Clock | Accepted |
