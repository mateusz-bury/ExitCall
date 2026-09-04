# ExitCall Technical Documentation v1.5

**Status:** living technical documentation  
**Baseline date:** 2026-09-04  
**Platform:** Android  
**Production application ID:** `com.mateuszbury.exitcall`

This document describes the current v1.0 implementation baseline and architecture after the P2 Settings, lifecycle and call-screen polish work merged in PRs #84-#87.

Versioned DOCX files under `docs/specifications/` remain historical specification snapshots. This Markdown document is the current technical source of truth.

---

## 1. Technical scope

ExitCall is a native Android application that lets a user configure an incoming-call experience, start it immediately or schedule one precise future call.

Current merged implementation covers:

- persisted caller identity and phone number,
- privacy-friendly Android single-phone contact selection,
- phone-number validation/normalization/formatting,
- immediate call start,
- exact future scheduling,
- edit/reschedule/cancel of one pending scheduled call,
- process-death and reboot scheduler recovery,
- high-priority CallStyle notification delivery,
- Full Screen Intent where Android permits it,
- CallStyle fallback when FSI is unavailable,
- lock-screen presentation,
- Answer / Decline / Android-Back / Hang Up lifecycle,
- configurable unanswered timeout 5–60 seconds,
- production Settings foundation,
- global ringtone/vibration/ring-duration persistence,
- scheduled snapshots of alert settings,
- system ringtone picker with default fallback,
- centralized theme and polished Incoming/Active call surfaces,
- consistent main-app back navigation/header presentation.

