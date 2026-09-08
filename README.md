# ExitCall

ExitCall is a native Android safety app that provides a user-configured incoming-call experience to help someone leave an awkward or uncomfortable situation naturally.

## Current v1.0 state

The core Android call pipeline works on physical hardware. P2 product-quality work is active; onboarding, the first production Home dashboard and the Settings information surfaces are merged.

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
- Settings tutorial replay that finishes without launching another demo,
- a production Home dashboard with next-call, caller, sound and real readiness state,
- in-app privacy, safe-use, support, About and open-source notices.

Most recent merged work:

- PR #98: completed onboarding/demo flow and safe Settings tutorial replay,
- PR #105: production Home dashboard foundation,
- PR #106: Settings privacy/safe-use/support/About and open-source notices.

Current focus:

1. implement missed-call behavior and its Settings toggle (#53),
2. continue with local custom call audio (#76),
3. add trial/access gating (#74), followed by purchase restore and Premium ownership (#75),
4. keep the physical regression suite (#9) and remaining route/capability checks current.

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
