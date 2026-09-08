# ExitCall release checklist

Use this checklist for every release candidate and after any production dependency, permission or data-flow change.

## Open-source notices

- [ ] Run `./gradlew :app:dependencies --configuration releaseRuntimeClasspath`.
- [ ] Compare the result with `app/build.gradle.kts` and `gradle/libs.versions.toml`.
- [ ] Update the in-app Open-source licenses inventory, versions, copyright notices and license destinations.
- [ ] Exclude test, debug and build-only tooling unless its license explicitly requires distribution in the app.
- [ ] Verify Settings → Open-source licenses is readable and Back returns to Settings.
- [ ] Verify the complete Apache 2.0 license link and its visible fallback address.

## Privacy, safe use and support

- [ ] Compare the in-app privacy summary with `AndroidManifest.xml`, backup rules and current local persistence.
- [ ] Confirm every described Android capability matches current runtime behavior.
- [ ] Verify the safe-use text still states that ExitCall is not an emergency service.
- [ ] Verify the support destination and visible fallback destination.
- [ ] Verify About displays the production app name, author and current version.
