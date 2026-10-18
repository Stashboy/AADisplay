# Release Notes v0.23.11

Date: 2026-10-18
Version Name: `0.23#17.8-r3`
Version Code: `3018`

## Summary

Updates AADisplay for **Android Auto `17.8.663884-release`**, restores the AADisplay
auto-open launcher that Android Auto 17.8 broke, makes auto-open opt-in, and adds an
adaptive virtual-display DPI so mirrored apps (notably YouTube) no longer get trapped
in fullscreen.

This release also rolls up the Android Auto 17.3 -> 17.6 compatibility candidates
(internally tracked as v0.23.7 - v0.23.10) that were never published, including the
removal of the Google Maps-on-Android-Auto toggle. The independent Waze control is
unchanged.

## Changes

### Android Auto 17.8 auto-open fix
- Android Auto 17.8 removed `com.google.android.projection.gearhead.service.CarSystemUiControllerService`,
  which the facet-bar auto-open used to launch AADisplay onto the car screen.
- Auto-open now launches through the first-party car SDK path
  (`CarFirstPartyManager.startCarActivity`, the same mechanism the in-app control panel
  already uses) instead of the obfuscated gearhead-internal class. This removes the
  version-fragile dependency, so auto-open should survive future Android Auto renames.
- The call is fully guarded: any failure is a silent no-op and never disturbs the
  live display.

### Auto-open is now opt-in
- `AutoOpen` now defaults to **off**. AADisplay only auto-opens when the switch is
  explicitly enabled, and never when preferences are temporarily unreadable.

### Adaptive virtual-display DPI
- When `VirtualDisplayDpi` is left on auto (0), the virtual display density is now
  computed as the highest value that still keeps `smallestWidthDp >= 600`
  (`dpi = shortSidePx * 160 / 600`).
- This puts mirrored apps in the large-screen layout bucket, so apps such as YouTube
  use their tablet/two-pane UI and do **not** force-lock landscape video fullscreen
  (previously a video opened fullscreen on the mirror with no working exit).
- It auto-tunes per head unit and prefers the sharper result on higher-resolution
  screens, e.g. 720x480 -> 128 dpi, 1920x720 -> 192 dpi, 1920x1080 -> 288 dpi. A
  manual `VirtualDisplayDpi` value still overrides.
- The mirror's `VIRTUAL_DISPLAY_FLAG_PRESENTATION` was also removed so the display is
  not treated as a cast/secondary presentation screen.

### Rolled-up 17.3 -> 17.6 candidates (previously unpublished)
- Compatibility baselines tracked from Android Auto 17.3 through 17.6 with no hook
  name/constructor/argument/resource changes required (semantic + resource-name
  resolvers already followed the obfuscation changes).
- Removed the Google Maps-on-Android-Auto toggle, its config option, UI strings/layout,
  and component manager.

## Verification

- Clean release build produced `0.23#17.8-r3` (`versionCode` `3018`), installed in
  place; the installed APK matched the built artifact.
- Auto-open: resolves and installs with no hook failure; the launcher no longer
  depends on the removed `CarSystemUiControllerService`.
- YouTube fullscreen-lock: reproduced and then **confirmed fixed on a connected head
  unit (Desktop Head Unit)** after the DPI change - the mirrored 2nd display reported
  density 128 (720x480 -> smallestWidthDp 600) and YouTube became exitable. The
  adaptive path reproduces this automatically.
- Android Auto `17.8.663884-release` offset check: 8 of 9 hook fingerprint groups
  still resolve unchanged on 17.8; the only drift (the auto-open launcher class) is
  addressed above.

## Pending validation

- Full live head-unit validation of rendering, controls, auto-open, layout
  replacement, DPI behavior, and steering-wheel events in the vehicle.

No machine-specific identifiers, filesystem paths, screenshots, or private APK
artifacts are included in this release.
