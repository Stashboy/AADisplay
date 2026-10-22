# Changelog

## 0.23.15 (2026-10-22)

### Fixed
- **Activation fix (regression from v0.23.14).** The v0.23.14 rename missed the extensionless
  `assets/xposed_init` entry file, which still pointed at the old `io.github.nitsuya.aa.display`
  entry class. LSPosed could not find the module entry point, so v0.23.14 installed as **Not
  Activated** and hooked nothing. `xposed_init` now points at
  `io.github.stashboy.aa.pwned.xposed.XposedInit`; the module activates and hooks normally.

### Added
- **"Disable Waze on Android Auto" toggle (re-implemented).** When enabled, Waze keeps its map on
  the phone (visible on-device) while connected to Android Auto, instead of projecting to the car
  head unit, and is hidden from the Android Auto app list. When disabled, Waze projects to Android
  Auto as normal.
  - Implemented entirely **Android Auto-side**: a hook on Android Auto's own car-connection provider
    (`androidx.car.app.connection` / `ProjectionStateProvider`) reports "not connected" to **Waze
    only** (by calling package), so Waze runs on the phone; every other app still sees the true
    connection state. A second hook drops Waze from Android Auto's app discovery.
  - Uses only stable framework APIs and the `com.waze` package name — **no Waze code is hooked and
    no Waze offsets are used**, so Waze app updates do not affect it. The toggle is read live (no
    reboot needed to change it once the module is loaded).

### Changed
- Version updated to `versionName` `0.23#17.8-r19`, `versionCode` `3034`.
- Hardened the app-list filter so it only rewrites a multi-resolver query result and never returns an
  empty list, so no gearhead caller that dereferences the first result can crash.

### Verification
- Confirmed on the owner's device and in-vehicle: with the toggle on, Waze's map stays on the phone
  while Android Auto is connected and Waze does not appear or run on the car screen; with it off,
  Waze projects to Android Auto normally. Module reports **Activated** after the `xposed_init` fix.

## 0.23.14 (2026-10-22)

### Changed
- Version updated to `versionName` `0.23#17.8-r15`, `versionCode` `3030`.
- **Rebranded from AADisplay to AAPwned**, and renamed the application package from
  `io.github.nitsuya.aa.display` to `io.github.stashboy.aa.pwned`. The module label, Xposed
  description, launcher name, and in-app text now read "AAPwned"; the release asset is now
  `aapwned-<version>.apk`.
- No functional changes — behavior is identical to v0.23.13.

### Note
- Because the application package changed, this installs as a **new** module. Uninstall AADisplay,
  install AAPwned, re-enable it in LSPosed, re-grant its scope, and reboot; prior settings do not
  carry over.

### Credits
- AAPwned remains a fork of [Nitsuya/AADisplay](https://github.com/Nitsuya/AADisplay); credit for the
  original module is in the README (deliberately not in the installed package listing). Bundled
  template/helper code from duzhaokun123 and QAuxiliary (cinit) is retained under their licenses.

## 0.23.13 (2026-10-22)

### Changed
- Version updated to `versionName` `0.23#17.8-r14`, `versionCode` `3029`.
- Removed the Waze integration entirely: the "Disable Waze on AA" toggle, its persisted config
  option, UI row and strings, the Waze content-provider hook, the component/global-setting manager,
  and the Waze entry in the LSPosed scope. Google Maps support was already removed in v0.23.11, so
  the module no longer ties into any navigation app. Waze behaves as a normal, untied app.
- Floating controller: the close ("X") button is now a **session kill switch**. It runs the standard
  teardown (the same countdown path used when the car disconnects). Pressing "X" again during the
  countdown **cancels** the teardown and keeps the session; tapping the countdown kills immediately.
- Tucking the controller is now a **gesture**: drag it off the screen edge to tuck it to the peek,
  with a smooth slide animation. The controller also now stays **wherever you drop it** (free
  placement; it no longer snaps to the nearest side).
- When the mirror session is destroyed, Android Auto now **returns to its native launcher
  automatically** (via `CarActivityHost.finish()`) instead of freezing on the dead mirror surface.
  It returns home immediately on a real kill; a session restart (old display replaced by a new one)
  is detected and left running.

### Verification
- Confirmed on a connected Desktop Head Unit and the owner's device: kill returns to the Android Auto
  launcher near-instantly, cancel works, the tuck slides smoothly, the controller free-drags and
  stays put, Waze is restored to normal on-device, and session restart still works.

## 0.23.12 (2026-10-18)

### Changed
- Version updated to `versionName` `0.23#17.8-r8`, `versionCode` `3023`.
- Replaced the Android Auto Coolwalk facet-bar microphone/assistant slot with the AADisplay Home
  button, keeping the rest of Android Auto's native vertical rail intact (recents, AA's own Home
  button, and the recent-apps dock). Implemented as a single opaque overlay on the assistant slot
  (no bar rebuild, no per-frame listeners), so there is no projection-encoder frame backlog.

