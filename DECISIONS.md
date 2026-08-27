# ExitCall Decisions

This file records decisions already made so rejected ideas do not repeatedly return to the active backlog.

## Product language

- Avoid `fake call` in user-facing and public-facing wording.
- Prefer `ExitCall`, `scheduled incoming call`, or `safety call` depending on context.
- Preferred primary action copy: `Start call`, `Schedule a call`, `Set up a call`.

## Product structure

- **Call Setup** is a dedicated area and should remain separate from **Settings**.
- Default Hang Up behaviour is to leave the ExitCall call flow and return to Android.
- A post-call summary is optional/post-MVP and must not slow down the user's exit.

## Contacts and privacy

- Do not import the full address book.
- Use a system picker to choose one contact on demand.
- Do not implement random contact selection.

## Audio / system behaviour

- ExitCall may have its own vibration preference.
- Do not intentionally bypass Android global vibration or Do Not Disturb choices.
- System ringtone selection is desirable.
- Bluetooth/headset and speakerphone support are backlog features, not blockers for finishing the core Answer/Hang Up flow.

## Scheduler / Android call flow

Target shared flow:

`user action / exact alarm -> call launcher/receiver -> foreground call service -> ringtone + vibration -> high-priority call notification -> Full Screen Intent or heads-up fallback -> IncomingCallActivity -> Answer/Decline -> ActiveCallScreen -> Hang Up`

Immediate and scheduled calls should use the same core pipeline and saved CallConfig.

## UI / branding

- Core visual direction: matte/dark black surfaces + pastel/sage green accents.
- Incoming/active call UI should feel believable and phone-like without tying core behaviour to one OEM skin.
- Google Phone / Samsung-like variants, if ever implemented, should be UI variants on top of shared call logic.

## Demo / onboarding

- Prefer one practical demo call over a sequence of onboarding slides.
- Demo caller: `ExitCall`.
- Guided audio starts after the user answers and demonstrates how the experience can help the user leave an uncomfortable situation.

## Localization

Initial language direction:
- EN
- PL
- DE
- ES

Both UI localization and guided voice content are later product layers; they must not delay the core Android call reliability milestone.

## Monetization — working hypothesis, not final

- Working concept: 3 free calls total, including 1 demo + 2 user calls, then paid access.
- Working pricing idea discussed: 4.99 USD/EUR and 14.99 PLN.
- Billing model and pricing must be validated before implementation.

## Platform strategy

- Android/Kotlin/Compose is the current product focus.
- iOS is a later validation-dependent project and should be evaluated as a native Swift/SwiftUI implementation.

## Growth

- Lightweight marketing experiments are P4 and should start only after the product is stable enough to demonstrate.
- Asian-market partner/distribution work is exploratory P4 and must not compete with Android MVP delivery.

## Development workflow

- GitHub Issues are the actionable source of truth.
- Keep at most one large task or two small tasks active.
- New ideas go to Backlog/Icebox instead of interrupting current work.