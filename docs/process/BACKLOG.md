# ExitCall Backlog

GitHub Issues are the source of truth for actionable work. This file is the maintained roadmap and priority map.

## Current phase

**P2 product-quality work is active.**

As of 2026-09-03:

- P1 implementation baseline is complete on `main`, but the Done/acceptance audit reopened a few verification/integration issues that were closed too early.
- Permanent Android identity is `com.mateuszbury.exitcall`.
- Reboot recovery is implemented and physically verified.
- The pre-P2 `MainActivity` hardening work from PR #71 / #72 is merged.
- PR #79 implements the final capability policy: Notifications are required, Full Screen Intent is recommended with CallStyle fallback, and exact-alarm access is required only for scheduling.
- #12 scheduler edit/cancel is merged and closed via PR #80.
- #13 Android system ringtone picker is physically verified, merged and closed via PR #81.
- #14 single-contact picker is active on PR #82.
- #15 phone validation/formatting is stacked on #14 on PR #83.
- #8, #51 and #59 were reopened for explicit verification instead of being left falsely complete.
- #54 was reopened because onboarding/demo and visual-variant integration are still part of its original acceptance scope.
- #9 remains the maintained regression safety net during P2 and the release-quality gate before the v1.0 release candidate.

Working rule: **one large task or at most two small tasks active at once**.

## Active sequence

Current recommended order:

`#14 single contact picker -> #15 phone validation/formatting -> #16/#17 Incoming UI polish -> #18 Back handling`

Targeted verification for reopened #8 / #51 / #59 can be folded into normal physical regression sessions rather than interrupting the active feature sequence.

---

## P1 — completed implementation baseline

Completed core behavior includes:

- persisted `CallConfig` and caller identity,
- one shared immediate/scheduled call pipeline,
- exact `AlarmManager` scheduling,
- scheduler persistence and reboot recovery,
- notification / Full Screen Intent capability handling,
- CallStyle notification fallback when FSI is unavailable,
- lock-screen Answer / Decline / Hang Up lifecycle,
- unanswered-call cleanup implementation,
- portrait-only Activity configuration,
- permanent production application ID,
- maintained physical regression suite.

Key completed issues: #1, #2, #3, #4, #5, #6, #7, #10, #11, #27, #38, #44-#50, #52 and #55.

### Reopened verification/follow-up issues

- [ ] #8 Final capability UX/policy: implementation is present; onboarding wording, TC-039 cleanup and targeted physical verification remain.
- [ ] #51 Unanswered timeout: 20-second cleanup implementation is present; targeted locked/unlocked/FSI-fallback verification remains.
- [ ] #59 Portrait lock: manifest implementation is present; targeted physical rotation verification remains.

These issues are not considered Done until their remaining acceptance criteria are explicitly verified.

---

## Pre-P2 — architecture hardening

- #35 Light `MainActivity` refactor.
  - PR #71 extracted capability/platform resolution and the capability dialog.
  - PR #72 extracted the Compose root and lightweight main-screen navigation state.
  - PR #73 followed with scheduler picker composition/performance cleanup.
  - No Navigation Compose, ViewModel/DI framework or second scheduler/call pipeline was introduced.

---

## P2 — product quality / important functionality

### Scheduler and Call Setup

- [x] #12 Edit/reschedule/cancel the single pending scheduled call. Merged via PR #80.
- [x] #13 Android system ringtone picker with persisted URI/name and system-default fallback. Merged via PR #81.
- [ ] #14 Privacy-friendly person-first single-contact picker; Android owns the contact list, no bulk address-book import. Active on PR #82.
- [ ] #15 International phone-number validation, normalization and formatting. Active on stacked PR #83.
- [ ] #76 Local custom voice recording and audio-file import; app recording M4A/AAC, import at least MP3 and M4A/AAC, local-only storage.

### Incoming Call / Active Call

- [ ] #16 Polish IncomingCallScreen layout and vertical positioning.
- [ ] #17 Polish Answer/Decline slider visuals and interaction.
- [ ] #18 Block Back navigation in lock-screen Incoming/Active flows.
- [ ] #19 Respect ExitCall vibration setting and Android DND/system behavior.
- [ ] #20 Bluetooth/headset audio-route support.
- [ ] #22 Finish ActiveCallScreen controls and remove prototype placeholders.
- [ ] #53 Configurable missed-call notification after unanswered timeout only.
- [ ] #54 Keep the EC marker and integrate its explanation into onboarding/demo; current marker implementation is complete, but #29/#36 integration and final regression remain.
- [ ] #58 Configurable unanswered-call timeout: 5s / 10s / 20s / Custom 5-60s, default 20s.

### Home / Settings