### Verification
- Confirmed on a connected Desktop Head Unit: only the Home icon shows in the slot, the native bar
  is intact, and no frames are dropped.

## 0.23.11 (2026-10-18)

### Changed
- Version updated to `versionName` `0.23#17.8-r3`, `versionCode` `3018`.
- Android Auto compatibility baseline updated to `17.8.663884-release`.
- Restored the AADisplay auto-open launcher for Android Auto 17.8, which removed
  `com.google.android.projection.gearhead.service.CarSystemUiControllerService`. Auto-open now
  launches via the first-party car SDK (`CarFirstPartyManager.startCarActivity`) instead of the
  obfuscated gearhead-internal class, removing the version-fragile dependency. Fully guarded.
- `AutoOpen` now defaults to off (opt-in); it no longer auto-opens on unreadable preferences.
- Added adaptive virtual-display DPI: when `VirtualDisplayDpi` is auto (0), density is set to the
  highest value keeping smallestWidthDp >= 600 (`shortSidePx * 160 / 600`), so mirrored apps use the
  large-screen layout and do not force-lock fullscreen (fixes YouTube opening/locking in fullscreen).
  Auto-tunes per head unit (720x480->128, 1920x720->192, 1920x1080->288); a manual value overrides.
- Removed `VIRTUAL_DISPLAY_FLAG_PRESENTATION` from the mirror virtual display.
- Rolls up the previously-unpublished 17.3 -> 17.6 candidates (v0.23.7 - v0.23.10), including removal
  of the Google Maps-on-Android-Auto toggle. Independent Waze control unchanged.

### Verification
- Clean release build `0.23#17.8-r3` (`versionCode` `3018`) installed in place; installed APK matched the built artifact.
- YouTube fullscreen-lock confirmed fixed on a connected Desktop Head Unit after the DPI change (2nd display density 128, smallestWidthDp 600, video exitable); the adaptive path reproduces this automatically.
- Android Auto 17.8 offset check: 8/9 hook fingerprint groups resolve unchanged; the only drift (auto-open launcher class) is addressed above.
- Full live in-vehicle head-unit validation still recommended.

## 0.23.10 (2026-09-20)

### Changed
- Compatibility candidate version updated to:
  - `versionName`: `0.23#17.6-r1`
  - `versionCode`: `3015`
- Android Auto compatibility baseline updated from `17.5.663214-release` to `17.6.663454-release`.
- No hook name, constructor shape, argument index, or resource-name change was made. Runtime evidence and direct APK comparison proved that the existing semantic and resource-name resolvers already follow the Android Auto 17.6 obfuscation changes.

