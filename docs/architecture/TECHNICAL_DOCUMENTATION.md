# ExitCall Technical Documentation v1.4

**Status:** living technical documentation  
**Baseline date:** 2026-09-03  
**Platform:** Android  
**Production application ID:** `com.mateuszbury.exitcall`

This document describes the current technical architecture and verified v1.0 implementation baseline of ExitCall. It is maintained together with the codebase and should be updated when architecture, Android capability handling, persistence, scheduling, call delivery or other cross-cutting behavior changes materially.

The versioned DOCX files under `docs/specifications/` remain historical specification snapshots. This Markdown document is the current technical source of truth.

---

## 1. Technical scope

ExitCall is a native Android application that lets the user configure and start an incoming-call experience immediately or schedule one for a precise future time.

The current implementation covers:

- persistent caller configuration,
- immediate call start,
- exact future scheduling,
- edit/reschedule/cancel of the single pending scheduled call,
- process-death and device-reboot scheduler recovery,
- high-priority CallStyle notification delivery,
- Full Screen Intent presentation where Android permits it,
- CallStyle notification fallback when Full Screen Intent is unavailable,
- lock-screen presentation,
- Answer / Decline / Hang Up lifecycle,
- unanswered-call timeout and cleanup,
- Android system ringtone selection,
- ringtone fallback to the Android system default,
- user vibration preference,
- portrait-only primary and call-flow Activities.

The current P2 implementation sequence continues with contact selection and international phone-number handling.

---

## 2. Technology stack

- Kotlin
- Android SDK
- Jetpack Compose
- Material 3
- AndroidX Activity / Activity Result APIs
- AlarmManager exact alarms
- BroadcastReceiver
- Foreground Service
- NotificationCompat CallStyle
- Full Screen Intent
- SharedPreferences
- Device Protected Storage for scheduler recovery metadata

No backend, remote database, cloud synchronization or account system is required by the current baseline.

---

## 3. High-level architecture

ExitCall deliberately uses a compact native Android architecture. The application is still small enough that introducing a DI framework, Navigation Compose or a large presentation architecture would add more complexity than value.

The main boundaries are:

```text
Compose UI
    |
    v
MainApp / Activities
    |
    +--> CallConfigStore
    |
    +--> CallCapabilityResolver
    |
    +--> ScheduledCallScheduler --> AlarmManager
    |                              |
    |                              v
    |                        CallAlarmReceiver
    |                              |
    v                              v
IncomingCallService <--------------+
    |
    +--> CallStyle notification
    +--> ringtone
    +--> vibration
    +--> Full Screen Intent / notification fallback
    |
    v
IncomingCallActivity
    |
    +--> IncomingCallScreen
    +--> ActiveCallScreen
```

Immediate and scheduled calls intentionally converge on the same `IncomingCallService -> IncomingCallActivity` delivery path.

---

## 4. Package structure

Primary source root:

`app/src/main/java/com/mateuszbury/exitcall`

### Root Android components

- `MainActivity.kt`  
  Android lifecycle owner for the main application surface. Coordinates capability checks, scheduling, scheduler recovery and starting the foreground call service.

- `IncomingCallService.kt`  
  Foreground service responsible for incoming-call notification delivery, ringtone, vibration, timeout and cleanup.

- `IncomingCallActivity.kt`  
  Lock-screen-capable Activity that owns the incoming-to-active call UI transition.

- `CallAlarmReceiver.kt`  
  Receives the exact scheduled alarm, consumes persisted scheduled state and starts the normal call-delivery pipeline.

### `capability/`

- `CallCapability.kt`
- `CallCapabilityResolver.kt`

Contains Android/version-specific capability resolution and settings routing for Notifications, Full Screen Intent and exact alarms.

### `model/`

- `CallConfig.kt`
- `CallConfigStore.kt`
- `ScheduledCallStore.kt`

Contains persistent configuration and scheduler state.

### `scheduler/`

- `ScheduledCallScheduler.kt`
- `ScheduledCallRecoveryReceiver.kt`

Owns exact-alarm registration, replacement, cancellation and recovery after reboot or exact-alarm permission restoration.

