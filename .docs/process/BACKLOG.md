# ExitCall Backlog

GitHub Issues are the source of truth for actionable work. This file is the roadmap and priority map.

## Current focus

Work in this order unless a blocker forces a change:

1. **#6 — Verify saved CallConfig in the real call flow.** Implementation from PR #64 is merged: IncomingCallActivity and IncomingCallService now read the persisted setup. Next session: physically verify caller identity, ringtone and vibration for immediate and scheduled calls.
2. **#4 — Finish verification of the real scheduler.** Exact Alarm scheduling is working physically. Pending branch adds persisted next-call state, restores the current timestamp in ScheduleCallScreen, displays it on Home and clears it after the alarm fires. Next session: run the scheduler state checklist and close #4 if clean.
3. **#8 — Complete permission/capability UX.** Do this after #4/#6 are physically signed off so failures can be tested against the real flow.
4. **#9 — Run the end-to-end locked/unlocked regression checklist.**
5. **#10 — Restore pending scheduled calls after device reboot.**
6. **#11 — Replace `com.example.exitcall` with the permanent production application ID before public release.**

Working rule: **one large task or at most two small tasks active at once**.

Recommended immediate sequence:

`verify #6 + #4 -> #8 -> #9 -> #10 -> #11`

---

## P1 — MVP blockers / reliability-critical

### Active / next

- #6 Make IncomingCallService and IncomingCallActivity use saved CallConfig, including configured ringtone/vibration/caller identity. **Implementation merged in PR #64; physical end-to-end verification pending.**
- #4 Finish scheduler UI and exact-alarm integration. **Exact Alarm trigger physically works. Persisted next-call state and Home/Schedule display are implemented on `fix/scheduled-call-state`; physical verification pending.**

### Remaining P1

- #8 Complete permission/capability UX for exact alarms, notifications and Full Screen Intent.
- #9 Add end-to-end locked-device regression checklist.
- #10 Restore scheduled calls after device reboot.
- #11 Replace `com.example.exitcall` with the permanent production application ID before public release.

## P2 — MVP quality / important functionality

### Scheduler and configuration

- #12 Add explicit edit/cancel controls for the pending scheduled call.
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
- #3 CallConfig persistence survives app kill/relaunch and device restart; `CallConfigStore` is the durable configuration source.
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

- PR #64 connected persisted CallConfig to IncomingCallActivity and IncomingCallService; physical verification of all configured fields is still pending.
- Exact Alarm scheduling is now connected and physically confirmed to trigger a call.
- `ScheduledCallStore` is being added as the source of truth for the pending next-call timestamp; Home and ScheduleCallScreen will read the same persisted value.
- scheduled alarms are still lost after a full device reboot until #10 adds BOOT_COMPLETED restoration; persisted UI state alone must not be treated as reboot support.
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
