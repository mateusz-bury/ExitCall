# ExitCall Decisions

This file records product and technical decisions that should remain stable unless new evidence justifies changing them.

## Production Android identity

Permanent Android application identity:

`com.mateuszbury.exitcall`

- Gradle `namespace` and `applicationId` stay aligned to this value.
- Kotlin packages use the same root namespace.
- App-scoped action strings use the production package identity.
- The former `com.example.exitcall` builds were development-only and are treated as a separate Android application identity.

## Product language and positioning

Use **ExitCall**, **scheduled incoming call**, **incoming call flow** or **safety call** depending on context.

Preferred primary actions:

- `Start call`
- `Schedule a call`
- `Edit scheduled call`
- `Set up a call`

The product should feel discreet, credible and safety-oriented rather than playful or prank-oriented.

## Product structure

- Call Setup is separate from global Settings.
- Home is a compact readiness/configuration dashboard rather than an analytics-heavy screen.
- Default post-call behavior is to return to Android.
- v1.0 supports one pending scheduled call at a time.

## Core Android call pipeline

Immediate and scheduled calls share one pipeline and the same persisted `CallConfig`:

`user action / exact alarm -> launcher/receiver -> IncomingCallService -> ringtone + vibration -> high-priority CallStyle notification -> Full Screen Intent when available -> IncomingCallActivity -> Answer/Decline -> ActiveCallScreen -> Hang Up`

There must not be separate business-logic engines for immediate calls, scheduled calls or visual call-screen variants.

## Capability policy

### Notifications

Notifications are a hard requirement for ExitCall delivery.

- If notifications are denied/unavailable, Start and Schedule do not launch a call.
- No ringtone/vibration-only hidden delivery is treated as success.
- The user is given a clear path to restore notification capability.

### Full Screen Intent

Full Screen Intent is strongly recommended but is **not** a hard requirement.

- When available and permitted by Android, use the full-screen lock-screen presentation.
- When unavailable/denied, continue through the high-priority CallStyle notification with working Answer/Decline actions.
- Missing FSI must not block an otherwise valid immediate or scheduled ExitCall.

### Exact alarms

Exact-alarm capability is required only for exact future scheduling.

- Immediate calls remain independent from exact-alarm access.
- Scheduling must not persist a false successful state if exact-alarm registration cannot be completed.

## Scheduling and lifecycle

- Use `AlarmManager.setExactAndAllowWhileIdle()` for the maintained exact scheduling path.
- Use Material/Android DatePicker and TimePicker for user input.
- `ScheduledCallStore` holds the durable future trigger timestamp.
- `AlarmManager` is the execution mechanism, not the durable source of truth.
- `ScheduledCallScheduler` owns registration, replacement, cancellation and recovery.
- Rescheduling reuses the one scheduled-call `PendingIntent` identity so the previous pending call is replaced rather than duplicated.
- Explicit cancellation removes the alarm `PendingIntent` and clears persisted schedule state.
- The Schedule screen is the management surface for editing and cancelling a pending call.
- A fired call clears the persisted trigger immediately.
- Expired scheduled calls are discarded rather than replayed late.

## Reboot recovery

- Pending scheduler metadata is stored in device-protected storage.
- `ScheduledCallRecoveryReceiver` restores still-future calls after `LOCKED_BOOT_COMPLETED`, `BOOT_COMPLETED` and exact-alarm permission restoration.
- Recovery reuses the same scheduler and `PendingIntent` identity and remains idempotent.
- `MainActivity` performs an app-start recovery attempt as an OEM fallback.
- Full `CallConfig` and the normal call service/activity path remain in the existing credential-protected design.
- Reboot recovery was physically verified on the primary Motorola device.

## Contacts and privacy

- Do not bulk-import the address book.
- Use Android's single-contact system picker on demand.
- Read/store only the selected contact data required for caller presentation.
- Do not add random-contact selection.

## Ringtone selection

The production ringtone path uses Android's system ringtone picker.

- `Call Setup -> Change` opens the system picker.
- Selected ringtone URI and display name are stored in `CallConfig` and persisted by `CallConfigStore`.
- Immediate and scheduled calls read the same saved ringtone source.
- Cancelling the picker preserves the previous selection.
- If a stored ringtone can no longer be resolved, `IncomingCallService` falls back to the Android system default.
- Normal Answer, Decline, Hang Up and unanswered-timeout cleanup must stop ringtone playback.

This path was physically verified and merged in PR #81.

## Vibration and Android system behavior

- ExitCall may expose an app-level vibration preference.
- The app does not intentionally bypass Android global vibration or Do Not Disturb behavior.
- Alert cleanup must stop ExitCall-generated vibration on all termination paths.

## Custom call audio

v1.0 supports local user-provided audio as a separate feature from ringtone selection.

