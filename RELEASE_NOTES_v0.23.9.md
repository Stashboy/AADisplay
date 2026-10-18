# Release Notes v0.23.9

Date: 2026-09-07
Version Name: `0.23#17.5-r1`
Version Code: `3014`

## Summary

This candidate updates AADisplay's tested Android Auto baseline to `17.5.663214-release` and completely removes AADisplay's Google Maps-on-Android-Auto configuration and component-management behavior.

Android Auto 17.5 changed several obfuscated class and method names. Evidence showed that AADisplay's existing DexKit and resource-name resolvers already track those changes. No hook name, constructor shape, argument index, or resource-name patch was required.

## Google Maps Functionality Removed

- Removed the Google Maps settings toggle and explanatory UI.
- Removed the persisted Google Maps config option and settings save-state handling.
- Removed the Google Maps component manager and all package, component, force-stop, and global-setting commands associated with it.
- Preserved the separate Waze setting and behavior unchanged.
- Reset the five component overrides created by the prior implementation to their manifest-default states on the test device and deleted its legacy global setting.

## Verification So Far

- The Play-installed Android Auto 17.5 package and its base/split hashes were confirmed before analysis and again after reboot.
- Hook resolution completed in the active Android Auto projection, shared, car, and provider processes without an AADisplay-tagged resolver failure or fatal exception.
- Direct comparison of the Android Auto 17.4 and 17.5 APK contracts confirmed:
  - display DPI remains constructor argument 8
  - car display type and DPI remain arguments 1 and 2
  - layout resource, layout type, right-hand-drive, and vertical-rail values remain arguments 0, 3, 4, and 5
  - projection corner radius remains argument 5
  - the Android Auto launcher method signature remains unchanged
  - button broadcast action contracts remain present
  - all required UI resource names and numeric IDs remain present
- Active source outside historical release documentation contains no Google Maps package, component, config, UI, manager, or command reference.
- A clean release build produced version `0.23#17.5-r1` (`versionCode` `3014`) with no Google Maps manager, config symbol, or UI resources in the packaged APK.
- The candidate was installed in place and rebooted; the installed APK matched the built artifact and the app/module version handshake reported `0.23#17.5-r1`.
- A fresh capture from the installed build completed the Android Auto projection, shared, and car resolver paths without an AADisplay-tagged failure or AndroidRuntime fatal exception.
- The post-reboot AADisplay UI contains no Google Maps setting, and the legacy global setting and component overrides remain removed.

## Pending Validation

- Live head-unit validation of AADisplay rendering, controls, auto-open, layout replacement, DPI behavior, steering-wheel events, and Google Maps stability under normal Android Auto ownership.

No machine-specific identifiers, filesystem paths, screenshots, or private APK artifacts are included in this candidate.
