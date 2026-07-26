# Release Notes v0.23.6

Date: 2026-07-26
Version Name: `0.23#17.2-r5`
Version Code: `3011`

## Summary

This production release hardens AADisplay for Android Auto `17.2` and fixes the blank display path seen after the latest Android Auto update.

## What Changed

- Creates the AADisplay virtual display with the live Android Auto `TextureView` surface attached from the start.
- Reattaches the display surface on reconnect/resume so Android Auto lifecycle churn does not leave the virtual display detached.
- Prevents duplicate virtual displays when Android Auto sends overlapping create-display callbacks.
- Resolves the default launcher dynamically from installed home launchers when no explicit launch package is configured.
- Makes hook preferences readable and verifiable for LSPosed/system-server access.
- Allows Android Auto hooks to continue with safe defaults if preferences are temporarily unreadable.

## Verification

- Build passed:
  - `:aa-display:assembleRelease`
- Installed to the connected test device.
- Reboot validation completed.
- Live Android Auto testing confirmed:
  - AADisplay side controls work
  - on-device Android Auto controls work
  - AADisplay opens successfully
  - rendered launcher content appears in the AA-hosted AADisplay surface

## Notes

- No Android Auto hook-name offset change was required for this release; the issue was proven to be in display/surface lifecycle handling.
- Local debug screenshots and device-specific paths were not included in the release commit.
