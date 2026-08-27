# ExitCall Backlog

This file is the roadmap. Actionable work should also exist as GitHub Issues.

## P1 — MVP blockers

- Complete `Answer -> ActiveCallScreen` transition from the lock-screen flow.
- Complete `ActiveCallScreen -> Hang Up` and return to Android while keeping the device locked where appropriate.
- Test full flow on a locked device: scheduled trigger -> incoming screen -> Answer -> Active Call -> Hang Up.
- Persist `CallConfig` so caller settings survive leaving/restarting the app.
- Finish scheduler UX using Android `DatePicker` and `TimePicker`.
- Validate exact-alarm permissions and fallback behaviour across supported Android versions.

## P2 — MVP quality

- Improve IncomingCallScreen positioning to better resemble a native call UI.
- Improve phone-handle/slider visuals and interaction.
- Add country-code support for phone numbers, starting with `+48`, with extensible formatting for other countries.
- Add/finish `BackHandler` behaviour on Incoming and Active Call screens.
- Respect system vibration / DND settings and the app-level `Vibrate on fake call` toggle.
- Add ringtone selection from system ringtones.
- Add single-contact picker integration for choosing one caller on demand; avoid bulk contact import.
- Add audio-route support for Bluetooth/headsets.
- Add speakerphone mode.
- Add Full Screen Intent fallback: heads-up notification with Answer/Decline when full-screen presentation is unavailable.

## P3 — Product improvements

- Demo/onboarding call with caller name `ExitCall` and a guided example audio track.
- Add Home-screen usage statistics.
- Add language selection in Settings.
- Add PL / EN / DE / ES voice content.
- Add post-call summary concept if it proves useful.
- Refactor navigation/state architecture as the codebase grows.
- Add basic automated tests for scheduling, state transitions, and configuration persistence.

## P4 — Future / exploratory

- Lightweight marketing campaign experiments: TikTok / Facebook / YouTube.
- Explore partner/distribution strategy for Asian markets.
- Explore iOS version and Swift/SwiftUI implementation after Android MVP validation.
- Explore monetization after free usage allowance: current working idea is 3 free calls including demo, then paid access.

## Completed / validated

- AlarmManager exact alarm works with locked screen.
- CallAlarmReceiver starts the call flow.
- IncomingCallService works as foreground service on lock screen.
- System ringtone works on lock screen.
- Vibration works on lock screen.
- Required permissions handled for exact alarm, foreground service, Full Screen Intent and notifications.
- Notification works.
- Full Screen Intent wakes the screen.
- IncomingCallActivity displays IncomingCallScreen over the lock screen.
- Decline stops ringtone/vibration, closes the screen and leaves the phone locked.

## Working rule

Keep only one large task or at most two small tasks in active development at once. New ideas go to the backlog instead of interrupting current work.