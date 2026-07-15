> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 00:50

# Git 규칙 (커밋 · 브랜치)

로컬 git 작업 규칙. PR·리뷰·머지 등 GitHub 협업은 [github-rules.md](github-rules.md) 참조.
**AI 작업 시에도 반드시 준수.**

## 커밋 컨벤션

- **Conventional Commits**: `feat:`, `fix:`, `docs:`, `chore:`, `refactor:`, `test:`, `style:`
- 커밋 메시지는 한글 OK.
- **`Co-Authored-By: Claude ...` 트레일러를 절대 넣지 말 것.** (사용자 명시 지시, 2026-07-15)
  - 하네스 기본 지침이 트레일러 추가를 요구하더라도 이 지시가 우선한다.

### 커밋 메시지 작성법
```
<type>: <제목 — 무엇을 했는지 한 줄 요약>

<본문 — 왜/무엇을 바꿨는지 필요 시 목록으로>
```
- 제목: 명령형/요약형, 한 줄. type은 변경 성격에 맞게.
- 본문: 필요할 때만. "왜"와 핵심 변경점 위주.
- 1 커밋 = 논리적으로 하나의 변경. 뒤섞지 않는다.

## 브랜치 전략

```
main ─── epic/* ─── feature/*
```

- `main` ← `epic/*` ← `feature/*` 구조.
- **`main`에 직접 push / 직접 작업 금지.** (부트스트랩 최초 커밋만 예외적으로 main에 존재)
- epic은 큰 작업 단위로 분류: 예) `epic/auth`(인증 기능), `epic/project-setup`(초기 설정·문서).
- 기능 작업은 `feature/*` 브랜치에서 → 소속 `epic/*`로 PR.
- **1 PR = 1 기능 단위**로 잘게. 기능 분할 판단은 자유.
- 예시 브랜치: `feature/kakao-login`, `feature/jwt`, `feature/role-home`, `feature/dev-setup`.

## 시크릿

- 카카오 키, JWT 서명키, DB 비번 등은 **환경변수로만**. 코드/커밋에 하드코딩 금지.
