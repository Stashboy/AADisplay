# AAPwned v0.23.14

Rebrand: **AADisplay → AAPwned**.

## What changed
- The module is now named **AAPwned**, and the application package is
  `io.github.stashboy.aa.pwned` (was `io.github.nitsuya.aa.display`).
- The module label, Xposed description, launcher name, and in-app text read **AAPwned**. The release
  asset is now `aapwned-<version>.apk`.
- **No functional changes** — behavior is identical to v0.23.13 (kill switch, return-to-AA-home,
  controller free-move + slide-to-tuck, Waze/Maps removed).

## Important: this is a fresh install
Because the application package changed, AAPwned is a **brand-new module** — it does not upgrade
AADisplay in place. To switch over:
1. Uninstall the old **AADisplay**.
2. Install **AAPwned** (`aapwned-0.23-17.8-r15.apk`).
3. Enable AAPwned in LSPosed and grant its scope (Android Auto + System Framework).
4. Reboot.
5. Reconfigure settings (Default Launch Package, Delay Destroy Time, etc.) — prior settings do not
   carry over.

## Credits
AAPwned is a fork of **[Nitsuya/AADisplay](https://github.com/Nitsuya/AADisplay)** — all credit for
the original module goes to Nitsuya. Credit is kept here in the repo and README, never in the
installed package listing. Bundled template/helper code from duzhaokun123 and QAuxiliary (cinit) is
retained under their respective licenses.

versionName `0.23#17.8-r15` · versionCode `3030`
