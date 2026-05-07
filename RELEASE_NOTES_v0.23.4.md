# Release Notes v0.23.4

Date: 2026-05-06  
Version Name: `0.23#16.6-r5`  
Version Code: `3005`

## Summary

This is a production-stability and responsiveness update for the Android Auto `16.6` branch.
The session lifecycle is now stricter, teardown is immediate, and wireless interaction overhead was reduced without lowering video quality.

## What Changed

- Head-unit-first session model:
  - AA home/launcher rail action now explicitly tears down the active AADisplay session before returning to launcher UI.
  - Phone-side monitor/taskview session entry behavior was removed from active flow.
- Settings simplification:
  - Removed `Delay Destroy Time` from the app UI.
  - Backend default is now immediate destroy (`DelayDestroyTime=0`).
- Disconnect safety:
  - AA disconnect now explicitly triggers backend destroy (`onDisconnected`).

## Performance Optimizations (No Quality Reduction)

- Removed per-call dynamic binder proxy logging in `CoreManager` to reduce IPC overhead during frequent interactions.
- Added single-touch fast path for MotionEvent allocation in AA touch forwarding to reduce object churn and GC pressure.

## Reliability Fixes

- Reverted an unsafe touch dispatch threading optimization that caused input regression on-device.
- Kept the proven dispatch path for touch/key injection while retaining safe performance changes.

## Verification

- Build passed: `:aa-display:assembleDebug`
- Build passed: `:aa-display:assembleRelease`
- Lint passed: `:aa-display:lintDebug`
