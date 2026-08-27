# ExitCall GitHub Project Workflow

This is the intended configuration for the ExitCall GitHub Project.

## Status field

Use these columns/status values:

1. **Backlog** — accepted work, not ready/current yet.
2. **Ready** — sufficiently defined and ready to start.
3. **In Progress** — actively being worked on.
4. **Testing** — implementation is ready; physical/device/regression verification is in progress.
5. **Done** — completed and validated.
6. **Icebox** — future/exploratory ideas that must not interrupt MVP work.

WIP rule: keep **maximum 1 issue in In Progress**. If a task is very small, a second small task may be active, but do not create a queue of half-finished work.

## Project fields

### Priority — single select

- P1 — MVP blocker / reliability-critical
- P2 — MVP quality / important functionality
- P3 — Beta / post-MVP improvement
- P4 — future / exploratory

### Type — single select

Recommended values:

- Feature
- Bug
- Technical
- UX
- Testing
- Research
- Product
- Release
- Documentation

### Area — single select

Recommended values:

- Call Flow
- Incoming Call
- Active Call
- Scheduler
- Call Setup
- Settings
- Audio
- Android System
- Home
- UI / Theme
- Repository
- Release / Growth
- iOS

### Size — single select

- S — small, normally one focused session
- M — several connected steps / likely more than one session
- L — large or uncertain; should be decomposed before starting when possible

### Release — single select

- MVP
- Beta
- Post-MVP

## Views

### 1. Flow

**Layout:** Board

**Group by:** Status

Columns:
`Backlog -> Ready -> In Progress -> Testing -> Done -> Icebox`

Purpose: full project flow.

### 2. MVP

**Layout:** Board or Table

**Filter:**
- Release = MVP
- Status != Done
- Status != Icebox

Sort by:
1. Priority ascending (P1 first)
2. Size ascending where useful

Purpose: this is the primary planning view. If an item is not here, it should usually not interrupt current MVP work.

### 3. Today / Current

**Layout:** Board or Table

**Filter:**
- Status = Ready OR In Progress OR Testing

Purpose: the only view needed when sitting down for a focused development session.

### 4. Icebox

**Layout:** Table

**Filter:** Status = Icebox

Purpose: store interesting ideas without allowing them to compete with current development.

### 5. Done

**Layout:** Table

**Filter:** Status = Done

Sort: newest completed first.

Purpose: visible record of progress and completed milestones.

## Initial status assignment

- **Ready:** #1
- **Backlog:** #2–#39 (move the next dependency to Ready only when it is actually actionable)
- **Icebox:** #40–#43
- **Done:** #44–#50

Suggested near-term sequence:

`#1 -> #2 -> #5 -> #3/#6 -> #4 -> #7/#8 -> #9`

Do not pull P2 visual polish ahead of unresolved P1 call reliability unless a P1 task is genuinely blocked.

## Issue format

Each actionable issue should contain, where applicable:

- Priority
- Type
- Area
- Size
- Release
- Suggested status
- Current code state / context
- Acceptance criteria checklist

The Project fields remain the operational source for filtering; metadata in issue bodies keeps context readable even outside the Project view.