### `ui/`

- `MainApp.kt`
- `components/`
- `screens/`
- `theme/`

Contains the Compose application root, reusable UI, application screens and centralized Material theme.

### Current Compose screens

- `HomeScreen`
- `CallSetupScreen`
- `ScheduleCallScreen`
- `ScheduleDatePickerDialog`
- `ScheduleTimePickerDialog`
- `IncomingCallScreen`
- `ActiveCallScreen`

---

## 5. Main application flow

`MainActivity` owns Android lifecycle and platform orchestration. Compose screen state is delegated to `MainApp`.

`MainApp` currently uses a lightweight internal screen state:

```text
HOME
  |-- Set up a call --> CALL_SETUP
  |-- Schedule ------> SCHEDULE_CALL
  |-- Start call ----> Android capability gate -> IncomingCallService
```

This is intentional. Navigation Compose is not currently required for the small three-screen main-process flow.

`BackHandler` in `MainApp` returns secondary main-app screens to Home.

The separate incoming/active call Activity is not part of this main-screen state machine.

---

## 6. Call configuration model

`CallConfig` is the user-configurable source model for the current call presentation.

Current persisted fields include:

- caller name,
- phone number,
- ringtone URI,
- ringtone display name,
- vibration enabled/disabled.

`CallConfigStore` is the durable local source of truth.

Both immediate and scheduled calls read the latest stored `CallConfig` at call-delivery time. A scheduled alarm does not carry a duplicate snapshot of caller configuration. This prevents two competing configuration sources.

### Planned extension

Issue #14 will populate caller identity through a privacy-friendly single-contact picker.

Issue #15 will own phone-number normalization, country-code support, formatting and validation. Phone-number validation is therefore intentionally not implemented as a separate architectural path.

---

## 7. Ringtone selection and playback

The ringtone picker is implemented directly in `CallSetupScreen` using `rememberLauncherForActivityResult` with Android's system `RingtoneManager.ACTION_RINGTONE_PICKER`.

Behavior:

1. The currently stored ringtone URI is passed as the existing selection.
2. If no custom ringtone is stored, Android's default ringtone URI is preselected.
3. Silent selection is disabled for the current v1.0 setup flow.
4. On successful selection, the URI and human-readable title are stored in `CallConfig`.
5. Cancelling the Android picker leaves the previous value unchanged.
6. The saved configuration persists through `CallConfigStore`.

`IncomingCallService` loads the saved ringtone URI when a call starts.

Playback fallback:

```text
saved ringtone URI
        |
        | unavailable / null
        v
Android system default ringtone
```

The service uses ringtone audio attributes with `USAGE_NOTIFICATION_RINGTONE` and loops playback on supported Android versions.

Answer, Decline, timeout and service destruction all stop ringtone playback through the shared cleanup path.

---

## 8. Immediate call pipeline

The immediate path is:

```text
Home: Start call
    |
    v
MainActivity.ensureCallCapabilities(requireExactAlarm = false)
    |
    +--> Notifications missing -> permission/settings UX -> stop until resolved
    |
    +--> FSI unavailable -> allowed; notification fallback remains valid
    |
    v
ContextCompat.startForegroundService(IncomingCallService)
    |
    v
CallStyle notification + ringtone + vibration
    |
    +--> Full Screen Intent where available
    |
    +--> high-priority CallStyle notification fallback
    |
    v
IncomingCallActivity
```

Immediate calls never depend on exact-alarm permission.

---

## 9. Scheduled call pipeline

The scheduled path is:

```text
Home: Schedule / Edit scheduled call
    |
    v
ScheduleCallScreen
    |
    +--> Material DatePicker
    +--> Material TimePicker
    |
    v
MainActivity capability gate
    |
    +--> Notifications required
    +--> FSI recommended, fallback allowed
    +--> exact-alarm capability required
    |
    v
ScheduledCallScheduler.schedule(timestamp)
    |
    v
AlarmManager.setExactAndAllowWhileIdle(...)
    |
    v
ScheduledCallStore persists timestamp
    |
    v
CallAlarmReceiver
    |
    v
IncomingCallService
```

