# Scheduler lifecycle and recovery

This document describes the maintained v1.0 scheduling path for ExitCall.

## Validation status

The core scheduler and reboot-recovery design was physically verified on the primary Motorola device. The scheduler management UX was extended and merged through PR #80 on 2026-09-03.

Maintained behavior includes:

- one future scheduled trigger can exist at a time,
- selected date/time persists and is shown on Home,
- an existing call can be edited/rescheduled,
- rescheduling replaces the previous alarm rather than creating a duplicate,
- a scheduled call can be explicitly cancelled,
- cancellation removes the Android alarm and clears persisted schedule state,
- future scheduler metadata survives process death and device reboot,
- recovery re-registers exactly one future `RTC_WAKEUP` alarm,
- exact-alarm permission restoration can recreate the same future alarm,
- expired calls are not replayed late,
- a fired call clears the persisted scheduled state.

## Scope

v1.0 supports **one pending scheduled ExitCall at a time**.

The persisted trigger timestamp is the durable source of truth for whether a future call should exist. `AlarmManager` is the operating-system execution mechanism, not the durable source of truth.

## Components

- `ScheduledCallStore` persists the future trigger timestamp in device-protected `SharedPreferences`.
- `ScheduledCallScheduler` owns exact-alarm capability checks, scheduled-call `PendingIntent` identity, registration, replacement, explicit cancellation and recovery.
- `CallAlarmReceiver` handles the actual trigger, clears the persisted schedule and starts `IncomingCallService`.
- `ScheduledCallRecoveryReceiver` restores a valid future call after locked boot, normal boot and exact-alarm access restoration.
- `MainActivity` performs an idempotent recovery attempt on startup as an OEM/device fallback and keeps the Compose-facing scheduled timestamp in sync.
- `MainApp` wires Home and Schedule UI actions to the Activity-owned scheduler operations.
- `IncomingCallService` and the call Activity read the normal persisted `CallConfig`; scheduler recovery does not create a second configuration path.

## Capability policy

Scheduling uses the shared v1.0 capability policy:

1. Notifications are required.
2. Full Screen Intent is recommended but not mandatory; if unavailable, delivery may continue through the high-priority CallStyle notification path.
3. Exact-alarm capability is required for exact future scheduling.

An immediate call does not depend on exact-alarm access.

## Normal scheduling

1. User opens `ScheduleCallScreen`.
2. With no pending call, the screen initializes to a future candidate time.
3. User selects date/time using the Material date/time picker components.
4. Past date/time is rejected by the UI.
5. `MainActivity` runs the scheduling capability gate.
6. `ScheduledCallScheduler.schedule()` checks exact-alarm access again at registration time.
7. Scheduler creates the single v1.0 broadcast `PendingIntent` for `CallAlarmReceiver`.
8. `AlarmManager.setExactAndAllowWhileIdle()` registers the RTC wake-up alarm.
9. Only after successful registration is the trigger timestamp persisted in `ScheduledCallStore`.
10. Home reads/displays the persisted next call.

## Edit / reschedule lifecycle

When a future call already exists:

1. Home shows the persisted next-call date/time and exposes `Edit scheduled call`.
2. Schedule screen initializes from the current persisted trigger rather than resetting to a new default.
3. User changes date and/or time.
4. `ScheduledCallScheduler.schedule()` registers the new trigger using the same scheduled-call `PendingIntent` identity.
5. Android therefore replaces the previous pending alarm rather than keeping both A and B.
6. The new trigger timestamp becomes the persisted source of truth.
7. Returning Home shows the updated value.

No secondary scheduler path or list of multiple alarms is introduced.

## Explicit cancellation lifecycle

Cancellation is owned by `ScheduledCallScheduler.cancel()`.

When user taps `Cancel scheduled call` on the Schedule screen:

1. Scheduler reconstructs the existing scheduled-call `PendingIntent` identity with `FLAG_NO_CREATE`.
2. If the pending intent exists, `AlarmManager.cancel()` removes the registered alarm.
3. The pending intent itself is cancelled.
4. `ScheduledCallStore.clear()` removes the durable future trigger timestamp.
5. Activity/UI scheduled state is cleared.
6. User returns to Home and `Next call` becomes `Not scheduled`.

Cancellation is intentionally safe when no OS alarm exists: persisted state is still cleared so the app cannot advertise a stale future call.

## Trigger lifecycle

When the scheduled alarm fires:

