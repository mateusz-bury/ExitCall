# ExitCall

ExitCall is a native Android safety app that provides a user-configured incoming-call experience to help someone leave an awkward or uncomfortable situation naturally.

## Current v1.0 state

The core Android call pipeline is working on physical hardware and P2 product-quality work is active.

Current merged baseline includes:

- immediate calls through one shared foreground-service / Activity pipeline,
- exact scheduled calls through `AlarmManager`,
- one pending scheduled call with edit, reschedule and explicit cancel,
- persisted scheduler state with device-reboot recovery,
- Notifications as a hard requirement for delivery,
- Full Screen Intent where Android permits it with CallStyle notification fallback otherwise,
- exact-alarm capability required only for future scheduling,
- unified Answer / Decline / Android-Back / timeout cleanup,
- privacy-friendly Android single-phone contact picker without broad Contacts permission,
- phone-number validation, normalization and display formatting,
- production Settings with persisted global ringtone, vibration and 5–60 s ring duration,
- scheduled-call snapshots of global alert settings,
- polished Incoming and Active call presentation based on native Android call references,
- consistent app navigation headers and Home Settings gear action,
- production Android identity `com.mateuszbury.exitcall`.

Recently completed P2 slices:

- #12 scheduler edit/reschedule/cancel in PR #80,
- #13 system ringtone picker in PR #81,
- #14 privacy-friendly single-phone contact picker in PR #82,
- #15 phone validation/formatting in PR #83,
- #58 Settings + configurable timeout in PR #84,
- incoming-call lifecycle consistency in PR #85,
- navigation/header polish in PR #86,
- Incoming/Active call presentation polish in PR #87.

Current important remaining P2 work includes Active Call control behavior (#22), Settings/help integrations (#23), missed-call behavior (#53), custom local audio (#76), Home dashboard (#30), regression maintenance (#9) and remaining capability/portrait verification (#8/#59).

## Stack

- Kotlin
- Jetpack Compose / Material 3
- Android AlarmManager
- BroadcastReceiver
- Foreground Service
- NotificationCompat CallStyle / Full Screen Intent
- SharedPreferences + Device Protected Storage

## Repository layout

- `app/` - Android application source
- `docs/architecture/` - maintained architecture and product/technical decisions
- `docs/process/` - backlog and project workflow
- `docs/specifications/` - historical versioned specification snapshots

## Documentation

See [`docs/README.md`](docs/README.md) for the documentation index.

GitHub Issues are the source of truth for actionable work. The maintained roadmap is in [`docs/process/BACKLOG.md`](docs/process/BACKLOG.md).