The application supports one pending scheduled call in v1.0.

---

## 10. Scheduled-call edit, reschedule and cancel

PR #80 / issue #12 added explicit management of the single pending call.

### Edit / reschedule

- Home displays the persisted next-call timestamp.
- When a schedule exists, the Home action changes to `Edit scheduled call`.
- `ScheduleCallScreen` receives the stored timestamp as its initial value.
- Saving a different future time reuses the same scheduled-call `PendingIntent` identity.
- The new exact alarm replaces the previous alarm instead of creating another independent scheduled call.

### Cancel

`ScheduledCallScheduler.cancel()`:

1. resolves the existing scheduled-call `PendingIntent`,
2. cancels it in `AlarmManager`,
3. cancels the `PendingIntent` itself,
4. clears `ScheduledCallStore`.

The Schedule screen exposes `Cancel scheduled call` as a destructive UI action and returns the application to Home after cancellation.

This preserves the invariant that the durable scheduler state and Android alarm registration represent the same single pending call.

---

## 11. Scheduler persistence

`ScheduledCallStore` is the durable source of truth for the pending trigger timestamp.

The timestamp is stored separately from normal `CallConfig` because scheduler recovery has different Android boot-time requirements.

Important invariant:

- the timestamp is persisted only after a successful alarm registration,
- firing the scheduled alarm consumes and clears the stored timestamp,
- cancellation clears both Android alarm registration and stored scheduler state,
- expired timestamps are not replayed.

---

## 12. Device reboot recovery

Android removes normal AlarmManager registrations when the device reboots. ExitCall therefore restores still-future scheduled calls.

`ScheduledCallRecoveryReceiver` is Direct-Boot-aware and listens for:

- `LOCKED_BOOT_COMPLETED`,
- `BOOT_COMPLETED`,
- `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`.

Scheduler recovery metadata is stored in Device Protected Storage so it can be read before normal credential-protected application storage is available.

Recovery path:

```text
boot / permission restoration
    |
    v
ScheduledCallRecoveryReceiver
    |
    v
ScheduledCallScheduler.restorePersistedCall()
    |
    +--> no future timestamp -> nothing to restore
    +--> expired timestamp -> clear it
    +--> exact-alarm unavailable -> retain valid persisted state
    |
    v
re-register same PendingIntent identity
```

`MainActivity` also runs an idempotent restore attempt during startup as an OEM/device fallback.

The reboot-recovery path has been physically verified on the primary Motorola device.

---

## 13. Android capability policy

### Notifications

Notifications are a hard requirement for successful ExitCall delivery.

If notification capability is unavailable, Start/Schedule does not silently start ringtone or vibration as a substitute.

### Full Screen Intent

Full Screen Intent is preferred for the strongest lock-screen presentation but is not a hard requirement.

If Android does not permit FSI, the foreground service still produces a high-priority incoming CallStyle notification with Answer and Decline actions.

### Exact alarms

Exact-alarm capability is required only for exact future scheduling.

It must not block an immediate `Start call` action.

### Capability orchestration

`CallCapabilityResolver` centralizes:

- version-specific checks,
- first missing required capability,
- Android settings intents,
- app-details fallback routing.

`MainActivity` owns the pending user action and rechecks capabilities after returning from Android settings.

This prevents capability UI from creating a second call/scheduler execution path.

---

## 14. Foreground service and notification delivery

`IncomingCallService` is declared as a foreground service with `mediaPlayback` type.

On creation it:

1. creates/ensures the high-importance notification channel,
2. loads the latest `CallConfig`,
3. creates full-screen, Answer and Decline PendingIntents,
4. starts itself in the foreground with `NotificationCompat.CallStyle.forIncomingCall`,
5. starts ringtone,
6. starts vibration if enabled,
7. starts the unanswered-call timeout.

Current notification constants:

- channel: `incoming_call_channel`
- notification ID: `1002`

The notification is categorized as a call, high priority and ongoing while the incoming call is active.

---

## 15. Incoming call lifecycle

