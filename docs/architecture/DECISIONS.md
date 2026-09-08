# ExitCall Decisions

This file records product and technical decisions that should remain stable unless new evidence justifies changing them.

## Production Android identity

Permanent Android application identity:

`com.mateuszbury.exitcall`

- Gradle `namespace` and `applicationId` stay aligned to this value.
- Kotlin packages use the same root namespace.
- App-scoped action strings use the production package identity.
- Former `com.example.exitcall` builds are development-only and are treated as a separate Android application identity.

## Product language and positioning

Use **ExitCall**, **scheduled incoming call**, **incoming call flow** or **safety call** depending on context.

Preferred primary actions:

- `Start call`
- `Schedule a call`
- `Edit scheduled call`
- `Set up a call`

The product should feel discreet, credible and safety-oriented rather than prank-oriented.

## Product structure

- Call Setup owns caller identity and phone number.
- Global Settings owns ringtone, vibration and ring duration.
- Home is a compact readiness/configuration dashboard rather than an analytics-heavy screen.
- Default post-call behavior is Return to Android.
- v1.0 supports one pending scheduled call at a time.
- Secondary main-app screens use one consistent back-arrow header; Home exposes Settings through a gear action.

## Core Android call pipeline

Immediate and scheduled calls share one call engine:

`user action / exact alarm -> launcher/receiver -> IncomingCallService -> ringtone + vibration -> high-priority CallStyle notification -> Full Screen Intent when available -> IncomingCallActivity -> Incoming/Active screen -> Hang Up/exit`

There must not be separate business-logic engines for immediate calls, scheduled calls or future visual call-screen variants.

## Configuration ownership

### `CallConfig`

`CallConfigStore` is the durable source of truth for caller presentation data:

- caller name,
- phone number.

Legacy ringtone/vibration fields remain in `CallConfig` only for compatibility with the one-time migration introduced with #58. New product behavior must not treat them as the current settings source of truth.

### `AppSettings`

`AppSettingsStore` is the durable source of truth for global call settings:

- ringtone URI,
- ringtone display name,
- vibration enabled/disabled,
- ring duration in seconds.

Current ring-duration rules:

- presets: 5 / 10 / 20 seconds,
- Custom: integer 5–60 seconds,
- default: 20 seconds.

## Scheduling snapshot semantics

Scheduling intentionally uses mixed ownership:

- **when** the call fires is stored by `ScheduledCallStore`,
- caller name/phone remain current `CallConfig` at delivery time,
- ringtone/vibration/ring duration are captured as an `AppSettings` snapshot when the call is scheduled.

Therefore changing ringtone/vibration/timeout after scheduling does **not** silently modify the already pending call. Editing/rescheduling creates a fresh settings snapshot.

The scheduler still supports only one pending call and reuses one `PendingIntent` identity.

## Capability policy

### Notifications

Notifications are a hard requirement for ExitCall delivery.

- If notifications are denied/unavailable, Start and Schedule do not launch the call pipeline.
- No ringtone/vibration-only hidden delivery is treated as success.

### Full Screen Intent

Full Screen Intent is strongly recommended but is **not** a hard requirement.

- When available, use full-screen/lock-screen presentation.
- When unavailable, continue through the high-priority CallStyle notification with Answer/Decline.
- Missing FSI must not block an otherwise valid immediate or scheduled ExitCall.

### Exact alarms

Exact-alarm capability is required only for precise future scheduling.

- Immediate calls remain independent from exact-alarm access.
- Scheduling must not persist a false successful state when exact-alarm registration fails.

## Scheduling and recovery

- Use `AlarmManager.setExactAndAllowWhileIdle()` for exact future scheduling.
- Use Material date/time pickers for input.
- `ScheduledCallScheduler` owns registration, replacement, cancellation and recovery.
- `ScheduledCallStore` stores the future trigger and scheduled `AppSettings` snapshot in device-protected storage.
- Rescheduling reuses the one scheduled-call `PendingIntent` identity.
- Explicit cancellation removes the alarm and clears all persisted scheduled metadata.
- A fired call consumes/clears the pending schedule.
- Expired scheduled calls are discarded rather than replayed late.
- Recovery after boot/exact-alarm restoration remains idempotent and reuses the same scheduler path.

