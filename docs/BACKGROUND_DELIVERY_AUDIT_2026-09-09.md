# Background execution and locked-screen delivery audit (#99 / PR #112)

## Evidence and limits

Reported on Motorola: a scheduled call rings/vibrates with the screen locked, but the display does not wake when background use is disabled. No device model, Android version, notification-channel dump or activity-launch logs were supplied. This proves alert playback occurred, NOT why full-screen presentation failed. The earlier claim that `isBackgroundRestricted` specifically explains this symptom was not established and is withdrawn.

This audit inspected the complete source tree at `4be922713d20c11b59144ec9bc69ada21130ef43`, including the production receiver/service/activity path, recovery, capability resolution, onboarding/demo, Home, Settings, notification consumers, manifest, target SDK and existing tests.

## Findings before changes

| Finding | Evidence | Resolution |
| --- | --- | --- |
| High: battery status was advisory only | BatteryOptimizationResolver was only wired into Settings through MainActivity/MainApp. CallCapability and onboarding knew nothing about background restrictions. | Add BACKGROUND_EXECUTION and shared CallReadiness policy. |
| High: notification readiness ignored its channel | Resolver checked POST_NOTIFICATIONS and app-wide enablement; service independently plays audio. A disabled/low-importance channel can prevent appropriate visual controls while playback continues. | Ensure the existing channel early; check high importance and group blocking; route to channel settings. Recheck before playback after FGS promotion. |
| High: Doze exception was described as unrestricted execution | `isIgnoringBatteryOptimizations` was treated as a general Ready signal. It does not establish FSI eligibility or all OEM permissions. | Separate required background access from optional Doze guidance and correct copy. |
| Medium: settings preflight could skip a launchable destination | `resolveActivity` was used before launch; package visibility can affect queries. | Attempt each intent, catch actual launch failure, try app details before unrelated global list; show manual instructions if none launch. |
| Medium: screen navigation was lost on recreation | MainApp screen used remember while changing system settings can recreate the process/activity. | rememberSaveable for current destination; no automatic replay of transient pending actions after process death. |
| High: no evidence distinguished notification publication from Activity entry | FSI permission log existed, but no structured state at alarm/service/Activity boundaries. | Add ExitCallDelivery diagnostics including separate background, Doze, notification, channel, FSI, lock and interactive state. |
| Medium: receiver did not handle service-start refusal | Background FGS launch can throw; receiver cleared the elapsed schedule first. | Catch and log refusal; do not crash/replay an elapsed call unexpectedly. |

## Permission contract

- **Required:** notifications, high-importance Incoming calls channel, Android-reported background execution allowed.
- **Required only when scheduling:** exact alarms.
- **Recommended, not mandatory:** full-screen access; preserve the established notification fallback when denied.
- **Recommended:** Doze optimization exemption. Not a substitute for background access or FSI.
- On API 28+, background execution uses `ActivityManager.isBackgroundRestricted`; a query failure is unknown and does not grant access. API 24–27 has no equivalent standard user restriction query, so the standard gate is not applicable there. This cannot verify proprietary OEM switches that are not exposed by Android.
- No new manifest exemption, overlay, wake-lock, hidden permission or direct Activity launch from the service is introduced. The existing notification FSI remains the supported presentation path.
- The requirement is an app setup prerequisite, not a new runtime permission dialog. Opening settings alone never marks it granted.

## Dependency impact and decisions

