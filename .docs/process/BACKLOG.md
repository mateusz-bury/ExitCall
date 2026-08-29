# ExitCall Backlog

GitHub Issues are the source of truth for actionable work. This file is the roadmap and priority map.

## Current focus

Work in this order unless a blocker forces a change:

1. **#3 — Persist CallConfig between app launches/process death.** Implementation has started: `CallConfigStore` exists and `MainActivity` now loads/saves it. Next: verify persistence and remove remaining default-config reads from the real call flow.
2. **#6 — Make IncomingCallService and IncomingCallActivity use the saved CallConfig.** This is the next prepared task after #3 verification.
3. **#4 — Finish the real scheduler.** Scheduler UI, DatePicker, TimePicker and past-time validation are already implemented. Pause here until #3/#6 are complete, then connect the selected time to exact AlarmManager, persist the next call and expose it on Home.
4. **#8 — Complete permission/capability UX.** Do this after the real scheduling path exists so permission failures can be tested against the real user flow.
5. **#9 — Run the end-to-end locked/unlocked regression checklist.**
6. **#10 — Restore pending scheduled calls after device reboot.**
7. **#11 — Replace `com.example.exitcall` with the permanent production application ID before public release.**

Working rule: **one large task or at most two small tasks active at once**.

Recommended immediate sequence:

`#3 -> #6 -> #4 -> #8 -> #9 -> #10 -> #11`

---

## P1 — MVP blockers / reliability-critical

### Active / next

- #3 Persist CallConfig between app launches/process death. **In progress.** `CallConfigStore` and MainActivity load/save are on `main`; real call Activity/Service still need the shared persisted config and verification.
- #6 Make IncomingCallService and IncomingCallActivity use saved CallConfig, including configured ringtone/vibration/caller identity. **Ready after #3 readback verification.**
- #4 Finish scheduler UI and exact-alarm integration. **UI foundation complete; paused behind #3/#6.** Date/time selection and validation work, but no real alarm/persisted next-call state yet.

### Remaining P1

- #8 Complete permission/capability UX for exact alarms, notifications and Full Screen Intent.
- #9 Add end-to-end locked-device regression checklist.
- #10 Restore scheduled calls after device reboot.
- #11 Replace `com.example.exitcall` with the permanent production application ID before public release.

## P2 — MVP quality / important functionality

### Scheduler and configuration

- #12 Add edit/cancel controls for scheduled calls and show/manage the real next scheduled call on Home.
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
- #24 Centralize ExitCall theme. **Partially implemented:** central Material palette is now in `Color.kt`/`Theme.kt`; legacy screens still repeat local colors and need final device polish.
- #53 Decide whether to show an optional missed-call notification after an unanswered timeout.
- #54 ExitCall marker is implemented on incoming/active screens; keep open until demo/onboarding #29 teaches the marker rule.
- #58 Make the unanswered-call timeout configurable in Settings: 5s / 10s / 20s, default 20s.

### Reliability / release quality

- #25 Add automated tests for scheduling, state and persistence.
- #26 Build Android device/OEM compatibility test matrix.
- #28 Review current Google Play/Android requirements for exact alarm, foreground service, notification and Full Screen Intent capabilities before store release.

## P3 — Beta / product improvements

- #29 Build one ExitCall demo/onboarding call instead of slide onboarding; caller `ExitCall`, guided audio after Answer, teach the EC marker.
- #30 Add real Home dashboard statistics/status data.
- #31 Add language selection in Settings and move user-facing text toward Android string resources.
- #32 Add guided voice content in PL / EN / DE / ES.
- #33 Explore lightweight post-call summary.
- #34 Add configurable post-call exit behaviour, with return-to-Android remaining the default.
- #35 Refactor navigation/state architecture after core MVP call flow stabilizes.
- #36 Explore Google Phone / Samsung-style visual variants while keeping shared call logic.
- #37 Design and validate monetization model.
- #38 Repository cleanup. Current findings include tracked `.idea/`, hidden `.docs/` instead of standard `docs/`, no root README, package/path mismatches for screen files, an unused duplicate `MainScreen` in `HomeScreen`, stale imports and default example tests.
- #39 Maintain architecture/release documentation. Specs v1.1-v1.3 and process/decision docs exist; root README, docs links and current architecture status still need cleanup.

## P4 — Icebox / future / exploratory

- #40 Run lightweight marketing experiments after product validation: TikTok / Facebook / YouTube.
- #41 Explore partner/distribution strategy for selected Asian markets.
- #42 Explore native iOS version in Swift / SwiftUI after Android validation.
- #43 Review ExitCall IP, branding/name protection and launch protection before wider public marketing.

## Completed / validated

- #1 Answer -> ActiveCallScreen works in the real service/activity flow and was physically verified.
- #2 ActiveCallScreen -> Hang Up closes the call UI cleanly and was physically verified.
- #5 Immediate and scheduled triggers now converge on the same service/activity call pipeline; automatic debug scheduling and the duplicate MainActivity call path are gone.
- #7 Locked-device Full Screen Intent and unlocked-device call-style notification paths support working Answer/Decline actions.
- #27 Product wording standardized across Home, Call Setup and public documentation.
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

These are not reasons to interrupt the active P1 sequence, but they should remain visible:

- `CallConfigStore` is now the beginning of the persistent source of truth, but `IncomingCallActivity` and `IncomingCallService` still bypass it.
- Scheduler UI exists and produces a validated `triggerTimeMillis`, but the value is currently only logged.
- Home still displays `Next call: Not scheduled` as a placeholder.
- `IncomingCallService` still hardcodes notification identity, system-default ringtone and vibration behavior.
- central Material theme now uses ExitCall colors, while several screens still use duplicated local constants.
- `.docs/README.md` is stale and its relative links no longer match the new subfolder layout.
- the repository has no root `README.md` at the moment.
- `.idea/` is still tracked despite ignore rules.

## Explicit product decisions / not planned

- Do **not** bulk-import the address book. Use a single system contact picker on demand.
- Do **not** add random-contact selection.
- Do **not** intentionally bypass Android global vibration or Do Not Disturb behaviour.
- Keep Call Setup separate from Settings.
- Default post-call behaviour is to leave the call flow and return to Android.
- English is the source language for the initial product; broader localization is tracked separately.
- Android comes first; iOS is a separate later native project if the product validates.