## Contacts and privacy

- Do not bulk-import or enumerate the address book.
- Use Android's system single-phone-data picker on demand.
- Do not request broad `READ_CONTACTS` for this flow.
- Persist only caller name and the selected phone number after Save.
- Do not add random-contact selection.

## Phone-number handling

- Manual and contact-picked numbers pass through one shared formatter/validator.
- Explicit international `+` prefixes are preserved.
- `00` international prefixes normalize to `+`.
- E.164 maximum length is respected.
- Blank phone number remains allowed; the incoming UI omits the number row when blank.
- Polish `+48`/9-digit values may be grouped for display while stored normalized values remain suitable for reuse.

## Ringtone and notification sound ownership

The production ringtone path uses Android's system ringtone picker from Settings.

- Selected URI/title are persisted in `AppSettingsStore`.
- Immediate calls use current global ringtone settings.
- Scheduled calls use the captured scheduled settings snapshot.
- Missing/unresolvable URI falls back to the Android system default ringtone.
- Incoming CallStyle notification channel is intentionally silent/non-vibrating so `IncomingCallService` is the single owner of call alert audio/haptics.
- Answer, Decline, Android Back, timeout and service shutdown must stop call alerting.

## Vibration and Android system behavior

- ExitCall exposes a global vibration preference in Settings.
- The app does not intentionally bypass Android global vibration or Do Not Disturb behavior.
- Alert cleanup cancels ExitCall-generated vibration on all incoming termination paths.
- Focused ring/vibrate/silent/DND verification remains part of #19.

## Incoming-call lifecycle and Back behavior

Incoming termination uses one maintained cleanup contract.

- Incoming Android Back is an intentional Decline action.
- Notification Decline and unanswered timeout clean the service and close any still-incoming Activity through the shared app-scoped finish signal.
- A late incoming finish signal must not close an already active call.
- Active Call Back follows the current product default: Return to Android.
- Future configurable post-call behavior belongs to #34.

## Incoming-call timeout and missed-call behavior

- Timeout is configurable globally from 5–60 seconds; default 20 seconds.
- Scheduled calls preserve the timeout captured at scheduling time.
- Answer and Decline do not create a missed-call notification.
- Unanswered timeout may create a missed-call notification when the #53 Settings toggle is enabled.
- Missed-call notification uses normal notification semantics, not the incoming-call ringtone.

## Active Call controls and routing

The visual baseline was established by PR #87; behavior was completed through #20/#21/#22 and PR #97.

- `Audio Emoji` is removed.
- Keypad opens a numeric keypad surface.
- More opens an intentional dismissible menu.
- Mute is stateful. For demo narration it controls player volume; it does not claim to mute a real telephony microphone.
- Phone, Speaker and Bluetooth are real communication-audio route requests, not cosmetic toggles.
- Route UI follows the device Android accepted, including external changes where supported.
- Production and demo Active Call share `ActiveCallAudioRouter`; no competing route implementation is allowed.
- Cleanup restores prior audio mode/route.
- Wired/headset/device-removal and OEM differences remain regression-matrix concerns rather than separate audio engines.

## Visual direction

