# ExitCall

ExitCall is an Android app that creates a realistic incoming-call experience to help a user leave an awkward or uncomfortable situation naturally.

## Current status

The core Android lock-screen trigger is already working:

- exact `AlarmManager` alarm can trigger while the phone is locked,
- `CallAlarmReceiver` starts the call flow,
- `IncomingCallService` runs as a foreground service,
- system ringtone and vibration work on the lock screen,
- high-priority notification and Full Screen Intent work,
- `IncomingCallActivity` wakes the display and shows `IncomingCallScreen` over the lock screen,
- Decline stops ringtone/vibration, closes the call screen and leaves the phone locked.

The current critical milestone is completing:

`scheduled trigger -> incoming call -> Answer -> ActiveCallScreen -> Hang Up -> Android`

## Stack

- Kotlin
- Jetpack Compose / Material 3
- Android AlarmManager
- BroadcastReceiver
- Foreground Service
- Notifications / Full Screen Intent

## Product direction

The MVP focuses on:

- a convincing incoming-call experience,
- reliable scheduling while the app is not in the foreground,
- correct behaviour on a locked device,
- configurable caller name/number/ringtone/vibration,
- a small, privacy-friendly configuration surface.

Product wording should use **ExitCall**, **scheduled incoming call** or **safety call** depending on context.

## Development workflow

GitHub Issues are the source of truth for actionable work.

Priority convention:

- `P1` — MVP blocker / reliability-critical
- `P2` — MVP quality / important product functionality
- `P3` — Beta / post-MVP product improvement
- `P4` — future / exploratory / icebox

Daily rule: keep **one large task or at most two small tasks** active at once.

See:

- [`BACKLOG.md`](BACKLOG.md) — roadmap and issue map
- [`PROJECT_WORKFLOW.md`](PROJECT_WORKFLOW.md) — GitHub Project fields, statuses and views
- [`DECISIONS.md`](DECISIONS.md) — product and technical decisions