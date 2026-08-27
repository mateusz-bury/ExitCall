# ExitCall Project Workflow

This file defines the intended GitHub Project setup and daily operating rules.

## Status columns

Use these columns in the main board:

1. **Backlog** — known work that is not selected for immediate execution.
2. **Ready** — the next task(s) that are prepared to start.
3. **In Progress** — work actively being implemented now.
4. **Testing** — implementation is complete and needs validation.
5. **Done** — completed and validated work.
6. **Icebox** — future/exploratory ideas that must not interrupt MVP delivery.

## Project fields

### Priority

- `P1` — MVP blocker / reliability-critical
- `P2` — MVP quality / important functionality
- `P3` — beta / post-MVP improvement
- `P4` — future / exploratory

### Type

- Feature
- Bug
- Technical
- UX
- Testing
- Research
- Product
- Release
- Documentation

### Area

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

### Size

- `S` — small, usually suitable as a light task
- `M` — medium, normally one focused work item
- `L` — large, should usually be broken down before implementation

### Release

- `MVP`
- `Beta`
- `Post-MVP`

## Recommended views

### Flow

Board grouped by Status. Show all six status columns.

### MVP

Filter:

- `Release = MVP`
- exclude `Done`
- exclude `Icebox`

Sort Priority with P1 first.

### Today / Current

Show only:

- `Ready`
- `In Progress`
- `Testing`

Keep this view intentionally small.

### Icebox

Filter `Status = Icebox`.

### Done

Filter `Status = Done`, newest completed work first.

## Working rules

- Keep at most **one issue In Progress**.
- A normal session should contain **one large task or at most two small tasks**.
- New ideas go to Backlog or Icebox instead of interrupting current P1 work.
- A task moves to Testing when implementation is finished but physical/device validation is still needed.
- A task moves to Done only after its acceptance criteria are satisfied.

## Current sequence

Near-term execution order:

`#1 -> #2 -> #5 -> #3/#6 -> #4 -> #7/#8/#51 -> #9`

P2 work can be used occasionally as a small secondary task when it does not open a large technical branch.

Preferred user-facing actions are:

- `Start call`
- `Schedule a call`
- `Set up a call`
