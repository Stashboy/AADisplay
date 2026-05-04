# Release Notes v0.23.0

Date: 2026-05-04
Version Name: `0.23#16.6-r1`
Version Code: `3001`

## Summary

This release promotes the module from beta to production-ready for Android Auto 16.6 usage, with a strong focus on stability, TaskView lifecycle correctness, and rendering consistency.

## Highlights

- Android Auto 16.6 hook/offset compatibility integrated and validated.
- Kernel panic path fixed using evidence-driven lifecycle adjustments.
- Dynamic TaskView dimension lock behavior improved for mixed display targets.
- Main app UI consolidated to one settings screen for easier production usage.

## Migration Notes

- Previous beta branding/version strings have been removed.
- Existing user settings remain compatible.
- Reboot is still required after install/update for reliable hook activation.

## Known Constraints

- Desktop Head Unit (DHU) may still differ from real head units in rendering smoothness due to emulator limitations.
- Resolution compatibility in DHU depends on supported profiles; unsupported sizes may fall back automatically.

## Verification Checklist

- Module activates in LSPosed.
- AADisplay starts and exits without kernel panic.
- YouTube enters/exits fullscreen correctly.
- TaskView teardown no longer leaves abnormal app residue in recents.
- Real head unit run confirms stable behavior.
