> 생성: 2026-07-16 00:50 · 최종 수정: 2026-07-16 11:55

# GitHub 규칙 (이슈 · PR · 리뷰 · 머지)

GitHub 플랫폼에서의 협업 규칙. 로컬 git(커밋·브랜치)은 [git-rules.md](git-rules.md) 참조.

## 작업 흐름: 이슈 먼저 (Issue-driven)

**코드/문서를 만지기 전에 이슈로 문제를 먼저 정의한다.** 전체 프로세스(Milestone↔epic 매핑, 이슈→브랜치→PR, 이슈 자동 close 제약)는 **[issue-driven-workflow.md](issue-driven-workflow.md)** 에 정리돼 있다. 아래는 GitHub PR 단계의 세부 규칙이다.

- **PR 본문에 `Closes #<이슈>`** 를 넣어 이슈와 연결한다.
- `feature/*` → `epic/*` PR은 머지해도 이슈가 **자동으로 닫히지 않으므로**(default 브랜치가 아님) 머지 후 `gh issue close <N>`로 수동 close한다.

## PR 대상 (base)

```
feature/*  ──PR──▶  epic/*  ──PR──▶  main
```

- `feature/*` 작업은 소속 `epic/*`(예: `epic/auth`, `epic/project-setup`)로 PR.
- epic이 일단락되면 `epic/*` → `main`으로 PR.
- **`main`으로의 직접 push 금지** — 항상 PR을 통한다.

## PR 제목

- 커밋 컨벤션과 동일한 형식: `feat: ...`, `docs: ...`, `chore: ...`
- 한 기능 단위를 한 줄로 요약.

## PR 본문 템플릿

```markdown
## 개요
무엇을, 왜 했는지 1~3줄.

Closes #<이슈번호>

## 변경 사항
- 핵심 변경점 목록

## 검증
- 어떻게 동작/보안을 확인했는지 (빌드·테스트·수동 확인)

## 관련 문서
- 관련 ADR / docs 링크 (있으면)
```

- `feature/*` → `epic/*` PR은 `Closes #N`으로 이슈를 연결하되, 머지 후 이슈를 **수동 close**한다(위 제약 참조).
- `epic/*` → `main` PR은 그 epic에 속한 이슈/PR들을 요약해 링크한다.

## 리뷰 · 머지

- 1 PR = 1 기능 단위로 작게 유지 (리뷰 부담 최소화).
- 머지 후 feature 브랜치는 정리(삭제) 권장.
- LLM으로 생성한 변경이 포함되면 **동작·보안을 직접 검증**하고 그 내용을 PR "검증" 섹션 및 [llm-usage.md](llm-usage.md)에 남긴다.
