# ExitCall Backlog

GitHub Issues are the source of truth for actionable work. This file is the maintained roadmap and priority map.

## Current phase

**P2 product-quality work is active.**

As of 2026-09-04:

- P1 implementation baseline is complete and stable enough for continued P2 feature work.
- Production Android identity is `com.mateuszbury.exitcall`.
- Reboot recovery is implemented and physically verified.
- Final capability policy is implemented: Notifications required, FSI recommended with CallStyle fallback, exact alarms required only for precise scheduling.
- #12 scheduler edit/reschedule/cancel is complete via PR #80.
- #13 Android system ringtone picker is complete via PR #81.
- #14 privacy-friendly single-phone contact picker is complete via PR #82.
- #15 phone-number validation/normalization/formatting is complete via PR #83.
- #58 production Settings foundation + configurable timeout is complete via PR #84.
- incoming-call termination consistency was hardened in PR #85.
- #16/#17 Incoming Call layout/slider polish is complete through PR #87.
- #18 intentional call-flow Back behavior is complete through PR #85.
- navigation/header consistency and Home Settings gear are complete through PR #86.
- Active Call presentation is polished, but #22 remains open for behavior of Keypad/Mute/Speaker/More.
- #23 remains open only for the real Settings/help/information integrations that were intentionally left as placeholders.
- #9 remains the maintained physical regression safety net and release-quality gate.

Working rule: **one large task or at most two small tasks active at once**.

## Recommended next sequence

Current recommended order:

`#22 Active Call control behavior -> #53 missed-call notification -> #76 custom local audio -> #30 production Home dashboard`

Notes:

- #23 is completed incrementally as #53 and #29 land; do not create duplicate Settings logic.
- #19 should be closed only after focused system-mode/DND verification, even though the app-level vibration setting and cleanup path are already implemented.
- #8 and #59 remain verification/follow-up work and can be folded into normal physical regression sessions.
- #9 needs a maintenance pass because several old exclusions/expectations became stale after #13/#14/#15/#58 and the final FSI policy.

---

## P1 — completed implementation baseline

Completed core behavior includes:

- persisted caller identity,
- one shared immediate/scheduled call pipeline,
- exact `AlarmManager` scheduling,
- scheduler persistence and reboot recovery,
- notification / Full Screen Intent capability handling,
- CallStyle notification fallback when FSI is unavailable,
- lock-screen Answer / Decline / Hang Up lifecycle,
- unified unanswered/decline/back cleanup,
- portrait-only Activity configuration,
- permanent production application ID,
- maintained physical regression suite.

Key completed issues include #1-#7, #10-#18, #27, #38, #44-#52, #55 and #58 where applicable to their final scope.

### Remaining P1 verification/follow-up

- [ ] #8 Final capability UX/policy: implementation exists; onboarding wording, regression-suite cleanup and targeted physical verification remain.
- [ ] #59 Portrait lock: implementation and prior physical evidence exist; issue remains open until its explicit targeted pass is recorded/accepted.

#51 timeout cleanup is closed; configurable timeout ownership moved to #58 and is now implemented.

---

## Pre-P2 — architecture hardening

- #35 Light `MainActivity` refactor.
  - PR #71 extracted capability/platform resolution and the capability dialog.
  - PR #72 extracted the Compose root and lightweight main-screen navigation state.
  - PR #73 extracted heavy scheduler picker composition/performance work.
  - No Navigation Compose, ViewModel/DI framework or duplicate scheduler/call pipeline was introduced.
- PR #85 later unified Incoming Call termination signals and removed stale lifecycle divergence.

---

## P2 — product quality / important functionality

### Scheduler and Call Setup

- [x] #12 Edit/reschedule/cancel the single pending scheduled call. PR #80.
- [x] #13 Android system ringtone picker. PR #81.
- [x] #14 Privacy-friendly Android single-phone contact picker; no broad address-book import. PR #82.
- [x] #15 International phone-number validation, normalization and formatting. PR #83.
- [x] #58 Production Settings foundation + global ringtone/vibration + configurable 5–60 s ring duration. PR #84.
- [ ] #76 Local custom voice recording and audio-file import; local-only storage, M4A/AAC recording, MP3 + M4A/AAC import.

### Incoming Call / Active Call

- [x] #16 Polish IncomingCallScreen layout and vertical positioning. PR #87.
- [x] #17 Polish Answer/Decline slider visuals and native-style call icon. PR #87.
- [x] #18 Intentional Back behavior: Incoming Back = decline/cleanup, Active Back = Return to Android. PR #85.
- [ ] #19 Finish/verify system vibration and Do Not Disturb behavior. Implementation is largely present; focused system-mode verification remains.
- [ ] #20 Bluetooth/headset audio-route support.
- [ ] #22 Finish ActiveCallScreen controls. Visual baseline is complete via PR #87; Keypad/Mute/Speaker/More behavior remains.
- [ ] #53 Configurable missed-call notification after unanswered timeout only.
- [ ] #54 EC marker onboarding/demo and future visual-variant integration. Current marker remains implemented and preserved by PR #87.

