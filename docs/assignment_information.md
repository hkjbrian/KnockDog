> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 00:50

# 과제 정보 (Assignment Information)

## 과제 개요
- **제목**: [사전과제] 반려견 유치원 인증/권한 API 구축
- **도메인**: 반려견 유치원 (견주 ↔ 원장 연결 플랫폼)
- **범위**: Kakao 소셜 로그인(OAuth 2.0) → 자체 JWT 발급/검증 → 역할(원장/견주) 기반 홈 분기
- **제출 기한**: 협의

## 기술 스택 (버전 필수 준수)
- 언어: Java 17 이상 (Java 21 허용)
- 프레임워크: Spring Boot 3.3.x
- 보안: Spring Security 6.3.x (OAuth2 Client + Resource Server)
- 빌드: Gradle 8.x
- DB: MySQL 8.0 + Spring Data JPA
- JWT: jjwt 0.12.x 또는 nimbus-jose-jwt
- 형상관리: GitHub
- 시크릿(카카오 키, JWT 서명키, DB 비번) 하드코딩 금지

## 기능 요구사항
- **Kakao 로그인**: Authorization Code 방식, 최초 로그인 시 가입 처리, 인증 성공 후 자체 JWT 발급
- **JWT**: Access/Refresh 발급, 클레임에 사용자 식별자+역할 포함, Bearer 검증, 만료·재발급·로그아웃 처리
- **역할 분기(핵심)**: `ROLE_OWNER`(견주) / `ROLE_DIRECTOR`(원장). 동일 홈 API 호출 시 JWT의 role에 따라 응답이 달라져야 함
  - 견주: 내 반려견 목록 / 주변 유치원 등
  - 원장: 등록된 원생(반려견) 목록 / 등원 현황 등
  - 데이터는 더미 수준이어도 OK, 핵심은 role 기반 분기 흐름

## 홈 호출 예시
```
GET /api/home  (Authorization: Bearer <JWT>)
```
- 견주 응답: `{ "role": "OWNER", "myDogs": [...], "nearbyKindergartens": [...] }`
- 원장 응답: `{ "role": "DIRECTOR", "kindergartenName": "...", "enrolledDogs": [...], "todayAttendance": 5 }`

## Git 규칙 (필수)
- 작업 시작 전 GitHub repo 생성 후 공유 (repo 없이 착수 금지)
- 브랜치 전략: `main` ← `epic/auth` ← `feature/*`
  - `feature/kakao-login`, `feature/jwt`, `feature/role-home` 등
  - 한 PR = 한 기능 단위로 잘게 (기능 분할은 본인 판단), `main` 직접 push 금지
- 커밋 컨벤션: `feat:`, `fix:`, `docs:` 등

## 문서화 (1장 이상 필수)
- 전체 흐름도 (카카오 로그인 → JWT → 권한 검증 → 역할 분기)
- 기술 선택 근거, 실행 방법 + 환경변수 목록, API 명세 요약

## LLM 적극 활용 (필수)
- 설계·코드·테스트·문서에 적극 활용
- 활용 내역(어떤 작업에 어떻게 썼고 어떻게 검증했는지) 문서에 기록 → `docs/llm-usage.md`
- 생성 코드 맹신 금지, 동작·보안 직접 검증

## 제출 방법
- GitHub repo URL / 문서 / 제출 기한
