# ExitCall Decisions

This file records product and technical decisions that should remain stable unless new evidence justifies changing them.

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

## Scheduling

- Use `AlarmManager` exact alarms for scheduled ExitCalls where the product requires precise timing.
- Use Material/Android DatePicker and TimePicker for user input.
- Persist the scheduled call so it can be restored after reboot.
- A pending incoming call must have a finite timeout so ringtone/vibration cannot run indefinitely.

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
- New ideas go to Backlog/Icebox instead of interrupting current P1 work.
- Avoid large architecture refactors until the core MVP call flow is stable.
