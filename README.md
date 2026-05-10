# AADisplay

[![Fork](https://img.shields.io/badge/Fork-Nitsuya%2FAADisplay-blue?logo=github)](https://github.com/Nitsuya/AADisplay)
![Xposed Module](https://img.shields.io/badge/Xposed-Module-blue)
![Android SDK min 31](https://img.shields.io/badge/Android%20SDK-%3E%3D%2031-brightgreen?logo=android)
![Android SDK target 36](https://img.shields.io/badge/Android%20SDK-target%2036-brightgreen?logo=android)

Production-ready fork of [Nitsuya/AADisplay](https://github.com/Nitsuya/AADisplay), focused on Android Auto 16.6 compatibility, TaskView stability, and reliable display sizing behavior.

## Version

- Current module version: `0.23#16.6-r5`
- See [CHANGELOG.md](CHANGELOG.md) and [RELEASE_NOTES_v0.23.5.md](RELEASE_NOTES_v0.23.5.md)

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
4. Open AADisplay and configure:
   - Auto Open
   - Disable Waze on AA
   - Default Launch Package
   - Delay Destroy Time

## Build

```bash
./gradlew :aa-display:assembleDebug
./gradlew :aa-display:assembleRelease
```

## License

Inherited from upstream. See [LICENSE](LICENSE).