| Consumer | Behavior after change |
| --- | --- |
| OnboardingScreen | Notifications + background gate the permission page, swipe range and final action. Background card opens system settings. |
| MainActivity | Refresh on return; fresh shared gate before demo and before completion writes; preserve caller reset only after successful first-run completion. |
| Settings tutorial replay | Shows the new card but stays informational, completes without a demo or data reset even with permissions denied. |
| Immediate call / Schedule UI | Shared capability policy gates both. Exact alarms only for Schedule. Cancel remains available. |
| Home | Required background issue is visible and actionable even for users who already completed onboarding. No forced onboarding replay/migration. |
| Settings | Separate Background use row and Doze guidance; same source of state as call gates. |
| Scheduler / boot recovery | Existing registration/persistence logic retained. Do not delete a future schedule merely because background policy changed. Best-effort delivery of an already requested call remains appropriate; OS can refuse execution. |
| CallAlarmReceiver | Logs live state at fire time and handles actual service-launch refusal. Elapsed schedule remains consumed, as before; no delayed surprise replay. |
| IncomingCallService | Shares the existing v2 channel; adds content tap target; rechecks notification controls before sound/vibration. Does not bypass revoked background restrictions or force-start an Activity. |
| IncomingCallActivity | Existing showWhenLocked/turnScreenOn retained; logs create/newIntent/resume to distinguish non-launch from non-wake. |
| DemoCallFlow | Remains in-app, lifecycle-cancelled; its successful display is not a lock-screen/FSI test. |
| MissedCallNotifier | Separate channel and timeout-only semantics retained. Failed service start or unavailable controls is not classified as an unanswered call. |
| Audio / DND / timeout | Audio route ownership and timeout policy unchanged. No DND bypass. |

## Verification status

Added five JVM policy tests and two Compose background-gate tests; updated the three existing onboarding regression tests with an explicit background capability and callback. Static resource-reference, XML and call-site checks are run before publication. This environment has Java but no Android SDK, Gradle or Kotlin compiler; neither Android compilation nor JVM/Compose execution is claimed. Physical wake-up behavior remains unverified.

## Motorola acceptance and diagnosis

1. Record model and Android version. Install this PR, not main.
2. Turn background use OFF. Return to ExitCall: Home and Settings must report Action required; Start call and submitting a Schedule must route to background settings. Opening Schedule remains possible so a pending call can be cancelled.
3. First-run onboarding: allow notifications but deny background; Continue and swiping past permissions must stay blocked. Enabling background unlocks progress without requiring FSI or a Doze exemption.
4. Revoke background while on the final onboarding page: demo must not start. Tutorial replay must still finish without resetting caller/schedule/demo state.
5. Enable background and FSI. In Android notification settings set Incoming calls to high importance/allow pop-ups. Schedule, lock screen, test; also test screen unlocked (heads-up is a valid OS outcome).
6. Repeat with FSI OFF: notification fallback is expected; battery access does not repair FSI permission.
7. Disable/lower Incoming calls channel: onboarding/start/schedule must surface notification settings; revoke after scheduling and verify there is no ringtone without accessible controls.
8. Test notification Answer/Decline/content tap, timeout/missed-call toggle, future schedule recovery after reboot and return from settings after Activity recreation.

If the screen still does not wake, capture during the failure (stop with Ctrl+C):

```powershell
adb logcat -v time ExitCallDelivery:I ActivityTaskManager:W NotificationService:W '*:S'
```

- `alarm-received` absent: alarm/OS scheduling investigation.
- `alarm-received`, no `service-start`: service-start refusal; inspect logged exception.
- `notification-posted`, no `activity-created`/`activity-new-intent`: FSI/channel/system launch-policy investigation, not a ringtone problem.
- `activity-resumed` present but screen dark: investigate wake/display/OEM behavior.
- UI reports allowed while the OEM switch is OFF: API does not reflect that switch (or device-specific semantics differ). Do not relabel a Doze exemption as proof; record the OEM gap under #26 with logs before defining a workaround.

## Primary references

- [ActivityManager.isBackgroundRestricted](https://developer.android.com/reference/android/app/ActivityManager#isBackgroundRestricted()) — standard restriction query, API 28+.
- [Doze and standby](https://developer.android.com/training/monitoring-device-state/doze-standby) — optimization is distinct from background and presentation eligibility.
- [Full-screen intent limits](https://source.android.com/docs/core/permissions/fsi-limits) — grant state and screen-state dependent presentation.
- [Activity launch security](https://developer.android.com/guide/components/activities/secure-bal) — sender/creator privileges and system logs. Missing creator opt-in alone is not proof of failure when the system sends a notification PendingIntent; no broad opt-in is added speculatively.
- [Package visibility](https://developer.android.com/training/package-visibility/use-cases) — querying an Activity and actually launching it are different operations.