`IncomingCallActivity` is:

- `singleTop`,
- portrait-only,
- configured to show when locked,
- configured to turn the screen on.

It loads `CallConfig` from the same persistent store used by the service.

### Incoming state

`IncomingCallScreen` supports:

- Answer,
- Decline.

Answer:

```text
stop IncomingCallService
-> stop ringtone/vibration/notification
-> switch Activity state to ActiveCallScreen
```

Decline or Android Back while the incoming screen is visible:

```text
stop IncomingCallService
-> stop ringtone/vibration/notification
-> finish IncomingCallActivity
```

Decline from the CallStyle notification is handled by the service. The shared app-scoped call-finished signal closes an open incoming-call Activity after service cleanup begins.

### Notification Answer

The Answer notification action starts/reuses `IncomingCallActivity` with `EXTRA_START_ACTIVE = true`.

Because the Activity is `singleTop` and uses `CLEAR_TOP | SINGLE_TOP` launch flags, `onNewIntent()` can transition the existing Activity to active-call state rather than creating duplicate Active Call instances.

This is the maintained duplicate-call prevention mechanism introduced after the earlier double-flow defect.

---

## 16. Active call lifecycle

After Answer, `IncomingCallActivity` renders `ActiveCallScreen`.

The incoming foreground service is no longer responsible for ringtone/vibration once the call is active.

Hang Up finishes `IncomingCallActivity`, returning the user out of the call flow.

Current product default is Return to Android after the call flow. More configurable post-call behavior remains later work.

---

## 17. Unanswered timeout and cleanup

The current default unanswered timeout is:

`20 seconds`

`IncomingCallService` owns this timer on the main Looper.

On timeout:

1. an app-scoped call-finished broadcast is sent,
2. ringtone is stopped,
3. vibration is cancelled,
4. foreground notification is removed,
5. service stops,
6. an open incoming-call Activity closes if it is still in incoming state.

The same call-finished signal is emitted for Decline from the notification. The Activity handles it only while still on the incoming screen, so a late termination signal cannot close an already active call.

The cleanup path is guarded by `isEnding` and callback removal so late or duplicate termination events do not continue the incoming-call lifecycle.

Future timeout configurability is tracked separately.

---

## 18. Vibration behavior

`CallConfig.vibrate` controls whether ExitCall starts vibration for the incoming state.

The service uses:

- `VibratorManager` on Android 12+,
- legacy `Vibrator` access on older supported versions,
- `VibrationEffect.createWaveform` on Android 8+,
- ringtone vibration usage attributes on Android 13+.

The application should respect Android's system-level vibration and Do Not Disturb behavior. It must not intentionally bypass those policies.

---

## 19. Manifest and Android components

Current declared permissions:

- `VIBRATE`
- `SCHEDULE_EXACT_ALARM`
- `RECEIVE_BOOT_COMPLETED`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `USE_FULL_SCREEN_INTENT`
- `POST_NOTIFICATIONS`

Declared runtime components:

- `MainActivity`
- `IncomingCallActivity`
- `IncomingCallService`
- `CallAlarmReceiver`
- `ScheduledCallRecoveryReceiver`

`ScheduledCallRecoveryReceiver` is `directBootAware`.

`IncomingCallActivity` and `MainActivity` are portrait-locked in the current baseline.

---

## 20. UI architecture and theme

The UI is implemented with Jetpack Compose and centralized Material 3 colors.

Current direction:

- dark/matte background,
- sage/pastel green primary accent,
- restrained surface hierarchy,
- explicit ExitCall marker on call screens,
- destructive actions use the theme error role.

Heavy Material date/time picker composition was extracted into dedicated files so the normal Schedule screen remains lightweight.

The current application source language is English and `MainActivity.attachBaseContext()` enforces an English locale for the v1.0 baseline.

Localization is planned after the English-first v1.0 work is stable.

---

## 21. Error handling and fallback rules

The current implementation follows these rules:

