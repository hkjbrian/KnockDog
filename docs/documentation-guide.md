> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 00:50

# 문서 작성 가이드 (Documentation Guide)

이 프로젝트의 `docs/` 문서를 작성·관리하는 규칙. **AI(Claude)와 사용자 모두 이 규칙을 따른다.**

## 1. 결정 기록 워크플로우 (가장 중요)

**논의를 통해 무언가 확정되면, 관련 문서를 반드시 남긴다.**

- 방식: **자동 기록 후 알림.** 결정이 확정되면 Claude가 관련 문서/ADR을 자동으로 작성·갱신하고, "무엇을 어디에 기록했는지" 사용자에게 알린다. 잘못됐으면 되돌린다.
- **강제 장치**: `.claude/settings.json`의 PreToolUse 훅이 `git commit` / `gh pr create` 직전에 발동해, 문서 동기화(ADR·llm-usage·시간정보 갱신) 확인을 요구한다. 커밋/PR 전 문서 일치는 **필수**.
- 대상 분류:
  - **설계/아키텍처 결정** (프로젝트 전반에 영향) → `docs/ADR/`에 주제별 ADR 파일로
  - **작업 규칙/컨벤션** → `docs/git-rules.md`, `docs/github-rules.md`, 본 가이드 등
  - **참조 정보** (API, 흐름, 스택) → `docs/architecture-overview.md`, `docs/tech-stack.md` 등
  - **AI 활용 내역** → `docs/llm-usage.md` (과제 필수)

## 2. 시간정보 헤더 (모든 docs 문서 필수)

모든 `docs/*.md`는 **가장 앞 줄에 시간정보**를 넣는다. 제목(H1)보다 먼저.
**날짜뿐 아니라 정확한 시각(시:분)까지** 기록한다. 기준 시간대는 KST.

```markdown
> 생성: YYYY-MM-DD HH:MM · 최종 수정: YYYY-MM-DD HH:MM

# 문서 제목
```

- 예) `> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 00:50`
- 문서를 의미 있게 수정할 때 "최종 수정" 시각을 갱신한다.

## 3. ADR (docs/ADR/)

ADR(Architecture Decision Record) = 프로젝트 전반에 영향을 주는 결정 1건 = 파일 1개.
번호가 붙는 **순차적(append-only) 결정 로그**다. (번호 없는 다른 docs는 순서 무관한 주제별 참조 문서)

- 파일명: `NNNN-슬러그.md` (예: `0001-oauth2login.md`), 4자리 일련번호.
- **작성 시 [adr-template.md](ADR/adr-template.md)를 복사해 사용한다.**
- 섹션 순서: **맥락 → 검토한 후보 → 결정 → 결과.** (후보를 먼저 제시하고, 그 뒤에 결정이 나오도록)
- `docs/ADR/README.md`에 인덱스를 유지한다.

## 4. 문서 인덱스는 CLAUDE.md

`CLAUDE.md`가 전체 문서의 인덱스 겸 핵심 규칙을 담는다. 새 문서를 추가하면 `CLAUDE.md` 인덱스도 갱신한다.
