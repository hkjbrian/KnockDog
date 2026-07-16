> 생성: 2026-07-16 11:55 · 최종 수정: 2026-07-16 12:30

# 이슈 기반 개발 프로세스 (이슈→브랜치→PR)

**코드/문서를 만지기 전에 이슈로 문제를 먼저 정의한다.** 이슈 → 브랜치 → PR 순서를 지킨다.
로컬 git(커밋·브랜치명)은 [git-rules.md](git-rules.md), GitHub PR·리뷰·머지 세부는 [github-rules.md](github-rules.md) 참조.

## 프로세스

```
[큰 틀]  Milestone 생성  +  epic/* 브랜치 (main에서 분기)
   │
[문제정의] Issue 작성(작업 템플릿) → Milestone 지정 → 라벨
   │
[브랜치]  feature/#<이슈>-<slug>  ← 해당 epic 브랜치에서 분기
   │
[작업]    커밋 (Conventional Commits, 트레일러 금지)
   │
[PR]      base = epic 브랜치, 본문에 "Closes #<이슈>"
   │
[머지]    feature PR 머지 → 이슈 수동 close
   │
[통합]    Milestone 일단락 → epic/* → main PR
```

## 매핑 규칙

- **Milestone = 큰 틀**(기존 epic 개념). epic 브랜치와 1:1 대응 (예: Milestone `인증` ↔ `epic/auth`).
- **Issue = feature 단위 문제 정의.** 반드시 Milestone에 소속시킨다. [`.github/ISSUE_TEMPLATE/task.md`](../.github/ISSUE_TEMPLATE/task.md)(작업 템플릿)로 배경/목표/체크리스트를 채운다.
- **브랜치명 = `feature/#<이슈>-<slug>`** (예: `feature/#12-jwt-provider`). `<slug>`는 kebab-case.
- **PR 본문에 `Closes #<이슈>`** 를 넣어 이슈와 연결한다.

## ⚠️ 이슈 자동 close 제약 (중요)

GitHub의 `Closes #N` 자동 close는 **PR이 default 브랜치(`main`)로 머지될 때만** 동작한다.
우리는 `feature/*` → **`epic/*`** 로 PR을 올리므로 **자동으로 닫히지 않는다.**

- `Closes #N`은 **링크·추적용**으로 그대로 적어 둔다.
- feature PR이 머지되면 그 시점에 기능이 완성되므로 **이슈를 수동으로 닫는다**: `gh issue close <N>`.

## 이 방식을 택한 이유

- **이슈 단위**: feature 단위만 이슈로 발행하고 큰 틀은 Milestone으로 묶는다. epic 이슈까지 계층화(sub-issue)하는 방식은 혼자 진행하는 사전과제 규모엔 관리 부담이 커서 배제.
- **연결 방식**: 브랜치명에 이슈번호를 넣으면(`feature/#12-...`) 브랜치만 봐도 어떤 이슈인지 드러나고, PR `Closes`로 링크가 걸린다. PR 본문에만 `Closes`를 쓰는 방식은 가볍지만 브랜치만으로는 추적이 안 돼 배제.
- **이슈 템플릿**: 기능 중심 과제라 단일 '작업' 템플릿 하나로 충분. bug 템플릿은 거의 쓰이지 않아 두지 않음.
