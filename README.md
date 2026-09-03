# ExitCall

ExitCall is an Android safety app that provides a user-configured incoming-call experience to help someone leave an awkward or uncomfortable situation naturally.

## Current v1.0 state

The core Android flow is working on physical hardware and P2 product-quality work is active.

Current merged baseline includes:

- immediate calls through the shared foreground-service / Activity pipeline,
- scheduled calls through exact `AlarmManager` alarms,
- persisted scheduler state with edit, reschedule and explicit cancel support,
- device-reboot recovery for pending future schedules,
- high-priority CallStyle notifications,
- Full Screen Intent presentation when available,
- usable CallStyle notification fallback when FSI is unavailable,
- Notifications as a hard requirement for call delivery,
- exact-alarm capability required only for scheduled calls,
- working Answer, Decline, Hang Up and unanswered-timeout cleanup,
- persisted caller name, phone number, ringtone URI/name and vibration preference,
- Android system ringtone picker with system-default fallback,
- one shared saved `CallConfig` used by immediate and scheduled calls,
- production Android identity `com.mateuszbury.exitcall`.

Recently completed P2 slices:

- #12 scheduled-call edit/reschedule/cancel, merged in PR #80,
- #13 Android system ringtone picker, physically verified and merged in PR #81.

Next planned configuration slice: #14 privacy-friendly single-contact picker.

## Stack

- Kotlin
- Jetpack Compose / Material 3
- Android AlarmManager
- BroadcastReceiver
- Foreground Service
- CallStyle Notifications / Full Screen Intent

## Repository layout

- `app/` - Android application source
- `docs/architecture/` - maintained architecture and product/technical decisions
- `docs/process/` - backlog and project workflow
- `docs/specifications/` - historical versioned specification snapshots

## Documentation

See [`docs/README.md`](docs/README.md) for the documentation index.

GitHub Issues are the source of truth for actionable work. The maintained roadmap is in [`docs/process/BACKLOG.md`](docs/process/BACKLOG.md).
