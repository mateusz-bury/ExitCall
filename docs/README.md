# ExitCall documentation

This directory contains the maintained project documentation for ExitCall.

## Current technical baseline

As of 2026-09-03, P1 is complete and P2 product-quality work is active.

Current merged baseline includes:

- production Android identity `com.mateuszbury.exitcall`,
- shared immediate/scheduled call pipeline,
- scheduler persistence and physically verified reboot recovery,
- Notifications-required capability policy with Full Screen Intent as recommended rather than mandatory,
- high-priority CallStyle notification fallback when FSI is unavailable,
- exact-alarm gating only for scheduled calls,
- scheduler edit/reschedule/cancel management from #12 / PR #80,
- Android system ringtone picker from #13 / PR #81.

The next planned P2 configuration feature is #14, the privacy-friendly single-contact picker.

The Markdown architecture/process documents below are the maintained living documentation. Versioned DOCX specifications are historical specification snapshots and are not the day-to-day source of truth for implementation status.

## Architecture and decisions

- [`architecture/DECISIONS.md`](architecture/DECISIONS.md) - maintained product and technical decisions, including capability policy, scheduler behavior, ringtone/audio direction, onboarding, monetization and release boundaries
- [`architecture/SCHEDULER_LIFECYCLE.md`](architecture/SCHEDULER_LIFECYCLE.md) - exact scheduling, edit/reschedule/cancel behavior, persistence, Direct Boot recovery, trigger lifecycle and scheduler invariants

## Process

- [`process/BACKLOG.md`](process/BACKLOG.md) - current roadmap, completed work, active P2 sequence and later v1.0 workstreams
- [`process/PROJECT_WORKFLOW.md`](process/PROJECT_WORKFLOW.md) - GitHub Project workflow and status conventions

## Specifications

Historical specification snapshots:

- `specifications/ExitCall_MVP_Specification_v1.1_opt.docx`
- `specifications/ExitCall_MVP_Specification_v1.2_opt.docx`
- `specifications/ExitCall_MVP_Specification_v1.3_opt.docx`

## Source-of-truth order

1. GitHub Issues for actionable task scope and acceptance criteria.
2. Maintained Markdown under `docs/` for current architecture, product decisions and roadmap context.
3. Versioned DOCX files for historical specification snapshots.

When implementation or product decisions change materially, update the relevant issue and maintained Markdown documentation in the same workstream.
