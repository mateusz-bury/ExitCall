# ExitCall

ExitCall is an Android app that simulates an incoming phone call to help the user leave an awkward or uncomfortable situation naturally.

## Current status

The Android lock-screen fake-call flow is already partially working:

- exact alarm can trigger while the phone is locked,
- foreground service starts the call flow,
- ringtone and vibration work on the lock screen,
- high-priority notification and Full Screen Intent work,
- the incoming-call screen appears over the lock screen,
- Decline stops the call and leaves the phone locked.

The next critical milestone is completing the full Answer -> Active Call -> Hang Up flow.

## Stack

- Kotlin
- Jetpack Compose
- Android AlarmManager
- Foreground Service
- Full Screen Intent

## Product direction

The MVP focuses on a convincing fake incoming-call experience, configurable caller details, scheduling, and reliable behaviour on a locked phone.

## Development workflow

GitHub Issues are the source of truth for actionable work.

Priority convention:

- `P1` — MVP blocker / do next
- `P2` — important for MVP quality
- `P3` — post-MVP / product improvement
- `P4` — future / exploratory

Daily rule: work on one large issue or at most two small issues at a time.

See [`BACKLOG.md`](BACKLOG.md) for the full roadmap.