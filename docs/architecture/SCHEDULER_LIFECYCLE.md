# Scheduler lifecycle and recovery

This document describes the maintained v1.0 scheduling path for ExitCall.

## Validation status

The core scheduler and reboot-recovery design was physically verified on the primary Motorola device. Scheduler management was extended in PR #80. PR #84 added scheduled snapshots of global alert settings.

Maintained behavior includes:

- one future scheduled trigger at a time,
- selected date/time persists and is shown on Home,
- edit/reschedule replaces the previous alarm,
- explicit cancellation removes the Android alarm and persisted schedule,
- future scheduler metadata survives process death and reboot,
- recovery re-registers exactly one future `RTC_WAKEUP` alarm,
- exact-alarm permission restoration can recreate the same future alarm,
- expired calls are not replayed late,
- firing consumes the persisted scheduled state,
- ringtone/vibration/ring-duration settings are captured when scheduling so later global changes do not mutate an already pending call.

## Scope and ownership

v1.0 supports **one pending scheduled ExitCall at a time**.

`ScheduledCallStore` persists two categories of data in device-protected storage:

1. trigger time,
2. `AppSettings` snapshot for the pending call:
   - ringtone URI/name,
   - vibration,
   - ring duration.

The persisted trigger is the durable source of truth for whether a future call should exist. `AlarmManager` is the OS execution mechanism, not the durable source of truth.

Caller identity is intentionally different: caller name/phone remain current `CallConfigStore` data and are read when the call is delivered.

## Components

- `ScheduledCallStore` persists trigger time + scheduled `AppSettings` snapshot in device-protected `SharedPreferences`.
- `ScheduledCallScheduler` owns capability checks, one `PendingIntent` identity, registration, replacement, cancellation and recovery.
- `CallAlarmReceiver` reads the scheduled settings snapshot, clears the pending schedule, then starts `IncomingCallService` with that snapshot.
- `ScheduledCallRecoveryReceiver` restores a valid future call after locked boot, normal boot and exact-alarm access restoration.
- `MainActivity` performs an idempotent recovery attempt on startup as an OEM/device fallback and keeps Compose-facing scheduled state in sync.
- `MainApp` wires Home/Schedule UI actions to Activity-owned scheduler operations.
- `IncomingCallService` receives the scheduled settings snapshot when available and still reads caller identity from `CallConfigStore`.

## Capability policy

Scheduling uses the shared v1.0 capability policy:

1. Notifications are required.
2. Full Screen Intent is recommended but not mandatory; unavailable FSI falls back to the high-priority CallStyle notification path.
3. Exact-alarm capability is required for exact future scheduling.

Immediate calls do not depend on exact-alarm access.

## Normal scheduling

1. User opens `ScheduleCallScreen`.
2. With no pending call, the screen initializes to a future candidate time.
3. User selects date/time with Material date/time pickers.
4. Past date/time is rejected.
5. `MainActivity` runs the scheduling capability gate.
6. Current `AppSettings` are supplied to `ScheduledCallScheduler.schedule(triggerTimeMillis, settingsSnapshot)`.
7. Scheduler checks exact-alarm access again at registration time.
8. Scheduler creates/reuses the one broadcast `PendingIntent` for `CallAlarmReceiver`.
9. `AlarmManager.setExactAndAllowWhileIdle()` registers the RTC wake-up alarm.
10. Only after successful registration does `ScheduledCallStore.save(...)` persist trigger + settings snapshot.
11. Home displays the persisted next call.

This ordering prevents a false `Next call` state when exact alarm registration fails.

## Edit / reschedule lifecycle

When a future call already exists:

1. Home shows the persisted next-call time and exposes `Edit scheduled call`.
2. Schedule screen initializes from the current persisted trigger.
3. User changes date and/or time.
4. The current global `AppSettings` are captured again.
5. Scheduler registers the new trigger using the same scheduled-call `PendingIntent` identity.
6. Android replaces the previous alarm rather than keeping A and B.
7. New trigger + new settings snapshot replace the old persisted scheduled metadata.
8. Home shows the updated time.

Therefore editing/rescheduling is the point at which a pending call adopts newer global ringtone/vibration/timeout values.

## Explicit cancellation lifecycle

`ScheduledCallScheduler.cancel()` owns cancellation.

When the user taps `Cancel scheduled call`:

1. reconstruct the existing scheduled-call `PendingIntent` with `FLAG_NO_CREATE`,
2. cancel it in `AlarmManager` if present,
3. cancel the `PendingIntent`,
4. call `ScheduledCallStore.clear()`, removing trigger and all settings-snapshot fields,
5. clear Activity/UI scheduled state,
6. return Home to `Not scheduled`.

Cancellation remains safe when no OS alarm exists because persisted state is still cleared.

## Trigger lifecycle

When the alarm fires:

1. Android invokes `CallAlarmReceiver`.
2. Receiver loads the scheduled `AppSettings` snapshot.
3. Receiver clears the persisted trigger/snapshot immediately so the schedule is consumed once.
4. Receiver starts `IncomingCallService` and attaches the settings snapshot to the service intent when present.
5. Service loads current caller identity from `CallConfigStore`.
6. Service uses the supplied scheduled settings snapshot for ringtone, vibration and ring duration.
7. Delivery continues through CallStyle notification, ringtone/vibration, FSI where permitted and `IncomingCallActivity`.

If a legacy scheduled record has no settings snapshot, the service falls back to current `AppSettingsStore` values rather than failing delivery.

## Configuration semantics

A scheduled call intentionally combines two time semantics:

### Current-at-delivery data

Read from `CallConfigStore` when the service starts:

- caller name,
- phone number.

### Captured-at-schedule data

Read from the scheduled snapshot:

- ringtone URI/name,
- vibration preference,
- ring duration.

This is deliberate product behavior. Users can update caller identity without silently changing how an already scheduled call will ring, vibrate or time out.

## Device reboot recovery

`AlarmManager` registrations do not survive reboot, so ExitCall declares `RECEIVE_BOOT_COMPLETED` and a Direct-Boot-aware recovery receiver.

The trigger and settings snapshot are stored in device-protected storage.

Recovery path:

1. `ScheduledCallRecoveryReceiver` calls the shared scheduler recovery path.
2. `ScheduledCallStore.load()` returns only a still-future trigger; expired state is cleared.
3. Scheduler checks exact-alarm capability.
4. If allowed, the same `PendingIntent` identity and exact registration path are used again.
5. Existing persisted trigger/settings snapshot are retained as the durable scheduled state.

Repeated valid recovery signals are idempotent because the same `PendingIntent` identity is reused.

There is no catch-up call for a timestamp that passed while the device was unavailable.

### Direct Boot boundary

Direct Boot support covers scheduler metadata, including the settings snapshot required for scheduled alert behavior, and alarm re-registration.

Normal caller configuration and the regular service/activity UI remain in the credential-protected design. A trigger before first unlock is not claimed as a fully supported end-to-end v1.0 scenario unless separately validated.

## App-start recovery fallback

`MainActivity` calls the shared restore path during startup.

This OEM fallback:

- repairs a missing OS alarm when a still-future persisted trigger exists,
- reuses the same scheduled-call identity,
- preserves the original scheduled settings snapshot,
- does not create duplicate schedules,
- clears/ignores expired state through the store rules.

## Exact-alarm permission recovery

When Android grants exact-alarm access again:

- future persisted trigger -> re-register once,
- original settings snapshot remains attached to that pending schedule,
- expired trigger -> clear all scheduled metadata and do not fire,
- capability still unavailable -> keep valid future persisted state for a later restore attempt.

## Ringtone/timeout relationship

Global ringtone/vibration/ring-duration settings live in `AppSettingsStore`.

Immediate ExitCalls use the current global values. A newly scheduled call copies those values into `ScheduledCallStore`; the scheduled call then keeps that snapshot until it is fired, cancelled or edited/rescheduled.

If a stored ringtone URI cannot be resolved during delivery, `IncomingCallService` falls back to Android's system default ringtone.

## Application identity boundary

Production Android identity:

`com.mateuszbury.exitcall`

Former `com.example.exitcall` development builds are a different Android app identity, so their scheduler preferences/permissions are not part of production migration.

## Reliability invariants

1. Persist scheduled metadata only after successful exact-alarm registration.
2. Maintain one v1.0 scheduled-call `PendingIntent` identity.
3. Rescheduling replaces the previous OS alarm.
4. Rescheduling replaces the stored settings snapshot with current global values.
5. Explicit cancellation removes OS alarm + trigger + snapshot.
6. Valid future scheduled metadata survives process death and reboot.
7. Scheduler metadata is device-protected for recovery.
8. Boot/exact-alarm/app-start recovery use the same registration path.
9. Recovery preserves the original settings snapshot.
10. Expired schedules are cleared instead of replayed.
11. Triggering consumes scheduled metadata before starting normal call delivery.
12. Caller identity remains one current source of truth in `CallConfigStore`.
13. Missing FSI never creates a second scheduler/call engine.

## Regression coverage

Issue #9 is the maintained physical regression suite. Scheduler-relevant coverage should include:

- future schedule creation,
- edit/reschedule replacement,
- explicit cancellation,
- process-death persistence,
- foreground/background/locked delivery,
- reboot recovery,
- expired-state cleanup,
- exact-alarm grant/recovery,
- post-trigger clearing,
- duplicate prevention,
- scheduled settings snapshot behavior: schedule with A -> change global settings to B -> pending call still uses A,
- edit/reschedule after changing settings -> pending call then uses B.

Run targeted scheduler regression after changes to scheduling, persistence, Settings snapshots, capabilities, app startup or Android system integration, and run the full P0 suite before the v1.0 release candidate.