Important incomplete v1.0 areas include Active Call secondary-control behavior (#22), Settings help/information completion (#23), missed-call notification (#53), custom local audio (#76), Home dashboard (#30), onboarding/monetization/release work and broader regression/OEM verification.

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
- Device Protected Storage for scheduled metadata/recovery

No backend, remote database, cloud synchronization or user account backend is required by the current baseline.

---

## 3. High-level architecture

ExitCall intentionally keeps a compact native Android architecture. The project does not currently need Navigation Compose, a DI framework or a second call engine.

```text
Compose UI
    |
    v
MainApp / MainActivity
    |
    +--> CallConfigStore --------------------+
    |                                       |
    +--> AppSettingsStore ----------------+  |
    |                                    |  |
    +--> CallCapabilityResolver          |  |
    |                                    |  |
    +--> ScheduledCallScheduler          |  |
             |                           |  |
             v                           |  |
         AlarmManager                    |  |
             |                           |  |
             v                           |  |
       CallAlarmReceiver                 |  |
             | settings snapshot         |  |
             +---------------------------+  |
                                            |
                                            v
                                   IncomingCallService
                                            |
                   +------------------------+------------------+
                   |                        |                  |
                   v                        v                  v
             CallStyle notif.          ringtone           vibration
                   |
                   v
           IncomingCallActivity
                   |
             +-----+------+
             |            |
             v            v
      IncomingCallScreen  ActiveCallScreen
```

Immediate and scheduled calls converge on the same `IncomingCallService -> IncomingCallActivity` lifecycle.

---

## 4. Package structure

Primary source root:

`app/src/main/java/com/mateuszbury/exitcall`

### Root Android components

- `MainActivity.kt`  
  Main-process lifecycle/platform coordinator. Owns capability gating, scheduler operations/recovery, app-settings state and immediate foreground-service start.

- `IncomingCallService.kt`  
  Foreground service responsible for CallStyle delivery, ringtone, vibration, configurable unanswered timeout and shared incoming-call cleanup.

- `IncomingCallActivity.kt`  
  Lock-screen-capable call Activity. Owns incoming-to-active transition, incoming Back/decline behavior and call-surface system-bar presentation.

- `CallAlarmReceiver.kt`  
  Receives the scheduled alarm, loads the scheduled settings snapshot, consumes scheduled metadata and starts the normal call-delivery pipeline.

### `capability/`

- `CallCapability.kt`
- `CallCapabilityResolver.kt`

Centralizes Android/version-specific checks and settings routing for Notifications, Full Screen Intent and exact alarms.

### `model/`

- `CallConfig.kt`
- `CallConfigStore.kt`
- `AppSettings.kt`
- `AppSettingsStore.kt`
- `ScheduledCallStore.kt`

### `phone/`

- shared phone-number formatter/validator used by manual entry and contact-picked values.

### `scheduler/`

- `ScheduledCallScheduler.kt`
- `ScheduledCallRecoveryReceiver.kt`

Owns exact registration, replacement, cancellation and recovery.

### `ui/`

- `MainApp.kt`
- `components/ExitCallTopBar.kt`
- `components/`
- `screens/`
- `theme/`

Current screens include Home, Call Setup, Schedule, Settings, Incoming Call and Active Call plus isolated date/time picker dialogs.

---

## 5. Sources of truth

The current architecture intentionally separates three persistence concerns.

### 5.1 `CallConfigStore`

Current product source of truth for caller presentation:

- caller name,
- phone number.

`CallConfig` still contains legacy ringtone/vibration fields because `AppSettingsStore` performs one-time migration from older installations. New code should not reintroduce these legacy fields as the global settings source.

### 5.2 `AppSettingsStore`

Global call settings source of truth:

- ringtone URI,
- ringtone display name,
- vibration enabled/disabled,
- ring duration seconds.

`RingDuration` rules:

- minimum: 5 s,
- maximum: 60 s,
- default: 20 s,
- presets: 5 / 10 / 20 s.

`AppSettingsStore` migrates ringtone/vibration once from legacy `CallConfigStore` state and then owns future changes.

### 5.3 `ScheduledCallStore`

Device-protected scheduled state:

- future trigger timestamp,
- scheduled `AppSettings` snapshot: ringtone URI/name, vibration and ring duration.

A scheduled call therefore has intentional snapshot semantics for alert behavior while caller identity remains current-at-delivery.

---

## 6. Main application navigation

`MainActivity` owns Android lifecycle/platform orchestration. `MainApp` owns the lightweight Compose screen state.

```text
HOME
  |-- Set up a call --> CALL_SETUP
  |-- Schedule/Edit --> SCHEDULE_CALL
  |-- Settings ------> SETTINGS
  |-- Start call ----> capability gate -> IncomingCallService
```

Navigation conventions after PR #86:

- Home exposes Settings as a gear icon.
- Call Setup, Schedule and Settings share `ExitCallTopBar` and the same back-arrow asset/placement.
- Android Back on secondary main-app screens returns Home.

The separate Incoming/Active call Activity is outside this main-process screen state machine.

---

## 7. Caller identity and contact selection

Call Setup edits caller name and phone number only.

PR #82 uses Android's system phone-data picker:

- `Intent.ACTION_PICK`,
- `ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE`,
- no broad `READ_CONTACTS`,
- ExitCall reads only the returned selected row,
- picker cancellation preserves previous values,
- selected values are persisted only when Call Setup is saved.

This avoids address-book enumeration and OEM-specific duplicated custom contact lists inside ExitCall.

---

## 8. Phone-number validation and formatting

PR #83 introduced one shared validation/normalization path.

Rules include:

- blank number is allowed,
- common visual separators are accepted while editing,
- `+` is valid only as a leading international prefix,
- `00` international prefix normalizes to `+`,
- unsupported letters/characters are rejected,
- E.164 15-digit maximum is enforced,
- a conservative minimum digit count rejects obviously unusable input,
- Polish `+48`/9-digit values receive readable display grouping,
- incoming call surfaces use the same formatter.

Call Setup disables Save for invalid non-blank phone values.

---

## 9. Production Settings architecture

PR #84 introduced the Settings foundation.

### Live settings

- Ringtone: Android system ringtone picker.
- Vibration: persisted global switch.
- Ring duration: 5 / 10 / 20 s presets + validated Custom 5–60 s.

### Live device readiness

- Notifications,
- Exact alarms,
- Full-screen calls.

Each supported capability row can route to the relevant Android setting and state refreshes after returning to the app.

### Explicit placeholders

Later features are shown as disabled/`Coming soon` rows rather than empty navigation targets. These include voice/audio route, language/theme, after-call behavior, purchase restore, local-data management, tutorial/help and information rows where the owning feature is not yet implemented.

### Version display

Settings reads the installed version from `BuildConfig.VERSION_NAME` rather than hardcoding display text. The app module must keep Android `BuildConfig` generation enabled when this path is present.

---

## 10. Ringtone and vibration ownership

The system ringtone picker now lives in Settings, not Call Setup.

Selection flow:

1. open Android `RingtoneManager.ACTION_RINGTONE_PICKER`,
2. preselect stored URI or Android default,
3. silent selection is disabled,
4. store URI + display title in `AppSettingsStore`,
5. cancelling preserves the previous selection.

Playback fallback:

```text
configured AppSettings ringtone URI
          |
          | null / unavailable
          v
Android system default ringtone
```

Vibration is controlled by `AppSettings.vibrate`.

ExitCall does not intentionally override Android DND/global sound policy.

---

## 11. Immediate call pipeline

```text
Home: Start call
    |
    v
MainActivity.ensureCallCapabilities(requireExactAlarm = false)
    |
    +--> Notifications missing -> capability UX -> stop until resolved
    +--> FSI missing -> allowed; CallStyle fallback remains valid
    |
    v
startForegroundService(IncomingCallService)
    |
    +--> current caller identity from CallConfigStore
    +--> current global settings from AppSettingsStore
    |
    v
CallStyle notification + ringtone + vibration + configured timeout
    |
    +--> Full Screen Intent where available
    +--> high-priority CallStyle fallback otherwise
    |
    v
IncomingCallActivity
```

Immediate calls never depend on exact-alarm permission.

---

## 12. Scheduled call pipeline

```text
Home: Schedule / Edit
    |
    v
ScheduleCallScreen
    |
    v
capability gate: Notifications + Exact Alarm
    |
    v
ScheduledCallScheduler.schedule(trigger, current AppSettings)
    |
    v
AlarmManager.setExactAndAllowWhileIdle(...)
    |
    v
ScheduledCallStore.save(trigger + AppSettings snapshot)
    |
    v
CallAlarmReceiver
    |
    +--> load snapshot
    +--> clear pending scheduled metadata
    |
    v
IncomingCallService(snapshot)
```

At delivery:

- caller name/phone are read from current `CallConfigStore`,
- ringtone/vibration/timeout use the captured scheduled `AppSettings` snapshot.

Editing/rescheduling captures a new snapshot.

See `SCHEDULER_LIFECYCLE.md` for complete invariants and recovery behavior.

---

## 13. Reboot and exact-alarm recovery

`ScheduledCallRecoveryReceiver` is Direct-Boot-aware and listens for:

- `LOCKED_BOOT_COMPLETED`,
- `BOOT_COMPLETED`,
- `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`.

Recovery reuses the same scheduler registration path and same scheduled-call `PendingIntent` identity.

Behavior:

- future state + exact access -> re-register once,
- future state + no exact access -> keep persisted state for later recovery,
- expired state -> clear and never replay late,
- app startup also performs an idempotent restore attempt as an OEM fallback.

Trigger and settings snapshot live in device-protected storage so recovery does not lose scheduled alert semantics.

---

## 14. Android capability policy

### Notifications

Hard requirement. No call pipeline starts successfully without them.

### Full Screen Intent

Recommended but not mandatory. When denied/unavailable, the CallStyle notification remains the fallback path.

### Exact alarms

Required only for exact future scheduling. Immediate calls remain independent.

`CallCapabilityResolver` centralizes checks and Android settings intents. `MainActivity` owns pending user actions and rechecks after settings return.

---

## 15. Foreground service and CallStyle notification

`IncomingCallService` is the single incoming alert owner.

At call start it:

1. creates/ensures the incoming notification channel,
2. loads caller identity,
3. resolves scheduled snapshot or current `AppSettings`,
4. calculates `unansweredTimeoutMs`,
5. builds full-screen/Answer/Decline PendingIntents,
6. starts foreground CallStyle notification,
7. starts ringtone,
8. starts vibration when enabled,
9. starts timeout callback.

Current constants:

- channel: `incoming_call_channel_v2`,
- notification ID: `1002`.

The v2 notification channel is intentionally high-importance but has channel sound disabled and channel vibration disabled. This prevents Android notification audio from competing with the selected ExitCall ringtone/vibration managed by the service.

---

## 16. Shared incoming-call termination lifecycle

PR #85 unified the incoming termination contract.

`IncomingCallService.finishIncomingCall(reason)` is guarded by `isEnding` and:

1. removes timeout callback,
2. broadcasts app-scoped `ACTION_CALL_FINISHED`,
3. stops ringtone/vibration,
4. removes foreground notification,
5. stops the service.

This path is used for timeout and notification Decline.

### Incoming screen Answer

```text
stop IncomingCallService
-> stop alerting/notification
-> switch Activity state to ACTIVE_CALL
```

### Incoming screen Decline / Android Back

```text
stop IncomingCallService
-> finish IncomingCallActivity
```

The Activity also listens for `ACTION_CALL_FINISHED` and closes only while still in Incoming state. A late incoming termination signal therefore cannot close an already active call.

### Notification Answer

Answer starts/reuses `IncomingCallActivity` with `EXTRA_START_ACTIVE = true` and `CLEAR_TOP | SINGLE_TOP` semantics. This remains the duplicate-Active-call prevention mechanism.

---

## 17. Active Call lifecycle and UI

After Answer, `IncomingCallActivity` renders `ActiveCallScreen`.

Current behavior:

- timer increments locally once per second,
- caller name/phone and avatar remain visible,
- Hang Up finishes the Activity,
- Android Back follows current default Return-to-Android behavior,
- no incoming service alerting remains active after Answer.

PR #87 established the current visual baseline:

- timer -> caller name -> phone hierarchy,
- large caller initial/avatar,
- rounded bottom control sheet,
- proper vector icons for Keypad/Mute/Speaker/More,
- wide red Hang Up control,
- `Audio Emoji` removed,
- shared ExitCall theme colors,
- dark system navigation bar,
- discreet `EC` marker preserved.

Keypad/Mute/Speaker/More are still intentionally non-interactive until #22 is completed. They must not ship as dead controls.

---

## 18. Incoming Call UI

PR #87 also polished `IncomingCallScreen`:

- removed the prototype oversized avatar from Incoming,
- centered caller name and optional number in the upper portion,
- kept lower space clear for the answer control,
- replaced Unicode handset with a vector call icon,
- retained left Decline / right Answer horizontal drag semantics,
- retained a threshold so partial drags return to center,
- aligned slider/handle colors and rounded geometry with the ExitCall theme.

The `EC` marker remains top-right and unobtrusive.

---

## 19. Theme and system bars

Centralized Material 3 theme direction:

- matte/dark background,
- restrained dark surface hierarchy,
- sage/pastel green primary accent,
- clear red destructive Hang Up treatment,
- light foreground text,
- explicit `EC` call marker.

Call-flow colors alias the main ExitCall palette rather than maintaining a separate blue/black sub-theme.

`IncomingCallActivity` explicitly aligns the Android navigation bar with the dark call UI and disables light navigation-bar icon mode for the call surface where supported.

---

## 20. Manifest / Android components

Current declared permissions include:

- `VIBRATE`
- `SCHEDULE_EXACT_ALARM`
- `RECEIVE_BOOT_COMPLETED`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `USE_FULL_SCREEN_INTENT`
- `POST_NOTIFICATIONS`

Runtime components:

- `MainActivity`
- `IncomingCallActivity`
- `IncomingCallService`
- `CallAlarmReceiver`
- `ScheduledCallRecoveryReceiver`

`ScheduledCallRecoveryReceiver` is Direct-Boot-aware.

`MainActivity` and `IncomingCallActivity` are portrait-locked.

---

## 21. Reliability invariants

The current architecture should preserve these invariants:

1. Immediate and scheduled calls share one service/activity call engine.
2. Notifications are required; FSI is not.
3. Exact-alarm access is required only for scheduling.
4. One pending scheduled call exists in v1.0.
5. Scheduled state is persisted only after successful alarm registration.
6. Rescheduling replaces the previous alarm and settings snapshot.
7. Cancellation clears OS alarm + all scheduled metadata.
8. Expired schedules never replay late.
9. Caller identity has one current source of truth in `CallConfigStore`.
10. Global ringtone/vibration/ring duration have one source of truth in `AppSettingsStore`.
11. Pending scheduled calls preserve their captured alert-settings snapshot.
12. CallStyle notification channel does not create competing ringtone/vibration alerting.
13. Incoming termination is centralized/idempotent.
14. A late incoming finish event cannot kill an Active Call.
15. UI variants/polish must not fork call business logic.

---

## 22. Testing state

Physical-device testing remains essential because ExitCall depends heavily on Android/OEM behavior around alarms, notifications, FSI, lock screen and system bars.

Current test assets:

- maintained physical regression suite in #9,
- phone-number unit tests from #15,
- ring-duration validation/fallback unit tests from #58,
- reboot/scheduler physical verification on primary Motorola hardware,
- targeted physical smoke review for recent lifecycle/Settings/call-UI work.

#9 needs a maintenance pass because several exclusions and assumptions became stale after #13/#14/#15/#58 and the final FSI policy.

#25 remains open for broader automated coverage of persistence/scheduler/state.

#26 remains open for an explicit OEM/device compatibility matrix.

---

## 23. Privacy boundaries

Current baseline:

- no backend/cloud account required,
- no bulk contacts access,
- contact picker reads only the selected phone-data row,
- caller configuration is local,
- app settings are local,
- scheduler metadata is local,
- future custom voice/audio under #76 remains local to device,
- no user recording is uploaded externally.

---

## 24. Current technical frontier

Recommended next technical/feature work:

1. #22 implement intentional behavior for Keypad/Mute/Speaker/More without changing the call engine.
2. #53 add missed-call notification and Settings toggle on timeout only.
3. #76 add local custom recording/import/playback with lifecycle-safe cleanup.
4. #30 replace Home placeholders with real configuration/readiness state.
5. Maintain #9 regression alongside each change.

Settings/help completion (#23) should consume functionality from owning issues rather than duplicating their state or logic.

---

## 25. Documentation rule

GitHub Issues define actionable scope/acceptance criteria. Maintained Markdown under `docs/` defines current architecture, decisions and roadmap context. Versioned DOCX files are historical snapshots unless explicitly refreshed.

Material architecture/product changes should update code/issue scope and living documentation in the same workstream.
