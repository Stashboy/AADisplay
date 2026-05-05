# Release Notes v0.23.3

Date: 2026-05-05
Version Name: `0.23#16.6-r4`
Version Code: `3004`

## Summary

This release is a production polish update focused on daily usability.  
The floating controller is now out of the way by default, disconnect controls are cleaner, and on-screen keyboard behavior is aligned for AADisplay-side input.

Validated against the current Android Auto `16.6` integration path.

## What's New

- Smart-sidebar style default behavior for the floating controller:
  - starts minimized on the edge
  - tap to open, tap close to hide
  - draggable + edge snap retained

## Improvements

- Virtual-display IME policy now enforces local keyboard rendering (local display policy), reducing fallback-to-phone keyboard behavior.
- Controller cleanup:
  - removed the non-functional `A` button
  - disconnect countdown now replaces the monitor slot directly for a cleaner layout

## Verification

- Build passed: `:aa-display:assembleDebug`
- Installed successfully for device-side test flow

