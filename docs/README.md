# ExitCall documentation

This directory contains the maintained project documentation for ExitCall.

## Current technical baseline

As of 2026-09-01 the P1 implementation baseline is physically verified. Reboot recovery is validated and the permanent Android identity is finalized as `com.mateuszbury.exitcall` on PR #70. P2 product-quality work begins after that PR is merged into `main`.

The Markdown architecture/process documents below are the maintained living documentation. The versioned DOCX specifications are historical specification snapshots and are not used as the day-to-day source of truth for implementation status.

## Architecture and decisions

- [`architecture/DECISIONS.md`](architecture/DECISIONS.md) - stable product and technical decisions, including production Android identity and P1 baseline
- [`architecture/SCHEDULER_LIFECYCLE.md`](architecture/SCHEDULER_LIFECYCLE.md) - exact-alarm scheduling, Direct Boot recovery, trigger lifecycle and verified reboot behavior

## Process

- [`process/BACKLOG.md`](process/BACKLOG.md) - roadmap, priority map and current P2 phase
- [`process/PROJECT_WORKFLOW.md`](process/PROJECT_WORKFLOW.md) - GitHub Project workflow and status conventions

## Specifications

- `specifications/ExitCall_MVP_Specification_v1.1_opt.docx`
- `specifications/ExitCall_MVP_Specification_v1.2_opt.docx`
- `specifications/ExitCall_MVP_Specification_v1.3_opt.docx`

GitHub Issues remain the source of truth for actionable work. The maintained Markdown documentation provides the current architecture, decisions and roadmap context around those issues.
