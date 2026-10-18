# Release Notes v0.23.7

Date: 2026-08-14
Version Name: `0.23#17.3-r1`
Version Code: `3012`

## Summary

This compatibility candidate updates AADisplay's tested Android Auto baseline to `17.3.662854-release`.

Android Auto 17.3 changed several obfuscated class and method names, but evidence showed that AADisplay's existing DexKit and resource-name resolvers already track those changes. No hook name, constructor shape, argument index, or resource-name patch was required.

## Verification So Far

- Android Auto was updated through Google Play and the new version was confirmed after reboot.
- Hook resolution completed in the active Android Auto projection, shared, and car processes without an AADisplay-tagged resolver failure or fatal exception.
- A clean release build completed successfully. Its manifest and DEX both report version `0.23#17.3-r1` / code `3012`, with no stale prior-version constants.
- The clean candidate was installed in place without clearing app data, the device was rebooted, and the installed APK hash matched the built artifact.
- After reboot, the app and injected module service reported the same new build timestamp and version, and the AADisplay settings activity cold-started and rendered without a fatal exception.
- A fresh Android Auto launch again completed the projection, shared, and car resolver chains without an AADisplay-tagged failure or AndroidRuntime fatal exception.
- Direct comparison of the Android Auto 17.2 and 17.3 APK contracts confirmed:
  - display DPI remains constructor argument 8
  - car display type and DPI remain arguments 1 and 2
  - layout resource, layout type, right-hand-drive, and vertical-rail values remain arguments 0, 3, 4, and 5
  - projection corner radius remains argument 5
  - the Android Auto launcher method signature remains unchanged
  - all required UI resource names remain present

## Pending Validation

- Live head-unit validation of AADisplay rendering, controls, auto-open, layout replacement, DPI behavior, and steering-wheel events.

No machine-specific identifiers, filesystem paths, screenshots, or private APK artifacts are included in this candidate.