- missing notifications -> do not start successful call delivery,
- missing FSI -> use CallStyle notification fallback,
- missing exact-alarm capability -> do not create false scheduled state,
- past schedule -> reject scheduling,
- missing saved ringtone -> Android default ringtone,
- cancelled ringtone picker -> preserve previous value,
- expired persisted schedule -> clear, do not replay late,
- reboot recovery -> reuse existing scheduled-call identity,
- repeated incoming-call termination -> idempotent cleanup.

---

## 22. Persistence boundaries

### Credential-protected application configuration

`CallConfigStore`

Stores user call configuration such as caller identity, ringtone and vibration.

### Device-protected scheduler metadata

`ScheduledCallStore`

Stores only scheduler recovery information needed to re-register a future exact alarm across boot.

This split is deliberate. The full call configuration is not duplicated into the Direct Boot storage path.

---

## 23. Privacy and data locality

The current architecture is local-first.

- Call configuration is stored locally.
- Scheduler metadata is stored locally.
- No bulk address-book import is planned.
- #14 will use a one-contact system picker on demand.
- User-generated custom audio planned for later v1.0 work is intended to remain local to the device.

The application should request only Android capabilities that map to implemented user-facing behavior.

---

## 24. Current architectural invariants

The following should remain true unless an explicit architecture decision changes them:

1. Immediate and scheduled calls use one shared call-delivery pipeline.
2. `CallConfigStore` is the single configuration source of truth.
3. `ScheduledCallStore` is the single persisted future-trigger source of truth.
4. The MVP supports one pending scheduled call.
5. Scheduling, rescheduling, cancellation and recovery reuse one scheduled-call PendingIntent identity.
6. Notifications are required for call delivery.
7. Full Screen Intent is optional from a functional perspective because CallStyle fallback exists.
8. Exact alarms are required only for precise scheduled calls.
9. The call service owns incoming ringtone/vibration/notification cleanup.
10. Answer does not create a second parallel Active Call screen.
11. Normal configuration stays outside the Direct Boot scheduler-recovery store.
12. No bulk contact import is introduced.

---

## 25. Near-term technical roadmap

### #14 — single contact picker

Expected technical boundary:

- Android system picker,
- one contact per explicit user action,
- no bulk Contacts Provider synchronization,
- update caller name and selected phone number in `CallSetupScreen` / `CallConfig`,
- safe handling for cancellation and contacts without a usable number.

### #15 — phone-number handling

This issue will include the phone-number validator requested for Call Setup.

Expected scope:

- validation of manually entered numbers,
- validation/normalization of contact-picked numbers,
- international `+` country prefixes,
- preservation of valid country codes,
- consistent display formatting,
- error state in Call Setup for invalid values,
- prevent invalid phone-number configuration from being saved as if valid,
- formatting logic must remain extensible beyond Poland.

Validation and formatting should share one coherent normalization layer rather than separate ad-hoc UI regexes.

---

## 26. Testing strategy

The maintained physical regression suite is tracked in GitHub issue #9.

High-risk technical changes should trigger targeted physical coverage for the affected area.

Core regression areas include:

- immediate call exactly once,
- scheduled call exactly once,
- Answer / Decline / Hang Up,
- timeout cleanup,
- lock-screen delivery,
- FSI unavailable fallback,
- notifications denied,
- exact-alarm denied/granted,
- schedule edit/reschedule/cancel,
- persistence after process death,
- reboot recovery,
- selected ringtone playback and fallback.

A full release-quality regression remains required before the v1.0 release candidate.

---

## 27. Documentation ownership

The maintained documentation hierarchy is:

- `docs/architecture/TECHNICAL_DOCUMENTATION.md` — current implementation architecture and technical behavior,
- `docs/architecture/DECISIONS.md` — stable product/technical decisions,
- `docs/architecture/SCHEDULER_LIFECYCLE.md` — detailed scheduler/recovery behavior,
- `docs/process/BACKLOG.md` — roadmap and current phase,
- GitHub Issues — actionable work and acceptance criteria,
- `docs/specifications/*.docx` — historical versioned specification snapshots.

When code and documentation disagree, the discrepancy should be resolved in the same development cycle rather than allowing chat history to become the de facto architecture record.