### Home / Settings

- [ ] #23 Complete Settings Help/information integrations. Foundation is complete via #58/PR #84; remaining work is missed-call toggle, functional tutorial/help, licenses/privacy/safe-use/support/about.
- [ ] #30 Production Home dashboard: Next call, Caller, Sound, readiness and trial/Premium state.
- [x] Shared navigation polish: Home Settings gear + common Back top bar for Setup/Schedule/Settings. PR #86.
- #24 theme centralization is complete; PR #87 additionally aligned call surfaces/system navigation bar with the shared palette.

### Reliability / testing

- [ ] #9 Maintain and execute the physical regression suite during P2; update stale cases/exclusions after the recent feature wave.
- [ ] #25 Expand the automated safety net for scheduling, state and persistence. Pure unit tests now exist for phone-number handling and ring-duration validation, but the broader task remains open.
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

#37 monetization-model decision is complete; delivery remains in #74/#75/#77 and related release work.

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

### #14 / #15 — contact and phone identity quality

PR #82 uses Android's system phone-data picker and intentionally avoids `READ_CONTACTS`/bulk enumeration. PR #83 adds one shared phone-number validation/normalization/formatting layer and keeps the incoming notification channel silent so `IncomingCallService` remains the single owner of ringtone/vibration alerting.

### #58 — production Settings and configurable timeout

PR #84 introduced `AppSettings` / `AppSettingsStore` as the source of truth for global ringtone, vibration and ring duration. Legacy ringtone/vibration values are migrated once from `CallConfigStore`. Ring duration supports 5/10/20-second presets plus Custom 5–60 seconds. Settings exposes live Notifications/Exact Alarm/FSI status and explicit Coming soon placeholders for later work.

A new scheduled call captures an `AppSettings` snapshot. Therefore changing global ringtone/vibration/timeout later does not silently alter an already pending scheduled call.

### PR #85 — incoming lifecycle consistency

Incoming termination now uses one maintained cleanup contract. Notification Decline, timeout and Android Back cannot leave a stranded incoming Activity or alerting service behind. Active Call behavior is isolated from late incoming termination signals.

### #16 / #17 and PR #87 — call-screen presentation polish

Incoming and Active surfaces were rebuilt against the supplied native call references while keeping ExitCall's own design language and `EC` marker. The call UI now uses centralized theme colors, a dark system navigation bar, vector call/action icons and a wide Hang Up control. Active secondary controls remain visually complete but intentionally non-functional until #22.

### PR #86 — navigation/header consistency

Home uses a Settings gear action. Call Setup, Schedule and Settings share one reusable back-arrow top bar so icon, size, placement and behavior are consistent.

---

## Current architecture observations

- Production package identity is `com.mateuszbury.exitcall`.
- `CallConfigStore` is the durable source of truth for caller identity and phone number. Legacy ringtone/vibration fields remain only as migration compatibility until cleanup is safe.
- `AppSettingsStore` is the durable source of truth for global ringtone URI/name, vibration and ring duration.
- `ScheduledCallStore` persists the single future trigger plus the `AppSettings` snapshot captured when it was scheduled.
- `ScheduledCallScheduler` owns exact-alarm registration, replacement, cancellation and recovery.
- Normal scheduling, reboot recovery, exact-alarm permission recovery and app-start recovery reuse one scheduled-call `PendingIntent` identity.
- Immediate calls use current `AppSettings`; scheduled calls use their captured alert-settings snapshot.
- Caller identity remains current `CallConfig` at delivery time for both immediate and scheduled calls.
- Notifications are required for call delivery.
- Full Screen Intent improves lock-screen presentation but is not a hard requirement; high-priority CallStyle notification is the fallback.
- Exact-alarm access is required only for exact future scheduling.
- Immediate and scheduled ExitCalls converge on the same service/activity lifecycle.
- Markdown under `docs/` is the maintained living documentation; versioned DOCX files are specification snapshots.

## Explicit product decisions

- Do **not** bulk-import contacts; use Android's system single-phone picker on demand.
- Do **not** add random-contact selection.
- Do **not** intentionally bypass Android DND/global vibration policy.
- Keep caller identity Call Setup separate from global Settings.
- Global ringtone, vibration and ring duration belong to Settings.
- Keep one pending scheduled call in v1.0.
- v1.0 UI is English-only; additional UI/audio localization is post-v1.0.
- Onboarding includes 1 demo + 2 free configurable calls, then a one-time Premium unlock.
- Demo and unanswered timeout do not consume a free call; Answer or Decline consumes exactly one.
- Custom user audio remains local to the device.
- Default post-call behavior is Return to Android.
- Android ships first; iOS is a separate later platform project if product validation justifies it.