1. Android invokes `CallAlarmReceiver`.
2. Persisted scheduled timestamp is cleared immediately.
3. `IncomingCallService` starts through the normal foreground-service pipeline.
4. Service loads the current saved `CallConfig`.
5. Delivery continues through ringtone/vibration, CallStyle notification, Full Screen Intent when available and the call Activity.

A fired schedule is consumed once and must not remain visible as pending.

## Device reboot recovery

`AlarmManager` registrations do not survive reboot, so ExitCall declares `RECEIVE_BOOT_COMPLETED` and a Direct-Boot-aware recovery receiver for `LOCKED_BOOT_COMPLETED` and `BOOT_COMPLETED`.

The trigger timestamp is stored in device-protected storage so recovery can inspect it before first unlock.

On recovery:

1. `ScheduledCallRecoveryReceiver` calls the shared scheduler recovery path.
2. `ScheduledCallStore.load()` returns only a still-future timestamp; expired values are cleared.
3. Scheduler checks exact-alarm capability.
4. If allowed, the same scheduled-call `PendingIntent` identity and exact registration path are used again.
5. Existing persisted timestamp remains the durable state; recovery does not create a second record.

Receiving more than one valid recovery signal remains idempotent because the same `PendingIntent` identity is reused.

There is no catch-up call for a timestamp that passed while the device was unavailable. A stale call is discarded rather than surprising the user later.

### Direct Boot boundary

Direct Boot support covers scheduler metadata and alarm re-registration.

Normal `CallConfig`, ringtone configuration and the service/activity call flow remain in the existing credential-protected design. A call whose trigger occurs before first unlock after reboot is not claimed as a fully supported v1.0 scenario unless separately validated end-to-end.

## App-start recovery fallback

`MainActivity` calls `restorePersistedCall()` during startup.

This OEM fallback:

- repairs a missing OS alarm when a still-future persisted timestamp exists,
- uses the same pending-call identity as all other scheduling paths,
- does not create duplicate schedules,
- does nothing useful for expired state because the store clears it.

## Exact-alarm permission recovery

Android may revoke exact-alarm access and remove registered exact alarms. ExitCall listens for `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` when access is granted again.

Recovery then follows the same rules:

- future persisted timestamp -> re-register once,
- expired timestamp -> clear and do not fire,
- capability still unavailable -> leave valid future persisted state intact for a later recovery attempt.

## Configuration relationship

Scheduler stores only **when** a call should start.

The actual call presentation uses the current `CallConfigStore` values at delivery time, including:

- caller identity,
- phone number,
- ringtone URI/name,
- vibration preference.

This keeps scheduling metadata separate from user call configuration and avoids duplicate sources of truth.

## Ringtone relationship

PR #81 added the Android system ringtone picker, but it does not change scheduler architecture.

A scheduled ExitCall uses the same persisted ringtone selection as an immediate call. If the saved URI cannot be resolved, `IncomingCallService` falls back to the Android system default ringtone.

## Application identity boundary

Production Android identity is:

`com.mateuszbury.exitcall`

The former `com.example.exitcall` development build is a different Android app identity, so scheduler preferences and alarm permissions are not migrated from it.

## Reliability invariants

The scheduler preserves these invariants:

1. A newly scheduled timestamp is persisted only after successful alarm registration.
2. Only one v1.0 scheduled-call `PendingIntent` identity exists.
3. Rescheduling replaces the old OS alarm rather than creating a second call.
4. Explicit cancellation removes the OS alarm and clears persisted schedule state.
5. A valid persisted timestamp survives process death and reboot.
6. Scheduler recovery metadata is available from device-protected storage.
7. Reboot/exact-alarm/app-start recovery share the same registration path.
8. Expired schedules are cleared instead of replayed.
9. Triggering consumes the persisted schedule.
10. Scheduler recovery reads normal `CallConfig` at call-delivery time rather than duplicating caller/audio configuration.
11. Missing FSI never creates a second scheduler or call engine; presentation falls back through the notification path.

## Regression coverage

The maintained physical regression suite is issue #9. Scheduler-relevant coverage includes:

- future schedule creation,
- edit/reschedule replacement,
- explicit cancellation,
- process-death persistence,
- foreground/background/locked delivery,
- reboot recovery,
- expired-state cleanup,
- exact-alarm grant/recovery,
- post-trigger clearing,
- duplicate prevention.

Run targeted scheduler regression after changes to scheduling, persistence, capabilities, app startup or Android system integration, and run the full P0 release suite before the v1.0 release candidate.
