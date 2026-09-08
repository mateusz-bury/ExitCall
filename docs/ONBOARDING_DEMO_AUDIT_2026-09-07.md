# Issue #29: onboarding and demo audit

Started: 2026-09-07. Final source review: 2026-09-08. Baseline: main after merged PR #97, commit 4c924d35bfc005b7f6ac08512606f80496e08947.
Scope: full issue #29, first-run MainActivity gate, OnboardingStore, OnboardingScreen and strings, SettingsScreen, MainApp, DemoCallFlow, and the audio components delivered in #97. Capability policy was checked against CallCapabilityResolver. This is source-level requirement coverage, not a measured test-coverage percentage.

## Correction to the previous backlog update

Replay was already possible through Settings → View tutorial again → onboarding pages → DemoCallFlow. MainApp handled that replay without calling OnboardingStore.markCompleted or saving call configuration. The user's repeated tests through Settings are valid. The remaining gap was a *direct* Replay demo call entry, not an absent replay engine.

## Findings and changes

| ID | Severity | Finding at baseline | Resolution in this branch |
| --- | --- | --- | --- |
| ONB-01 | Medium | Dedicated Settings replay action required by #29 was missing; indirect tutorial replay already worked. | Add a direct action; share the existing DemoCallFlow rendering branch. Completion/cancellation returns to Settings. Preserve tutorial replay and its cancellation destination. |
| ONB-02 | Medium | Final onboarding page said setup was ready and Get started, although it launched mandatory demo; custom recording/import and default-voice fallback were unexplained. | Replace only relevant copy in string resources with explicit demo instructions and later custom-audio education. |
| ONB-03 | High | DisposableEffect cleanup occurred only on composition removal. Home/background could leave demo ringtone/voice running, including delayed voice start. | Observe host ON_STOP; cancel demo and stop its audio. Share idempotent finish/cleanup between Back, Decline, Hang Up, timeout and background cancellation. |
| ONB-04 | Medium | Demo Incoming had no unanswered timeout. | Use demo AppSettings ring duration and the existing screen-keyed coroutine. Answer cancels the timeout; timeout cancels rather than completes onboarding. |
| ONB-05 | Medium | Notification gating only disabled Continue on the capability page. Last-page state could briefly remain actionable after permission revocation; tutorial completion relied only on cached UI capability state. | Gate every page at/after capability education and recheck capability at demo launch in both entry flows. |
| ONB-06 | Informational | First-run and tutorial replay both invoke DemoCallFlow. | Keep these two small adapters: only first-run is authorized to persist completion. No duplicate player/router implementation or extra navigation framework is justified. |

## Requirement-to-code coverage

Paths below are relative to app/src/main/java/com/mateuszbury/exitcall unless stated otherwise.

| Requirement in #29 | Evidence | Assessment |
| --- | --- | --- |
| Mandatory first launch before Home | MainActivity.onCreate/setContent reads model/OnboardingStore.isCompleted | Implemented; reinstall/process-death test pending |
| Persist completion | OnboardingStore completed_version >= 2; markCompleted only from first-run demo completion callback | Implemented; replay does not access store |
| Welcome/product purpose | OnboardingScreen.WelcomePage and values/strings.xml | Covered |
| EC vs genuine-call education | EcMarkerPage + shared Incoming/Active call surfaces | Covered in source; marker/layout physical regression pending |
| Required notifications | CapabilityPage + pager page count + launch-time resolver check | Covered; two instrumentation gate tests added |
| FSI recommended, notification fallback explained | CapabilityPage + CallCapabilityResolver (FSI is not a hard gate) | Covered; real notification delivery belongs to #8 |
| Exact alarm contextual | Explanation has no exact-alarm action; MainApp Schedule uses requireExactAlarm=true | Covered |
| Controlled demo caller/profile | DemoCallFlow creates its own CallConfig("ExitCall", empty number) and default settings | Covered; no user configuration passed into demo |
| One shared demo engine | MainActivity first-run and MainApp tutorial/direct replay call DemoCallFlow | Covered |
| Default English voice and selected transcript | DemoVoicePlayer uses committed res/raw/exitcall_demo_voice.mp3 | Asset and playback confirmed by user; voice/transcript not independently transcribed in this audit |
| Voice only after Answer | ACTIVE_CALL effect, one-second delay | Implemented; user heard voice after Answer |
| Hang Up/cancel/destroy cleanup | Shared finish function + DisposableEffect; background and timeout cancellation added | Implemented; full exit-path device matrix pending |
| First-run cannot complete through Decline/Back/timeout | Only ActiveCallScreen Hang Up invokes completed=true | Covered; Answer then immediate Hang Up is allowed by existing design |
| Demo never consumes trial | No entitlement or production call-service invocation in DemoCallFlow or replay callbacks | Isolated in current source; verify integration when #74/#75 land |
| Custom voice recording/import and fallback education | Revised ready-page hint | Covered as future capability; actual feature remains #76 |
| Dedicated Settings replay entry | SettingsScreen.onReplayDemo → MainApp.DEMO_REPLAY | Added here |
| Replay leaves onboarding/user data intact | Replay only changes MainApp.currentScreen, reuses in-memory config/settings; no save callback invoked | Source-level covered; persistence comparison test pending |
| Commercial model: demo + two calls + one-time purchase | TrialPage and string resources | Copy covered; enforcement/purchase remains #74/#75/#77 |
| Back/relaunch behavior | Replay Back/Decline return to origin; first-run cancellation does not mark complete; process death reloads persisted first-run gate | Safe restart by design; demo audio is not restored automatically |

