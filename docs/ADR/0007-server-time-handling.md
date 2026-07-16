> 생성: 2026-07-16 13:28 · 최종 수정: 2026-07-16 13:28

# ADR-0007: 서버 시각은 Instant(UTC) + 주입 Clock으로 다룬다

- **상태**: Accepted
- **결정일**: 2026-07-16

## 맥락 (Context)
인증 서버는 JWT의 `iat`/`exp`, refresh 토큰 `expires_at`, 각종 `created_at` 등 **시각(時刻)** 을 반복해서 다룬다. 시각 타입과 "현재시각을 어떻게 얻을지"를 통일하지 않으면 다음 문제가 생긴다.

- 서버/DB 타임존이 배포 환경마다 다르면 값 해석이 흔들린다.
- 코드가 `new Date()`처럼 현재시각을 직접 만들면, 만료 같은 시간 의존 로직을 **테스트로 고정**할 수 없다.
- 발급 서버와 검증 서버가 서로 다른 기준을 쓰면 만료 판정이 어긋난다.

## 검토한 후보 (Candidates)
- **후보 A — `java.util.Date`**: 레거시 표준. mutable이라 값이 외부에서 바뀔 수 있고, `toString()`이 로컬 타임존으로 출력돼 "타임존을 가진 값"이라는 착각을 부른다. 다수 메서드가 deprecated.
- **후보 B — `LocalDateTime`**: 사람이 읽기 좋으나 **타임존/오프셋 정보가 없다.** "서버 로컬 시각"에 암묵적으로 의존해, 타임존이 다른 환경 간 이동 시 의미가 달라진다.
- **후보 C — `Instant`(UTC) + 주입 `Clock`**: `Instant`는 UTC 타임라인 위의 한 순간을 나타내는 불변 값. 현재시각은 `java.time.Clock` 빈에서 얻어, 테스트에선 `Clock.fixed(...)`로 시간을 결정론적으로 고정할 수 있다.

## 결정 (Decision)
**후보 C를 채택한다.**

1. **모든 시각은 `Instant`(UTC)로 다룬다.** DB에도 UTC로 저장한다(`refresh_tokens.expires_at` 등). 지역 타임존 변환은 표시(응답/로그) 계층에서만 한다.
2. **현재시각은 `new Date()`/`Instant.now()`를 코드에 직접 쓰지 않고, 주입된 `java.time.Clock`에서 얻는다.** 운영 빈은 `Clock.systemUTC()`, 테스트는 `Clock.fixed(...)`.
3. 외부 라이브러리가 `Date`만 받는 **경계에서만** `Date.from(instant)`로 변환한다(예: jjwt의 `issuedAt`/`expiration`). 도메인/서비스 내부는 `Instant`를 유지한다.

## 결과 (Consequences)
- `Clock` 빈(`Clock.systemUTC()`)을 등록한다([TimeConfig](../../src/main/java/com/knockdog/auth/config/TimeConfig.java)).
- 시간 의존 로직은 `Clock.fixed(...)`로 결정론적 단위 테스트가 가능해진다(예: JWT 만료).
- JPA는 `Instant`를 표준 매핑하므로 엔티티 타임스탬프도 `Instant`로 통일한다.
- 서버·DB는 UTC 가동을 전제로 한다(컨테이너 `TZ=UTC` 권장).