- Matte/dark surfaces with sage/pastel green accent are the primary design language.
- Incoming/Active presentation may borrow hierarchy and ergonomics from native Android call UIs but must remain an ExitCall design, not a pixel-for-pixel clone.
- Call surfaces use the centralized theme palette and dark system navigation presentation.
- Hang Up uses a clear red destructive action.
- `EC` remains discreet, fixed and predictable on Incoming and Active surfaces.
- Future Google-like/Samsung-like variants are presentation layers only over the shared engine (#36).

## EC marker / real-call distinction

- Incoming and Active surfaces show a discreet fixed `EC` marker.
- v1.0 onboarding/demo teaches: marker visible = ExitCall; marker absent = treat the call as genuine.
- The marker remains predictable across every shipping call-screen presentation.

## Custom call audio

v1.0 supports local user-provided audio separately from ringtone selection.

- Record with microphone or choose an existing audio file.
- App recordings target M4A/AAC where supported.
- Import supports at least MP3 and M4A/AAC that Android can decode.
- Audio remains local; no backend/cloud upload is required.
- User can preview, replace and delete selected custom audio.
- Custom audio starts approximately 3 seconds after Answer.
- Missing/unusable custom audio falls back to the bundled default English voice track.
- Playback stops immediately on Hang Up/end of call.

Tracked in #76.

## Settings structure

Production Settings foundation is implemented.

- Global ringtone, vibration and ring duration are live.
- Notifications / Exact alarms / Full-screen calls expose live readiness and Android settings actions.
- App version is read from build metadata.
- Future features appear as explicit disabled/`Coming soon` rows rather than fake working navigation.
- Functional missed-call toggle, tutorial replay, licenses/privacy/safe-use/support/about are completed through their owning issues (#53/#29/#23).

## Onboarding and demo

v1.0 uses mandatory first-run onboarding.

- Explain ExitCall's purpose, the `EC` marker and capability policy.
- Notifications are required; FSI is recommended with notification fallback; exact-alarm access is contextual to precise scheduling.
- Include one controlled demo call with caller `ExitCall`.
- Demo uses the same Incoming/Active surfaces and `EC` marker as normal calls, but does not invoke the production foreground-service pipeline.
- Default guided content is one local, emotionally neutral English voice track.
- Demo narration begins only after Answer and uses the shared communication-audio route.
- Decline, Back, unanswered timeout, background/lock and disposal cancel without completing first-run onboarding.
- Only Answer followed by Hang Up completes first-run onboarding.
- Settings can replay the tutorial, which ends with the same controlled demo.
- Tutorial replay must not reset or mutate onboarding completion, caller/settings/schedule data, free-call state or Premium state.
- Demo playback never consumes a configurable call.
- Future custom recording/import remains owned by #76; until then the bundled default voice is used.

Tracked and physically accepted in #29 before closure.

## Monetization and entitlement

- Onboarding includes 1 demo call.
- New installation has exactly 2 free configurable calls after demo.
- Demo and unanswered timeout do not consume a free call.
- Answer or Decline consumes exactly one free use.
- Capability failure, cancelled scheduling or configuration changes do not consume a free call.
- At 0 free uses, usable call execution is gated behind Premium.
- Local persistence is sufficient for the free counter in v1.0; clear-data/reinstall reset is an accepted tradeoff.
- Premium is a non-consumable one-time Google Play purchase.
- Google Play owned-purchase state is authoritative when synchronization is available.
- Working price direction: 4.99 USD / 4.99 EUR with Play-localized pricing; PLN may use a regional value such as 14.99 PLN.

Tracked in #74/#75/#77.

## Language and localization

- v1.0 UI is English-only.
- User-facing strings should remain resource-based and localization-ready.
- Polish, German and Spanish are post-v1.0 candidates.
- Additional guided voice languages are post-v1.0.

Tracked in #31/#32.

## Platform and release strategy

- Android first.
- Validate Android product before committing to iOS.
- iOS, if pursued, is a separate native Swift/SwiftUI implementation.
- Do not protect an arbitrary release date by cutting required quality.
- Google Play readiness includes capability-policy review, billing, privacy/data-safety, store assets, testing-track requirements, release AAB verification and final P0 regression.
- Final Go/No-Go is explicit.

Tracked in #28/#78.

## Development workflow

- GitHub Issues are the source of truth for actionable work.
- Markdown under `docs/` is the living architecture/product/process documentation.
- Versioned DOCX specifications are historical snapshots unless explicitly refreshed.
- Keep one large task or at most two small tasks active at once.
- New ideas enter Backlog/Icebox instead of interrupting the active milestone.
- Prefer controlled refactors protected by targeted regression over broad rewrites.
