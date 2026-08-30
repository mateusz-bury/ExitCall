# ExitCall Backlog

GitHub Issues are the source of truth for actionable work. This file is the roadmap and priority map.

## Current focus

Work in this order unless a blocker forces a change:

1. **#24 — Finish visual theme polish.** Theme centralization is merged and the app looks coherent overall. Remaining device observations: DatePicker/TimePicker still show some purple Material accents, and the filled `Start call` CTA needs a final color/layout decision.
2. **#9 — Run the end-to-end locked/unlocked regression checklist.**
3. **#10 — Restore pending scheduled calls after device reboot.**
4. **#11 — Replace `com.example.exitcall` with the permanent production application ID before public release.**

Working rule: **one large task or at most two small tasks active at once**.

Recommended immediate sequence:

`polish #24 -> #9 -> #10 -> #11`

---

## P1 — MVP blockers / reliability-critical

### Active / next

- #9 Add end-to-end locked-device regression checklist.

### Remaining P1

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
- #24 Centralize ExitCall theme. **Core implementation merged and physically smoke-tested.** Remaining polish: remove purple Material accents from DatePicker/TimePicker and make a final visual decision for the filled `Start call` CTA.
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
- #39 Maintain architecture/release documentation. Specs v1.1-v1.3 and process/decision docs live under standard `docs/`; ongoing architecture/release updates remain tracked here.

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
- #5 Immediate and scheduled triggers now converge on the same service/activity call pipeline; automatic debug scheduling and the duplicate MainActivity call path are gone.
- #6 IncomingCallService and IncomingCallActivity use the saved CallConfig; caller identity, ringtone/vibration configuration and immediate/scheduled parity were physically verified.
- #7 Locked-device Full Screen Intent and unlocked-device call-style notification paths support working Answer/Decline actions.
- #8 Permission/capability UX is implemented and physically verified: notifications, Full Screen Intent and exact-alarm gating handle grant, denial, settings return and `Not now` paths without false success or loops.
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

These are not reasons to interrupt the active P1 sequence, but they should remain visible:

- persisted CallConfig is the shared source of truth for the real immediate and scheduled call flow and has passed physical verification.
- real scheduler state is persisted and displayed consistently on Home and ScheduleCallScreen; the scheduled call clears the stored next-call value after firing.
- permission/capability gating for notifications, Full Screen Intent and exact alarms is merged and physically verified.
- scheduled alarms are still lost after a full device reboot until #10 adds BOOT_COMPLETED restoration; persisted UI state alone must not be treated as reboot support.
- theme colors are centralized; remaining visual work is product polish, especially DatePicker/TimePicker accents and the final `Start call` treatment.
- repository documentation has a standard root `README.md` and `docs/` layout.
- local IDE metadata is ignored and no longer tracked.
- `CallScreen.kt` intentionally remains under `com.example.exitcall.screens`; UI composables live under the matching `com.example.exitcall.ui.screens` path.

## Explicit product decisions / not planned

- Do **not** bulk-import the address book. Use a single system contact picker on demand.
- Do **not** add random-contact selection.
- Do **not** intentionally bypass Android global vibration or Do Not Disturb behaviour.
- Keep Call Setup separate from Settings.
- Default post-call behaviour is to leave the call flow and return to Android.
- English is the source language for the initial product; broader localization is tracked separately.
- Android comes first; iOS is a separate later native project if the product validates.
