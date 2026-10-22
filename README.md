# AAPwned

[![Fork](https://img.shields.io/badge/Fork-Nitsuya%2FAADisplay-blue?logo=github)](https://github.com/Nitsuya/AADisplay)
![Xposed Module](https://img.shields.io/badge/Xposed-Module-blue)
![Android SDK min 31](https://img.shields.io/badge/Android%20SDK-%3E%3D%2031-brightgreen?logo=android)
![Android SDK target 36](https://img.shields.io/badge/Android%20SDK-target%2036-brightgreen?logo=android)

**AAPwned** is an LSPosed/Xposed module that mirrors Android Auto onto the phone's own screen via a virtual display, letting you run phone apps on the car head unit. Package: `io.github.stashboy.aa.pwned`.

## Credits

AAPwned is a fork of **[Nitsuya/AADisplay](https://github.com/Nitsuya/AADisplay)** — all credit for the original module and its core design goes to Nitsuya. This fork maintains Android Auto compatibility and adds features on top of that work. It also bundles template/helper code from [duzhaokun123](https://github.com/duzhaokun123) and [QAuxiliary (cinit)](https://github.com/cinit/QAuxiliary), under their respective licenses.

## Version

- Current module version: `0.23#17.8-r15` (Android Auto target 17.8 / 17.9)
- See [CHANGELOG.md](CHANGELOG.md) and the latest release notes (e.g. `RELEASE_NOTES_v0.23.14.md`).

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

## Build

```bash
./gradlew :aa-display:assembleDebug
./gradlew :aa-display:assembleRelease
```

## License

Inherited from upstream. See [LICENSE](LICENSE).
