# AAPwned

[![Fork](https://img.shields.io/badge/Fork-Nitsuya%2FAADisplay-blue?logo=github)](https://github.com/Nitsuya/AADisplay)
![Xposed Module](https://img.shields.io/badge/Xposed-Module-blue)
![Android SDK min 31](https://img.shields.io/badge/Android%20SDK-%3E%3D%2031-brightgreen?logo=android)
![Android SDK target 36](https://img.shields.io/badge/Android%20SDK-target%2036-brightgreen?logo=android)

**AAPwned** (formerly **AADisplay**) is an LSPosed/Xposed module that mirrors Android Auto onto the phone's own screen via a virtual display, letting you run phone apps on the car head unit. Package: `io.github.stashboy.aa.pwned`.

> **Renamed:** This project was renamed from **AADisplay** to **AAPwned** (and the package from `io.github.nitsuya.aa.display` to `io.github.stashboy.aa.pwned`) as of v0.23.14. If you used AADisplay, see **[Migrating from AADisplay](#migrating-from-aadisplay)** below — because the package changed, AAPwned installs as a separate module and does not upgrade AADisplay in place.

## Migrating from AADisplay

AAPwned is the continuation of AADisplay under a new name and package, so it installs **alongside** the old AADisplay rather than replacing it. To switch over:

1. Install **AAPwned** (the `aapwned-*.apk` from [Releases](https://github.com/Stashboy/AAPwned/releases)).
2. In **LSPosed → Modules → AAPwned**: enable it and grant its scope (**System Framework** + **Android Auto**). *(This step must be done manually — LSPosed does not expose a way to automate module enablement or scope.)*
3. **Uninstall** the old **AADisplay** (and disable it in LSPosed).
4. **Reboot.**
5. Open **AAPwned** and reconfigure your settings (Default Launch Package, Delay Destroy Time, etc.) — settings do not carry over from the old package.

AAPwned should then show **Activated**. Behavior is identical to the last AADisplay release.

## Credits

AAPwned is a fork of **[Nitsuya/AADisplay](https://github.com/Nitsuya/AADisplay)** — all credit for the original module and its core design goes to Nitsuya. This fork maintains Android Auto compatibility and adds features on top of that work. It also bundles template/helper code from [duzhaokun123](https://github.com/duzhaokun123) and [QAuxiliary (cinit)](https://github.com/cinit/QAuxiliary), under their respective licenses.

## Version

- Current module version: `0.23#17.8-r19` (Android Auto target 17.8 / 17.9)
- See [CHANGELOG.md](CHANGELOG.md) and the latest release notes (e.g. `RELEASE_NOTES_v0.23.15.md`).

## Requirements

- Android 12+ (SDK 31+)
- Rooted device with LSPosed (or compatible Xposed framework)
- Android Auto (`com.google.android.projection.gearhead`)

## Quick Start

1. Build and install the APK.
2. Enable the module in LSPosed for:
   - Android Auto
   - System Framework
3. Reboot device.
4. Open AAPwned and configure:
    - Auto Open
    - Default Launch Package
    - Delay Destroy Time
    - Disable Waze on Android Auto — keeps Waze on the phone (map on-device) while connected,
      instead of projecting to the car. Entirely Android Auto-side; no Waze changes required.

## Build

```bash
./gradlew :aa-display:assembleDebug
./gradlew :aa-display:assembleRelease
```

## License

Inherited from upstream. See [LICENSE](LICENSE).
