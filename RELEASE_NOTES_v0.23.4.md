# Release Notes v0.23.4

Date: 2026-05-23
Version Name: `0.23#16.8-r1`
Version Code: `3006`

## Summary

This production release updates AADisplay for the current Android Auto cycle and adds independent per-app navigation toggles for Waze and Google Maps phone-side execution while connected to Android Auto.

Validated against live device builds:
- Android Auto `16.8.661854-release`
- Google Maps `26.20.01.913318892`
- Waze `5.18.5.6`

## What Is New

- Added a dedicated Google Maps toggle in the main settings screen:
  - `Disable Google Maps on AA`
  - independent from the existing Waze toggle

## Reliability and Hardening

- Waze and Google Maps managers now share the same strict success behavior:
  - all component toggle operations must succeed before reporting success
- Hook safety improvements:
  - `AaUiHook` now conditionally applies layout/facet hooks only when required resources exist
  - projection corner-radius hook now uses compatible constructor matching
  - `AaPropsHook` string-field resolution was hardened for obfuscation variance

## Cleanup

- Removed stale/unused code paths and resources no longer used by production flow:
  - legacy settings activity + XML
  - retired launcher hook
  - unused service code and orphaned icon/layout resources

## Verification

- Build checks passed:
  - `:aa-display:assembleRelease`
  - `:aa-display:lintDebug`
- APK installed on test device and prepared for post-install reboot validation.
