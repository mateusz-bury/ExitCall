# ExitCall Backlog

GitHub Issues are the source of truth for actionable work. This file is the roadmap and priority map.

## Current phase

**P1 implementation baseline is complete and physically verified.**

As of 2026-09-01:

- #10 reboot recovery is merged, closed and physically verified on the primary Motorola device.
- #11 production Android identity is implemented and physically verified on PR #70 with permanent ID `com.mateuszbury.exitcall`; only the final merge into `main` remains.
- #9 regression suite is defined and remains the maintained release-quality safety net. It no longer blocks starting P2; run relevant subsets after high-risk changes and the full suite before the MVP release candidate.
- #35 is reclassified as P2 architecture hardening rather than a gate before P2.

Working rule: **one large task or at most two small tasks active at once**.

### Recommended P2 opening sequence

After merging PR #70 and closing #11:

`#12 scheduler edit/cancel -> #13 ringtone picker -> #14 single contact picker -> #15 phone formatting/country codes`

Then continue through the remaining P2 functionality in small, testable slices. Schedule #35 when there is a clean architecture window rather than interrupting a partially implemented feature.

---

## P1 — MVP core baseline

### Status

**Complete / physically validated.**

P1 established the stable core needed to safely begin product-quality work:

- persisted CallConfig and caller identity,
- immediate and scheduled call convergence,
- exact-alarm scheduling,
- foreground service / notification / Full Screen Intent flow,
- lock-screen Answer / Decline / Hang Up lifecycle,
- capability/permission UX,
- scheduler persistence and reboot recovery,
- permanent Android application identity,
- maintained physical regression suite definition.

Administrative note: PR #70 is verified and pending final merge into `main`; #11 should be closed with that merge.

---

## P2 — MVP quality / important functionality

### Scheduler and configuration

- #12 Add explicit edit/cancel controls for the single pending scheduled call.
- #13 Add Android system ringtone picker.
- #14 Add privacy-friendly single contact picker; no bulk contact import and no random-contact feature.
- #15 Add country-code support and phone-number formatting, extensible internationally.

### Incoming / active call UX

- #16 Polish IncomingCallScreen layout/vertical positioning.
- #17 Polish Answer/Decline slider visuals and interaction.
- #18 Block Back navigation in the lock-screen incoming/active call flow.
- #19 Respect system vibration/DND behaviour and the ExitCall vibration preference.
- #20 Add Bluetooth and headset audio-route support.
- #21 Add speakerphone mode.
- #22 Finish ActiveCallScreen controls and remove prototype placeholders.
- #23 Implement Settings screen and global preferences.
- #24 Centralize ExitCall theme. **Core implementation merged and physically smoke-tested.** Remaining P2 polish: remove purple Material accents from DatePicker/TimePicker and make a final visual decision for the filled `Start call` CTA.
- #53 Decide whether to show an optional missed-call notification after an unanswered timeout.
- #54 ExitCall marker is implemented on incoming/active screens; keep open until demo/onboarding #29 teaches the marker rule.
- #58 Make the unanswered-call timeout configurable in Settings: 5s / 10s / 20s, default 20s.

### Architecture, reliability and release quality

- #9 Maintain and execute the MVP regression suite. Use targeted subsets during P2 and run the full suite before the MVP release candidate.
- #25 Add automated tests for scheduling, state and persistence.
- #26 Build Android device/OEM compatibility test matrix.
- #28 Review current Google Play/Android requirements for exact alarm, foreground service, notification and Full Screen Intent capabilities before store release.
- #35 Run controlled core architecture hardening without blocking P2 feature work or rewriting stable behavior.

## P3 — Beta / product improvements

- #29 Build one ExitCall demo/onboarding call instead of slide onboarding; caller `ExitCall`, guided audio after Answer, teach the EC marker.
- #30 Add real Home dashboard statistics/status data.
- #31 Add language selection in Settings and move user-facing text toward Android string resources.
- #32 Add guided voice content in PL / EN / DE / ES.
- #33 Explore lightweight post-call summary.
- #34 Add configurable post-call exit behaviour, with return-to-Android remaining the default.
- #36 Explore Google Phone / Samsung-style visual variants while keeping shared call logic.
- #37 Design and validate monetization model.
- #39 Maintain architecture/release documentation. Specs v1.1-v1.3 and maintained process/decision docs live under `docs/`.

## P4 — Icebox / future / exploratory

