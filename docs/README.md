# ExitCall documentation

This directory contains the maintained project documentation for ExitCall.

## Current technical baseline

As of 2026-09-08, P2 product-quality work is active. Onboarding/demo, the first production Home dashboard and the Settings information surfaces are merged.

The merged baseline includes:

- production Android identity `com.mateuszbury.exitcall`,
- one shared immediate/scheduled call pipeline,
- scheduler persistence, edit/reschedule/cancel and physically verified reboot recovery,
- Notifications-required capability policy with optional FSI and CallStyle fallback,
- exact-alarm gating only for precise scheduled calls,
- privacy-friendly system contact selection and shared phone-number handling,
- global `AppSettings` for ringtone, vibration and configurable 5–60 s ring duration,
- scheduled snapshots of alert settings,
- unified incoming-call termination behavior,
- polished Incoming/Active call presentation,
- functional Active Call keypad, Mute, More and real Phone/Speaker/Bluetooth routing,
- mandatory first-run onboarding and one controlled ExitCall demo,
- guided demo audio on the communication route, with handset output, Bluetooth ringtone isolation and Mute accepted on the primary device,
- Settings tutorial replay that finishes without launching another demo,
- a production Home dashboard driven by existing call/configuration/readiness state,
- in-app privacy, safe-use, support, About and open-source notices.

PR #98 completed the onboarding/demo flow, PR #105 added the production Home dashboard foundation and PR #106 completed Settings privacy/safe-use/support/About plus open-source notices.

The immediate work frontier is: implement missed-call behavior (#53), continue with local custom audio (#76), then add local trial/access gating (#74). Focused route/lifecycle/portrait/capability checks and regression issue #9 remain the cross-cutting quality track.

The Markdown files below are maintained living documentation. Versioned DOCX specifications are historical snapshots.

## Architecture and decisions

- [`architecture/TECHNICAL_DOCUMENTATION.md`](architecture/TECHNICAL_DOCUMENTATION.md) - living component architecture, state ownership, scheduler/capability/call/demo lifecycles and technical boundaries
- [`architecture/DECISIONS.md`](architecture/DECISIONS.md) - maintained product and technical decisions, including capabilities, audio routing, onboarding, monetization and release boundaries
- [`architecture/SCHEDULER_LIFECYCLE.md`](architecture/SCHEDULER_LIFECYCLE.md) - exact scheduling, edit/reschedule/cancel, settings snapshots, Direct Boot recovery and scheduler invariants
- [`ONBOARDING_DEMO_AUDIT_2026-09-07.md`](ONBOARDING_DEMO_AUDIT_2026-09-07.md) - requirement-to-code audit and physical verification checklist for #29 / PR #98

## Process

- [`process/BACKLOG.md`](process/BACKLOG.md) - roadmap, current session focus, completed work and later v1.0 workstreams
- [`process/PROJECT_WORKFLOW.md`](process/PROJECT_WORKFLOW.md) - GitHub Project workflow and status conventions
- [`process/RELEASE_CHECKLIST.md`](process/RELEASE_CHECKLIST.md) - release checks for dependency notices, privacy accuracy, safe-use copy and support destinations

## Specifications

Historical specification snapshots:

- `specifications/ExitCall_MVP_Specification_v1.1_opt.docx`
- `specifications/ExitCall_MVP_Specification_v1.2_opt.docx`
- `specifications/ExitCall_MVP_Specification_v1.3_opt.docx`

## Source-of-truth order

1. GitHub Issues for actionable scope, execution records and acceptance criteria.
2. Maintained Markdown under `docs/` for current architecture, decisions and roadmap context.
3. Versioned DOCX files for historical specification snapshots.

When implementation or product decisions change materially, update the relevant issue and maintained Markdown in the same workstream.
