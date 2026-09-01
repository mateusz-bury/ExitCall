# Scheduler lifecycle and recovery

This document describes the maintained MVP scheduling path for ExitCall.

## Scope

The MVP supports one pending scheduled call at a time. Explicit edit/cancel UX remains tracked separately in issue #12.

The persisted trigger timestamp is the durable source of truth for whether a future call still needs to exist across process death and device reboot. `AlarmManager` is the operating-system execution mechanism, not the durable source of truth.

## Components

- `ScheduledCallStore` persists the future trigger timestamp in device-protected `SharedPreferences` so recovery metadata is available during Direct Boot.
- `ScheduledCallScheduler` owns exact-alarm capability checks, `PendingIntent` creation and `AlarmManager` registration.
- `CallAlarmReceiver` handles the actual alarm trigger, clears the persisted schedule and starts `IncomingCallService`.
- `ScheduledCallRecoveryReceiver` restores a valid persisted call after locked boot, normal boot and after exact-alarm access is granted again.
- `MainActivity` performs an idempotent recovery attempt on app startup as an OEM/device fallback.
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

`AlarmManager` registrations do not survive a device reboot. ExitCall therefore declares `RECEIVE_BOOT_COMPLETED` and a Direct-Boot-aware manifest receiver for both `LOCKED_BOOT_COMPLETED` and `BOOT_COMPLETED`.

The scheduled trigger timestamp is stored in device-protected storage so the recovery receiver can read it before the first post-reboot unlock.

On either boot recovery broadcast:

1. `ScheduledCallRecoveryReceiver` asks `ScheduledCallScheduler` to restore the persisted call.
2. `ScheduledCallStore.load()` returns only a still-future trigger timestamp. Expired values are cleared and are not replayed.
3. The shared scheduler checks exact-alarm capability.
4. If capability is available, the same `PendingIntent` and `setExactAndAllowWhileIdle()` registration path is used again.
5. The existing persisted timestamp is retained; recovery does not create a second schedule record.

The same `PendingIntent` identity is reused, so receiving both locked-boot and normal-boot recovery broadcasts remains idempotent rather than producing duplicate scheduled calls.

There is deliberately no catch-up behavior for a call whose trigger time passed while the device was unavailable. A stale scheduled call is discarded rather than firing unexpectedly after boot.

### Direct Boot boundary

Issue #10 uses Direct Boot only for **scheduler recovery metadata and alarm re-registration**.

The full incoming-call pipeline is not moved into device-protected storage. `CallConfig`, ringtone configuration and the normal call service/activity path remain in their existing credential-protected design. The maintained MVP requirement is that a future scheduled call is restored across reboot and works after the user unlocks the device normally before its trigger time.

A call whose trigger time occurs before the first unlock after reboot is not yet claimed as a supported MVP scenario and requires separate end-to-end validation before that guarantee can be made.

## App-start recovery fallback

`MainActivity` also calls `restorePersistedCall()` during startup. This is intentionally idempotent and uses the same `PendingIntent` identity as every other registration path.

This fallback protects against devices or OEM software that fail to deliver the expected boot recovery broadcast while still preserving the persisted future schedule. Opening ExitCall can therefore repair a missing `AlarmManager` registration without creating a second schedule.

## Exact-alarm permission recovery

Android can revoke `SCHEDULE_EXACT_ALARM`, and revocation removes exact alarms that were already registered. ExitCall therefore also listens for `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, which Android sends when exact-alarm access is granted.

When that broadcast arrives, ExitCall runs the same persisted-call recovery path:

- if the persisted trigger is still in the future, it is registered again;
- if the trigger is already past, it is cleared and not fired;
- if exact-alarm capability is still unavailable when checked, the persisted timestamp is left intact so a later valid recovery attempt can still succeed.

## Reliability invariants

The scheduler should preserve these invariants:

1. A newly scheduled timestamp is persisted only after `AlarmManager` registration succeeds.
2. A valid persisted timestamp survives process death and device reboot.
3. Scheduler recovery metadata is available from device-protected storage during Direct Boot.
4. Reboot recovery never creates a second `PendingIntent` identity for the same MVP scheduled call.
5. Expired persisted schedules are cleared instead of replayed.
6. Triggering the call consumes the persisted schedule.
7. Normal scheduling, boot recovery, exact-alarm permission recovery and app-start fallback share the same exact-alarm registration code.
8. Reboot recovery uses the normal persisted `CallConfig` source rather than duplicating caller/ringtone/vibration configuration.

## Manual verification for issue #10

Before closing issue #10, verify on the primary Android device:

1. Install/run the current branch build and complete required capabilities.
2. Schedule a call several minutes in the future and confirm Home shows the expected `Next call`.
3. Reboot the device before the trigger time.
4. Unlock the device normally; do not manually recreate the schedule.
5. Confirm the incoming call starts at the originally selected time exactly once.
6. Confirm the normal Answer / Decline flow still works.
7. Confirm Home returns to `Not scheduled` after the recovered call fires.
8. Hardening check: repeat while confirming `ExitCallRecovery` receives a locked-boot or normal-boot recovery action and that `dumpsys alarm` contains one ExitCall alarm after reboot.
9. Optional permission check: schedule another future call, revoke exact-alarm access, grant it again before the trigger, and confirm the pending call is restored without creating a duplicate.
10. OEM fallback check: if a device fails to restore the alarm from boot broadcast, launch ExitCall before the trigger and confirm app-start recovery restores exactly one pending alarm.

The broader repeatable regression suite is tracked in issue #9 and should reference this lifecycle.