### Verification
- Confirmed the Play-installed Android Auto `17.6.663454-release` package and all four base/split hashes before and after reboot.
- Confirmed the existing AADisplay build completed resolver loading in the active Android Auto projection, shared, and car processes without an AADisplay-tagged resolver failure or AndroidRuntime fatal exception.
- Compared the Android Auto 17.5 and 17.6 DEX/resource contracts used by AADisplay and verified that constructor semantics, argument positions, the fixed launcher method, button-action contracts, required resource names, and target layout structures remain compatible.
- Clean-built and in-place installed the release candidate, rebooted, verified that the installed APK matched the built artifact, and confirmed the app/module version handshake reports `0.23#17.6-r1`.
- Repeated the Android Auto projection, shared, and car resolver capture from the installed build with no AADisplay-tagged failure or AndroidRuntime fatal exception.
- Live head-unit validation remains required before promoting this candidate as production-verified.

## 0.23.9 (2026-09-07)

### Changed
- Compatibility candidate version updated to:
  - `versionName`: `0.23#17.5-r1`
  - `versionCode`: `3014`
- Android Auto compatibility baseline updated from `17.4.663054-release` to `17.5.663214-release`.
- Removed the Google Maps-on-Android-Auto toggle, its persisted config option, UI strings/layout, component manager, and all active Google Maps package/component/global-setting manipulation. The independent Waze control remains unchanged.
- No hook name, constructor shape, argument index, or resource-name change was made. Post-reboot runtime evidence and direct APK comparison proved that the existing semantic and resource-name resolvers already follow the Android Auto 17.5 obfuscation changes.

### Verification
- Confirmed the Play-installed Android Auto `17.5.663214-release` package and its base/split hashes before and after reboot.
- Confirmed the AADisplay hook resolver chain completed in the active Android Auto projection, shared, car, and provider processes without an AADisplay-tagged resolver failure or fatal exception.
- Compared the Android Auto 17.4 and 17.5 DEX/resource contracts used by AADisplay and verified that constructor semantics, argument positions, the fixed launcher method, button-action contracts, and required resource names remain compatible.
- Reset all five legacy Google Maps component overrides to their manifest-default state and removed the legacy AADisplay global setting from the test device.
- Clean-built and in-place installed the release candidate, rebooted, verified that the installed APK matched the built artifact, and confirmed the app/module version handshake reports `0.23#17.5-r1`.
- Repeated the Android Auto projection, shared, and car resolver capture from the installed build with no AADisplay-tagged failure or AndroidRuntime fatal exception.
- Confirmed after reboot that the Google Maps setting is absent from the live AADisplay UI and that the legacy global setting and component overrides remain removed.
- Live head-unit validation remains required before promoting this candidate as production-verified.

## 0.23.8 (2026-08-29)

### Changed
- Compatibility candidate version updated to:
  - `versionName`: `0.23#17.4-r1`
  - `versionCode`: `3013`
- Android Auto compatibility baseline updated from `17.3.662854-release` to `17.4.663054-release`.
- No hook name, constructor shape, argument index, or resource-name change was made. Post-reboot runtime evidence and direct APK comparison proved that the existing semantic and resource-name resolvers already follow the Android Auto 17.4 obfuscation and numeric resource-ID changes.

### Verification
- Confirmed the Play-installed Android Auto `17.4.663054-release` package after reboot.
- Confirmed the AADisplay hook resolver chain completed in the active Android Auto projection, shared, and car processes without an AADisplay-tagged resolver failure or fatal exception.
- Compared the Android Auto 17.3 and 17.4 DEX/resource contracts used by AADisplay and verified that constructor semantics, argument positions, the fixed launcher method, button-action contracts, and required resource names remain compatible.
- Clean-built and in-place installed the release candidate, rebooted, verified that the installed APK matched the built artifact, and confirmed the app/module version handshake reports `0.23#17.4-r1`.
- Repeated the Android Auto projection, shared, and car resolver capture from the installed build with no AADisplay-tagged failure or AndroidRuntime fatal exception.
- Live head-unit validation remains required before promoting this candidate as production-verified.

## 0.23.7 (2026-08-14)

### Changed
- Compatibility candidate version updated to:
  - `versionName`: `0.23#17.3-r1`
  - `versionCode`: `3012`
