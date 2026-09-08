# ExitCall

ExitCall is a native Android safety app that provides a user-configured incoming-call experience to help someone leave an awkward or uncomfortable situation naturally.

## Current v1.0 state

The core Android call pipeline works on physical hardware. P2 product-quality work is active and the onboarding/demo workstream is in final verification.

Current merged baseline includes:

- immediate and exact scheduled calls through one shared foreground-service / Activity pipeline,
- one persisted pending schedule with edit, reschedule, cancel and reboot recovery,
- Notifications as a hard delivery requirement,
- Full Screen Intent where Android permits it, with high-priority CallStyle notification fallback,
- exact-alarm access required only for precise future scheduling,
- unified Answer / Decline / Android-Back / timeout cleanup,
- privacy-friendly Android single-phone contact selection and shared phone-number validation,
- production Settings for ringtone, vibration and 5–60 s ring duration,
- polished Incoming and Active call surfaces with the persistent `EC` safety marker,
- functional Active Call keypad, Mute, route picker and More panel,
- real Phone / Speaker / Bluetooth audio routing with route-state synchronization and cleanup,
- mandatory first-run onboarding and a controlled ExitCall demo with guided local audio,
- Settings tutorial replay of the demo.

Most recent merged work:

- PR #94: onboarding foundation,
- PR #95: controlled onboarding demo,
- PR #97: demo communication-audio routing, Bluetooth ringtone fix and functional playback Mute.

PR #98 is open for final build/device verification. It audits the complete onboarding/demo flow, hardens demo cancellation/timeout cleanup and adds notification-gate instrumentation coverage.

Current focus:

1. accept PR #98 on a physical device,
2. record the remaining Phone ↔ Speaker ↔ Bluetooth and lifecycle checks,
3. refresh/execute the physical regression suite,
4. continue with missed-call behavior (#53), custom local audio (#76) and the production Home dashboard (#30).

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