- [ ] #23 Production Settings, Help and app information.
- [ ] #30 Production Home dashboard: Next call, Caller, Sound, readiness and trial/Premium state.
- #24 Theme centralization is already merged; remaining visual polish belongs to normal P2 UI work.

### Reliability / testing

- [ ] #9 Maintain and execute the physical regression suite during P2; full P0 gate before release candidate.
- [ ] Update stale TC-039 wording in #9 to the final FSI-fallback policy before the #8 verification pass.
- [ ] #25 Add a small automated safety net for scheduling, state and persistence.
- [ ] #26 Build Android/OEM compatibility matrix.

---

## P3 — onboarding, monetization and release

- [ ] #29 Mandatory first-run onboarding with one ExitCall demo, capability education and EC-marker rule.
- [ ] #74 Local free-call trial: 2 configurable calls after demo; consume only on Answer or Decline; timeout/demo do not consume.
- [ ] #75 Google Play Billing one-time Premium entitlement; Play purchase state is authoritative when available.
- [ ] #77 Premium/paywall UX during onboarding and after trial exhaustion.
- [ ] #28 Review current Google Play requirements for FSI, exact alarms, foreground service and notifications before submission.
- [ ] #39 Maintain architecture/product/release documentation.
- [ ] #78 Final Google Play release package and production-readiness gate.
- [ ] #33 Lightweight post-call summary.
- [ ] #34 Configurable post-call exit behavior; Return to Android remains default.
- [ ] #36 Google Phone-like and Samsung-like presentation variants over one shared call engine.

#37 monetization-model decision is complete; delivery remains in #74 / #75 / #77 and related release work.

---

## P4 — post-v1.0 / icebox

- #21 Explore real speakerphone routing only if product need is demonstrated.
- #31 Localization framework and language selection.
- #32 Localized guided voice content.
- #40 Lightweight marketing experiments after product validation.
- #41 Selected Asian-market partner/distribution research.
- #42 Native iOS evaluation in Swift / SwiftUI after Android validation.
- #43 IP, branding and launch-protection review.

---

## Recently completed

### #12 — scheduled-call management

PR #80 added explicit edit/reschedule/cancel behavior for the single pending call:

- Home displays the persisted trigger time and changes its CTA to `Edit scheduled call` when appropriate.
- Schedule screen loads the existing date/time for editing.
- Rescheduling replaces the existing alarm using the same pending-call identity.
- `Cancel scheduled call` is presented on the Schedule screen as a destructive action.
- Cancel removes the AlarmManager `PendingIntent`, clears persisted schedule state and returns Home to `Not scheduled`.

### #13 — ringtone picker

PR #81 connected Call Setup to Android's system ringtone picker:

- selected ringtone URI and display name are stored in `CallConfig`,
- selection persists through `CallConfigStore`,
- immediate and scheduled calls use the same ringtone source,
- picker cancellation preserves the previous value,
- missing/unavailable saved ringtone falls back to the Android system default,
- normal Answer / Decline / timeout cleanup still stops ringtone playback.

---

## Current architecture observations

- Production package identity is `com.mateuszbury.exitcall`.
- `CallConfigStore` is the durable source of truth for caller identity, ringtone URI/name and vibration preference.
- `ScheduledCallStore` is the durable source of truth for the single future trigger timestamp.
- `ScheduledCallScheduler` owns exact-alarm registration, replacement, cancellation and recovery.
- Normal scheduling, reboot recovery, exact-alarm permission recovery and app-start recovery reuse one scheduled-call `PendingIntent` identity.
- Notifications are required for call delivery.
- Full Screen Intent improves lock-screen presentation but is not a hard requirement; high-priority CallStyle notification is the fallback.
- Exact-alarm access is required only for exact future scheduling, not for immediate calls.
- Immediate and scheduled ExitCalls converge on the same service/activity lifecycle.
- Markdown under `docs/` is the maintained living documentation; versioned DOCX files are specification snapshots.

## Explicit product decisions

- Do **not** bulk-import contacts; use Android's person-first system contact picker on demand and read only the selected person's required data.
- Do **not** add random-contact selection.
- Do **not** intentionally bypass Android DND/global vibration policy.
- Keep Call Setup separate from Settings.
- Keep one pending scheduled call in v1.0.
- v1.0 UI is English-only; additional UI/audio localization is post-v1.0.
- Onboarding includes 1 demo + 2 free configurable calls, then a one-time Premium unlock.
- Demo and unanswered timeout do not consume a free call; Answer or Decline consumes exactly one.
- Custom user audio remains local to the device.
- Default post-call behavior is Return to Android.
- Android ships first; iOS is a separate later platform project if product validation justifies it.
