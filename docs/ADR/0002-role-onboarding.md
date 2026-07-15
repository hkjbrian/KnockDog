> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 00:50

# ADR-0002: 역할 결정 = 가입 후 온보딩

- **상태**: Accepted
- **결정일**: 2026-07-15

## 맥락 (Context)
서비스는 `ROLE_OWNER`(견주) / `ROLE_DIRECTOR`(원장)로 나뉘고, 홈 API가 role로 분기해야 한다. 그런데 카카오 로그인은 "누가 로그인했는가"만 알려줄 뿐 role은 알려주지 않는다. role을 언제/어떻게 확정할지 결정이 필요했다.

## 검토한 후보 (Candidates)
- **후보 A — 가입 후 온보딩**: 일단 카카오 로그인으로 가입하고, 신규 유저는 `role=null`("온보딩 미완료")로 두었다가 별도 API로 role을 확정. 실제 서비스에서 흔한 패턴, 백엔드가 깔끔.
- **후보 B — 버튼 분리(로그인 시작부터 role 지정)**: "견주로 시작 / 원장으로 시작"을 나눠 흐름에 role을 실어 보냄. 상태(state 파라미터 등)를 로그인 흐름 내내 끌고 다녀야 해 번거로움.

## 결정 (Decision)
**후보 A(가입 후 온보딩)를 채택.**
1. 카카오 로그인 → 신규 유저는 `role = null`로 저장.
2. `POST /api/auth/signup/role` (body `{"role":"OWNER"|"DIRECTOR"}`)로 role 확정 → JWT 재발급.
3. `role`이 없는 유저는 `/api/home` 접근 시 403, `/api/auth/signup/role`만 통과.

## 결과 (Consequences)
- `users.role`은 NULL 허용.
- JWT 발급 시 role 유무를 구분(온보딩 전 토큰엔 role 클레임 생략, 확정 후 재발급).
- `/api/home`에 role 필수 가드 필요.