- Android Auto compatibility baseline updated from `17.2.662634-release` to `17.3.662854-release`.
- No hook name, constructor shape, argument index, or resource-name change was made. Runtime resolver evidence and direct APK comparison proved that the existing semantic resolvers already follow the Android Auto 17.3 obfuscation changes.

### Verification
- Updated Android Auto through Google Play and confirmed the installed `17.3.662854-release` package after reboot.
- Confirmed the AADisplay hook resolver chain completed in the active Android Auto projection, shared, and car processes without an AADisplay-tagged resolver failure or fatal exception.
- Compared the Android Auto 17.2 and 17.3 DEX/resource contracts used by AADisplay and verified that constructor semantics, argument positions, the fixed launcher method, and required resource names remain compatible.
- Clean-built and in-place installed the release candidate, rebooted, verified that the installed APK matched the built artifact, and confirmed the app/module version handshake reports `0.23#17.3-r1`.
- Repeated the Android Auto projection, shared, and car resolver capture from the corrected installed build with no AADisplay-tagged failure or AndroidRuntime fatal exception.
- Live head-unit validation remains required before promoting this candidate as production-verified.

## 0.23.6 (2026-07-26)

### Changed
- Production version updated to:
  - `versionName`: `0.23#17.2-r5`
  - `versionCode`: `3011`
- AADisplay now creates its virtual display with the active Android Auto `TextureView` surface immediately instead of creating a surface-less display and attaching later.
- Default launcher startup now resolves from installed home launchers at runtime when no explicit launch package is configured, avoiding stale hard-coded package defaults.

### Fixed
- Fixed blank AADisplay rendering on Android Auto `17.2` by preserving the `TextureView` surface, attaching it during virtual-display creation, and reattaching it on reconnect.
- Prevented duplicate virtual display creation during rapid Android Auto lifecycle callbacks.
- Made hook preference access resilient under LSPosed by committing preferences synchronously and verifying hook-readable preference files.
- Android Auto hooks now continue with safe defaults if preference access is temporarily unavailable instead of aborting all hook setup.

### Verification
- Built successfully:
  - `:aa-display:assembleRelease`
- Installed and reboot-tested on the connected device.
- Live Android Auto validation confirmed:
  - AADisplay side control panel works
  - on-device Android Auto controls work
  - AADisplay opens and renders launcher content
  - backend virtual display and AA-hosted display both showed the same rendered content

## 0.23.4 (2026-05-23)

### Added
- Separate production toggle for Google Maps-on-phone while Android Auto remains connected:
  - new persisted setting `DisableGoogleMapsOnAa`
  - component-level enable/disable manager for Maps projection services + ghost activity

### Changed
- Production version updated to:
  - `versionName`: `0.23#16.8-r1`
  - `versionCode`: `3006`
- Waze component toggle execution now uses the same success gating strategy as Google Maps (all component operations must succeed before returning success).

### Fixed
- Android Auto UI/resource hook stability:
  - `AaUiHook` now skips optional hooks when required resources are missing instead of asserting/crashing.
  - projection decoration constructor hook now uses compatible constructor matching rather than hard-coded constructor lookup.
- Gearhead phenotype property hook robustness:
  - `AaPropsHook` now resolves string fields dynamically to reduce obfuscation fragility.

### Cleanup
- Removed dead/unused code and assets not used by current production flow:
  - removed `AaControlService`, `AaCarService`, `SettingsActivity`, legacy `LauncherHook`, deprecated pref XML, and orphaned resources.
- Removed obsolete global build-feature flag from `gradle.properties` (now explicitly configured in module build config).

### Evidence / Validation
- Built successfully:
  - `:aa-display:assembleRelease`
  - `:aa-display:lintDebug`
- Verified on connected device app targets:
  - Android Auto `16.8.661854-release`
  - Google Maps `26.20.01.913318892`
  - Waze `5.18.5.6`
- Waze and Maps toggles validated as separate independent controls in app flow.

## 0.23.3 (2026-05-05)

