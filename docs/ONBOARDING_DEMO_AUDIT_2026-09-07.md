# Issue #29: onboarding and demo audit

Started: 2026-09-07. Final source review: 2026-09-08. Baseline: main after merged PR #97, commit 4c924d35bfc005b7f6ac08512606f80496e08947.
Scope: full issue #29, first-run MainActivity gate, OnboardingStore, OnboardingScreen and strings, SettingsScreen, MainApp, DemoCallFlow, and the audio components delivered in #97. Capability policy was checked against CallCapabilityResolver. This is source-level requirement coverage, not a measured test-coverage percentage.

## Correction to the previous backlog update

Settings → View tutorial again reuses the educational pages but finishes directly in Settings. It does not launch another demo. A dedicated or indirect post-onboarding demo was rejected because it creates an unlimited call-shaped path outside the trial model and makes the product feel prank-oriented.

## Findings and changes

| ID | Severity | Finding at baseline | Resolution in this branch |
| --- | --- | --- | --- |
| ONB-02 | Medium | Final onboarding page said setup was ready and Get started, although it launched mandatory demo; custom recording/import and default-voice fallback were unexplained. | Replace only relevant copy in string resources with explicit demo instructions and later custom-audio education. |
| ONB-03 | High | DisposableEffect cleanup occurred only on composition removal. Home/background could leave demo ringtone/voice running, including delayed voice start. | Observe host ON_STOP; cancel demo and stop its audio. Share idempotent finish/cleanup between Back, Decline, Hang Up, timeout and background cancellation. |
| ONB-04 | Medium | Demo Incoming had no unanswered timeout. | Use demo AppSettings ring duration and the existing screen-keyed coroutine. Answer cancels the timeout; timeout cancels rather than completes onboarding. |
| ONB-05 | Medium | Notification gating only disabled Continue on the capability page. Last-page state could briefly remain actionable after permission revocation. | Gate every page at/after capability education during first-run onboarding and recheck capability before its demo launch. Tutorial replay remains informational and does not require Notifications. |
| ONB-06 | High | Tutorial replay also invoked DemoCallFlow, creating an unlimited post-onboarding call-shaped path. | Keep DemoCallFlow exclusive to first-run onboarding. Settings tutorial replay ends with Finish and returns to Settings. |

## Requirement-to-code coverage

Paths below are relative to app/src/main/java/com/mateuszbury/exitcall unless stated otherwise.

| Requirement in #29 | Evidence | Assessment |
| --- | --- | --- |
| Mandatory first launch before Home | MainActivity.onCreate/setContent reads model/OnboardingStore.isCompleted | Implemented; reinstall/process-death test pending |
| Persist completion | OnboardingStore completed_version >= 2; markCompleted only from first-run demo completion callback | Implemented; tutorial replay does not access store |
| Welcome/product purpose | OnboardingScreen.WelcomePage and values/strings.xml | Covered |
| EC vs genuine-call education | EcMarkerPage + shared Incoming/Active call surfaces | Covered in source; marker/layout physical regression pending |
| Required notifications | CapabilityPage + pager page count + launch-time resolver check | Covered; two instrumentation gate tests added |
| FSI recommended, notification fallback explained | CapabilityPage + CallCapabilityResolver (FSI is not a hard gate) | Covered; real notification delivery belongs to #8 |
| Exact alarm contextual | Explanation has no exact-alarm action; MainApp Schedule uses requireExactAlarm=true | Covered |
| Controlled demo caller/profile | DemoCallFlow creates its own CallConfig("ExitCall", empty number) and default settings | Covered; no user configuration passed into demo |
| Demo limited to first run | Only MainActivity first-run state invokes DemoCallFlow; MainApp tutorial replay returns directly to Settings | Covered |
| Default English voice and selected transcript | DemoVoicePlayer uses committed res/raw/exitcall_demo_voice.mp3 | Asset and playback confirmed by user; voice/transcript not independently transcribed in this audit |
| Voice only after Answer | ACTIVE_CALL effect, one-second delay | Implemented; user heard voice after Answer |
| Hang Up/cancel/destroy cleanup | Shared finish function + DisposableEffect; background and timeout cancellation added | Implemented; full exit-path device matrix pending |
| First-run cannot complete through Decline/Back/timeout | Only ActiveCallScreen Hang Up invokes completed=true | Covered; Answer then immediate Hang Up is allowed by existing design |
| Demo never consumes trial | No entitlement or production call-service invocation in first-run DemoCallFlow callbacks | Isolated in current source; verify integration when #74/#75 land |
| Custom voice recording/import and fallback education | Revised ready-page hint | Covered as future capability; actual feature remains #76 |
| Tutorial replay leaves onboarding/user data intact | Replay only changes MainApp.currentScreen and returns to Settings; no demo or save callback is invoked | Source-level covered; persistence comparison test pending |
| Commercial model: demo + two calls + one-time purchase | TrialPage and string resources | Copy covered; enforcement/purchase remains #74/#75/#77 |
| Back/relaunch behavior | Tutorial replay Back returns to Settings; first-run demo cancellation does not mark complete; process death reloads persisted first-run gate | Safe restart by design; demo audio is not restored automatically |

