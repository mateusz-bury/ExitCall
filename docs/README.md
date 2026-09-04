# ExitCall documentation

This directory contains the maintained project documentation for ExitCall.

## Current technical baseline

As of 2026-09-04, P1 is complete and P2 product-quality work is active.

Current merged baseline includes:

- production Android identity `com.mateuszbury.exitcall`,
- one shared immediate/scheduled call pipeline,
- scheduler persistence, edit/reschedule/cancel and physically verified reboot recovery,
- Notifications-required capability policy with Full Screen Intent as recommended rather than mandatory,
- high-priority CallStyle notification fallback when FSI is unavailable,
- exact-alarm gating only for scheduled calls,
- privacy-friendly system contact selection and phone-number validation,
- global `AppSettings` for ringtone, vibration and configurable 5–60 s ring duration,
- settings snapshots for already scheduled calls,
- section-based production Settings with live capability status,
- unified incoming-call termination behavior,
- polished Incoming/Active call presentation and consistent app navigation headers.

The maintained P2 frontier has moved from basic configuration/polish into remaining behavior: Active Call controls (#22), Settings/help completion (#23), Home dashboard (#30), missed-call behavior (#53), custom local audio (#76), regression maintenance (#9), and remaining verification/integration work.

The Markdown architecture/process documents below are the maintained living documentation. Versioned DOCX specifications are historical snapshots and are not the day-to-day source of truth for implementation status.

## Architecture and decisions

- [`architecture/TECHNICAL_DOCUMENTATION.md`](architecture/TECHNICAL_DOCUMENTATION.md) - living technical documentation v1.5: component architecture, configuration/settings ownership, scheduler snapshots, Android capabilities, call lifecycle, UI structure and current technical boundaries
- [`architecture/DECISIONS.md`](architecture/DECISIONS.md) - maintained product and technical decisions, including capability policy, settings ownership, scheduler behavior, audio direction, onboarding, monetization and release boundaries
- [`architecture/SCHEDULER_LIFECYCLE.md`](architecture/SCHEDULER_LIFECYCLE.md) - exact scheduling, edit/reschedule/cancel behavior, settings snapshots, Direct Boot recovery, trigger lifecycle and scheduler invariants

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