### Added
- Smart-sidebar style controller flow:
  - floating controller now starts minimized by default
  - tap edge handle to open, tap close to hide, drag and snap left/right edge

### Changed
- Keyboard routing on virtual display now enforces local IME policy (`DisplayImePolicy=0`) for on-screen input on AADisplay side instead of fallback-to-phone behavior.

### Fixed
- Removed the non-functional disconnect-type (`A`) button from the floating controller.
- Disconnect countdown now replaces the monitor button slot directly during disconnect flow for a cleaner 2-control vertical stack.

### Verification
- `:aa-display:assembleDebug` passed after controller + IME policy updates.
- Installed successfully to test device for live verification.

## 0.23.2 (2026-05-05)

### Changed
- Release build signing now falls back to debug signing when `KEY_ANDROID` is not present, so maintainers can still produce a release artifact locally without exposing private signing credentials.

### Fixed
- Prevented DHU disconnect teardown from forcing the phone back to launcher/home when AADisplay session closes on the virtual-display home screen.
- Teardown package-stop logic now excludes:
  - AADisplay itself
  - configured launch/home package
  - current foreground package on the primary phone display

### Verification
- Reproduced issue with config where `LauncherPackage` and `HomePackage` were both launcher-based, then validated corrected behavior after patch.
- `:aa-display:assembleDebug` succeeds with this change.
- Installed and prepared for live device retest flow.

## 0.23.1 (2026-05-04)

### Added
- In-app GitHub menu now points to the production fork: `https://github.com/Stashboy/AADisplay`.

### Changed
- Internal AADisplay behavior now syncs `HomePackage` to `LauncherPackage` so task-view home behavior is consistent with selected launch package.
- Sidebar Back key handling now uses Android virtual-display key event semantics (`FLAG_FROM_SYSTEM | FLAG_VIRTUAL_HARD_KEY`) for more reliable back navigation.

### Fixed
- Sidebar Back now works reliably in task-view sessions.
- Backing out to AADisplay home now clears pinned PiP state using the same cleanup strategy as Home (with front-home checks).

### Cleanup / Optimization (Evidence-Based)
- Fixed a real API-compatibility risk in `AADisplayConfig`: replaced Java Stream `.toList()` usage (API 34+) with Kotlin collection operators (safe for min SDK 31).
- Added missing `super.onDestroy()` in `AaControlService` to resolve lifecycle correctness lint error.
- Removed unused settings warning view/resources and unused string bloat in main settings screen.
- Internationalization cleanup for top-right menu title (`GitHub` moved to string resource).

### Verification
- `:aa-display:assembleDebug` succeeds after all changes.
- `:aa-display:lintDebug` re-run confirms resolved issues for:
  - `MissingSuperCall` (AaControlService)
  - `NewApi` (AADisplayConfig stream/toList path)
  - `HardcodedText` (`menu_main.xml` GitHub title)
- Runtime verification completed against Android Auto `16.6` behavior in both DHU and real head unit sessions.

## 0.23.0 (2026-05-04)

### Added
- Dynamic TaskView sizing lock to stabilize UI rendering across different head unit dimensions.
- Better teardown behavior for TaskView sessions to reduce lingering app states on disconnect/exit.
- Android Auto 16.6 hook compatibility updates.

### Changed
- Simplified settings UI to a single screen with only production-relevant options:
  - Auto Open
  - Default Launch Package
  - Delay Destroy Time
- Default GitHub menu flow retained from the main screen (settings gear flow removed).
- Production version string updated to `0.23#16.6-r1`.

### Fixed
- Kernel panic scenario during specific TaskView lifecycle transitions.
- Home/exit transition handling that previously left abnormal PiP-like states.
- Inconsistent YouTube rendering outcomes caused by unstable virtual display dimensions.

### Security / Privacy
- Removed repository-tracked signing key material from source control.
- Added keystore patterns to `.gitignore`.
- Removed machine-specific Gradle JDK path from tracked config.

### Compatibility
- Validated behavior against Android Auto 16.6 and device-side testing with real head unit workflow.