- User may record audio with the phone microphone or choose an existing audio file.
- App recordings target M4A/AAC where supported.
- Import supports at least common MP3 and M4A/AAC files that Android can decode.
- Audio remains local to the device; no cloud/backend upload is required.
- User can preview, replace and delete the selected custom audio.
- Custom audio starts approximately 3 seconds after Answer.
- If no usable custom audio exists, use the bundled default English voice track.
- Playback stops immediately on Hang Up/end of call.

Tracked in #76.

## Incoming-call timeout and missed-call behavior

- Default unanswered timeout is 20 seconds.
- v1.0 Settings will offer 5s / 10s / 20s / Custom 5-60s.
- Answer and Decline do not create a missed-call notification.
- Unanswered timeout may create the configured missed-call notification when that setting is enabled.
- Missed-call notification uses normal notification sound semantics, not the incoming-call ringtone.

Tracked in #53 and #58.

## EC marker / real-call distinction

- Incoming and Active surfaces show a discreet fixed `EC` marker.
- The marker is the primary visual rule for distinguishing an ExitCall from a genuine incoming phone call in v1.0.
- Mandatory onboarding/demo teaches: marker visible = ExitCall; marker absent = treat it as a genuine call.
- The marker remains predictable across supported call-screen visual variants.

## Onboarding and demo

v1.0 uses mandatory first-run onboarding.

- Onboarding explains what ExitCall does and the Android capabilities it needs.
- Notifications are explained as required.
- FSI is explained as recommended with notification fallback.
- Exact-alarm access is explained for scheduled calls and may be requested contextually.
- Onboarding includes one demo call with caller `ExitCall`.
- Demo uses the same EC marker position/style as normal calls.
- Default guided content is one emotionally neutral English voice track.
- Demo audio begins after Answer and stops on Hang Up.
- Demo can be replayed later from Settings/Help.

Tracked in #29.

## Monetization and entitlement

Final v1.0 direction from the 2026-09-02 product rebaseline:

- Onboarding includes 1 demo call.
- After onboarding, a new installation has exactly 2 free configurable calls.
- Demo does not consume a free call.
- Unanswered timeout does not consume a free call.
- A normal call consumes one free use only when the user performs Answer or Decline.
- Capability failure, cancelled scheduling or configuration changes do not consume a free call.
- When free uses reach 0, usable call execution is gated behind Premium.
- Local persistence is sufficient for the free-use counter in v1.0; clear-data/reinstall resetting the trial is an accepted tradeoff.
- Premium is a non-consumable one-time Google Play purchase.
- Google Play owned-purchase state is authoritative when synchronization is available; a local verified cache may support resilient offline UX.
- Working price is 4.99 USD / 4.99 EUR with Play-localized regional pricing; PLN may use a regional price such as 14.99 PLN.

Tracked in #74, #75 and #77.

## Language and localization

- v1.0 application UI is English-only.
- User-facing strings should remain resource-based and ready for later localization.
- Polish, German and Spanish are post-v1.0 candidates rather than v1.0 release blockers.
- Additional guided voice languages are also post-v1.0.

Tracked in #31 and #32.

## Active Call controls and audio routing

- Remove prototype-only `Audio Emoji`.
- Keypad, Mute, Speaker and More must not remain dead controls.
- Mute/Speaker may be stateful presentation controls in v1.0 where no real telephony operation exists.
- Bluetooth/headset routing is a real supported behavior and is tracked separately.
- Real speakerphone routing is not a v1.0 requirement unless later evidence justifies it.

## Visual direction

- Matte black/dark surfaces with pastel/sage green accent are the primary design language.
- Destructive actions may use the centralized pastel error/red role.
- Call presentation may offer Google Phone-like and Samsung-like variants, but only as presentation layers over one shared call engine.
- Do not guess OEM behavior unless there is a reliable deterministic rule.

## Platform strategy

- Android first.
- Validate the Android product before committing to iOS.
- If iOS is pursued, treat it as a separate native Swift / SwiftUI implementation and research iOS platform restrictions first.

## Release strategy

- Do not protect an arbitrary calendar release date by cutting required product quality.
- Google Play release readiness includes capability-policy review, billing verification, privacy/data-safety declarations, store assets, closed/internal testing as required, release AAB verification and final P0 regression.
- Final Go/No-Go is explicit.

Tracked in #28 and #78.

## Development workflow

- GitHub Issues are the source of truth for actionable work.
- Maintained Markdown under `docs/` is the living architecture/product/process documentation.
- Versioned DOCX specifications are historical snapshots unless explicitly refreshed.
- Keep one large task or at most two small tasks active at once.
- New ideas enter Backlog/Icebox rather than interrupting the active milestone.
- Prefer controlled refactors protected by targeted regression coverage over broad rewrites.
