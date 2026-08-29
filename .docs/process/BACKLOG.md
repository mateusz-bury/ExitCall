# ExitCall Backlog

GitHub Issues are the source of truth for actionable work. This file is the roadmap and priority map.

## Current focus

Work in this order unless a blocker forces a change:

1. #5 — Unify immediate and scheduled call launch pipeline; remove the automatic short debug alarm from normal startup.
2. #3 — Persist CallConfig between app launches/process death.
3. #6 — Make IncomingCallService and the real call UI use the saved CallConfig.
4. #51 — Add unanswered-call timeout and guaranteed cleanup.
5. Then continue through the remaining P1 reliability work.

Working rule: **one large task or at most two small tasks active at once**.

---

## P1 — MVP blockers / reliability-critical

- #3 Persist CallConfig between app launches/process death.
- #4 Finish scheduler UI with DatePicker and TimePicker.
- #5 Unify immediate and scheduled call launch pipeline; remove automatic debug scheduling from normal startup.
- #6 Make IncomingCallService use saved CallConfig, including configured ringtone/vibration/caller identity.
- #8 Complete permission/capability UX for exact alarms, notifications and Full Screen Intent.
- #9 Add end-to-end locked-device regression checklist.
- #10 Restore scheduled calls after device reboot.
- #11 Replace `com.example.exitcall` with the permanent production application ID before public release.
- #51 Add unanswered-call timeout and guaranteed cleanup so ringtone/vibration cannot run indefinitely.

## P2 — MVP quality / important functionality

### Scheduler and configuration

- #12 Add edit/cancel controls for scheduled calls and show the real next scheduled call on Home.
- #13 Add Android system ringtone picker.
- #14 Add privacy-friendly single contact picker; no bulk contact import and no random-contact feature.
- #15 Add country-code support and phone-number formatting, starting with `+48` and extensible internationally.

### Incoming / active call UX

- #16 Polish IncomingCallScreen layout/vertical positioning.
- #17 Polish Answer/Decline slider visuals and interaction.
- #18 Block Back navigation in the lock-screen incoming/active call flow.
- #19 Respect system vibration/DND behaviour and the ExitCall vibration preference.
- #20 Add Bluetooth and headset audio-route support.
- #21 Add speakerphone mode.
- #22 Finish ActiveCallScreen controls and remove prototype placeholders.
- #23 Implement Settings screen and global preferences.
- #24 Centralize the matte-black + sage/pastel-green ExitCall theme and remove default Material purple styling; fix the glaring light outline around the filled `Start call` CTA observed on the Motorola test device.
- #53 Decide whether to show an optional missed-call notification after an unanswered timeout; never show it after Answer or explicit Decline.

### Reliability / release quality

- #25 Add automated tests for scheduling, state and persistence.
- #26 Build Android device/OEM compatibility test matrix.
- #28 Review current Google Play/Android requirements for exact alarm, foreground service, notification and Full Screen Intent capabilities before store release.

## P3 — Beta / product improvements

- #29 Build one ExitCall demo/onboarding call instead of slide onboarding; caller `ExitCall`, guided audio after Answer.
- #30 Add real Home dashboard statistics/status data.
- #31 Add language selection in Settings.
- #32 Add guided voice content in PL / EN / DE / ES.
- #33 Explore lightweight post-call summary.
- #34 Add configurable post-call exit behaviour, with return-to-Android remaining the default.
- #35 Refactor navigation/state architecture after core MVP call flow stabilizes.
- #36 Explore Google Phone / Samsung-style visual variants while keeping shared call logic.
- #37 Design and validate monetization model: working concept is 3 free calls total including demo, then paid access; working price idea 4.99 USD/EUR and 14.99 PLN.
- #38 Clean tracked IDE files and prototype leftovers from the repository.
- #39 Maintain concise architecture/release documentation as the project grows.

## P4 — Icebox / future / exploratory

- #40 Run lightweight marketing experiments after product validation: TikTok / Facebook / YouTube.
- #41 Explore partner/distribution strategy for selected Asian markets.
- #42 Explore native iOS version in Swift / SwiftUI after Android validation.
- #43 Review ExitCall IP, branding/name protection and launch protection before wider public marketing.

## Completed / validated

- #1 Answer -> ActiveCallScreen works in the real service/activity flow and was physically verified on 2026-08-28.
- #2 ActiveCallScreen -> Hang Up closes the call UI cleanly and was physically verified on 2026-08-28.
- #7 Locked-device Full Screen Intent and unlocked-device call-style notification paths both support working Answer/Decline actions; 6/6 physical tests passed on the primary Motorola device on 2026-08-28.
- #27 Product wording standardized across Home, Call Setup and public documentation.
- #44 Exact Alarm -> Receiver trigger works on a locked device.
- #45 Foreground service plays ringtone and vibration on lock screen and cleans them up when stopped.
- #46 Full Screen Intent wakes device and shows incoming call over lock screen.
- #47 Decline stops the incoming call and leaves the device locked.
- #48 Initial Home, Call Setup, Incoming Call and Active Call screens/navigation foundation implemented.
- #49 Branding, icon and splash-screen groundwork implemented.
- #50 Private GitHub repository and structured backlog/workflow created.
- #52 Stale IncomingCallScreen after Decline/Answer fixed; completed call state no longer remains as the broken call screen observed during testing.

## Explicit product decisions / not planned

- Do **not** bulk-import the address book. Use a single system contact picker on demand.
- Do **not** add random-contact selection.
- Do **not** intentionally bypass Android global vibration or Do Not Disturb behaviour.
- Keep Call Setup separate from Settings.
- Default post-call behaviour is to leave the call flow and return to Android.
- Use ExitCall / scheduled incoming call / safety call consistently in user-facing and public wording.
- Android comes first; iOS is a separate later native project if the product validates.
