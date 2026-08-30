# Scheduler lifecycle and recovery

This document describes the maintained MVP scheduling path for ExitCall.

## Scope

The MVP supports one pending scheduled call at a time. Explicit edit/cancel UX remains tracked separately in issue #12.

The persisted trigger timestamp is the durable source of truth for whether a future call still needs to exist across process death and device reboot. `AlarmManager` is the operating-system execution mechanism, not the durable source of truth.

## Components

- `ScheduledCallStore` persists the future trigger timestamp.
- `ScheduledCallScheduler` owns exact-alarm capability checks, `PendingIntent` creation and `AlarmManager` registration.
- `CallAlarmReceiver` handles the actual alarm trigger, clears the persisted schedule and starts `IncomingCallService`.
- `ScheduledCallRecoveryReceiver` restores a valid persisted call after device boot and after exact-alarm access is granted again.
- `IncomingCallService` and the call Activity continue to read the normal persisted `CallConfig`; reboot recovery does not create a second configuration path.

## Normal scheduling

1. The user chooses a future date/time in `ScheduleCallScreen`.
2. `MainActivity` runs the normal capability gate.
3. `ScheduledCallScheduler.schedule()` checks exact-alarm access again at registration time.
4. The scheduler creates the single MVP `PendingIntent` for `CallAlarmReceiver`.
5. `AlarmManager.setExactAndAllowWhileIdle()` registers the RTC wake-up alarm.
6. Only after successful registration is the trigger timestamp persisted in `ScheduledCallStore`.
7. Home reads the persisted timestamp and displays the next call.

Using one shared scheduler keeps normal registration and recovery on the same exact-alarm and `PendingIntent` path.

## Trigger lifecycle

When the alarm fires:

1. Android invokes `CallAlarmReceiver`.
2. The persisted scheduled timestamp is cleared immediately.
3. `IncomingCallService` starts through the existing foreground-service pipeline.
4. The service loads the current saved `CallConfig` and continues through notification / Full Screen Intent / call Activity behavior.

A scheduled call is therefore consumed once. The persisted timestamp must not remain after the trigger.

## Device reboot recovery

`AlarmManager` registrations do not survive a device reboot, so ExitCall declares `RECEIVE_BOOT_COMPLETED` and a manifest receiver for `BOOT_COMPLETED`.

On boot:

1. `ScheduledCallRecoveryReceiver` asks `ScheduledCallScheduler` to restore the persisted call.
2. `ScheduledCallStore.load()` returns only a still-future trigger timestamp. Expired values are cleared and are not replayed.
3. The shared scheduler checks exact-alarm capability.
4. If capability is available, the same `PendingIntent` and `setExactAndAllowWhileIdle()` registration path is used again.
5. The existing persisted timestamp is retained; recovery does not create a second schedule record.

There is deliberately no catch-up behavior for a call whose trigger time passed while the device was unavailable. A stale scheduled call is discarded rather than firing unexpectedly after boot.

## Exact-alarm permission recovery

Android can revoke `SCHEDULE_EXACT_ALARM`, and revocation removes exact alarms that were already registered. ExitCall therefore also listens for `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, which Android sends when exact-alarm access is granted.

When that broadcast arrives, ExitCall runs the same persisted-call recovery path:

- if the persisted trigger is still in the future, it is registered again;
- if the trigger is already past, it is cleared and not fired;
- if exact-alarm capability is still unavailable when checked, the persisted timestamp is left intact so a later valid recovery attempt can still succeed.

## Reliability invariants

The scheduler should preserve these invariants:

1. A newly scheduled timestamp is persisted only after `AlarmManager` registration succeeds.
2. A valid persisted timestamp survives process death and reboot.
3. Reboot recovery never creates a second `PendingIntent` identity for the same MVP scheduled call.
4. Expired persisted schedules are cleared instead of replayed.
5. Triggering the call consumes the persisted schedule.
6. Normal scheduling and recovery share the same exact-alarm registration code.
7. Reboot recovery uses the normal persisted `CallConfig` source rather than duplicating caller/ringtone/vibration configuration.

## Manual verification for issue #10

Before closing issue #10, verify on the primary Android device:

1. Schedule a call several minutes in the future and confirm Home shows the expected `Next call`.
2. Reboot the device before the trigger time.
3. Do not manually recreate the schedule after reboot.
4. Confirm the incoming call starts at the originally selected time.
5. Confirm the normal Answer / Decline flow still works.
6. Confirm Home returns to `Not scheduled` after the recovered call fires.
7. Optional hardening check: schedule another future call, revoke exact-alarm access, grant it again before the trigger, and confirm the pending call is restored without creating a duplicate.

The broader repeatable regression suite is tracked in issue #9 and should reference this lifecycle once formal test cases are written.
