# Release Notes v0.23.8

Date: 2026-08-29
Version Name: `0.23#17.4-r1`
Version Code: `3013`

## Summary

This compatibility candidate updates AADisplay's tested Android Auto baseline to `17.4.663054-release`.

Android Auto 17.4 changed several obfuscated class and method names and shifted several numeric resource IDs. Evidence showed that AADisplay's existing DexKit and resource-name resolvers already track those changes. No hook name, constructor shape, argument index, or resource-name patch was required.

## Verification So Far

- The Play-installed Android Auto 17.4 package and its base/split hashes were confirmed before analysis and again after reboot.
- Hook resolution completed in the active Android Auto projection, shared, and car processes without an AADisplay-tagged resolver failure or fatal exception.
- Direct comparison of the Android Auto 17.3 and 17.4 APK contracts confirmed:
  - display DPI remains constructor argument 8
  - car display type and DPI remain arguments 1 and 2
  - layout resource, layout type, right-hand-drive, and vertical-rail values remain arguments 0, 3, 4, and 5
  - projection corner radius remains argument 5
  - the Android Auto launcher method signature remains unchanged
  - button broadcast action contracts remain present
  - all required UI resource names remain present
- Numeric UI resource IDs shifted, but AADisplay resolves those resources by name.
- A clean release build completed successfully. Its manifest and optimized DEX both report version `0.23#17.4-r1` / code `3013`, with no stale prior-version constants.
- The candidate was installed in place without clearing app data, the device was rebooted, and the installed APK hash matched the built artifact.
- After reboot, the app and injected module service reported the same new build timestamp and version, and the AADisplay settings activity cold-started without a fatal exception.
- A fresh Android Auto launch again completed the projection, shared, and car resolver chains without an AADisplay-tagged failure or AndroidRuntime fatal exception.

## Pending Validation

- Live head-unit validation of AADisplay rendering, controls, auto-open, layout replacement, DPI behavior, and steering-wheel events.

No machine-specific identifiers, filesystem paths, screenshots, or private APK artifacts are included in this candidate.
