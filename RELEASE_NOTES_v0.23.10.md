# Release Notes v0.23.10

Date: 2026-09-20
Version Name: `0.23#17.6-r1`
Version Code: `3015`

## Summary

This candidate updates AADisplay's tested Android Auto baseline to `17.6.663454-release` while retaining the Google Maps functionality removal introduced in the previous candidate.

Android Auto 17.6 changed several obfuscated class and method names. Evidence showed that AADisplay's existing DexKit and resource-name resolvers already track those changes. No hook name, constructor shape, argument index, or resource-name patch was required.

## Verification So Far

- The Play-installed Android Auto 17.6 package and all four base/split hashes were confirmed before analysis and again after reboot.
- A controlled cold start completed AADisplay resolver loading in the Android Auto projection, shared, and car processes without an AADisplay-tagged resolver failure or AndroidRuntime fatal exception.
- Runtime resolution selected the expected 10-argument UI constructor and resolved the 17.6 Props target with its two non-static String fields.
- Direct comparison of the Android Auto 17.5 and 17.6 APK contracts confirmed:
  - display DPI remains constructor argument 8
  - car display type and DPI remain arguments 1 and 2
  - layout resource, layout type, right-hand-drive, and vertical-rail values remain arguments 0, 3, 4, and 5
  - projection corner radius remains argument 5
  - the signature verifier and Android Auto launcher method signatures remain unchanged
  - button broadcast and receiver contracts remain compatible
  - all six required UI resource names and numeric IDs remain present
  - the facet and canonical LHD/RHD layouts are byte-identical to Android Auto 17.5
- Active source outside historical release documentation still contains no Google Maps package, component, config, UI, manager, or command reference.
- A clean release build produced version `0.23#17.6-r1` (`versionCode` `3015`); its optimized DEX contains the new version, no stale 17.5 version, and no removed Google Maps implementation symbols.
- The candidate was installed in place and rebooted; the installed APK matched the built artifact and the app/module version handshake reported `0.23#17.6-r1`.
- A fresh capture from the installed build completed the Android Auto projection, shared, and car resolver paths with no AADisplay-tagged failure, AndroidRuntime fatal exception, or crash-buffer entry.

## Pending Validation

- Live head-unit validation of AADisplay rendering, controls, auto-open, layout replacement, DPI behavior, and steering-wheel events.

No machine-specific identifiers, filesystem paths, screenshots, or private APK artifacts are included in this candidate.