## Refactor boundaries and remaining risks

The refactor is local: one finish path inside DemoCallFlow and a shared replay render branch. No new state store, Activity, ViewModel, dependency or global audio mutation was added.

Tutorial cancellation currently starts the tutorial again rather than preserving its final page. MainApp's in-memory navigation resets to Home on Activity recreation after completed onboarding. These are usability limitations, not data resets; they are left unchanged.

ON_STOP now cancels a demo when the app is hidden or the phone locks. It does not change scheduled-call service behavior. This must be explicitly checked on the user's device.

The existing audio engine still needs the #20/#21 device matrix: wired headphones, explicit Phone/Speaker/BT switching, device removal, SCO negotiation and cleanup after a genuine call interruption. The BT ringtone patch uses Android media routing and media volume; it is not a guarantee for every OEM or multiple simultaneous outputs.

The current demo-player mute is playback mute, not microphone mute. Scheduled-call audio content is a separate #76 concern. Future entitlements must continue excluding both replay paths.

## Verification and acceptance

Completed here: reviewed source paths and diff; XML parsed with unique resource names; diff whitespace validation. Confirmed existing APIs support the project's declared Lifecycle 2.6.1 dependency. No Kotlin build, instrumentation execution, emulator or physical-device test was possible here: Android SDK is unavailable. GitHub code search returned no results, so it cannot establish that no other tests/call sites exist.

Added OnboardingGateTest: denied notifications block progression; revocation from the final page blocks demo until permission returns. Tests are authored, not executed. Run :app:connectedDebugAndroidTest with the test class filter after building.

Physical checklist (all pending for this branch):
1. First-run: notifications OFF blocks continuation; grant notifications, leave FSI/exact alarms OFF, reach Start demo call.
2. First-run demo: Decline/Back/timeout/Home never completes onboarding. Answer → Hang Up completes it; relaunch skips onboarding.
3. Settings → View tutorial again still reaches demo; completion returns to Settings and cancellation returns to tutorial.
4. Settings → Replay demo call opens Incoming directly. Decline/Back/timeout/Hang Up return to Settings.
5. Save custom caller/settings and a future schedule before both replay paths; verify unchanged afterward and after restart. Verify future trial/Premium stores once implemented.
6. Home/lock during Incoming, the one-second delay and active voice: no continuing/delayed audio; returning does not auto-restart demo.
7. Answer shortly before timeout; rapid repeated Hang Up/Back: one exit, no late ringtone/voice, next demo starts cleanly.
8. Repeat known-good #97 earpiece, Bluetooth ringtone and Mute checks.
9. Revoke notifications from system settings during tutorial; returning cannot launch demo until re-granted.

Do not close #29 solely on this source audit. Its new replay and lifecycle behavior needs physical acceptance.

References: [Compose side effects](https://developer.android.com/develop/ui/compose/side-effects), [Lifecycle 2.6 release notes](https://developer.android.com/jetpack/androidx/releases/lifecycle).
