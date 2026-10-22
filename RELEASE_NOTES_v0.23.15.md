# AAPwned v0.23.15

Fixes the v0.23.14 activation regression and re-introduces the "Disable Waze on Android Auto" toggle
with a new, update-proof mechanism.

## Activation fix (important)

v0.23.14 shipped **broken** — it installed as **Not Activated** and hooked nothing. The rename to
`io.github.stashboy.aa.pwned` missed the extensionless `assets/xposed_init` entry file, which still
named the old entry class, so LSPosed could not find the module's entry point. That file now points
at `io.github.stashboy.aa.pwned.xposed.XposedInit`, and the module activates and hooks normally.

**If you installed v0.23.14**, update to v0.23.15, make sure AAPwned is enabled in LSPosed with its
scope (Android Auto + System Framework), and reboot. It should then report **Activated**.

## New: "Disable Waze on Android Auto"

A settings toggle. When **on**, Waze keeps its map **on your phone** (visible on-device) while you're
connected to Android Auto, instead of projecting to the car head unit — and it's hidden from the
Android Auto app list. When **off**, Waze projects to Android Auto as normal.

How it works:
- It hooks Android Auto's **own** car-connection provider (`androidx.car.app.connection` /
  `ProjectionStateProvider`) and reports "not connected" **to Waze only** (matched by calling
  package). Waze therefore behaves as if there's no car and runs on the phone. Every other app still
  sees the true "connected" state.
- A second hook drops Waze from Android Auto's app discovery so it doesn't appear on the car screen.

Why it's robust:
- It uses only stable Android framework APIs plus the `com.waze` package name. **No Waze code is
  hooked and no Waze offsets are used**, so Waze app updates won't break it.
- The toggle is read live — once the module is loaded, flipping it on/off takes effect without a
  reboot (reconnect / relaunch Waze to re-evaluate).

## Install

This updates the existing AAPwned package in place (`aapwned-0.23-17.8-r19.apk`). After installing,
reboot so the updated Android Auto hook loads, then in AAPwned flip **Disable Waze on Android Auto**
and press **Save** if you want the feature on.

## Credits

AAPwned is a fork of **[Nitsuya/AADisplay](https://github.com/Nitsuya/AADisplay)** — all credit for
the original module goes to Nitsuya. Credit is kept here in the repo and README, never in the
installed package listing. Bundled template/helper code from duzhaokun123 and QAuxiliary (cinit) is
retained under their respective licenses.

versionName `0.23#17.8-r19` · versionCode `3034`
