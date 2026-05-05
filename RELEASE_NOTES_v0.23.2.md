# Release Notes v0.23.2

Date: 2026-05-05
Version Name: `0.23#16.6-r3`
Version Code: `3003`

## Summary

This is the final production hardening pass for the current release cycle.  
Everything that was already stable stays stable, with one critical teardown fix added to protect normal phone usage when DHU disconnects.

This build remains aligned with Android Auto `16.6` (latest tested target in this cycle).

## What Changed

- Fixed a disconnect edge case where closing DHU could push the real phone UI to launcher/home unexpectedly.
- Added teardown safety guards so force-stop cleanup does not hit:
  - AADisplay package
  - configured AADisplay launcher/home package
  - currently foreground package on the phone's primary display
- Release build flow now supports secure fallback signing for maintainers:
  - if `KEY_ANDROID` is present: uses configured release key
  - if `KEY_ANDROID` is absent: uses debug signing so artifacts are still buildable without private key exposure

## Why This Is Safe

- Root cause was confirmed from live config + teardown path before patching.
- Fix scope is narrow: cleanup exclusion logic only, no hook-offset churn or TaskView feature rewrites.
- Existing validated behavior (no kernel panic, proper layout handling, PiP cleanup, back/home flow) remains intact.

## Validation

- Build passed: `:aa-display:assembleDebug`
- APK installed successfully for device verification.