- #40 Run lightweight marketing experiments after product validation: TikTok / Facebook / YouTube.
- #41 Explore partner/distribution strategy for selected Asian markets.
- #42 Explore native iOS version in Swift / SwiftUI after Android validation.
- #43 Review ExitCall IP, branding/name protection and launch protection before wider public marketing.

## Completed / validated

- #1 Answer -> ActiveCallScreen works in the real service/activity flow and was physically verified.
- #2 ActiveCallScreen -> Hang Up closes the call UI cleanly and was physically verified.
- #3 CallConfig persistence survives app kill/relaunch and device restart; `CallConfigStore` is the durable configuration source.
- #4 Real scheduler is implemented and physically verified: exact alarm trigger, persisted next-call state, Home display, scheduler restore and post-fire clearing all work.
- #5 Immediate and scheduled triggers converge on the same service/activity call pipeline; automatic debug scheduling and the duplicate MainActivity call path are gone.
- #6 IncomingCallService and IncomingCallActivity use the saved CallConfig; caller identity, ringtone/vibration configuration and immediate/scheduled parity were physically verified.
- #7 Locked-device Full Screen Intent and unlocked-device call-style notification paths support working Answer/Decline actions.
- #8 Permission/capability UX is implemented and physically verified: notifications, Full Screen Intent and exact-alarm gating handle grant, denial, settings return and `Not now` paths without false success or loops.
- #10 Scheduled calls survive reboot through device-protected scheduler state, Direct-Boot-aware recovery and shared exact-alarm re-registration; physically verified on Motorola.
- #11 Production Android identity is finalized as `com.mateuszbury.exitcall` and physically verified on PR #70; final merge/issue closure pending.
- #27 Product wording standardized across Home, Call Setup and public documentation.
- #38 Repository cleanup completed: `.idea` removed from tracking, `.docs` moved to `docs/`, root README restored, UI screen paths aligned with packages and default example tests removed.
- #44 Exact Alarm -> Receiver trigger works on a locked device.
- #45 Foreground service plays ringtone and vibration on lock screen and cleans them up when stopped.
- #46 Full Screen Intent wakes device and shows incoming call over lock screen.
- #47 Decline stops the incoming call and leaves the device locked.
- #48 Initial Home, Call Setup, Incoming Call and Active Call UI foundation implemented.
- #49 Branding, icon and splash-screen groundwork implemented.
- #50 Private GitHub repository and structured backlog/workflow created.
- #51 Unanswered-call timeout and guaranteed service/UI cleanup implemented with a 20-second default.
- #52 Stale IncomingCallScreen after Decline/Answer fixed.
- #55 Duplicate incoming/active call flows fixed by unifying the launch pipeline.
- #59 MainActivity and IncomingCallActivity locked to portrait and verified.

## Current repository observations

- Permanent Android identity is `com.mateuszbury.exitcall`; source packages, namespace and application ID are aligned on PR #70.
- Persisted CallConfig is the shared source of truth for the real immediate and scheduled call flow and has passed physical verification.
- Scheduled trigger metadata is stored in device-protected storage and reboot recovery is physically verified.
- `ScheduledCallScheduler` owns normal and recovery exact-alarm registration; recovery uses a stable PendingIntent identity and remains idempotent.
- `ScheduledCallRecoveryReceiver` handles `LOCKED_BOOT_COMPLETED`, `BOOT_COMPLETED` and exact-alarm permission restoration; `MainActivity` provides an app-start OEM fallback.
- Permission/capability gating for notifications, Full Screen Intent and exact alarms is physically verified.
- Scheduler lifecycle/recovery is documented under `docs/architecture/SCHEDULER_LIFECYCLE.md`.
- #24 visual details remain intentionally deferred to P2 polish.
- Repository documentation uses the standard root `README.md` and `docs/` layout.
- Local IDE metadata is ignored and no longer tracked.
- `CallScreen.kt` remains under `com.mateuszbury.exitcall.screens`; UI composables live under `com.mateuszbury.exitcall.ui.screens`.

## Explicit product decisions / not planned

- Do **not** bulk-import the address book. Use a single system contact picker on demand.
- Do **not** add random-contact selection.
- Do **not** intentionally bypass Android global vibration or Do Not Disturb behaviour.
- Keep Call Setup separate from Settings.
- Keep the MVP scheduler to one pending call at a time; multiple scheduled calls are unnecessary complexity unless future product evidence justifies them.
- Default post-call behaviour is to leave the call flow and return to Android.
- English is the source language for the initial product; broader localization is tracked separately.
- Android comes first; iOS is a separate later native project if the product validates.
