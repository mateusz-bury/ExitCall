# ExitCall

ExitCall is an Android app that provides a realistic simulated incoming-call experience to help a user leave an awkward or uncomfortable situation naturally.

## Current MVP state

The core Android flow is working on a physical device:

- immediate calls use the foreground-service / activity pipeline,
- scheduled calls use exact `AlarmManager` alarms,
- the lock-screen flow uses a high-priority call notification and Full Screen Intent,
- Answer, Decline, Hang Up and unanswered timeout cleanup are implemented,
- Call Setup persists caller name, phone number, ringtone and vibration preferences,
- scheduled-call state persists in the app and is shown on Home and in the scheduler,
- immediate and scheduled calls use the same saved call configuration.

Device-reboot restoration for pending scheduled alarms is tracked separately.

## Stack

- Kotlin
- Jetpack Compose / Material 3
- Android AlarmManager
- BroadcastReceiver
- Foreground Service
- Notifications / Full Screen Intent

## Repository layout

- `app/` - Android application source
- `docs/` - architecture, workflow, backlog and MVP specifications

## Documentation

See [`docs/README.md`](docs/README.md) for the documentation index.

GitHub Issues are the source of truth for actionable work. The maintained priority map is in [`docs/process/BACKLOG.md`](docs/process/BACKLOG.md).
