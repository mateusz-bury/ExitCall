# ExitCall Decisions

This file records product and technical decisions that should remain stable unless new evidence justifies changing them.

## Production Android identity

Permanent Android application identity:

`com.mateuszbury.exitcall`

- Gradle `namespace` and `applicationId` should remain aligned to this value.
- Kotlin source packages should use the same root namespace.
- App-scoped action strings should use the production package identity rather than the former `com.example.exitcall` placeholder.
- Treat this identity as permanent before the first public Play release. Changing it later would create a different Android application identity rather than a normal app update.
- No migration is required from development builds using `com.example.exitcall`; those builds were never public production releases.

## Product language

Use **ExitCall**, **scheduled incoming call** or **safety call** depending on context.

Preferred primary action wording:

- `Start call`
- `Schedule a call`
- `Set up a call`

The product should feel discreet and credible rather than playful or prank-oriented.

## Product structure

- Call Setup is separate from Settings.
- Default behaviour after Hang Up is to leave the call flow and return to Android.
- Home may later show useful statistics/status, but this is not a core MVP blocker.

## Contacts and privacy

- Do not bulk-import the address book.
- Use Android's system contact picker for one contact on demand.
- Do not add a random-contact feature.

## Audio and system behaviour

- ExitCall may provide an app-level vibration preference.
- The app should respect Android's global vibration and Do Not Disturb behaviour rather than trying to bypass it.
- Ringtone selection should use Android's system ringtone picker.
- Bluetooth/headset routing and speakerphone are important later call-quality features.

## Core Android call flow

The shared target pipeline is:

`user action / exact alarm -> call launcher/receiver -> foreground call service -> ringtone + vibration -> high-priority call notification -> Full Screen Intent or heads-up fallback -> IncomingCallActivity -> Answer/Decline -> ActiveCallScreen -> Hang Up`

Immediate and scheduled calls should use the same pipeline and the same persisted `CallConfig`.

The app must work when the device is locked without unexpectedly unlocking it.

## Scheduling and reboot recovery

- Use `AlarmManager` exact alarms for scheduled ExitCalls where the product requires precise timing.
- Use Material/Android DatePicker and TimePicker for user input.
- Support one pending scheduled call in the MVP.
- Persist scheduler recovery metadata separately from the operating-system alarm registration.
- Store the pending trigger timestamp in device-protected storage so it is available to a Direct-Boot-aware recovery receiver.
- Restore still-future calls after `LOCKED_BOOT_COMPLETED`, `BOOT_COMPLETED` and exact-alarm permission restoration through the same `ScheduledCallScheduler` registration path.
- Reuse the same PendingIntent identity so repeated recovery is idempotent and does not create duplicate calls.
- Keep an app-start recovery attempt as an OEM/device fallback.
- Do not replay a call whose trigger time is already past.
- Keep normal `CallConfig`, ringtone settings and the call service/activity path in the existing credential-protected design.
- A pending incoming call must have a finite timeout so ringtone/vibration cannot run indefinitely.

The reboot-recovery implementation was physically verified on the primary Motorola device on 2026-09-01.

## P1 baseline and P2 transition

The P1 implementation baseline is considered physically stable after verification of:

- CallConfig persistence,
- immediate and scheduled call convergence,
- exact-alarm scheduling,
- notification / Full Screen Intent capability flow,
- lock-screen Answer / Decline / Hang Up lifecycle,
- scheduler state persistence and reboot recovery,
- permanent Android identity `com.mateuszbury.exitcall` on PR #70.

P2 may begin after the final merge of PR #70. The regression suite in #9 remains the maintained safety net and final release-quality gate rather than a blocker to starting P2. Controlled architecture hardening in #35 is part of P2 and must preserve the verified P1 behavior.

## UI direction

- Primary visual direction: matte black/dark surfaces with pastel/sage green accent.
- Move these colors into the real Material theme rather than duplicating constants across screens.
- Incoming Call should feel credible and familiar while remaining ExitCall's own interface.
- Device-specific Google Phone/Samsung-inspired variants may be explored later, but system/call logic must stay shared.

## Demo and onboarding

Prefer one practical ExitCall demo call over slide-based onboarding:

- caller displayed as `ExitCall`,
- after Answer, guided audio calmly demonstrates how to use the app to leave an uncomfortable situation.

## Languages

Initial target languages for UI and guided content:

- English
- Polish
- German
- Spanish

## Monetization

Current working hypothesis only — not final:

- 3 free calls total, including the demo,
- then paid access,
- rough price idea: 4.99 USD/EUR and 14.99 PLN.

Before implementation, decide whether the paid model is one-time purchase or subscription and define restore-purchase/offline behaviour.

## Platform strategy

- Android first.
- Validate the product before committing to iOS.
- If iOS is built, use native Swift / SwiftUI and first research iOS restrictions around lock-screen/call-like presentation.

## Growth

- Lightweight TikTok/Facebook/YouTube experiments are P4 until the product is stable.
- Asian-market partner/distribution exploration is P4 until the product is validated.

## Development workflow

- GitHub Issues are the source of truth for actionable tasks.
- Keep one large task or at most two small tasks active at once.
- New ideas go to Backlog/Icebox instead of interrupting the active milestone.
- Prefer controlled refactors protected by targeted regression coverage over broad rewrites.
