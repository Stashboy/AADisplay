# Release Notes v0.23.1

Date: 2026-05-04
Version Name: `0.23#16.6-r2`
Version Code: `3002`

## Summary

This release is the production polish pass. It keeps the strong stability from the last round, tightens Back/Home behavior, and finalizes launcher/settings internals for predictable real-world use.

Tested and aligned for Android Auto `16.6` (latest tested build in this cycle).

## What's New

- GitHub menu now opens the production fork directly:
  - [Stashboy/AADisplay](https://github.com/Stashboy/AADisplay)

## Reliability Improvements

- Back key injection path was rebuilt to match Android's virtual-display event model.
- PiP cleanup now runs on Back-to-Home transitions the same way it does on Home button flows.
- Internal home/launch routing is now synced so the launcher behavior is consistent and predictable.

## Evidence-Based Cleanup

- Fixed API-level compatibility risk (`Stream.toList()` on min SDK 31 path).
- Fixed service lifecycle correctness (`super.onDestroy()` now present).
- Removed unused settings-screen UI/resource bloat and hardcoded menu text.

## Validation

- Build: `:aa-display:assembleDebug` passed.
- Lint: `:aa-display:lintDebug` passed with previously flagged items resolved for:
  - Missing super call
  - API 34-only collection call
  - Hardcoded GitHub menu title

## Notes

- Existing user settings remain compatible.
- LSPosed/module scope and reboot requirements are unchanged.