## Refactor boundaries and remaining risks

The refactor keeps DemoCallFlow exclusive to the first-run gate. MainApp owns only informational tutorial replay and returns directly to Settings. No new state store, Activity, ViewModel, dependency or global audio mutation was added.


ON_STOP now cancels a demo when the app is hidden or the phone locks. It does not change scheduled-call service behavior. This must be explicitly checked on the user's device.

The existing audio engine still needs the #20/#21 device matrix: wired headphones, explicit Phone/Speaker/BT switching, device removal, SCO negotiation and cleanup after a genuine call interruption. The BT ringtone patch uses Android media routing and media volume; it is not a guarantee for every OEM or multiple simultaneous outputs.

The current demo-player mute is playback mute, not microphone mute. Scheduled-call audio content is a separate #76 concern. Future entitlements must continue excluding the one first-run demo.

## Verification and acceptance

Completed here: reviewed source paths and diff; XML parsed with unique resource names; diff whitespace validation. Confirmed existing APIs support the project's declared Lifecycle 2.6.1 dependency. No Kotlin build, instrumentation execution, emulator or physical-device test was possible here: Android SDK is unavailable. GitHub code search returned no results, so it cannot establish that no other tests/call sites exist.

Added OnboardingGateTest: denied notifications block first-run progression; revocation from the final page blocks its demo until permission returns; Settings tutorial replay can finish without notifications and without a demo. Tests are authored, not executed. Run :app:connectedDebugAndroidTest with the test class filter after building.

Physical checklist (all pending for this branch):
1. First-run: notifications OFF blocks continuation; grant notifications, leave FSI/exact alarms OFF, reach Start demo call.
2. First-run demo: Decline/Back/timeout/Home never completes onboarding. Answer → Hang Up completes it; relaunch skips onboarding.
3. Settings → View tutorial again shows all tutorial pages, ends with Finish and returns to Settings without an Incoming/Active call.
4. Save custom caller/settings and a future schedule before tutorial replay; verify unchanged afterward and after restart. Verify future trial/Premium stores once implemented.
6. Home/lock during Incoming, the one-second delay and active voice: no continuing/delayed audio; returning does not auto-restart demo.
7. During first-run demo, answer shortly before timeout; rapid repeated Hang Up/Back: one exit and no late ringtone/voice.
8. Repeat known-good #97 earpiece, Bluetooth ringtone and Mute checks.
9. Revoke notifications during first-run onboarding; it cannot launch the demo until re-granted. Settings tutorial replay remains readable and finishes without a demo.

Do not close #29 solely on this source audit. Its lifecycle behavior needs physical acceptance.

References: [Compose side effects](https://developer.android.com/develop/ui/compose/side-effects), [Lifecycle 2.6 release notes](https://developer.android.com/jetpack/androidx/releases/lifecycle).
